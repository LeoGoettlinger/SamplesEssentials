package de.theredend2000.advancedhunt.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class ItemSerializer {
   private static final int MAX_SERIALIZED_SIZE = 10485760;

   public static String serialize(ItemStack item) {
      if (item == null) {
         throw new IllegalArgumentException("Item cannot be null");
      } else {
         try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            String var3;
            try {
               BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

               try {
                  dataOutput.writeObject(item);
                  dataOutput.flush();
                  var3 = Base64Coder.encodeLines(outputStream.toByteArray());
               } catch (Throwable var7) {
                  try {
                     dataOutput.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }

                  throw var7;
               }

               dataOutput.close();
            } catch (Throwable var8) {
               try {
                  outputStream.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }

               throw var8;
            }

            outputStream.close();
            return var3;
         } catch (IOException var9) {
            Bukkit.getLogger().severe("Failed to serialize item: " + var9.getMessage());
            return null;
         }
      }
   }

   public static ItemStack deserialize(String data) {
      if (data != null && !data.isEmpty()) {
         byte[] decodedData = Base64Coder.decodeLines(data);
         if (decodedData.length > 10485760) {
            throw new IllegalArgumentException("Serialized data exceeds maximum allowed size of 10485760 bytes");
         } else {
            try {
               ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedData);

               ItemStack var5;
               try {
                  BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

                  try {
                     Object obj = dataInput.readObject();
                     if (!(obj instanceof ItemStack)) {
                        throw new IllegalArgumentException("Deserialized object is not an ItemStack");
                     }

                     var5 = (ItemStack)obj;
                  } catch (Throwable var8) {
                     try {
                        dataInput.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }

                     throw var8;
                  }

                  dataInput.close();
               } catch (Throwable var9) {
                  try {
                     inputStream.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }

                  throw var9;
               }

               inputStream.close();
               return var5;
            } catch (IOException var10) {
               Bukkit.getLogger().severe("Failed to deserialize item (IO error): " + var10.getMessage());
               return null;
            } catch (ClassNotFoundException var11) {
               Bukkit.getLogger().severe("Failed to deserialize item (class not found): " + var11.getMessage());
               return null;
            }
         }
      } else {
         throw new IllegalArgumentException("Data cannot be null or empty");
      }
   }
}
