package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.Strings;
import com.google.errorprone.annotations.Immutable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

@Immutable
public enum CommentHandlers implements CommentHandler {
   HASH(new CommentHandlers.AbstractPrefixHandler("#")),
   DOUBLE_SLASH(new CommentHandlers.AbstractPrefixHandler("//")),
   SLASH_BLOCK(new CommentHandlers.AbstractDelineatedHandler("/*", "*/", "*")),
   XML_STYLE(new CommentHandlers.AbstractDelineatedHandler("<!--", "-->", "~"));

   private static final int READAHEAD_LEN = 4096;
   private final CommentHandler delegate;

   private CommentHandlers(final CommentHandler delegate) {
      this.delegate = delegate;
   }

   @Nullable
   @Override
   public String extractHeader(final BufferedReader reader) throws IOException {
      return this.delegate.extractHeader(reader);
   }

   @Override
   public Stream<String> toComment(final Stream<String> lines) {
      return this.delegate.toComment(lines);
   }

   public static String extractComment(final BufferedReader reader, final Iterable<CommentHandler> allowedHeaderTypes) throws IOException {
      reader.mark(4096);

      for (CommentHandler handler : allowedHeaderTypes) {
         String comment = handler.extractHeader(reader);
         if (comment != null) {
            return comment;
         }

         reader.reset();
      }

      return null;
   }

   static boolean beginsWithPrefix(final String commentPrefix, final BufferedReader reader) throws IOException {
      CharBuffer buf = CharBuffer.allocate(commentPrefix.length());
      if (reader.read(buf) != buf.limit()) {
         return false;
      } else {
         ((Buffer)buf).flip();
         return commentPrefix.contentEquals(buf);
      }
   }

   @Immutable
   private static final class AbstractDelineatedHandler implements CommentHandler {
      private final String startSequence;
      private final String endSequence;
      private final String lineIndentSequence;

      private AbstractDelineatedHandler(final String startSequence, final String endSequence, final String lineIndentSequence) {
         this.startSequence = startSequence;
         this.endSequence = endSequence;
         this.lineIndentSequence = lineIndentSequence;
      }

      @Nullable
      @Override
      public String extractHeader(final BufferedReader reader) throws IOException {
         if (!CommentHandlers.beginsWithPrefix(this.startSequence, reader)) {
            return null;
         } else {
            StringBuilder build = new StringBuilder();
            String line = reader.readLine();
            if (line == null) {
               return null;
            } else {
               if (this.handleSingleLine(build, line)) {
                  line = reader.readLine();

                  while (line != null && this.handleSingleLine(build, line)) {
                     line = reader.readLine();
                  }
               }

               line = reader.readLine();
               if (line != null && !Strings.isBlank(line)) {
                  return null;
               } else {
                  return build.length() > 0 ? build.toString() : null;
               }
            }
         }
      }

      private boolean handleSingleLine(final StringBuilder builder, String line) {
         boolean moreLines = true;
         if (line.trim().endsWith(this.endSequence)) {
            line = line.substring(0, line.lastIndexOf(this.endSequence));
            if (line.endsWith(" ")) {
               line = line.substring(0, line.length() - 1);
            }

            moreLines = false;
            if (line.isEmpty()) {
               return false;
            }
         }

         if (line.trim().startsWith(this.lineIndentSequence)) {
            line = line.substring(line.indexOf(this.lineIndentSequence) + 1);
         }

         if (line.length() > 0 && line.charAt(0) == ' ') {
            line = line.substring(1);
         }

         if (builder.length() > 0) {
            builder.append("\n");
         }

         builder.append(line.replace("\r", "").replace("\n", "").replace("\r\n", ""));
         return moreLines;
      }

      @Override
      public Stream<String> toComment(final Stream<String> lines) {
         Builder<String> build = Stream.builder();
         boolean first = true;
         Iterator<String> it = lines.iterator();

         while (it.hasNext()) {
            String next = it.next();
            if (first) {
               if (!it.hasNext()) {
                  build.add(this.startSequence + " " + next + " " + this.endSequence);
                  return build.build();
               }

               build.add(this.startSequence);
               first = false;
            }

            build.add(" " + this.lineIndentSequence + " " + next);
         }

         build.add(" " + this.endSequence);
         return build.build();
      }
   }

   @Immutable
   private static final class AbstractPrefixHandler implements CommentHandler {
      private final String commentPrefix;

      AbstractPrefixHandler(final String commentPrefix) {
         this.commentPrefix = commentPrefix;
      }

      @Nullable
      @Override
      public String extractHeader(final BufferedReader reader) throws IOException {
         if (!CommentHandlers.beginsWithPrefix(this.commentPrefix, reader)) {
            return null;
         } else {
            boolean firstLine = true;
            StringBuilder build = new StringBuilder();

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
               if (firstLine) {
                  if (line.length() > 0 && line.charAt(0) == ' ') {
                     line = line.substring(1);
                  }

                  build.append(line);
                  firstLine = false;
               } else {
                  if (!line.trim().startsWith(this.commentPrefix)) {
                     if (!Strings.isBlank(line)) {
                        return null;
                     }
                     break;
                  }

                  line = line.substring(line.indexOf(this.commentPrefix) + 1);
                  if (line.length() > 0 && line.charAt(0) == ' ') {
                     line = line.substring(1);
                  }

                  if (build.length() > 0) {
                     build.append("\n");
                  }

                  build.append(line);
               }
            }

            return build.length() > 0 ? build.toString() : null;
         }
      }

      @Override
      public Stream<String> toComment(final Stream<String> lines) {
         return lines.map(s -> s.length() > 0 && s.charAt(0) == ' ' ? this.commentPrefix + s : this.commentPrefix + " " + s);
      }
   }
}
