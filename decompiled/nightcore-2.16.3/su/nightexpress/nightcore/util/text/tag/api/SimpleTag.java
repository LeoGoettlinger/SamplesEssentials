package su.nightexpress.nightcore.util.text.tag.api;

import org.jspecify.annotations.NonNull;

@Deprecated
public class SimpleTag extends Tag {
   public SimpleTag(@NonNull String name) {
      super(name);
   }

   public SimpleTag(@NonNull String name, String[] aliases) {
      super(name, aliases);
   }

   @Deprecated
   @NonNull
   public String enclose(@NonNull String text) {
      return this.wrap(text);
   }

   @NonNull
   public String wrap(@NonNull String text) {
      return this.getBracketsName() + text + this.getClosingName();
   }
}
