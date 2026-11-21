# Document Processing for RAG: Parsing and Chunking

### Introduction

A retrieval-augmented generation (RAG) system can only be as good as the text it retrieves. Before any embedding model or vector database comes into play, you have to turn raw files, such as PDFs, web pages, and spreadsheets, into clean text, and then split that text into pieces that are small enough to retrieve precisely but large enough to make sense on their own.

This step is called **document processing**. It is often the least glamorous part of a RAG pipeline, and also the part where most quality problems start. A table that is parsed into a jumble of numbers, a page header repeated in every chunk, or a sentence cut in half can all lead to wrong or missing answers.

This article covers how to parse PDFs, HTML, and tables, and how the main chunking strategies (fixed-size, recursive, and semantic chunking) work, with Python examples and practical advice for choosing between them.

---

### 1. Where Document Processing Fits in a RAG Pipeline

A typical RAG indexing pipeline has these steps:

1. **Load**: Read the raw files from a file system, a website, or an API.
2. **Parse**: Extract text and structure (headings, lists, tables) from each file format.
3. **Clean**: Remove noise such as navigation menus, page numbers, and repeated headers.
4. **Chunk**: Split the cleaned text into smaller pieces.
5. **Enrich**: Attach metadata, such as the source, title, section, and page number.
6. **Embed and store**: Generate an embedding for each chunk and save it in a vector store (see [Understanding Embeddings](https://donny-nguyen.github.io/2026/11/09/embeddings.html)).

Steps 2 to 5 are document processing. They decide what the retriever can find, so a mistake here cannot be fixed later by a better embedding model or a smarter prompt.

---

### 2. Parsing PDFs

PDF is the most common and the most difficult format in RAG. A PDF describes where to draw characters on a page, not how the text is structured. There is no built-in concept of a paragraph, a heading, or a table, so a parser has to reconstruct that structure from character positions.

#### Text-Based and Scanned PDFs

There are two kinds of PDFs:

- **Text-based (digital) PDFs**: Created by exporting from Word, LaTeX, or a browser. The text is stored in the file and can be extracted directly.
- **Scanned PDFs**: Images of pages, such as scanned contracts or invoices. There is no text layer, so you need **optical character recognition (OCR)** to read them.

A quick check: if `extract_text()` returns an empty string or garbage for a page, that page probably needs OCR.

#### Simple Text Extraction

For clean, single-column digital PDFs, a lightweight library is enough. `pypdf` is pure Python and easy to install:

```python
from pypdf import PdfReader

reader = PdfReader("handbook.pdf")

pages = []
for page_number, page in enumerate(reader.pages, start=1):
    text = page.extract_text() or ""
    pages.append({"page": page_number, "text": text})

print(pages[0]["text"][:500])
```

Keep the page number with the text. You will want it later for citations, so users can check the source.

`PyMuPDF` is faster and handles more layouts well. Its companion package `pymupdf4llm` converts a PDF directly into Markdown, keeping headings, lists, and simple tables:

```python
import pymupdf4llm

markdown = pymupdf4llm.to_markdown("handbook.pdf")
print(markdown[:1000])
```

Markdown output is very useful for RAG, because headings give you natural places to split the text later.

#### Common PDF Problems

- **Multi-column layouts**: A naive parser reads straight across the page and mixes lines from two columns. Use a layout-aware parser.
- **Headers, footers, and page numbers**: They repeat on every page and pollute chunks. Remove lines that appear on most pages.
- **Hyphenation and line breaks**: Words split across lines ("informa-\ntion") need to be joined back together.
- **Reading order**: Sidebars, captions, and footnotes may appear in the middle of the main text.
- **Images and charts**: Their content is lost unless you describe them with OCR or a vision model.

#### Layout-Aware Parsers

For complex documents, such as reports, research papers, and forms, use a parser that detects layout elements (titles, paragraphs, tables, figures) with machine learning models:

- **Docling** (open source, from IBM): Converts PDFs, Word, PowerPoint, and HTML into structured Markdown or JSON, with good table detection.
- **Unstructured**: Splits documents into typed elements, such as `Title`, `NarrativeText`, and `Table`, across many file formats.
- **Marker**: Converts PDFs to Markdown with high accuracy, including equations.
- **Managed services**: Amazon Textract, Azure AI Document Intelligence, Google Document AI, and LlamaParse, which also handle OCR and forms.

A Docling example:

```python
from docling.document_converter import DocumentConverter

converter = DocumentConverter()
result = converter.convert("annual-report.pdf")

markdown = result.document.export_to_markdown()
print(markdown[:1000])
```

#### Using Vision Models

Modern multimodal LLMs, such as Claude, GPT, and Gemini, can read a page image directly. You can render each page as an image and ask the model to transcribe it into Markdown, including tables and a short description of each chart. This works very well for messy layouts and scanned documents, but it costs more and is slower than a traditional parser, so it is best used for pages that other parsers handle poorly.

**Rule of thumb:** Start with a fast parser, look at the output for a sample of your real documents, and move to a layout-aware parser or a vision model only for the documents that need it.

---

### 3. Parsing HTML

HTML already has structure: headings (`<h1>` to `<h6>`), paragraphs, lists, and tables. The challenge is the opposite of PDFs. A web page contains a lot of content that is not part of the article, such as navigation menus, cookie banners, ads, sidebars, and footers. This is called **boilerplate**, and it should be removed before chunking.

#### Extracting the Main Content

`trafilatura` is designed for this task. It finds the main content of a page and can output Markdown:

```python
import trafilatura

html = trafilatura.fetch_url("https://example.com/blog/post")
markdown = trafilatura.extract(
    html,
    output_format="markdown",
    include_tables=True,
    include_links=False,
)
print(markdown)
```

#### Controlling Extraction Yourself

When you know the structure of the pages, such as your own documentation site, `BeautifulSoup` gives you full control:

```python
from bs4 import BeautifulSoup

with open("page.html", encoding="utf-8") as file:
    soup = BeautifulSoup(file, "html.parser")

# Remove elements that are not part of the content
for tag in soup(["script", "style", "nav", "header", "footer", "aside"]):
    tag.decompose()

main = soup.find("main") or soup.body

sections = []
for element in main.find_all(["h1", "h2", "h3", "p", "li"]):
    text = element.get_text(" ", strip=True)
    if text:
        sections.append({"tag": element.name, "text": text})
```

**Tips for HTML:**

- Keep the heading hierarchy. Converting HTML to Markdown (for example, with `markdownify` or `html2text`) is an easy way to preserve it.
- Save the page URL and title as metadata for citations.
- Watch for content loaded by JavaScript. If the HTML you download is almost empty, render the page with a headless browser, such as Playwright, before parsing.

---

### 4. Parsing Tables

Tables are where many RAG systems break. A table's meaning comes from its structure: the value "4.2" only makes sense when you know its row ("Q3 2026") and column ("Revenue, $M"). If a parser flattens a table into plain text, that relationship is lost.

#### Extracting Tables from PDFs

`pdfplumber` can detect and extract tables from digital PDFs:

```python
import pdfplumber

with pdfplumber.open("financials.pdf") as pdf:
    page = pdf.pages[2]
    tables = page.extract_tables()

for table in tables:
    header, *rows = table
    print(header)
    for row in rows:
        print(row)
```

Other options are Camelot, Docling, and the managed document services listed earlier. For HTML tables, `pandas.read_html()` turns each `<table>` into a DataFrame.

#### Representing Tables for Retrieval

Once you have a table as rows and columns, you need to turn it into text that an embedding model and an LLM can understand. Common approaches:

**1. Markdown table.** Compact and easy for LLMs to read. Works well for small tables.

```text
| Quarter | Revenue ($M) | Growth |
|---------|--------------|--------|
| Q2 2026 | 3.8          | 5%     |
| Q3 2026 | 4.2          | 11%    |
```

**2. Row serialization.** Turn each row into a sentence that repeats the column names. Each row can then be retrieved on its own without losing its meaning.

```python
def serialize_rows(header: list[str], rows: list[list[str]], title: str) -> list[str]:
    lines = []
    for row in rows:
        pairs = [f"{column}: {value}" for column, value in zip(header, row)]
        lines.append(f"{title}. " + "; ".join(pairs) + ".")
    return lines

header = ["Quarter", "Revenue ($M)", "Growth"]
rows = [["Q2 2026", "3.8", "5%"], ["Q3 2026", "4.2", "11%"]]

for line in serialize_rows(header, rows, "Quarterly revenue"):
    print(line)
# Quarterly revenue. Quarter: Q2 2026; Revenue ($M): 3.8; Growth: 5%.
# Quarterly revenue. Quarter: Q3 2026; Revenue ($M): 4.2; Growth: 11%.
```

**3. Summary plus original.** Ask an LLM to write a short description of what the table shows, embed the summary for retrieval, and pass the full table to the model when the summary matches.

**Tips for tables:**

- Never split a small table across chunks. Keep it whole, with its title or caption.
- For large tables, split by groups of rows, and repeat the header row in every chunk.
- For questions that need calculations across many rows ("What was the total revenue in 2026?"), consider loading the table into a database and letting the model query it with SQL instead of relying on retrieval.

---

### 5. Cleaning and Adding Metadata

After parsing, clean the text before you chunk it:

- Normalize whitespace and Unicode characters (for example, with `unicodedata.normalize("NFKC", text)`).
- Remove repeated headers, footers, and page numbers.
- Join hyphenated words and broken lines.
- Remove duplicate documents, which waste space and crowd out other results.
- Remove or mask sensitive data, such as personal information, if it should not be searchable.

Then attach **metadata** to each document, and later to each chunk:

```python
chunk = {
    "text": "Employees receive 20 days of paid leave per year...",
    "metadata": {
        "source": "employee-handbook.pdf",
        "title": "Employee Handbook 2026",
        "section": "Leave Policy > Annual Leave",
        "page": 14,
        "updated_at": "2026-09-01",
        "department": "HR",
    },
}
```

Metadata enables citations ("Source: Employee Handbook 2026, page 14"), filtering ("only search HR documents"), and freshness rules ("prefer the newest version").

---

### 6. Why Chunking Matters

Chunking splits a long document into smaller pieces, and each piece gets its own embedding. There are three reasons to chunk:

- **Model input limits**: Embedding models have a maximum input length, often 512 to 8,192 tokens.
- **Retrieval precision**: One embedding for a 50-page document averages many topics together, so it matches nothing well. Smaller chunks produce more focused embeddings.
- **Context efficiency**: Passing only the relevant paragraphs to the LLM is cheaper and faster, and reduces the chance that the model is distracted by unrelated text.

The trade-off is chunk size:

- **Chunks that are too small** lose context. "It increased by 11%" is useless if the chunk does not say what "it" is.
- **Chunks that are too large** mix several topics, which weakens the embedding and adds noise to the prompt.

A good chunk is a piece of text that a person could read on its own and understand. The strategies below are different ways to get closer to that goal.

---

### 7. Fixed-Size Chunking

The simplest strategy is to split text into chunks of a fixed number of characters or tokens, with some **overlap** between neighboring chunks. The overlap reduces the chance that an important sentence is split across a boundary with no context on either side.

Measuring size in **tokens** is more accurate than characters, because model limits are defined in tokens:

```python
import tiktoken

encoding = tiktoken.get_encoding("cl100k_base")

def fixed_size_chunks(text: str, chunk_size: int = 500, overlap: int = 50) -> list[str]:
    tokens = encoding.encode(text)
    chunks = []
    step = chunk_size - overlap
    for start in range(0, len(tokens), step):
        window = tokens[start:start + chunk_size]
        chunks.append(encoding.decode(window))
        if start + chunk_size >= len(tokens):
            break
    return chunks

chunks = fixed_size_chunks(document_text, chunk_size=500, overlap=50)
```

**Pros:**

- Simple, fast, and predictable.
- Every chunk fits within the embedding model's limit.

**Cons:**

- Ignores structure, so it cuts through sentences, paragraphs, lists, and tables.
- Chunks often start or end mid-thought.

Fixed-size chunking is a reasonable baseline, and it works acceptably for uniform text without much structure, such as transcripts. For most documents, recursive chunking is a better default.

---

### 8. Recursive Chunking

Recursive chunking splits text using a list of separators, from the largest unit to the smallest. It first tries to split on paragraph breaks. If a piece is still too long, it splits that piece on line breaks, then on sentence endings, then on spaces. This keeps natural units together whenever they fit.

The most widely used implementation is `RecursiveCharacterTextSplitter` from the `langchain-text-splitters` package:

```python
from langchain_text_splitters import RecursiveCharacterTextSplitter

splitter = RecursiveCharacterTextSplitter.from_tiktoken_encoder(
    encoding_name="cl100k_base",
    chunk_size=500,
    chunk_overlap=50,
    separators=["\n\n", "\n", ". ", " ", ""],
)

chunks = splitter.split_text(document_text)
```

To see how it works, here is a simplified version without overlap:

```python
def recursive_split(text: str, max_chars: int, separators: list[str]) -> list[str]:
    if len(text) <= max_chars:
        return [text]
    if not separators:
        # No separators left: fall back to a hard cut
        return [text[i:i + max_chars] for i in range(0, len(text), max_chars)]

    separator, *rest = separators
    parts = text.split(separator)

    chunks, current = [], ""
    for part in parts:
        candidate = f"{current}{separator}{part}" if current else part
        if len(candidate) <= max_chars:
            current = candidate
        else:
            if current:
                chunks.append(current)
            if len(part) > max_chars:
                chunks.extend(recursive_split(part, max_chars, rest))
                current = ""
            else:
                current = part
    if current:
        chunks.append(current)
    return chunks

chunks = recursive_split(document_text, 2000, ["\n\n", "\n", ". ", " "])
```

#### Structure-Aware Splitting

If your parser produces Markdown, you can go one step further and split on headings first, so that each chunk belongs to a single section. The section path becomes useful metadata:

```python
from langchain_text_splitters import MarkdownHeaderTextSplitter

header_splitter = MarkdownHeaderTextSplitter(
    headers_to_split_on=[("#", "h1"), ("##", "h2"), ("###", "h3")],
)

sections = header_splitter.split_text(markdown)
for section in sections:
    print(section.metadata)  # for example: {'h1': 'Employee Handbook', 'h2': 'Leave Policy'}

# Sections that are still too long can be split further with the recursive splitter
chunks = splitter.split_documents(sections)
```

The same idea applies to other formats. Split code by functions and classes, HTML by headings, and Q&A documents by question.

**Pros:**

- Respects paragraphs and sentences, so chunks read naturally.
- Still guarantees a maximum size.
- Fast and does not need any model.

**Cons:**

- Relies on formatting. Text without paragraph breaks falls back to sentence or word splits.
- Does not know whether two neighboring paragraphs are about the same topic.

**Recursive chunking, combined with heading-based splitting when structure is available, is the best default for most RAG systems.**

---

### 9. Semantic Chunking

Semantic chunking uses embeddings to decide where to split. Instead of relying on formatting, it looks for points where the **topic changes**.

The algorithm:

1. Split the text into sentences.
2. Embed each sentence (or a small window of neighboring sentences, for more stable results).
3. Compute the similarity between each pair of neighboring sentences.
4. Start a new chunk where the similarity drops sharply, which suggests a new topic.

```python
import re
import numpy as np
from sentence_transformers import SentenceTransformer

model = SentenceTransformer("all-MiniLM-L6-v2")

def semantic_chunks(text: str, percentile: float = 90, max_sentences: int = 20) -> list[str]:
    sentences = [s.strip() for s in re.split(r"(?<=[.!?])\s+", text) if s.strip()]
    if len(sentences) < 2:
        return sentences

    embeddings = model.encode(sentences, normalize_embeddings=True)

    # Cosine distance between each sentence and the next one
    similarities = np.sum(embeddings[:-1] * embeddings[1:], axis=1)
    distances = 1 - similarities

    # Split where the distance is in the top (100 - percentile)% of all distances
    threshold = np.percentile(distances, percentile)

    chunks, current = [], [sentences[0]]
    for i, distance in enumerate(distances):
        if distance > threshold or len(current) >= max_sentences:
            chunks.append(" ".join(current))
            current = []
        current.append(sentences[i + 1])
    chunks.append(" ".join(current))
    return chunks

for chunk in semantic_chunks(document_text):
    print(chunk[:100], "...")
```

Ready-made implementations include `SemanticChunker` in `langchain-experimental`, `SemanticSplitterNodeParser` in LlamaIndex, and the `chonkie` library.

**Pros:**

- Chunks follow topic boundaries, even in text with little formatting, such as transcripts or long emails.
- Each chunk tends to be about one idea, which produces focused embeddings.

**Cons:**

- Slower and more expensive, because every sentence has to be embedded during indexing.
- Chunk sizes vary a lot, so you still need a maximum size limit.
- Results depend on the threshold and the embedding model, which need tuning.
- Benchmarks show mixed results. On well-structured documents, semantic chunking often performs no better than recursive chunking.

Try semantic chunking when your documents lack structure and your evaluations show that recursive chunking is splitting topics badly.

---

### 10. Techniques That Improve Any Chunking Strategy

Whichever strategy you choose, these techniques help chunks keep their context:

- **Add a context header**: Prepend the document title and section path to each chunk before embedding, for example "Employee Handbook > Leave Policy > Annual Leave". This is cheap and often gives a clear improvement.
- **Contextual retrieval**: Use an LLM to write one or two sentences that explain where a chunk fits in the whole document, and prepend them before embedding. Anthropic reported that this approach, combined with hybrid search and reranking, significantly reduced retrieval failures. Prompt caching keeps the cost of processing each document manageable.
- **Parent-child (small-to-big) retrieval**: Embed small chunks for precise matching, but return the larger parent section to the LLM, so the model gets the full context.
- **Sentence window retrieval**: Embed single sentences, and return each match together with a few sentences before and after it.
- **Late chunking**: Embed the whole document with a long-context embedding model first, then pool the token embeddings for each chunk. Each chunk's vector then carries information from the surrounding text.

---

### 11. Choosing a Chunk Size and Strategy

There is no universally best chunk size. Reasonable starting points:

| Content type | Suggested strategy | Starting chunk size |
|---|---|---|
| Documentation, manuals, policies | Heading-based plus recursive | 300 to 800 tokens |
| Research papers and reports | Layout-aware parsing plus recursive | 500 to 1,000 tokens |
| FAQs and Q&A pages | One chunk per question and answer | Natural size |
| Transcripts, chats, long emails | Semantic or recursive | 200 to 500 tokens |
| Source code | Split by function or class | Natural size |
| Tables | Keep whole, or row groups with header | Natural size |

Use an overlap of about 10 to 20 percent of the chunk size as a starting point. Overlap matters most for fixed-size chunking, and less when chunks already follow natural boundaries.

**Comparison of the three main strategies:**

| | Fixed-size | Recursive | Semantic |
|---|---|---|---|
| How it splits | Every N tokens | By separators, largest first | Where the topic changes |
| Respects structure | No | Yes | Partly (by meaning) |
| Indexing cost | Very low | Very low | Higher (embeds every sentence) |
| Chunk size | Uniform | Bounded, varies | Varies widely |
| Best for | Baselines, uniform text | Most documents (default) | Unstructured text |

#### Evaluate, Do Not Guess

Chunking choices should be decided by measurement, not intuition:

1. Build a small evaluation set of real questions and the passages that answer them.
2. Index your documents with two or three chunking configurations.
3. Measure retrieval metrics, such as recall@k (does the right passage appear in the top k results?) and mean reciprocal rank (MRR).
4. Measure end-to-end answer quality with tools such as `Ragas` or `DeepEval`.
5. Read the actual chunks for a sample of documents. Many problems, such as broken tables or repeated footers, are obvious once you look.

---

### 12. Putting It Together

A simple but solid indexing pipeline for a folder of Markdown files produced by a parser:

```python
from pathlib import Path
from langchain_text_splitters import MarkdownHeaderTextSplitter, RecursiveCharacterTextSplitter

header_splitter = MarkdownHeaderTextSplitter(
    headers_to_split_on=[("#", "h1"), ("##", "h2"), ("###", "h3")],
)
text_splitter = RecursiveCharacterTextSplitter.from_tiktoken_encoder(
    encoding_name="cl100k_base",
    chunk_size=500,
    chunk_overlap=50,
)

def process_file(path: Path) -> list[dict]:
    markdown = path.read_text(encoding="utf-8")
    sections = header_splitter.split_text(markdown)
    pieces = text_splitter.split_documents(sections)

    chunks = []
    for index, piece in enumerate(pieces):
        section_path = " > ".join(piece.metadata.values())
        chunks.append({
            "id": f"{path.stem}-{index}",
            # Prepend a context header so the embedding knows where the chunk belongs
            "text": f"{path.stem}: {section_path}\n\n{piece.page_content}",
            "metadata": {"source": path.name, "section": section_path},
        })
    return chunks

all_chunks = [chunk for path in Path("parsed_docs").glob("*.md") for chunk in process_file(path)]
print(f"Created {len(all_chunks)} chunks")
```

From here, embed each chunk's `text`, store it with its metadata in a vector database, and you have the indexing half of a RAG system.

---

### Summary

- Document processing turns raw files into clean, well-structured chunks, and it sets the upper limit on RAG quality.
- PDFs need the most care: use simple extractors for clean digital files, layout-aware parsers such as Docling for complex ones, and OCR or vision models for scanned pages.
- For HTML, remove boilerplate, keep the heading structure, and save the URL and title.
- Tables need special handling: keep them whole with their headers, represent them as Markdown or serialized rows, and consider SQL for numeric questions.
- Fixed-size chunking is simple but ignores structure. Recursive chunking, especially combined with heading-based splitting, is the best default. Semantic chunking splits by topic and helps with unstructured text, at a higher cost.
- Context headers, contextual retrieval, and parent-child retrieval help chunks keep their meaning.
- Choose parsers, chunk sizes, and strategies by evaluating retrieval on your own documents.

Document processing is the second topic in the RAG stage of the [Learning Path to Become an AI Engineer](https://donny-nguyen.github.io/2026/07/11/learning-path-to-ai-engineer.html), right after [Understanding Embeddings](https://donny-nguyen.github.io/2026/11/09/embeddings.html).
