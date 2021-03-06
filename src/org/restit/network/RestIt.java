package org.restit.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.restit.model.ServerError;
import org.restit.network.insecure.NullHostNameVerifier;
import org.restit.network.insecure.NullX509TrustManager;
import org.restit.objectmapping.RestItMapper;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class RestIt {

	//Logging tag
	private static final String LOG_TAG = "RestIt";
	private static final String SLASH_CHAR = "/";

	
	protected static RestItClient client;
	private static ConnectivityManager restItConnectivityManager;
	private static IRestItNetworkListener restItNetworkListener;
 
    
    /**
     * Get the complete URL for a request
     * @param path The path to REST service
     * @return Complete URL with base URL and given path
     */
    protected static String getUrlWithPath(String path)
    {
	
    	return getUrlWithPath(getClient().getBaseUrl(),path);
    }
    
    /**
     * Get the complete URL for a request
     * @param baseUrl The base url of the request
     * @param path The path to REST service
     * @return Complete URL with base URL and given path
     */
    protected static String getUrlWithPath(String baseUrl, String path)
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
    	
    	return baseUrl + path;
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
	 * Get the base url that has been set for the RestIt framework
	 * @return
	 */
	public static String getBaseUrl()
	{
		return getClient().getBaseUrl();
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
	 * Set a header value that will be included on every request. Useful for headers like authentication tokens or cookies
	 * @param header The header name
	 * @param value Value of the header
	 */
	public static void setDefaultHeader(String header, String value)
	{
		if(header == null)
		{
			return;
			
		} else if(value == null)
		{
			getClient().getDefaultHeaders().remove(header);
		} else
		{
			getClient().setDefaultHeader(header, value);
		}
	}
	
	/**
	 * Make a GET request to the given path
	 * @param path The path to the REST service, not the full URL
 	 * @param callback The code that will be executed upon completion by the server
	 * @throws ServerErrorException 
	 */
	public static Object get(String path) throws ServerErrorException, NetworkNotAvailableException
	{
		updateNetworkConnectivity();

		// make sure that base URL has been set
		if (getClient().getBaseUrl() == null) {
			Log.e(LOG_TAG,
					"Could not make GET request because a base URL has not been set. Please use RestIt.setBaseUrl().");
			return null;
		}

		String fullUrlValue = getUrlWithPath(path);

		HttpURLConnection connection = null;

		try {

			Log.d(LOG_TAG, "Starting GET request to: " + fullUrlValue);

			URL fullUrl = new URL(fullUrlValue);

			// make server call
			connection = getClient().getConnection(fullUrl);

			String result = processConnection(connection);

			// convert to POJO
			Object response = RestItMapper.parseResponse(result);
			return response;

		}
		catch (ClientProtocolException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);

			throw new ServerErrorException(e);
			
		}catch (SocketTimeoutException e) {
			
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
			sendNetworkStatusUpdate(RestItNetworkStatus.DISCONNECTED);
			
			
		} catch(SocketException e)
		{
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
			sendNetworkStatusUpdate(RestItNetworkStatus.DISCONNECTED);
			
		}
		catch (IOException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);

			// UnknownHostException thrown when there is no service - handled by network listener

			// check for the following exceptions related to server connection errors:
			// SocketException, ConnectException, SocketTimeoutException
			
			throw new ServerErrorException(e);

		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		
		return null;
	}
	
	/**
	 * Make a DELETE request to the given path
	 * @param path The path to the REST service, not the full URL
 	 * @param callback The code that will be executed upon completion by the server
	 * @throws ServerErrorException 
	 */
	public static Object delete(String path) throws ServerErrorException
	{
		//make sure that base URL has been set
		if(getClient().getBaseUrl() == null)
		{
			Log.e(LOG_TAG, "Could not make DELETE request because a base URL has not been set. Please use RestIt.setBaseUrl().");
			return null;
		}

		String fullUrlValue = getUrlWithPath(path);
		
		HttpURLConnection connection = null;
		
		try {
			
			Log.d(LOG_TAG, "Starting DELETE request to: " +fullUrlValue);
			
			URL fullUrl = new URL(fullUrlValue);
			
			//make server call
			connection = getClient().getConnection(fullUrl, RequestMethod.DELETE);
			

			//get response from server
			String result = processConnection(connection);
			
			//convert to POJO
			Object response = RestItMapper.parseResponse(result);
			return response;
			
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
			throw new ServerErrorException(e);
			
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			// UnknownHostException thrown when there is no service - handled by network listener

			// check for the following exceptions related to server connection errors:
			// SocketException, ConnectException, SocketTimeoutException

			if (e instanceof SocketTimeoutException || e instanceof SocketException || e instanceof ConnectException) {
				//sendNetworkStatusDisconnected();
			}
			
			throw new ServerErrorException(e);
			
		} finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}

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
			Object response = RestItMapper.parseResponse(result);
			return response;
			
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
			throw new ServerErrorException(e);
			
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			// UnknownHostException thrown when there is no service - handled by network listener

			// check for the following exceptions related to server connection errors:
			// SocketException, ConnectException, SocketTimeoutException

			if (e instanceof SocketTimeoutException || e instanceof SocketException || e instanceof ConnectException) {
				//sendNetworkStatusDisconnected();
			}
			
			throw new ServerErrorException(e);
			
		} finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}
	}
	
	/**
	 * Make POST request to given path using a multipart form
	 * @param path Url path not including server name
	 * @param postObjectBytes The byte array of the object to post
	 * @param formElementName The name of element in the HTML form
	 * @param fileName The file name of the file
	 * @param additionalParameters A map of additional form field values to add on the request
	 * @param progressListener A listener that will respond to upload progress
	 * @return
	 * @throws ServerErrorException
	 */
	public static Object multipartPostObject(String path, byte[] postObjectBytes, String formElementName, String fileName, Map<String, String> additionalParameters, ProgressListener progressListener) throws ServerErrorException
	{
		return multipartPostObject(path, postObjectBytes, formElementName, fileName, additionalParameters, progressListener, null);
	}
	
	/**
	 * Make POST request to given path using a multipart form
	 * @param path Url path not including server name
	 * @param postObjectBytes The byte array of the object to post
	 * @param formElementName The name of element in the HTML form
	 * @param fileName The file name of the file
	 * @param additionalParameters A map of additional form field values to add on the request
	 * @param progressListener A listener that will respond to upload progress
	 * @param requestOptions Additional options for the request
	 * @return
	 * @throws ServerErrorException
	 */
	public static Object multipartPostObject(String path, byte[] postObjectBytes, String formElementName, String fileName, Map<String, String> additionalParameters, ProgressListener progressListener, RequestOptions requestOptions) throws ServerErrorException
	{
		//make sure that base URL has been set
		if(getClient().getBaseUrl() == null)
		{
			Log.e(LOG_TAG, "Could not make multipart POST request because a base URL has not been set. Please use RestIt.setBaseUrl().");
			return null;
		}

		String fullUrlValue = getUrlWithPath(path);
		if(requestOptions != null && requestOptions.getOverrideBaseUrl() != null)
		{
			fullUrlValue = getUrlWithPath(requestOptions.getOverrideBaseUrl(), path);
		}
		
		HttpURLConnection connection = null;
		
		try {
			
			Log.d(LOG_TAG, "Starting multipart POST request to: " +fullUrlValue);
			
			URL fullUrl = new URL(fullUrlValue);
			
			//make server call
			connection = getClient().getConnection(fullUrl, RequestMethod.POST);
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(60000); //60 seconds to complete an upload 
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			//see if we are streaming the upload
			if(requestOptions == null || requestOptions.isChunckedStreamingMode())
			{
				connection.setChunkedStreamingMode(2048);
			}
			
		    String boundry = "z6fQbdm2TTgLwPQj9u1HjAM25z9AJuGSx7WG9dnD";
		    		
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundry);
			
			//attach post objects
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
			
			//add form fields
			if(additionalParameters != null && additionalParameters.keySet().size() > 0)
			{
				Iterator<String> it = additionalParameters.keySet().iterator();
				while(it.hasNext())
				{
					String fieldName = it.next();
					if(fieldName != null)
					{
						String value = additionalParameters.get(fieldName);
						if(value != null)
						{
							//add form field to upload form
							outputStream.writeBytes("--" + boundry + "\r\n" + "Content-Disposition: form-data; name=\"" + fieldName + "\"\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n"+value+"\r\n");
						}
					}
				}
			}
			
			//attach file
			if(postObjectBytes != null)
			{	
			    //build the request body
				outputStream.writeBytes("--" + boundry + "\r\n" + "Content-Disposition: form-data; name=\"" + formElementName + "\"; filename=\"" + fileName + "\"\r\n\r\n");
    

			    //object upload content size
			    long totalUploadSize = postObjectBytes.length;
			    
				//we have to manually process the stream in order to correctly update the progress listener
				byte[] buffer = new byte[2048];
		        long totalBytes = 0;
		        int bytesRead = 0;
		        
		        InputStream inputStream = new ByteArrayInputStream(postObjectBytes);
		        
		        while ((bytesRead = inputStream.read(buffer)) > -1) {
		            totalBytes += bytesRead;
		            outputStream.write(buffer, 0, bytesRead);
		            
		            if(progressListener != null){
		            	progressListener.transferred(totalBytes, totalUploadSize);
		            }
		        }
		        
		        outputStream.writeBytes("\r\n--" + boundry + "--\r\n");
				
			}
			
			outputStream.flush();
			outputStream.close();

			//get response from server
			String result = processConnection(connection);
			
			//convert to POJO
			Object response = RestItMapper.parseResponse(result);
			return response;
			
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			
			throw new ServerErrorException(e);
			
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			// UnknownHostException thrown when there is no service - handled by network listener

			// check for the following exceptions related to server connection errors:
			// SocketException, ConnectException, SocketTimeoutException

			if (e instanceof SocketTimeoutException || e instanceof SocketException || e instanceof ConnectException) {
				//sendNetworkStatusDisconnected();
			}
			
			throw new ServerErrorException(e);
			
		} finally
		{
			if(connection != null)
			{
				connection.disconnect();
			}
		}
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
			Object error = RestItMapper.parseResponse(result);
			if(error instanceof ServerError)
			{
				((ServerError) error).setServerCode(status);
				throw new ServerErrorException((ServerError)error);
			}
		}
		
		return null;
	}
	
	/**
	 * Catches IOExceptions related to network connection. Use checkNetworkConnectivity() and isNetworkConnected()
	 * instead.
	 * 
	 * @param connection
	 * @return
	 */
	protected static boolean isServerAvailable(HttpURLConnection connection)
	{
		try {
			connection.getResponseCode();
		}
		catch (IOException e) {
			
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Store the network connection listener and connectivity manager for network connection status updates.
	 * 
	 * @param connectivityManager
	 * @param networkListener
	 */
	public static void setNetworkConnectionListener(ConnectivityManager connectivityManager, IRestItNetworkListener networkListener)
	{
		restItConnectivityManager = connectivityManager;
		restItNetworkListener = networkListener;
	}
	
	/**
	 * Check if the android device has a network connection. This does not check for connection issues due to the
	 * server. Those exceptions are handled in the get, post, and multipartpost methods.
	 * 
	 * @return
	 */
	public static boolean isNetworkConnected()
	{
		if (restItConnectivityManager == null)
			return false;
		
		NetworkInfo activeConnection = restItConnectivityManager.getActiveNetworkInfo();
		if ((activeConnection != null)  && activeConnection.isConnected())
		{
		  return true;
		}

		return false;
	}
	
	/**
	 * Check for network connection
	 * @throws NetworkNotAvailableException 
	 * 
	 */
	protected static void updateNetworkConnectivity() throws NetworkNotAvailableException, ServerErrorException
	{
		if (!isNetworkConnected()) {
			sendNetworkStatusUpdate(RestItNetworkStatus.DISCONNECTED);
		}
		else {
			sendNetworkStatusUpdate(RestItNetworkStatus.CONNECTED);
		}
	}
	
	/**
	 * Send network status updates for the network connection listener to receive.
	 * 
	 * @param status
	 * @throws NetworkNotAvailableException 
	 */
	protected static void sendNetworkStatusUpdate(RestItNetworkStatus status) throws NetworkNotAvailableException, ServerErrorException 
	{
		if (status.equals(RestItNetworkStatus.CONNECTED)) {

			//nothing to do, continue with call
		}
		else {
			
			//not connected
			Log.w(LOG_TAG, "Could not connect to server");
			
			if (restItNetworkListener != null)
			{
				//throw the exception so that we can cancel the call
				throw new NetworkNotAvailableException();
			} else
			{
				//no listener, thrown an exception with a message so that the ServerAsyncTask can display the message
				throw new ServerErrorException("Could not connect to server");
			}
		}
	}
	
	/**
	 * Tell the listener, if it exists, that the network is  connected. This can only be called from the main thread
	 * @throws NetworkNotAvailableException
	 */
	public static void doTellListenerConnected()
	{
		if (restItNetworkListener != null) {
			
			//if a listener exists, notify the app
			restItNetworkListener.onNetworkStatusChanged(RestItNetworkStatus.CONNECTED);
		}
	}
	
	/**
	 * Tell the listener if it exists that the network is no longer connected. This can only be called from the main thread
	 * @throws NetworkNotAvailableException
	 */
	public static void doTellListenerDisconnected()
	{
		if (restItNetworkListener != null) {
			
			//if a listener exists, notify the app
			restItNetworkListener.onNetworkStatusChanged(RestItNetworkStatus.DISCONNECTED);
		}
	}
}
