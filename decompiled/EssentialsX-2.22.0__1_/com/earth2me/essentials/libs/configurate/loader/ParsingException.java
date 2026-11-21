package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import java.io.IOException;
import java.util.Arrays;

public class ParsingException extends ConfigurateException {
   public static final int UNKNOWN_POS = -1;
   private static final char POSITION_MARKER = '^';
   private static final long serialVersionUID = 8379206111053602577L;
   private final int line;
   private final int column;
   @Nullable
   private final String context;

   public static ParsingException wrap(final ConfigurationNode source, final IOException ex) {
      return ex instanceof ParsingException ? (ParsingException)ex : new ParsingException(source, -1, -1, null, null, ex);
   }

   public ParsingException(final ConfigurationNode position, final int line, final int column, final String context, @Nullable final String message) {
      this(position, line, column, context, message, null);
   }

   public ParsingException(final int line, final int column, @Nullable final String context, @Nullable final String message, @Nullable final Throwable cause) {
      super(message, cause);
      this.line = line;
      this.column = column;
      this.context = context;
   }

   public ParsingException(
      final ConfigurationNode position,
      final int line,
      final int column,
      @Nullable final String context,
      @Nullable final String message,
      @Nullable final Throwable cause
   ) {
      super(position, message, cause);
      this.line = line;
      this.column = column;
      this.context = context;
   }

   public int line() {
      return this.line;
   }

   public int column() {
      return this.column;
   }

   @Nullable
   public String context() {
      return this.context;
   }

   @Override
   public String getMessage() {
      if (this.line != -1 && this.column != -1) {
         String rawMessage = this.rawMessage();
         StringBuilder message = new StringBuilder(rawMessage == null ? 0 : rawMessage.length() + 20);
         message.append(this.path()).append("@(line ").append(this.line).append(", col ").append(this.column).append("): ").append(rawMessage);
         if (this.context != null) {
            message.append(System.lineSeparator()).append(this.context);
            if (this.column >= 0 && this.column < this.context.length()) {
               message.append(System.lineSeparator());
               if (this.column > 0) {
                  char[] spaces = new char[this.column - 1];
                  Arrays.fill(spaces, ' ');
                  message.append(spaces);
               }

               message.append('^');
            }
         }

         return message.toString();
      } else {
         return super.getMessage();
      }
   }
}
