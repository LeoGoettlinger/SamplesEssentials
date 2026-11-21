package org.sethomegui.SetHomeGUI.libs.mysql.cj.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EscapeTokenizer {
   private static final char CHR_BACKSLASH = '\\';
   private static final char CHR_SLASH = '/';
   private static final char CHR_SGL_QUOTE = '\'';
   private static final char CHR_DBL_QUOTE = '"';
   private static final char CHR_LF = '\n';
   private static final char CHR_CR = '\r';
   private static final char CHR_DASH = '-';
   private static final char CHR_HASH = '#';
   private static final char CHR_STAR = '*';
   private static final char CHR_BEGIN_TOKEN = '{';
   private static final char CHR_END_TOKEN = '}';
   private static final char CHR_VARIABLE = '@';
   private static final char CHR_SPACE = ' ';
   private String source = null;
   private int sourceLength = 0;
   private int pos = 0;
   private boolean emittingEscapeCode = false;
   private boolean sawVariableUse = false;
   private int bracesLevel = 0;
   private boolean inQuotes = false;
   private char quoteChar = 0;
   private final Lock lock = new ReentrantLock();

   public EscapeTokenizer(String source) {
      this.source = source;
      this.sourceLength = source.length();
      this.pos = 0;
   }

   public boolean hasMoreTokens() {
      this.lock.lock();

      boolean var1;
      try {
         var1 = this.pos < this.sourceLength;
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   public String nextToken() {
      this.lock.lock();

      try {
         StringBuilder tokenBuf = new StringBuilder();
         boolean backslashEscape = false;
         if (this.emittingEscapeCode) {
            tokenBuf.append("{");
            this.emittingEscapeCode = false;
         }

         label250:
         for (; this.pos < this.sourceLength; this.pos++) {
            char c = this.source.charAt(this.pos);
            if (c == '\\') {
               tokenBuf.append(c);
               backslashEscape = !backslashEscape;
            } else if ((c == '\'' || c == '"') && !backslashEscape) {
               tokenBuf.append(c);
               if (this.inQuotes) {
                  if (c == this.quoteChar) {
                     if (this.pos + 1 < this.sourceLength && this.source.charAt(this.pos + 1) == this.quoteChar) {
                        tokenBuf.append(c);
                        this.pos++;
                     } else {
                        this.inQuotes = false;
                     }
                  }
               } else {
                  this.inQuotes = true;
                  this.quoteChar = c;
               }
            } else if (c != '\n' && c != '\r') {
               if (!this.inQuotes && !backslashEscape) {
                  if (c == '/') {
                     tokenBuf.append(c);
                     if (this.pos + 1 < this.sourceLength && this.source.charAt(this.pos + 1) == '*') {
                        while (++this.pos < this.sourceLength - 1) {
                           c = this.source.charAt(this.pos);
                           tokenBuf.append(c);
                           if (c == '*' && this.source.charAt(this.pos + 1) == '/') {
                              tokenBuf.append('/');
                              this.pos++;
                              continue label250;
                           }
                        }
                     }
                     continue;
                  }

                  if (c == '#') {
                     tokenBuf.append(c);

                     while (++this.pos < this.sourceLength && c != '\n' && c != '\r') {
                        c = this.source.charAt(this.pos);
                        tokenBuf.append(c);
                     }

                     this.pos--;
                     continue;
                  }

                  if (c == '-') {
                     tokenBuf.append(c);
                     if (this.pos + 2 < this.sourceLength && this.source.charAt(this.pos + 1) == '-' && this.source.charAt(this.pos + 2) == ' ') {
                        while (++this.pos < this.sourceLength && c != '\n' && c != '\r') {
                           c = this.source.charAt(this.pos);
                           tokenBuf.append(c);
                        }

                        this.pos--;
                     }
                     continue;
                  }

                  if (c == '{') {
                     this.bracesLevel++;
                     if (this.bracesLevel == 1) {
                        this.emittingEscapeCode = true;
                        this.pos++;
                        return tokenBuf.toString();
                     }

                     tokenBuf.append(c);
                     continue;
                  }

                  if (c == '}') {
                     tokenBuf.append(c);
                     this.bracesLevel--;
                     if (this.bracesLevel == 0) {
                        this.pos++;
                        return tokenBuf.toString();
                     }
                     continue;
                  }

                  if (c == '@') {
                     this.sawVariableUse = true;
                  }
               }

               tokenBuf.append(c);
               backslashEscape = false;
            } else {
               tokenBuf.append(c);
               backslashEscape = false;
            }
         }

         return tokenBuf.toString();
      } finally {
         this.lock.unlock();
      }
   }

   public boolean sawVariableUse() {
      return this.sawVariableUse;
   }
}
