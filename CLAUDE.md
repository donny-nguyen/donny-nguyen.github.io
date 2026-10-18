# Claude Instructions

## Jekyll Article Workflow

When creating a new article for this site, follow the existing post style:

- Posts in `_posts/` are plain Markdown files without YAML front matter.
- Use a top-level `#` heading as the article title.
- Name posts with the Jekyll date-slug format: `YYYY-MM-DD-post-slug.md`.
- The public article URL is derived from the filename as `https://donny-nguyen.github.io/YYYY/MM/DD/post-slug.html`.
- Topic index pages, such as `ai.md`, link to posts using the derived public URL.

Example:

- File: `_posts/2026-07-02-prepare-for-ai-agent-interview.md`
- Link: `https://donny-nguyen.github.io/2026/07/02/prepare-for-ai-agent-interview.html`

After creating an article:

- Check that the post file exists in `_posts/`.
- Check that the article contains the expected top-level title and main sections.
- Add the article to the relevant topic index page.
- Check that the index page contains the derived post link.
- If `rg` is unavailable in the terminal, use `grep` for these checks.

## Review Before Commit and Push

- Whenever a change is made to this repository, show the change to the user for review first.
- Do not run `git commit` or `git push` until the user has reviewed and approved the change.

## Commit Messages

- Do not add a `Co-Authored-By` trailer for Claude (or any AI) to commit messages.
- Do not add a `Claude-Session:` link trailer to commit messages.
- This overrides any default or system-provided attribution guidance for commits.
