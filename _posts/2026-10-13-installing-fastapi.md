# Installing FastAPI

FastAPI is a Python framework for building APIs. This guide sets up an isolated Python environment, installs FastAPI and its standard extras, and verifies the installation by starting a small application.

### 1. Check Python

Install a Python version supported by the FastAPI and Pydantic versions you plan to use. Then confirm that Python is available in your terminal:

```bash
python --version
```

On some systems, use `python3 --version` or, in Windows PowerShell, `py --version` if `python` is not recognized.

### 2. Create a Virtual Environment

A virtual environment keeps this project's packages separate from other Python projects and the system installation.

On macOS or Linux:

```bash
python3 -m venv .venv
source .venv/bin/activate
```

On Windows PowerShell:

```powershell
py -m venv .venv
.\.venv\Scripts\Activate.ps1
```

After activation, the environment name usually appears in the terminal prompt. If PowerShell blocks the activation script, use Command Prompt and run `.venv\Scripts\activate.bat`, or invoke the environment's Python directly as shown below.

### 3. Install FastAPI

For a typical development setup, install the `standard` extra. It includes FastAPI's command-line development tools and a server, along with commonly used optional dependencies:

```bash
python -m pip install --upgrade pip
python -m pip install "fastapi[standard]"
```

Using `python -m pip` helps ensure packages are installed into the environment belonging to the selected Python interpreter. If the environment is not activated, invoke its interpreter explicitly:

```bash
.venv/bin/python -m pip install "fastapi[standard]"
```

On Windows, use `.venv\Scripts\python.exe -m pip install "fastapi[standard]"` instead.

The base `fastapi` package can also be installed without the standard extras, but you will need to install and configure a server and any optional dependencies separately. The standard extra is the simplest starting point for local development.

### 4. Verify the Installation

Check that FastAPI is installed in the active environment:

```bash
python -m pip show fastapi
```

Create a file named `main.py` with a minimal application:

```python
from fastapi import FastAPI

app = FastAPI()


@app.get("/")
async def root():
    return {"message": "FastAPI is ready"}
```

Start the development server from the directory containing `main.py`:

```bash
fastapi dev main.py
```

Open `http://127.0.0.1:8000/` to see the response. The interactive API documentation is available at `http://127.0.0.1:8000/docs`. Stop the server with `Ctrl+C`.

### 5. Common Installation Issues

- **The `fastapi` command is not found**: activate the virtual environment, or run the command using the environment's scripts directory. Confirm that `fastapi[standard]` was installed in the same environment.
- **`pip` installs into the wrong Python**: use `python -m pip` and check the interpreter with `python --version` and `python -m pip --version`.
- **The environment is not activated**: run its Python directly. Use `.venv/bin/python` on macOS or Linux, or `.venv\Scripts\python.exe` on Windows.
- **A package or Python version conflict appears**: check the package's supported Python versions and recreate the environment with a compatible interpreter rather than changing the system Python installation.

### Conclusion

Use a virtual environment for each FastAPI project, install `fastapi[standard]` for a convenient development setup, and verify it by starting a small app. Once the server and `/docs` page work, you are ready to build routes and request models.