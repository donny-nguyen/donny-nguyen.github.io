# Setup Bitbucket Pipelines for Deployment

Bitbucket Pipelines is a CI/CD service built into Bitbucket. It allows us to run commands automatically when code is pushed to a branch. A common use case is deploying an application to a remote server whenever code is merged into a staging or production branch.

In this article, we will set up a Bitbucket Pipeline that connects to a remote Ubuntu server through SSH, pulls the latest code, creates the environment file, builds a Node.js application, and restarts the application with PM2.

***

### What Bitbucket Pipelines Needs

To use Bitbucket Pipelines, we need a file named `bitbucket-pipelines.yml` in the root of the repository.

This file tells Bitbucket:

* Which Docker image to use when running the pipeline
* Which branch should trigger the pipeline
* Which commands should run
* Which pipes or integrations should be used

For deployment through SSH, we also need:

* A remote server, such as an Ubuntu EC2 instance
* SSH access to that server
* The application already cloned or prepared on the server
* Node.js, Yarn, and PM2 installed on the server
* Repository variables configured in Bitbucket for sensitive values

***

### Example Pipeline Configuration

Here is an example `bitbucket-pipelines.yml` file. The server IP address is fake and uses a reserved documentation IP address.

```yaml
image: atlassian/default-image:3

pipelines:
  branches:
    staging:
      - step:
          name: Deploy to Staging
          script:
            - pipe: atlassian/ssh-run:0.4.2
              variables:
                SSH_USER: 'ubuntu'
                SSH_KEY: $BITBUCKET_SSH_KEY
                SERVER: '203.0.113.10'
                COMMAND: |
                  export NVM_DIR="/home/ubuntu/.nvm"
                  [ -s "/home/ubuntu/.nvm/nvm.sh" ] && \. "/home/ubuntu/.nvm/nvm.sh"
                  cd /var/www/sample-node-app
                  sudo chown -R ubuntu:ubuntu .
                  git config pull.rebase true
                  git checkout -f
                  git pull origin staging
                  echo "$ENV_FILE_CONTENTS" | base64 -d > .env
                  yarn install
                  yarn build
                  pm2 restart sample-node-app || pm2 start ecosystem.staging.config.cjs
                  pm2 save
```

***

### Explanation of the Configuration

```yaml
image: atlassian/default-image:3
```

This defines the Docker image used by Bitbucket to run the pipeline step.

`atlassian/default-image:3` is a general-purpose image provided by Atlassian. It includes common tools such as Git, SSH, and other utilities needed by many pipelines.

***

```yaml
pipelines:
  branches:
    staging:
```

This tells Bitbucket to run the pipeline only when code is pushed to the `staging` branch.

For example, when we merge a pull request into `staging`, Bitbucket automatically starts this deployment pipeline.

***

```yaml
- step:
    name: Deploy to Staging
```

A pipeline is made of one or more steps. This step is named `Deploy to Staging`, so it is easy to understand what the pipeline is doing in the Bitbucket UI.

***

```yaml
script:
  - pipe: atlassian/ssh-run:0.4.2
```

The `script` section contains the commands that run during the step.

Instead of writing a raw `ssh` command manually, this example uses the official `atlassian/ssh-run` pipe. A pipe is a reusable Bitbucket Pipeline component. The `ssh-run` pipe connects to a remote server and runs a command there.

***

```yaml
variables:
  SSH_USER: 'ubuntu'
  SSH_KEY: $BITBUCKET_SSH_KEY
  SERVER: '203.0.113.10'
```

These variables configure the SSH connection.

* `SSH_USER` is the Linux user on the remote server. For many Ubuntu servers, this is `ubuntu`.
* `SSH_KEY` is the private key used to connect to the server. It should be stored as a secured Bitbucket repository variable, not written directly in the YAML file.
* `SERVER` is the remote server address. In a real project, this would be the server IP address or domain name.

Never commit the real private key, real server IP address, passwords, tokens, or `.env` contents to the repository.

***

### Explanation of the Remote Commands

The `COMMAND` block contains commands that run on the remote server after the SSH connection is created.

```bash
export NVM_DIR="/home/ubuntu/.nvm"
[ -s "/home/ubuntu/.nvm/nvm.sh" ] && \. "/home/ubuntu/.nvm/nvm.sh"
```

These lines load NVM on the remote server.

When a command runs through SSH, it may not load the same shell profile files as an interactive terminal. Because of that, `node`, `npm`, or `yarn` may not be available unless NVM is loaded manually.

***

```bash
cd /var/www/sample-node-app
```

This moves into the application directory on the server.

The repository should already exist in this folder. If it does not exist yet, clone it first on the server before relying on this pipeline.

***

```bash
sudo chown -R ubuntu:ubuntu .
```

This changes ownership of the project files to the `ubuntu` user.

