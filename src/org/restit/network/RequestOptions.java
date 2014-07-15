package org.restit.network;

public class RequestOptions {

	private boolean chunckedStreamingMode = true;
	private String overrideBaseUrl;
	
	public String getOverrideBaseUrl() {
		return overrideBaseUrl;
	}

	/**
	 * Set a base URL that will override the stock rest it URL
	 * @param overrideBaseUrl
	 */
	public void setOverrideBaseUrl(String overrideBaseUrl) {
		this.overrideBaseUrl = overrideBaseUrl;
	}

	public boolean isChunckedStreamingMode() {
		return chunckedStreamingMode;
	}

	/**
	 * Should file uploads support chunked streaming mode
	 * @param chunckedStreamingMode
	 */
	public void setChunckedStreamingMode(boolean chunckedStreamingMode) {
		this.chunckedStreamingMode = chunckedStreamingMode;
	}
	
	
}
