package jp.co.ap_com.spring.oauth2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerTokenServicesConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Enable OAuth2.
 *
 * @author Kazuya HATTORI
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableConfigurationProperties(OAuth2SsoProperties.class)
@Import({ OAuth2SecurityConfiguration.class, ResourceServerTokenServicesConfiguration.class })
@ComponentScan("jp.co.ap_com.spring.oauth2")
public @interface EnableOAuth2Serializable {
}
