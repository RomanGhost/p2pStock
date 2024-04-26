CREATE OR REPLACE FUNCTION cascade_delete_request()
RETURNS TRIGGER AS $$
BEGIN
    -- Удаляем записи из таблицы "request", которые связаны с удаляемой записью из таблицы "card"
    DELETE FROM request WHERE card_id = OLD.id;

    -- Удаляем удаляемую запись из таблицы "card"
    DELETE FROM card WHERE id = OLD.id;

    -- Возвращаем OLD, чтобы указать, что операция удаления должна быть продолжена
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Создаем триггер на каскадное удаление
CREATE TRIGGER cascade_delete_card_trigger
BEFORE DELETE ON card
FOR EACH ROW
EXECUTE FUNCTION cascade_delete_request();
