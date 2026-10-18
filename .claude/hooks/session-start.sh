#!/bin/bash
set -euo pipefail

# Only run in Claude Code cloud sessions
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

# Record commits under the repository owner's identity
cd "${CLAUDE_PROJECT_DIR:-.}"
git config user.name "Donny Nguyen"
git config user.email "donny.nguyen.swe@outlook.com"
