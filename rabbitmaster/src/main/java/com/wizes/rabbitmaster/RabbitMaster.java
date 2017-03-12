package com.wizes.rabbitmaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class RabbitMaster {
	private String rabbitUrl;
	private String masterId;
	private String masterPassword;
	private String cookies;
	private CloseableHttpClient httpClient;
	private final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

	private final static Logger logger = Logger.getLogger(RabbitMaster.class.getName());

	public RabbitMaster(String rabbitUrl, String masterId, String masterPassword) {

		// Make the URL ends with "/"
		// e.g. http://localhost:1975/
		if (!rabbitUrl.endsWith("/")) {
			this.rabbitUrl = rabbitUrl + "/";
		} else {
			this.rabbitUrl = rabbitUrl;
		}

		this.masterId = masterId;
		this.masterPassword = masterPassword;
	}

	public String getMasterid() {
		return masterId;
	}

	public void setMasterid(String masterid) {
		this.masterId = masterid;
	}
	public String getMasterpassword() {
		return masterPassword;
	}
	public void setMasterpassword(String masterpassword) {
		this.masterPassword = masterpassword;
	}
	public String getRabbitURL() {
		return rabbitUrl;
	}
	public void setRabbitURL(String rabbitURL) {
		this.rabbitUrl = rabbitURL;
	}

	public boolean grantWritePerm(String userId) {
		login();
		searchUser(userId);
		return true;
	}

	public boolean revokeWritePerm(String userId) {
		login();

		return true;
	}

	public boolean revokeWritePermAll() {
		return true;
	}

	public boolean readPageAfterLogin() {
		
		try {
			
			httpClient = HttpClients.createDefault();
			// make sure cookies is turn on
			CookieHandler.setDefault(new CookieManager());
			logger.log(Level.INFO, "Loggin in");
			if (!login()) {
				logger.log(Level.INFO, "Login failed");
				return false;
			}
			logger.log(Level.INFO, "Retreiving a page");
			getPage("http://localhost:8080/r/user/list");
			return true;
			
		} finally {
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Cannot close HttpClient instance!", e );
				}
			}
		}
		
	}
	
	private void searchUser(String userId) {

	}

	private boolean login() {

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("userid", masterId));
		formparams.add(new BasicNameValuePair("password", masterPassword));
		formparams.add(new BasicNameValuePair("remember_me", ""));
		UrlEncodedFormEntity formentity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);

		String loginUrl = rabbitUrl + "r/signon/login";

		HttpPost httppost = new HttpPost(loginUrl);
		httppost.setEntity(formentity);

		try {

			CloseableHttpResponse response = httpClient.execute(httppost);
			int responseCode = response.getStatusLine().getStatusCode();

			logger.log(Level.INFO, "Sending 'POST' request to URL : " + loginUrl);
			logger.log(Level.INFO, "Post parameters : " + formparams);
			logger.log(Level.INFO, "Response Code : " + responseCode);

			if (responseCode != 302) {
				logger.log(Level.WARNING, "Login Failed");
				return false;
			}

			// set cookies
			setCookies(response.getFirstHeader("Set-Cookie") == null ? "" :
				response.getFirstHeader("Set-Cookie").toString());
			if (null != response) {
				try {
					response.close();
				} catch(IOException e) {
					logger.log(Level.SEVERE, "Cannot close response!", e);
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Login Failed!!", e);
			return false;
		}
		return true;
	}

	public String getPage(String url) {
		String result = "";
		HttpGet request = new HttpGet(url);
		request.setHeader("User-Agent", USER_AGENT);
		request.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		request.setHeader("Accept-Language", "en-US,en;q=0.5");
		request.setHeader("Cookie", getCookies());
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			CloseableHttpResponse response = httpClient.execute(request);

			int responseCode = response.getStatusLine().getStatusCode();
			logger.log(Level.INFO, "Sending 'GET' request to URL : " + url);
			logger.log(Level.INFO, "Response Code : " + responseCode);
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));

			StringBuffer resultBuf = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				resultBuf.append(line);
			}
			result = resultBuf.toString();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception happened while reding a page : " + url , e);
		}
		return result;
	}


	private String getCookies() {
		return cookies;
	}

	private void setCookies(String cookies) {
		this.cookies = cookies;
	} 
}
