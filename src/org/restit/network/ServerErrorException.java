package org.restit.network;

import org.restit.model.ServerError;

public class ServerErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5525943103533374970L;
	
	private ServerError error;
	
	public ServerErrorException()
	{
		
	}
	
	public ServerErrorException(String message)
	{
		this.error = new ServerError(message);
	}
	
	public ServerErrorException(ServerError error)
	{
		this.error = error;
	}
	
	public ServerErrorException(Exception exception)
	{
		this.error = new ServerError(exception);
	}

	public ServerError getError() {
		return error;
	}

	public void setError(ServerError error) {
		this.error = error;
	}
	
	
}
