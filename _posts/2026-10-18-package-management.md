# Package Management

Python's standard library covers a lot, but most real projects depend on third-party **packages** such as `requests`, `pytest`, or `fastapi`. **Package management** is the process of installing, upgrading, and removing those packages, and keeping track of which versions your project uses. In Python, the main tools are `pip` for installing packages and `venv` for creating isolated environments.

### What pip Does

`pip` is the package installer that comes with Python. It downloads packages from the [Python Package Index (PyPI)](https://pypi.org/) and installs them into the Python environment you are using.

```bash
pip install requests
```

Use `python -m pip` instead of plain `pip` when you have more than one Python installed. It guarantees that the packages go into the interpreter you are actually running:

```bash
python -m pip install requests
```

Check which version of `pip` you have, and confirm that it is tied to the right Python:

```bash
python -m pip --version
```

### Common pip Commands

```bash
pip install requests            # Install the latest version
pip install requests==2.32.3    # Install an exact version
pip install "requests>=2.31"    # Install a minimum version
pip install --upgrade requests  # Upgrade to the latest version
pip uninstall requests          # Remove a package
pip list                        # Show installed packages and versions
pip show requests               # Show details about one package
```

Version specifiers such as `==`, `>=`, `~=`, and `!=` tell pip which versions are acceptable. Quote them in the shell, as shown above, so the shell does not interpret `>` or `=` as redirection or other special characters.

### Virtual Environments

By default, `pip` installs packages into a shared location for your Python installation. If two projects need different versions of the same package, they will conflict. A **virtual environment** solves this by giving each project its own copy of `site-packages`.

Create one with `venv`, which ships with Python 3:

```bash
python3 -m venv .venv
```

Activate it:

```bash
# macOS and Linux
source .venv/bin/activate

# Windows (Command Prompt)
.venv\Scripts\activate

# Windows (PowerShell)
.venv\Scripts\Activate.ps1
```

When the environment is active, your prompt usually shows its name, and `python` and `pip` refer to the copies inside `.venv`. Confirm with:

```bash
which python      # macOS and Linux
where python      # Windows
```

Leave the environment with:

```bash
deactivate
```

Name the folder `.venv` and add it to `.gitignore`. Its contents are machine-specific and can be recreated from your dependency list, so they do not belong in version control.

### Recording Dependencies with requirements.txt

To reproduce your environment on another machine, record what you installed. The traditional way is a `requirements.txt` file:

```bash
pip freeze > requirements.txt
```

`pip freeze` lists every installed package, including the dependencies of your dependencies, with exact versions:

```text
certifi==2024.8.30
charset-normalizer==3.3.2
idna==3.10
requests==2.32.3
urllib3==2.2.3
```

To set up the same environment elsewhere, create a new virtual environment, activate it, and run:

```bash
pip install -r requirements.txt
```

A good habit is to keep a short, hand-written list of the packages you actually use, such as `requests>=2.31`, and generate the full pinned list from it. This keeps your intent separate from the transitive details that pip adds.

### Checking for Outdated Packages

To see which installed packages have newer versions available:

```bash
pip list --outdated
```

Upgrade them one at a time when you can, and run your tests after each change. Upgrading everything at once makes it harder to find which change broke something.

### Modern Alternatives

`pip` and `venv` are the foundation, and they work everywhere. You may also see newer tools that manage environments and dependency locking in one place, such as [uv](https://docs.astral.sh/uv/), [Poetry](https://python-poetry.org/), and [PDM](https://pdm-project.org/). They can be faster or add lock files and project metadata. Learn `pip` and `venv` first, because the concepts carry over and many projects still use them directly.

### Good Practices

- **Use one virtual environment per project.** Do not install project packages into your system or base Python.
- **Pin versions for applications** with `requirements.txt` or a lock file, so builds are reproducible.
- **Keep `requirements.txt` in version control**, but do not commit `.venv`.
- **Install only what you need.** Remove packages you no longer use with `pip uninstall`, then regenerate the file.
- **Check packages before installing them.** Install from PyPI and confirm the package name, because typos and look-alike names are a known way to distribute malicious code.

### Conclusion

Package management keeps your Python projects reliable. Use `pip` to install and upgrade packages, `venv` to isolate each project's dependencies, and `requirements.txt` to make the environment reproducible. Build these habits early, and setting up any project later becomes a routine, predictable step.
