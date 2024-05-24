# Пример настройки мониторинга для приложения

## Настройка проекта
Кпоирование проектв
``` shell
git clone https://github.com/RomanGhost/p2pStock.git
```
Переключение на ветку **monitoring**
``` shell
git checkout monitoring
```

### Запуск проекта в **docker**
Сборка контейнеров 
``` shell
docker-compose bild
```
Запуск проектов в фоне
``` shell
docker-compose up -d
```
### Просмотр запущенных контейнеров
``` shell
docker-compose ps
```
