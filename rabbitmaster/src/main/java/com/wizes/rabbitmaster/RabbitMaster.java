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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
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
	private final String USER_AGENT = "Mozilla/5.0";
	
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
		this.httpClient = HttpClients.createDefault();
		
		// make sure cookies is turn on
		CookieHandler.setDefault(new CookieManager());
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
	
	private void searchUser(String userId) {
		
	}
	
	public boolean login() {
		
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

			logger.log(Level.INFO, "\nSending 'POST' request to URL : " + loginUrl);
			logger.log(Level.INFO, "Post parameters : " + formparams);
			logger.log(Level.INFO, "Response Code : " + responseCode);

			BufferedReader rd = new BufferedReader(
		                new InputStreamReader(response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			
			logger.log(Level.INFO, result.toString());
			// set cookies
			setCookies(response.getFirstHeader("Set-Cookie") == null ? "" :
		                     response.getFirstHeader("Set-Cookie").toString());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException", e);
			return false;
		}
		return true;
	}

	private void setCookies(String cookies) {
		this.cookies = cookies;
	} 
}
