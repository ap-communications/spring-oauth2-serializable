package jp.co.ap_com.spring.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * Configurer for OAuth2.
 *
 * @author Kazuya HATTORI
 */
@Configuration
@Order(101)
public class OAuth2SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private OAuth2SsoProperties sso;

	@Autowired
	private AuthorizationCodeResourceDetails resourceDetails;

	@Autowired
	private UserInfoTokenServices tokenServices;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**").authorizeRequests().anyRequest().authenticated();
		http.addFilterBefore(oAuth2ProcessingFilter(), AbstractPreAuthenticatedProcessingFilter.class);
	}

	@Bean
	public OAuth2ProcessingFilter oAuth2ProcessingFilter() throws Exception {
		OAuth2ProcessingFilter filter = new OAuth2ProcessingFilter(sso.getLoginPath(), resourceDetails, tokenServices);
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}
}
