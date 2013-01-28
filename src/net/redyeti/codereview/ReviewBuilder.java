/**
 * $Source:$
 * $Id:$
 *
 * Copyright 2006 KBC Financial Products - Risk Technology
 */
package net.redyeti.codereview;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diff.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.diff.Diff;

/**
 * Builds up HTML and/or plain text that shows the differences in a list of changes
 *
 * @author <a href="mailto:Chris.Miller@kbcfp.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class ReviewBuilder {

  private static final String PLAIN_EMAIL_TEMPLATE_VM = "/net/redyeti/codereview/resources/plainEmailTemplate.vm";
  private static final String HTML_EMAIL_TEMPLATE_VM = "/net/redyeti/codereview/resources/htmlEmailTemplateInlineStyles.vm";

  private Project project;
  private Collection<Change> changes;
  private String headerMessage;
  private String htmlContent;
  private String plainContent;

  public ReviewBuilder(Project project, Collection<Change> changes, String headerMessage) {
    this.project = project;
    this.changes = changes;
    this.headerMessage = headerMessage;
  }

  public String getHtmlContent() {
    return htmlContent;
  }

  public String getPlainContent() {
    return plainContent;
  }

  public void build() throws BuildException {
    CodeReviewConfig config = ApplicationManager.getApplication().getComponent(CodeReviewConfig.class);

    List<ChangedFile> changeList = buildChangeList(changes, config);

    VelocityContext context = new VelocityContext();
    context.put("comment", headerMessage);
    context.put("config", config);
    context.put("changes", changeList);
    context.put("htmlHelper", new HtmlUtils());

//      plainText = renderTemplate(config.getPlainTextTemplate(), context);
    String template;
    try {
      template = FileUtils.readTextResource(PLAIN_EMAIL_TEMPLATE_VM);
    } catch (IOException e) {
      throw new BuildException("An IO error occurred while loading the plain text template " + PLAIN_EMAIL_TEMPLATE_VM + "\n\n" + e.getMessage(), e);
    }
    if (template == null)
      throw new BuildException("Could not find the plain text template " + PLAIN_EMAIL_TEMPLATE_VM + " on the classpath.");

    plainContent = renderTemplate(template, context);

//      htmlText = renderTemplate(config.getHtmlTemplate(), context);
    if (config.isSendAsHtml()) {
      try {
        template = FileUtils.readTextResource(HTML_EMAIL_TEMPLATE_VM);
      } catch (IOException e) {
        throw new BuildException("An IO error occurred while loading the HTML template " + HTML_EMAIL_TEMPLATE_VM + "\n\n" + e.getMessage(), e);
      }
      if (template == null)
        throw new BuildException("Could not find the HTML template " + HTML_EMAIL_TEMPLATE_VM + " on the classpath.");
      htmlContent = renderTemplate(template, context);
    }
  }

  /**
   * Builds up a user-friendly list of changes that have been made to each file.
   *
   * @param changes the changes selected in the commit dialog, as specified by IDEA's OpenAPI.
   * @param config  the configuration for this plugin.
   * @return
   * @throws BuildException if the content couldn't be retrieved from the VCS.
   */
  private List<ChangedFile> buildChangeList(Collection<Change> changes, CodeReviewConfig config) throws BuildException {
    List<ChangedFile> changeList = new ArrayList<ChangedFile>(changes.size());

    for (Change change : changes) {
      ContentRevision beforeRevision = change.getBeforeRevision();
      ContentRevision afterRevision = change.getAfterRevision();

      if (beforeRevision == null && afterRevision == null)
        continue;

      String beforeName = beforeRevision != null ? beforeRevision.getFile().getPath().replace('\\', '/') : null;
      String afterName = afterRevision != null ? afterRevision.getFile().getPath().replace('\\', '/') : null;
      String beforeRevStr = beforeRevision != null ? beforeRevision.getRevisionNumber().asString() : null;
      String afterRevStr = afterRevision != null ? afterRevision.getRevisionNumber().asString() : null;

      DiffContent beforeContent;
      DiffContent afterContent;
      try {
        beforeContent = createContent(project, beforeRevision);
        afterContent = createContent(project, afterRevision);
      } catch (VcsException e) {
        StringBuilder buf = new StringBuilder(200);
        buf.append("Error loading content from VCS for file\n").append(beforeName != null ? beforeName : afterName);
        if (beforeRevStr == null)
          buf.append(" (new file)");
        else if (afterRevStr == null)
          buf.append(" (revision ").append(beforeRevStr).append(" deleted)");
        else
          buf.append(" (revision ").append(beforeRevStr).append(" modified)");

        buf.append("\n\nReason: ").append(e.getMessage());
        throw new BuildException(buf.toString(), e);
      }

      List<ChangedFile.Line> lines;
      if (beforeContent.isBinary() || afterContent.isBinary()) {
        lines = new ArrayList<ChangedFile.Line>(1);
        if (beforeRevStr == null)
          lines.add(new ChangedFile.Line("Binary file has been added", ChangedFile.LineType.UNCHANGED, 1, 1));
        else if (afterRevStr == null)
          lines.add(new ChangedFile.Line("Binary file has been deleted", ChangedFile.LineType.UNCHANGED, 1, 1));
        else
          lines.add(new ChangedFile.Line("Binary file has been changed", ChangedFile.LineType.UNCHANGED, 1, 1));
      } else {
        CharSequence[] beforeLines = getLinesOfText(beforeContent, config.isIgnoreTrailingWhitespace());
        CharSequence[] afterLines = getLinesOfText(afterContent, config.isIgnoreTrailingWhitespace());

        Diff.Change diff = null;
        if (beforeLines != null && afterLines != null)
          diff = Diff.buildChanges(beforeLines, afterLines);

        lines = buildLines(diff, beforeLines, afterLines, config.getLinesOfContext());
      }
      ChangedFile changedFile = new ChangedFile(beforeName, afterName, beforeRevStr, afterRevStr, lines);
      changeList.add(changedFile);
    }
    return changeList;
  }

  /**
   * Takes two pieces of content and their differences and builds up a list of
   * lines that is suitable for displaying as a diff.
   *
   * @param diff
   * @param beforeLines    the old content as an array of lines
   * @param afterLines     the new content as an array of lines
   * @param linesOfContext the number of unaltered lines of text to retain around
   *                       each difference so the user can see some context for each difference.
   * @return a list of lines that together make up a complete diff suitable for displaying
   *         to an end user.
   */
  private List<ChangedFile.Line> buildLines(Diff.Change diff, CharSequence[] beforeLines, CharSequence[] afterLines, int linesOfContext) {
    if (linesOfContext == 0)
      linesOfContext = 10000;

    List<ChangedFile.Line> lines = new ArrayList<ChangedFile.Line>(100);
    int previousLine = 0;
    while (diff != null) {
      int i = previousLine;

      // Display the unaltered lines (skipping some if there's too many)
      int linesSkippped = diff.line0 - previousLine - 2 * linesOfContext;
      for (; i < diff.line0; i++) {
        if (i < previousLine + linesOfContext || i >= diff.line0 - linesOfContext || linesSkippped == 1)
          lines.add(new ChangedFile.Line(beforeLines[i], ChangedFile.LineType.UNCHANGED, i + 1, diff.line1 - diff.line0 + i + 1));
        else if (i == previousLine + linesOfContext) {
          lines.add(new ChangedFile.Line("...", ChangedFile.LineType.OMITTED, 0, 0, linesSkippped));
        }
      }
      // Display the deleted and/or inserted lines for this difference
      for (i = 0; i < diff.deleted; i++)
        lines.add(new ChangedFile.Line(beforeLines[diff.line0 + i], ChangedFile.LineType.DELETED, diff.line0 + i + 1, 0));
      for (i = 0; i < diff.inserted; i++)
        lines.add(new ChangedFile.Line(afterLines[diff.line1 + i], ChangedFile.LineType.INSERTED, 0, diff.line1 + i + 1));
      previousLine = diff.line0 + diff.deleted;
      diff = diff.link;
    }
    // Display any remaining lines (plus skip some if there's too many)
    int lastLine = Math.min(beforeLines.length, previousLine + linesOfContext);
    for (int i = previousLine; i < lastLine; i++)
      lines.add(new ChangedFile.Line(beforeLines[i], ChangedFile.LineType.UNCHANGED, i + 1, afterLines.length - beforeLines.length + i + 1));

    int linesSkipped = beforeLines.length - lastLine;
    if (linesSkipped > 0) {
      lines.add(new ChangedFile.Line("...", ChangedFile.LineType.OMITTED, 0, 0, linesSkipped));
    }

    setNextAndPreviousStates(lines);

    return lines;
  }

  /**
   * For each line, sets the nextDifferent/previousDifferent flags. This makes rendering
   * the template easier
   */
  private void setNextAndPreviousStates(List<ChangedFile.Line> lines) {
    for (int i = 0; i < lines.size(); i++) {
      ChangedFile.Line thisLine = lines.get(i);
      thisLine.setPreviousDifferent(i == 0 || lines.get(i - 1).getType() != thisLine.getType());
      thisLine.setNextDifferent(i == lines.size() - 1 || lines.get(i + 1).getType() != thisLine.getType());
    }
  }

  /**
   * Retrieves the contents of a particular revision
   */
  private DiffContent createContent(Project project, ContentRevision revision) throws VcsException {
    if (revision == null) return new SimpleContent("");
    FileType fileType = revision.getFile().getFileType();
    Charset utf8 = Charset.forName("UTF-8");
    boolean binary = fileType.isBinary();
    if (revision instanceof CurrentContentRevision) {
      CurrentContentRevision current = (CurrentContentRevision) revision;
      VirtualFile vFile = current.getVirtualFile();
      if (vFile != null) {
        return new FileContent(project, vFile);
      } else {
        if (binary) {
          return new BinaryContent(new byte[0], utf8, fileType);
        } else {
          return new SimpleContent("");
        }
      }
    }
    String revisionContent = revision.getContent();
    if (binary) {
      return revisionContent == null
          ? new BinaryContent(new byte[0], utf8, fileType)
          : new BinaryContent(revisionContent.getBytes(utf8), utf8, fileType);
    } else {
      SimpleContent content = revisionContent == null
          ? new SimpleContent("")
          : new SimpleContent(revisionContent, fileType);
      content.setReadOnly(true);
      return content;
    }
  }

  /**
   * Gets the lines of text that makes up this content as an array of strings
   */
  private CharSequence[] getLinesOfText(DiffContent content, boolean ignoreTrailingWhitespace) {
    Document document = content.getDocument();
    if (document == null)
      return null;
    int lines = document.getLineCount();
    CharSequence[] result = new CharSequence[lines];
    CharSequence text = document.getCharsSequence();

    for (int i = 0; i < lines; i++) {
      CharSequence line = text.subSequence(document.getLineStartOffset(i), document.getLineEndOffset(i));
      if (ignoreTrailingWhitespace) {
        line = rightTrim(line);
      }
      // Convert to a string otherwise the diff seems to fail everything :(
      result[i] = line.toString();
    }
    return result;
  }

  /**
   * Trims any trailing whitespace from a <code>CharSequence</code>.
   *
   * @param line the <code>CharSequence</code> to trim.
   * @return a <code>CharSequence</code> that has no trailing whitespace, but
   *         is otherwise identical to the one supplied.
   */
  private CharSequence rightTrim(CharSequence line) {
    int len = line.length();
    while (0 < len && line.charAt(len - 1) <= ' ') {
      len--;
    }
    line = line.subSequence(0, len);
    return line;
  }

  /**
   * Merge together the supplied template and Velocity context
   */
  private String renderTemplate(String template, VelocityContext context) throws BuildException {
    VelocityEngine engine;
    try {
      engine = VelocityUtils.newVeloictyEngine();
    } catch (Exception e) {
      throw new BuildException("Failed to initialize the Velocity Engine.", e);
    }
    Writer writer = new StringWriter();
    try {
      engine.evaluate(context, writer, "error", template);
    } catch (ParseErrorException e) {
      throw new BuildException("Failed to parse the email template.", e);
    } catch (Exception e) {
      throw new BuildException("Unable to render the email template.", e);
    }
    return writer.toString();
  }
}
