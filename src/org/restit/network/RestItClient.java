package org.restit.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.restit.model.ServerError;
import org.restit.model.serialization.ServerErrorDeserializer;
import org.restit.model.serialization.ServerErrorSerializer;
import org.restit.objectmapping.RestItMapper;

public class RestItClient {

	protected static final String CHARSET = "UTF-8";
	
	protected boolean allowInsecureConnection = false;
	protected SSLContext insecureSSLContext;
	protected String baseUrl;
	protected String contentType = ContentType.FORM;
	
	protected Map<String, String> defaultHeaders;
	
	 /** Timeout (in ms) we specify for each http request */
    protected int httpRequestTimeout = 30 * 1000;
	
	protected RestItClient()
	{
		//by default, register the error class
		RestItMapper.addClass("error", ServerError.class, new ServerErrorSerializer(), new ServerErrorDeserializer());
	}

	/**
	 * Does the client allow development certificates
	 * @return
	 */
	public boolean isAllowInsecureConnection() {
		return allowInsecureConnection;
	}

	/**
	 * Set if the client should allow development certificates
	 * @param allowInsecureConnection
	 */
	public void setAllowInsecureConnection(boolean allowInsecureConnection) {
		this.allowInsecureConnection = allowInsecureConnection;
	}

	/**
	 * Get the insecure SSLContext
	 * @return
	 */
	public SSLContext getInsecureSSLContext() {
		return insecureSSLContext;
	}

	/**
	 * Set the insecure SSLContext
	 * @param insecureSSLContext
	 */
	public void setInsecureSSLContext(SSLContext insecureSSLContext) {
		this.insecureSSLContext = insecureSSLContext;
	}

	/**
	 * Get the base URL for all requests
	 * @return string of the URL
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Set the base URL for all requests
	 * @param baseUrl
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * Get the default content type for each request
	 * @return
	 */
	public String getDefaultContentType() {
		return contentType;
	}

	/**
	 * Set the default content type for each request
	 * @param contentType
	 */
	public void setDefaultContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Get the number of milliseconds until the HTTP request will time out
	 * @return
	 */
	public int getHttpRequestTimeout() {
		return httpRequestTimeout;
	}

	/**
	 * Set the number of milliseconds until the HTTP request will time out
	 * @param httpRequestTimeout The time in milliseconds
	 */
	public void setHttpRequestTimeout(int httpRequestTimeout) {
		this.httpRequestTimeout = httpRequestTimeout;
	}

    /**
     * Configure the connection with the default settings
     * @param url The URL to connect to
     * @return HttpURLConnection
     */
    public HttpURLConnection getConnection(URL url) throws IOException
    {
    	return getConnection(url, RequestMethod.GET);
    }
    
    /**
     * Get the headers that should be included on each request
     */
    protected Map<String, String> getDefaultHeaders()
    {
    	if(this.defaultHeaders == null)
    	{
    		this.defaultHeaders = new HashMap<String, String>();
    	}
    	
    	return this.defaultHeaders;
    }
    
    /**
     * Set a header that will be included with every request
     * @param header The header name
     * @param value The value of the header
     */
    public void setDefaultHeader(String header, String value)
    {
    	getDefaultHeaders().put(header, value);
    }
    
    /**
     * Configure the connection with the default settings
     * @param url The URL to connect to
     * @param requestMethod The request method to use
     * @return HttpURLConnection
     */
    public HttpURLConnection getConnection(URL url, String requestMethod) throws IOException
    {
    	if(url == null)
    		return null;
    	
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(getHttpRequestTimeout());
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Charset", CHARSET);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type", getDefaultContentType()+";charset=" + CHARSET);
        connection.setRequestProperty("Accept", ContentType.JSON +", " + ContentType.TEXT_HTML + ", " + ContentType.TEXT_PLAIN + ", " + ContentType.XML);
        
        //see if we need to allow insecure connections
        if(isAllowInsecureConnection() && (connection instanceof HttpsURLConnection) )
        {
        	HttpsURLConnection secureConnection = (HttpsURLConnection)connection;
        	secureConnection.setSSLSocketFactory(getInsecureSSLContext().getSocketFactory());
        }
        
        //set default headers
        if(getDefaultHeaders().size() > 0)
        {
        	Iterator<String> it = getDefaultHeaders().keySet().iterator();
        	while(it.hasNext())
        	{
        		String header = it.next();
        		if(header != null)
        		{
        			String headerValue = getDefaultHeaders().get(header);
        			connection.setRequestProperty(header, headerValue);
        		}
        	}
        }
        
        return connection;
    }
	
}
