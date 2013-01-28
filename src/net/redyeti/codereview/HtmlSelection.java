package net.redyeti.codereview;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

public class HtmlSelection implements Transferable {

  private static final List<DataFlavor> htmlFlavors = new ArrayList<DataFlavor>();
  private static final List<DataFlavor> textFlavors = new ArrayList<DataFlavor>();
  private static final List<DataFlavor> bothFlavors = new ArrayList<DataFlavor>();

  static {
    try {
      textFlavors.add(new DataFlavor("text/plain;class=java.lang.String"));
      textFlavors.add(new DataFlavor("text/plain;class=java.io.Reader"));
      textFlavors.add(new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream"));
      htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
      htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
      htmlFlavors.add(new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"));
      bothFlavors.addAll(textFlavors);
      bothFlavors.addAll(htmlFlavors);
    }
    catch (ClassNotFoundException ex) {
      // Won't happen...
    }
  }

  private final String html;
  private final String plainText;
  private final List<DataFlavor> flavors;

  public HtmlSelection(String html, String plainText) {
    if (html == null && plainText == null) {
      throw new IllegalArgumentException("Both html and plainText cannot be null");
    }
    if (html == null) {
      flavors = textFlavors;
    } else if (plainText == null) {
      flavors = htmlFlavors;
    } else {
      flavors = bothFlavors;
    }
    this.html = html;
    this.plainText = plainText;
  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors.toArray(new DataFlavor[flavors.size()]);
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavors.contains(flavor);
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
    String result;
    if (flavor.isMimeTypeEqual("text/plain")) {
      if (plainText == null)
        throw new UnsupportedFlavorException(flavor);
      result = plainText;
    }
    else {
      if (html == null)
        throw new UnsupportedFlavorException(flavor);
      result = html;
    }

    if (String.class.equals(flavor.getRepresentationClass())) {
      return result;
    } else if (Reader.class.equals(flavor.getRepresentationClass())) {
      return new StringReader(result);
    } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
      try {
        return new ByteArrayInputStream(result.getBytes("UTF-8"));
      }
      catch (UnsupportedEncodingException e) {
        // Won't happen...
      }
    }
    throw new UnsupportedFlavorException(flavor);
  }
}
