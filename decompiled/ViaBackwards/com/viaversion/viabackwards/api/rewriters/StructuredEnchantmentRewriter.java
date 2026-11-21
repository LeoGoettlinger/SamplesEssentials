package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.utils.ChatUtil;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap.Entry;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;
import com.viaversion.viaversion.rewriter.IdRewriteFunction;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StructuredEnchantmentRewriter {
   protected final BackwardsStructuredItemRewriter<?, ?, ?> itemRewriter;
   private boolean rewriteIds = true;

   public StructuredEnchantmentRewriter(BackwardsStructuredItemRewriter<?, ?, ?> itemRewriter) {
      this.itemRewriter = itemRewriter;
   }

   public void handleToClient(Item item) {
      StructuredDataContainer data = item.dataContainer();
      BackwardsMappingData mappingData = ((BackwardsProtocol)this.itemRewriter.protocol()).getMappingData();
      IdRewriteFunction idRewriteFunction = id -> {
         Mappings mappings = mappingData.getEnchantmentMappings();
         return mappings.getNewId(id);
      };
      StructuredEnchantmentRewriter.DescriptionSupplier descriptionSupplier = (id, level) -> {
         String remappedName = mappingData.mappedEnchantmentName(id);
         return ComponentUtil.jsonStringToTag(ChatUtil.legacyToJsonString("§7" + remappedName, "enchantment.level.%s".formatted(level), true));
      };
      this.rewriteEnchantmentsToClient(data, StructuredDataKey.ENCHANTMENTS1_20_5, idRewriteFunction, descriptionSupplier, false);
      this.rewriteEnchantmentsToClient(data, StructuredDataKey.STORED_ENCHANTMENTS1_20_5, idRewriteFunction, descriptionSupplier, true);
   }

   public void handleToServer(Item item) {
      StructuredDataContainer data = item.dataContainer();
      CompoundTag customData = (CompoundTag)data.get(StructuredDataKey.CUSTOM_DATA);
      if (customData != null) {
         this.rewriteEnchantmentsToServer(data, customData, StructuredDataKey.ENCHANTMENTS1_20_5);
         this.rewriteEnchantmentsToServer(data, customData, StructuredDataKey.STORED_ENCHANTMENTS1_20_5);
      }
   }

   public void rewriteEnchantmentsToClient(
      StructuredDataContainer data,
      StructuredDataKey<Enchantments> key,
      IdRewriteFunction rewriteFunction,
      StructuredEnchantmentRewriter.DescriptionSupplier descriptionSupplier,
      boolean storedEnchant
   ) {
      Enchantments enchantments = (Enchantments)data.get(key);
      if (enchantments != null && enchantments.size() != 0) {
         List<Tag> loreToAdd = new ArrayList<>();
         boolean removedEnchantments = false;
         boolean updatedLore = false;
         ObjectIterator<Entry> iterator = enchantments.enchantments().int2IntEntrySet().iterator();
         List<StructuredEnchantmentRewriter.PendingIdChange> updatedIds = new ArrayList<>();

         while (iterator.hasNext()) {
            Entry entry = (Entry)iterator.next();
            int id = entry.getIntKey();
            int mappedId = rewriteFunction.rewrite(id);
            int level = entry.getIntValue();
            if (mappedId != -1) {
               if (this.rewriteIds) {
                  updatedIds.add(new StructuredEnchantmentRewriter.PendingIdChange(id, mappedId, level));
               }
            } else {
               if (!removedEnchantments) {
                  CompoundTag customData = this.customData(data);
                  this.itemRewriter.saveListTag(customData, this.asTag(enchantments), key.identifier());
                  removedEnchantments = true;
               }

               Tag description = descriptionSupplier.get(id, level);
               if (description != null && enchantments.showInTooltip()) {
                  loreToAdd.add(description);
                  updatedLore = true;
               }

               iterator.remove();
            }
         }

         for (StructuredEnchantmentRewriter.PendingIdChange change : updatedIds) {
            enchantments.remove(change.id());
         }

         for (StructuredEnchantmentRewriter.PendingIdChange change : updatedIds) {
            enchantments.add(change.mappedId(), change.level());
         }

         if (removedEnchantments) {
            CompoundTag tag = this.customData(data);
            if (!storedEnchant && enchantments.size() == 0) {
               Boolean glintOverride = (Boolean)data.get(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE);
               if (glintOverride != null) {
                  tag.putBoolean(this.itemRewriter.nbtTagName("glint"), glintOverride);
               } else {
                  tag.putBoolean(this.itemRewriter.nbtTagName("noglint"), true);
               }

               data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, true);
            }

            if (enchantments.showInTooltip()) {
               tag.putBoolean(this.itemRewriter.nbtTagName("show_" + key.identifier()), true);
            }
         }

         if (updatedLore) {
            CompoundTag tagx = this.customData(data);
            Tag[] lore = (Tag[])data.get(StructuredDataKey.LORE);
            if (lore != null) {
               List<Tag> loreList = Arrays.asList(lore);
               this.itemRewriter.saveGenericTagList(tagx, loreList, "lore");
               loreToAdd.addAll(loreList);
            } else {
               tagx.putBoolean(this.itemRewriter.nbtTagName("nolore"), true);
            }

            data.set(StructuredDataKey.LORE, loreToAdd.toArray(new Tag[0]));
         }
      }
   }

   private CompoundTag customData(StructuredDataContainer data) {
      CompoundTag tag = (CompoundTag)data.get(StructuredDataKey.CUSTOM_DATA);
      if (tag == null) {
         tag = new CompoundTag();
         data.set(StructuredDataKey.CUSTOM_DATA, tag);
      }

      return tag;
   }

   private ListTag<CompoundTag> asTag(Enchantments enchantments) {
      ListTag<CompoundTag> listTag = new ListTag(CompoundTag.class);
      ObjectIterator var3 = enchantments.enchantments().int2IntEntrySet().iterator();

      while (var3.hasNext()) {
         Entry entry = (Entry)var3.next();
         CompoundTag enchantment = new CompoundTag();
         enchantment.putInt("id", entry.getIntKey());
         enchantment.putInt("lvl", entry.getIntValue());
         listTag.add(enchantment);
      }

      return listTag;
   }

   public void rewriteEnchantmentsToServer(StructuredDataContainer data, CompoundTag tag, StructuredDataKey<Enchantments> key) {
      ListTag<CompoundTag> enchantmentsTag = this.itemRewriter.removeListTag(tag, key.identifier(), CompoundTag.class);
      if (enchantmentsTag != null) {
         Tag glintTag = tag.remove(this.itemRewriter.nbtTagName("glint"));
         if (glintTag instanceof ByteTag) {
            data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, ((NumberTag)glintTag).asBoolean());
         } else if (tag.remove(this.itemRewriter.nbtTagName("noglint")) != null) {
            data.remove(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE);
         }

         List<Tag> lore = this.itemRewriter.removeGenericTagList(tag, "lore");
         if (lore != null) {
            data.set(StructuredDataKey.LORE, lore.toArray(new Tag[0]));
         } else if (tag.remove(this.itemRewriter.nbtTagName("nolore")) != null) {
            data.remove(StructuredDataKey.LORE);
         }

         Enchantments enchantments = new Enchantments(tag.remove(this.itemRewriter.nbtTagName("show_" + key.identifier())) != null);

         for (CompoundTag enchantment : enchantmentsTag) {
            enchantments.add(enchantment.getInt("id"), enchantment.getInt("lvl"));
         }

         data.set(key, enchantments);
      }
   }

   public void setRewriteIds(boolean rewriteIds) {
      this.rewriteIds = rewriteIds;
   }

   @FunctionalInterface
   public interface DescriptionSupplier {
      Tag get(int var1, int var2);
   }

   private record PendingIdChange(int id, int mappedId, int level) {
   }
}
