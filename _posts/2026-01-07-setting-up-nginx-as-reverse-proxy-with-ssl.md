# Setting up Nginx as a Reverse Proxy with SSL

To use Nginx as a reverse proxy with SSL, you typically terminate HTTPS at Nginx (it handles TLS and certificates) and proxy plain HTTP traffic to your upstream application servers. This is done by defining HTTP and HTTPS `server` blocks, configuring certificates, and using `proxy_pass` plus standard `X-Forwarded-*` headers in your `location` blocks.[1][2][3][4]

## Basic reverse proxy (HTTP)

1. Edit a site config, for example `/etc/nginx/sites-available/example.conf`.[4]
2. Add a reverse proxy `server` block on port 80 pointing to your app (e.g. `http://127.0.0.1:3000`):[5][1]

```nginx
server {
    listen 80;
    server_name example.com www.example.com;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

3. Enable the site (`ln -s` into `sites-enabled` on Debian/Ubuntu) and test/reload Nginx with `nginx -t` and `systemctl reload nginx`.[4]

## Obtaining an SSL certificate

- For public domains, a common approach is Let’s Encrypt with Certbot, using the webroot or Nginx plugin to issue a certificate for `example.com`.[5]
- Certificates are typically stored under `/etc/letsencrypt/live/example.com/` with `fullchain.pem` and `privkey.pem` paths used in Nginx.[5]

Example (Certbot webroot pattern):  

```bash
sudo certbot certonly --webroot -w /var/www/example -d example.com -d www.example.com
```

## HTTPS reverse proxy with SSL termination

1. Add an HTTPS `server` block listening on 443 with SSL enabled and pointing to your certificate and key.[2][3]
2. Proxy to the same upstream, usually over HTTP, while keeping the same `proxy_set_header` set so your app knows the original scheme and client IP.[2][4]

```nginx
server {
    listen 443 ssl http2;
    server_name example.com www.example.com;

    ssl_certificate     /etc/letsencrypt/live/example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/example.com/privkey.pem;

    # Optional: basic SSL settings (use a hardened snippet in production)
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}
```

## Redirect HTTP to HTTPS

- Use a separate port 80 `server` that only redirects to HTTPS so all traffic is encrypted.[3][2]

```nginx
server {
    listen 80;
    server_name example.com www.example.com;
    return 301 https://$host$request_uri;
}
```

## Practical tips and best practices

- Keep a single reverse proxy host with multiple `server_name` blocks when serving many apps; use subdomains instead of long path prefixes to avoid cookie and routing issues.[6][4]
- Always test configuration with `nginx -t` before reloading, and use `X-Forwarded-Proto` and `X-Forwarded-For` consistently so upstream apps (like Node, PHP, or frameworks) reconstruct original URLs and client IP correctly.[1][4]

[1](https://www.scaleway.com/en/docs/tutorials/nginx-reverse-proxy/)
[2](https://www.digitalocean.com/community/tutorials/how-to-set-up-nginx-load-balancing-with-ssl-termination)
[3](https://docs.nginx.com/nginx/admin-guide/security-controls/terminating-ssl-http/)
[4](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)
[5](https://community.openhab.org/t/using-nginx-reverse-proxy-authentication-and-https/14542)
[6](https://www.reddit.com/r/nginx/comments/1c18agk/best_practice_for_reverse_proxy/)
[7](https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/How-to-setup-Nginx-reverse-proxy-servers-by-example)
[8](https://kinsta.com/blog/reverse-proxy/)
[9](https://docs.nginx.com/nginx/admin-guide/security-controls/terminating-ssl-tcp/)
[10](https://forums.truenas.com/t/guide-how-to-set-up-nginx-reverse-proxy-with-ssl-certificates-and-subdomains-pointing-to-your-applications-in-truenas-scale-without-having-to-pay-for-a-domain-by-using-duckdns-electric-eel/31712)