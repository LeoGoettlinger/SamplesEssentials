package com.magmaguy.elitemobs.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class ObjectSerializer {
   private ObjectSerializer() {
   }

   public static Object fromString(String s) throws IOException, ClassNotFoundException {
      byte[] data = Base64.getDecoder().decode(s);
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
      Object o = ois.readObject();
      ois.close();
      return o;
   }

   public static String toString(Serializable o) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(o);
      oos.close();
      return Base64.getEncoder().encodeToString(baos.toByteArray());
   }

   public static String itemStackArrayToBase64(ItemStack itemStack) throws IllegalStateException {
      try {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
         dataOutput.writeObject(itemStack);
         dataOutput.close();
         return Base64.getMimeEncoder().encodeToString(outputStream.toByteArray());
      } catch (Exception var3) {
         throw new IllegalStateException("Unable to save item stacks.", var3);
      }
   }

   public static ItemStack itemStackArrayFromBase64(String data) throws IOException {
      try {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(data));
         BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
         ItemStack itemStack = (ItemStack)dataInput.readObject();
         dataInput.close();
         return itemStack;
      } catch (ClassNotFoundException var4) {
         throw new IOException("Unable to decode class type.", var4);
      }
   }
}
