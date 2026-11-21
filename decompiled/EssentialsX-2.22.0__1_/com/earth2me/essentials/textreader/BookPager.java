package com.earth2me.essentials.textreader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.ess3.api.TranslatableException;

public class BookPager {
   final double pageMax = 254.0;
   final double charMax = 18.5;
   final int lineMax = 12;
   private final transient IText text;

   public BookPager(final IText text) {
      this.text = text;
   }

   public List<String> getPages(final String pageStr) throws Exception {
      List<String> lines = this.text.getLines();
      List<String> pageLines = new ArrayList<>();
      Map<String, Integer> bookmarks = this.text.getBookmarks();
      if (!bookmarks.containsKey(pageStr.toLowerCase(Locale.ENGLISH))) {
         throw new TranslatableException("infoUnknownChapter");
      } else {
         int chapterstart = bookmarks.get(pageStr.toLowerCase(Locale.ENGLISH)) + 1;

         int chapterend;
         for (chapterend = chapterstart; chapterend < lines.size(); chapterend++) {
            String line = lines.get(chapterend);
            if (line.length() > 0 && line.charAt(0) == '#') {
               break;
            }
         }

         List<String> pages = new ArrayList<>();
         double pageLength = 0.0;

         for (int lineNo = chapterstart; lineNo < chapterend; lineNo++) {
            String pageLine = lines.get(lineNo);
            int lineLength = pageLine.length();
            double length = 0.0;
            int pointer = 0;
            int start = 0;
            double weight = 1.0;
            boolean forcePageEnd = false;

            while (pointer < lineLength) {
               char letter = pageLine.charAt(pointer);
               if (pageLine.charAt(start) == ' ') {
                  start++;
                  pointer++;
               } else {
                  if (pageLength >= 254.0) {
                     length = 18.5;
                     pageLength = 0.0;
                     forcePageEnd = true;
                  }

                  if (length >= 18.5 || letter == 167 && length + 1.0 >= 18.5) {
                     int pos = pointer;

                     int rollback;
                     for (rollback = 0; pos > start && pageLine.charAt(pos) != ' ' && pageLine.charAt(pos) != "\n".charAt(0); pos--) {
                        rollback++;
                     }

                     if (pos != start) {
                        pointer = pos;
                        pageLength -= rollback;
                     }

                     String tempLine = pageLine.substring(start, pointer);
                     pageLines.add(tempLine);
                     if (this.buildPage(pages, pageLines, forcePageEnd)) {
                        pageLength = 0.0;
                     }

                     forcePageEnd = false;
                     start = pointer;
                     length = 0.0;
                     pageLength++;
                  }

                  pageLength++;
                  if (letter == 167 && pointer + 1 < lineLength) {
                     char nextLetter = pageLine.charAt(pointer + 1);
                     if (nextLetter != 'l' && nextLetter != 'L') {
                        weight = 1.0;
                     } else {
                        weight = 1.25;
                     }

                     pointer++;
                  } else if (letter == 'i' || letter == '.' || letter == ',' || letter == '!' || letter == ':' || letter == ';' || letter == '|') {
                     length += 0.34 * weight;
                  } else if (letter == 'l' || letter == '\'' || letter == '`') {
                     length += 0.53 * weight;
                  } else if (letter == ' ' || letter == 't' || letter == 'I' || letter == '[' || letter == ']') {
                     length += 0.69 * weight;
                  } else if (letter == 'f'
                     || letter == 'k'
                     || letter == '"'
                     || letter == '*'
                     || letter == '('
                     || letter == ')'
                     || letter == '{'
                     || letter == '}'
                     || letter == '<'
                     || letter == '>') {
                     length += 0.85 * weight;
                  } else if (letter != '@' && letter != '~') {
                     length += weight;
                  } else {
                     length += 1.2 * weight;
                  }

                  pointer++;
               }
            }

            if (length > 0.0) {
               String tempLine = pageLine.substring(start, lineLength);
               pageLines.add(tempLine);
               if (this.buildPage(pages, pageLines, false)) {
                  pageLength = 0.0;
               }
            }
         }

         this.buildPage(pages, pageLines, true);
         return pages;
      }
   }

   boolean buildPage(final List<String> pages, final List<String> lines, final boolean override) {
      if (!override && lines.size() <= 12) {
         return false;
      } else {
         StringBuilder newPage = new StringBuilder();

         for (String aline : lines) {
            newPage.append(aline).append("\n");
         }

         pages.add(newPage.toString());
         lines.clear();
         return true;
      }
   }
}
