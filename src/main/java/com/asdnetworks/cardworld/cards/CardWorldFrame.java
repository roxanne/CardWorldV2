/*

Copyright 2010, 2012 James A. Mason, York University, Toronto, Canada
http://www.yorku.ca/jmason

Unless required by applicable law or agreed to in writing, this software
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
ANY KIND, either express or implied.

You may use and modify it for research purposes, provided that
the copyright holder above is acknowledged as the original author.

 */

package com.asdnetworks.cardworld.cards;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.Toolkit;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager.LookAndFeelInfo;

/*
   CardWorldFrame2 defines the window through which
   CardWorld interacts with a user.
   It contains a display of the card table top,
   a line in the middle for text input,
   and a pane at the bottom for text messages.
*/
class CardWorldFrame extends JFrame
{
   public CardWorldFrame(CardAgent agnt, CardTable t)
   {  agent = agnt;
      cardTable = t;
      setTitle("CardWorld 2");
      
     // Trying to set Nimbus look and feel
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
      
      addWindowListener(new WindowCloser(this));
         // listens for window closing events (see below)
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      JMenuBar menuBar = new JMenuBar();
      setJMenuBar(menuBar);
      WindowMenu windowMenu = new WindowMenu(this);
      windowMenu.setMnemonic(KeyEvent.VK_W);
      menuBar.add(windowMenu);
      HelpMenu hMenu = new HelpMenu(this);
      hMenu.setMnemonic(KeyEvent.VK_H);
      menuBar.add(hMenu);

      cardTable.setWindow(this);
      setSize(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
      cardTable.setMinimumSize(new Dimension(DEFAULT_FRAME_WIDTH,
         DEFAULT_PANEL_HEIGHT));

       // Main panel
      panelScrollPane = new JScrollPane(cardTable);
      panelScrollPane.setPreferredSize(new Dimension(DEFAULT_FRAME_WIDTH,
         DEFAULT_PANEL_HEIGHT));
      pane = new JPanel(new BorderLayout());
      pane.setLayout(
         new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(panelScrollPane, BorderLayout.CENTER);
      utteranceField = new JTextField(40);
      utteranceField.setFont(FONT);
      utteranceField.setSize(DEFAULT_FRAME_WIDTH, 10);
      utteranceField.addActionListener(
         new UtteranceFieldListener(this));
      pane.add(
         new LabeledTextField("Input? ", utteranceField));
      outputPane = new JTextArea();
      outputPane.setFont(FONT);
      bottomScrollPane = new JScrollPane(outputPane);
      bottomScrollPane.setPreferredSize(new Dimension(DEFAULT_FRAME_WIDTH, 20));

      pane.add(bottomScrollPane);
      Container contentPane = getContentPane();
      contentPane.removeAll();
      contentPane.add(pane);
      setVisible(true);
   }

   void close()
   {  processWindowEvent(
         new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
   }

   JTextArea getOutputPane()
   {
      return outputPane;
   }

   JTextField getUtteranceField()
   {
      return utteranceField;
   }

   private void utteranceFieldChanged()
   {
      String command = utteranceField.getText();
      agent.interpretCommand(command);
   }

   void showAboutInfo()
      throws FileNotFoundException, IOException, URISyntaxException
   {  // responds to About CardWorld choice in Help menu
     // CardWorldAboutWindow w = new CardWorldAboutWindow(
        // "CardWorld2Documentation.txt");
      
       DesktopClassToLaunch.openUrl
       ("http://www.yorku.ca/jmason/CardWorld/CardWorld2Documentation.html");
      
   //}
   }

   
    private static ResourceBundle resources;
  //  private final static String EXIT_AFTER_PAINT = "-exit";
  //  private static boolean exitAfterFirstPaint;

    static {
       try {
           resources = ResourceBundle.getBundle("CardWorld",
                   Locale.getDefault());
            //String propertyValue = resources.getString("key")
       } catch (MissingResourceException mre) {
            System.err.println("resources/CardWorld.properties not found");
            System.exit(1);
       }
   }
   

  static String VERSION = "2.0";
   private CardAgent agent;
   private CardTable cardTable;
   private JTextArea outputPane;
   private JTextField utteranceField;
   static final int DEFAULT_FRAME_WIDTH = 800;  // window width
   static final int DEFAULT_FRAME_HEIGHT = 600; // window height
   static final int DEFAULT_PANEL_HEIGHT = 400;
;
   static final Font FONT
      = new Font("Monospaced", Font.BOLD, 14);
   private JPanel pane;  // main panel for the window
   private JScrollPane panelScrollPane;
   private JScrollPane bottomScrollPane;

   /**
      An instance defines what should happen when a window
      closes.
    */
   class WindowCloser extends WindowAdapter
   {  WindowCloser(CardWorldFrame w)
      {  window = w;
      }

      public void windowClosing(WindowEvent e)
      {
            window.setVisible(false);
            window.dispose();
            System.exit(0);        // stop the program
      }

      CardWorldFrame window;
   } // end class WindowCloser

   private class UtteranceFieldListener implements ActionListener
   {
      UtteranceFieldListener(CardWorldFrame w)
      {  window = w;
      }

      public void actionPerformed(ActionEvent e)
      {  window.utteranceFieldChanged();
      }

      private CardWorldFrame window;
   } // end class UtteranceFieldListener


   private class LabeledTextField extends JPanel
   {  LabeledTextField(String labelText, JTextField textField)
      {  setMaximumSize(new Dimension(800,10));
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         JLabel label = new JLabel(labelText);
         textField.setFont(CardWorldFrame.FONT);
         this.add(label);
         this.add(textField);
      }
   } // end class LabeledTextField

} // end class CardWorldFrame2


class WindowMenu extends JMenu implements ActionListener
{  WindowMenu(CardWorldFrame w)
   {  super("Window");
      window = w;

      JMenuItem closeMenuItem = new JMenuItem("Close",
         KeyEvent.VK_C);
      closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_C, ActionEvent.CTRL_MASK));
      closeMenuItem.addActionListener(this);
      add(closeMenuItem);
   }

   /**
     Listens for menu item events.
    */
   public void actionPerformed(ActionEvent e)
   {  String command = e.getActionCommand();
      if (command.equals("Close"))
         window.close();
   }

   private CardWorldFrame window;
} // end class WindowMenu

class HelpMenu extends JMenu implements ActionListener
{  HelpMenu(CardWorldFrame w)
   {  super("Help");
      window = w;
      JMenuItem aboutMenuItem = new JMenuItem("About CardWorld",
         KeyEvent.VK_A);
      add(aboutMenuItem);
      aboutMenuItem.addActionListener(this);
   }

