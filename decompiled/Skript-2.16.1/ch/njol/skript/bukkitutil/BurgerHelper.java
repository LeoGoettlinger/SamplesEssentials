package ch.njol.skript.bukkitutil;

import com.google.gson.Gson;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public class BurgerHelper {
   private static MethodHandle typeIdMethod;
   public BurgerHelper.Burger burger;

   public BurgerHelper(String data) {
      if (typeIdMethod == null) {
         throw new IllegalStateException("requires Minecraft 1.12.2 or older");
      } else {
         this.burger = ((BurgerHelper.Burger[])new Gson().fromJson(data, BurgerHelper.Burger[].class))[0];
      }
   }

   public Map<String, Material> mapMaterials() {
      Map<String, Material> materials = new HashMap<>();

      for (Material material : Material.values()) {
         int id;
         try {
            id = (int)typeIdMethod.invokeExact((Material)material);
         } catch (Throwable var10) {
            throw new RuntimeException(var10);
         }

         String vanillaId = null;

         for (BurgerHelper.ItemOrBlock item : this.burger.items.item.values()) {
            if (item.numeric_id == id) {
               vanillaId = item.text_id;
               break;
            }
         }

         for (BurgerHelper.ItemOrBlock block : this.burger.blocks.block.values()) {
            if (block.numeric_id == id) {
               vanillaId = block.text_id;
               break;
            }
         }

         materials.put(vanillaId, material);
      }

      return materials;
   }

   public static Map<Integer, Material> mapIds() {
      if (typeIdMethod == null) {
         throw new IllegalStateException("requires Minecraft 1.12.2 or older");
      } else {
         Map<Integer, Material> ids = new HashMap<>();

         for (Material mat : Material.values()) {
            try {
               ids.put((int)typeIdMethod.invokeExact((Material)mat), mat);
            } catch (Throwable var6) {
               throw new RuntimeException(var6);
            }
         }

         return ids;
      }
   }

   static {
      try {
         MethodHandle mh = MethodHandles.lookup().findVirtual(Material.class, "getId", MethodType.methodType(int.class));
         typeIdMethod = mh;
      } catch (IllegalAccessException | NoSuchMethodException var2) {
         typeIdMethod = null;
      }
   }

   public static class Blocks {
      public Map<String, BurgerHelper.ItemOrBlock> block;
   }

   public static class Burger {
      public BurgerHelper.Items items;
      public BurgerHelper.Blocks blocks;
      public BurgerHelper.Source source;
   }

   public static class ItemOrBlock {
      public String text_id;
      public int numeric_id;
      public String display_name;
   }

   public static class Items {
      public Map<String, BurgerHelper.ItemOrBlock> item;
   }

   public static class Source {
      public String file;
   }
}
