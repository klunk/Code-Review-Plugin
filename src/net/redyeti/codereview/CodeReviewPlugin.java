/**
 * $Source:$
 * $Id:$
 */
package net.redyeti.codereview;

import java.security.Security;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;

/**
 * An IntelliJ IDEA plugin that allows code reviews to be emailed before checkin.
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class CodeReviewPlugin implements ProjectComponent {
  private Project project;

  public CodeReviewPlugin(Project project) {
    this.project = project;
  }

  public void projectOpened() {
  }

  public void projectClosed() {
    project = null;
  }

  public Project getProject() {
    return project;
  }

  @NotNull
  public String getComponentName() {
    return "Code Review";
  }

  public void initComponent() {
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    ChangeListManager.getInstance(project).registerCommitExecutor(new CodeReviewCommitExecutor(project));
  }

  public void disposeComponent() {
  }
}
