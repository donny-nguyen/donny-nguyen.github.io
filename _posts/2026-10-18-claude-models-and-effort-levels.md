# Claude Models and Effort Levels Explained

### Introduction

When you start a conversation with Claude, the model picker shows two choices: which model to use, and how much effort it should put into each answer. The names can look like a list of unrelated options, but they are two separate dials. The model decides the underlying capability and cost. The effort level decides how long Claude thinks before it responds. This article explains both so you can pick a sensible combination for your work.

### The Models

The picker lists four models in the order most people compare them.

**Opus 5.5** is the most capable model in the current lineup for demanding work. Choose it for complex multi-step reasoning, large refactors, difficult debugging, architecture decisions, and tasks where a subtle mistake would be expensive. It is usually the slowest and the most expensive per response, so it is not the default choice for quick questions.

**Sonnet 5.5** balances capability and speed. It handles most day-to-day coding, writing, and analysis well, and it responds faster than Opus. For many tasks it is the practical default: good enough for the job without the extra cost or wait.

**Fable 5.1** is marked as requiring usage credits, so it is not included in every plan's standard usage. Treat it as a premium option and check your plan before relying on it for routine work.

**Haiku 5.5** is the fastest and lightest model in the list. It suits simple, high-volume, or latency-sensitive tasks such as short edits, quick lookups, classification, summarizing short text, or generating many small outputs. It is less suited to long, intricate reasoning.

A useful rule of thumb is to start with Sonnet 5.5 for everyday work, move up to Opus 5.5 when a task is hard or high-stakes, and drop down to Haiku 5.5 when speed and volume matter more than depth.

### The Effort Levels

Effort controls how thoroughly the model works on a response. Higher effort generally produces more careful, more complete answers, but it takes longer and uses your usage limits faster. The picker offers six levels.

* **Low** gives the quickest responses with the least deliberation. Use it for simple questions, small formatting changes, or rapid back-and-forth.
* **Medium** is the recommended setting. It is a good balance of quality and speed for most tasks, which is why it is pre-selected.
* **High** spends more effort on reasoning. Choose it when a task has several interacting parts, such as tracing a bug across files or planning a multi-step change.
* **Extra** goes further than High for tasks that need careful, thorough consideration, at a higher cost in time and usage.
* **Max** uses the most effort the picker offers for a single response. Reserve it for the hardest problems where thoroughness matters more than speed.
* **Ultracode** appears in the picker in a distinct color, separate from the other levels. Check the picker's own description for its exact behavior, since it is not part of the standard scale.

Effort and model work together. A strong model at low effort can answer a simple question quickly. A smaller model at high effort can sometimes reason through a problem more carefully than a larger model at low effort. When in doubt, begin with the recommended Medium effort on Sonnet 5.5 and adjust after you see the quality of the first answer.

### Choosing a Combination

Here are a few practical starting points:

* **Quick questions and small edits:** Haiku 5.5 with Low or Medium effort.
* **Everyday coding and writing:** Sonnet 5.5 with Medium effort.
* **Hard debugging or design decisions:** Opus 5.5 with High effort.
* **Critical, difficult analysis where you can wait:** Opus 5.5 with Max effort.

### Summary

The model sets the capability and cost of each response, and the effort level sets how deeply Claude thinks before answering. Start with the recommended defaults, and raise the model or the effort only when the task demands it. That keeps responses fast and usage under control while still getting strong results when it matters.
