# Artificial Intelligence

## Defining Rules for GitHub Copilot AI Agents

### Introduction

GitHub Copilot's AI agents can be customized to follow specific rules and guidelines for your project. These customizations can be committed to your Git repository, allowing your entire development team to share the same AI behavior and coding standards. This ensures consistency across the team and helps Copilot provide more relevant and context-aware assistance.

### What Are GitHub Copilot Instruction Files?

GitHub Copilot instruction files are markdown files that contain rules, guidelines, and context that the AI agent should follow when assisting with your codebase. These files are automatically detected and loaded by Copilot when you work in your workspace.

### Types of Instruction Files

GitHub Copilot supports several types of instruction files:

#### 1. `.github/copilot-instructions.md`

This is the most common location for team-wide instructions. Place this file at the root of your repository in the `.github` directory.

**Example structure:**
```
your-repo/
├── .github/
│   └── copilot-instructions.md
├── src/
└── README.md
```

#### 2. `.instructions.md`

This file can be placed at any level in your project hierarchy. Instructions cascade down, meaning files in subdirectories inherit rules from parent directories while being able to add their own specific rules.

**Example structure:**
```
your-repo/
├── .instructions.md          # Root-level instructions
├── frontend/
│   ├── .instructions.md      # Frontend-specific instructions
│   └── src/
└── backend/
    ├── .instructions.md      # Backend-specific instructions
    └── src/
```

#### 3. `copilot-instructions.md`

An alternative to `.instructions.md` that works similarly but may be more visible in file listings.

### Creating Your First Instruction File

Let's create a `.github/copilot-instructions.md` file for your project:

#### Step 1: Create the Directory Structure

```bash
mkdir -p .github
touch .github/copilot-instructions.md
```

#### Step 2: Define Your Rules

Here's a comprehensive example:

```markdown
# GitHub Copilot Instructions

## General Guidelines

- Follow our team's coding standards as defined in CONTRIBUTING.md
- Write clear, descriptive commit messages following Conventional Commits
- Prioritize code readability and maintainability over cleverness
- Always include error handling for external API calls
- Write comprehensive JSDoc/JavaDoc comments for public APIs

## Language-Specific Rules

### TypeScript
- Use TypeScript strict mode
- Prefer interfaces over type aliases for object shapes
- Always define return types for functions
- Use const assertions where appropriate
- Prefer functional programming patterns over imperative

### Python
- Follow PEP 8 style guide
- Use type hints for all function signatures
- Prefer pathlib over os.path for file operations
- Use dataclasses for data containers
- Write docstrings in Google style

### Java
- Follow Java naming conventions (camelCase for methods, PascalCase for classes)
- Use Optional instead of null where appropriate
- Prefer composition over inheritance
- Use Spring Boot's dependency injection patterns
- Always close resources with try-with-resources

## Testing Requirements

- Write unit tests for all new business logic
- Aim for at least 80% code coverage
- Use descriptive test names following the pattern: should_ExpectedBehavior_When_Condition
- Mock external dependencies in unit tests
- Include both positive and negative test cases

## Security Guidelines

- Never commit sensitive data (API keys, passwords, tokens)
- Validate and sanitize all user input
- Use parameterized queries to prevent SQL injection
- Implement proper authentication and authorization checks
- Keep dependencies up to date

## Documentation

- Update README.md when adding new features
- Document architectural decisions in docs/architecture/
- Include examples in code comments for complex logic
- Keep API documentation in sync with implementation

## Code Review Standards

- Code should be self-documenting
- Functions should do one thing and do it well
- Avoid magic numbers - use named constants
- Maximum function length: 50 lines
- Maximum file length: 500 lines
```

### Domain-Specific Instructions

For different parts of your codebase, create directory-specific instructions:

#### Frontend Example (`frontend/.instructions.md`):

```markdown
# Frontend Development Guidelines

## React Best Practices
- Use functional components with hooks
- Implement proper error boundaries
- Use React.memo() for expensive renders
- Keep components under 200 lines
- Extract custom hooks for reusable logic

## State Management
- Use Context API for simple global state
- Implement Redux for complex state management
- Keep state as close to where it's used as possible
- Avoid prop drilling beyond 2 levels

## Styling
- Use CSS Modules or styled-components
- Follow BEM naming convention for CSS classes
- Ensure responsive design for mobile, tablet, and desktop
- Use theme variables for colors and spacing
```

#### Backend Example (`backend/.instructions.md`):

