package org.tarantool.controllers;

import org.apache.tomcat.util.json.JSONParser;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tarantool.models.rest.CreateNewPinRequest;
import org.tarantool.models.rest.GeoObject;
import org.tarantool.services.GeoService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
public class GeoController {
    private final GeoService geoService;

    GeoController(GeoService geoService) {
        this.geoService = geoService;
    }

    @PostMapping(value = "/new", produces={"application/json"})
    public ResponseEntity<List<GeoObject>> createNewPin(@RequestBody CreateNewPinRequest request) {
        // создаем новый пин
        List<GeoObject> pins = geoService.createNewPin(request);

        // возвращаем его или ответ
        if(pins != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .cacheControl(CacheControl.noCache())
                    .body(pins);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/remove", produces={"application/json"})
    public ResponseEntity<List<GeoObject>> deletePin(@RequestBody GeoObject request) {
        // удаляем пин
        List<GeoObject> pins = geoService.deletePin(request.getId());

        // возвращаем список удалённых пинов или ответ
        if(pins != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .cacheControl(CacheControl.noCache())
                    .body(pins);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/list", produces={"application/json"})
    public ResponseEntity<List<GeoObject>> getPinsList(@RequestParam String rect) {

        // парсим querystring
        List<Double> coordinates = new ArrayList<>();
        JSONParser parser = new JSONParser(rect);

        try {
            List<Object> coords = parser.parseArray();
            for(Object coord: coords) {
                coordinates.add(((BigDecimal) coord).doubleValue());
            }
        }
        catch (Exception e) {
            for(int i = 0; i < 4; i++) {
                coordinates.add(0.);
            }

            e.printStackTrace();
        }

        // получаем список пинов
        List<GeoObject> pins = geoService.getPinsList(coordinates);

        // возвращаем его или ответ
        if(pins != null) {

            return ResponseEntity.status(HttpStatus.OK)
                    .cacheControl(CacheControl.noCache())
                    .body(pins);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}