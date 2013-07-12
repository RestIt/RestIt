package org.restit.model;

public class NetworkNotAvailableError extends ServerError
{
	private String message;
	private int serverCode;
	
	public NetworkNotAvailableError()
	{
		
	}
	
	public NetworkNotAvailableError(String message)
	{
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getServerCode() {
		return serverCode;
	}
	public void setServerCode(int serverCode) {
		this.serverCode = serverCode;
	}
}
