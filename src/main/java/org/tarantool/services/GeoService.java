package org.tarantool.services;

import org.springframework.stereotype.Service;
import org.tarantool.models.rest.CreateNewPinRequest;
import org.tarantool.models.rest.GeoObject;

import java.util.List;

//Вызывает методы StorageService
@Service
public class GeoService {
    private final StorageService storageService;

    public GeoService(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<GeoObject> createNewPin(CreateNewPinRequest request) {
        return this.storageService.createNewPin(request);
    }

    public List<GeoObject> deletePin(String id) {
        return this.storageService.deletePin(id);
    }

    public List<GeoObject> getPinsList(List<Double> rect) {
        return this.storageService.getPinsList(rect);
    }
}