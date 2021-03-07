# How to run
* Specify file `jenkins2discord.yaml` with following structure
```yaml
jenkins2discord:
  request:
    read-timeout: 5000
    connect-timeout: 5000
  jenkins:
    check-on-startup: false
    address: http://localhost:8080
    user:
      username: admin
      password: admin
    job-filters:
      - name:
        display-parameters:
        filter-by-parameters:
          - name:
            allowed-values:
  discord:
    webhook:
      url:
    message:
      username:
      avatar-url:
      prefix: "[JENKINS]"
      template-path: classpath:templates/discord_message.template
```
* Specify ENV variable: `SPRING_CONFIG_LOCATION: file:///F:/jenkins2discord.yaml` - your path to .yaml file
* Mount file into container if needed
* Specify required ENV variables if not specified in the `jenkins2discord.yaml`:

|Name|Since|Description|Default value|
|----|-----|-----------|-------------|
| JENKINS2DISCORD_DISCORD_WEBHOOK_URL | 0.0.1 | Target Webhook where a message will be pushed. Your Discord Server -> Settings -> Integrations -> Webhooks -> Copy Webhook URL | |
| JENKINS2DISCORD_JENKINS_ADDRESS | 0.0.1 |  URL to your jenkins like: http://localhost:8080 | http://localhost:8080 |
| JENKINS2DISCORD_JENKINS_USER_USERNAME | 0.0.1 |  Username to access Jenkins | admin |
| JENKINS2DISCORD_JENKINS_USER_PASSWORD | 0.0.1 |  Password to access Jenkins | admin |

* Specify additional ENV variables (if needed)

|Name|Since|Description|Default value|
|----|-----|-----------|-------------|
| JENKINS2DISCORD_DISCORD_MESSAGE_USERNAME | 0.0.1 |  Username that will be displayed in Discord's message | |
| JENKINS2DISCORD_DISCORD_MESSAGE_AVATAR-URL | 0.0.1 |  URL to image that will be displayed in Discord's message | |
| *[DEPRECATED SINCE 0.0.2]* JENKINS2DISCORD_DISCORD_MESSAGE_PREFIX | 0.0.1 |  *[DEPRECATED]* Prefix that will be displayed in Discord's message | [JENKINS] |
| JENKINS2DISCORD_DISCORD_MESSAGE_TEMPLATE-PATH | 0.0.2 | A path to discord message's template.<br>Default path specifies a path to embedded default template. See ["Discord message template"](#discord-message-template) for more details | classpath:templates/discord_message.template |
| JENKINS2DISCORD_REQUEST_READ-TIMEOUT | 0.0.1 |  Time in millis to receive response | 5000 |
| JENKINS2DISCORD_REQUEST_CONNECT-TIMEOUT | 0.0.1 |  Time in millis to establish a connection between hosts | 5000 |
| JENKINS2DISCORD_JENKINS_CHECK-ON-STARTUP | 0.0.4 | Is needed to check jenkins connection at startup | false |
| LOGGING_LEVEL_COM_GITHUB_SHAART_JENKINS2DISCORD_NOTIFICATION | 0.0.1 |  Log level for this application | INFO |

- Build a jar
```shell script
./gradlew bootJar
``` 

#### Docker
##### Build an image
```shell script
docker build -t jenkins2discord-notification -f docker/Dockerfile .
```
##### Run built image
```shell script
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
```shell script
java \
  -Djenkins2discord.discord.webhook.url=https://discord.com/api/webhooks/<YOUR_WEBHOOK> \
  -Djenkins2discord.jenkins.address=<YOUR_JENKINS_ADDRESS> \
  -Djenkins2discord.jenkins.user.username=<YOUR_JENKINS_USERNAME> \
  -Djenkins2discord.jenkins.user.password=<YOUR_JENKINS_PASSWORD> \
  -jar jenkins2discord-notification.jar
```

## Discord message template
### Variables

| Variable alias | Description | Result example |
|-----------------|-------------|---------|
|${JOB_NAME}| Jenkins job name | myJob |
|${BUILD_NUMBER}| Jenkins build number | 5 |
|${BUILD_PHASE}| Jenkins build phase | STARTED |
|${BUILD_STATUS}| Jenkins build status | SUCCESS |
|${BUILD_FULL_URL}| Jenkins build full url | http://my.jenkins.com/myJob/5/
|${JOB_USER}| User that started job in Jenkins | user.name
|${JOB_PARAMETERS}| Jenkins job'parameters | - GIT_PROJECT: j2d-notification<br> - GIT_BRANCH: develop|
### Default template (RU)
```text
[JENKINS] Сборка ${JOB_NAME} (#${BUILD_NUMBER}): фаза - ${BUILD_PHASE}, статус - ${BUILD_STATUS}
Ссылка на сборку: ${BUILD_FULL_URL}
Кто запустил: ${JOB_USER}
Параметры сборки: ${JOB_PARAMETERS}
```
#### Default template result example (RU)
```text
[JENKINS] Сборка myJob (#5): фаза - STARTED, статус - SUCCESS
Ссылка на сборку: http://my.jenkins.com/myJob/5/
Кто запустил: user.name
Параметры сборки: 
 - GIT_PROJECT: j2d-notification
 - GIT_BRANCH: develop
```

## Jenkins jobs filters
### Structure
```yaml
jenkins2discord:
  jenkins:
    job-filters:
      - name:
        display-parameters:
        filter-by-parameters:
          - name:
            allowed-values:
```
### Uses
- For each job (name is empty) display only parameter `GIT_BRANCH`:
```yaml

jenkins2discord:
  jenkins:
    job-filters:
      - name: 
        display-parameters: GIT_BRANCH
        filter-by-parameters:
          - name:
            allowed-values:
```
- For each job (name is empty) where there is a parameter `GIT_BRANCH`:
```yaml

jenkins2discord:
  jenkins:
    job-filters:
      - name: 
        display-parameters: 
        filter-by-parameters:
          - name: GIT_BRANCH
            allowed-values:
```
- For each job (name is empty) where there is a parameter `GIT_BRANCH` with value `develop` 
or `master`:
```yaml

jenkins2discord:
  jenkins:
    job-filters:
      - name: 
        display-parameters: 
        filter-by-parameters:
          - name: GIT_BRANCH
            allowed-values: develop,master
```
- Ignore all jobs except `firstJob` and `secondJob`:
```yaml

jenkins2discord:
  jenkins:
    job-filters:
      - name: firstJob
      - name: secondJob
```
- Ignore all jobs except `firstJob` and `secondJob`. Also display only parameters `GIT_BRANCH` 
and `NAMESPACE` from `secondJob`:
```yaml

jenkins2discord:
  jenkins:
    job-filters:
      - name: firstJob
      - name: secondJob
        display-parameters: GIT_BRANCH,NAMESPACE
```