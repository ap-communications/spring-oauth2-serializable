# spring-oauth2-serializable

## Install

Maven usage:
```xml
    <dependency>
      <groupId>org.springframework.security.oauth</groupId>
      <artifactId>spring-security-oauth2</artifactId>
      <!-- available since 2.0.0.RELEASE  -->
      <!--
      <version>2.0.0.RELEASE</version>
      -->
    </dependency>
    <dependency>
      <groupId>jp.co.ap-com</groupId>
      <artifactId>spring-oauth2-serializable</artifactId>
      <version>0.0.1</version>
    </dependency>
```

And add repository settings
```xml
  <repositories>
    <repository>
      <id>github_ap-com_spring-oauth2-serializable</id>
      <name>github.com/ap-com/spring-oauth2-serializable</name>
      <url>https://raw.githubusercontent.com/ap-com/spring-oauth2-serializable/repo</url>
    </repository>
  </repositories>
```

## Settings

Add `@EnableOAuth2Serializable`.

Example:

```java
@EnableOAuth2Serializable
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  // ...
}
```

And make application.yml as follows.

Example(Github OAuth):

```yaml
security:
  oauth2:
    sso:
      login-path: /login
    client:
      clientId: [YOUR_CLIENT_ID]
      clientSecret: [YOUR_CLIENT_SECRET]
      accessTokenUri: https://github.com/login/oauth/access_token
      userAuthorizationUri: https://github.com/login/oauth/authorize
    resource:
      userInfoUri: https://api.github.com/user
```

If you are already using `spring-security-oauth2`, please change [like this](https://github.com/apc-hattori/spring-oauth2-demo/compare/use-spring-security-oauth2...master).

## Sample

[Demo aplication source](https://github.com/apc-hattori/spring-oauth2-demo)
