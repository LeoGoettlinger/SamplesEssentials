package com.oheers.fish.api.config.serializer;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemSerializer implements EMFSerializer<ItemStack> {
   private static final ItemSerializer INSTANCE = new ItemSerializer();

   private ItemSerializer() {
   }

   @NonNull
   public static ItemSerializer get() {
      return INSTANCE;
   }

   @NonNull
   public String serialize(@NonNull ItemStack element) {
      return this.serialize(element, true);
   }

   @NonNull
   public String serialize(@NonNull ItemStack element, boolean useItemAddons) {
      if (useItemAddons) {
         for (ItemAddon addon : EMFRegistry.ITEM_ADDON.getRegistry().values()) {
            String string = addon.convertToString(element);
            if (string != null) {
               return string;
            }
         }
      }

      return element.getType().toString();
   }

   @Nullable
   public ItemStack deserialize(@Nullable String element) {
      return this.deserialize(element, true);
   }

   @Nullable
   public ItemStack deserialize(@Nullable String element, boolean useItemAddons) {
      if (element == null) {
         return null;
      } else {
         return useItemAddons && element.contains(":") ? this.deserializeItemAddon(element) : this.deserializeMaterial(element);
      }
   }

   @Nullable
   public ItemStack deserializeMaterial(@Nullable String element) {
      if (element == null) {
         return null;
      } else {
         try {
            Material material = Material.valueOf(element.toUpperCase(Locale.ROOT));
            return new ItemStack(material);
         } catch (IllegalArgumentException var3) {
            Logging.debug(element + " is not a valid material.");
            return null;
         }
      }
   }

   @Nullable
   public ItemStack deserializeItemAddon(@Nullable String element) {
      if (element == null) {
         return null;
      } else {
         try {
            String[] split = element.split(":", 2);
            String prefix = split[0];
            String id = split[1];
            Logging.debug("GET ITEM for Addon(%s) Id(%s)".formatted(prefix, id));
            return EMFRegistry.ITEM_ADDON.getItem(prefix, id);
         } catch (ArrayIndexOutOfBoundsException var5) {
            return null;
         }
      }
   }
}
