package net.redyeti.util;

import java.io.*;

/**
 * Gives the ZipBuilder class a bit of a workout.
 */
public class TestZipBuilder {
  public static void main(String[] args) throws IOException {
    OutputStream os = new FileOutputStream("C:\\test.zip");
    ZipBuilder builder = new ZipBuilder(os);

    String test = "this is a test text file";
    InputStream is = new ByteArrayInputStream(test.getBytes("UTF-8"));
    builder.addFile("this\\is\\a\\test.txt", is);

    test = "test file number 2!";
    is = new ByteArrayInputStream(test.getBytes("UTF-8"));
    builder.addFile("this\\is\\the\\2nd\\test2.txt", is);

    test = "final test";
    is = new ByteArrayInputStream(test.getBytes("UTF-8"));
    builder.addFile("final\\test3.txt", is);

    builder.setComment("This is a comment for the zip file");
    builder.close();
  }
}