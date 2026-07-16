# How to Trigger GitHub Copilot AI Agent Regularly

### Introduction

GitHub Copilot's agent mode can autonomously plan and execute multi-step coding tasks, run tools, and iterate on results. But an agent only acts when it is triggered. If you want Copilot to work on recurring tasks—such as nightly dependency checks, scheduled documentation updates, or routine code reviews—you need a reliable way to invoke the agent on a regular basis. This article explains several practical approaches to trigger the GitHub Copilot AI agent regularly, from manual habits to fully automated scheduling.

### Ways to Trigger the Copilot Agent

There are three broad categories for triggering the agent:

1. **Manual triggers** – You start the agent yourself inside VS Code.
2. **Event-driven triggers** – The agent runs in response to repository events (pull requests, issues, pushes).
3. **Scheduled triggers** – The agent runs automatically on a time-based schedule.

Let's look at each in detail.

### 1. Manual Triggers Inside VS Code

The simplest way to run the agent is directly in the editor.

- Open the **Chat** view in VS Code (`Ctrl+Alt+I` / `Cmd+Ctrl+I`).
- Switch the mode selector from **Ask** to **Agent**.
- Type your instruction and submit it.

To make manual triggering *regular*, standardize the prompts you reuse:

#### Use Prompt Files

Create reusable prompt files so you can invoke the same task consistently.

```
your-repo/
└── .github/
    └── prompts/
        └── weekly-review.prompt.md
```

**Example `weekly-review.prompt.md`:**

```markdown
---
mode: agent
description: Perform a weekly code health review
---

Review the `src/` directory for:
- Outdated dependencies in package.json
- TODO and FIXME comments
- Functions longer than 50 lines

Summarize findings in a markdown report.
```

Run it from the Chat view by typing `/weekly-review`. This turns a recurring task into a one-command action.

### 2. Event-Driven Triggers with the Copilot Coding Agent

GitHub Copilot's **coding agent** can run on GitHub's infrastructure and respond to repository events. This is the most powerful option for regular, automated work.

#### Assigning Issues to Copilot

You can assign a GitHub issue to Copilot, and it will start working automatically:

- Open an issue and assign it to **Copilot**.
- The agent creates a branch, makes changes, and opens a pull request.

By creating issues on a regular cadence, the agent is triggered each time.

#### Triggering from Pull Requests

The coding agent can also respond when you request changes on a pull request or mention `@copilot` in a comment:

```markdown
@copilot please add unit tests for the new service layer
```

### 3. Scheduled Triggers with GitHub Actions

To trigger the agent on a true schedule, combine **GitHub Actions** with the Copilot coding agent or the GitHub CLI. GitHub Actions supports cron-based scheduling through the `schedule` event.

#### Step 1: Create a Scheduled Workflow

Create `.github/workflows/scheduled-copilot.yml`:

```yaml
name: Scheduled Copilot Task

on:
  schedule:
    # Runs at 02:00 UTC every day
    - cron: '0 2 * * *'
  workflow_dispatch: # Allows manual runs too

permissions:
  contents: write
  issues: write
  pull-requests: write

jobs:
  trigger-copilot:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Create a task issue for Copilot
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          gh issue create \
            --title "Daily dependency audit $(date +%F)" \
            --body "Review dependencies for known vulnerabilities and outdated versions. Open a PR with any safe updates." \
            --assignee "copilot"
```

#### Step 2: Understand the Cron Syntax

The `cron` field uses standard cron expressions:

```
┌───────────── minute (0 - 59)
│ ┌───────────── hour (0 - 23)
│ │ ┌───────────── day of month (1 - 31)
│ │ │ ┌───────────── month (1 - 12)
│ │ │ │ ┌───────────── day of week (0 - 6)
│ │ │ │ │
* * * * *
```

Common schedules:

| Schedule | Cron Expression |
|----------|-----------------|
| Every hour | `0 * * * *` |
| Every day at 02:00 UTC | `0 2 * * *` |
| Every Monday at 09:00 UTC | `0 9 * * 1` |
| First day of each month | `0 0 1 * *` |

> **Note:** GitHub Actions scheduled workflows run on UTC time and may be delayed during periods of high load. Avoid scheduling exactly on the hour if timing is critical.

### Choosing the Right Approach

| Approach | Best For | Automation Level |
|----------|----------|------------------|
| Manual + prompt files | Ad-hoc but repeatable tasks | Low |
| Issue assignment | Well-defined feature or fix work | Medium |
| GitHub Actions schedule | Recurring maintenance and audits | High |

### Best Practices

- **Keep tasks scoped.** Give the agent clear, bounded instructions so its regular runs stay predictable.
- **Always review the output.** Scheduled agent runs should open pull requests for human review rather than pushing directly to `main`.
- **Use least-privilege tokens.** Grant only the permissions the workflow needs.
- **Add `workflow_dispatch`.** This lets you trigger a scheduled workflow manually for testing.
- **Log and monitor.** Track the agent's runs so you can catch failures early.

### Conclusion

Triggering the GitHub Copilot AI agent regularly comes down to matching the trigger mechanism to the task. Use prompt files for repeatable manual work, assign issues for feature-driven tasks, and combine GitHub Actions with cron schedules for fully automated, recurring maintenance. With these patterns in place, Copilot can handle routine work on a dependable cadence while you focus on higher-value engineering.
