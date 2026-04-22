# AutoShop FileStorageService

Stores file bytes in private MinIO buckets and file metadata in PostgreSQL.

This is a standalone service. Core services should call its REST API or store only `fileId` references; they should not know MinIO buckets, object keys, presigned signatures, or file bytes.

## Implemented

- Spring Boot 3 / Java 17 service on `8083`.
- PostgreSQL metadata schema through Liquibase.
- MinIO client behind an `ObjectStorageService` boundary.
- Startup initialization for private buckets:
  - `car-inspections`
  - `documents`
  - `avatars`
  - `estimates`
- Multipart upload through the service.
- Metadata lifecycle: `PENDING -> AVAILABLE`, `UPLOAD_FAILED`, `DELETED`.
- Service download endpoint.
- Presigned download URL endpoint.
- Owner-based list endpoint for future Core integration.
- Soft delete with idempotent repeated delete.
- Validation for category, owner type, content type, extension, size and unsafe filenames.
- JSON error contract for validation, not found, deleted, conflict and storage unavailable cases.
- Allow-all access policy boundary for later Auth/Core integration.
- MinIO health indicator for Actuator.

## Local Setup

Create local environment variables. Do not commit real credentials:

```bash
cp .env.example .env
```

Edit `.env`, then start dependencies:

```bash
docker compose --env-file .env up -d
```

Load the same environment for the app:

```bash
set -a
source .env
set +a
export FILES_DB_URL=jdbc:postgresql://localhost:5433/files_db
./gradlew bootRun
```

URLs:

- Service: `http://localhost:8083`
- Health: `http://localhost:8083/actuator/health`
- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`

## API

Base path: `/api/files`

Upload:

```http
POST /api/files
Content-Type: multipart/form-data
```

Multipart fields:

- `file`
- `category`
- `ownerType`
- `ownerId`
- `uploadedBy` optional until Auth integration

Other endpoints:

- `GET /api/files/{fileId}`
- `GET /api/files?ownerType=ORDER&ownerId=42&includeDeleted=false&page=0&size=20`
- `GET /api/files/{fileId}/download`
- `POST /api/files/{fileId}/presigned-download-url`
- `DELETE /api/files/{fileId}`

Presigned URL request body is optional:

```json
{
  "ttlSeconds": 900
}
```

## Categories

`category` is the only client-facing storage selector. Clients never send bucket names or object keys.

| Category | Bucket |
|---|---|
| `ORDER_DOCUMENT` | `documents` |
| `ORDER_ESTIMATE` | `estimates` |
| `ORDER_INSPECTION_PHOTO` | `car-inspections` |
| `VEHICLE_PHOTO` | `car-inspections` |
| `VEHICLE_DOCUMENT` | `documents` |
| `CUSTOMER_DOCUMENT` | `documents` |
| `CUSTOMER_AVATAR` | `avatars` |
| `EMPLOYEE_AVATAR` | `avatars` |
| `INVOICE` | `estimates` |
| `REPORT` | `estimates` |

Allowed `ownerType` values:

- `ORDER`
- `VEHICLE`
- `CUSTOMER`
- `CLIENT`
- `EMPLOYEE`
- `PART`
- `PURCHASE_ORDER`
- `SYSTEM`

`CLIENT` is kept as a compatibility owner type for the current Core naming, while `CUSTOMER` matches the broader roadmap language.

## Smoke Scenario

Create a sample file:

```bash
printf 'AutoShop test document\n' > /tmp/autoshop-file.txt
```

Upload:

```bash
curl -s -X POST http://localhost:8083/api/files \
  -F 'category=ORDER_DOCUMENT' \
  -F 'ownerType=ORDER' \
  -F 'ownerId=42' \
  -F 'uploadedBy=employee-1' \
  -F 'file=@/tmp/autoshop-file.txt;type=text/plain'
```

Copy the returned `id`:

```bash
FILE_ID=<returned-id>
```

Verify metadata and owner list:

```bash
curl -s "http://localhost:8083/api/files/${FILE_ID}"
curl -s "http://localhost:8083/api/files?ownerType=ORDER&ownerId=42"
```

Download through the service:

```bash
curl -OJ "http://localhost:8083/api/files/${FILE_ID}/download"
```

Get a presigned download URL:

```bash
curl -s -X POST "http://localhost:8083/api/files/${FILE_ID}/presigned-download-url" \
  -H 'Content-Type: application/json' \
  -d '{"ttlSeconds":900}'
```

Delete and verify download is unavailable:

```bash
curl -i -X DELETE "http://localhost:8083/api/files/${FILE_ID}"
curl -i "http://localhost:8083/api/files/${FILE_ID}/download"
```

Expected final download response: `410` with `FILE_DELETED`.

Repeated delete should still return `204`.

## Core Integration Notes For 23.04

Core should upload files by owner reference:

- Order document: `category=ORDER_DOCUMENT`, `ownerType=ORDER`, `ownerId={orderId}`
- Order inspection photo: `category=ORDER_INSPECTION_PHOTO`, `ownerType=ORDER`, `ownerId={orderId}`
- Vehicle document: `category=VEHICLE_DOCUMENT`, `ownerType=VEHICLE`, `ownerId={vehicleId}`
- Vehicle photo: `category=VEHICLE_PHOTO`, `ownerType=VEHICLE`, `ownerId={vehicleId}`

Core should not call MinIO directly, store permanent MinIO URLs, or duplicate file metadata. The simplest MVP integration is to request files by `ownerType` and `ownerId`.

## Tests

```bash
./gradlew test
```

Current coverage includes bucket/category mapping, object key generation, upload validation, presigned TTL policy, metadata lifecycle and controller contract tests. Full MinIO Testcontainers integration is still a useful follow-up; the manual smoke above covers the real object storage path for now.