```markdown
# Backend Development Guidelines

## API Design
- Follow RESTful conventions
- Use proper HTTP status codes
- Implement pagination for list endpoints
- Version APIs using URL paths (/api/v1/)
- Include rate limiting for public endpoints

## Database
- Use migrations for schema changes
- Index foreign keys and frequently queried columns
- Implement proper transaction handling
- Use connection pooling
- Log slow queries for optimization

## Error Handling
- Return consistent error response format
- Include error codes for client handling
- Log errors with appropriate severity levels
- Never expose sensitive information in error messages
```

### Advanced Features

#### Using YAML Front Matter

You can add metadata to control when instructions apply:

```markdown
---
applyTo:
  - "src/**/*.ts"
  - "src/**/*.tsx"
---

# TypeScript-Specific Instructions

These instructions only apply to TypeScript files...
```

#### Agent-Specific Instructions

Create custom agents for specific tasks:

```markdown
---
agent: code-reviewer
description: Expert code reviewer focused on quality and best practices
---

# Code Review Agent

When reviewing code:
1. Check for security vulnerabilities
2. Verify test coverage
3. Assess code complexity
4. Suggest performance improvements
5. Ensure documentation is complete
```

### Committing to Git

Once you've created your instruction files, commit them to your repository:

```bash
git add .github/copilot-instructions.md
git add .instructions.md
git commit -m "Add GitHub Copilot instructions for team"
git push origin main
```

### Best Practices

#### 1. Start Simple
Begin with a few essential rules and gradually expand as patterns emerge.

#### 2. Be Specific
Instead of "write good code," specify "functions should have a maximum cyclomatic complexity of 10."

#### 3. Include Examples
Show both good and bad examples to clarify your expectations.

#### 4. Keep It Updated
Review and update instructions as your team's practices evolve.

#### 5. Make It Discoverable
Include a reference to your instruction files in your README.md:

```markdown
## Development Guidelines

This project uses GitHub Copilot instructions to maintain code quality.
See `.github/copilot-instructions.md` for details.
```

#### 6. Team Collaboration
Treat instruction files like any other code - review changes via pull requests.

### Verifying Instructions Are Loaded

To check if your instructions are being used:

1. Open a file in your project
2. Ask Copilot Chat: "What instructions are you following for this project?"
3. Copilot will summarize the loaded instructions

### Example: Complete Project Structure

Here's how a well-organized project might look:

```
my-project/
├── .github/
│   └── copilot-instructions.md       # Team-wide guidelines
├── .instructions.md                   # Root project context
├── frontend/
│   ├── .instructions.md               # Frontend-specific rules
│   └── src/
│       ├── components/
│       └── utils/
├── backend/
│   ├── .instructions.md               # Backend-specific rules
│   └── src/
│       ├── controllers/
│       └── services/
├── docs/
│   └── architecture/
├── tests/
│   └── .instructions.md               # Testing-specific rules
└── README.md
```

### Common Use Cases

#### Enforcing Architectural Patterns

```markdown
## Architecture Guidelines

- Follow Clean Architecture principles
- Dependencies should point inward
- Business logic must be independent of frameworks
- Use dependency injection for loose coupling
```

#### Maintaining Code Style Consistency

```markdown
## Code Style

- Use single quotes for strings
- Prefer arrow functions for callbacks
- No semicolons in JavaScript/TypeScript
- Maximum line length: 100 characters
```

#### Project-Specific Context

```markdown
## Project Context

This is a healthcare application handling sensitive patient data.

- All data access must go through the DataAccessLayer
- PHI (Protected Health Information) must never be logged
- Implement audit logging for all data modifications
- Follow HIPAA compliance guidelines
```

### Troubleshooting

#### Instructions Not Being Applied

1. Verify file location and naming
2. Check markdown syntax is valid
3. Ensure YAML front matter (if used) is properly formatted
4. Reload VS Code window: Cmd+Shift+P → "Developer: Reload Window"

#### Conflicting Instructions

When multiple instruction files exist, Copilot merges them with directory-specific instructions taking precedence for their scope.

### Conclusion

GitHub Copilot instruction files are a powerful way to ensure consistent AI assistance across your development team. By committing these files to Git, you create a shared understanding of your project's standards and practices that evolves with your codebase. Start with basic guidelines and iterate based on your team's needs, and you'll find Copilot becoming an increasingly valuable and context-aware member of your team.

### Additional Resources

- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [VS Code Copilot Extension](https://marketplace.visualstudio.com/items?itemName=GitHub.copilot)
- [Example Instruction Files](https://github.com/topics/copilot-instructions)