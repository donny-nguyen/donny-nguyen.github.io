# `docker build` Command

The `docker build` command is used to create a Docker image from a Dockerfile and a specified context. The context includes the Dockerfile itself and any files or directories needed during the build process. This command packages an application, its dependencies, and the environment into a single image that can be shared and deployed.

### Syntax
```bash
docker build [OPTIONS] PATH | URL | -
```

### Key Components
- **`PATH`**: Specifies the build context (directory) containing the Dockerfile and other files. It can also be a URL or `-` (to read the Dockerfile from standard input).
- **`OPTIONS`**: Flags that modify the behavior of the build process.

### Commonly Used Options
1. **`-t, --tag`**  
   Assigns a name and optionally a tag to the resulting image.  
   Example:  
   ```bash
   docker build -t my-app:latest .
   ```

2. **`-f, --file`**  
   Specifies the Dockerfile to use if it’s not named `Dockerfile` or located in the build context.  
   Example:  
   ```bash
   docker build -f Dockerfile.dev -t my-app:dev .
   ```

3. **`--no-cache`**  
   Prevents Docker from using the cache during the build, ensuring all instructions are re-executed.  
   Example:  
   ```bash
   docker build --no-cache -t my-app .
   ```

4. **`--build-arg`**  
   Passes build-time arguments (`ARG`) to the Dockerfile.  
   Example:  
   ```bash
   docker build --build-arg ENV=production -t my-app .
   ```

5. **`--progress`**  
   Controls the type of progress output (`auto`, `plain`, or `tty`).  
   Example:  
   ```bash
   docker build --progress=plain -t my-app .
   ```

6. **`--platform`**  
   Specifies the target platform for the build (e.g., `linux/amd64`, `linux/arm64`).  
   Example:  
   ```bash
   docker build --platform linux/arm64 -t my-app .
   ```

### Examples
1. **Basic Build**  
   Build an image using the default Dockerfile in the current directory:  
   ```bash
   docker build -t my-app .
   ```

2. **Using a Custom Dockerfile**  
   Build an image using a specific Dockerfile:  
   ```bash
   docker build -f custom.Dockerfile -t my-app:custom .
   ```

3. **Passing Build Arguments**  
   Build an image with build-time arguments:  
   ```bash
   docker build --build-arg API_KEY=12345 -t my-app .
   ```

4. **Disabling Cache**  
   Force a fresh build without cache:  
   ```bash
   docker build --no-cache -t my-app .
   ```

### Key Features
1. **Layer Caching**: Docker caches intermediate layers, speeding up subsequent builds when no changes are detected.
2. **Context**: Docker sends all files in the build context to the Docker daemon. Use a `.dockerignore` file to exclude unnecessary files.
3. **Tagged Builds**: Tags make it easy to manage and version images (e.g., `my-app:1.0`, `my-app:latest`).

### Building from a Remote Context
You can provide a remote Git repository or tarball as the context:
```bash
docker build https://github.com/example/repo.git#branch
```

### Inspecting the Build Output
You can use `--progress` to view detailed logs of each step in the build process.