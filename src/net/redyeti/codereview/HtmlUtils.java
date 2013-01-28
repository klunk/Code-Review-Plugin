/**
 * $Source:$
 * $Id:$
 */
package net.redyeti.codereview;

/**
 * Text handling helper methods for dealing with HTML.
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class HtmlUtils {
  public static String escape(Object obj) {
    return escape(obj.toString());
  }

  public static String escape(String str) {
    StringBuilder result = new StringBuilder((int) (str.length() * 1.05 + 20));

    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      switch (c) {
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '\"':
          result.append("&quot;");
          break;
        case '\'':
          result.append("&#039;");
          break;
        case '\\':
          result.append("&#092;");
          break;
        case '&':
          result.append("&amp;");
          break;
        case '\n':
          result.append("<br/>");
          break;
        case '\r':
          break;
        default:
          result.append(c);
      }
    }
    return result.toString();
  }

  public static String pad(int number, int size) {
    StringBuilder buf = new StringBuilder(size);
    buf.setLength(size);
    for (int i = size; --i >= 0;) {
      int digit = number % 10;
      number /= 10;
      if (number != 0 || digit != 0)
        buf.setCharAt(i, (char) ('0' + digit));
      else
        buf.setCharAt(i, ' ');
    }
    return buf.toString();
  }
}
