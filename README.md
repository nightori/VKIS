# VKIS
## Description
This is the back-end part of VKIS, which is hosted [here](https://vkis.nightori.ru).

It's a service that lets you find an image in a VK album (or check if it exists). An example of a possible application: go through a group's wall pictures album to find if something was already posted. 

You can use a direct link to an image or upload it from your device.
## Dependencies
This application is built with [Spring Framework](https://spring.io/projects/spring-framework). The following modules are used:
- Spring Boot
- Spring Web
- Spring Security

It also uses [VK Java SDK](https://github.com/VKCOM/vk-java-sdk) for VK interaction.
## Running it yourself
Use Gradle to run or build this, it's a regular Spring Boot application. You will also need to set the required env variables.

Use [this](src/main/resources/application.properties.sample) as a sample. The variables are:
 - APP_URL - full link to the main page
 - APP_ID - VK application ID
 - REDIRECT_URI - VK redirect URI
 - CLIENT_SECRET - VK application's  secret key
