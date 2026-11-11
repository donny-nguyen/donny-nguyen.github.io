# AI Model Lineup in GitHub Copilot

### Introduction

GitHub Copilot is not tied to a single AI model. Its model picker lets you choose from models built by several providers, including OpenAI, Anthropic, Google, Microsoft, xAI, and Moonshot AI. Each family has a different balance of speed, reasoning depth, and cost, so picking the right model for a task makes Copilot faster and cheaper to use.

This article explains each provider's lineup in Copilot, how the models are grouped by cost, and which model to choose for common development tasks.

> **Note:** The lineup below reflects GitHub's documentation as of November 2026. GitHub adds and retires models often, so always check the model picker in your editor and the official [Supported AI models in GitHub Copilot](https://docs.github.com/en/copilot/reference/ai-models/supported-models) page for the current list.

---

### How Copilot Groups Models

GitHub places every model into one of three cost categories:

| Category | What it means | Typical use |
|----------|---------------|-------------|
| **Lightweight** | Cheapest and fastest | Quick questions, simple edits, high-volume tasks |
| **Versatile** | Mid-range cost with balanced capability | Everyday coding, tests, reviews |
| **Powerful** | Most capable and most expensive | Hard debugging, architecture, long-running agent work |

Many providers also name their models by tier. Learning the naming patterns helps you guess a new model's role before you try it.

---

### OpenAI (GPT)

OpenAI provides the largest set of models in Copilot. Recent GPT generations use tier names: **Luna** for small and fast, **Terra** for balanced, and **Sol** for the most capable. **Astra** marks OpenAI's top GPT-6 model, and **Codex** models are tuned specifically for software development.

| Model | Category | Best for |
|-------|----------|----------|
| GPT-5.4 nano | Lightweight | Very fast, simple tasks |
| GPT-5.6 Luna | Lightweight | Fast help with simple or repetitive tasks, and image input such as screenshots |
| GPT-5.6 Terra | Versatile | Everyday interactive and agentic coding. GitHub suggests it as the default when you have no specific requirements |
| GPT-5.6 Sol | Powerful | Deep reasoning, debugging, and long-running agentic work |
| GPT-5.3-Codex | Powerful | General coding and agentic software development |
| GPT-6 Luna | Lightweight | Low-cost tasks on the newest generation |
| GPT-6 Sol | Powerful | Complex coding and reasoning |
| GPT-6.1 Sol and Terra | Powerful (Sol) | The latest GPT-6.1 versions of the Sol and Terra tiers |
| GPT-6 Astra | Powerful | The most demanding agentic tasks. One of the most expensive models in Copilot |

**When to choose OpenAI:** Start with GPT-5.6 Terra for daily work. Use GPT-5.3-Codex for coding-focused agent sessions, and move up to a Sol model when a task needs deeper reasoning.

---

### Anthropic (Claude)

Anthropic's Claude models are known for strong coding, careful instruction following, and reliable multi-step agent work. The lineup has four tiers, from smallest to largest:

- **Haiku**: Small, fast, and inexpensive.
- **Sonnet**: The balanced everyday workhorse.
- **Opus**: High capability for hard, long-horizon tasks.
- **Fable**: Anthropic's top and highest-priced tier, for the most demanding work.

| Model | Category | Best for |
|-------|----------|----------|
| Claude Haiku 4.5 | Versatile | Fast help with simple or repetitive tasks |
| Claude Haiku 5.5 | Lightweight | Low-cost, high-speed tasks on the newest generation |
| Claude Sonnet 4.6 | Versatile | Deep reasoning, debugging, and image input |
| Claude Sonnet 5 and 5.5 | Versatile | Everyday coding, multi-file edits, tests, and reviews |
| Claude Opus 4.8 | Powerful | Complex refactoring and difficult debugging |
| Claude Opus 4.8 (fast mode) | Powerful | The same model with faster output at a higher price (preview) |
| Claude Opus 5 and 5.5 | Powerful | Long-running agentic coding and knowledge work |
| Claude Fable 5, 5.1, and 5.2 | Powerful | The hardest agentic and reasoning tasks. Among the most expensive models in Copilot |

**When to choose Claude:** Sonnet is a strong default for agent mode and multi-file changes. Drop to Haiku for quick tasks, and reach for Opus or Fable only when Sonnet struggles with a genuinely hard problem.

