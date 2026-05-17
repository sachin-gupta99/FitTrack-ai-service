# FitTrack AI Service

AI microservice for **FitTrack**. Consumes activity / nutrition / user events from RabbitMQ, enriches them with an LLM (calorie & macro estimation), stores embeddings in **pgvector** for retrieval-augmented generation, and exposes an SSE-streamed chat endpoint backed by **Spring AI**.

## What this service does

- **Consumes FitTrack domain events from RabbitMQ**
  - `activity_queue` — estimates calories burned via LLM
  - `nutrition_queue` — estimates calories / macros via LLM
  - `user_queue` — stores basic user profile context
- **Persists enriched entities to MongoDB**
- **Stores searchable embeddings in pgvector** (PostgreSQL) — domain-tagged (`activity`, `nutrition`, `user`)
- **Provides an SSE chat endpoint** with conversation memory + RAG over the vector store
- Registers with **Eureka** as `ai-service` on port **8073**

## Tech stack

- Java **21** · Spring Boot **4.0.2** · Maven
- **Spring AI 2.0.0-M2**
  - Chat: **Groq** via the OpenAI-compatible API (`llama-3.3-70b-versatile` by default)
  - Embeddings: **Google GenAI** (`gemini-embedding-001`, **768** dimensions)
  - Chat memory: JDBC repository (`spring_ai_chat_memory` on Postgres)
  - Vector store: pgvector (`vector_store` table)
- **MongoDB** (Spring Data MongoDB) for enriched domain entities
- **PostgreSQL + pgvector** for embeddings & chat memory
- **RabbitMQ** (Spring AMQP) — single URI from SSM (CloudAMQP-friendly)
- **Eureka client** (Spring Cloud 2025.1.0)
- **AWS SDK v2 — SSM Parameter Store** for runtime secrets

## Configuration — AWS SSM Parameter Store

`src/main/resources/application.yaml` only stores **parameter paths**, not values. The runtime needs AWS credentials with `ssm:GetParameter` for the `/Fittrack/*` namespace (region `ap-south-1`).

| SSM parameter                         | Used for                                    |
| ------------------------------------- | ------------------------------------------- |
| `/Fittrack/rabbitmq/uri`              | Full RabbitMQ URI (e.g. `amqps://user:pass@host/vhost`) |
| `/Fittrack/ai-srvc/groq/apiKey`       | Groq API key (chat model)                   |
| `/Fittrack/ai-srvc/gemini/apiKey`     | Google GenAI API key (embeddings)           |
| `/Fittrack/postgres-db/url`           | JDBC URL for chat-memory + pgvector         |
| `/Fittrack/postgres-db/username`      | Postgres username                           |
| `/Fittrack/postgres-db/password`      | Postgres password                           |
| `/Fittrack/ai-srvc/mongodb/uri`       | MongoDB connection string                   |
| `/Fittrack/ai-srvc/mongodb/database`  | MongoDB database name                       |
| `/Fittrack/accessKey`                 | IAM user access key (used by SSM client)    |
| `/Fittrack/secretKey`                 | IAM user secret key                         |

Provide AWS credentials via the standard chain (env vars, `~/.aws/credentials`, instance role, etc.). Other defaults: `server.port=8073`, `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/`.

## Runtime dependencies

- **RabbitMQ** reachable via the SSM-provided URI (TLS auto-detected from `amqps://` scheme)
- **MongoDB**
- **PostgreSQL** with the **pgvector** extension installed (`CREATE EXTENSION IF NOT EXISTS vector;`)
- **Eureka server** at `http://localhost:8761/eureka/`
- **Groq** API account + key
- **Google AI Studio** API key for Gemini embeddings

## Repository layout

