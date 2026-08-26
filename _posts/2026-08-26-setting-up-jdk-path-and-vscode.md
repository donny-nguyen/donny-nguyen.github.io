# Setting Up JDK, PATH, and VS Code

Before you can write and run Java programs, you need a working development environment. This guide walks you through installing the JDK, configuring the `PATH` environment variable, and setting up Visual Studio Code as your Java editor.

## Step 1: Install the JDK

The **JDK (Java Development Kit)** contains everything you need to compile and run Java code, including the compiler (`javac`) and the runtime (`java`).

### Choose a Distribution

Java has several distributions built from the same OpenJDK source. Popular choices include:

- **Eclipse Temurin (Adoptium)** — free, widely used, and well-supported.
- **Oracle JDK** — official Oracle build.
- **Amazon Corretto**, **Microsoft Build of OpenJDK**, **Azul Zulu** — production-ready alternatives.

Pick a **Long-Term Support (LTS)** version such as Java 17 or Java 21 for stability.

### Install It

**Windows**
1. Download the `.msi` installer from your chosen distribution.
2. Run the installer and follow the prompts.
3. Note the install location, typically `C:\Program Files\Eclipse Adoptium\jdk-21...`.

**macOS**
1. Download the `.pkg` installer, or use Homebrew:
   ```bash
   brew install temurin
   ```
2. The JDK is installed under `/Library/Java/JavaVirtualMachines/`.

**Linux**
1. Use your package manager, for example on Ubuntu/Debian:
   ```bash
   sudo apt update
   sudo apt install temurin-21-jdk
   ```
2. Or download the `.tar.gz`, extract it, and place it in `/opt/`.

## Step 2: Configure the PATH

The `PATH` environment variable tells your operating system where to find the `java` and `javac` commands so you can run them from any directory.

### Set JAVA_HOME

`JAVA_HOME` points to your JDK installation folder. Many tools (Maven, Gradle, IDEs) rely on it.

**Windows**
1. Open **Start** → search **Environment Variables** → **Edit the system environment variables**.
2. Click **Environment Variables**.
3. Under **System variables**, click **New**:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-21...`
4. Find the `Path` variable, click **Edit**, then **New**, and add:
   ```
   %JAVA_HOME%\bin
   ```
5. Click **OK** on all dialogs.

**macOS / Linux**

Add the following to your shell profile (`~/.zshrc`, `~/.bashrc`, or `~/.bash_profile`):

```bash
export JAVA_HOME=/path/to/your/jdk
export PATH=$JAVA_HOME/bin:$PATH
```

Then reload the profile:

```bash
source ~/.zshrc
```

On macOS you can locate the JDK automatically:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Verify the Installation

Open a **new** terminal or command prompt and run:

```bash
java -version
javac -version
```

You should see the version you installed. If the commands are not found, double-check the `PATH` entry and make sure you opened a new terminal so the changes take effect.

## Step 3: Set Up VS Code

Visual Studio Code is a lightweight, free editor with excellent Java support through extensions.

### Install VS Code

Download and install VS Code from the official website for your operating system.

### Install the Java Extension Pack

1. Open VS Code.
2. Click the **Extensions** icon in the sidebar (or press `Ctrl+Shift+X`).
3. Search for **Extension Pack for Java** (published by Microsoft).
4. Click **Install**.

This pack bundles several essential extensions:

- **Language Support for Java** — code completion, navigation, and error checking.
- **Debugger for Java** — set breakpoints and step through code.
- **Test Runner for Java** — run and debug JUnit tests.
- **Maven for Java** and **Gradle for Java** — build tool integration.
- **Project Manager for Java** — manage projects and dependencies.

### Point VS Code at Your JDK

VS Code usually detects `JAVA_HOME` automatically. To set it explicitly:

1. Open the Command Palette (`Ctrl+Shift+P`).
2. Search for **Preferences: Open User Settings (JSON)**.
3. Add a `java.jdt.ls.java.home` entry pointing to your JDK:
   ```json
   {
     "java.jdt.ls.java.home": "/path/to/your/jdk"
   }
   ```

## Step 4: Write and Run Your First Program

1. Create a folder for your project and open it in VS Code.
2. Create a file named `Hello.java`:
   ```java
   public class Hello {
       public static void main(String[] args) {
           System.out.println("Hello, Java!");
       }
   }
   ```
3. Click the **Run** button above the `main` method, or open a terminal and run:
   ```bash
   javac Hello.java
   java Hello
   ```

You should see `Hello, Java!` printed to the console.

## Troubleshooting

- **`java: command not found`**: The `PATH` is not set correctly, or you did not open a new terminal after editing it.
- **Wrong Java version**: Multiple JDKs may be installed. Check `JAVA_HOME` and the order of `PATH` entries.
- **VS Code does not recognize Java**: Reload the window (`Ctrl+Shift+P` → **Developer: Reload Window**) and confirm the Extension Pack for Java is installed and enabled.

With the JDK installed, the `PATH` configured, and VS Code ready to go, you now have a complete environment for learning and building Java applications.
