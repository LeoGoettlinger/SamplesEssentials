package com.earth2me.essentials.textreader;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.I18n;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextPager {
   private final transient IText text;
   private final transient boolean onePage;

   public TextPager(final IText text) {
      this(text, false);
   }

   public TextPager(final IText text, final boolean onePage) {
      this.text = text;
      this.onePage = onePage;
   }

   public void showPage(final String pageStr, final String chapterPageStr, final String commandName, final CommandSource sender) {
      List<String> lines = this.text.getLines();
      List<String> chapters = this.text.getChapters();
      Map<String, Integer> bookmarks = this.text.getBookmarks();
      if (pageStr != null && !pageStr.isEmpty() && !pageStr.matches("[0-9]+")) {
         int chapterpage = 0;
         if (chapterPageStr != null) {
            try {
               chapterpage = Integer.parseInt(chapterPageStr) - 1;
            } catch (NumberFormatException var15) {
            }

            if (chapterpage < 0) {
               chapterpage = 0;
            }
         }

         if (!bookmarks.containsKey(pageStr.toLowerCase(Locale.ENGLISH))) {
            sender.sendTl("infoUnknownChapter");
         } else {
            int chapterstart = bookmarks.get(pageStr.toLowerCase(Locale.ENGLISH)) + 1;

            int chapterend;
            for (chapterend = chapterstart; chapterend < lines.size(); chapterend++) {
               String line = lines.get(chapterend);
               if (line.length() > 0 && line.charAt(0) == '#') {
                  break;
               }
            }

            int start = chapterstart + (this.onePage ? 0 : chapterpage * 9);
            int page = chapterpage + 1;
            int pages = (chapterend - chapterstart) / 9 + ((chapterend - chapterstart) % 9 > 0 ? 1 : 0);
            if (!this.onePage && commandName != null) {
               StringBuilder content = new StringBuilder();
               content.append(I18n.capitalCase(commandName)).append(": ");
               content.append(pageStr);
               sender.sendTl("infoChapterPages", content, page, pages);
            }

            for (int i = start; i < chapterend && i < start + (this.onePage ? 20 : 9); i++) {
               sender.sendMessage("§r" + lines.get(i));
            }

            if (!this.onePage && page < pages && commandName != null) {
               sender.sendTl("readNextPage", commandName, pageStr + " " + (page + 1));
            }
         }
      } else if (!lines.isEmpty() && lines.get(0).startsWith("#")) {
         if (!this.onePage) {
            sender.sendTl("infoChapter");
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            for (String string : chapters) {
               if (!first) {
                  sb.append(", ");
               }

               first = false;
               sb.append(string);
            }

            sender.sendMessage(sb.toString());
         }
      } else {
         int pagex = 1;
         if (pageStr != null) {
            try {
               pagex = Integer.parseInt(pageStr);
            } catch (NumberFormatException var16) {
            }

            if (pagex < 1) {
               pagex = 1;
            }
         }

         int startx = this.onePage ? 0 : (pagex - 1) * 9;

         int end;
         for (end = 0; end < lines.size(); end++) {
            String line = lines.get(end);
            if (line.startsWith("#")) {
               break;
            }
         }

         int pagesx = end / 9 + (end % 9 > 0 ? 1 : 0);
         if (pagex > pagesx) {
            sender.sendTl("infoUnknownChapter");
         } else {
            if (!this.onePage && commandName != null) {
               StringBuilder content = new StringBuilder();
               String[] title = commandName.split(" ", 2);
               if (title.length > 1) {
                  content.append(I18n.capitalCase(title[0])).append(": ");
                  content.append(title[1]);
               } else {
                  content.append(I18n.capitalCase(commandName));
               }

               sender.sendTl("infoPages", pagex, pagesx, content);
            }

            for (int i = startx; i < end && i < startx + (this.onePage ? 20 : 9); i++) {
               sender.sendMessage("§r" + lines.get(i));
            }

            if (!this.onePage && pagex < pagesx && commandName != null) {
               sender.sendTl("readNextPage", commandName, pagex + 1);
            }
         }
      }
   }
}
