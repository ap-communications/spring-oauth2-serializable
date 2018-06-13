package jp.co.ap_com.spring.oauth2;

import java.io.IOException;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.http.HttpRequest;

/**
 * Support json request.
 *
 * @see ClientParametersAuthentication
 * @author Kazuya HATTORI
 */
public class JsonClientParametersAuthentication extends ClientParametersAuthentication {

	public JsonClientParametersAuthentication(String clientId, String clientSecret) {
		super(clientId, clientSecret);
	}

	@Override
	public void intercept(HttpRequest request) throws IOException {
		request.getHeaders().setAccept("application/json");
		super.intercept(request);
	}
}
