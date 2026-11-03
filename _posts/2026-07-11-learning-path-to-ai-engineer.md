# Learning Path to Become an AI Engineer

### Introduction

AI engineering is one of the fastest-growing disciplines in software development. An AI engineer builds products and systems on top of foundation models, such as large language models (LLMs), and connects them to real data, tools, and users. The role sits closer to software engineering than to research: most of the work is integrating models through APIs, giving them the right context, measuring their quality, and running them reliably in production.

It helps to separate two related roles:

- **AI engineer**: Builds applications with existing models. The core skills are LLM APIs, context engineering, retrieval, agents, evaluation, and production operations.
- **Machine learning engineer**: Trains, tunes, and serves models. This role leans more on mathematics, classical ML, deep learning, and training infrastructure.

This guide focuses on the AI engineer path. It puts the skills that employers screen for first, and moves machine learning theory later as optional depth. If you are aiming for an ML-heavy role, see [The Learning Roadmap for the AWS Certified Machine Learning Engineer – Associate Exam](https://donny-nguyen.github.io/2026/07/11/learning-roadmap-for-aws-certified-machine-learning-engineer-associate.html).

---

### Stage 1: Build Strong Software Engineering Foundations

AI engineering is software engineering first. Models are only one component in a system that also needs APIs, data stores, queues, tests, and deployment.

**Core skills to develop:**

- **Python**: The primary language for AI work. Learn data structures, classes, type hints, `async`/`await`, virtual environments, and packaging with `uv` or `pip`.
- **TypeScript**: Many AI engineer roles are full-stack. TypeScript is common for AI product front ends and backends, for example with Node.js and the Vercel AI SDK.
- **APIs and web services**: Build and consume REST APIs, handle streaming responses (Server-Sent Events and WebSockets), and work with JSON schemas. FastAPI is a popular choice for Python services.
- **Version control**: Git for collaboration, code review, and tracking prompt and configuration changes.
- **Linux, Docker, and the command line**: Most AI systems run in containers on Linux servers.
- **Testing**: Unit and integration tests, plus mocking of model calls so your test suite stays fast and deterministic.

**Recommended resources:**

- The official Python tutorial and "Fluent Python" by Luciano Ramalho
- FastAPI documentation
- The TypeScript Handbook

---

### Stage 2: Work with LLM APIs

The fastest way into AI engineering is to start calling models directly. Learn how the major providers work and what they have in common.

**Key concepts to learn:**

- **Model families**: Know the main commercial providers (OpenAI GPT, Anthropic Claude, Google Gemini) and leading open-weight models (Llama, Qwen, Mistral, DeepSeek, Gemma). Model names change every few months, so learn how to compare them rather than memorizing versions.
- **Messages and roles**: System, user, and assistant messages, and multi-turn conversation state.
- **Tokens and context windows**: How text is tokenized, how input and output tokens are priced, and what happens when you hit the context limit.
- **Sampling parameters**: Temperature, top-p, max tokens, and stop sequences.
- **Streaming**: Returning tokens to the user as they are generated for a responsive UI.
- **Structured outputs**: Getting reliable JSON that matches a schema, and validating it with libraries such as Pydantic or Zod.
- **Tool calling (function calling)**: Letting the model request that your code run a function and return the result.
- **Reasoning models**: Models that "think" before answering, and the effort or thinking-budget settings that trade quality for cost and latency.
- **Multimodal inputs**: Sending images, PDFs, and audio to models that support them.
- **Cost and latency**: Prompt caching, batch APIs, choosing smaller models for simple tasks, and measuring time to first token.
- **Reliability**: Rate limits, retries with backoff, timeouts, and fallback models.

**Key tools and platforms:**

- OpenAI API, Anthropic API, and Google Gemini API, with their official SDKs
- Amazon Bedrock, Google Cloud Vertex AI, and Azure AI Foundry for enterprise access
- Model gateways such as LiteLLM or OpenRouter for calling many providers through one interface
- Ollama for running open-weight models locally

---

### Stage 3: Learn Context Engineering

Prompt engineering has grown into **context engineering**: deciding everything the model sees on each call, including instructions, examples, retrieved documents, tool definitions, and conversation history. Most quality problems in LLM applications come from the context, not the model.

**Topics to cover:**

- **Writing clear instructions**: Specific system prompts, explicit output formats, and stating what the model should do when it is unsure.
- **Examples**: Zero-shot and few-shot prompting, and choosing representative examples.
- **Reasoning**: Chain-of-thought prompting, and when a reasoning model makes it unnecessary.
- **Context structure**: Separating instructions from data, for example with XML tags or Markdown sections.
- **Managing long context**: Summarizing or compacting history, trimming irrelevant content, and deciding between putting documents in the prompt and retrieving them.
- **Prompt caching**: Ordering context so the stable parts are cached and reused.
- **Prompt management**: Versioning prompts like code and reviewing changes against evaluations (see Stage 5).

**Recommended resources:**

- The prompt engineering guides and cookbooks from OpenAI, Anthropic, and Google
- Provider changelogs, since best practices shift as models improve

---

### Stage 4: Build Retrieval-Augmented Generation (RAG)

RAG gives a model access to knowledge it was not trained on, such as company documents, by retrieving relevant content and adding it to the context.

**Topics to cover:**

- **Embeddings**: Turning text into vectors, and choosing an embedding model.
- **Document processing**: Parsing PDFs, HTML, and tables, and chunking strategies such as fixed-size, recursive, and semantic chunking.
- **Vector search**: Similarity metrics, approximate nearest neighbor indexes, and metadata filtering.
- **Hybrid search**: Combining keyword search (BM25) with vector search for better recall.
- **Reranking**: Using a reranker model to reorder retrieved results before passing them to the LLM.
- **Query transformation**: Rewriting, expanding, or decomposing user questions before retrieval.
- **Advanced patterns**: Agentic RAG, where the model decides what to search for, and GraphRAG for connected knowledge.
- **Citations and grounding**: Returning sources so users can verify answers.
- **Retrieval evaluation**: Measuring recall, precision, and answer faithfulness (see Stage 5).

**Key tools:**

- `pgvector`, `Chroma`, `Qdrant`, `Weaviate`, `Pinecone`, or `OpenSearch` for vector storage
- `LlamaIndex` or `LangChain` for RAG pipelines (optional; many teams use provider SDKs directly)
- Cohere, Voyage AI, or open-source rerankers

---

### Stage 5: Master Evaluation and Testing

Evaluation is the skill that separates a demo from a production system, and it now appears in most AI engineer job descriptions. Because LLM outputs are non-deterministic, you cannot rely on traditional unit tests alone.

**Topics to cover:**

- **Eval datasets**: Building a "golden set" of realistic inputs and expected outputs from real user traffic and edge cases.
- **Code-based checks**: Exact match, regular expressions, JSON schema validation, and running generated code.
- **LLM-as-judge**: Using a model with a clear rubric to grade outputs, and checking that the judge agrees with human reviewers.
- **Human evaluation**: Side-by-side comparisons and expert review for high-stakes tasks.
- **RAG metrics**: Context precision and recall, faithfulness, and answer relevance.
- **Agent metrics**: Task completion rate, number of steps, tool-call accuracy, and cost per task.
- **Regression testing in CI**: Running evals automatically when prompts, models, or code change.
- **Online evaluation**: Monitoring quality in production with user feedback, sampling, and A/B tests.

**Key tools:**

- `promptfoo`, `DeepEval`, `Ragas`, and OpenAI Evals
- LangSmith, Langfuse, Braintrust, and Arize Phoenix for combined tracing and evaluation

---

### Stage 6: Build AI Agents and Agentic Systems

AI agents go beyond single LLM calls. They plan, use tools, and take multiple steps to complete a task.

**Concepts to understand:**

- **The agent loop**: The model decides on an action, your code runs the tool, the result goes back to the model, and the loop repeats until the task is done.
- **Workflows versus agents**: Start with fixed workflows (prompt chaining, routing, parallelization) and only add autonomy when the task needs it.
- **Tool design**: Clear tool names, descriptions, and input schemas, and returning concise, useful results.
- **Model Context Protocol (MCP)**: The open standard for connecting models to tools and data sources. Learn to build MCP servers that expose your APIs and to use MCP clients in agent applications.
- **Memory**: Short-term conversation state, long-term memory stores, and retrieval-based memory.
- **Multi-agent systems**: Orchestrator and worker patterns, handoffs between specialized agents, and when multiple agents are worth the extra cost.
- **Human-in-the-loop**: Approval steps before an agent takes actions that are hard to undo.
- **Coding agents**: Tools such as Claude Code, GitHub Copilot agent mode, and Cursor, both as productivity tools and as examples of agent design.

**Key frameworks:**

- OpenAI Agents SDK, Claude Agent SDK, and Google Agent Development Kit (ADK)
- `LangGraph` for stateful, graph-based agent workflows
- `CrewAI` for role-based multi-agent systems
- Microsoft Agent Framework (the successor to AutoGen and Semantic Kernel)
- Official MCP SDKs for Python and TypeScript

---

### Stage 7: Run AI Systems in Production (LLMOps)

Building a working prototype is only half the job. AI engineers must also deploy, observe, and operate AI systems reliably and affordably.

**Key topics:**

- **Deployment**: Packaging AI services with Docker and running them on Kubernetes, serverless platforms, or managed cloud services.
- **Observability and tracing**: Recording every model call, tool call, prompt, token count, latency, and cost. Learn the OpenTelemetry semantic conventions for generative AI.
- **Model gateways and routing**: Centralizing API keys, rate limits, fallbacks, and routing simple requests to cheaper models.
- **Caching**: Prompt caching at the provider level and semantic caching of repeated questions.
- **Cost management**: Tracking cost per request, per user, and per feature, and setting budgets and alerts.
- **Latency optimization**: Streaming, parallel tool calls, smaller models, and reducing context size.
- **Self-hosted inference**: Serving open-weight models with `vLLM`, `SGLang`, or Hugging Face TGI, understanding quantization (GGUF, AWQ, GPTQ), and sizing GPUs.
- **CI/CD for AI**: Running evals as a deployment gate and rolling out prompt or model changes gradually.
- **Experiment tracking**: MLflow or Weights & Biases for comparing prompts, models, and fine-tuning runs.

**Key cloud platforms:**

- AWS: Amazon Bedrock, SageMaker AI, and Lambda
- Google Cloud: Vertex AI
- Azure: Azure AI Foundry and Azure OpenAI

---

### Stage 8: Apply Security, Safety, and Governance

AI systems introduce new risks because they act on untrusted text. Employers increasingly expect AI engineers to design for these risks from the start.

**Topics to cover:**

- **OWASP Top 10 for LLM Applications**: The standard list of risks, including prompt injection, sensitive data disclosure, and excessive agency.
- **Prompt injection**: Direct attacks from users and indirect attacks hidden in documents, web pages, or tool results.
- **Least privilege for agents**: Limiting which tools an agent can call, sandboxing code execution, and requiring human approval for risky actions.
- **Data protection**: Handling personally identifiable information (PII), data retention, and provider data-usage policies.
- **Guardrails**: Input and output filtering with provider moderation APIs, Llama Guard, or NVIDIA NeMo Guardrails.
- **Red teaming**: Testing your system with adversarial inputs before release.
- **Governance and regulation**: Awareness of the EU AI Act, the NIST AI Risk Management Framework, and your company's AI policies.

---

### Stage 9: Learn Fine-Tuning and Post-Training

Most problems can be solved with better context or retrieval, but fine-tuning is the right tool when you need a consistent style, a specialized format, lower latency, or a smaller and cheaper model.

**Topics to cover:**

- **When to fine-tune**: Compare prompting, RAG, and fine-tuning for your use case, and measure the result with evals.
- **Supervised fine-tuning (SFT)**: Preparing high-quality instruction and response datasets.
- **Parameter-efficient fine-tuning**: LoRA and QLoRA for adapting large models with limited compute.
- **Preference tuning**: Direct Preference Optimization (DPO) and the ideas behind reinforcement learning from human feedback (RLHF).
- **Distillation and synthetic data**: Using a large model to generate training data for a smaller model.
- **Managed fine-tuning**: Fine-tuning APIs offered by model providers and cloud platforms.

**Key tools:**

- Hugging Face `transformers`, `peft`, and `trl`
- Unsloth and Axolotl for efficient fine-tuning
- Hugging Face Hub for finding and sharing open-weight models

---

### Stage 10: Deepen Your Machine Learning Foundations

Understanding how models work under the hood helps you debug strange behavior, read research, and move toward ML engineering roles. For most AI engineer positions, a working understanding is enough, so learn these topics alongside the earlier stages as you need them.

**Machine learning fundamentals:**

- Supervised learning: linear and logistic regression, decision trees, and gradient boosting
- Unsupervised learning: clustering and dimensionality reduction
- Model evaluation: train/validation/test splits, precision, recall, F1 score, and ROC-AUC
- Overfitting and regularization

**Deep learning and transformers:**

- Neural networks, loss functions, gradient descent, and backpropagation
- The Transformer architecture: tokenization, embeddings, self-attention, and next-token prediction
- How LLMs are trained: pre-training, instruction tuning, and preference tuning
- Why models hallucinate, and what that means for system design

**Mathematics (as needed):**

- Linear algebra: vectors, matrices, and dot products (the basis of embeddings and attention)
- Probability and statistics: distributions, sampling, and statistical significance for A/B tests and evals
- Calculus: derivatives and gradients, for understanding how training works

**Recommended tools and resources:**

- `PyTorch`, `scikit-learn`, `pandas`, and `numpy`
- Andrej Karpathy's "Neural Networks: Zero to Hero" series on YouTube
- 3Blue1Brown's "Neural Networks" series on YouTube
- "Hands-On Machine Learning with Scikit-Learn and PyTorch" by Aurélien Géron
- fast.ai's "Practical Deep Learning for Coders"
- [Core Machine Learning Concepts](https://donny-nguyen.github.io/2026/07/13/core-ml-concepts.html), [Supervised Learning](https://donny-nguyen.github.io/2026/09/11/supervised-learning.html), and [Unsupervised Learning](https://donny-nguyen.github.io/2026/09/11/unsupervised-learning.html) on this site

---

### Stage 11: Develop Domain Expertise

AI engineers who combine strong technical skills with deep domain knowledge are most valuable. After building your technical foundation, develop expertise in a domain where AI can make a real impact.

**Common domains:**

- Healthcare: Clinical documentation, decision support, and medical imaging
- Finance: Risk assessment, fraud detection, and document processing
- Software engineering: Coding agents, code review, and developer tools
- Legal and compliance: Contract review, document analysis, and regulatory search
- Customer support: Support agents, ticket triage, and voice assistants
- Education: Personalized tutoring, content generation, and assessment tools

---

### Building a Portfolio

Employers want evidence that you can build AI systems that work reliably, not just demos. Build projects that show the full lifecycle:

- A RAG application over a real document set, with citations and a measured improvement (for example, adding hybrid search or a reranker)
- An AI agent that completes a multi-step task using tools, with human approval for risky actions
- An MCP server that exposes a real API or database to AI clients
- An evaluation harness with a golden dataset that runs in CI
- A deployed LLM application with tracing, cost tracking, and a monitoring dashboard
- A fine-tuned small model compared against a larger prompted model on cost and quality
- An open-source contribution to an AI library or framework

Publish your work on GitHub, write about what you measured and learned, and share it with the community.

---

### Preparing for Interviews

AI engineer interviews often include:

- **Coding**: Practical problems in Python or TypeScript, often involving APIs and data handling, rather than only algorithm puzzles.
- **AI system design**: Designing a RAG system, an agent, or an AI feature with latency, cost, quality, and safety requirements.
- **Debugging and evaluation**: Explaining how you would find out why a model gives bad answers and how you would measure a fix.
- **Project deep dives**: Walking through a system you built and the trade-offs you made.

See [How to Prepare for an AI Agent Interview](https://donny-nguyen.github.io/2026/07/02/prepare-for-ai-agent-interview.html) for more detail.

---

### Staying Current

The AI field moves quickly. Make staying current a habit:

- Read model provider changelogs, documentation, and cookbooks from OpenAI, Anthropic, and Google
- Follow practitioner blogs and newsletters such as Latent Space, Simon Willison's blog, The Batch by DeepLearning.AI, and Import AI
- Watch talks from the AI Engineer World's Fair and AI Engineer Summit
- Skim research papers on arXiv (cs.CL, cs.AI, and cs.LG) for major developments
- Participate in communities on Discord, Reddit (r/LocalLLaMA, r/MachineLearning), and Hugging Face

---

### Summary

The path to becoming an AI engineer is a progressive journey:

1. **Software engineering foundations**: Python, TypeScript, APIs, Git, Docker, testing
2. **LLM APIs**: Providers, tokens, streaming, structured outputs, tool calling, cost
3. **Context engineering**: Instructions, examples, long context, prompt caching
4. **RAG**: Embeddings, chunking, hybrid search, reranking, citations
5. **Evaluation**: Golden datasets, LLM-as-judge, regression tests in CI
6. **AI agents**: Agent loops, tool design, MCP, multi-agent systems
7. **LLMOps**: Deployment, tracing, gateways, caching, self-hosted inference
8. **Security and safety**: Prompt injection, least privilege, guardrails, governance
9. **Fine-tuning**: SFT, LoRA, DPO, distillation
10. **ML foundations**: Classical ML, transformers, and math as needed
11. **Domain expertise**: Specializing in a high-value area

You do not need to finish one stage before starting the next. Start building with LLM APIs early, and add evaluation, security, and production skills as your projects grow. The most important habit is to build real things, measure how well they work, and learn from the results.
