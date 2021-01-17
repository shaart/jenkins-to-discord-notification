# How to run
* Specify required ENV variables:

|Name|Description|Default value|
|----|-----------|-------------|
| JENKINS2DISCORD_DISCORD_WEBHOOK_URL | Target Webhook where a message will be pushed. Your Discord Server -> Settings -> Integrations -> Webhooks -> Copy Webhook URL | |
| JENKINS2DISCORD_JENKINS_ADDRESS | URL to your jenkins like: http://localhost:8080 | http://localhost:8080 |
| JENKINS2DISCORD_JENKINS_USER_USERNAME | Username to access Jenkins | admin |
| JENKINS2DISCORD_JENKINS_USER_PASSWORD | Password to access Jenkins | admin |

* Specify additional ENV variables (if needed)

|Name|Description|Default value|
|----|-----------|-------------|
| JENKINS2DISCORD_DISCORD_MESSAGE_USERNAME | Username that will be displayed in Discord's message | |
| JENKINS2DISCORD_DISCORD_MESSAGE_AVATAR-URL | URL to image that will be displayed in Discord's message | |
| JENKINS2DISCORD_DISCORD_MESSAGE_PREFIX | Prefix that will be displayed in Discord's message | [JENKINS] |
| JENKINS2DISCORD_REQUEST_READ-TIMEOUT | Time in millis to receive response | 5000 |
| JENKINS2DISCORD_REQUEST_CONNECT-TIMEOUT | Time in millis to establish a connection between hosts | 5000 |
| LOGGING_LEVEL_COM_GITHUB_SHAART_JENKINS2DISCORD_NOTIFICATION | Log level for this application | INFO |

#### Docker
##### Build an image
```bash
docker build -t jenkins2discord-notification -f docker/Dockerfile .
```
##### Run built image
```bash
docker run \ 
  -e JENKINS2DISCORD_DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/<YOUR_WEBHOOK> \
  -e JENKINS2DISCORD_JENKINS_ADDRESS=<YOUR_JENKINS_ADDRESS> \
  -e JENKINS2DISCORD_JENKINS_USER_USERNAME=<YOUR_JENKINS_USERNAME> \
  -e JENKINS2DISCORD_JENKINS_USER_PASSWORD=<YOUR_JENKINS_PASSWORD> \
  --name jenkins2discord-notification \
  -p 8080:8080 \ 
  jenkins2discord-notification:latest
```

#### JRE
```bash
java \
  -Djenkins2discord.discord.webhook.url=https://discord.com/api/webhooks/<YOUR_WEBHOOK> \
  -Djenkins2discord.jenkins.address=<YOUR_JENKINS_ADDRESS> \
  -Djenkins2discord.jenkins.user.username=<YOUR_JENKINS_USERNAME> \
  -Djenkins2discord.jenkins.user.password=<YOUR_JENKINS_PASSWORD> \
  -jar jenkins2discord-notification.jar
```