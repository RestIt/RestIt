package org.restit.network;

import org.restit.model.ServerError;

import android.os.AsyncTask;

public abstract class ServerAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	private ServerError serverError;
	
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
		}
	}
	
	/**
	 * After the background thread has finished executing, call the server error
	 */
	protected void onPostExecute(Result result) {
		
		if(this.serverError != null)
		{
			onServerError(this.serverError);
		}
		
	};
	
	/**
	 * Execute code that will call the server
	 * @param params
	 * @return
	 */
	protected abstract Result doOnServer(Params... params) throws ServerErrorException;
	
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
