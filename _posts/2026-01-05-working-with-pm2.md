# Working with PM2

PM2 is a process manager for Node.js applications that helps you keep your apps running continuously. Here's how to work with it:

**Installation**

```bash
npm install pm2 -g
```

**Basic Usage**

Start an application:
```bash
pm2 start app.js
pm2 start app.js --name "my-app"  # with a custom name
```

View running processes:
```bash
pm2 list
pm2 status
```

Monitor logs:
```bash
pm2 logs           # all apps
pm2 logs my-app    # specific app
pm2 logs --lines 100  # last 100 lines
```

Manage processes:
```bash
pm2 stop my-app
pm2 restart my-app
pm2 delete my-app
pm2 reload my-app  # zero-downtime reload
```

**Advanced Features**

Run with a configuration file (ecosystem.config.js):
```javascript
module.exports = {
  apps: [{
    name: "my-app",
    script: "./app.js",
    instances: 2,  // or "max" for cluster mode
    exec_mode: "cluster",
    env: {
      NODE_ENV: "production"
    }
  }]
}
```

Then start with:
```bash
pm2 start ecosystem.config.js
```

**Auto-restart on system reboot**

```bash
pm2 startup
pm2 save
```

**Monitoring**

```bash
pm2 monit  # real-time monitoring dashboard
```

PM2 is especially useful for keeping your Node.js apps running in production, automatically restarting them if they crash, and managing multiple instances for load balancing.