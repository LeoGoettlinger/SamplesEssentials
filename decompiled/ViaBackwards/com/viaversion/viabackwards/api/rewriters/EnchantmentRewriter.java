package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.utils.ChatUtil;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.util.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EnchantmentRewriter {
   public static final String ENCHANTMENT_LEVEL_TRANSLATION = "enchantment.level.%s";
   protected final Map<String, String> enchantmentMappings = new HashMap<>();
   protected final BackwardsItemRewriter<?, ?, ?> itemRewriter;
   private final boolean jsonFormat;

   public EnchantmentRewriter(BackwardsItemRewriter<?, ?, ?> itemRewriter, boolean jsonFormat) {
      this.itemRewriter = itemRewriter;
      this.jsonFormat = jsonFormat;
   }

   public EnchantmentRewriter(BackwardsItemRewriter<?, ?, ?> itemRewriter) {
      this(itemRewriter, true);
   }

   public void registerEnchantment(String key, String replacementLore) {
      this.enchantmentMappings.put(Key.stripMinecraftNamespace(key), replacementLore);
   }

   public void handleToClient(Item item) {
      CompoundTag tag = item.tag();
      if (tag != null) {
         if (tag.getListTag("Enchantments") != null) {
            this.rewriteEnchantmentsToClient(tag, false);
         }

         if (tag.getListTag("StoredEnchantments") != null) {
            this.rewriteEnchantmentsToClient(tag, true);
         }
      }
   }

   public void handleToServer(Item item) {
      CompoundTag tag = item.tag();
      if (tag != null) {
         if (tag.contains(this.itemRewriter.nbtTagName("Enchantments"))) {
            this.rewriteEnchantmentsToServer(tag, false);
         }

         if (tag.contains(this.itemRewriter.nbtTagName("StoredEnchantments"))) {
            this.rewriteEnchantmentsToServer(tag, true);
         }
      }
   }

   public void rewriteEnchantmentsToClient(CompoundTag tag, boolean storedEnchant) {
      String key = storedEnchant ? "StoredEnchantments" : "Enchantments";
      ListTag<CompoundTag> enchantments = tag.getListTag(key, CompoundTag.class);
      List<StringTag> loreToAdd = new ArrayList<>();
      boolean changed = false;
      Iterator<CompoundTag> iterator = enchantments.iterator();

      while (iterator.hasNext()) {
         CompoundTag enchantmentEntry = iterator.next();
         StringTag idTag = enchantmentEntry.getStringTag("id");
         if (idTag != null) {
            String enchantmentId = Key.stripMinecraftNamespace(idTag.getValue());
            String remappedName = this.enchantmentMappings.get(enchantmentId);
            if (remappedName != null) {
               if (!changed) {
                  this.itemRewriter.saveListTag(tag, enchantments, key);
                  changed = true;
               }

               iterator.remove();
               NumberTag levelTag = enchantmentEntry.getNumberTag("lvl");
               int level = levelTag != null ? levelTag.asInt() : 1;
               String loreValue;
               if (this.jsonFormat) {
                  loreValue = ChatUtil.legacyToJsonString(remappedName, "enchantment.level.%s".formatted(level), true);
               } else {
                  loreValue = remappedName + " " + getRomanNumber(level);
               }

               loreToAdd.add(new StringTag(loreValue));
            }
         }
      }

      if (!loreToAdd.isEmpty()) {
         if (!storedEnchant && enchantments.isEmpty()) {
            CompoundTag dummyEnchantment = new CompoundTag();
            dummyEnchantment.putString("id", "");
            dummyEnchantment.putShort("lvl", (short)0);
            enchantments.add(dummyEnchantment);
         }

         CompoundTag display = tag.getCompoundTag("display");
         if (display == null) {
            tag.put("display", display = new CompoundTag());
         }

         ListTag<StringTag> loreTag = display.getListTag("Lore", StringTag.class);
         if (loreTag == null) {
            display.put("Lore", loreTag = new ListTag(StringTag.class));
         } else {
            this.itemRewriter.saveListTag(display, loreTag, "Lore");
         }

         loreToAdd.addAll(loreTag.getValue());
         loreTag.setValue(loreToAdd);
      }
   }

   public void rewriteEnchantmentsToServer(CompoundTag tag, boolean storedEnchant) {
      String key = storedEnchant ? "StoredEnchantments" : "Enchantments";
      this.itemRewriter.restoreListTag(tag, key);
   }

   public static String getRomanNumber(int number) {
      return switch (number) {
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         case 4 -> "IV";
         case 5 -> "V";
         case 6 -> "VI";
         case 7 -> "VII";
         case 8 -> "VIII";
         case 9 -> "IX";
         case 10 -> "X";
         default -> "enchantment.level.%s".formatted(number);
      };
   }
}
