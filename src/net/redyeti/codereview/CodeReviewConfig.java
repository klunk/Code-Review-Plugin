package net.redyeti.codereview;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.jetbrains.annotations.*;

import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.*;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(
    name = CodeReviewConfig.COMPONENT_NAME,
    storages = {@Storage(id = "other", file = "$APP_CONFIG$/code.review.xml")}
)
public class CodeReviewConfig implements ApplicationComponent, Configurable, PersistentStateComponent<CodeReviewConfig> {

  static final String COMPONENT_NAME = "CodeReview.CodeReviewPlugin";

  private CodeReviewConfigForm form;
  private String smtpServer;
  private int smtpPort = Mailer.DEFAULT_PORT;
  private String smtpUsername;
  private String encodedSmtpPassword;
  private boolean useSSL;
  private String fromAddress;
  private String replyToAddress;
  private String toAddresses;
  private String ccAddresses;
  private String bccAddresses;
  private String subject;
  private String subjectPrefix;
  private int linesOfContext;
  private boolean ignoreTrailingWhitespace;
  private boolean sendAsHtml;
  private boolean attachZipFile;
  private Color insertedLineColor = Color.BLUE;
  private Color deletedLineColor = Color.RED;
  private Color omittedLineColor = Color.GRAY;

  @NonNls
  @NotNull
  public String getComponentName() {
    return COMPONENT_NAME;
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  @Nls
  public String getDisplayName() {
    return "Code Review";
  }

  public Icon getIcon() {
    return IconLoader.getIcon("/net/redyeti/codereview/resources/ConfigIcon.png");
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    return null;
  }

  public JComponent createComponent() {
    if (form == null) {
      form = new CodeReviewConfigForm();
    }
    return form.getRootComponent();
  }

  public CodeReviewConfig getState() {
    return this;
  }

  public void loadState(CodeReviewConfig state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public boolean isModified() {
    return form != null && form.isModified(this);
  }

  public void apply() throws ConfigurationException {
    if (form != null)
      form.getData(this);
  }

  public void reset() {
    if (form != null) {
      form.setData(this);
    }
  }

  public void disposeUIResources() {
    form = null;
  }

  public String getSmtpServer() {
    return smtpServer;
  }

  public void setSmtpServer(final String smtpServer) {
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

  public String getEncodedSmtpPassword() {
    return encodedSmtpPassword;
  }

  public void setEncodedSmtpPassword(String encodedPassword) {
    this.encodedSmtpPassword = encodedPassword;
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

  public void setFromAddress(final String fromAddress) {
    this.fromAddress = fromAddress;
  }

  public String getReplyToAddress() {
    return replyToAddress;
  }

  public void setReplyToAddress(final String replyToAddress) {
    this.replyToAddress = replyToAddress;
  }

  public String getToAddresses() {
    return toAddresses;
  }

  public void setToAddresses(final String toAddresses) {
    this.toAddresses = toAddresses;
  }

  public String getCcAddresses() {
    return ccAddresses;
  }

  public void setCcAddresses(final String ccAddresses) {
    this.ccAddresses = ccAddresses;
  }

  public String getBccAddresses() {
    return bccAddresses;
  }

  public void setBccAddresses(final String bccAddresses) {
    this.bccAddresses = bccAddresses;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getSubjectPrefix() {
    return subjectPrefix;
  }

  public void setSubjectPrefix(String subjectPrefix) {
    this.subjectPrefix = subjectPrefix;
  }

  public int getLinesOfContext() {
    return linesOfContext;
  }

  public void setLinesOfContext(int linesOfContext) {
    this.linesOfContext = linesOfContext;
  }

  public boolean isIgnoreTrailingWhitespace() {
    return ignoreTrailingWhitespace;
  }

  public void setIgnoreTrailingWhitespace(boolean ignoreTrailingWhitespace) {
    this.ignoreTrailingWhitespace = ignoreTrailingWhitespace;
  }

  public boolean isSendAsHtml() {
    return sendAsHtml;
  }

  public void setSendAsHtml(final boolean sendAsHtml) {
    this.sendAsHtml = sendAsHtml;
  }

  public boolean isAttachZipFile() {
    return attachZipFile;
  }

  public void setAttachZipFile(boolean attachZipFile) {
    this.attachZipFile = attachZipFile;
  }

  public Color getInsertedLineColor() {
    return insertedLineColor;
  }

  public void setInsertedLineColor(Color insertedLineColor) {
    this.insertedLineColor = insertedLineColor;
  }

  public String getInsertedLineColorAsHtml() {
    return getHtmlColorString(insertedLineColor);
  }

  public Color getDeletedLineColor() {
    return deletedLineColor;
  }

  public void setDeletedLineColor(Color deletedLineColor) {
    this.deletedLineColor = deletedLineColor;
  }

  public String getDeletedLineColorAsHtml() {
    return getHtmlColorString(deletedLineColor);
  }

  public Color getOmittedLineColor() {
    return omittedLineColor;
  }

  public void setOmittedLineColor(Color omittedLineColor) {
    this.omittedLineColor = omittedLineColor;
  }

  public String getOmittedLineColorAsHtml() {
    return getHtmlColorString(omittedLineColor);
  }

  private static String getHtmlColorString(Color color) {
    return Integer.toHexString(color.getRGB() & 0xffffff | 0x1000000).substring(1);
  }
}
