# Semantic Versioning

Semantic Versioning (often abbreviated as **SemVer**) is a standardized way to assign version numbers to software releases that clearly communicate the nature of changes made. It follows a **three-part format**:

### 📦 Format: `MAJOR.MINOR.PATCH`
Each segment of the version number has a specific meaning:

- **MAJOR**: Increased when you make **incompatible API changes**.
- **MINOR**: Increased when you add **new functionality** in a **backward-compatible** manner.
- **PATCH**: Increased when you make **backward-compatible bug fixes**.

### 🧪 Optional Extensions
Semantic versions can also include:
- **Pre-release labels**: e.g., `1.2.3-beta`, `2.0.0-rc.1`
- **Build metadata**: e.g., `1.2.3+build.456`

### ✅ Why It Matters
- Helps developers understand the impact of updates.
- Makes dependency management easier.
- Encourages consistent release practices across teams and projects.

### 🔍 Example
If a library moves from `1.4.2` to `2.0.0`, it signals that breaking changes were introduced. If it goes from `1.4.2` to `1.5.0`, new features were added without breaking compatibility.

You can explore the full specification at [semver.org](https://semver.org/).