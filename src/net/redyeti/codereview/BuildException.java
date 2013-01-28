/**
 * $Source:$
 * $Id:$
 *
 * Copyright 2006 KBC Financial Products - Risk Technology
 */
package net.redyeti.codereview;

/**
 * Thrown when a problem is encountered while building an email preview
 *
 * @author <a href="mailto:Chris.Miller@kbcfp.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class BuildException extends Exception {

  public BuildException(String message) {
    super(message);
  }

  public BuildException(String message, Throwable cause) {
    super(message, cause);
  }
}
