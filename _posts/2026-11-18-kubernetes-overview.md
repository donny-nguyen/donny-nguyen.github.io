# Kubernetes Overview

**Kubernetes** (often shortened to **K8s**) is an open-source platform for running containerized applications across a cluster of machines. Docker answers the question "how do I package and run one container?" Kubernetes answers the next one: "how do I run hundreds of containers across many servers, keep them healthy, scale them, and update them without downtime?"

Kubernetes was originally designed at Google, drawing on its internal Borg system, and was donated to the **Cloud Native Computing Foundation (CNCF)** in 2015. Today it is the de facto standard for container orchestration and is offered as a managed service by every major cloud: **Amazon EKS**, **Google GKE**, and **Azure AKS**.

---

### Why Kubernetes?

Running a few containers on one host with `docker run` or Docker Compose works well for development. In production, new problems appear:

- A container crashes at 3 AM. Who restarts it?
- A server dies. Who moves its containers to a healthy machine?
- Traffic triples. Who starts more copies and spreads the load?
- A new version ships. How do you roll it out gradually and roll back if it fails?
- Containers move around. How do other services find them?

Kubernetes solves these with a **declarative model**: you describe the *desired state* ("run 3 replicas of this image, exposed on port 80"), and Kubernetes continuously works to make the *actual state* match it.

| Capability | What Kubernetes Does |
|---|---|
| **Self-healing** | Restarts failed containers, replaces Pods on failed nodes, kills containers that fail health checks |
| **Scheduling** | Places containers on nodes based on CPU/memory requests, affinity rules, and constraints |
| **Horizontal scaling** | Scales replicas manually or automatically based on CPU, memory, or custom metrics |
| **Service discovery & load balancing** | Gives a stable DNS name and virtual IP to a group of Pods |
| **Rolling updates & rollbacks** | Replaces Pods incrementally and reverts if the new version is unhealthy |
| **Configuration & secrets** | Injects config and credentials without rebuilding images |
| **Storage orchestration** | Attaches cloud disks, network storage, or local volumes to Pods |

---

### High-Level Architecture

A Kubernetes **cluster** consists of a **control plane** (the brain) and a set of **worker nodes** (the muscle) that run your workloads.

```
                    kubectl / CI pipelines / API clients
                                  |
                                  v
  +------------------------- Control Plane --------------------------+
  |                                                                  |
  |   [ kube-apiserver ] <------> [ etcd ]                           |
  |         ^      ^                                                 |
  |         |      |                                                 |
  |   [ scheduler ]  [ controller-manager ]  [ cloud-controller-mgr ]|
  +------------------------------------------------------------------+
                 |                        |
                 v                        v
  +---------- Worker Node 1 ----------+  +---------- Worker Node 2 ----------+
  |  [ kubelet ]   [ kube-proxy ]     |  |  [ kubelet ]   [ kube-proxy ]     |
  |  [ container runtime (containerd)]|  |  [ container runtime (containerd)]|
  |   Pod   Pod   Pod                 |  |   Pod   Pod                       |
  +-----------------------------------+  +-----------------------------------+
```

---

### Control Plane Components

#### 1. kube-apiserver

The **API server** is the front door of the cluster. Every interaction, whether from `kubectl`, a CI pipeline, or an internal component, goes through its REST API. It:

- Authenticates and authorizes requests (RBAC)
- Validates objects and runs admission controllers
- Persists the cluster state to etcd

It is the **only** component that talks to etcd directly. All other components watch the API server for changes.

#### 2. etcd

**etcd** is a consistent, highly available key-value store that holds the entire cluster state: every Pod, Service, ConfigMap, and Secret. It uses the **Raft** consensus algorithm, so production clusters typically run 3 or 5 etcd members to tolerate failures. If etcd is lost without a backup, the cluster's state is lost.

#### 3. kube-scheduler

The **scheduler** watches for newly created Pods that have no node assigned and picks the best node for each one. It filters out nodes that can't run the Pod (insufficient resources, taints, node selectors) and then scores the remaining ones (spreading, affinity, resource balance).

#### 4. kube-controller-manager

The **controller manager** runs the built-in **controllers**: control loops that watch the cluster state and push it toward the desired state. Examples include:

- **Deployment / ReplicaSet controller**: keeps the right number of Pod replicas running
- **Node controller**: notices when nodes stop responding
- **Job controller**: runs Pods to completion
- **EndpointSlice controller**: tracks which Pods back each Service

#### 5. cloud-controller-manager

On cloud providers, the **cloud controller manager** integrates with the provider's APIs, for example creating a cloud load balancer for a `LoadBalancer` Service or attaching block storage volumes.

---

### Worker Node Components

#### 1. kubelet

The **kubelet** is an agent running on every node. It receives Pod specs from the API server, tells the container runtime to start the containers, runs liveness and readiness probes, and reports node and Pod status back to the control plane.

