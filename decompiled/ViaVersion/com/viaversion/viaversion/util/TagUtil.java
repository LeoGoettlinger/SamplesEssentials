package com.viaversion.viaversion.util;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TagUtil {
   public static ListTag<CompoundTag> getRegistryEntries(CompoundTag tag, String key) {
      return getRegistryEntries(tag, key, null);
   }

   public static ListTag<CompoundTag> getRegistryEntries(CompoundTag tag, String key, @Nullable ListTag<CompoundTag> defaultValue) {
      CompoundTag registry = getNamespacedCompoundTag(tag, key);
      return registry == null ? defaultValue : registry.getListTag("value", CompoundTag.class);
   }

   public static ListTag<CompoundTag> removeRegistryEntries(CompoundTag tag, String key) {
      return removeRegistryEntries(tag, key, null);
   }

   public static ListTag<CompoundTag> removeRegistryEntries(CompoundTag tag, String key, @Nullable ListTag<CompoundTag> defaultValue) {
      String actualKey = getNamespacedTagKey(tag, key);
      CompoundTag registry = tag.getCompoundTag(actualKey);
      if (registry == null) {
         return defaultValue;
      } else {
         tag.remove(actualKey);
         return registry.getListTag("value", CompoundTag.class);
      }
   }

   public static boolean removeNamespaced(CompoundTag tag, String key) {
      return tag.remove(Key.namespaced(key)) != null || tag.remove(Key.stripMinecraftNamespace(key)) != null;
   }

   public static boolean containsNamespaced(CompoundTag tag, String key) {
      return tag.contains(Key.namespaced(key)) || tag.contains(Key.stripMinecraftNamespace(key));
   }

   public static String getNamespacedTagKey(CompoundTag tag, String name) {
      return tag.contains(Key.namespaced(name)) ? Key.namespaced(name) : Key.stripMinecraftNamespace(name);
   }

   @Nullable
   public static Tag getNamespacedTag(CompoundTag tag, String key) {
      Tag value = tag.get(Key.namespaced(key));
      return value != null ? value : tag.get(Key.stripMinecraftNamespace(key));
   }

   @Nullable
   public static CompoundTag getNamespacedCompoundTag(CompoundTag tag, String key) {
      CompoundTag compoundTag = tag.getCompoundTag(Key.namespaced(key));
      return compoundTag != null ? compoundTag : tag.getCompoundTag(Key.stripMinecraftNamespace(key));
   }

   @Nullable
   public static ListTag<? extends Tag> getNamespacedTagList(CompoundTag tag, String key) {
      ListTag<? extends Tag> listTag = (ListTag<? extends Tag>)tag.getListTag(Key.namespaced(key));
      return listTag != null ? listTag : tag.getListTag(Key.stripMinecraftNamespace(key));
   }

   @Nullable
   public static ListTag<CompoundTag> getNamespacedCompoundTagList(CompoundTag tag, String key) {
      ListTag<CompoundTag> listTag = tag.getListTag(Key.namespaced(key), CompoundTag.class);
      return listTag != null ? listTag : tag.getListTag(Key.stripMinecraftNamespace(key), CompoundTag.class);
   }

   @Nullable
   public static StringTag getNamespacedStringTag(CompoundTag tag, String key) {
      StringTag stringTag = tag.getStringTag(Key.namespaced(key));
      return stringTag != null ? stringTag : tag.getStringTag(Key.stripMinecraftNamespace(key));
   }

   @Nullable
   public static NumberTag getNamespacedNumberTag(CompoundTag tag, String key) {
      NumberTag numberTag = tag.getNumberTag(Key.namespaced(key));
      return numberTag != null ? numberTag : tag.getNumberTag(Key.stripMinecraftNamespace(key));
   }

   public static Tag handleDeep(Tag tag, TagUtil.TagUpdater consumer) {
      return handleDeep(null, tag, consumer);
   }

   private static Tag handleDeep(@Nullable String key, Tag tag, TagUtil.TagUpdater consumer) {
      if (tag instanceof CompoundTag compoundTag) {
         for (Entry<String, Tag> entry : compoundTag.entrySet()) {
            Tag updatedTag = handleDeep(entry.getKey(), entry.getValue(), consumer);
            entry.setValue(updatedTag);
         }
      } else if (tag instanceof ListTag<?> listTag) {
         handleListTag(listTag, consumer);
      }

      return consumer.update(key, tag);
   }

   private static <T extends Tag> void handleListTag(ListTag<T> listTag, TagUtil.TagUpdater consumer) {
      listTag.getValue().replaceAll(t -> handleDeep(null, t, consumer));
   }

   @FunctionalInterface
   public interface TagUpdater {
      Tag update(@Nullable String var1, Tag var2);
   }
}