For more detail on Claude tiers and reasoning effort, see [Claude Models and Effort Levels Explained](https://donny-nguyen.github.io/2026/10/18/claude-models-and-effort-levels.html).

---

### Google (Gemini)

Google's Gemini models offer large context windows and strong multimodal understanding. **Flash** models focus on speed and low cost, while the larger Gemini 4 model targets harder reasoning.

| Model | Category | Best for |
|-------|----------|----------|
| Gemini 3.8 Flash | Versatile | Fast, low-cost everyday tasks |
| Gemini 4 Argon | Powerful | Complex reasoning and large-context work |

**When to choose Gemini:** Try it when you work with large amounts of context, such as many files at once, or when you want a fast alternative to the OpenAI and Anthropic defaults.

---

### Microsoft (MAI)

Microsoft, which owns GitHub, offers its own **MAI** (Microsoft AI) models in Copilot.

| Model | Category | Best for |
|-------|----------|----------|
| MAI-Code-1.1-Flash | Lightweight | Fast, inexpensive coding help |

**When to choose MAI:** Use it as a low-cost option for routine coding questions and small edits.

---

### xAI (Grok)

xAI's Grok models are mid-priced general-purpose models.

| Model | Category | Best for |
|-------|----------|----------|
| Grok 4.5, 4.6, and 4.7 | Versatile | General coding. GitHub lists Grok 4.7 among its agentic options |

**When to choose Grok:** Try it as a mid-range alternative, and prefer the newest version, Grok 4.7.

---

### Moonshot AI (Kimi)

Moonshot AI is a Chinese AI company whose Kimi models are known for strong agentic coding at a competitive price.

| Model | Category | Best for |
|-------|----------|----------|
| Kimi K3 | Powerful | Agentic coding tasks |

**When to choose Kimi:** Try it as an alternative to the larger OpenAI and Anthropic models for agent mode. Check whether your organization allows it before you use it on company code.

---

### Auto Model Selection

If you do not want to choose a model yourself, select **Auto** in the model picker. Copilot then picks a model for each request based on the task and model availability. Copilot Free and Copilot Student users access models only through auto selection.

Auto is a good default if you are new to Copilot. Choose a model manually when you want consistent behavior or need a specific capability.

---

### How Billing Works

Since June 1, 2026, Copilot uses **usage-based billing**. Premium requests and model multipliers have been replaced by **GitHub AI Credits**:

- Each plan includes a monthly allowance of AI Credits. Business and Enterprise allowances are pooled across the organization.
- Each interaction costs credits based on the model and the number of input, cached input, and output tokens it uses.
- Larger models cost more per token, so a long agent session on a Powerful model can use far more credits than the same session on a Lightweight model.
- **Code completions and next edit suggestions do not use AI Credits.**

Annual Pro and Pro+ subscribers keep request-based billing until their plan expires. See [Models and pricing for GitHub Copilot](https://docs.github.com/en/copilot/reference/copilot-billing/models-and-pricing) for current per-token rates.

---

### Quick Decision Guide

| Need | Good choices |
|------|--------------|
| Fast answers, simple edits, low cost | GPT-5.6 Luna, Claude Haiku, MAI-Code-1.1-Flash |
| Everyday coding, tests, and reviews | GPT-5.6 Terra, Claude Sonnet, Gemini 3.8 Flash |
| Agent mode and multi-file changes | GPT-5.3-Codex, Claude Sonnet, Claude Opus |
| Deep reasoning and tricky debugging | GPT-5.6 Sol, Claude Sonnet 4.6, Claude Opus |
| Very large context | Gemini 4 Argon |
| Screenshots and diagrams | GPT-5.6 Luna, Claude Sonnet 4.6 |
| The hardest long-running tasks | GPT-6 Astra, Claude Opus 5.5, Claude Fable |
| No preference | Auto |

---

### Practical Tips

- **Start balanced, then adjust.** Begin with a Versatile model such as GPT-5.6 Terra or Claude Sonnet. Move down for speed and up only when the model struggles.
- **Match the model to the task, not the brand.** Each provider has fast, balanced, and powerful tiers.
- **Watch your credits.** Powerful models in agent mode can use your allowance quickly. Check usage in your GitHub billing settings.
- **Compare on your own work.** Benchmarks are only a starting point. Try two or three models on a real task from your codebase.
- **Check plan and policy availability.** Some models depend on your Copilot plan (Free, Pro, Pro+, Max, Business, or Enterprise), and organization admins can enable or disable specific models.
- **Expect change.** New models appear and older ones are retired every few months. Learning each provider's tier names helps you place new models quickly.

---

### Summary

- GitHub Copilot offers models from OpenAI, Anthropic, Google, Microsoft, xAI, and Moonshot AI.
- GitHub groups them into Lightweight, Versatile, and Powerful categories by cost and capability.
- OpenAI uses Luna, Terra, and Sol tiers, plus Codex and Astra. Anthropic uses Haiku, Sonnet, Opus, and Fable. Google uses Flash for speed and larger Gemini models for reasoning.
- Billing is now based on AI Credits and token usage, so model choice directly affects cost.
- A Versatile model is the best default for most daily development, and Auto is a good choice if you would rather not pick.
