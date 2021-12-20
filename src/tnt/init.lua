box.cfg({listen="127.0.0.1:3301"})
--Создание пользователя 'storage'
box.schema.user.create('storage', {password='passw0rd', if_not_exists=true})
--Наделения созданного пользователя правами
box.schema.user.grant('storage', 'super', nil, nil, {if_not_exists=true})
-- Конфигурирование модуля 'msgpack'
require('msgpack').cfg{encode_invalid_as_nil = true}
-- fiber
fiber = require('fiber')