package net.redyeti.codereview;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;
import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;

public class PreviewEmailForm {
  private JPanel contentPane;
  private ComboBoxWithStoredHistory toTextField;
  private ComboBoxWithStoredHistory ccTextField;
  private ComboBoxWithStoredHistory bccTextField;
  private ComboBoxWithStoredHistory subjectTextField;
  private HtmlPanel htmlPreview;
  private JTabbedPane previewTabs;
  private JTextArea textPreview;
  private JCheckBox attachZipCheckBox;
  private JButton copyHtmlButton;

  private String plainContent;
  private String htmlContent;

  public PreviewEmailForm(String subject, final String plainContent, final String htmlContent) {
    this.plainContent = plainContent;
    this.htmlContent = htmlContent;

    initHistory(toTextField);
    initHistory(ccTextField);
    initHistory(bccTextField);
    initHistory(subjectTextField);
    int i = subject.indexOf('\n');
    if (i >= 0 && i <= 100)
      subject = subject.substring(0, i);
    if (subject.length() > 100)
      subject = subject.substring(0, 97) + "...";
    subjectTextField.setText(subject);

    if (plainContent != null) {
      textPreview.setText(plainContent);
      textPreview.setCaretPosition(0);
    } else {
      previewTabs.setEnabledAt(1, false);
    }

    CodeReviewConfig config = ApplicationManager.getApplication().getComponent(CodeReviewConfig.class);
    attachZipCheckBox.setSelected(config.isAttachZipFile());

    if (config.isSendAsHtml() && htmlContent != null) {
      try {
        initHtmlPanel(htmlContent);
      }
      catch (Exception e) {
        Messages.showMessageDialog("The attempt to render the email preview failed.\n\nReason: " + e.getMessage(),
                                   "Code Review Error",
                                   Messages.getErrorIcon());
      }
    } else {
      previewTabs.setEnabledAt(0, false);
      previewTabs.setSelectedIndex(1);
    }

    Icon copyIcon = new ImageIcon(getClass().getResource("/general/copy.png"));
    copyHtmlButton.setIcon(copyIcon);
    copyHtmlButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Transferable html = new HtmlSelection(htmlContent, plainContent);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(html, null);
      }
    });
  }

  private void initHistory(ComboBoxWithStoredHistory field) {
    field.reset();
    if (!field.getHistory().isEmpty()) {
      field.setSelectedItem(field.getHistory().get(0));
    }
    field.setMinimumSize(new Dimension(150, field.getMinimumSize().height));
  }

  protected void saveSettings() {
    // Save the state of the history dropdowns
    toTextField.saveCurrentState();
    ccTextField.saveCurrentState();
    bccTextField.saveCurrentState();
    subjectTextField.saveCurrentState();
  }

  @Nullable
  protected JComponent getPreviewComponent() {
    return contentPane;
  }

  protected void initHtmlPanel(String html) throws Exception {
    Reader reader = new StringReader(html);
    InputSource is = new InputSourceImpl(reader);
    UserAgentContext agentContext = new SimpleUserAgentContext();
    HtmlRendererContext rendererContext = new SimpleHtmlRendererContext(htmlPreview);
    DocumentBuilderImpl builder = new DocumentBuilderImpl(agentContext, rendererContext);
    Document document = builder.parse(is);
    htmlPreview.setDocument(document, rendererContext);
  }

  private void createUIComponents() {
    toTextField = new ComboBoxWithStoredHistory("codeReview.toFieldHistory");
    toTextField.setEditable(true);
    ccTextField = new ComboBoxWithStoredHistory("codeReview.ccFieldHistory");
    ccTextField.setEditable(true);
    bccTextField = new ComboBoxWithStoredHistory("codeReview.bccFieldHistory");
    bccTextField.setEditable(true);
    subjectTextField = new ComboBoxWithStoredHistory("codeReview.subjectFieldHistory");
    subjectTextField.setEditable(true);
  }

  public ReviewSettings getChosenSettings() {

    CodeReviewConfig config = ApplicationManager.getApplication().getComponent(CodeReviewConfig.class);
    ReviewSettings settings = new ReviewSettings();

    settings.setSmtpServer(config.getSmtpServer());
    settings.setSmtpPort(config.getSmtpPort());
    settings.setSmtpUsername(config.getSmtpUsername());
    String password = PasswordMangler.decode(config.getEncodedSmtpPassword());
    settings.setSmtpPassword(password);
    settings.setUseSSL(config.isUseSSL());
    settings.setFromAddress(config.getFromAddress());

    settings.setToAddresses(toTextField.getText());
    settings.setCcAddresses(ccTextField.getText());
    settings.setBccAddresses(bccTextField.getText());

    if (config.getSubjectPrefix() != null)
      settings.setSubject(config.getSubjectPrefix() + subjectTextField.getText());
    else
      settings.setSubject(subjectTextField.getText());

    settings.setZipAttached(attachZipCheckBox.isSelected());
    settings.setPlainAttached(plainContent != null && plainContent.length() > 0);
    settings.setHtmlAttached(htmlContent != null && htmlContent.length() > 0);

    return settings;
  }
}
