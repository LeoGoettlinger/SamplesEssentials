package com.magmaguy.elitemobs.advancedcombat.classes;

import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;

public final class BuiltInClassCatalog {
   public static final int VERSION = 1;

   private BuiltInClassCatalog() {
   }

   public static ClassCatalog catalog() {
      return BuiltInClassContent.catalog();
   }
}
