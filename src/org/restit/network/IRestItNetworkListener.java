package org.restit.network;

/**
 * Interface that allows network connectivity change updates
 * 
 * @author Cody
 *
 */
public interface IRestItNetworkListener
{
	public void onNetworkStatusChanged(RestItNetworkStatus status);
}
