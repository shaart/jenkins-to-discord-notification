# How to run
1. Specify ENV variables:

|Name|Description|
|----|-----------|
| JENKINS2DISCORD_DISCORD_WEBHOOK | Target Webhook where a message will be pushed. Your Discord Server -> Settings -> Integrations -> Webhooks -> Copy Webhook URL |
| JENKINS2DISCORD_JENKINS_ADDRESS | URL to your jenkins like: http://localhost:8080 |

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