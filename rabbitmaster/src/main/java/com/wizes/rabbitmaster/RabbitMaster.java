package com.wizes.rabbitmaster;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.wizes.rabbitmaster.handler.RabbitResponseHandler;

/**
 * @author HyunWoo Jo (showaid@cyberlogitec.com)
 *
 */
public class RabbitMaster {

	private final static Logger logger = Logger.getLogger(RabbitMaster.class.getName());
	private String cookies;
	private CloseableHttpClient httpClient;
	private String masterId;
	private String masterPassword;
	private String rabbitUrl;

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

	public String getRabbitURL() {
		return rabbitUrl;
	}

	public void setMasterid(String masterid) {
		this.masterId = masterid;
	}

	public void setMasterpassword(String masterpassword) {
		this.masterPassword = masterpassword;
	}

	public void setRabbitURL(String rabbitUrl) {
		// Make the URL ends with "/"
		// e.g. http://localhost:1975/
		if (!rabbitUrl.endsWith("/")) {
			this.rabbitUrl = rabbitUrl + "/";
		} else {
			this.rabbitUrl = rabbitUrl;
		}
	} 

	private String getCookies() {
		return cookies;
	}

	private void setCookies(String cookies) {
		this.cookies = cookies;
	}

	/**
	 * Grant a write permission to a user.
	 * @param userId
	 * @return a result message in JSONObject
	 */
	public JSONObject grantWritePerm(String userId) {
		JSONObject returnData = new  JSONObject();

		try {
			httpClient = HttpClients.createDefault();

			// make sure cookies is turn on
			CookieHandler.setDefault(new CookieManager());

			if (!loginSvc()) {
				returnData.put("status_code", "1");
				returnData.put("error_message", "Cannot login to the host! Check url, id, or password!" 
						+ rabbitUrl + ", " + masterId);;
						return returnData;
			} 

			String intId = searchUserSvc(userId);
			if (null == intId) {
				returnData.put("status_code", "1");
				returnData.put("error_message", "Cannot find the user! Check if user id exists : " + userId);;
				return returnData;
			}

			if (!grantWritePermSvc(intId)) {
				returnData.put("status_code", "1");
				returnData.put("error_message", "Cannot grant the write permission! Check if master id has a manage permission : ");;
				return returnData;
			}

			returnData.put("status_code", "0");

		} finally {
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Cannot close HttpClient instance!", e );
				}
			}
		}

		return returnData;
	}


	public JSONObject revokeWritePerm(String userId) {
		JSONObject returnData = new  JSONObject();

		try {
			httpClient = HttpClients.createDefault();

			// make sure cookies is turn on
			CookieHandler.setDefault(new CookieManager());

			if (!loginSvc()) {
				returnData.put("status_code", "1");
				returnData.put("error_message", "Cannot login to the host! Check url, id, or password!" 
						+ rabbitUrl + ", " + masterId);;
						return returnData;
			} 

			String intId = searchUserSvc(userId);
			if (null == intId) {
				returnData.put("status_code", "1");
				returnData.put("error_message", "Cannot find the user! Check if user id exists : " + userId);;
				return returnData;
			}

			if (!revokeWritePermSvc(intId)) {
				returnData.put("status_code", "1");
				returnData.put("error_message", "Cannot revoke the write permission! Check if master id has a manage permission : ");;
				return returnData;
			}

			returnData.put("status_code", "0");

		} finally {
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Cannot close HttpClient instance!", e );
				}
			}
		}

		return returnData;
	}

	public boolean revokeAllWritePerm() {
		return true;
	}

	/**
	 * This method is a helper method which implicitly uses a HttpClient of the class
	 * @return true if it is able to login
	 */
	private boolean loginSvc() {

		String loginUrl = rabbitUrl + "r/signon/login";
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		
		HttpPost httppost = new HttpPost(loginUrl);
		
		formparams.add(new BasicNameValuePair("userid", masterId));
		formparams.add(new BasicNameValuePair("password", masterPassword));
		formparams.add(new BasicNameValuePair("remember_me", ""));
		
		UrlEncodedFormEntity formentity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		httppost.setEntity(formentity);

		try {

			CloseableHttpResponse response = httpClient.execute(httppost);
			logger.log(Level.INFO, "Sending 'POST' request to URL : " + loginUrl);
			logger.log(Level.INFO, "Post parameters : " + formparams);
			int responseCode = response.getStatusLine().getStatusCode();
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

	/**
	 * This method is a helper method which implicitly uses a HttpClient of the class
	 * @return internal user id
	 */
	private String searchUserSvc(String userId) {

		int page = 1;
		String userIntId = null;
		Document htmlDoc = null;
		String userPageUrl = rabbitUrl + "r/user/page";

		String currentPageUrl = null;

		// Search the user until it find the user ID in a page 
		do {
			currentPageUrl = userPageUrl + "/" + page;
			htmlDoc = getHtmlDocSvc(currentPageUrl);

			if (null == htmlDoc) {
				logger.log(Level.WARNING, "Could not retrieve a response html at " + currentPageUrl);
				return null;
			} 
			page++;

			if (null != htmlDoc.select("a:contains(" + userId +")").first()) {
				userIntId = htmlDoc.select("a:contains(" + userId +")").first().parent().parent().attr("id");
				break;
			}
			// Loop while the page has rows which has id attribute
		} while (htmlDoc.getElementsByAttribute("id").size() > 0);

		return userIntId;
	}

	/**
	 * This method is a helper method which implicitly uses a HttpClient of the class
	 * @return true if it's able to grant a write permission
	 */
	private boolean grantWritePermSvc(String intId) {

		Document htmlDoc = null;
		String userUpdateUrl = rabbitUrl + "r/user/update/" + intId;
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		
		// Retrieve User Info
		htmlDoc = getHtmlDocSvc(userUpdateUrl);

		if (null == htmlDoc) {
			logger.log(Level.WARNING, "Cannot retrieve a html page at " + userUpdateUrl);
			return false;
		}
		
		
		// Set user name
		String userName = htmlDoc.getElementById("user_name").attr("value");
		if (null != userName) { 
			formparams.add(new BasicNameValuePair("user[name]", userName));
		}
		else {
			logger.log(Level.WARNING, "Please check if the page has proper format");
			return false;
		}
		
		// Set user email
		String userEmail = htmlDoc.getElementById("user_email").attr("value");
		if (null != userEmail) 
			formparams.add(new BasicNameValuePair("user[email]", userEmail));
		
		// Set user locale
		String userLocale = htmlDoc.getElementById("user_locale").select("select option[selected]").attr("value");
		if (null != userLocale) 
			formparams.add(new BasicNameValuePair("user[locale]", userLocale));
		
		// Set User Timezone
		String timeZone = htmlDoc.getElementById("user_time_zone").select("select option[selected]").attr("value");
		if (null != timeZone) 
			formparams.add(new BasicNameValuePair("user[time_zone]", timeZone));
		
		// Set User Roles
		Elements roles = htmlDoc.getElementsByAttributeValue("name", "role_type");

		for (Element role : roles) {
			// immediate return if the id has a write permission
			if ("4".equals(role.attr("value")) && role.hasAttr("checked")) {
				logger.log(Level.INFO, "The user already has a write permission");
				return true;
			}
				
			if (role.hasAttr("checked"))
				formparams.add(new BasicNameValuePair("role_type", role.attr("value")));
			
		}
		// Add a write role
		formparams.add(new BasicNameValuePair("role_type", "4"));
		
		// Set User Groups
		Elements groups = htmlDoc.getElementsByAttributeValue("name", "group");
		for (Element group : groups) {
			if (group.hasAttr("checked"))
				formparams.add(new BasicNameValuePair("group", group.attr("value")));
		}
		
		htmlDoc = postHtmlDocSvc(userUpdateUrl, formparams);
		
		if (null == htmlDoc) {
			logger.log(Level.WARNING, "Cannot retrieve a html page at " + userUpdateUrl);
			return false;
		}
		
		if (null != htmlDoc.getElementById("license_msg")) {
			logger.log(Level.WARNING, "In suffcient license");
			return false;
		}
		return true;
	}

	/**
	 * This method is a helper method which implicitly uses a HttpClient of the class
	 * @return true if it's able to revoke a write permission
	 */
	private boolean revokeWritePermSvc(String intId) {
		Document htmlDoc = null;
		String userUpdateUrl = rabbitUrl + "r/user/update/" + intId;
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		
		// Retrieve User Info
		htmlDoc = getHtmlDocSvc(userUpdateUrl);

		if (null == htmlDoc) {
			logger.log(Level.WARNING, "Cannot retrieve a html page at " + userUpdateUrl);
			return false;
		}
		
		// Set user name
		String userName = htmlDoc.getElementById("user_name").attr("value");
		if (null != userName) 
			formparams.add(new BasicNameValuePair("user[name]", userName));
		else {
			logger.log(Level.WARNING, "Please check if the page has proper format");
			return false;
		}
			
		// Set user email
		String userEmail = htmlDoc.getElementById("user_email").attr("value");
		if (null != userEmail) 
			formparams.add(new BasicNameValuePair("user[email]", userEmail));
		
		// Set user locale
		String userLocale = htmlDoc.getElementById("user_locale").select("select option[selected]").attr("value");
		if (null != userLocale) 
			formparams.add(new BasicNameValuePair("user[locale]", userLocale));
		
		// Set User Timezone
		String timeZone = htmlDoc.getElementById("user_time_zone").select("select option[selected]").attr("value");
		if (null != timeZone) 
			formparams.add(new BasicNameValuePair("user[time_zone]", timeZone));
		
		// Set User Roles
		Elements roles = htmlDoc.getElementsByAttributeValue("name", "role_type");

		for (Element role : roles) {
			if (role.hasAttr("checked") && !"4".equals(role.attr("value")))
				formparams.add(new BasicNameValuePair("role_type", role.attr("value")));
		}
		
		// Set User Groups
		Elements groups = htmlDoc.getElementsByAttributeValue("name", "group");
		for (Element group : groups) {
			if (group.hasAttr("checked"))
				formparams.add(new BasicNameValuePair("group", group.attr("value")));
		}
		
		
		htmlDoc = postHtmlDocSvc(userUpdateUrl, formparams);

		if (null == htmlDoc) {
			logger.log(Level.WARNING, "Cannot retrieve a html page at " + userUpdateUrl);
			return false;
		}
		
		return true;
	}

	
	private Document getHtmlDocSvc(String pageUrl) {
		Document htmlDoc = null;
		HttpGet httpGet = new HttpGet(pageUrl);
		ResponseHandler<Document> responseHandler = (ResponseHandler<Document>) new RabbitResponseHandler();

		try {
			logger.log(Level.INFO, "Sending 'GET' request to URL : " + pageUrl);
			htmlDoc = httpClient.execute(httpGet, responseHandler);
		} catch (IOException e)	{
			logger.log(Level.SEVERE, "Getting a Html Document failed!!", e);
			return null;
		} 
		return htmlDoc;
	}
	
	
	private Document postHtmlDocSvc(String pageUrl, List<NameValuePair> formparams ) {
		Document htmlDoc = null;
		HttpPost httpPost = new HttpPost(pageUrl);
		
		UrlEncodedFormEntity formentity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		httpPost.setEntity(formentity);
		ResponseHandler<Document> responseHandler = (ResponseHandler<Document>) new RabbitResponseHandler();
		
		try {
			logger.log(Level.INFO, "Sending 'POST' request to URL : " + pageUrl);
			logger.log(Level.INFO, "Post parameters : " + formparams);
			htmlDoc = httpClient.execute(httpPost, responseHandler);
		} catch (IOException e)	{
			logger.log(Level.SEVERE, "Getting a Html Document failed!!", e);
			return null;
		} 
		return htmlDoc;
	}
}
