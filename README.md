# Leondemo

## Spring Boot + Kafka + Kafka UI + PostgreSQL + Loki + Grafana + Prometheus мониторинг

## Быстрый старт

### 1) Склонировать репозиторий
```bash
git clone https://github.com/oooximionnn/leondemo.git
```
### 2) Если у вас mac os - раскоментируйте 106 строку в docker-compose.yml

### 3) Перейти в корневую папку проекта leondemo

### 4) Воспользоваться docker-compose, чтобы запустить проект
```bash
docker-compose build --no-cache && docker-compose up
```

[Swagger](http://localhost:8081/swagger-ui/index.html)

[Kafka ui](http://localhost:8080)

[Grafana](http://localhost:3000/dashboards)
