# Understanding Embeddings

### Introduction

Embeddings are one of the most useful building blocks in AI engineering. They power semantic search, retrieval-augmented generation (RAG), recommendations, clustering, deduplication, and classification. If you build anything that has to find "content that means the same thing" rather than "content with the same words", you will use embeddings.

This article explains what embeddings are, how similarity is measured, how to generate and store them, and the practical choices you need to make when you use them in production.

---

### 1. What Is an Embedding?

An embedding is a list of numbers (a vector) that represents the meaning of a piece of data. An embedding model takes an input, such as a sentence, and returns a fixed-length vector, for example 384, 1024, or 1536 numbers.

```text
"How do I reset my password?"  ->  [0.021, -0.113, 0.087, ..., 0.042]   (1536 numbers)
```

The key property is that **similar meanings produce nearby vectors**. These two sentences share almost no words, but their embeddings are close together:

- "How do I reset my password?"
- "I forgot my login credentials."

This sentence is about something else, so its embedding is far away from both:

- "What are your store opening hours?"

You can think of an embedding model as placing every piece of text at a point in a high-dimensional "meaning space". Each individual number has no human-readable meaning on its own. What matters is the distance between points.

---

### 2. Why Embeddings Matter

Traditional keyword search matches exact words. It fails when the user and the document use different words for the same idea, such as "car" and "automobile", or "refund" and "money back". Embeddings capture meaning, so they handle synonyms, paraphrases, and even different languages with multilingual models.

**Common use cases:**

- **Semantic search**: Find documents that match the meaning of a query.
- **RAG**: Retrieve relevant chunks of your documents and add them to an LLM's context.
- **Recommendations**: Suggest items similar to what a user liked.
- **Clustering and topic discovery**: Group support tickets, reviews, or articles by theme.
- **Deduplication**: Detect near-duplicate content.
- **Classification**: Use embeddings as input features for a simple classifier, such as logistic regression.
- **Anomaly detection**: Flag inputs that are far from everything seen before.

---

### 3. How Embedding Models Are Trained

Modern text embedding models are usually transformer encoders, similar in structure to BERT. The model reads the input tokens, produces a vector for each token, and then combines them (for example, by averaging, called mean pooling) into a single vector for the whole input.

Most models are trained with **contrastive learning**:

1. Collect pairs of texts that belong together, such as a question and its answer, or a title and its article.
2. Train the model so that matching pairs end up close together.
3. At the same time, push non-matching texts (negatives) far apart.

After training on millions of such pairs, the model learns a space where distance reflects meaning. This is also why an embedding model works best on data similar to what it was trained on, and why domain-specific or fine-tuned embedding models can outperform general ones.

---

### 4. Measuring Similarity

Once you have vectors, you compare them with a similarity or distance metric.

**Cosine similarity** measures the angle between two vectors, ignoring their length. It ranges from -1 to 1, where higher means more similar. It is the most common choice for text embeddings.

```text
cosine_similarity(a, b) = (a · b) / (||a|| * ||b||)
```

**Dot product** multiplies matching elements and adds them up. If vectors are **normalized** (scaled to length 1), the dot product equals cosine similarity and is faster to compute. Many embedding APIs return normalized vectors for this reason.

**Euclidean distance (L2)** measures the straight-line distance between two points. Lower means more similar. For normalized vectors, it ranks results in the same order as cosine similarity.

**Rule of thumb:** Use the metric your embedding model's documentation recommends. If the vectors are normalized, cosine similarity and dot product give the same ranking.

A simple example in Python with `numpy`:

```python
import numpy as np

def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))

a = np.array([0.2, 0.8, 0.1])
b = np.array([0.25, 0.75, 0.05])
c = np.array([0.9, -0.1, 0.4])

print(cosine_similarity(a, b))  # close to 1.0: similar
print(cosine_similarity(a, c))  # much lower: different
```

---

### 5. Generating Embeddings

You can call a hosted embedding API or run an open-source model yourself.

#### Using a Hosted API

Hosted APIs are the quickest way to start. This example uses the OpenAI Python SDK:

```python
from openai import OpenAI

client = OpenAI()  # reads OPENAI_API_KEY from the environment

texts = [
    "How do I reset my password?",
    "I forgot my login credentials.",
    "What are your store opening hours?",
]

response = client.embeddings.create(
    model="text-embedding-3-small",
    input=texts,
)

vectors = [item.embedding for item in response.data]
print(len(vectors), len(vectors[0]))  # 3 vectors, 1536 dimensions each
```

Other hosted options include Voyage AI (recommended by Anthropic, since Anthropic does not offer its own embedding model), Cohere Embed, Google Gemini embeddings, and embedding models on Amazon Bedrock, Google Cloud Vertex AI, and Azure.

#### Running an Open-Source Model

The `sentence-transformers` library makes it easy to run open models locally:

```python
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")  # small and fast, 384 dimensions

texts = [
    "How do I reset my password?",
    "I forgot my login credentials.",
    "What are your store opening hours?",
]

embeddings = model.encode(texts, normalize_embeddings=True)
similarities = model.similarity(embeddings, embeddings)
print(similarities)
```

Running models yourself avoids per-token costs and keeps data on your own infrastructure. The trade-off is that you need to manage compute, especially GPUs for large volumes.

---

### 6. Building a Simple Semantic Search

The basic flow has two phases.

**Indexing (done ahead of time):**

1. Split documents into chunks.
2. Generate an embedding for each chunk.
3. Store each chunk's text, embedding, and metadata.

**Querying (done per request):**

1. Embed the user's query with **the same model**.
2. Find the stored chunks with the most similar embeddings.
3. Return the top results, or pass them to an LLM in a RAG system.

A minimal in-memory version:

