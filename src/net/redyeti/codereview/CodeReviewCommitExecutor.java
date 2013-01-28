/**
 * $Source:$
 * $Id:$
 */
package net.redyeti.codereview;

import javax.swing.Icon;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.CommitSession;

/**
 * The code review plugin that is responsible for creating the CommitSession
 * object.
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class CodeReviewCommitExecutor implements CommitExecutor {
  private Project project;

  public CodeReviewCommitExecutor(Project project) {
    this.project = project;
  }

  @NotNull
  public Icon getActionIcon() {
    return IconLoader.getIcon("/actions/back.png");
  }

  @Nls
  public String getActionText() {
    return "Code Review...";
  }

  @Nls
  public String getActionDescription() {
    return "Sends a code review email before code is committed to the VCS";
  }

  @NotNull
  public CommitSession createCommitSession() {
    return new CodeReviewCommitSession(project);
  }
}
