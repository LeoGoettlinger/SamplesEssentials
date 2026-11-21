package com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import java.util.List;

public final class RegistryDataRewriter1_21_5 extends BackwardsRegistryRewriter {
   public RegistryDataRewriter1_21_5(BackwardsProtocol<?, ?, ?, ?> protocol) {
      super(protocol);

      for (String registry : List.of(
         "pig_variant", "cow_variant", "frog_variant", "cat_variant", "chicken_variant", "test_environment", "test_instance", "wolf_sound_variant"
      )) {
         this.remove(registry);
      }
   }

   @Override
   public RegistryEntry[] handle(UserConnection connection, String key, RegistryEntry[] entries) {
      boolean trimPatternRegistry = key.equals("trim_pattern");
      if (trimPatternRegistry || key.equals("trim_material")) {
         this.updateTrim(entries, trimPatternRegistry ? "template_item" : "ingredient");
      } else if (key.equals("enchantment")) {
         this.updateEnchantment(entries);
      } else if (key.equals("wolf_variant")) {
         for (RegistryEntry entry : entries) {
            if (entry.tag() != null) {
               CompoundTag variant = (CompoundTag)entry.tag();
               CompoundTag assets = (CompoundTag)variant.remove("assets");
               variant.put("wild_texture", assets.get("wild"));
               variant.put("tame_texture", assets.get("tame"));
               variant.put("angry_texture", assets.get("angry"));
               variant.put("biomes", new ListTag(StringTag.class));
            }
         }
      }

      return super.handle(connection, key, entries);
   }

   private void updateTrim(RegistryEntry[] entries, String itemKey) {
      for (RegistryEntry entry : entries) {
         if (entry.tag() != null) {
            CompoundTag tag = (CompoundTag)entry.tag();
            tag.putString(itemKey, "stone");
         }
      }
   }

   private void updateEnchantment(RegistryEntry[] entries) {
      for (RegistryEntry entry : entries) {
         if (entry.tag() != null) {
            CompoundTag enchantment = (CompoundTag)entry.tag();
            ListTag<StringTag> slots = enchantment.getListTag("slots", StringTag.class);
            if (slots != null) {
               slots.getValue().removeIf(tag -> tag.getValue().equals("saddle"));
            }
         }
      }
   }
}
