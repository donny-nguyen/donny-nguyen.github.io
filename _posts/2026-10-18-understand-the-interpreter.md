# Understand the Interpreter

Python is an **interpreted** language. Unlike a compiled language such as C or Go, you do not turn your source code into a standalone executable before running it. Instead, the **Python interpreter** reads your code and executes it directly. Understanding what the interpreter does, and how you talk to it, makes many everyday errors much easier to diagnose.

### What the Interpreter Does

When you run a Python program, the interpreter performs a few steps:

1. **Reads the source file**: It loads the text of your `.py` file.
2. **Compiles to bytecode**: It translates the source into an intermediate form called *bytecode*, which is a set of low-level instructions for the Python virtual machine.
3. **Executes the bytecode**: The virtual machine runs those instructions one by one.

Bytecode is cached in `__pycache__` folders as `.pyc` files, so modules that have not changed do not need to be recompiled on every run. You rarely need to touch these files, but it helps to know they exist if you see them appear in your project.

### Two Ways to Use the Interpreter

There are two main ways to work with the interpreter.

**1. Run a script**

Save your code in a file and pass it to the interpreter:

```bash
python script.py
```

The interpreter runs the file from top to bottom, then exits. Use this for programs, automation tasks, and anything you want to run again later.

**2. Use the interactive REPL**

Run `python` with no arguments to start the **Read-Eval-Print Loop (REPL)**:

```bash
python
```

```python
>>> 2 + 3
5
>>> name = "Python"
>>> name.upper()
'PYTHON'
>>> exit()
```

The REPL reads one statement at a time, evaluates it, and prints the result. It is ideal for trying out small expressions, checking how a built-in function behaves, or exploring a library. Type `exit()` or press `Ctrl+D` (macOS and Linux) or `Ctrl+Z` then `Enter` (Windows) to leave.

### Check Which Interpreter You Are Using

A machine can have several Python installations, and the command you type determines which one runs. Check the version and location with:

```bash
python --version
python -c "import sys; print(sys.executable)"
```

The second command prints the full path to the interpreter that is running. This is useful when a package seems to be missing: you may have installed it into a different Python than the one you are running.

Some systems use `python3` instead of `python`. If `python` is not found, or points to Python 2, try:

```bash
python3 --version
```

### Command-Line Options

The interpreter accepts options that change how it runs. A few useful ones:

- `python -c "print('hello')"`: Run a single line of code from the command line.
- `python -m module_name`: Run a module as a script, such as `python -m venv myenv` or `python -m pip install requests`. Using `-m` also guarantees that the `pip` or `venv` tied to that interpreter is used.
- `python -i script.py`: Run a script, then stay in the REPL with its variables still loaded. This is handy for inspecting results after a script finishes.

### Modules and the Import System

When your code runs `import`, the interpreter searches for the module in a list of locations stored in `sys.path`. You can inspect that list:

```python
import sys

for path in sys.path:
    print(path)
```

The first entry is usually the directory of the script being run (or an empty string in the REPL, which means the current directory). The rest include the standard library and the `site-packages` folder where `pip` installs third-party packages.

Because the current directory is searched early, a file named after a standard library module, such as `random.py` or `json.py`, can shadow the real module and cause confusing errors. Avoid naming your own files after built-in modules.

### Handling Errors

When something goes wrong, the interpreter prints a **traceback**. Read it from the bottom up: the last line names the exception type and message, and the lines above it show the chain of calls that led there.

```python
def divide(a, b):
    return a / b

divide(10, 0)
```

```text
Traceback (most recent call last):
  File "script.py", line 4, in <module>
    divide(10, 0)
  File "script.py", line 2, in divide
    return a / b
           ~~^~~
ZeroDivisionError: division by zero
```

The message tells you the exception type (`ZeroDivisionError`), and the traceback tells you where it happened. Syntax errors are reported before any code runs, because the interpreter must compile the whole file first. Runtime errors appear only when the faulty line is actually executed.

### Interpreter Settings You Can Ignore for Now

The interpreter also supports environment variables such as `PYTHONPATH` and `PYTHONDONTWRITEBYTECODE`, and flags such as `-O` for optimized mode. You do not need them when you are starting out. Learn them when a specific problem calls for them.

### Conclusion

The Python interpreter is the program that reads and runs your code. Knowing that your source is compiled to bytecode and then executed, learning to use both script mode and the REPL, and checking which interpreter and import path you are using will save you time whenever something does not behave as expected. Use the REPL to experiment, use scripts to build programs, and keep an eye on the traceback when things fail.
