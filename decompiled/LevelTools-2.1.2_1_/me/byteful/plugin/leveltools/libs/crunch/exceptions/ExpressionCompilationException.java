package me.byteful.plugin.leveltools.libs.crunch.exceptions;

import me.byteful.plugin.leveltools.libs.crunch.ExpressionParser;

public class ExpressionCompilationException extends RuntimeException {
   private final ExpressionParser parser;

   public ExpressionCompilationException(ExpressionParser parser, String message) {
      super(generateMessage(parser, message));
      this.parser = parser;
   }

   public ExpressionParser getParser() {
      return this.parser;
   }

   private static String generateMessage(ExpressionParser parser, String message) {
      return parser == null ? message : message + ":\n" + parser.getInput() + "\n" + repeat(' ', parser.getCursor()) + "^";
   }

   private static String repeat(char c, int n) {
      StringBuilder builder = new StringBuilder(n);

      for (int i = 0; i < n; i++) {
         builder.append(c);
      }

      return builder.toString();
   }
}
