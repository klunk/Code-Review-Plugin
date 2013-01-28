/**
 * $Source:$
 * $Id:$
 *
 * Copyright 2006 KBC Financial Products - Risk Technology
 */
package net.redyeti.codereview;

/**
 * The settings that dictate how the code review email should be constructed.
 *
 * @author <a href="mailto:Chris.Miller@kbcfp.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class ReviewSettings {

  private String smtpServer;
  private int smtpPort;
  private String smtpUsername;
  private String smtpPassword;
  private boolean useSSL;
  private String fromAddress;
  private String replyToAddress;
  private String toAddresses;
  private String ccAddresses;
  private String bccAddresses;
  private String subject;
  private boolean plainAttached;
  private boolean htmlAttached;
  private boolean zipAttached;

  public String getSmtpServer() {
    return smtpServer;
  }

  public void setSmtpServer(String smtpServer) {
    this.smtpServer = smtpServer;
  }

  public int getSmtpPort() {
    return smtpPort;
  }

  public void setSmtpPort(int smtpPort) {
    this.smtpPort = smtpPort;
  }

  public String getSmtpUsername() {
    return smtpUsername;
  }

  public void setSmtpUsername(String smtpUsername) {
    this.smtpUsername = smtpUsername;
  }

  public String getSmtpPassword() {
    return smtpPassword;
  }

  public void setSmtpPassword(String smtpPassword) {
    this.smtpPassword = smtpPassword;
  }

  public boolean isUseSSL() {
    return useSSL;
  }

  public void setUseSSL(boolean useSSL) {
    this.useSSL = useSSL;
  }

  public String getFromAddress() {
    return fromAddress;
  }

  public void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }

  public String getReplyToAddress() {
    return replyToAddress;
  }

  public void setReplyToAddress(String replyToAddress) {
    this.replyToAddress = replyToAddress;
  }

  public String getToAddresses() {
    return toAddresses;
  }

  public void setToAddresses(String toAddresses) {
    this.toAddresses = toAddresses;
  }

  public String getCcAddresses() {
    return ccAddresses;
  }

  public void setCcAddresses(String ccAddresses) {
    this.ccAddresses = ccAddresses;
  }

  public String getBccAddresses() {
    return bccAddresses;
  }

  public void setBccAddresses(String bccAddresses) {
    this.bccAddresses = bccAddresses;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public boolean hasPlainAttached() {
    return plainAttached;
  }

  public void setPlainAttached(boolean plainAttached) {
    this.plainAttached = plainAttached;
  }

  public boolean hasHtmlAttached() {
    return htmlAttached;
  }

  public void setHtmlAttached(boolean htmlAttached) {
    this.htmlAttached = htmlAttached;
  }

  public boolean hasZipAttached() {
    return zipAttached;
  }

  public void setZipAttached(boolean zipAttached) {
    this.zipAttached = zipAttached;
  }
}
