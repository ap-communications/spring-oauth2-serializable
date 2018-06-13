package jp.co.ap_com.spring.oauth2;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential.AccessMethod;
import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * An OAuth2 client filter that process the OAuth2 flow.
 *
 * @author Kazuya HATTORI
 */
public class OAuth2ProcessingFilter extends AbstractAuthenticationProcessingFilter {

	private static final Log logger = LogFactory.getLog(OAuth2ProcessingFilter.class);

	private final Lock lock = new ReentrantLock();

	private final OAuth2CsrfManager csrfManager = new OAuth2CsrfManager();

	private AuthorizationCodeFlow flow;

	private AuthorizationCodeResourceDetails resourceDetails;

	private UserInfoTokenServices tokenServices;

	protected OAuth2ProcessingFilter(String loginPath,
			AuthorizationCodeResourceDetails authorizationCodeResourceDetails, UserInfoTokenServices tokenServices) {
		super(loginPath);
		this.resourceDetails = authorizationCodeResourceDetails;
		this.tokenServices = tokenServices;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		String code;
		try {
			code = getAuthorizationCode(request);
		} catch (HttpStatusCodeException e) {
			response.setStatus(e.getRawStatusCode());
			return null;
		}
		if (code == null) {
			this.setupRedirect(request, response);
			return null;
		}
		lock.lock();
		try {
			String currentUrl = getCurrentUrl(request);
			TokenRequest tokenRequest = getFlow().newTokenRequest(code).setRedirectUri(currentUrl);
			TokenResponse tokenResponse = tokenRequest.execute();
			return tokenServices.loadAuthentication(tokenResponse.getAccessToken());
		} finally {
			lock.unlock();
		}
	}

	private String getAuthorizationCode(HttpServletRequest request) {
		AuthorizationCodeResponseUrl resUrl;
		try {
			resUrl = new AuthorizationCodeResponseUrl(getCurrentUrl(request, true));
		} catch (IllegalArgumentException e) {
			return null;
		}
		if (resUrl.getError() != null) {
			logger.error(resUrl.getError());
			throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (!csrfManager.isValidState(request, resUrl)) {
			throw new HttpServerErrorException(HttpStatus.FORBIDDEN);
		}
		csrfManager.removeState(request);
		return resUrl.getCode();
	}

	private void setupRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
		lock.lock();
		try {
			String currentUrl = getCurrentUrl(request);
			String stateKey = csrfManager.generateAndSaveState(request);
			AuthorizationCodeRequestUrl authUrl = getFlow().newAuthorizationUrl();
			authUrl.setRedirectUri(currentUrl);
			authUrl.setResponseTypes(Arrays.asList("code"));
			authUrl.setState(stateKey);
			response.sendRedirect(authUrl.build());
		} finally {
			lock.unlock();
		}
	}

	private String getCurrentUrl(HttpServletRequest request) {
		return getCurrentUrl(request, false);
	}

	private String getCurrentUrl(HttpServletRequest request, boolean full) {
		StringBuffer currentUrl = request.getRequestURL();
		if (full) {
			String query = request.getQueryString();
			if (query != null) {
				currentUrl.append("?").append(request.getQueryString());
			}
		}
		return currentUrl.toString();
	}

	private AuthorizationCodeFlow getFlow() {
		if (flow == null) {
			flow = initializeFlow();
		}
		return flow;
	}

	private AuthorizationCodeFlow initializeFlow() {
		AuthenticationScheme scheme;
		if (resourceDetails.getAuthenticationScheme() != null) {
			scheme = resourceDetails.getAuthenticationScheme();
		} else {
			scheme = AuthenticationScheme.header;
		}
		AccessMethod method;
		switch (scheme) {
		case header:
			method = BearerToken.authorizationHeaderAccessMethod();
			break;
		case form:
			method = BearerToken.formEncodedBodyAccessMethod();
			break;
		case query:
			method = BearerToken.queryParameterAccessMethod();
			break;
		default:
			throw new IllegalStateException(
					"Default authentication handler doesn't know how to handle scheme: " + scheme);
		}
		String clientId = resourceDetails.getClientId();
		String clientSecret = resourceDetails.getClientSecret();
		String accessTokenUri = resourceDetails.getAccessTokenUri();
		String userAuthorizationUri = resourceDetails.getUserAuthorizationUri();
		HttpExecuteInterceptor clientAuthentication = new JsonClientParametersAuthentication(clientId, clientSecret);
		return new AuthorizationCodeFlow.Builder(method, new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
				new GenericUrl(accessTokenUri), clientAuthentication, clientId, userAuthorizationUri).build();
	}
}
