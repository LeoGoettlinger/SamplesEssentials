package me.byteful.plugin.leveltools.libs.lamp.util;

import java.util.List;
import me.byteful.plugin.leveltools.libs.lamp.command.ArgumentStack;
import me.byteful.plugin.leveltools.libs.lamp.exception.ArgumentParseException;
import org.jetbrains.annotations.NotNull;

public final class QuotedStringTokenizer {
   private static final List<String> EMPTY_TEXT = java.util.Collections.singletonList("");
   private static final int CHAR_BACKSLASH = 92;
   private static final int CHAR_SINGLE_QUOTE = 39;
   private static final int CHAR_DOUBLE_QUOTE = 34;

   private QuotedStringTokenizer() {
   }

   public static ArgumentStack parse(@NotNull String arguments) throws ArgumentParseException {
      if (arguments.length() == 0) {
         return ArgumentStack.empty();
      } else {
         QuotedStringTokenizer.TokenizerState state = new QuotedStringTokenizer.TokenizerState(arguments);
         ArgumentStack returnedArgs = ArgumentStack.empty();

         while (state.hasMore()) {
            skipWhiteSpace(state);
            String arg = nextArg(state);
            returnedArgs.add(arg);
         }

         return returnedArgs;
      }
   }

   public static ArgumentStack parseForAutoCompletion(@NotNull String args) {
      return args.isEmpty() ? ArgumentStack.copyExact(EMPTY_TEXT) : parse(args);
   }

   private static void skipWhiteSpace(QuotedStringTokenizer.TokenizerState state) throws ArgumentParseException {
      if (state.hasMore()) {
         if (Character.isWhitespace(state.peek())) {
            state.next();
         }
      }
   }

   private static String nextArg(QuotedStringTokenizer.TokenizerState state) throws ArgumentParseException {
      StringBuilder argBuilder = new StringBuilder();
      if (state.hasMore()) {
         int codePoint = state.peek();
         if (codePoint != 34 && codePoint != 39) {
            parseUnquotedString(state, argBuilder);
         } else {
            parseQuotedString(state, codePoint, argBuilder);
         }
      }

      return argBuilder.toString();
   }

   private static void parseQuotedString(QuotedStringTokenizer.TokenizerState state, int startQuotation, StringBuilder builder) throws ArgumentParseException {
      int nextCodePoint = state.next();
      if (nextCodePoint != startQuotation) {
         throw state.createException(String.format("Actual next character '%c' did not match expected quotation character '%c'", nextCodePoint, startQuotation));
      } else {
         while (state.hasMore()) {
            nextCodePoint = state.peek();
            if (nextCodePoint == startQuotation) {
               state.next();
               return;
            }

            if (nextCodePoint == 92) {
               parseEscape(state, builder);
            } else {
               builder.appendCodePoint(state.next());
            }
         }
      }
   }

   private static void parseUnquotedString(QuotedStringTokenizer.TokenizerState state, StringBuilder builder) throws ArgumentParseException {
      while (state.hasMore()) {
         int nextCodePoint = state.peek();
         if (Character.isWhitespace(nextCodePoint)) {
            return;
         }

         if (nextCodePoint == 92) {
            parseEscape(state, builder);
         } else {
            builder.appendCodePoint(state.next());
         }
      }
   }

   private static void parseEscape(QuotedStringTokenizer.TokenizerState state, StringBuilder builder) throws ArgumentParseException {
      state.next();
      builder.appendCodePoint(state.next());
   }

   private static class TokenizerState {
      private final String buffer;
      private int index = -1;

      TokenizerState(String buffer) {
         this.buffer = buffer;
      }

      public boolean hasMore() {
         return this.index + 1 < this.buffer.length();
      }

      public int peek() throws ArgumentParseException {
         if (!this.hasMore()) {
            throw this.createException("Buffer overrun while parsing args");
         } else {
            return this.buffer.codePointAt(this.index + 1);
         }
      }

      public int next() throws ArgumentParseException {
         if (!this.hasMore()) {
            throw this.createException("Buffer overrun while parsing args");
         } else {
            return this.buffer.codePointAt(++this.index);
         }
      }

      public ArgumentParseException createException(String message) {
         return new ArgumentParseException(message, this.buffer, this.index);
      }
   }
}
