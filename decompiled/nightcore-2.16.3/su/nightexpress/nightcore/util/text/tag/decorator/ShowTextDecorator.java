package su.nightexpress.nightcore.util.text.tag.decorator;

import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.text.NightAbstractComponent;
import su.nightexpress.nightcore.bridge.text.event.NightHoverEvent;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Placeholders;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.NightMessage;

@Deprecated
public class ShowTextDecorator implements Decorator {
   private final List<String> text;

   public ShowTextDecorator(String... strings) {
      this(Lists.newList(strings));
   }

   public ShowTextDecorator(@NonNull List<String> text) {
      this.text = text;
   }

   @NonNull
   @Override
   public NightComponent decorate(@NonNull NightComponent component) {
      String oneLined = String.join(Placeholders.TAG_LINE_BREAK, this.text);
      NightComponent nightComponent = NightMessage.parse(oneLined);
      return component.hoverEvent(NightHoverEvent.showText((NightAbstractComponent)nightComponent));
   }
}
