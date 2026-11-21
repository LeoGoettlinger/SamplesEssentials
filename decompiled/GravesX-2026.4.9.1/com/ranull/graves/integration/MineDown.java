package com.ranull.graves.integration;

public class MineDown {
   public static String parseString(String string) {
      return new com.ranull.graves.libraries.minedown.adventure.MineDown(string).message();
   }
}