```python
import numpy as np
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")

documents = [
    "To reset your password, click 'Forgot password' on the login page.",
    "Our stores are open from 9 AM to 9 PM, Monday to Saturday.",
    "Refunds are processed within 5 to 7 business days.",
]

doc_vectors = model.encode(documents, normalize_embeddings=True)

def search(query: str, top_k: int = 2):
    query_vector = model.encode(query, normalize_embeddings=True)
    scores = doc_vectors @ query_vector  # dot product = cosine for normalized vectors
    best = np.argsort(scores)[::-1][:top_k]
    return [(documents[i], float(scores[i])) for i in best]

for text, score in search("I can't log in to my account"):
    print(f"{score:.3f}  {text}")
```

The password document ranks first even though the query never uses the word "password".

---

### 7. Storing Embeddings in a Vector Database

An in-memory array works for a few thousand items. For larger collections, use a vector database or a vector extension for your existing database.

Vector databases use **approximate nearest neighbor (ANN)** indexes, such as HNSW (Hierarchical Navigable Small World) or IVF (Inverted File Index), to find similar vectors quickly without comparing the query against every stored vector. They trade a small amount of accuracy for a large gain in speed.

**Popular options:**

- `pgvector`: A PostgreSQL extension. A strong default if you already use Postgres.
- `Qdrant`, `Weaviate`, `Milvus`, and `Chroma`: Open-source vector databases.
- `Pinecone`: A fully managed vector database.
- `OpenSearch` and `Elasticsearch`: Search engines with vector support, useful for hybrid search.

An example with `pgvector`:

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE documents (
    id        BIGSERIAL PRIMARY KEY,
    content   TEXT NOT NULL,
    embedding VECTOR(384) NOT NULL
);

-- HNSW index for fast cosine-distance search
CREATE INDEX ON documents USING hnsw (embedding vector_cosine_ops);

-- Find the 5 closest documents to a query embedding
-- (<=> is cosine distance, so smaller means more similar)
SELECT id, content, 1 - (embedding <=> $1) AS similarity
FROM documents
ORDER BY embedding <=> $1
LIMIT 5;
```

Store useful **metadata** alongside each vector, such as source, author, date, and access permissions. Metadata filters let you restrict a search, for example "only documents this user is allowed to see", which is essential for security in multi-user systems.

---

### 8. Choosing an Embedding Model

There is no single best model. Consider these factors:

- **Quality on your data**: Public leaderboards such as the Massive Text Embedding Benchmark (MTEB) are a useful starting point, but always test on a sample of your own queries and documents.
- **Dimensions**: More dimensions can capture more detail but cost more storage and slower search. Common sizes range from 384 to 3072.
- **Flexible dimensions**: Some models support Matryoshka representation learning, which lets you shorten vectors (for example, from 1536 to 512) with only a small loss in quality.
- **Maximum input length**: Models have a token limit per input. Text beyond it is truncated or rejected.
- **Language support**: Use a multilingual model if your users or documents span languages.
- **Domain**: Code, legal, medical, and financial text may benefit from specialized models.
- **Cost and latency**: Compare per-token API pricing against the cost of hosting a model yourself.
- **Data privacy**: Self-hosting may be required for sensitive data.

---

### 9. Best Practices and Common Pitfalls

- **Use the same model for documents and queries.** Vectors from different models live in different spaces and cannot be compared.
- **Re-embed everything when you change models.** Plan for this, and store the model name and version alongside each vector.
- **Follow the model's input conventions.** Some models expect a prefix or an input type, such as `query:` versus `passage:`, or separate "query" and "document" modes. Skipping it can noticeably reduce quality.
- **Chunk thoughtfully.** Chunks that are too large blur several topics into one vector. Chunks that are too small lose context. A few hundred tokens with some overlap is a common starting point.
- **Batch your requests.** Embedding many texts per call is faster and cheaper than one at a time.
- **Cache embeddings.** The same text always produces the same vector with the same model, so there is no need to recompute it.
- **Combine with keyword search.** Embeddings can miss exact matches like product codes, names, and error messages. Hybrid search (BM25 plus vectors) usually beats either one alone.
- **Add a reranker for quality.** Retrieve a larger candidate set with embeddings, then reorder it with a reranker model for better precision.
- **Do not treat similarity scores as absolute.** A score of 0.8 means different things for different models. Use scores to rank results, and set thresholds only after testing.
- **Evaluate retrieval.** Build a small set of real queries with known relevant documents and measure metrics such as recall@k and Mean Reciprocal Rank (MRR) whenever you change the model, chunking, or index.

---

### 10. Beyond Text

Embeddings are not limited to text:

- **Image embeddings**: Models such as CLIP place images and text in the same space, enabling "search images with words".
- **Multimodal embeddings**: Newer models embed text, images, and documents (such as PDF pages) together.
- **Code embeddings**: Specialized models help with code search and finding similar functions.
- **Audio embeddings**: Used for speaker identification and audio similarity search.

---

### Summary

- An embedding is a vector that represents the meaning of data, and similar meanings produce nearby vectors.
- Similarity is measured with cosine similarity, dot product, or Euclidean distance.
- You can generate embeddings with hosted APIs or open-source models such as `sentence-transformers`.
- Vector databases and extensions like `pgvector` use ANN indexes to search millions of vectors quickly.
- Choose a model by testing it on your own data, and weigh dimensions, language, domain, cost, and privacy.
- In production, use the same model for queries and documents, chunk carefully, combine with keyword search, and evaluate retrieval quality.

Embeddings are the foundation of retrieval, which makes them one of the first concepts to master on the [Learning Path to Become an AI Engineer](https://donny-nguyen.github.io/2026/07/11/learning-path-to-ai-engineer.html).
