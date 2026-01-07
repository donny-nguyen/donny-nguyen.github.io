# Reverse Proxy

A reverse proxy is a **server** that sits in front of one or more backend web servers and forwards client requests to them, then returns the responses as if it were the original server. It acts as an intermediary layer that improves security, performance, and manageability of web applications.[1][2][6]

## Core idea

- Clients (browsers, apps) send requests to the reverse proxy’s address, not directly to the backend servers.[3][1]
- The reverse proxy decides which backend server should handle each request, forwards it, receives the response, and sends it back to the client.[2][1]

## Why it’s used

- **Security**: Hides real server IPs, filters malicious traffic, and can enforce authentication and SSL/TLS at the edge.[4][5][1]
- **Performance**: Caches responses, compresses content, and offloads expensive work like SSL termination, reducing load on backend servers.[3][4]

## Load balancing role

- Distributes incoming traffic across multiple backend servers to avoid overloading any single machine and to increase availability.[7][2][4]
- Can implement health checks and failover, routing around unhealthy servers to keep the service online.[10][4]

## Reverse proxy vs forward proxy

- Forward proxy: Sits in front of clients, hides client identity, and is often used for privacy, filtering, or outbound access control.[6][3]
- Reverse proxy: Sits in front of servers, hides server identity, and is used for load balancing, caching, and securing internal services.[9][6][3]

[1](https://www.fortinet.com/resources/cyberglossary/reverse-proxy)
[2](https://en.wikipedia.org/wiki/Reverse_proxy)
[3](https://www.geeksforgeeks.org/computer-networks/what-is-a-reverse-proxy/)
[4](https://stytch.com/blog/what-is-a-reverse-proxy/)
[5](https://www.zscaler.com/resources/security-terms-glossary/what-is-reverse-proxy)
[6](https://www.cloudflare.com/learning/cdn/glossary/reverse-proxy/)
[7](https://www.vmware.com/topics/reverse-proxy-server)
[8](https://www.reddit.com/r/unRAID/comments/zcw7ik/what_exactly_does_reverse_proxy_do/)
[9](https://www.upguard.com/blog/what-is-a-reverse-proxy)
[10](https://www.imperva.com/learn/performance/reverse-proxy/)