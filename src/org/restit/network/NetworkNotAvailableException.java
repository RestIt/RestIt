package org.restit.network;

import org.restit.model.NetworkNotAvailableError;

/**
 * Error Exception thrown when the network is not available
 * 
 * @author Cody
 *
 */
public class NetworkNotAvailableException extends ServerErrorException
{
	private NetworkNotAvailableError error;

	/**
	 * 
	 */
	private static final long serialVersionUID = -501311373528878782L;
	
	public NetworkNotAvailableException()
	{
		
	}
	
	public NetworkNotAvailableException(String message)
	{
		this.error = new NetworkNotAvailableError(message);
	}
	
	public NetworkNotAvailableException(NetworkNotAvailableError error)
	{
		this.error = error;
	}
	
	public NetworkNotAvailableError getError() {
		return error;
	}

	public void setError(NetworkNotAvailableError error) {
		this.error = error;
	}
}
