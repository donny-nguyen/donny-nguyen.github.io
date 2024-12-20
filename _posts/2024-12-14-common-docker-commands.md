# Common Docker Commands

Docker is a powerful tool for containerizing applications. Here's how to work with Docker:

1. Install Docker: Download and install Docker Desktop for your operating system (Windows, Mac, or Linux).

2. Run containers:
   - Use `docker run` to start a container from an image.
   - For example, `docker run hello-world` runs a simple test container.
   - Use flags like `-d` for detached mode and `-p` for port mapping.

3. Manage containers:
   - `docker ps` lists running containers.
   - `docker ps -a` shows all containers, including stopped ones.
   - `docker start` restarts a stopped container.
   - `docker stop` halts a running container.
   - `docker rm <container-id-or-name>` removes a stopped container.

4. Work with images:
   - `docker pull` downloads an image from a registry.
   - `docker images` lists local images.
   - `docker rmi <image-id>` remove an image.
   - Create a Dockerfile to build custom images.

5. Build images:
   - Use `docker build` to create an image from a Dockerfile.
   - Optimize images with multi-stage builds.

6. Use Docker Compose:
   - Create a `compose.yaml` file to define multi-container applications.
   - Use `docker compose up` to start all services defined in the Compose file.

7. Share images:
   - Push your images to Docker Hub or other registries.

8. Persist data:
   - Use volumes or bind mounts for data that needs to persist beyond the container's lifecycle.

Remember to use `docker --help` or `docker <command> --help` for more information on specific commands.