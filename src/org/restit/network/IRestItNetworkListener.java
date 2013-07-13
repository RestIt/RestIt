package org.restit.network;

/**
 * Interface that listens for network connectivity status change updates
 * 
 * @author Cody
 *
 */
public interface IRestItNetworkListener
{
	/**
	 * Called when the network connection status changes
	 * 
	 * @param status
	 */
	public void onNetworkStatusChanged(RestItNetworkStatus status);
}
