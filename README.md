# ITQ

## 1. Запуск БД

```bash
docker compose up -d
```

Проверить, что Postgres поднялся:

```bash
docker compose ps
```

## 2. Запуск сервиса

```bash
./mvnw -pl itq-impl -am spring-boot:run
```

Сервис стартует на `http://localhost:8080`.

## 3. Запуск генератора

Базовый запуск:

```bash
./mvnw -pl itq-generator -am spring-boot:run
```

Запуск с параметрами (кол-во документов, параллельность и т.д.):

```bash
./mvnw -pl itq-generator -am spring-boot:run -- --generator.baseUrl=http://localhost:8080 --generator.n=100 --generator.parallelism=4 --generator.logEvery=10
```

Если ваш Maven не принимает аргументы после `--`, используйте:

```bash
./mvnw -pl itq-generator -am "-Dspring-boot.run.arguments=--generator.baseUrl=http://localhost:8080 --generator.n=100 --generator.parallelism=4 --generator.logEvery=10" spring-boot:run
```

## 4. Как смотреть прогресс в логах

Искать в логах:

- `SUBMIT-worker`
- `APPROVE-worker`
- `[GEN]`

Пример для PowerShell:

```powershell
Select-String -Path .\service.log -Pattern "SUBMIT-worker|APPROVE-worker|\[GEN\]"
```

## 5. Примеры API (curl / Postman)

Base URL:

```text
http://localhost:8080/api/v1/documents
```

### 5.1 Create

```bash
curl -X POST "http://localhost:8080/api/v1/documents" \
  -H "Content-Type: application/json" \
  -d '{
    "author": "Ivan",
    "title": "Contract-001",
    "initiator": "manual"
  }'
```

### 5.2 Submit (batch)

```bash
curl -X POST "http://localhost:8080/api/v1/documents/submit" \
  -H "Content-Type: application/json" \
  -d '{
    "ids": [1,2,3],
    "initiator": "manual",
    "comment": "submit batch"
  }'
```

### 5.3 Approve (batch)

```bash
curl -X POST "http://localhost:8080/api/v1/documents/approve" \
  -H "Content-Type: application/json" \
  -d '{
    "ids": [1,2,999999],
    "initiator": "manual",
    "comment": "approve batch"
  }'
```

### 5.4 Search

```bash
curl -G "http://localhost:8080/api/v1/documents/search" \
  --data-urlencode "status=APPROVED" \
  --data-urlencode "author=Ivan" \
  --data-urlencode "dateFrom=2026-02-27T00:00:00Z" \
  --data-urlencode "dateTo=2026-02-27T23:59:59Z" \
  --data-urlencode "page=0" \
  --data-urlencode "size=20"
```

Важно: период `dateFrom/dateTo` в `/search` применяется к полю `createdAt`.

### 5.5 Concurrent-check

```bash
curl -X POST "http://localhost:8080/api/v1/documents/1/concurrent" \
  -H "Content-Type: application/json" \
  -d '{
    "threads": 10,
    "attempts": 100,
    "initiator": "load-test",
    "comment": "concurrency check"
  }'
```

