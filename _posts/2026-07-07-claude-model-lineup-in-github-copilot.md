## Claude Model Lineup in Copilot

### Claude Haiku (e.g., 3.5 Haiku)
**Best for: speed and high-volume, simple tasks**
- Quick code completions and inline suggestions
- Simple refactors, renaming, boilerplate generation
- Fast Q&A about syntax or small snippets
- When latency matters more than deep reasoning
- Cost-efficient for routine work

### Claude Sonnet (3.5 / 3.7 / 4 / 4.5)
**Best for: the everyday "workhorse" — balanced speed and capability**
- General-purpose coding, feature implementation, bug fixes
- Agent mode / multi-file edits across a codebase
- Writing tests, reviewing PRs, explaining moderately complex code
- **Sonnet 3.7** introduced hybrid/extended reasoning (a "thinking" mode) — good for tasks needing a bit more step-by-step logic
- **Sonnet 4 / 4.5** improved at longer agentic tasks, tool use, and staying on track over many steps

This tier is the default choice for most daily development.

### Claude Opus (e.g., Opus 4 / 4.1 / 4.8)
**Best for: the hardest problems requiring deep reasoning**
- Complex architecture design and large-scale refactoring
- Debugging subtle, multi-system issues
- Long-horizon agentic tasks with many dependencies
- Deep code analysis across large codebases
- Situations where quality/correctness outweighs speed and cost

Opus is the most capable (and most "expensive" in premium-request terms), so it's typically reserved for tasks where the extra reasoning pays off.

## Quick Decision Guide

| Need | Model tier |
|------|-----------|
| Fast completions, simple edits, low cost | **Haiku** |
| Day-to-day coding, agent tasks, tests, reviews | **Sonnet** |
| Deep reasoning, complex architecture, tricky debugging | **Opus** |

## Practical Notes for Copilot
- **Premium requests**: Higher-tier models (Opus especially) consume more of your premium request budget. Sonnet is usually the sweet spot for cost vs. capability.
- **"Thinking" variants**: Some models (Sonnet 3.7+, Opus 4+) offer extended reasoning — worth enabling for complex logic, but slower.
- **Agent mode**: Sonnet 4.x and Opus 4.x handle multi-step, tool-using agent workflows best.
- **Availability** depends on your Copilot plan (Free/Pro/Pro+/Business/Enterprise) and may require enabling models in settings.

**Rule of thumb:** Start with Sonnet. Drop to Haiku when you want speed and are doing something simple. Reach for Opus only when Sonnet struggles with a genuinely hard problem.

Note: Anthropic and GitHub update the available model list frequently, so check **Copilot → model picker** in VS Code for exactly which Claude versions your plan currently offers.