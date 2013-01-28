package net.redyeti.util;

import java.io.*;
import java.util.zip.*;

/**
 * Creates a zip file by adding input streams that represent files one by one.
 */
public class ZipBuilder {
  private static final int BUFFER = 2048;

  private byte data[] = new byte[BUFFER];
  private ZipOutputStream zipOut;

  /**
   * Generates a zip file.
   *
   * @param out the output stream to write the zip file to as it is built.
   */
  public ZipBuilder(OutputStream out) {
    OutputStream bufOut = new BufferedOutputStream(out, 16384);
    zipOut = new ZipOutputStream(bufOut);
    zipOut.setMethod(Deflater.DEFLATED);
    zipOut.setLevel(Deflater.BEST_COMPRESSION);
  }

  /**
   * Sets a comment for the zip file.
   */
  public void setComment(String comment) {
    zipOut.setComment(comment);
  }

  /**
   * Adds data to the zip file.
   *
   * @param name   the name to give the file.
   * @param source an input stream to use as the source for the file.
   * @throws IOException if there was a problem writing the stream to the zip file.
   */
  public void addFile(String name, InputStream source) throws IOException {
    ZipEntry entry = new ZipEntry(name);
    zipOut.putNextEntry(entry);

    int count;
    while ((count = source.read(data, 0, BUFFER)) != -1) {
      zipOut.write(data, 0, count);
    }
    source.close();
  }

  /**
   * Adds a file to the zip file.
   *
   * @param name the name to give the file.
   * @param file the file to add.
   * @throws IOException if there was a problem writing the stream to the zip file.
   */
  public void addFile(String name, File file) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    try {
      addFile(name, fis);
    } finally {
      fis.close();
    }
  }

  /**
   * Closes the output stream. This should be called once the zip file
   * has been read off the output stream.
   *
   * @throws IOException if there was a problem closing the output stream.
   */
  public void close() throws IOException {
    zipOut.close();
  }
}