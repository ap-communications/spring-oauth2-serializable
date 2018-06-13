package jp.co.ap_com.spring.oauth2;

import java.security.SecureRandom;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;

/**
 * Manage the state keys of cross site request forgeries.
 *
 * @author Kazuya HATTORI
 */
public class OAuth2CsrfManager {

	private static final String OAUTH2_SESSION_STATE = "OAUTH2_SESSION_STATE";

	private static final int OAUTH2_SESSION_STATE_LENGHT = 16;

	private static final char[] CODEC = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	public void removeState(HttpServletRequest request) {
		request.getSession().removeAttribute(OAUTH2_SESSION_STATE);
	}

	public boolean isValidState(HttpServletRequest request, AuthorizationCodeResponseUrl resUrl) {
		String responseState = resUrl.getState();
		if (responseState == null) {
			return false;
		}
		Object savedState = request.getSession().getAttribute(OAUTH2_SESSION_STATE);
		if (savedState == null) {
			return false;
		}
		return responseState.equals(savedState.toString());
	}

	public String generateAndSaveState(HttpServletRequest request) {
		String stateKey = generateState();
		request.getSession().setAttribute(OAUTH2_SESSION_STATE, stateKey);
		return stateKey;
	}

	private static String generateState() {
		Random random = new SecureRandom();
		byte[] bytes = new byte[OAUTH2_SESSION_STATE_LENGHT];
		random.nextBytes(bytes);
		char[] chars = new char[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			chars[i] = CODEC[((bytes[i] & 0xFF) % CODEC.length)];
		}
		return new String(chars);
	}

}
