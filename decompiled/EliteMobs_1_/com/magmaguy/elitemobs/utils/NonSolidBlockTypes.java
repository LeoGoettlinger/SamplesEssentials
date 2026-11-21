package com.magmaguy.elitemobs.utils;

import java.util.ArrayList;
import org.bukkit.Material;

public class NonSolidBlockTypes {
   private static final ArrayList<Material> nonSolidBlocks = new ArrayList<>();

   public static boolean isPassthrough(Material material) {
      return !material.isSolid();
   }
}
