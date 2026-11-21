package su.nightexpress.nightcore.core.tag;

import java.awt.Color;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.text.night.tag.handler.NamedColorTagHandler;

public record ColorCode(@NonNull String name, @NonNull Color color) {
   @NonNull
   public NamedColorTagHandler createHandler() {
      return new NamedColorTagHandler(this.name, this.color);
   }

   @Override
   public String toString() {
      return "ColorCode{name='" + this.name + "', color=" + this.color + "}";
   }
}
