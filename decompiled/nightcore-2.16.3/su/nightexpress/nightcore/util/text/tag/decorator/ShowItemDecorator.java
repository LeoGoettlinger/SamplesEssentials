package su.nightexpress.nightcore.util.text.tag.decorator;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.text.event.NightHoverEvent;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.ItemNbt;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

@Deprecated
public class ShowItemDecorator implements Decorator {
   private final String itemData;

   public static ShowItemDecorator from(@NonNull ItemStack item) {
      String content = ItemNbt.compress(item);
      return new ShowItemDecorator(content == null ? BukkitThing.toString(item.getType()) : content);
   }

   public ShowItemDecorator(@NonNull String string) {
      this.itemData = string;
   }

   @NonNull
   @Override
   public NightComponent decorate(@NonNull NightComponent component) {
      ItemStack itemStack = ItemNbt.getHoverEventItem(this.itemData);
      return itemStack == null ? component : component.hoverEvent(NightHoverEvent.showItem(itemStack));
   }
}
