/**
 * $Source:$
 * $Id:$
 */
package net.redyeti.codereview;

import java.io.*;
import java.util.Collection;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;

import net.redyeti.util.EmailSettings;
import net.redyeti.util.ZipBuilder;

/**
 * Presents the code review dialog to the user and then sends the email if the user
 * clicked OK.
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
class CodeReviewSender implements Runnable {

  private final Project project;
  private final Collection<Change> changes;
  private final EmailSettings emailSettings;
  private final boolean attachZip;

  CodeReviewSender(Project project, Collection<Change> changes, ReviewSettings settings, String plainText, String htmlText) {
    this.project = project;
    this.changes = changes;
    emailSettings = createEmailSettings(settings, plainText, htmlText);
    attachZip = settings.hasZipAttached();
  }

  public void run() {
    Mailer mailer = new Mailer();
    try {
      if (attachZip) {
        Attachment zip = createZipAttachment(changes);
        emailSettings.addAttachment(zip);
      }
      mailer.sendMail(emailSettings);
    }
    catch (MessagingException e) {
      Throwable cause = e;
      while (cause.getCause() != null)
        cause = cause.getCause();
      StringBuilder buf = new StringBuilder(200);
      int i = 0, size = cause.getStackTrace().length;
      for (StackTraceElement ste : cause.getStackTrace()) {
        if (++i > 10)
          break;
        buf.append(ste.toString()).append('\n');
      }
      if (size > 10)
        buf.append("and ").append(size - 10).append(" more...");
      Messages.showMessageDialog("The attempt to send the code review email failed.\n\nReason: " + cause.getClass().getSimpleName() + " - " + cause.getMessage() + "\n\n" + buf.toString(),
                                 "Code Review Error",
                                 Messages.getErrorIcon());
    } catch (IOException e) {
      Messages.showMessageDialog("A problem occurred while creating the zip file.\n\nReason: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                                 "Code Review Error",
                                 Messages.getErrorIcon());
    }
  }

  private EmailSettings createEmailSettings(ReviewSettings settings, String plainText, String htmlText) {
    EmailSettings emailSettings = new EmailSettings();
    emailSettings.setToAddresses(settings.getToAddresses());
    emailSettings.setFromAddress(settings.getFromAddress());
    emailSettings.setCcAddresses(settings.getCcAddresses());
    emailSettings.setBccAddresses(settings.getBccAddresses());
    emailSettings.setReplyToAddress(settings.getReplyToAddress());
    emailSettings.setSubject(settings.getSubject());
    emailSettings.setPlainContent(plainText);
    emailSettings.setHtmlContent(htmlText);
    emailSettings.setSmtpServer(settings.getSmtpServer());
    emailSettings.setSmtpPort(settings.getSmtpPort());
    emailSettings.setSmtpUsername(settings.getSmtpUsername());
    emailSettings.setSmtpPassword(settings.getSmtpPassword());
    emailSettings.setUseSSL(settings.isUseSSL());
    return emailSettings;
  }

  private Attachment createZipAttachment(Collection<Change> changes) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream(16384);
    ZipBuilder builder = new ZipBuilder(os);
    for (Change change : changes) {
      ContentRevision revision = change.getAfterRevision();
      if (revision != null) {
        VirtualFile vf = revision.getFile().getVirtualFile();
        builder.addFile(getZipEntryName(vf), vf.getInputStream());
      }
    }
    builder.close();

    InputStream is = new ByteArrayInputStream(os.toByteArray());
    DataSource ds = new ByteArrayDataSource(is, "application/octet-stream");
    return new Attachment("CodeReview.zip", ds);
  }

  private String getZipEntryName(VirtualFile vf) {
    ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    Module module = fileIndex.getModuleForFile(vf);
    String moduleName = module == null ? "Unknown Module" : module.getName();
    VirtualFile root = fileIndex.getContentRootForFile(vf);
    String path = root == null ? "/" : vf.getPath().substring(root.getPath().length());
    return moduleName + path;
  }
}
