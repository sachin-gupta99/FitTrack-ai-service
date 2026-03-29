CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE IF NOT EXISTS vector_store (
    id TEXT PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(768)
);


CREATE INDEX IF NOT EXISTS idx_vector_store_embedding ON vector_store USING HNSW(embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_vector_metadata ON vector_store USING GIN (metadata);


