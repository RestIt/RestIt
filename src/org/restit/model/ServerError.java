package org.restit.model;

public class ServerError {

	private String message;
	private int serverCode;
	private Exception exception;
	
	public ServerError()
	{
		
	}
	
	public ServerError(String message)
	{
		this.message = message;
	}
	
	public ServerError(Exception exception)
	{
		if(exception != null)
		{
			this.exception = exception;
			this.message = exception.getMessage();
		}
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

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