#### 2. Container Runtime

The **container runtime** actually runs containers. Kubernetes talks to it via the **Container Runtime Interface (CRI)**. The most common runtimes are **containerd** and **CRI-O**. Kubernetes removed its built-in Docker integration (dockershim) in v1.24, but images built with Docker still run unchanged, because they are standard OCI images. (See [Docker Architecture](https://donny-nguyen.github.io/2026/07/01/docker-architecture.html) for how containerd and runc fit together.)

#### 3. kube-proxy

**kube-proxy** maintains network rules (iptables, IPVS, or nftables) on each node so that traffic sent to a Service's virtual IP is forwarded to one of its healthy Pods. Some CNI plugins, such as Cilium, can replace kube-proxy entirely using eBPF.

---

### Core Kubernetes Objects

Everything in Kubernetes is an **object** (also called a **resource**) described in YAML or JSON and stored through the API server.

#### Pod

A **Pod** is the smallest deployable unit. It wraps one or more containers that share the same network namespace (one IP address) and can share volumes. Most Pods contain a single application container, sometimes with **sidecar** containers for logging, proxies, or service mesh.

Pods are **ephemeral**: when one dies, it is replaced by a new Pod with a new IP. You rarely create Pods directly; you use higher-level controllers instead.

#### Deployment and ReplicaSet

A **Deployment** manages stateless applications. It creates a **ReplicaSet**, which keeps a specified number of identical Pods running. Changing the Deployment (for example, a new image tag) triggers a **rolling update**: a new ReplicaSet scales up while the old one scales down.

#### Service

A **Service** gives a group of Pods (selected by labels) a stable virtual IP and DNS name, and load balances across them.

| Type | Description |
|---|---|
| `ClusterIP` | Default. Reachable only inside the cluster. |
| `NodePort` | Exposes the Service on a static port on every node. |
| `LoadBalancer` | Provisions an external cloud load balancer. |
| `ExternalName` | Maps the Service to an external DNS name. |

#### Ingress and Gateway API

An **Ingress** routes external HTTP(S) traffic to Services based on host names and paths, handled by an **ingress controller** (such as NGINX or Traefik). The newer **Gateway API** is the successor to Ingress, with a richer, role-oriented model (`GatewayClass`, `Gateway`, `HTTPRoute`).

#### ConfigMap and Secret

A **ConfigMap** stores non-sensitive configuration (environment variables, config files). A **Secret** stores sensitive data such as passwords and tokens. Secrets are only base64-encoded by default, so enable encryption at rest in etcd and restrict access with RBAC, or use an external secrets manager.

#### Namespace

A **Namespace** divides one cluster into virtual sub-clusters, for example per team or per environment. Resource quotas, network policies, and RBAC rules can be scoped to a namespace.

#### Other Workload Types

| Object | Use Case |
|---|---|
| **StatefulSet** | Stateful apps (databases, Kafka) needing stable network IDs and persistent storage per replica |
| **DaemonSet** | Runs one Pod on every (or selected) node, e.g., log collectors, monitoring agents |
| **Job** | Runs Pods to completion, e.g., a batch task or a database migration |
| **CronJob** | Runs Jobs on a schedule |

#### Storage Objects

- **PersistentVolume (PV)**: a piece of storage in the cluster (cloud disk, NFS, etc.)
- **PersistentVolumeClaim (PVC)**: a Pod's request for storage of a certain size and access mode
- **StorageClass**: defines how PVs are provisioned dynamically (e.g., `gp3` on AWS)

---

### A Simple Example

The manifest below runs three replicas of an NGINX web server and exposes them inside the cluster:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web
  labels:
    app: web
spec:
  replicas: 3
  selector:
    matchLabels:
      app: web
  template:
    metadata:
      labels:
        app: web
    spec:
      containers:
        - name: nginx
          image: nginx:1.27
          ports:
            - containerPort: 80
          resources:
            requests:
              cpu: "100m"
              memory: "128Mi"
            limits:
              memory: "256Mi"
          readinessProbe:
            httpGet:
              path: /
              port: 80
          livenessProbe:
            httpGet:
              path: /
              port: 80
---
apiVersion: v1
kind: Service
metadata:
  name: web
spec:
  type: ClusterIP
  selector:
    app: web
  ports:
    - port: 80
      targetPort: 80
```

Apply it and inspect the result:

```bash
kubectl apply -f web.yaml
kubectl get deployments
kubectl get pods -l app=web -o wide
kubectl get service web
```

Notice that the Service finds its Pods through the **label selector** `app: web`, not through Pod names or IPs. Labels are the glue that connects Kubernetes objects.

---

### What Happens When You Run `kubectl apply`

1. **kubectl** sends the Deployment manifest to the **kube-apiserver**.
2. The API server authenticates, authorizes, validates, and stores the Deployment in **etcd**.
3. The **Deployment controller** sees the new Deployment and creates a **ReplicaSet**.
4. The **ReplicaSet controller** sees that 0 of 3 Pods exist and creates 3 Pod objects (with no node yet).
5. The **scheduler** notices the unscheduled Pods and assigns each one to a node.
6. The **kubelet** on each chosen node sees a Pod assigned to it and asks the **container runtime** to pull the image and start the container.
7. The kubelet runs the readiness probe. Once it passes, the Pod is marked **Ready**.
8. The **EndpointSlice controller** adds the Ready Pods to the Service, and **kube-proxy** updates the node's routing rules so traffic can reach them.

No component tells another what to do directly. Each one **watches the API server** and reacts to changes. This loosely coupled, reconciliation-based design is what makes Kubernetes resilient and extensible.

---

### Common kubectl Commands

| Command | Purpose |
|---|---|
| `kubectl get pods -A` | List Pods in all namespaces |
| `kubectl describe pod <name>` | Show details and recent events for a Pod |
| `kubectl logs <pod> [-c container] [-f]` | View (or follow) container logs |
| `kubectl exec -it <pod> -- sh` | Open a shell inside a container |
| `kubectl apply -f <file.yaml>` | Create or update resources declaratively |
| `kubectl delete -f <file.yaml>` | Delete resources defined in a file |
| `kubectl scale deployment <name> --replicas=5` | Change the replica count |
| `kubectl rollout status deployment/<name>` | Watch a rolling update |
| `kubectl rollout undo deployment/<name>` | Roll back to the previous version |
| `kubectl port-forward svc/<name> 8080:80` | Forward a local port to a Service |
| `kubectl config get-contexts` | List clusters/contexts in your kubeconfig |

---

### Scaling and Resilience

- **Resource requests and limits**: Requests tell the scheduler how much CPU and memory a container needs. Limits cap what it can use. Containers that exceed their memory limit are killed (`OOMKilled`).
- **Probes**: **Liveness** probes restart stuck containers. **Readiness** probes keep a Pod out of Service traffic until it is ready. **Startup** probes give slow-starting apps time before liveness checks begin.
- **Horizontal Pod Autoscaler (HPA)**: Adjusts the replica count based on CPU, memory, or custom metrics.
- **Cluster Autoscaler / Karpenter**: Adds or removes nodes when Pods can't be scheduled or nodes are underused.
- **PodDisruptionBudget (PDB)**: Limits how many replicas can be taken down at once during voluntary disruptions such as node upgrades.
- **Topology spread constraints**: Spread replicas across nodes and availability zones so one failure doesn't take out the whole service.

---

### The Kubernetes Ecosystem

Kubernetes is intentionally a platform for building platforms. Much of what teams use day-to-day comes from the ecosystem:

| Area | Popular Tools |
|---|---|
| **Packaging** | Helm, Kustomize |
| **GitOps / CD** | Argo CD, Flux |
| **Networking (CNI)** | Cilium, Calico, AWS VPC CNI |
| **Service mesh** | Istio, Linkerd |
| **Observability** | Prometheus, Grafana, OpenTelemetry |
| **Policy & security** | OPA Gatekeeper, Kyverno, Falco |
| **Local clusters** | kind, minikube, k3d, Docker Desktop |

Kubernetes is also extensible through **Custom Resource Definitions (CRDs)** and the **Operator pattern**: you define new object types (such as `PostgresCluster`) and write controllers that manage them using the same reconciliation loop as the built-in controllers.

---

### When Kubernetes Is (and Isn't) the Right Choice

**Good fit:**

- Many services that need independent scaling and deployment
- Teams that want a consistent platform across clouds or on-premises
- Workloads that benefit from self-healing, autoscaling, and rolling updates

**Maybe overkill:**

- A single small application or a handful of containers
- Teams without the capacity to operate and secure a cluster
- Cases where simpler managed options work well, such as AWS ECS, AWS Fargate, Google Cloud Run, or Azure Container Apps (see [Develop, Build, and Deploy an Application to AWS ECS with Docker](https://donny-nguyen.github.io/2026/07/03/develop-build-deploy-to-aws-ecs.html))

Managed Kubernetes services (EKS, GKE, AKS) remove much of the control plane burden, but you still own node management, upgrades, networking, and security configuration.

---

### Summary

Kubernetes turns a group of machines into one platform for running containers:

- The **control plane** (API server, etcd, scheduler, controllers) stores the desired state and makes decisions.
- **Worker nodes** (kubelet, container runtime, kube-proxy) run the workloads.
- **Pods** are the smallest unit; **Deployments**, **StatefulSets**, **DaemonSets**, and **Jobs** manage them.
- **Services**, **Ingress**, and the **Gateway API** handle networking and discovery.
- Everything is **declarative**: you describe what you want, and controllers continuously reconcile reality to match it.

Once you understand Pods, Deployments, Services, and the reconciliation loop, the rest of Kubernetes builds naturally on those ideas.
