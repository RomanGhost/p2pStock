# Непрерывный **Blue-green deploy**

## Настройка непрерывного deploy
- Переключиться на ветку **blue-green_deploy**
``` shell
git checkout blue-green_deploy
```

## Запуск непрерывный blue-green deploy: 
- Копировать файл в папку выше
``` shell
cp blue-green_deploy.sh ../blue-green_deploy.sh
```
- Перейти в папку выше
``` shell
cd ..
```
- Запустить скрипт
``` shell
sh blue-green_deploy.sh
```
