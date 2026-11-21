package com.viaversion.viabackwards.api.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.util.Key;

public class MappedItem {
   private final int id;
   private final String jsonName;
   private final Tag tagName;
   private final int customModelData;

   public MappedItem(int id, String identifier, String fallbackName, int customModelData) {
      this.id = id;
      identifier = Key.stripMinecraftNamespace(identifier);
      String translateKey = "vb.item." + identifier;
      this.jsonName = itemJsonName(fallbackName);
      this.tagName = itemTagName(translateKey, fallbackName);
      this.customModelData = customModelData;
   }

   private static String itemJsonName(String name) {
      return "{\"italic\":false,\"color\":\"white\",\"text\":%s}\n".formatted(new JsonPrimitive(name).toString());
   }

   private static Tag itemTagName(String translateKey, String fallbackName) {
      CompoundTag tag = new CompoundTag();
      tag.putString("color", "white");
      tag.putString("translate", translateKey);
      tag.putString("fallback", fallbackName);
      tag.putBoolean("italic", false);
      return tag;
   }

   public int id() {
      return this.id;
   }

   public String jsonName() {
      return this.jsonName;
   }

   public Tag tagName() {
      return this.tagName.copy();
   }

   public int customModelData() {
      return this.customModelData;
   }
}
