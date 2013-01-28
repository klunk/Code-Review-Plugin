/**
 * $Source:$
 * $Id:$
 */
package net.redyeti.codereview;

import java.io.*;

/**
 * Utilities for reading and writing files and net.redyeti.codereview.resources
 *
 * @author <a href="mailto:chris_overseas@hotmail.com">Chris Miller</a>
 * @version $Revision: 1.0$
 */
public class FileUtils {
  private FileUtils() {}

  public static String readTextResource(String resource) throws IOException {
    InputStream is = FileUtils.class.getResourceAsStream(resource);
    if (is == null) {
      return null;
    } else {
      return readTextStream(is, "UTF-8");
    }
  }

  public static String readTextFile(String filename) throws IOException {
    return readTextFile(new File(filename));
  }

  public static String readTextFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);
    return readTextStream(is, "UTF-8");
  }

  public static void saveFile(String filename, String content) throws IOException {
    FileOutputStream fos = new FileOutputStream(filename);
    BufferedOutputStream bos = new BufferedOutputStream(fos, 8092);
    Writer osw = null;
    try {
      osw = new OutputStreamWriter(bos, "UTF-8");
      osw.write(content);
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UnsupportedEncodingException - This should never happen!", e);
    }
    finally {
      if (osw != null) {
        osw.close();
      }
      fos.close();
    }
  }

  private static String readTextStream(InputStream is, String encoding) throws IOException {
    StringBuilder buf;
    Reader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(is, encoding), 8092);
      buf = new StringBuilder();
      for (int i = in.read(); i != -1; i = in.read()) {
        buf.append((char) i);
      }
    }
    finally {
      if (in != null) {
        in.close();
      }
      is.close();
    }
    return buf.toString();
  }
}
