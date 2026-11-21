package su.nightexpress.nightcore.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.nbt.NbtUtil;

@Deprecated
public class ItemNbt {
   private static final Class<?> CLS_COMPOUND_TAG = Reflex.safeClass("net.minecraft.nbt", "CompoundTag", "NBTTagCompound");
   private static final Class<?> CLS_NBT_IO = Reflex.safeClass("net.minecraft.nbt", "NbtIo", "NBTCompressedStreamTools");
   private static final Method NBT_IO_WRITE = Reflex.getMethod(CLS_NBT_IO, "write", "a", CLS_COMPOUND_TAG, DataOutput.class);
   private static final Method NBT_IO_READ = Reflex.getMethod(CLS_NBT_IO, "read", "a", DataInput.class);

   @Deprecated
   @NonNull
   public static List<String> compress(ItemStack[] items) {
      return compress(Arrays.asList(items));
   }

   @Deprecated
   @NonNull
   public static List<String> compress(@NonNull List<ItemStack> items) {
      return new ArrayList<>(items.stream().map(ItemNbt::compress).filter(Objects::nonNull).toList());
   }

   @Deprecated
   @Nullable
   public static String compress(@NonNull ItemStack item) {
      Object compoundTag = toCompoundTag(item);
      if (compoundTag == null) {
         return null;
      } else {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         DataOutputStream dataOutput = new DataOutputStream(outputStream);
         Reflex.invokeMethod(NBT_IO_WRITE, null, compoundTag, dataOutput);
         return new BigInteger(1, outputStream.toByteArray()).toString(32);
      }
   }

   @Deprecated
   @Nullable
   public static ItemStack decompress(@NonNull String compressed) {
      if (compressed.isBlank()) {
         return null;
      } else {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(compressed, 32).toByteArray());
         DataInputStream dataInput = new DataInputStream(inputStream);
         Object compoundTag = Reflex.invokeMethod(NBT_IO_READ, null, dataInput);
         return compoundTag == null ? null : fromCompoundTag(compoundTag, Version.MC_1_21.getDataVersion());
      }
   }

   @Deprecated
   public static ItemStack[] decompress(@NonNull List<String> list) {
      List<ItemStack> items = list.stream().map(ItemNbt::decompress).filter(Objects::nonNull).toList();
      return items.toArray(new ItemStack[list.size()]);
   }

   @Deprecated
   @Nullable
   public static String getTagString(@NonNull ItemStack item) {
      Object compoundTag = toCompoundTag(item);
      return compoundTag == null ? null : compoundTag.toString();
   }

   @Deprecated
   @Nullable
   public static ItemStack fromTagString(@NonNull String tagString) {
      return new ItemTag(tagString, Version.MC_1_21.getDataVersion()).getItemStack();
   }

   @Deprecated
   @Nullable
   public static ItemStack fromTag(@NonNull ItemTag itemTag) {
      return itemTag.getItemStack();
   }

   @Deprecated
   @Nullable
   public static ItemTag getTag(@NonNull ItemStack item) {
      try {
         return ItemTag.of(item);
      } catch (IllegalStateException var2) {
         return null;
      }
   }

   @Deprecated
   @Nullable
   public static ItemStack getHoverEventItem(@NonNull String value) {
      ItemStack itemStack = null;

      try {
         itemStack = Bukkit.getItemFactory().createItemStack(value);
      } catch (IllegalArgumentException var5) {
         try {
            itemStack = decompress(value);
         } catch (NumberFormatException var4) {
         }
      }

      return itemStack != null && itemStack.getType().isAir() ? null : itemStack;
   }

   @Deprecated
   @Nullable
   private static Object toCompoundTag(@NonNull ItemStack bukkitStack) {
      return !bukkitStack.getType().isAir() && bukkitStack.getAmount() > 0 ? NbtUtil.tagFromItemStack(bukkitStack) : null;
   }

   @Deprecated
   @Nullable
   private static ItemStack fromCompoundTag(@NonNull Object tag, int sourceVersion) {
      return NbtUtil.tagToItemStack(tag, sourceVersion);
   }
}
