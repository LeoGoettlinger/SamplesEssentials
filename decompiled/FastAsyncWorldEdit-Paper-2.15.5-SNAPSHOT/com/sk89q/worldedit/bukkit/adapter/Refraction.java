package com.sk89q.worldedit.bukkit.adapter;

public class Refraction {
   private static final String MOJANG_MAPPED_CLASS_NAME = "net.minecraft.nbt.ListTag";
   private static final boolean IS_MOJANG_MAPPED;

   public static String pickName(String mojangName, String spigotName) {
      return IS_MOJANG_MAPPED ? mojangName : spigotName;
   }

   public static boolean isMojangMapped() {
      return IS_MOJANG_MAPPED;
   }

   private Refraction() {
   }

   static {
      boolean isMojangMapped;
      try {
         Class.forName("net.minecraft.nbt.ListTag", false, Refraction.class.getClassLoader());
         isMojangMapped = true;
      } catch (ClassNotFoundException var2) {
         isMojangMapped = false;
      }

      IS_MOJANG_MAPPED = isMojangMapped;
   }
}
