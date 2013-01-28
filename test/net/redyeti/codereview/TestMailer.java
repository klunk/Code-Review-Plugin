/**
 * $Source:$
 * $Id:$
 *
 * Copyright 2006 KBC Financial Products - Risk Technology
 */
package net.redyeti.codereview;

import java.io.*;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;

import net.redyeti.util.EmailSettings;

/**
 * Exercises the Mailer class
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class TestMailer {
  public void testMailer() throws MessagingException, IOException {
    Mailer mailer = new Mailer();

    EmailSettings settings = new EmailSettings();
    settings.setSmtpServer("dv1");
    settings.setFromAddress("millerc@kbcfp.com");
    settings.setToAddresses("millerc@kbcfp.com");
    settings.setSubject("Test email");

    // Plain text only
    settings.setPlainContent("Testing plain only");
    mailer.sendMail(settings);

    // HTML only
    settings.setPlainContent(null);
    settings.setHtmlContent("<h1>Testing HTML only</h1>");
    mailer.sendMail(settings);

    // Multipart alternative plain + HTML
    settings.setPlainContent("Testing plain + HTML");
    settings.setHtmlContent("<h1>Testing plain + HTML</h1>");
    mailer.sendMail(settings);

    // Multipart alternative with attachment
    settings.setPlainContent("Testing plain + HTML + attachment");
    settings.setHtmlContent("<h1>Testing plain + HTML + attachment</h1>");
    InputStream is = null;
    try {
      FileInputStream fis = new FileInputStream("C:\\tostring-plugin-src.jar");
      is = new BufferedInputStream(fis, 16384);
      DataSource ds = new ByteArrayDataSource(is, "application/octet-stream");
      settings.addAttachment(new Attachment("tostring.zip", ds));
      mailer.sendMail(settings);
    } finally {
      if (is != null)
        is.close();
    }

    // HTML with attachment
    settings.setPlainContent(null);
    settings.setHtmlContent("<h1>Testing HTML + attachment</h1>");
    mailer.sendMail(settings);

    // Plain text with attachment
    settings.setHtmlContent(null);
    settings.setPlainContent("Testing plain + attachment");
    mailer.sendMail(settings);
  }

  public static void main(String[] args) throws Exception {
    TestMailer mailer = new TestMailer();
    mailer.testMailer();
  }
}
