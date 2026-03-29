# FitTrack AI Service

AI microservice for **FitTrack**. It enriches activity and nutrition events using an LLM, stores embeddings for retrieval-augmented generation (RAG), and exposes chat + history endpoints for an AI coach experience.

## What this service does

- **Consumes FitTrack domain events from RabbitMQ**
  - Activity events: estimates calories burned
  - Nutrition events: estimates calories/macros
  - User events: stores basic user profile context
- **Persists enriched entities to MongoDB**
- **Stores searchable embeddings in PgVector** (PostgreSQL)
- **Provides an AI chat endpoint** backed by **Spring AI + OpenAI**, using RAG over the stored embeddings
- **Stores chat memory** in a JDBC-backed repository (`spring_ai_chat_memory`)
- Registers with **Eureka** as `ai-service`

## Tech stack

- Java **21**
- Spring Boot **4.0.2**
- Spring AI **2.0.0-M2** (OpenAI chat + embeddings)
- MongoDB (Spring Data MongoDB)
- PostgreSQL + **pgvector** (Spring AI vector store)
- RabbitMQ (AMQP)
- Eureka client (Spring Cloud Netflix)

## Repository layout

- `src/main/java/.../controller` – REST controllers
- `src/main/java/.../service` – chat + listeners + embedding logic
- `src/main/resources/prompts` – prompt templates
  - `calories_prompt.st`
  - `nutrition_prompt.st`
  - `system_prompt_for_rag.st`
- `src/main/resources/init/schema.sql` – SQL schema initialization (chat memory)

## Runtime dependencies

You’ll need the following services available:

- **RabbitMQ** (SSL enabled in `application.yaml`)
- **MongoDB**
- **PostgreSQL** with **pgvector** enabled
- (Optional) **Eureka server** (default points to `http://localhost:8761/eureka/`)

## Configuration

This service is configured via environment variables referenced in `src/main/resources/application.yaml`.

Required environment variables:

- `OPENAI_API_KEY` – OpenAI API key used by Spring AI
- `MONGODB_URI` – Mongo connection string (Spring property `spring.data.mongodb.url`)
- `MONGODB_DATABASE` – Mongo database name
- `RABBITMQ_ADDRESSES` – RabbitMQ addresses (e.g. `amqps://user:pass@host:5671/vhost`)
- `DB_URL` – JDBC URL for Postgres (used for chat memory + pgvector)
- `DB_USERNAME` – DB username
- `DB_PASSWORD` – DB password

Useful defaults from `application.yaml`:

- Service port: **8073**
- OpenAI chat model: `gpt-4o-mini`
- Embeddings model: `text-embedding-3-small` (dimensions **1024**)
- PgVector table: `vector_store` (schema initialization is **disabled** by default)

## Running locally

### 1) Build

Use the Maven wrapper (recommended):

```powershell
./mvnw.cmd -DskipTests package
```

### 2) Run

```powershell
$env:OPENAI_API_KEY = "..."
$env:MONGODB_URI = "mongodb://localhost:27017"
$env:MONGODB_DATABASE = "fittrack"
$env:RABBITMQ_ADDRESSES = "amqps://user:pass@localhost:5671"
$env:DB_URL = "jdbc:postgresql://localhost:5432/fittrack"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"

./mvnw.cmd spring-boot:run
```

The service starts on `http://localhost:8073`.

## API

Base path: `/api/ai`

### POST `/api/ai/chat` (Server-Sent Events)
Streams the assistant response as **text/event-stream**.

- **Consumes:** `application/json`
- **Produces:** `text/event-stream`

Request body (`ChatRequest`):

```json
{
  "id": "optional",
  "type": 1,
  "message": {
    "userId": 123,
    "content": "How am I doing this week?"
  },
  "timestamp": "2026-03-14T12:00:00Z"
}
```

Notes:
- `userId` is used as the **conversation id** for chat memory.
- The system prompt uses RAG context from the vector store (activity, nutrition, and user domains).

### GET `/api/ai/chat/history/{userId}`
Returns chat history for a user from `spring_ai_chat_memory`.

Response is wrapped in `GlobalResponseDTO`:

```json
{
  "data": [
    {
      "conversationId": "123",
      "message": "...",
      "type": 1,
      "timestamp": "2026-03-14T12:00:00.000+00:00"
    }
  ],
  "message": "Chat history retrieved successfully",
  "statusCode": 200
}
```

### GET `/api/ai/user/{userId}/recommendations`
Placeholder endpoint for user recommendations.

### GET `/api/ai/activity/{activityId}/recommendations`
Placeholder endpoint for activity-based recommendations.

## Messaging (RabbitMQ)

This service listens to these queues (see `rabbitmq.queue.*` in `application.yaml`):

- `activity_queue`
  - Header `action`: `create` | `update` | `delete`
  - Body: JSON representation of `Activity`
  - On create/update: calls the LLM to compute calories burned, saves to Mongo, and stores an embedding with domain `activity`.

- `nutrition_queue`
  - Header `action`: `create` | `update` | `delete`
  - Body: JSON representation of `Nutrition`
  - On create/update: calls the LLM to compute macros, saves to Mongo, and stores an embedding with domain `nutrition`.

- `user_queue`
  - Body: JSON representation of `User`
  - Stores an embedding with domain `user`.

## Prompt templates

Prompt templates live in `src/main/resources/prompts`.

- `calories_prompt.st` – used to estimate calories burned for an activity
- `nutrition_prompt.st` – used to estimate calories/macros for a nutrition entry
- `system_prompt_for_rag.st` – used for chat; receives a `{{context}}` variable built from retrieved documents

## Development notes

- PgVector schema initialization is disabled (`spring.ai.vectorstore.pgvector.initialize-schema: false`).
  Make sure the `vector_store` table exists and pgvector is installed in your Postgres instance.
- Chat memory schema is initialized from `classpath:init/schema.sql` on startup.
- CORS is open (`origins = "*"`) on the chat controller.

## License

See `LICENSE`.
