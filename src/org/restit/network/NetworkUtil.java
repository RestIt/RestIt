package org.restit.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

public class NetworkUtil {

	private final static String LOG_TAG = "NetworkUtil";
	
	/**
	 * Parse a stream to a string
	 * @param stream The stream to parse
	 * @return A readable string
	 */
	public static String parseStream(InputStream stream)
	{
		String value = null;
		
        if (stream != null) {
        
	        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			StringBuilder sb = new StringBuilder();
	
			String line = null;
			try
			{
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
			} catch (IOException e)
			{
				Log.e(LOG_TAG, e.getLocalizedMessage(), e);
			} finally
			{
				try
				{
					stream.close();
				} catch (IOException e)
				{
					Log.e(LOG_TAG, e.getLocalizedMessage(), e);
				}
			}
	
			value = sb.toString();
		
        }
        
        return value;
	}
}
