package org.tarantool.config;


import io.tarantool.driver.DefaultTarantoolTupleFactory;
import io.tarantool.driver.TarantoolClientConfig;
import io.tarantool.driver.TarantoolClusterAddressProvider;
import io.tarantool.driver.TarantoolServerAddress;
import io.tarantool.driver.ClusterTarantoolClient;
import io.tarantool.driver.ClusterTarantoolTupleClient;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolTupleFactory;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.auth.SimpleTarantoolCredentials;
import io.tarantool.driver.mappers.DefaultMessagePackMapperFactory;
import io.tarantool.driver.retry.RetryingTarantoolTupleClient;
import io.tarantool.driver.retry.TarantoolRequestRetryPolicies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutionException;

//Файл конфигурации подключения к Tarantool
@Configuration
@PropertySource(value="classpath:application-tarantool.properties", encoding="UTF-8")
public class TarantoolConfig {

    // Создаем "фабрику" таплов (кортежи - строки таблицы)
    private static final TarantoolTupleFactory tupleFactory =
            new DefaultTarantoolTupleFactory(
                    DefaultMessagePackMapperFactory
                            .getInstance()
                            .defaultComplexTypesMapper()
            );

    // Bean, который возвращает Tarantool клиент, с помощью которого мы можем ходить в tarantool
    @Bean
    public TarantoolClient tarantoolClient(
            @Value("${tarantool.nodes}") String nodes,
            @Value("${tarantool.username}") String username,
            @Value("${tarantool.password}") String password) {

        // Создание кред (реквизитов для подключения к tarantool) 
        SimpleTarantoolCredentials credentials = new SimpleTarantoolCredentials(username, password);

        // Конфигурация клиента
        TarantoolClientConfig config = new TarantoolClientConfig.Builder()
                .withCredentials(credentials)
                .withRequestTimeout(1000*10)
                .build();

        // Генерация списка адресов из списка nodes к которым можно подключаться
        TarantoolClusterAddressProvider provider = new TarantoolClusterAddressProvider() {
            @Override
            public Collection<TarantoolServerAddress> getAddresses() {
                ArrayList<TarantoolServerAddress> addresses = new ArrayList<>();

                for(String node: nodes.split(",")) {
                    String[] address = node.split(":");
                    addresses.add(new TarantoolServerAddress(address[0], Integer.parseInt(address[1])));
                }

                return addresses;
            }
        };

        // Создание клиента

        ClusterTarantoolClient clusterClient = new ClusterTarantoolTupleClient(config, provider);
        // Обертка над клиентом (делает несколько попыток подключения, если не успешно выводит сообщение)
        RetryingTarantoolTupleClient client = new RetryingTarantoolTupleClient(
                clusterClient,
                TarantoolRequestRetryPolicies.byNumberOfAttempts(
                        10, e -> e.getMessage().contains("Unsuccessfull attempt")
                ).build());

        // Конфигурация Tarantool
        // Перенесено в файл init.lua
        //try {

            // Создаём спейс (space)

            //Map<String, Object> options = new HashMap<>();
            //options.put("if_not_exists", true);
            //Call method create. client.call() возвращает future (обещание создать, когда дойдет до этого время), method get() return result right now
           //client.call("box.schema.create_space", "geo", options).get();

            //List<Map<String, String>> formatOptions = new ArrayList<>();
            // Передача списка опций (полей (3))
            //for(int i = 0; i < 3; i++) {
                //Map<String, String> field = new HashMap<>();

                //switch (i) {
                    //case 0:
                       // field.put("name", "id");
                       // field.put("type", "string");
                       // break;
                    //case 1:
                        //field.put("name", "coordinates");
                        //field.put("type", "array");
                        //break;
                    //case 2:
                        //field.put("name", "comment");
                        //field.put("type", "string");
                        //break;
                //}
                //formatOptions.add(field);
           // }

            // Создаем формат спейса через tupl

            //TarantoolTuple format = tupleFactory.create(formatOptions);
            //client.call("box.space.geo:format", format).get();

            // Создание первичного индекса

            //List<String> parts = new ArrayList<>();
            //parts.add("id");
            //options.put("parts", parts);
            //client.call("box.space.geo:create_index", "primary", options).get();

            // Создание вторичного индекса

            //options.remove("parts");
            //parts.remove(0);
            //parts.add("coordinates");
            //options.put("parts", parts);
            // Для ГЕО операции используется индекс RTREE
            //options.put("type", "RTREE");
            //options.put("unique", false);
            //client.call("box.space.geo:create_index", "geoidx", options).get();
        //}
        //catch(InterruptedException | ExecutionException e) {
           // e.printStackTrace();
       // }

        return client;
    }
}