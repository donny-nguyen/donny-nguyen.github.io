# Copilot Instructions

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