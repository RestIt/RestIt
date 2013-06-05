package org.restit.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.json.JSONObject;
import org.restit.model.ServerError;
import org.restit.network.CustomMultiPartEntity.ProgressListener;
import org.restit.network.insecure.NullHostNameVerifier;
import org.restit.network.insecure.NullX509TrustManager;
import org.restit.objectmapping.RestItMapper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class RestIt {

	//Logging tag
	private static final String LOG_TAG = "RestIt";
	private static final String SLASH_CHAR = "/";

	
	protected static RestItClient client;
 
    
    /**
     * Get the complete URL for a request
     * @param path The path to REST service
     * @return Complete URL with base URL and given path
     */
    protected static String getUrlWithPath(String path)
    {
    	if(path != null && path.length() > 0)
		{
			//remove forward slash if it exists. The BASE URL will already have a trailing slash
			String first = path.substring(0,1);
			if(first.equals(SLASH_CHAR))
			{
				path = path.substring(0);
			}
		}
    	
    	return getClient().getBaseUrl() + path;
    }
    
	
	/**
	 * Create the shared RestIt manager
	 * @return
	 */
	private static RestItClient getClient()
	{
		if(client == null)
		{
			client = new RestItClient();
		}
		
		return client;
	}
	
	
	
	/**
	 * Should the client allow connections to insecure servers. THIS SHOULD NOT BE SET TO
	 *  TRUE IN PRODUCTION!
	 * @param value
	 */
	public static void setAllowInsecureConnections(boolean value, InputStream certificateStream)
	{
		getClient().setAllowInsecureConnection(value);
		
        CertificateFactory cf;
		try {
			cf = CertificateFactory.getInstance("X.509");
		
	        Certificate ca;
	        
	        try {
	        	ca= cf.generateCertificate(certificateStream);
	        	System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
	        
			} finally {
				certificateStream.close();
			}

			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);
		
			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);
		
			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
			HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
		
			//store the context
			getClient().setInsecureSSLContext( context );
		
		} catch (Exception e) {
			
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Set the base URL
	 * @param url The base 
	 */
	public static void setBaseUrl(String url)
	{		
		if(url != null && url.length() > 0)
		{
			//add trailing slash if it does not exist
			String lastCharacter = url.substring(url.length()-1);
			if(!lastCharacter.equals(SLASH_CHAR))
			{
				url += SLASH_CHAR;
			}
		}
		
		getClient().setBaseUrl( url );
	}
	
	/**
	 * Set the default content type for the server
	 * @param type
	 */
	public static void setContentType(String type)
	{
		getClient().setDefaultContentType( type );
	}
	
	/**
	 * Set the connection timeout length
	 * @param time Time in seconds
	 */
	public static void setTimeout(int time)
	{
		getClient().setHttpRequestTimeout( time * 1000 );
	}
	
	/**
	 * Is the network currently available
	 * @return
	 */
	public static boolean isNetworkAvailable(Context mContent)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) mContent.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

	    if (networkInfo != null && networkInfo.isConnected()) {
	        return true;
	    } else
	    {
	    	return false;
	    }
	}
	
	/**
	 * Set a header value that will be included on every request. Useful for headers like authentication tokens or cookies
	 * @param header The header name
	 * @param value Value of the header
	 */
	public static void setDefaultHeader(String header, String value)
	{
		if(header == null || value == null)
			return;
		
		getClient().setDefaultHeader(header, value);
	}
	
	/**
	 * Make a GET request to the given path
	 * @param path The path to the REST service, not the full URL
 	 * @param callback The code that will be executed upon completion by the server
	 * @throws ServerErrorException 
	 */
	public static Object get(String path) throws ServerErrorException
	{
		
		//make sure that base URL has been set
		if(getClient().getBaseUrl() == null)
		{
			Log.e(LOG_TAG, "Could not make GET request because a base URL has not been set. Please use RestIt.setBaseUrl().");
			return null;
		}

		String fullUrlValue = getUrlWithPath(path);
		
		HttpURLConnection connection = null;
		
		try {
			
			Log.d(LOG_TAG, "Starting GET request to: " +fullUrlValue);
			
			URL fullUrl = new URL(fullUrlValue);
			
			//make server call
			connection = getClient().getConnection(fullUrl);

			String result = processConnection(connection);
			
			//convert to POJO
			Object pojo = RestItMapper.toPojo(result);
			if(pojo != null)
			{
				//found pojo
				return pojo;
			} else
			{
				//return raw response
				return result;
			}
			
			
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
			
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
		} finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}
		
		return null;
		
	}
	
	/**
	 * Make a POST request to the given path
	 * @param path The path to the REST service, not the full URL
	 * @param postObjectBytes Take the string representation of the parameters and put them in byte format
 	 * @param callback The code that will be executed upon completion by the server
	 * @throws ServerErrorException 
	 */
	public static Object post(String path, JSONObject jsonObject) throws ServerErrorException
	{
		//turn 
		byte[] bytes = null;
		
		if(jsonObject != null)
		{
			//convert json object to bytes
			try {
				bytes = jsonObject.toString().getBytes(RestItClient.CHARSET);
			} catch (UnsupportedEncodingException e) {
				//log error but continue
				Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			}
		}
		
		//complete post
		return post(path, bytes);
	}
	
	/**
	 * Make a POST request to the given path
	 * @param path The path to the REST service, not the full URL
	 * @param postObjectBytes Take the string representation of the parameters and put them in byte format
 	 * @param callback The code that will be executed upon completion by the server
	 * @throws ServerErrorException 
	 */
	public static Object post(String path, byte[] postObjectBytes) throws ServerErrorException
	{
		
		//make sure that base URL has been set
		if(getClient().getBaseUrl() == null)
		{
			Log.e(LOG_TAG, "Could not make POST request because a base URL has not been set. Please use RestIt.setBaseUrl().");
			return null;
		}

		String fullUrlValue = getUrlWithPath(path);
		
		HttpURLConnection connection = null;
		
		try {
			
			Log.d(LOG_TAG, "Starting POST request to: " +fullUrlValue);
			
			URL fullUrl = new URL(fullUrlValue);
			
			//make server call
			connection = getClient().getConnection(fullUrl, RequestMethod.POST);
			
			if(postObjectBytes != null)
			{
				//attach post objects
				OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
				outputStream.write(postObjectBytes);
				outputStream.flush();
				outputStream.close();
			}

			//get response from server
			String result = processConnection(connection);
			
			//convert to POJO
			Object pojo = RestItMapper.toPojo(result);
			
			if(pojo != null)
			{
				//found pojo
				return pojo;
			} else
			{
				//return raw response
				return result;
			}
			
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
			
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
		} finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}
		
		return null;
		
	}
	
	/**
	 * Make POST request to given path using a multipart form
	 * @param path Url path not including server name
	 * @param postObjectBytes The byte array of the object to post
	 * @param formElementName The name of element in the HTML form
	 * @param fileName The file name of the file
	 * @return
	 * @throws ServerErrorException
	 */
	public static Object multipartPostObject(String path, byte[] postObjectBytes, String formElementName, String fileName, ProgressListener progressListener) throws ServerErrorException
	{
		//make sure that base URL has been set
		if(getClient().getBaseUrl() == null)
		{
			Log.e(LOG_TAG, "Could not make multipart POST request because a base URL has not been set. Please use RestIt.setBaseUrl().");
			return null;
		}

		String fullUrlValue = getUrlWithPath(path);
		
		HttpURLConnection connection = null;
		
		try {
			
			Log.d(LOG_TAG, "Starting multipart POST request to: " +fullUrlValue);
			
			URL fullUrl = new URL(fullUrlValue);
			
			//make server call
			connection = getClient().getConnection(fullUrl, RequestMethod.POST);
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			//create multipart entity
			ContentBody contentPart = new ByteArrayBody(postObjectBytes, fileName);
			MultipartEntity reqEntity = null;
			
			if(progressListener != null)
			{
				//create custom multipart entity so that we can keep the application informed of progress
				reqEntity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, progressListener);
			} else
			{
				//normal multipart request
				reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			}
			 
		    reqEntity.addPart(formElementName, contentPart);
			
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
			connection.setRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());
			
			if(postObjectBytes != null)
			{
				//attach post objects
				OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
				reqEntity.writeTo(outputStream);
				outputStream.flush();
				outputStream.close();
			}

			//get response from server
			String result = processConnection(connection);
			
			//convert to POJO
			Object pojo = RestItMapper.toPojo(result);
			
			if(pojo != null)
			{
				//found pojo
				return pojo;
			} else
			{
				//return raw response
				return result;
			}
			
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
			
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
		} finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}
		
		return null;
	}
	
	/**
	 * Handle core processing, like redirects
	 * @param connection
	 * @return
	 * @throws ServerErrorException 
	 */
	protected static String processConnection(HttpURLConnection connection) throws IOException, ServerErrorException
	{
		int status = ((HttpURLConnection) connection).getResponseCode();
		
		//figure out the response
		if (status == HttpStatus.SC_OK) {
			//200 OK
            InputStream istream = new BufferedInputStream(connection.getInputStream());
            
            //parse
            String result = NetworkUtil.parseStream(istream);
            
            Log.d(LOG_TAG, "Received response: " + result);
            
            //close connection
            istream.close();
            
            //execute callback to originating code
            return result;
            
		} else if(status == HttpURLConnection.HTTP_NOT_FOUND )
		{
			//404, URL not found
			String errorMessage = "The requested URL '"+connection.getURL().toString()+"' does not exist";
			Log.e(LOG_TAG, errorMessage);
			
			ServerError error = new ServerError(errorMessage);
			error.setServerCode(status);
			throw new ServerErrorException(error);
			
		} else if(status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
		{
			//3XX redirect
			Log.e(LOG_TAG, "The requested URL is redirecting us to ");
			
			//get redirect data from old connection
			String newUrl = connection.getHeaderField("Location");
			String cookies = connection.getHeaderField("Set-Cookie");
			
			//create new connection
			HttpURLConnection newConnection = getClient().getConnection(new URL(newUrl));
			newConnection.setRequestProperty("Cookie", cookies);
			newConnection.setRequestMethod(connection.getRequestMethod());
			
			//close old connection
			connection.disconnect();
			
			//process new connection
			return processConnection(newConnection);
			
		}
		
		//We don't have a 200 OK response but see if we have a readable error
		
		//parse error stream
		InputStream istream = new BufferedInputStream(connection.getErrorStream());
        String result = NetworkUtil.parseStream(istream);
		
        //log
        Log.d(LOG_TAG, "Received possible error response: " + result);
        
		if(result != null)
		{
			Object error = RestItMapper.toPojo(result);
			if(error instanceof ServerError)
			{
				((ServerError) error).setServerCode(status);
				throw new ServerErrorException((ServerError)error);
			}
		}
		
		return null;
	}
	
}