```
src/main/java/com/fitness/ai_service
├── AiServiceApplication.java
├── config/                       # ChatClient, EmbeddingModel, RabbitMQ, Mongo, Postgres, SSM, IAM beans
├── controller/                   # ChatController, RecommendationController
├── dto/                          # ChatRequest, ChatHistoryResponse, GlobalResponseDTO, ErrorDTO, …
├── exceptions/                   # GlobalExceptionHandler
├── model/                        # Activity, Nutrition, User, Recommendation
├── repository/                   # Spring Data MongoDB repositories
└── service/                      # ChatService, EmbeddingService, *ListenerService, ParameterStoreService

src/main/resources
├── application.yaml
├── prompts/
│   ├── calories_prompt.st        # estimate calories burned for an activity
│   ├── nutrition_prompt.st       # estimate calories/macros for a nutrition entry
│   └── system_prompt_for_rag.st  # chat system prompt; receives {{context}} from RAG
└── init/schema.sql               # chat memory schema (auto-applied at startup)
```

## Running locally

### Build

```powershell
./mvnw.cmd -DskipTests package
```

### Run

```powershell
# AWS credentials must be on the default chain
./mvnw.cmd spring-boot:run
```

The service starts on `http://localhost:8073` and registers with Eureka.

## API

Base path: `/api/ai`

### `POST /api/ai/chat` — Server-Sent Events

Streams the assistant response as `text/event-stream`. Consumes `application/json`.

Request body (`ChatRequest`):

```json
{
  "id": "optional",
  "type": 1,
  "message": {
    "userId": 123,
    "content": "How am I doing this week?"
  },
  "timestamp": "2026-05-15T12:00:00Z"
}
```

- `userId` is the **conversation id** for chat memory.
- The system prompt is built with RAG context retrieved from pgvector across the user's `activity`, `nutrition`, and `user` domains.

### `GET /api/ai/chat/history/{userId}`

Returns chat history for a user from `spring_ai_chat_memory`, wrapped in `GlobalResponseDTO`:

```json
{
  "data": [
    {
      "conversationId": "123",
      "message": "...",
      "type": 1,
      "timestamp": "2026-05-15T12:00:00.000+00:00"
    }
  ],
  "message": "Chat history retrieved successfully",
  "statusCode": 200
}
```

### `GET /api/ai/user/{userId}/recommendations` · `GET /api/ai/activity/{activityId}/recommendations`

Placeholders for upcoming recommendation endpoints.

## Messaging (RabbitMQ)

Listens to (queue names from `rabbitmq.queue.*` in `application.yaml`):

- **`activity_queue`** — body: JSON `Activity`; header `action` ∈ {`create`,`update`,`delete`}. On create/update: LLM computes calories burned → save to Mongo → store embedding (`domain=activity`).
- **`nutrition_queue`** — body: JSON `Nutrition`; header `action` ∈ {`create`,`update`,`delete`}. On create/update: LLM computes macros → save to Mongo → store embedding (`domain=nutrition`).
- **`user_queue`** — body: JSON `User`. Stores embedding with `domain=user`.

The RabbitMQ `ConnectionFactory` is built from a single URI (`/Fittrack/rabbitmq/uri`). TLS is enabled automatically when the scheme is `amqps://`.

## Development notes

- pgvector schema initialization is **enabled** (`spring.ai.vectorstore.pgvector.initialize-schema=true`). The `vector` extension itself must be installed manually in the target database.
- Chat memory schema is initialized from `classpath:init/schema.sql` on every startup.
- Embedding dimensions are **768** (Gemini `gemini-embedding-001`). If you swap the embedding model, the `vector_store` column type must match.
- CORS is open (`origins = "*"`) on the chat controller — tighten before production.
- The service auto-detects `amqps://` and enables TLS for RabbitMQ; works out-of-the-box with CloudAMQP.

## Related services

- [`FitTrack-eureka-service`](https://github.com/sachin-gupta99/FitTrack-eureka-service) — service registry
- [`FitTrack-user-service`](https://github.com/sachin-gupta99/FitTrack-user-service) — auth + publishes `user_queue`
- [`FitTrack-tracking-service`](https://github.com/sachin-gupta99/FitTrack-tracking-service) — publishes `activity_queue` / `nutrition_queue`
- [`FitTrack-Host-UI`](https://github.com/sachin-gupta99/FitTrack-Host-UI) + [`FitTrack-Content-MFE`](https://github.com/sachin-gupta99/FitTrack-Content-MFE) — frontend (FitoAI page consumes `/api/ai/chat`)

## License

See `LICENSE`.
