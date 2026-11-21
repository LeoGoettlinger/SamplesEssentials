package com.artillexstudios.axafkzone.libs.axapi.serializers.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axafkzone.libs.axapi.serializers.Serializer;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemArraySerializer implements Serializer<ItemStack[], byte[]> {
   private static final ItemStack AIR = new ItemStack(Material.AIR);

   public byte[] serialize(ItemStack[] value) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      ByteArrayDataOutput outputStream = ByteStreams.newDataOutput(stream);
      outputStream.writeInt(value.length);

      for (ItemStack item : value) {
         if (item != null && !item.getType().isAir()) {
            try {
               byte[] serialized = WrappedItemStack.wrap(item).serialize();
               outputStream.writeShort(serialized.length);
               outputStream.write(serialized);
            } catch (Exception var9) {
               LogUtils.error("An unexpected error occurred while serializing itemstack of type {}. Item: {}", item.getType(), item, var9);
               outputStream.writeShort(0);
            }
         } else {
            outputStream.writeShort(0);
         }
      }

      return outputStream.toByteArray();
   }

   public ItemStack[] deserialize(byte[] value) {
      ByteArrayDataInput input = ByteStreams.newDataInput(value);
      int length = input.readInt();
      ItemStack[] items = new ItemStack[length];

      for (int i = 0; i < length; i++) {
         short size = input.readShort();
         if (size > 0) {
            byte[] read = new byte[size];
            input.readFully(read);

            try {
               items[i] = WrappedItemStack.wrap(read).toBukkit();
            } catch (Exception var9) {
               LogUtils.error("An unexpected error occurred while deserializing itemstack! Read bytes: {}", Arrays.toString(read), var9);
            }
         } else {
            items[i] = AIR.clone();
         }
      }

      return items;
   }
}
