package net.redyeti.codereview;

import java.awt.event.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ColorPanel;

/**
 * The configuration form for the Code Review Plugin.
 */
public class CodeReviewConfigForm {
  private JTabbedPane tabbedPane;
  private JTextField smtpServerTextField;
  private JTextField smtpPortTextField;
  private JTextField smtpUsernameTextField;
  private JPasswordField smtpPasswordField;
  private JTextField fromAddressTextField;
  private JTextField replyToAddressTextField;
  private JCheckBox sendEmailInHTMLCheckBox;
  private ColorPanel insertedColorPanel;
  private ColorPanel deletedColorPanel;
  private ColorPanel omittedColorPanel;
  private JPanel rootComponent;
  private JComboBox templateSelectionCombo;
  private JTextArea templateTextArea;
  private JTextField subjectPrefixTextField;
  private JTextField linesOfContextTextField;
  private JCheckBox ignoreTrailingWhitespaceCheckBox;
  private JCheckBox attachZipFileCheckBox;
  private JCheckBox useSslCheckBox;

  private boolean passwordChanged;

  public CodeReviewConfigForm() {
    sendEmailInHTMLCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean checked = ((AbstractButton) e.getSource()).isSelected();
        insertedColorPanel.setEnabled(checked);
        deletedColorPanel.setEnabled(checked);
        omittedColorPanel.setEnabled(checked);
      }
    });
    smtpPasswordField.setText("XXXXXXXX");
    smtpPasswordField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { passwordChanged = true; }

      public void removeUpdate(DocumentEvent e) { passwordChanged = true; }

      public void changedUpdate(DocumentEvent e) { passwordChanged = true; }
    });
    smtpPortTextField.addKeyListener(new NumericOnlyKeyAdapter(5));
    linesOfContextTextField.addKeyListener(new NumericOnlyKeyAdapter(4));
    templateSelectionCombo.addItem(new TemplateComboEntry("HTML", "htmlTemplate.vm"));
    templateSelectionCombo.addItem(new TemplateComboEntry("Plain text", "plainTextTemplate.vm"));
    tabbedPane.setEnabledAt(1, false);
  }

  public JComponent getRootComponent() {
    return rootComponent;
  }

  /**
   * Called by IDEA to apply the user's code review configuration to the form.
   */
  public void setData(CodeReviewConfig config) {
    smtpServerTextField.setText(config.getSmtpServer());
    smtpPortTextField.setText(String.valueOf(config.getSmtpPort()));
    smtpUsernameTextField.setText(config.getSmtpUsername());
    useSslCheckBox.setSelected(config.isUseSSL());
    fromAddressTextField.setText(config.getFromAddress());
    replyToAddressTextField.setText(config.getReplyToAddress());
    subjectPrefixTextField.setText(config.getSubjectPrefix());
    linesOfContextTextField.setText(String.valueOf(config.getLinesOfContext()));
    if (!config.isSendAsHtml())
      sendEmailInHTMLCheckBox.doClick();  // Disable the color pickers
    ignoreTrailingWhitespaceCheckBox.setSelected(config.isIgnoreTrailingWhitespace());
    attachZipFileCheckBox.setSelected(config.isAttachZipFile());
    insertedColorPanel.setSelectedColor(config.getInsertedLineColor());
    deletedColorPanel.setSelectedColor(config.getDeletedLineColor());
    omittedColorPanel.setSelectedColor(config.getOmittedLineColor());

  }

  /**
   * Called by IDEA to retrieve the user's settings from the form.
   */
  public void getData(CodeReviewConfig config) throws ConfigurationException {
    // validate the SMTP server
    String smtpServer = smtpServerTextField.getText();
    if (smtpServer == null || smtpServer.trim().length() == 0) {
      setFocus(smtpServerTextField);
      throw new ConfigurationException("An SMTP server must be supplied.");
    }

    // validate the port number
    boolean portOK = true;
    int smtpPort = 0;
    try {
      smtpPort = Integer.parseInt(smtpPortTextField.getText());
      if (smtpPort <= 0 || smtpPort > 65535)
        portOK = false;
    }
    catch (NumberFormatException e) {
      portOK = false;
    }
    if (!portOK) {
      setFocus(smtpPortTextField);
      throw new ConfigurationException("The port number must be an integer in the range 1-65535.");
    }

    // validate the 'from' address
    String fromAddress = fromAddressTextField.getText();
    if (fromAddress == null || fromAddress.length() == 0) {
      setFocus(fromAddressTextField);
      throw new ConfigurationException("A 'from' address must be supplied.");
    }
    try {
      new InternetAddress(fromAddress, true);
    }
    catch (AddressException e) {
      setFocus(fromAddressTextField);
      throw new ConfigurationException("The 'from' address is not a valid email address.");
    }

    // validate the 'reply-to' address
    String replyToAddress = replyToAddressTextField.getText();
    try {
      if (replyToAddress != null && replyToAddress.length() > 0)
        new InternetAddress(replyToAddress, true);
    }
    catch (AddressException e) {
      setFocus(replyToAddressTextField);
      throw new ConfigurationException("The 'Reply-to' address is not a valid email address.");
    }

    // validate the lines of context
    boolean linesOK = true;
    int linesOfContext = 0;
    try {
      linesOfContext = Integer.parseInt(linesOfContextTextField.getText());
      if (linesOfContext < 0 || linesOfContext > 9999)
        linesOK = false;
    }
    catch (NumberFormatException e) {
      linesOK = false;
    }
    if (!linesOK) {
      setFocus(linesOfContextTextField);
      throw new ConfigurationException("The lines of context must be an integer in the range 0-9999.");
    }

    // update the configuration object
    config.setSmtpServer(smtpServer);
    config.setSmtpPort(smtpPort);
    config.setSmtpUsername(smtpUsernameTextField.getText());
    if (passwordChanged) {
      String encodedPassword = PasswordMangler.encode(new String(smtpPasswordField.getPassword()));
      config.setEncodedSmtpPassword(encodedPassword);
    }
    passwordChanged = false;
    config.setUseSSL(useSslCheckBox.isSelected());
    config.setFromAddress(fromAddressTextField.getText());
    config.setReplyToAddress(replyToAddressTextField.getText());
    config.setSubjectPrefix(subjectPrefixTextField.getText());
    config.setLinesOfContext(linesOfContext);
    config.setIgnoreTrailingWhitespace(ignoreTrailingWhitespaceCheckBox.isSelected());
    config.setAttachZipFile(attachZipFileCheckBox.isSelected());
    config.setSendAsHtml(sendEmailInHTMLCheckBox.isSelected());
    config.setInsertedLineColor(insertedColorPanel.getSelectedColor());
    config.setDeletedLineColor(deletedColorPanel.getSelectedColor());
    config.setOmittedLineColor(omittedColorPanel.getSelectedColor());
  }

  /**
   * Sets the focus on to the given component.
   */
  private void setFocus(final JComponent component) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        component.requestFocus();
      }
    });
  }

  /**
   * Indicates whether or not any of the configuration has been changed by the user. This
   * determines whether or not the 'Apply' button should be enabled.
   */
  public boolean isModified(CodeReviewConfig config) {
    if (smtpServerTextField.getText() != null ? !smtpServerTextField.getText().equals(config.getSmtpServer()) : config.getSmtpServer() != null)
      return true;
    if (smtpPortTextField.getText() == null || !smtpPortTextField.getText().equals(String.valueOf(config.getSmtpPort())))
      return true;
    if (useSslCheckBox.isSelected() != config.isUseSSL())
      return true;
    if (smtpUsernameTextField.getText() != null ? !smtpUsernameTextField.getText().equals(config.getSmtpUsername()) : config.getSmtpUsername() != null)
      return true;
    if (passwordChanged)
      return true;
    if (fromAddressTextField.getText() != null ? !fromAddressTextField.getText().equals(config.getFromAddress()) : config.getFromAddress() != null)
      return true;
    if (replyToAddressTextField.getText() != null ? !replyToAddressTextField.getText().equals(config.getReplyToAddress()) : config.getReplyToAddress() != null)
      return true;
    if (subjectPrefixTextField.getText() != null ? !subjectPrefixTextField.getText().equals(config.getSubjectPrefix()) : config.getSubjectPrefix() != null)
      return true;
    if (linesOfContextTextField.getText() == null || !linesOfContextTextField.getText().equals(String.valueOf(config.getLinesOfContext())))
      return true;
    if (insertedColorPanel.getSelectedColor() != null ? !insertedColorPanel.getSelectedColor().equals(config.getInsertedLineColor()) : config.getInsertedLineColor() != null)
      return true;
    if (deletedColorPanel.getSelectedColor() != null ? !deletedColorPanel.getSelectedColor().equals(config.getDeletedLineColor()) : config.getDeletedLineColor() != null)
      return true;
    if (omittedColorPanel.getSelectedColor() != null ? !omittedColorPanel.getSelectedColor().equals(config.getOmittedLineColor()) : config.getOmittedLineColor() != null)
      return true;
    if (ignoreTrailingWhitespaceCheckBox.isSelected() != config.isIgnoreTrailingWhitespace())
      return true;
    if (attachZipFileCheckBox.isSelected() != config.isAttachZipFile())
      return true;
    return sendEmailInHTMLCheckBox.isSelected() != config.isSendAsHtml();
  }

  private class TemplateComboEntry {
    private String name;
    private String templateFile;

    public TemplateComboEntry(String name, String templateFile) {
      this.name = name;
      this.templateFile = templateFile;
    }

    public String getName() {
      return name;
    }

    public String getTemplateFile() {
      return templateFile;
    }

    public String toString() {
      return name;
    }
  }

  private class NumericOnlyKeyAdapter extends KeyAdapter {
    private int maxDigits;

    public NumericOnlyKeyAdapter(int maxDigits) {
      this.maxDigits = maxDigits;
    }

    public void keyTyped(KeyEvent e) {
      char c = e.getKeyChar();
      JTextField field = (JTextField) e.getComponent();
      if (!(Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) || (Character.isDigit(c) && field.getText().length() >= maxDigits)) {
        field.getToolkit().beep();
        e.consume();
      }
    }
  }
}
