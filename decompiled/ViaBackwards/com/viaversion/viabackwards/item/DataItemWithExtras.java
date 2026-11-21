package com.viaversion.viabackwards.item;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DataItemWithExtras extends DataItem {
   private JsonElement name;
   private List<JsonElement> lore;

   public DataItemWithExtras(Item from) {
      this.setIdentifier(from.identifier());
      this.setAmount(from.amount());
      this.setData(from.data());
      this.setTag(from.tag());
      if (this.tag() != null) {
         CompoundTag display = this.tag().getCompoundTag("display");
         if (display != null) {
            StringTag name = display.getStringTag("Name");
            if (name != null) {
               this.name = this.parse(name.getValue());
            }

            ListTag<StringTag> lore = display.getListTag("Lore", StringTag.class);
            if (lore != null) {
               this.lore = new ArrayList<>(lore.size());

               for (int i = 0; i < lore.size(); i++) {
                  this.lore.add(this.parse(((StringTag)lore.get(i)).getValue()));
               }
            }
         }
      }
   }

   @Nullable
   public JsonElement name() {
      return this.name;
   }

   @Nullable
   public StringTag rawName() {
      if (this.tag() == null) {
         return null;
      } else {
         CompoundTag display = this.tag().getCompoundTag("display");
         return display != null ? display.getStringTag("Name") : null;
      }
   }

   @Nullable
   public List<JsonElement> lore() {
      return this.lore;
   }

   @Nullable
   public ListTag<StringTag> rawLore() {
      if (this.tag() == null) {
         return null;
      } else {
         CompoundTag display = this.tag().getCompoundTag("display");
         return display != null ? display.getListTag("Lore", StringTag.class) : null;
      }
   }

   private JsonElement parse(String value) {
      try {
         return JsonParser.parseString(value);
      } catch (JsonSyntaxException var3) {
         return new JsonPrimitive(value);
      }
   }
}
