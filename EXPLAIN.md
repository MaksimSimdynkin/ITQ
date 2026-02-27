# EXPLAIN.md

Цель: показать пример поискового запроса и объяснить, какие индексы используются и почему.

## 1) Как трактуется период дат в поиске

Период `dateFrom/dateTo` применяется к полю `documents.created_at`.

## 2) Пример SQL поискового запроса (status + author + период + сортировка + пагинация)

Пример соответствует эндпоинту:  
`GET /api/v1/documents/search?status=...&author=...&dateFrom=...&dateTo=...&page=...&size=...&sort=createdAt,desc`

```sql
SELECT
  d.id,
  d.number,
  d.author,
  d.title,
  d.status,
  d.created_at,
  d.updated_at
FROM documents d
WHERE d.status = 'APPROVED'
  AND d.author = 'Ivan Ivanov'
  AND d.created_at >= '2026-02-01T00:00:00Z'::timestamptz
  AND d.created_at <= '2026-03-01T00:00:00Z'::timestamptz
ORDER BY d.created_at DESC
LIMIT 20 OFFSET 0;
```

## 3) Как получить EXPLAIN (ANALYZE) в PostgreSQL

Подключиться к БД (если Postgres запущен через docker-compose):

```bash
docker exec -it itq-postgres psql -U postgres -d soft
```

Перед замером желательно обновить статистику:

```sql
ANALYZE;
```

Запустить план с фактическим выполнением:

```sql
EXPLAIN (ANALYZE, BUFFERS, VERBOSE)
SELECT
  d.id,
  d.number,
  d.author,
  d.title,
  d.status,
  d.created_at,
  d.updated_at
FROM documents d
WHERE d.status = 'APPROVED'
  AND d.author = 'Ivan Ivanov'
  AND d.created_at >= '2026-02-01T00:00:00Z'::timestamptz
  AND d.created_at <= '2026-03-01T00:00:00Z'::timestamptz
ORDER BY d.created_at DESC
LIMIT 20 OFFSET 0;
```

Примечание:  
Если таблица маленькая (мало строк), PostgreSQL может выбрать `Seq Scan` - это нормально.  
Чтобы увидеть пользу индексов, можно прогнать генератором больше данных (например `N=10000+`) и повторить `EXPLAIN` после `ANALYZE`.

## 4) EXPLAIN (ANALYZE) output

Ниже приведён вывод из PostgreSQL после генерации данных и выполнения `ANALYZE`:

```text
Limit  (cost=0.29..8.31 rows=1 width=57) (actual time=0.029..0.030 rows=1 loops=1)
  Output: id, number, author, title, status, created_at, updated_at
  Buffers: shared hit=4
  ->  Index Scan Backward using idx_documents_author_created_at on public.documents d  (cost=0.29..8.31 rows=1 width=57) (actual time=0.029..0.029 rows=1 loops=1)
        Output: id, number, author, title, status, created_at, updated_at
        Index Cond: (((d.author)::text = 'Ivan Ivanov'::text) AND (d.created_at >= '2026-02-01 04:00:00+04'::timestamp with time zone) AND (d.created_at <= '2026-03-01 04:00:00+04'::timestamp with time zone))
        Filter: ((d.status)::text = 'APPROVED'::text)
        Buffers: shared hit=4
Planning:
  Buffers: shared hit=183
Planning Time: 1.207 ms
Execution Time: 0.051 ms
```

## 5) Пояснение по индексам

В проекте созданы индексы:

- `idx_documents_status_created_at (status, created_at)`
- `idx_documents_author_created_at (author, created_at)`
- `idx_documents_status_id (status, id)` - в основном полезен для воркеров (выборка пачек по статусу по `id`)

Почему эти индексы помогают поиску:

- Фильтр по `status` + период `created_at`:  
  Индекс `(status, created_at)` позволяет быстро найти строки нужного статуса, затем быстро отфильтровать диапазон по `created_at`. В планах это обычно `Index Scan` или `Bitmap Index Scan` по `idx_documents_status_created_at`.
- Фильтр по `author` + период `created_at`:  
  Индекс `(author, created_at)` аналогично ускоряет выборку, если активен фильтр `author`.
- Комбинация фильтров `status` + `author`:  
  В зависимости от статистики и селективности PostgreSQL может выбрать один индекс как основной, второй применить как фильтр, либо использовать `BitmapAnd` двух индексов (`idx_documents_status_created_at` и `idx_documents_author_created_at`).
- Сортировка `ORDER BY created_at DESC`:  
  Индекс `(status, created_at)` может помочь сортировке (например через `Index Scan Backward`), но в некоторых случаях PostgreSQL всё равно может выполнить отдельный `Sort` - это зависит от плана и статистики.
- Почему иногда виден `Seq Scan`:  
  Если таблица очень маленькая, полный проход по таблице может быть дешевле, чем проход через индекс.

