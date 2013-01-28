package net.redyeti.codereview;

import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;

import net.redyeti.util.EmailSettings;

public class Mailer {

  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 25;

  /**
   * Sends an email.
   *
   * @throws MessagingException if there was a problem sending the email
   */
  public void sendMail(EmailSettings emailSettings) throws MessagingException {

    String host = emailSettings.getSmtpServer();
    if (host == null || host.length() == 0)
      host = DEFAULT_HOST;
    int port = emailSettings.getSmtpPort() <= 0 ? DEFAULT_PORT : emailSettings.getSmtpPort();
    final String username = emailSettings.getSmtpUsername();
    final String password = emailSettings.getSmtpPassword();
    String from = emailSettings.getFromAddress();
    String replyTo = emailSettings.getReplyToAddress();
    String to = emailSettings.getToAddresses();
    String cc = emailSettings.getCcAddresses();
    String bcc = emailSettings.getBccAddresses();
    String subject = emailSettings.getSubject();
    String htmlText = emailSettings.getHtmlContent();
    String plainText = emailSettings.getPlainContent();
    List<Attachment> attachments = emailSettings.getAttachments();

    if (from == null || from.length() == 0)
      throw new MessagingException("A 'from' address must be specified. Please check the plugin settings.");
    if (to == null || to.length() == 0)
      throw new MessagingException("A 'to' address must be specified.");

    Properties props = new Properties();

    if (emailSettings.isUseSSL()) {
      props.put("mail.smtps.auth", "true");
    }
    Session session = Session.getDefaultInstance(props);
    MimeMessage message = new MimeMessage(session);

    InternetAddress fromAddr = new InternetAddress(from, true);
    message.setFrom(fromAddr);

    if (replyTo != null && replyTo.length() > 0) {
      String[] replyTos = replyTo.split(",");
      InternetAddress[] replyToAddrs = new InternetAddress[replyTos.length];
      for (int i = 0; i < replyTos.length; i++) {
        try {
          replyToAddrs[i] = new InternetAddress(replyTos[i], true);
        }
        catch (AddressException e) {
          throw new MessagingException("The reply-to address '" + replyTos[i] + "' is not a valid email address.");
        }
      }
      message.setReplyTo(replyToAddrs);
    }

    addRecipients(message, Message.RecipientType.TO, to);
    addRecipients(message, Message.RecipientType.CC, cc);
    addRecipients(message, Message.RecipientType.BCC, bcc);

    if (subject != null)
      message.setSubject(subject, "UTF-8");

    boolean havePlain = plainText != null && plainText.length() > 0;
    boolean haveHtml = htmlText != null && htmlText.length() > 0;
    boolean haveAttachments = attachments != null;

    Multipart alt = null;
    if (havePlain && haveHtml) {
      alt = buildAlternativePart(plainText, htmlText);
    }

    if (!haveAttachments) {
      if (!haveHtml) {
        // Just plain-text content
        if (plainText == null)
          plainText = "(No body specified)";
        message.setText(plainText, "UTF-8");
      } else if (!havePlain) {
        // Just HTML content
        if (htmlText == null)
          htmlText = "<body>(No body specified)</body>";
        message.setText(htmlText, "UTF-8", "html");
      } else {
        message.setContent(alt);
      }
    } else {
      Multipart att = new MimeMultipart();
      if (!haveHtml) {
        att.addBodyPart(buildPlainBodyPart(plainText));
        message.setContent(att);
      } else if (!havePlain) {
        att.addBodyPart(buildHtmlBodyPart(htmlText));
        message.setContent(att);
      } else {
        Multipart root = new MimeMultipart();
        MimeBodyPart child = new MimeBodyPart();
        child.setContent(alt);
        root.addBodyPart(child);
        child = new MimeBodyPart();
        child.setContent(att);
        root.addBodyPart(child);
        message.setContent(root);
      }
      buildAttachmentPart(att, attachments);
    }
    Transport tr = session.getTransport(emailSettings.isUseSSL() ? "smtps" : "smtp");
    tr.connect(host, port, username, password);
    message.saveChanges();
    tr.sendMessage(message, message.getAllRecipients());
    tr.close();
  }

  private Multipart buildAlternativePart(String plainText, String htmlText) throws MessagingException {
    Multipart alternativePart = new MimeMultipart("alternative");
    BodyPart bodyPlain = buildPlainBodyPart(plainText);
    alternativePart.addBodyPart(bodyPlain);
    BodyPart bodyHtml = buildHtmlBodyPart(htmlText);
    alternativePart.addBodyPart(bodyHtml);
    return alternativePart;
  }

  private BodyPart buildHtmlBodyPart(String htmlText) throws MessagingException {
    MimeBodyPart bodyHtml = new MimeBodyPart();
    bodyHtml.setText(htmlText, "UTF-8", "html");
    return bodyHtml;
  }

  private BodyPart buildPlainBodyPart(String plainText) throws MessagingException {
    MimeBodyPart bodyPlain = new MimeBodyPart();
    bodyPlain.setText(plainText, "UTF-8");
    return bodyPlain;
  }

  private void buildAttachmentPart(Multipart part, List<Attachment> attachments) throws MessagingException {
    for (Attachment attachment : attachments) {
      MimeBodyPart att = new MimeBodyPart();
      att.setDataHandler(new DataHandler(attachment.getDataSource()));
      att.setFileName(attachment.getName());
      att.setHeader("Content-Type", attachment.getDataSource().getContentType());
      att.setHeader("Content-Transfer-Encoding", "BASE64");
      part.addBodyPart(att);
    }
  }

  private void addRecipients(MimeMessage message, Message.RecipientType type, String addresses) throws MessagingException {
    if (addresses != null && addresses.length() > 0) {
      try {
        message.addRecipients(type, addresses);
      }
      catch (AddressException e) {
        throw new MessagingException("The '" + type.toString() + "' address list is not valid: " + e.getMessage());
      }
    }
  }
}
