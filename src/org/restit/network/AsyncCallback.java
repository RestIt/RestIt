package org.restit.network;

/**
 * The primary interface a caller must implement to receive a response from a
 * remote procedure call.
 * 
 * <p>
 * If call is successful, then {@link #onSuccess(Object)} is called, otherwise
 * {@link #onFailure(Throwable)} is called.
 * </p>
 * 
 */
public interface AsyncCallback<T> {

  /**
   * Called when a call fails, through exception or other means, onFailure is called 
   * 
   * @param caught The failure
   */
  void onFailure(Throwable caught);

  /**
   * Called when call is successful
   * 
   * @param result The return value of the remote call
   */
  void onSuccess(T result);
}