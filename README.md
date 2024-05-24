# Пример настройки мониторинга для приложения

## Стуктура проекта
- Nginx - веб сервер, перенаправляет трафик на два контейнера с приложенем
- Первый контейнер приложения: spring-boot-app-blue
- Второй контейнер приложения: spring-boot-app-green
- postgres - БД
- prometheus - считывает характеристики для grafana
- grafana - визуальная состовляющая мониторинга

## Получение данных мониторинга
(Из файла compose.yaml)
- Доступ к самому проекту(сайт): _http://localhost_
- Доступ к grafana: _http://localhost:3000_
- Доступ к prometheus: _http://localhost:9090_

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
docker-compose build
```
Запуск проектов в фоне
``` shell
docker-compose up -d
```
### Просмотр запущенных контейнеров
``` shell
docker-compose ps
```
