package su.nightexpress.nightcore.ui.inventory.action;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface MenuItemAction {
   void execute(@NonNull ActionContext var1);
}
