package org.restit.network;

/**
 * A call back to notify code about data that has been transfered
 *
 */
public interface ProgressListener {

	/**
	 * Tell the listener how much data has been transferred
	 * @param num The amount of transferred data
	 */
	void transferred(long num, final long totalSize);
}
