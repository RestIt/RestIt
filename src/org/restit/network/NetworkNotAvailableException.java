package org.restit.network;


/**
 * Error Exception thrown when the network is not available
 * 
 * @author Cody
 *
 */
public class NetworkNotAvailableException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -501311373528878782L;
	
	public NetworkNotAvailableException()
	{
		super();
	}
	
	public NetworkNotAvailableException(String message)
	{
		super(message);
	}
}
