# How to run
* Specify required ENV variables:

|Name|Description|Default value|
|----|-----------|-------------|
| JENKINS2DISCORD_DISCORD_WEBHOOK | Target Webhook where a message will be pushed. Your Discord Server -> Settings -> Integrations -> Webhooks -> Copy Webhook URL | |
| JENKINS2DISCORD_JENKINS_ADDRESS | URL to your jenkins like: http://localhost:8080 | http://localhost:8080 |

* Specify additional ENV variables (if needed)

|Name|Description|Default value|
|----|-----------|-------------|
| JENKINS2DISCORD_REQUEST_READ-TIMEOUT | Time in millis to receive response | 5000 |
| JENKINS2DISCORD_REQUEST_CONNECT-TIMEOUT | Time in millis to establish a connection between hosts | 5000 |

#### Docker
```bash
docker run \ 
  -e JENKINS2DISCORD_DISCORD_WEBHOOK=https://discord.com/api/webhooks/<YOUR_WEBHOOK> \
  -e JENKINS2DISCORD_JENKINS_ADDRESS=<YOUR_JENKINS_ADDRESS> \
  --name jenkins2discord-notification \
  -p 8080:8080 \ 
  jenkins2discord-notification:latest
```

#### JRE
```bash
java \
  -d jenkins2discord.discord.webhook=https://discord.com/api/webhooks/<YOUR_WEBHOOK> \
  -d jenkins2discord.jenkins.address=<YOUR_JENKINS_ADDRESS> \
  -jar jenkins2discord-notification.jar
```