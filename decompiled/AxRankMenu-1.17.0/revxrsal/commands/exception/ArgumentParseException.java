package revxrsal.commands.exception;

import revxrsal.commands.util.Strings;

public class ArgumentParseException extends RuntimeException {
   private static final long serialVersionUID = -8555316116315990226L;
   private final String source;
   private final int position;

   public ArgumentParseException(String message, String source, int position) {
      super(message);
      this.source = source;
      this.position = position;
   }

   public ArgumentParseException(String message, Throwable cause, String source, int position) {
      super(message, cause);
      this.source = source;
      this.position = position;
   }

   public String getAnnotatedPosition() {
      String source = this.source;
      int position = this.position;
      if (source.length() > 80 && position >= 37) {
         int startPos = position - 37;
         int endPos = Math.min(source.length(), position + 37);
         position -= 40;
      }

      return Strings.repeat(" ", position) + "^";
   }

   public int getPosition() {
      return this.position;
   }

   public String getSourceString() {
      return this.source;
   }
}
