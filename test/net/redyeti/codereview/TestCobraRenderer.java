package net.redyeti.codereview;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestCobraRenderer extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextArea textArea;
  private HtmlPanel htmlPanel;

  public TestCobraRenderer() {
    setContentPane(contentPane);
    setModal(true);

    textArea.setText("<html><head><style type='text/css'>\n" +
        "body { font-family: ProFontWindows,Monospaced }\n" +
        ".insert { color:#0000ff }\n" +
        ".delete { color:#ff0000 }\n" +
        "</style></head><body class='delete'>Testing<div class='insert'>Testing Insert</div></body></html>");

    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {onOK();}
    });

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {onCancel();}
    });

// call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

// call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onOK() {
    try {
      updateHtml();
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  private void onCancel() {
    dispose();
  }

  private void updateHtml() throws IOException, SAXException {
    Reader reader = new StringReader(textArea.getText());
    InputSource is = new InputSourceImpl(reader);
    UserAgentContext agentContext = new SimpleUserAgentContext();
    HtmlRendererContext rendererContext = new LocalHtmlRendererContext(htmlPanel);
    DocumentBuilderImpl builder = new DocumentBuilderImpl(agentContext, rendererContext);
    Document document = builder.parse(is);
    htmlPanel.setDocument(document, rendererContext);
  }

  private static class LocalHtmlRendererContext extends SimpleHtmlRendererContext {
    // Override methods here to implement browser functionality

    public LocalHtmlRendererContext(HtmlPanel contextComponent) {
      super(contextComponent);
    }
  }

  public static void main(String[] args) {
    TestCobraRenderer dialog = new TestCobraRenderer();
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
