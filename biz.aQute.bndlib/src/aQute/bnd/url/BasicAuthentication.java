package aQute.bnd.url;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import aQute.lib.base64.Base64;

/**
 * Provide Http Basic Authentication. This URL Connection Handler plugin will
 * add basic authentication to the matching URL Connections. The following
 * properties must be specified.
 * <ul>
 * <li>{@link aQute.bnd.service.url.URLConnectionHandler#MATCH MATCH} — The URL
 * {@link aQute.libg.glob.Glob Glob} expressions
 * <li>{@code .password} — The password for basic authentication
 * <li>{@code user} — The user ID
 * </ul>
 */
@aQute.bnd.annotation.plugin.BndPlugin(name = "url.basic.authentication", parameters = BasicAuthentication.Config.class)
public class BasicAuthentication extends DefaultURLConnectionHandler {

	interface Config extends DefaultURLConnectionHandler.Config {
		String user();

		String _password();
	}

	private static final String	USER					= "user";
	private static final String	PASSWORD				= ".password";
	private static final String	HEADER_AUTHORIZATION	= "Authorization";
	private static final String	PREFIX_BASIC_AUTH		= "Basic ";
	private String				password;
	private String				user;
	private String				authentication;

	public void setProperties(Map<String,String> map) throws Exception {
		super.setProperties(map);
		this.password = map.get(PASSWORD);
		this.user = map.get(USER);
		if (this.password == null) {
			error("No .password property set on this plugin %s", map);
		}
		if (this.password == null) {
			error("No user property set on this plugin %s", map);
		}
		String authString = user + ":" + password;
		try {
			String encoded = Base64.encodeBase64(authString.getBytes("UTF-8"));
			this.authentication = PREFIX_BASIC_AUTH + encoded;
		} catch (UnsupportedEncodingException e) {
			// cannot happen, UTF-8 is always present
		}
	}

	public void handle(URLConnection connection) {
		if (connection instanceof HttpURLConnection && matches(connection) && password != null && user != null) {
			if (!(connection instanceof HttpsURLConnection))
				trace("using basic authentication with http instead of https, this is very insecure: %s",
						connection.getURL());

			connection.setRequestProperty(HEADER_AUTHORIZATION, authentication);
		}
	}

}
