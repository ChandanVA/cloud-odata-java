package com.sap.core.odata.api.exception;

/**
 * Common checked exception for <code>OData</code> library and base exception for all <code>OData</code> related exceptions.
 */
public class ODataException extends Exception {

  private static final long serialVersionUID = 1L;

  public ODataException() {
    super();
  }

  public ODataException(String msg) {
    super(msg);
  }

  public ODataException(String msg, Throwable e) {
    super(msg, e);
  }

  public ODataException(Throwable e) {
    super(e);
  }

  /**
   * Check whether this exception was caused by a {@link ODataMessageException} exception.
   * 
   * @return <code>true</code> if it was caused by an {@link ODataMessageException}, otherwise <code>false</code>.
   */
  public boolean isCausedByMessageException() {
    return getMessageExceptionCause() != null;
  }

  /**
   * Search for and return first (from top) {@link ODataMessageException} in cause hierarchy.
   * If no {@link ODataMessageException} in cause hierarchy <code>NULL</code> is returned. 
   * 
   * @return the first found {@link ODataMessageException} in the cause exception hierarchy. 
   *          Or <code>NULL</code> if no {@link ODataMessageException} is found in cause hierarchy.
   */
  public ODataMessageException getMessageExceptionCause() {
    Throwable cause = getCause();
    while (cause != null) {
      if (cause instanceof ODataMessageException) {
        return (ODataMessageException) cause;
      }
      cause = cause.getCause();
    }
    return (ODataMessageException) cause;
  }
}
