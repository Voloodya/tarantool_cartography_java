package org.tarantool.services;

import org.tarantool.models.rest.CreateNewPinRequest;
import org.tarantool.models.rest.GeoObject;

import java.util.List;

// Интерфейс сервиса, который содержит декларативное описание методов для работы с Tarantool
public interface StorageService {
    List<GeoObject> createNewPin(CreateNewPinRequest request); // создание пина
    List<GeoObject> getPinsList(List<Double> rect); // получение списка пинов
    List<GeoObject> deletePin(String id); // удаление пина 
}