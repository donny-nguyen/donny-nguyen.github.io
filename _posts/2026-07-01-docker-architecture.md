# Docker Architecture

**Docker** uses a client-server architecture. Its core components work together to build, ship, and run containers. Understanding how these pieces fit together is key to using Docker effectively and debugging issues in production.

---

### High-Level Overview

```
  Docker CLI / API Clients
         |  (REST API over Unix socket or TCP)
         v
  [ Docker Daemon (dockerd) ]
         |
         v
  [ containerd ]
         |
         v
  [ runc (OCI runtime) ]
         |
         v
  [ Container Process ]
```

---

### Core Components

#### 1. Docker Client (`docker`)

The **Docker client** is the primary way users interact with Docker. When you run commands like `docker build` or `docker run`, the client sends these requests to the Docker daemon via a **REST API**. The client can communicate with:

- A **local daemon** over a Unix socket (`/var/run/docker.sock`)
- A **remote daemon** over TCP (with TLS for security)

The client itself does no heavy lifting — it's purely a command dispatcher.

#### 2. Docker Daemon (`dockerd`)

The **Docker daemon** is the long-running background process that does the actual work. It:

- Listens for Docker API requests (from the CLI, Compose, or other tools)
- Manages Docker objects: images, containers, networks, and volumes
- Delegates container lifecycle management to **containerd**
- Communicates with other daemons for features like Docker Swarm

`dockerd` is the brain of Docker. It handles higher-level orchestration while delegating low-level execution.

#### 3. containerd

**containerd** is an industry-standard container runtime that `dockerd` uses internally. It manages the complete lifecycle of a container:

- Pulling and storing images (via snapshotter)
- Managing container execution and supervision
- Handling low-level storage and network primitives

containerd is a graduated CNCF project and is also used directly by Kubernetes (via the CRI interface), making it a universal runtime layer.

#### 4. runc

**runc** is the low-level OCI (Open Container Initiative) runtime. It is the actual tool that:

- Creates namespaces and cgroups
- Sets up the container's root filesystem
- Starts the container process

runc is spawned by containerd for each container start, then exits once the container process is running. It is the reference implementation of the [OCI Runtime Specification](https://opencontainers.org/).

#### 5. Docker Registry

A **registry** stores Docker images. When you run `docker pull` or `docker push`, the daemon communicates with a registry:

- **Docker Hub**: The default public registry (`hub.docker.com`)
- **Private registries**: AWS ECR, Google Artifact Registry, GitHub Container Registry, or self-hosted (e.g., Harbor)

Images are stored as a series of read-only layers, identified by content-addressed SHA256 digests.

---

### Docker Objects

| Object | Description |
|---|---|
| **Image** | Read-only template built from a Dockerfile. Composed of stacked layers via Union FS. |
| **Container** | A running instance of an image. Adds a thin writable layer on top of the image layers. |
| **Volume** | Managed storage that persists beyond a container's lifetime. Stored at `/var/lib/docker/volumes/`. |
| **Network** | Virtual network that connects containers. Default types: `bridge`, `host`, `none`, `overlay`. |

---

### Image Layering and Union Filesystem

Docker images are built as a stack of **immutable layers**, each representing a Dockerfile instruction. The Union Filesystem (e.g., **OverlayFS**) merges these layers into a single unified view.

```
[ Writable container layer ]   ← created at container start
[ App layer (COPY . .)     ]   ← read-only image layer
[ Deps layer (RUN npm i)   ]   ← read-only image layer
[ Base OS layer (FROM node)]   ← read-only image layer
```

Benefits:
- **Layer caching**: Unchanged layers are reused across builds, speeding up CI/CD.
- **Efficiency**: Multiple containers sharing the same image share the same read-only layers in memory and disk.
- **Immutability**: Image layers are never modified; changes go to the container's writable layer.

---

### The `docker run` Flow

When you execute `docker run nginx`, the following sequence occurs:

1. **Docker CLI** sends a `POST /containers/create` request to `dockerd`.
2. **dockerd** checks if the `nginx` image is available locally.
3. If not, `dockerd` pulls the image from the registry (layer by layer).
4. `dockerd` instructs **containerd** to create and start the container.
5. **containerd** calls **runc** with the OCI bundle (config + rootfs).
6. **runc** sets up namespaces, cgroups, and the overlay filesystem.
7. **runc** starts the container process, then exits.
8. **containerd** supervises the running container process.

---

### Networking Architecture

Docker networking is handled by **libnetwork**, which implements the **Container Network Model (CNM)**:

- **Network Sandbox**: An isolated network stack per container (interfaces, routing, DNS).
- **Endpoint**: Connects a sandbox to a network.
- **Network**: A group of endpoints that can communicate.

**Built-in network drivers:**

| Driver | Description |
|---|---|
| `bridge` | Default for standalone containers. Creates a virtual bridge on the host. |
| `host` | Container shares the host's network stack directly. |
| `none` | No networking. Fully isolated. |
| `overlay` | Multi-host networking for Docker Swarm / distributed workloads. |
| `macvlan` | Assigns a MAC address to the container, making it appear as a physical device. |

---

### Storage Architecture

Docker supports three types of mounts for persisting or sharing data:

| Type | Description | Use Case |
|---|---|---|
| **Volume** | Managed by Docker, stored in `/var/lib/docker/volumes/` | Preferred for persistent data |
| **Bind mount** | Maps a host path directly into the container | Dev workflows, config injection |
| **tmpfs mount** | Stored in host memory only, not on disk | Sensitive data, ephemeral scratch space |

---

### Summary

Docker's architecture separates concerns cleanly:

- **Docker CLI** handles user interaction.
- **dockerd** manages high-level orchestration and API.
- **containerd** handles container lifecycle.
- **runc** performs low-level OCI container creation.
- **Registries** store and distribute images.

This layered design means components can be swapped or used independently — for example, Kubernetes can use containerd directly without dockerd, and runc can be replaced by alternatives like **crun** or **gVisor** for different performance or security profiles.
