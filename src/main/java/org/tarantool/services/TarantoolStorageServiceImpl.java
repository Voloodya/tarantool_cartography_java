package org.tarantool.services;

import io.tarantool.driver.DefaultTarantoolTupleFactory;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.TarantoolTupleFactory;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.mappers.DefaultMessagePackMapperFactory;
import org.springframework.stereotype.Service;
import org.tarantool.models.rest.CreateNewPinRequest;
import org.tarantool.models.rest.GeoObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class TarantoolStorageServiceImpl implements StorageService {
    private final TarantoolClient tarantoolClient;
    //Генерация фабрики tupls
    private static final TarantoolTupleFactory tupleFactory =
            new DefaultTarantoolTupleFactory(
                    DefaultMessagePackMapperFactory
                            .getInstance()
                            .defaultComplexTypesMapper()
            );

    //Объект для работы с объектами Space (егзды)
    private final TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>> geoSpace;

    public TarantoolStorageServiceImpl(TarantoolClient tarantoolClient) {
        this.tarantoolClient = tarantoolClient;
        // Получение интерфейса для работы со Space
        this.geoSpace = tarantoolClient.space("geo");
    }

    private List<GeoObject> getGeoObjects(List<Object> geos) {
        List<GeoObject> Geos = new ArrayList<>();
        for(Object tupleItem : geos) {
            GeoObject item = new GeoObject();
            TarantoolTuple tuple = (TarantoolTuple) tupleItem;
            item.setId(tuple.getString(0));

            for(Object coord : tuple.getList(1)) {
                item.addCoordinatesItem(((Float)coord).doubleValue());
            }
            item.setComment(tuple.getString(2));

            Geos.add(item);
        }

        return Geos;
    }

    @Override
    public List<GeoObject> createNewPin(CreateNewPinRequest request) {
        String uuid = UUID.randomUUID().toString(); // генерация id места

        List<GeoObject> geos = null;

        // создание тапла для вставки объекта
        TarantoolTuple geo = tupleFactory.create(uuid, request.getCoordinates(), request.getComment());

        try {
            geos = getGeoObjects(new ArrayList<>(Arrays.asList(geoSpace.insert(geo).get().toArray())));
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return geos;
    }

    @Override
    public List<GeoObject> getPinsList(List<Double> rect) {
        List<GeoObject> geos = null;

        try {
            //Выбор объектов лежащих внутри прямоугольника rect
            geos = getGeoObjects(new ArrayList<>(Arrays.asList(
                    geoSpace
                            .select(Conditions.indexLessOrEquals("geoidx", rect).withLimit(1000))
                            .get().toArray()))
            );
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return geos;
    }

    @Override
    public List<GeoObject> deletePin(String id) {
        List<GeoObject> geos = null;

        // Создание списка условий фильтрации
        List<String> conds = new ArrayList<>();
        conds.add(id);

        try {
            geos = getGeoObjects(new ArrayList<>(Arrays.asList(
                    geoSpace
                            .delete(Conditions.indexEquals("primary", conds))
                            .get().toArray()))
            );
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return geos;
    }
}