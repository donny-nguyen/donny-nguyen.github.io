# Vector Search: Similarity Metrics, ANN Indexes, and Metadata Filtering

### Introduction

Once your documents are parsed, chunked, and embedded, you have a collection of vectors. The next question is how to find the vectors that are closest to a query vector, quickly, among thousands or millions of others. This is **vector search**, and it is the retrieval engine at the heart of every RAG system, semantic search feature, and recommendation engine.

Comparing a query against every stored vector is simple and exact, but it becomes too slow as the collection grows. Vector databases solve this with **approximate nearest neighbor (ANN)** indexes, which find almost the same results in a fraction of the time. Real applications also need to combine similarity with ordinary conditions, such as "only documents from the HR department" or "only documents this user is allowed to see", which is called **metadata filtering**.

This article covers how similarity metrics work in practice, how the main ANN indexes (HNSW, IVF, and product quantization) work and how to tune them, and how to filter by metadata without breaking search quality, with Python and SQL examples.

---

### 1. Where Vector Search Fits in a RAG Pipeline

A RAG system has two halves:

**Indexing (done ahead of time):**

1. Parse and chunk documents (see [Document Processing for RAG](https://donny-nguyen.github.io/2026/11/21/document-processing-for-rag.html)).
2. Embed each chunk (see [Understanding Embeddings](https://donny-nguyen.github.io/2026/11/09/embeddings.html)).
3. Store each vector with its text and metadata, and build an index over the vectors.

**Querying (done per request):**

1. Embed the user's question with the same model.
2. **Search the index for the nearest vectors, applying any metadata filters.**
3. Pass the top results to the LLM, optionally after reranking.

This article is about step 3 of indexing and step 2 of querying. The choices you make here decide how fast retrieval is, how much memory it uses, and how often the right chunk is missed.

---

### 2. Exact Search: The Baseline

The simplest way to search is **exact k-nearest neighbor (kNN)** search, also called brute-force or flat search. Compare the query with every vector, sort by score, and return the top k:

```python
import numpy as np

rng = np.random.default_rng(42)

# 100,000 normalized vectors with 384 dimensions
vectors = rng.standard_normal((100_000, 384)).astype("float32")
vectors /= np.linalg.norm(vectors, axis=1, keepdims=True)

def exact_search(query: np.ndarray, k: int = 5) -> tuple[np.ndarray, np.ndarray]:
    scores = vectors @ query  # dot product = cosine similarity for normalized vectors
    top = np.argpartition(-scores, k)[:k]  # unordered top k, faster than a full sort
    top = top[np.argsort(-scores[top])]  # order the top k by score
    return top, scores[top]

query = vectors[0]
ids, scores = exact_search(query)
print(ids, scores)
```

Exact search has real advantages:

- **Perfect recall**: It always returns the true nearest neighbors.
- **No index to build or tune**: New vectors are searchable immediately.
- **Filters are easy**: Apply the filter first, then compare only the remaining vectors.

The problem is cost. Each query does N × d multiplications, where N is the number of vectors and d is the number of dimensions. For 10 million vectors with 1,024 dimensions, that is about 10 billion operations per query. Latency grows linearly with the collection, and so does the CPU cost per query.

**Rule of thumb:** Exact search is fine up to roughly tens of thousands to a few hundred thousand vectors, especially on a GPU or with a heavily optimized library such as FAISS. It is also the right choice when a filter leaves only a small number of candidates. Beyond that, use an ANN index.

---

### 3. Similarity Metrics in Practice

[Understanding Embeddings](https://donny-nguyen.github.io/2026/11/09/embeddings.html) introduced the three common metrics. Here is a quick recap, with the details that matter when you configure an index.

| Metric | Formula | Better when | Typical use |
|---|---|---|---|
| Cosine similarity | (a · b) / (‖a‖ ‖b‖) | Higher | Most text embedding models |
| Dot product (inner product) | a · b | Higher | Normalized vectors, some recommendation models |
| Euclidean distance (L2) | ‖a − b‖ | Lower | Image features, some older models |

#### Normalization Makes the Metrics Equivalent

If every vector has length 1, the three metrics produce the **same ranking**:

- Cosine similarity equals the dot product, because the lengths in the denominator are 1.
- Squared Euclidean distance equals 2 − 2 × cosine similarity, so the smallest distance is always the highest similarity.

```python
import numpy as np

a = np.array([0.6, 0.8])  # length 1
b = np.array([0.8, 0.6])  # length 1

cosine = a @ b
squared_l2 = np.sum((a - b) ** 2)

print(cosine)          # 0.96
print(squared_l2)      # 0.08
print(2 - 2 * cosine)  # 0.08, the same value
```

This is why many systems normalize every vector once, at insert time, and then use the dot product, which is the cheapest to compute.

#### When the Choice Matters

- **Unnormalized vectors**: The dot product rewards longer vectors. Some models are trained this way on purpose (for example, where length encodes popularity or confidence), so follow the model's documentation.
- **Distance versus similarity**: Databases often expose distances, even for cosine. In `pgvector`, `<=>` returns **cosine distance** (1 − cosine similarity), `<#>` returns the **negative** inner product, and `<->` returns L2 distance. All of them sort ascending, so `ORDER BY ... LIMIT k` returns the most similar rows.
- **The index metric must match the query metric**: An index is built for one metric. If you create a `pgvector` index with `vector_cosine_ops` and query with `<->`, the index is not used and the database falls back to a full scan.
- **Binary vectors**: Quantized binary embeddings (Section 7) are compared with **Hamming distance**, the number of bits that differ, which is extremely fast.

**Rule of thumb:** Normalize your vectors unless the model says otherwise, use the metric the model recommends, and configure the index and the query with the same metric.

---

### 4. Approximate Nearest Neighbor (ANN) Search

ANN indexes avoid comparing the query with every vector. They organize vectors ahead of time so that a search only looks at a small, promising part of the collection. The price is that they sometimes miss a true nearest neighbor.

Every ANN index balances three things:

- **Recall**: The fraction of the true top-k neighbors that the index returns. If exact search returns 10 results and the index returns 9 of them, recall@10 is 0.9.
- **Speed**: Query latency and queries per second (QPS).
- **Memory and build cost**: How much RAM or disk the index needs, and how long it takes to build and update.

You can usually move along this trade-off with a single search-time parameter: look at more candidates for higher recall, or fewer for lower latency. Production systems typically target recall@10 between 0.95 and 0.99, which is usually invisible in final answer quality, especially when a reranker follows.

The three building blocks you will meet in almost every vector database are:

- **HNSW**: A graph you navigate toward the query.
- **IVF**: Clusters, where you only search the clusters closest to the query.
- **Product quantization (PQ)**: Compression, so vectors take far less memory and compare faster.

The examples below use **FAISS**, Meta's open-source library for vector search, which implements all three:

```bash
pip install faiss-cpu numpy
```

---

### 5. HNSW (Hierarchical Navigable Small World)

HNSW is the most widely used ANN index today. It is the default in `pgvector`, Qdrant, Weaviate, Milvus, Chroma, OpenSearch, and Elasticsearch.

#### How It Works

HNSW builds a **graph** where each vector is a node connected to some of its nearest neighbors. Searching the graph is like asking for directions: start somewhere, move to whichever neighbor is closer to the query, and repeat until no neighbor is closer.

A single graph can be slow to cross, so HNSW stacks several layers, similar to a skip list:

- **The top layer** contains very few nodes with long-range links, like highways between cities.
- **Each lower layer** contains more nodes and shorter links.
- **The bottom layer** (layer 0) contains every vector.

A search enters at the top layer, greedily moves toward the query, then drops down a layer and continues with finer steps. On the bottom layer, it keeps a list of the best candidates found so far, explores their neighbors, and returns the top k.

```text
Layer 2:  A ------------------------------- F             (few nodes, long links)
          |                                 |
Layer 1:  A -------- C ---------- E ------- F             (more nodes)
          |          |            |         |
Layer 0:  A -- B -- C -- D -- E -- F -- G -- H -- ...     (all vectors, short links)
```

#### Parameters

| Parameter | Set at | Effect | Typical values |
|---|---|---|---|
| `M` | Build time | Links per node. Higher improves recall but uses more memory and slows building. | 16 to 64 |
| `efConstruction` | Build time | Candidate list size while building. Higher builds a better graph, more slowly. | 64 to 500 |
| `efSearch` | Query time | Candidate list size while searching. Higher improves recall but increases latency. Must be at least k. | 40 to 400 |

`efSearch` is the main knob you tune in production, because it can change per query without rebuilding anything.

#### Example with FAISS

```python
import faiss
import numpy as np

d = 384
rng = np.random.default_rng(42)
vectors = rng.standard_normal((100_000, d)).astype("float32")
faiss.normalize_L2(vectors)  # normalize in place, so inner product = cosine

# Exact index, used to measure recall
flat = faiss.IndexFlatIP(d)
flat.add(vectors)

# HNSW index with M = 32, using inner product
hnsw = faiss.IndexHNSWFlat(d, 32, faiss.METRIC_INNER_PRODUCT)
hnsw.hnsw.efConstruction = 200
hnsw.add(vectors)  # HNSW needs no training step

queries = vectors[:100]
k = 10
_, true_ids = flat.search(queries, k)

for ef in [16, 64, 256]:
    hnsw.hnsw.efSearch = ef
    _, ids = hnsw.search(queries, k)
    recall = np.mean([len(set(ids[i]) & set(true_ids[i])) / k for i in range(len(queries))])
    print(f"efSearch={ef:>3}  recall@10={recall:.3f}")
```

As `efSearch` grows, recall rises toward 1.0 and latency rises with it. (Random vectors are much harder to index than real embeddings, so expect better recall on your own data.)

#### Pros and Cons

**Pros:**

- Excellent recall and low latency, even at millions of vectors.
- No training step, and vectors can be added at any time.
- Easy to tune with a single query-time parameter.

**Cons:**

- High memory use. Full vectors and the graph links must stay in RAM for good performance.
- Slow to build for large collections, especially with high `M` and `efConstruction`.
- Deletes are hard. Most implementations mark deleted nodes and clean them up later, so heavy churn degrades the graph until it is rebuilt or compacted.
- Restrictive filters can break graph navigation (Section 9).

---

### 6. IVF (Inverted File Index)

IVF takes a different approach: divide the vector space into regions, and only search the regions closest to the query.

#### How It Works

1. **Train**: Run k-means clustering on a sample of the vectors to find `nlist` cluster centers (centroids).
2. **Assign**: Put each vector into the list of its nearest centroid. Each list is like a bucket.
3. **Search**: Compare the query with all centroids, pick the `nprobe` closest ones, and compare the query only with the vectors in those buckets.

With `nlist = 1,000` and `nprobe = 10`, a search looks at about 1% of the collection.

The weakness is at the boundaries. If the query sits near the edge of a cluster, its true nearest neighbor may be in a neighboring bucket that was not probed. Raising `nprobe` fixes this at the cost of speed.

#### Parameters

| Parameter | Set at | Effect | Typical values |
|---|---|---|---|
| `nlist` | Build time | Number of clusters. More clusters means smaller buckets and faster probes, but more boundary misses. | About √N to 4√N |
| `nprobe` | Query time | Number of clusters to search. Higher improves recall but increases latency. | 1% to 10% of `nlist` |

`pgvector` suggests `lists = rows / 1000` for up to 1 million rows and `lists = √rows` beyond that, with `probes = √lists` as a starting point.

#### Example with FAISS

```python
nlist = 1024
quantizer = faiss.IndexFlatIP(d)  # used to find the nearest centroids
ivf = faiss.IndexIVFFlat(quantizer, d, nlist, faiss.METRIC_INNER_PRODUCT)

ivf.train(vectors)  # k-means on the data; a representative sample is enough
ivf.add(vectors)

for nprobe in [1, 8, 32, 128]:
    ivf.nprobe = nprobe
    _, ids = ivf.search(queries, k)
    recall = np.mean([len(set(ids[i]) & set(true_ids[i])) / k for i in range(len(queries))])
    print(f"nprobe={nprobe:>3}  recall@10={recall:.3f}")
```

#### Pros and Cons

**Pros:**

- Lower memory than HNSW, because there is no graph, only centroids and lists.
- Fast to build, and it combines naturally with compression (Section 7).
- Scales to very large collections, especially on GPUs.

**Cons:**

- Needs a training step on representative data. If your data changes a lot after training, the clusters no longer fit it well and recall drops until you retrain.
- For the same latency, recall is usually lower than HNSW.
- `pgvector` IVFFlat indexes should be created after the table has data, because the clusters are learned from the rows that exist at build time.

---

### 7. Product Quantization and Other Compression

HNSW and IVFFlat both store full vectors. A float32 vector with 1,024 dimensions takes 4 KB, so 100 million vectors need about 400 GB of RAM before any index overhead. **Quantization** compresses vectors so they fit in memory and compare faster.

#### Product Quantization (PQ)

PQ splits each vector into `m` smaller sub-vectors and replaces each sub-vector with the ID of its closest entry in a small codebook:

1. Split each d-dimensional vector into `m` sub-vectors. For d = 768 and m = 96, each sub-vector has 8 dimensions.
2. For each position, run k-means on all the sub-vectors at that position to learn 256 centroids (the codebook).
3. Store each sub-vector as the ID of its nearest centroid, which fits in one byte (8 bits).

The 768-dimensional vector shrinks from 3,072 bytes (768 × 4) to **96 bytes**, a 32× reduction. One million vectors go from about 3 GB to about 96 MB.

At query time, the index precomputes the distance from each query sub-vector to all 256 centroids in each codebook. The distance to any stored vector is then just `m` table lookups and additions, which is very fast.

```text
Original:   [0.12, -0.40, 0.33, 0.08, ... 768 numbers ...]           3,072 bytes
             |---- sub 1 ----|---- sub 2 ----| ... |---- sub 96 ----|
PQ codes:   [     17,             203,        ...,       88       ]     96 bytes
```

PQ distances are approximate, so PQ loses some recall. It is almost always combined with IVF (**IVF-PQ**) and with a **rescoring** step that recomputes exact distances for the top candidates:

```python
m = 48      # number of sub-vectors; d must be divisible by m (384 / 48 = 8)
nbits = 8   # 256 centroids per codebook

quantizer = faiss.IndexFlatIP(d)
ivfpq = faiss.IndexIVFPQ(quantizer, d, nlist, m, nbits, faiss.METRIC_INNER_PRODUCT)
ivfpq.train(vectors)
ivfpq.add(vectors)
ivfpq.nprobe = 32

# Retrieve more candidates than needed, then rescore them with the full vectors
_, candidate_ids = ivfpq.search(queries, 100)

def rescore(query: np.ndarray, ids: np.ndarray, k: int) -> np.ndarray:
    ids = ids[ids >= 0]  # FAISS pads with -1 when fewer results are found
    exact_scores = vectors[ids] @ query
    return ids[np.argsort(-exact_scores)[:k]]

final_ids = [rescore(queries[i], candidate_ids[i], k) for i in range(len(queries))]
```

In a real system, the full vectors used for rescoring often live on disk or in a separate store, while only the compressed codes stay in RAM.

#### Scalar and Binary Quantization

Two simpler forms of compression are widely supported:

- **Scalar quantization**: Store each number in fewer bits, for example float32 to int8 (4× smaller) or float16 (2× smaller). Recall loss is usually very small. `pgvector` offers `halfvec` (float16), and Qdrant, Milvus, OpenSearch, and Elasticsearch support int8.
- **Binary quantization**: Keep only the sign of each number, one bit per dimension (32× smaller), and compare with Hamming distance. It loses more information, but with oversampling and rescoring it works surprisingly well for large, high-dimensional models. Some embedding providers offer models trained to tolerate binary and int8 outputs.

Shortening vectors with Matryoshka embeddings (mentioned in [Understanding Embeddings](https://donny-nguyen.github.io/2026/11/09/embeddings.html)) is another way to save memory, and it combines with quantization.

**Rule of thumb:** Start with full precision. When memory becomes the bottleneck, try int8 or float16 first, then binary or PQ with rescoring, and measure recall at each step.

---

### 8. Choosing an Index

| | Flat (exact) | HNSW | IVF | IVF-PQ |
|---|---|---|---|---|
| Recall | 100% | Very high | High | Medium, high with rescoring |
| Query speed | Slow at scale | Very fast | Fast | Very fast |
| Memory | Full vectors | Full vectors plus graph | Full vectors plus centroids | Very low |
| Training needed | No | No | Yes | Yes |
| Updates | Instant | Good inserts, costly deletes | Good, until clusters drift | Good, until clusters drift |
| Best for | Small collections, heavy filters | Most production workloads | Large collections, lower memory | Hundreds of millions of vectors and up |

Practical guidance by collection size:

- **Under about 100,000 vectors**: Exact search is often fast enough, and it is the simplest.
- **100,000 to tens of millions**: HNSW is the default. Tune `efSearch` to your recall target.
- **Tens of millions and up, or tight memory limits**: Add quantization (int8, binary, or PQ) with rescoring, or use IVF-based or disk-based indexes.

Disk-based indexes, such as **DiskANN** (graph-based, keeps compressed vectors in RAM and full vectors on SSD), and **ScaNN** (Google's library, which uses a refined form of quantization) are worth knowing for very large deployments. Managed services such as Pinecone choose and tune indexes for you.

---

### 9. Metadata Filtering

Real queries rarely mean "the nearest vectors in the whole collection". They mean things like:

- "Nearest chunks **from the HR department**."
- "Nearest products **in stock and under $50**."
- "Nearest documents **this user has permission to read**."
- "Nearest chunks **updated after 2026-01-01**."

Each vector is stored with metadata (called a **payload** in Qdrant), and the filter must be combined with similarity search. There are three ways to do it.

#### Post-Filtering

Run the vector search first, then drop results that do not match the filter.

- **Simple**, and the ANN index is used unchanged.
- **Can return too few results**. If you ask for 10 neighbors and only 2 belong to the HR department, you get 2 results, or none. Asking for more candidates (for example, 100) helps, but there is no number that is always enough, and highly selective filters still fail.

#### Pre-Filtering

Find all vectors that match the filter first, then search only among them.

- **Always correct**: Every result matches the filter, and you get k results when k exist.
- **Can be slow**: If the matching set is large, exact search over it is expensive. If you try to use the ANN index while skipping non-matching nodes, HNSW runs into a problem: when most nodes are filtered out, the graph breaks into disconnected islands, and the search can get stuck far from the true neighbors.

#### Filtered (In-Index) Search

Modern vector databases apply the filter **during** the index traversal and adapt to how selective it is:

- **Qdrant** builds extra HNSW links based on indexed payload fields, so the graph stays connected under filters. When a filter matches only a small number of points, it skips the graph and uses the payload index with an exact search instead.
- **Weaviate, Milvus, OpenSearch, and Elasticsearch** check filters during traversal and switch to exact search over the filtered set when it is small enough.
- **pgvector** (version 0.8.0 and later) supports **iterative index scans**: if the filter removes too many of the HNSW candidates, the scan continues to fetch more until enough rows match.

This is the approach to use whenever your database supports it. The cost-based switch between "graph search with a filter" and "exact search over a small filtered set" is what keeps results both fast and correct.

#### Filtering with Qdrant

```python
from qdrant_client import QdrantClient, models

client = QdrantClient(":memory:")  # use a URL such as "http://localhost:6333" in production

client.create_collection(
    collection_name="chunks",
    vectors_config=models.VectorParams(size=384, distance=models.Distance.COSINE),
)

# Index the fields you filter on, so filtered search stays fast
client.create_payload_index("chunks", field_name="department", field_schema="keyword")
client.create_payload_index("chunks", field_name="updated_at", field_schema="datetime")

client.upsert(
    collection_name="chunks",
    points=[
        models.PointStruct(
            id=1,
            vector=embedding,  # a 384-dimensional list of floats
            payload={
                "text": "Employees receive 20 days of paid leave per year...",
                "source": "employee-handbook.pdf",
                "department": "HR",
                "updated_at": "2026-09-01T00:00:00Z",
            },
        ),
    ],
)

results = client.query_points(
    collection_name="chunks",
    query=query_embedding,
    query_filter=models.Filter(
        must=[
            models.FieldCondition(key="department", match=models.MatchValue(value="HR")),
            models.FieldCondition(
                key="updated_at",
                range=models.DatetimeRange(gte="2026-01-01T00:00:00Z"),
            ),
        ],
    ),
    limit=5,
)

for point in results.points:
    print(f"{point.score:.3f}  {point.payload['source']}")
```

#### Filtering with pgvector

In PostgreSQL, metadata is just columns, so filters are ordinary `WHERE` clauses:

```sql
CREATE TABLE chunks (
    id          BIGSERIAL PRIMARY KEY,
    content     TEXT NOT NULL,
    source      TEXT NOT NULL,
    department  TEXT NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    embedding   VECTOR(384) NOT NULL
);

CREATE INDEX ON chunks USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);
CREATE INDEX ON chunks (department);

-- Higher ef_search improves recall; iterative scans keep fetching candidates
-- until enough rows pass the filter (pgvector 0.8.0 and later)
SET hnsw.ef_search = 100;
SET hnsw.iterative_scan = relaxed_order;

SELECT id, content, source, 1 - (embedding <=> $1) AS similarity
FROM chunks
WHERE department = 'HR'
  AND updated_at >= '2026-01-01'
ORDER BY embedding <=> $1
LIMIT 5;
```

When a filter is very selective, PostgreSQL's planner may use the B-tree index on `department` and do an exact scan of the matching rows, which is the right choice. When one value is queried constantly and the set is small, a **partial index** (`CREATE INDEX ... WHERE department = 'HR'`) or table partitioning by that column can help further.

#### Filtering for Security and Multi-Tenancy

When filters enforce **access control**, a missing filter is a data leak, not just a bad result:

- Apply permission filters in your backend on every query. Never let the LLM or the user decide whether to apply them.
- Store permissions as metadata, such as `tenant_id`, `allowed_groups`, or `visibility`, and index those fields.
- For strict tenant isolation, consider a separate collection, namespace, or partition per tenant. Many databases support this directly, for example Pinecone namespaces, Weaviate multi-tenancy, and Qdrant's tenant-aware payload indexes.
- Test it. Include queries in your evaluation set that must return nothing for a given user.

**Tips for metadata filtering:**

- Index every field you filter on. An unindexed filter forces a scan of every payload.
- Keep metadata small and flat. Filter on IDs, categories, dates, and numbers, not long text.
- Watch for empty results. If a filter leaves nothing, say so to the user rather than letting the LLM answer without context.
- Prefer filters to extra vectors. "Only 2026 documents" is a filter, not something to ask the embedding to understand.

---

### 10. Measuring and Tuning

Treat your index configuration like any other performance setting: measure it on your own data.

1. **Build a ground truth.** Run exact search for a sample of real queries (a few hundred is enough) to get the true top-k neighbors.
2. **Measure ANN recall@k** against that ground truth for each configuration, as in the FAISS examples above.
3. **Measure latency** at the percentiles that matter (p50, p95, p99) under realistic concurrency, with your real filters applied.
4. **Measure memory and build time**, and how long it takes to add new data.
5. **Measure end-to-end retrieval quality**, using recall@k and mean reciprocal rank (MRR) against passages labeled as relevant. ANN recall tells you how close you are to exact search, while retrieval quality tells you whether exact search itself finds the right chunks.

A typical tuning loop:

- Start with the database defaults, for example HNSW with `M = 16`.
- Increase `efSearch` (or `nprobe` for IVF) until ANN recall@10 reaches about 0.95 to 0.99.
- If latency is too high at that recall, increase `M` and rebuild, or add more CPU or replicas.
- If memory is too high, add quantization with rescoring and check recall again.

Retrieval often matters more than the index: a better embedding model, better chunks, hybrid search, and a reranker usually improve answers more than pushing ANN recall from 0.97 to 0.99.

---

### 11. Putting It Together

A small but complete search function over chunks stored in Qdrant, using the same model for documents and queries, a permission filter, and oversampling for a later reranking step:

```python
from qdrant_client import QdrantClient, models
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")  # 384 dimensions
client = QdrantClient("http://localhost:6333")

def search_chunks(question: str, user_groups: list[str], k: int = 5, oversample: int = 4) -> list[dict]:
    query_vector = model.encode(question, normalize_embeddings=True).tolist()

    results = client.query_points(
        collection_name="chunks",
        query=query_vector,
        # Security filter, always applied by the backend
        query_filter=models.Filter(
            must=[
                models.FieldCondition(key="allowed_groups", match=models.MatchAny(any=user_groups)),
            ],
        ),
        # Higher hnsw_ef improves recall for this query
        search_params=models.SearchParams(hnsw_ef=128),
        # Fetch extra candidates so a reranker can choose the best k
        limit=k * oversample,
        with_payload=True,
    )

    return [
        {
            "text": point.payload["text"],
            "source": point.payload["source"],
            "score": point.score,
        }
        for point in results.points
    ]

candidates = search_chunks("How many vacation days do I get?", user_groups=["employees"])
```

From here, a reranker reorders the candidates, and the top few are passed to the LLM with their sources for citations.

---

### Summary

- Vector search finds the stored vectors closest to a query vector, and it is the retrieval engine of a RAG system.
- Exact (flat) search is perfectly accurate and fine for small collections, but its cost grows linearly with the number of vectors.
- Normalize vectors and use the metric your model recommends. For normalized vectors, cosine similarity, dot product, and Euclidean distance give the same ranking. The index and the query must use the same metric.
- ANN indexes trade a little recall for a large gain in speed. HNSW navigates a layered graph and is the default for most workloads. IVF searches only the nearest clusters and uses less memory. Product quantization, scalar quantization, and binary quantization compress vectors to fit huge collections in memory, usually with a rescoring step.
- Tune the query-time parameters (`efSearch` or `nprobe`) against measured recall and latency on your own data.
- Metadata filtering can be done before, after, or during the search. Post-filtering can return too few results, and naive pre-filtering can break HNSW. Prefer databases that filter during the search, and index the fields you filter on.
- When filters enforce permissions or tenant isolation, apply them in the backend on every query and test them.

Vector search is the third topic in the RAG stage of the [Learning Path to Become an AI Engineer](https://donny-nguyen.github.io/2026/07/11/learning-path-to-ai-engineer.html), after [Understanding Embeddings](https://donny-nguyen.github.io/2026/11/09/embeddings.html) and [Document Processing for RAG](https://donny-nguyen.github.io/2026/11/21/document-processing-for-rag.html). The next topic is hybrid search, which combines vector search with keyword search.
