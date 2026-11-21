package su.nightexpress.nightcore.util.text.tag.decorator;

import java.awt.Color;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

@Deprecated
public class BaseColorDecorator implements ColorDecorator {
   private final Color color;

   public BaseColorDecorator(@NonNull Color color) {
      this.color = color;
   }

   @NonNull
   @Override
   public NightComponent decorate(@NonNull NightComponent component) {
      return component.color(this.color);
   }
}
