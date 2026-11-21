package com.sk89q.worldedit.internal.util;

import com.sk89q.worldedit.WorldEditManifest;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class InfoEntryPoint {
   private static final String INSTALL_URL = "https://intellectualsites.github.io/fastasyncworldedit-documentation/";
   private static final String SUPPORT_URL = "https://discord.gg/intellectualsites";

   private static String getMessage(boolean html) {
      WorldEditManifest manifest = WorldEditManifest.load();
      return "To install FastAsyncWorldEdit, place it in the "
         + manifest.getWorldEditKind().folderName
         + " folder.\nFor more detailed instructions, see "
         + formatLink("https://intellectualsites.github.io/fastasyncworldedit-documentation/", html)
         + "\nFor further help, check out our support Discord at "
         + formatLink("https://discord.gg/intellectualsites", html)
         + "\n\nVersion: "
         + manifest.getWorldEditVersion()
         + "\n";
   }

   private static String formatLink(String url, boolean html) {
      return html ? String.format("<a href=\"%1$s\">%1$s</a>", url) : url;
   }

   public static void main(String[] args) {
      if (System.console() != null) {
         System.err.println(getMessage(false));
      } else {
         System.setProperty("awt.useSystemAAFontSettings", "lcd");
         JOptionPane.showMessageDialog(null, new InfoEntryPoint.NavigableEditorPane(getMessage(true)), "FastAsyncWorldEdit", 1);
      }

      System.exit(1);
   }

   private static class NavigableEditorPane extends JTextPane {
      public NavigableEditorPane(String htmlBody) {
         super(new HTMLDocument());
         this.setEditorKit(new HTMLEditorKit());
         this.setText(htmlBody.replace("\n", "<br>"));
         this.setBackground(UIManager.getColor("Panel.background"));
         this.addHyperlinkListener(e -> {
            if (e.getEventType().equals(EventType.ACTIVATED)) {
               try {
                  Desktop.getDesktop().browse(e.getURL().toURI());
               } catch (URISyntaxException | IOException var2) {
                  var2.printStackTrace();
               }
            }
         });
         this.setEditable(false);
         this.setBorder(null);
      }
   }
}