   /**
      Listens for menu item events.
    */
   public void actionPerformed(ActionEvent e)
   {  String command = e.getActionCommand();
      if (command.equals("About CardWorld"))
         try
         {
            try {
                window.showAboutInfo();
            } catch (URISyntaxException ex) {
                Logger.getLogger(HelpMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
         catch (FileNotFoundException ex)
         {}
         catch (IOException ex)
         {}
   }

   private CardWorldFrame window;
} // end class HelpMenu

class OpenURI {

    public static void main(String [] args) {

        if( !java.awt.Desktop.isDesktopSupported() ) {

            System.err.println( "Desktop is not supported (fatal)" );
            System.exit( 1 );
        }

        if ( args.length == 0 ) {

            System.out.println( "Usage: OpenURI [URI [URI ... ]]" );
            System.exit( 0 );
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {

            System.err.println( "Desktop doesn't support the browse action (fatal)" );
            System.exit( 1 );
        }

        for ( String arg : args ) {

            try {

                java.net.URI uri = new java.net.URI( arg );
                desktop.browse( uri );
            }
            catch ( Exception e ) {

                System.err.println( e.getMessage() );
            }
        }
    }
}


class DesktopClassToLaunch {

   public static void openUrl(String a) throws URISyntaxException {
      try {
        URI uri = new
        URI ("http://www.yorku.ca/jmason/CardWorld/CardWorld2Documentation.html");
        Desktop desktop = null;

        if (Desktop.isDesktopSupported()) {
        desktop = Desktop.getDesktop();
        }
        if (desktop != null)
        desktop.browse(uri);
        } catch (IOException ioe) {

        ioe.printStackTrace();
        } catch (URISyntaxException use) {
        use.printStackTrace();
        }
    }
}


class CardWorldAboutWindow extends JFrame
{
   public CardWorldAboutWindow(String fileName)
      throws FileNotFoundException, IOException
   {
      setTitle("About CardWorld");
      setSize(700, 700);
      FileReader inputStream = new FileReader(fileName);

      StringBuilder outputStream = new StringBuilder(1000);

      while (inputStream.ready())
      {
         int nextChar = inputStream.read();
         outputStream.append((char) nextChar);
      }
      inputStream.close();
      String text = outputStream.toString();

      JEditorPane textPane = new JEditorPane();
      textPane.setContentType("text");
      textPane.setText(text);
      textPane.setFont(ABOUTFONT);

      textPane.setEditable(false);
      textPane.setCaretPosition(0); // to show text from the beginning

      JScrollPane textScrollPane = new JScrollPane(textPane);
      textScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      textScrollPane.setPreferredSize(new Dimension(700, 700));
      textScrollPane.setMinimumSize(new Dimension(10, 10));
      JPanel pane = new JPanel(new BorderLayout());
      pane.add(textScrollPane, BorderLayout.CENTER);
      Container contentPane = getContentPane();
      contentPane.removeAll();
      contentPane.add(pane);
      setVisible(true);
      textPane.repaint();
   }

   static final Font ABOUTFONT
      = new Font("Courier New", Font.PLAIN, 14);
   private TextArea textPane;
   private JScrollPane textScrollPane;

}
