package su.nightexpress.nightcore.ui.inventory.display;

import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.ui.inventory.item.DisplayModifier;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public record CompositeDisplayModifier(@NonNull List<DisplayModifier> modifiers) implements DisplayModifier {
   @Override
   public void modify(@NonNull ViewerContext context, @NonNull NightItem item) {
      this.modifiers.forEach(modifier -> modifier.modify(context, item));
   }
}
