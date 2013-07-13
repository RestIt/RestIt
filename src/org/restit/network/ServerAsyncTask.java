package org.restit.network;

import org.restit.model.ServerError;

import android.os.AsyncTask;

public abstract class ServerAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	private ServerError serverError;
	private boolean connectionError;
	
	/**
	 * Perform server task on background thread. This is a final method so it cannot be overridden. Implement
	 * doOnServer instead. 
	 */
	@Override
	protected final Result doInBackground(Params... params) {
		
		try {
			
			//execute code on server
			return doOnServer(params);
			
		} catch (ServerErrorException error) {
			
			//let the task know that an error occured
			serverError = error.getError();
			return null;
			
		} catch (NetworkNotAvailableException e)
		{
			connectionError = true;
			return null;
		}
	}
	
	/**
	 * Do not implement this method unless you know what you are doing. All of your logic should
	 *  go in onServerSuccess and onServerError
	 */
	protected final void onPostExecute(Result result) {
		
		if(this.serverError != null)
		{
			//the server sent an error, display to user
			onServerError(this.serverError);
			
		} else if(connectionError == true)
		{
			//notify the listener that we have a connection issue
			RestIt.doTellListenerDisconnected();
		} else
		{
			RestIt.doTellListenerConnected();
			
			//everything was ok, continue with post request logic
			onServerSuccess(result);
		}
		
	};
	
	/**
	 * Execute code that will call the server
	 * @param params
	 * @return
	 */
	protected abstract Result doOnServer(Params... params) throws ServerErrorException, NetworkNotAvailableException;
	
	/**
	 * Implement this method instead of onPostExecute
	 * @param result
	 */
	protected abstract void onServerSuccess(Result result);
	
	/**
	 * Called when a proper error message is thrown from the server
	 */
	protected abstract void onServerError(ServerError error);
	
	/**
	 * Get the server error if one exists
	 * @return
	 */
	protected ServerError getServerError()
	{
		return this.serverError;
	}

}
