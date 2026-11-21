package su.nightexpress.nightcore.ui.inventory.item;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bukkit.NightItem;

@FunctionalInterface
public interface DisplayModifier {
   void modify(@NonNull ViewerContext var1, @NonNull NightItem var2);
}
