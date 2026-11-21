package su.nightexpress.nightcore.integration.item.adapter.impl;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

public class MMOItemsAdapter extends IdentifiableItemAdapter {
   public MMOItemsAdapter() {
      super("mmoitems");
   }

   @Override
   public boolean canHandle(@NonNull ItemStack itemStack) {
      return MMOItems.getType(itemStack) != null && MMOItems.getID(itemStack) != null;
   }

   @Nullable
   @Override
   public ItemStack createItem(@NonNull String itemId) {
      String[] split = itemId.split(":");
      Type type = Type.get(split[0]);
      if (type != null && split.length >= 2) {
         String id = split[1];
         MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
         return template != null ? template.newBuilder().build().newBuilder().buildSilently() : null;
      } else {
         return null;
      }
   }

   public boolean canHandle(@NonNull ItemIdData data) {
      String[] split = data.getItemId().split(":");
      Type type = Type.get(split[0]);
      if (type != null && split.length >= 2) {
         String id = split[1];
         MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
         return template != null;
      } else {
         return false;
      }
   }

   @Nullable
   @Override
   public String getItemId(@NonNull ItemStack itemStack) {
      String type = MMOItems.getTypeName(itemStack);
      String id = MMOItems.getID(itemStack);
      return type != null && id != null ? type + ":" + id : null;
   }
}
