package me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings;

import java.util.Objects;
import java.util.Optional;
import me.byteful.plugin.leveltools.libs.nbtapi.NBTReflectionUtil;
import me.byteful.plugin.leveltools.libs.nbtapi.NbtApiException;

public class CodecHelper {
   public static Object convertItemStackToNbt(Object itemStack) {
      Object result = null;

      try {
         result = NBTReflectionUtil.itemstack_codec.encodeStart(NBTReflectionUtil.nbtRegistryOps, itemStack);
         Objects.requireNonNull(result);
         return ((Optional)result.getClass().getMethod("result").invoke(result)).get();
      } catch (Exception var3) {
         throw new NbtApiException("Failed to convert ItemStack to NBT. " + result + " " + itemStack, var3);
      }
   }

   public static Object convertNbtToItemStack(Object nbt) {
      Object result = null;

      try {
         result = NBTReflectionUtil.itemstack_codec.parse(NBTReflectionUtil.nbtRegistryOps, nbt);
         Objects.requireNonNull(result);
         return ((Optional)result.getClass().getMethod("result").invoke(result)).get();
      } catch (Exception var3) {
         throw new NbtApiException("Failed to convert NBT to ItemStack. " + result + " " + nbt, var3);
      }
   }
}