This is often used when previous commands created files as another user, such as `root`. Without correct ownership, commands like `git pull`, `yarn install`, or `yarn build` may fail because of permission errors.

***

```bash
git config pull.rebase true
```

This configures `git pull` to use rebase instead of merge.

It helps keep the server working tree history cleaner when pulling new changes.

***

```bash
git checkout -f
```

This resets tracked files in the working directory.

It is useful when the build process or manual server changes modified tracked files. However, it also means local changes to tracked files on the server will be discarded.

***

```bash
git pull origin staging
```

This pulls the latest code from the `staging` branch.

After this command finishes successfully, the server has the latest staging code.

***

```bash
echo "$ENV_FILE_CONTENTS" | base64 -d > .env
```

This creates the `.env` file from a base64-encoded environment variable.

The idea is:

1. Store the real `.env` contents in Bitbucket as a secured variable.
2. Encode the file content using base64.
3. Decode it during deployment and write it to `.env` on the server.

For example, locally we can encode an environment file like this:

```bash
base64 -w 0 .env
```

Then store the output in a secured Bitbucket variable such as `ENV_FILE_CONTENTS`.

Be careful with this part. Since the command runs on the remote server, the variable must be available to the command at execution time. Some teams pass the value into the SSH command from Bitbucket, while others store the environment file directly on the server and avoid rewriting it during every deployment.

***

```bash
yarn install
```

This installs project dependencies.

For more predictable builds, many projects use:

```bash
yarn install --frozen-lockfile
```

This makes sure the dependencies match the `yarn.lock` file.

***

```bash
yarn build
```

This builds the application for the staging environment.

For a frontend application, this usually creates optimized production files, such as a `dist`, `build`, or `.next` directory depending on the framework.

***

```bash
pm2 restart sample-node-app || pm2 start ecosystem.staging.config.cjs
```

This restarts the application using PM2.

The command uses `||`, which means:

* Try to restart the existing PM2 process named `sample-node-app`.
* If that process does not exist or restart fails, start the application using `ecosystem.staging.config.cjs`.

This is useful for the first deployment because there may not be an existing PM2 process yet.

***

```bash
pm2 save
```

This saves the current PM2 process list.

After running `pm2 save`, PM2 can restore the same process list later, for example after a server restart if PM2 startup has been configured.

***

### Setting Up Repository Variables in Bitbucket

Sensitive values should be stored in Bitbucket repository variables.

To add variables:

1. Open the Bitbucket repository.
2. Go to **Repository settings**.
3. Open **Repository variables** or **Deployment variables**.
4. Add the required variables.
5. Mark sensitive variables as secured.

Common variables for this pipeline are:

| Variable | Description |
| --- | --- |
| `BITBUCKET_SSH_KEY` | Private SSH key used to connect to the server |
| `ENV_FILE_CONTENTS` | Base64-encoded `.env` file content |
| `STAGING_SERVER` | Server IP address or domain name, if we do not want to hardcode it |

Using a variable for the server is usually better than writing the IP address directly in the YAML file:

```yaml
SERVER: $STAGING_SERVER
```

***

### Preparing the Server

Before the pipeline can deploy successfully, the remote server should be prepared.

Install the required tools:

```bash
sudo apt update
sudo apt install git -y
```

Install Node.js through NVM, then install Yarn and PM2:

```bash
npm install -g yarn pm2
```

Clone the project into the expected directory:

```bash
sudo mkdir -p /var/www/sample-node-app
sudo chown -R ubuntu:ubuntu /var/www/sample-node-app
git clone git@bitbucket.org:workspace-name/repository-name.git /var/www/sample-node-app
```

Make sure the server can pull from Bitbucket. Usually this means adding the server's public SSH key to Bitbucket as a repository access key or deployment key.

***

### Recommended Improvements

The example works as a simple deployment pipeline, but we can improve it.

Use repository variables for values that may change:

```yaml
SERVER: $STAGING_SERVER
```

Use a predictable dependency install:

```bash
yarn install --frozen-lockfile
```

Add safer shell behavior at the beginning of the remote command:

```bash
set -e
```

This makes the deployment stop when a command fails.

Also consider separating build and deploy. For larger applications, it is often better to build inside the pipeline, upload the build artifact to the server, and only restart the application on the server.

***

### Summary

This Bitbucket Pipeline deploys the `staging` branch to a remote Ubuntu server.

The flow is:

1. A push or merge happens on the `staging` branch.
2. Bitbucket starts the pipeline.
3. The `atlassian/ssh-run` pipe connects to the server.
4. The server loads NVM and moves into the app folder.
5. Git pulls the latest staging code.
6. The `.env` file is generated from a secured variable.
7. Dependencies are installed and the app is built.
8. PM2 restarts or starts the application.

This is a practical deployment setup for small to medium Node.js applications. For production systems, we should also add better rollback handling, deployment logs, health checks, and stricter secret management.
