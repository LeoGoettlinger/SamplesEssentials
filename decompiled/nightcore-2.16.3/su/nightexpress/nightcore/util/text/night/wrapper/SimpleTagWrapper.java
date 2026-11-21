package su.nightexpress.nightcore.util.text.night.wrapper;

import org.jspecify.annotations.NonNull;

public class SimpleTagWrapper implements TagWrapper {
   private final String tag;
   private final String[] arguments;

   public SimpleTagWrapper(@NonNull String tag, String[] arguments) {
      this.tag = tag;
      this.arguments = arguments;
   }

   @NonNull
   public String opening() {
      return "<" + this.openingNoBrackets() + ">";
   }

   @NonNull
   public String openingNoBrackets() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.tag);
      if (this.arguments.length > 0) {
         builder.append(':');
         builder.append(String.join(String.valueOf(':'), this.arguments));
      }

      return builder.toString();
   }

   @NonNull
   public String closing() {
      return '<' + "/" + this.tag + ">";
   }

   @NonNull
   @Override
   public String wrap(@NonNull String string) {
      return this.opening() + string + this.closing();
   }
}
