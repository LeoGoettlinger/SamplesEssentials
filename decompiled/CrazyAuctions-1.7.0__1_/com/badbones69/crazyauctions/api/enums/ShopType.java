package com.badbones69.crazyauctions.api.enums;

public enum ShopType {
   SELL("Sell"),
   BID("Bid");

   private final String name;

   private ShopType(String name) {
      this.name = name;
   }

   public static ShopType getFromName(String name) {
      for (ShopType type : values()) {
         if (type.getName().equalsIgnoreCase(name)) {
            return type;
         }
      }

      return null;
   }

   public String getName() {
      return this.name;
   }
}
