/**
 * $Source:$
 * $Id:$
 *
 * Copyright 2006 KBC Financial Products - Risk Technology
 */
package net.redyeti.codereview;

import javax.activation.DataSource;

/**
 * An email attachment
 *
 * @author <a href="mailto:Chris.Miller@kbcfp.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class Attachment {
  private String name;
  private DataSource ds;

  public Attachment(String name, DataSource ds) {
    this.name = name;
    this.ds = ds;
  }

  public String getName() {
    return name;
  }

  public DataSource getDataSource() {
    return ds;
  }
}
