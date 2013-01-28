/**
 * $Source:$
 * $Id:$
 */
package net.redyeti.codereview;

import java.util.List;

/**
 * Represents the differences between two file revisions. A <code>ChangedFile</code> instance
 * contains one or more {@link Line} objects that represent individual line changes.
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class ChangedFile {

  public enum LineType {
    INSERTED, DELETED, UNCHANGED, OMITTED
  }

  private String beforeFilename;
  private String afterFilename;
  private String beforeRevision;
  private String afterRevision;
  private List<Line> lines;
  private int maxLineNumberDigits;
  private boolean haveSearchedForMax;

  public ChangedFile(String beforeFilename, String afterFilename, String beforeRevision, String afterRevision, List<Line> lines) {
    this.beforeFilename = beforeFilename;
    this.afterFilename = afterFilename;
    this.beforeRevision = beforeRevision;
    this.afterRevision = afterRevision;
    this.lines = lines;
  }

  public String getBeforeFilename() {
    return beforeFilename;
  }

  public String getAfterFilename() {
    return afterFilename;
  }

  public String getBeforeRevision() {
    return beforeRevision;
  }

  public String getAfterRevision() {
    return afterRevision;
  }

  /**
   * @return The maximum number of digits in any of the line numbers for this change.
   */
  public int getMaxDigits() {
    if (!haveSearchedForMax) {
      haveSearchedForMax = true;
      int i = lines.size();
      while (--i >= 0) {
        Line line = lines.get(i);
        if (line.beforeLineNumber != 0) {
          int n = line.beforeLineNumber * 10;
          while ((n /= 10) != 0)
            maxLineNumberDigits++;
          break;
        }
      }
    }
    return maxLineNumberDigits;
  }

  public List<Line> getLines() {
    return lines;
  }

  /**
   * Holds the state of a single line of text.
   */
  public static class Line {
    private CharSequence text;
    private LineType type;
    private boolean previousDifferent, nextDifferent;
    private final int beforeLineNumber;
    private final int afterLineNumber;
    private int numLinesOmitted;

    public Line(CharSequence text, LineType type, int beforeLineNumber, int afterLineNumber) {
      this(text, type, beforeLineNumber, afterLineNumber, 0);
    }

    public Line(CharSequence text, LineType type, int beforeLineNumber, int afterLineNumber, int numLinesOmitted) {
      this.text = text;
      this.type = type;
      this.beforeLineNumber = beforeLineNumber;
      this.afterLineNumber = afterLineNumber;
      this.numLinesOmitted = numLinesOmitted;
    }

    /**
     * @return The line of text that is represented by this line object.
     */
    public CharSequence getText() {
      return text;
    }

    /**
     * @return Which line in the file this was before the change. Zero means the line
     *         didn't exist before the change.
     */
    public int getBeforeLineNumber() {
      return beforeLineNumber;
    }

    /**
     * @return Which line in the file this was after the change was made. Zero means the line
     *         was removed.
     */
    public int getAfterLineNumber() {
      return afterLineNumber;
    }

    /**
     * @return <code>true</code> if this line was inserted into the newer revision.
     */
    public boolean isInserted() {
      return type == LineType.INSERTED;
    }

    /**
     * @return <code>true</code> if this line was deleted from the newer revision.
     */
    public boolean isDeleted() {
      return type == LineType.DELETED;
    }

    /**
     * @return <code>true</code> if this line was unchanged between revisions.
     */
    public boolean isUnchanged() {
      return type == LineType.UNCHANGED;
    }

    /**
     * @return <code>true</code> if this line is a placeholder indicating that some
     *         unchanged lines have been removed. To find out how many lines were omitted,
     *         call {@link #getNumLinesOmitted()}.
     */
    public boolean isOmitted() {
      return type == LineType.OMITTED;
    }

    /**
     * @return The number of unaltered lines that were skipped. The number of lines
     *         to skip is dynamically determined based on the <i>Lines of context</i> setting
     *         in the code review settings.
     */
    public int getNumLinesOmitted() {
      return numLinesOmitted;
    }

    /**
     * Indicates what sort of line this represents.
     *
     * @return the type of line (INSERTED, DELETED, CHANGED, OMITTED)
     */
    public LineType getType() {
      return type;
    }

    public boolean isPreviousDifferent() {
      return previousDifferent;
    }

    protected void setPreviousDifferent(boolean previousDifferent) {
      this.previousDifferent = previousDifferent;
    }

    public boolean isNextDifferent() {
      return nextDifferent;
    }

    protected void setNextDifferent(boolean nextDifferent) {
      this.nextDifferent = nextDifferent;
    }

    /**
     * @return In the case of an inserted, deleted or unchanged line this returns the line
     *         of text that was inserted, deleted or unchanged. In the case of an omitted line this
     *         just contains the string <code>"..."</code> to indicate that multiple lines have been
     *         skipped.
     */
    @Override
    public String toString() {
      return text.toString();
    }
  }
}
