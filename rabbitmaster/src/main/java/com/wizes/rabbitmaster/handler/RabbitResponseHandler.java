package com.wizes.rabbitmaster.handler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class RabbitResponseHandler implements ResponseHandler<Document> {
	private final static Logger logger = Logger.getLogger(RabbitResponseHandler.class.getName());
	
	public Document handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
		Document returnDoc = null;
		logger.log(Level.INFO, "Checking Response Status");
		if (status >= 200 && status < 300) {
			HttpEntity entity = response.getEntity();
			try {
				if (null == entity) {
					logger.log(Level.WARNING, "Null data found");
				} else {
					returnDoc = Jsoup.parse(EntityUtils.toString(entity));
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Exception Occurred!", e);
			} 
		} else {
			logger.log(Level.WARNING, "Unexpected response code: " + status);
		}
		return returnDoc;
	}

}
