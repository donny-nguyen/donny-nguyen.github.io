# Nginx Overview

Nginx is high‑performance, open‑source software that primarily works as a **web** server and reverse proxy, often sitting in front of application servers to handle client HTTP/HTTPS traffic efficiently. It is designed around an event‑driven, asynchronous architecture that lets a small number of worker processes handle thousands of concurrent connections with low memory usage.[2][3][4][5]

## Core purpose

- Nginx serves static web content such as HTML, CSS, JavaScript, and images directly to clients very efficiently, which makes it popular for high‑traffic sites.[3][8]
- It can also forward requests to upstream application servers (for example Node.js, PHP‑FPM, or other backends) while handling buffering, caching, and connection management.[4][2]

## Key roles

- Web server: Provides full HTTP/HTTPS server capabilities and can host websites and APIs on its own.[1][8]
- Reverse proxy and load balancer: Receives client requests, distributes them across multiple backend servers, and can perform health checks, SSL/TLS termination, and caching.[2][3]
- Other proxy functions: Can act as a mail proxy (IMAP, POP3, SMTP) and as a generic TCP/UDP proxy in modern deployments.[4][2]

## Architecture and performance

- Nginx uses a master process that manages multiple worker processes; workers handle the actual network requests using an event‑driven model instead of spawning one thread or process per connection.[5][3]
- This architecture helps Nginx remain stable and predictable under high load while keeping CPU and memory usage relatively low compared to traditional process‑per‑request servers.[6][2]

## Common use cases

- Fronting application servers as a reverse proxy, handling SSL, gzip compression, and caching to offload expensive work from app code.[5][2]
- Acting as an edge component in modern architectures (microservices, containers, Kubernetes) for routing, API gateway behavior, and performance optimization.[1][3]

## Variants

- Nginx Open Source: Free version widely used to serve and proxy web traffic on Linux and other operating systems.[8][3]
- Nginx Plus: Commercial edition that adds advanced features like active health checks, additional load‑balancing methods, and richer monitoring and management APIs.[3][2]

[1](https://www.papertrail.com/solution/guides/nginx/)
[2](https://www.f5.com/glossary/nginx)
[3](https://www.hostinger.com/tutorials/what-is-nginx)
[4](https://en.wikipedia.org/wiki/Nginx)
[5](https://kinsta.com/blog/what-is-nginx/)
[6](https://www.geeksforgeeks.org/operating-systems/what-is-nginx-web-server-and-how-to-install-it/)
[7](https://www.reddit.com/r/nginx/comments/mvatwk/what_is_nginx_explain_to_me_like_im_5_because_im/)
[8](https://nginx.org/en/)
[9](https://www.youtube.com/watch?v=iInUBOVeBCc)
[10](https://nginx.org/en/docs/beginners_guide.html)