# AWS Load Balancer

**AWS Load Balancer** is a fully managed service that automatically distributes incoming application traffic across multiple targets, such as EC2 instances, containers, and IP addresses. This helps to improve the performance, availability, and scalability of our applications.

**Key benefits of using AWS Load Balancer:**

* **Improved performance:** By distributing traffic across multiple targets, AWS Load Balancer can help to reduce latency and improve response times.
* **Increased availability:** AWS Load Balancer can help to ensure that our applications remain available even if individual targets fail.
* **Enhanced scalability:** AWS Load Balancer can automatically scale up or down to meet changing traffic demands.
* **Simplified management:** AWS Load Balancer is a managed service, so we don't have to worry about managing and maintaining the underlying infrastructure.

**Types of AWS Load Balancers:**

AWS offers four types of load balancers, each operating at a different layer of the network stack and optimized for different workloads.

### 1. Application Load Balancer (ALB)

Operating at **Layer 7 (the application layer)** of the OSI model, the ALB makes routing decisions based on the content of the request, such as the URL path, HTTP headers, host name, query string, or HTTP method.

**Key features:**

* **Content-based routing:** Route requests to different target groups based on path (e.g., `/api/*` to one service, `/images/*` to another) or host (e.g., `api.example.com` vs. `www.example.com`).
* **Native support for microservices and containers:** Integrates tightly with Amazon ECS, EKS, and Lambda as targets.
* **HTTP/HTTPS and WebSocket support:** Handles modern web protocols, including HTTP/2 and gRPC.
* **Built-in security:** Supports SSL/TLS termination, integration with AWS WAF, and user authentication via Amazon Cognito or OIDC.

**Use cases:**

* Web applications and REST APIs that require request routing based on URL or headers.
* Microservices architectures where a single load balancer routes to many backend services.
* Container-based workloads running on ECS or EKS.
* Serverless applications that invoke Lambda functions in response to HTTP requests.

### 2. Network Load Balancer (NLB)

Operating at **Layer 4 (the transport layer)**, the NLB routes traffic based on IP protocol data (TCP, UDP, and TLS). It is designed for **ultra-high performance and low latency**, capable of handling millions of requests per second.

**Key features:**

* **Extreme performance:** Handles sudden and volatile traffic patterns with very low latency.
* **Static and Elastic IP support:** Provides a static IP address per Availability Zone, which is useful for firewall allow-listing.
* **Preserves the source IP address:** The client's IP is passed through to the target unchanged.
* **TLS termination:** Can offload TLS decryption while still operating at Layer 4.

**Use cases:**

* High-throughput, latency-sensitive applications such as gaming, real-time streaming, and IoT.
* TCP/UDP-based protocols that are not HTTP (e.g., MQTT, database connections).
* Applications that require a static or Elastic IP address for whitelisting.
* Workloads that need to scale to millions of requests per second.

### 3. Gateway Load Balancer (GWLB)

Operating at **Layer 3 (the network layer)**, the GWLB is used to deploy, scale, and manage a fleet of **third-party virtual network appliances**, such as firewalls, intrusion detection/prevention systems (IDS/IPS), and deep packet inspection tools.

**Key features:**

* **Transparent traffic inspection:** Uses the GENEVE protocol to route all traffic through security appliances without changing the packets.
* **Single entry and exit point:** Acts as a bump-in-the-wire for all network traffic flowing to and from your appliances.
* **Simplified appliance management:** Automatically scales appliances up or down and handles health checks.

**Use cases:**

* Inserting third-party firewalls or security appliances into the traffic path.
* Centralized network traffic inspection and packet filtering.
* Building security and compliance layers in front of application infrastructure.

### 4. Classic Load Balancer (CLB)

The CLB is the **legacy load balancer** that operates at both **Layer 4 and basic Layer 7**. It was the original Elastic Load Balancing offering and is now largely superseded by the ALB and NLB.

**Key features:**

* **Basic Layer 4 and Layer 7 routing** for simple TCP and HTTP/HTTPS traffic.
* **Limited feature set** compared to the newer load balancers (no content-based routing, no container-native integration).

**Use cases:**

* Existing applications built on the EC2-Classic network that have not yet migrated.
* Simple load balancing needs where advanced routing features are not required.
* **Note:** For new applications, AWS recommends using the ALB or NLB instead.

**Quick comparison:**

| Feature | ALB | NLB | GWLB | CLB |
|---|---|---|---|---|
| OSI Layer | 7 (Application) | 4 (Transport) | 3 (Network) | 4 & 7 |
| Protocols | HTTP, HTTPS, gRPC, WebSocket | TCP, UDP, TLS | IP (GENEVE) | TCP, SSL, HTTP, HTTPS |
| Content-based routing | Yes | No | No | No |
| Static IP | No (use with Global Accelerator) | Yes | No | No |
| Primary use case | Web apps, microservices | High-performance, low latency | Security appliances | Legacy applications |

**Distributing technique of AWS Load Balancer:**

* **Round Robin:** This is the simplest method, where traffic is distributed to each target in a round-robin fashion.
* **Least Connections:** Traffic is sent to the target with the fewest active connections.
* **Least Time:** Traffic is sent to the target with the shortest average response time.
* **Availability Zone:** Traffic is distributed across multiple Availability Zones to improve fault tolerance and availability.
* **Custom Health Checks:** Load Balancer can perform custom health checks on targets to determine their health status. This allows you to define specific criteria for a target to be considered healthy.
* **Weighted Round Robin:** You can assign weights to targets to influence how much traffic they receive. This can be useful for balancing traffic between targets with different capacities or performance characteristics.
* **Sticky Sessions:** This technique allows you to ensure that requests from the same client are always sent to the same target. This can be useful for applications that require stateful sessions.

By using AWS Load Balancer, we can improve the performance, availability, and scalability of our applications while reducing the complexity of our infrastructure.
