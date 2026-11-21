package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.util.ReflectionUtils;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {
   private final Method methodAsNMSCopy;
   private final Method methodGetTag;
   private final Method methodHasTag;
   private final Method methodSetTag;
   private final Method methodAsBukkitCopy;
   private final Field fieldHandle;
   private final BukkitImplAdapter adapter;
   private SoftReference<Int2ObjectOpenHashMap<WeakReference<Tag>>> hashToNMSTag = new SoftReference<>(new Int2ObjectOpenHashMap());

   public ItemUtil() throws Exception {
      this.adapter = Objects.requireNonNull(WorldEditPlugin.getInstance().getBukkitImplAdapter());
      Class<?> classCraftItemStack = BukkitReflectionUtils.getCbClass("inventory.CraftItemStack");
      Class<?> classNMSItem = BukkitReflectionUtils.getNmsClass("ItemStack");
      this.methodAsNMSCopy = ReflectionUtils.setAccessible(classCraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class));
      this.methodHasTag = ReflectionUtils.setAccessible(classNMSItem.getDeclaredMethod("hasTag"));
      this.methodGetTag = ReflectionUtils.setAccessible(classNMSItem.getDeclaredMethod("getTag"));
      this.fieldHandle = ReflectionUtils.setAccessible(classCraftItemStack.getDeclaredField("handle"));
      Class<?> classNBTTagCompound = BukkitReflectionUtils.getNmsClass("NBTTagCompound");
      this.methodSetTag = ReflectionUtils.setAccessible(classNMSItem.getDeclaredMethod("setTag", classNBTTagCompound));
      this.methodAsBukkitCopy = ReflectionUtils.setAccessible(classCraftItemStack.getDeclaredMethod("asBukkitCopy", classNMSItem));
   }

   public Object getNMSItem(ItemStack item) {
      try {
         Object nmsItem = this.fieldHandle.get(item);
         if (nmsItem == null) {
            nmsItem = this.methodAsNMSCopy.invoke(null, item);
         }

         return nmsItem;
      } catch (Throwable var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public CompoundTag getNBT(ItemStack item) {
      try {
         if (!item.hasItemMeta()) {
            return null;
         }

         Object nmsItem = this.fieldHandle.get(item);
         if (nmsItem == null) {
            nmsItem = this.methodAsNMSCopy.invoke(null, item);
         }

         if (this.methodHasTag.invoke(nmsItem).equals(true)) {
            Object nmsTag = this.methodGetTag.invoke(nmsItem);
            if (nmsTag == null) {
               return null;
            }

            Int2ObjectOpenHashMap<WeakReference<Tag>> map = this.hashToNMSTag.get();
            if (map == null) {
               map = new Int2ObjectOpenHashMap();
               this.hashToNMSTag = new SoftReference<>(new Int2ObjectOpenHashMap(map));
            }

            WeakReference<Tag> nativeTagRef = (WeakReference<Tag>)map.get(nmsTag.hashCode());
            if (nativeTagRef != null) {
               Tag nativeTag = nativeTagRef.get();
               if (nativeTag != null) {
                  return (CompoundTag)nativeTag;
               }
            }

            Tag nativeTag = this.adapter.toNative(nmsTag);
            map.put(nmsTag.hashCode(), new WeakReference<>(nativeTag));
            return null;
         }
      } catch (Throwable var7) {
         var7.printStackTrace();
      }

      return null;
   }

   public ItemStack setNBT(ItemStack item, CompoundTag tag) {
      try {
         Object nmsItem = this.fieldHandle.get(item);
         boolean copy = false;
         if (nmsItem == null) {
            copy = true;
            nmsItem = this.methodAsNMSCopy.invoke(null, item);
         }

         Object nmsTag = this.adapter.fromNative(tag);
         this.methodSetTag.invoke(nmsItem, nmsTag);
         return copy ? (ItemStack)this.methodAsBukkitCopy.invoke(null, nmsItem) : item;
      } catch (Throwable var6) {
         var6.printStackTrace();
         return null;
      }
   }
}
