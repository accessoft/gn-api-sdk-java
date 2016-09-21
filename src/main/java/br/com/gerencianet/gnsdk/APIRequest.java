package br.com.gerencianet.gnsdk;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.json.JSONObject;

import br.com.gerencianet.gnsdk.exceptions.AuthorizationException;
import br.com.gerencianet.gnsdk.exceptions.GerencianetException;

/**
 * This class instance a Auth Object, to authenticate client credentials in Gerencianet API. After client's credentials
 * are validated a client Object send a given request body to a given endpoint throw a given route.
 * @author Filipe Mata
 *
 */
public class APIRequest {
	private Request requester;
	private Auth authenticator;
	private String  route;
	
	
	public APIRequest(String method, String route, Config config) throws Exception {
		this.route = route;
		String authenticateRoute = config.getEndpoints().getJSONObject("authorize").getString("route");
		String authenticateMethod = config.getEndpoints().getJSONObject("authorize").getString("method");
		this.authenticator = new Auth(config.getOptions(), authenticateMethod, authenticateRoute);
		
		String url = config.getOptions().getString("baseUri") + route;
		URL link = new URL(url);
		HttpURLConnection client = (HttpURLConnection) link.openConnection();
		
		this.requester = new Request(method, client);
	}
	
	public APIRequest(Auth auth, Request request){
		this.authenticator = auth;
		this.requester = request;
	}
	
	public JSONObject send(JSONObject body) throws AuthorizationException, GerencianetException, IOException{
		Date expiredDate = this.authenticator.getExpires();
		if (this.authenticator.getExpires() == null || expiredDate.compareTo(new Date()) <= 0) {
			this.authenticator.authorize(); 
        }
		
		this.requester.addHeader("Authorization", "Bearer " + this.authenticator.getAccessToken());
        try {
			return this.requester.send(body);
		} catch (AuthorizationException e) {
			this.authenticator.authorize();
			return this.requester.send(body);
		}
	}
	
	public Request getRequester() {
		return requester;
	}
	
	public String getRoute() {
		return route;
	}
}