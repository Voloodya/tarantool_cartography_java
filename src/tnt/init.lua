box.cfg({listen="127.0.0.1:3301"})
--Создание пользователя 'storage'
box.schema.user.create('storage', {password='passw0rd', if_not_exists=true})
--Наделения созданного пользователя правами
box.schema.user.grant('storage', 'super', nil, nil, {if_not_exists=true})
-- Создание space-ов
box.schema.space.create('geo',{ if_not_exists = true }, {engine = 'memtx'})
box.space.geo:format({
    {name='id', type='string'},
    {name='coordinates', type='array'},
    {name='comment', type='string'},
})
-- Создание индексов
box.space.geo:create_index('primary', {type="TREE", unique=true, parts={1, 'string'}, if_not_exists=true})
box.space.geo:create_index('geoidx', {type="RTREE", unique=false, parts={2, 'array'}, if_not_exists=true})

-- Конфигурирование модуля 'msgpack'
require('msgpack').cfg{encode_invalid_as_nil = true}
-- fiber. Переменная не local -> глобальная - доступна из консоли
fiber = require('fiber')