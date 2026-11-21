package com.viaversion.viaversion.api.minecraft;

public enum GameMode {
   NOT_SET("", -1),
   SURVIVAL("Survival Mode", 0),
   CREATIVE("Creative Mode", 1),
   ADVENTURE("Adventure Mode", 2),
   SPECTATOR("Spectator Mode", 3);

   private final String text;
   private final int id;

   private GameMode(String text, int id) {
      this.text = text;
      this.id = id;
   }

   public String text() {
      return this.text;
   }

   public int id() {
      return this.id;
   }

   public static GameMode getById(int id) {
      return switch (id) {
         case -1 -> NOT_SET;
         default -> SURVIVAL;
         case 1 -> CREATIVE;
         case 2 -> ADVENTURE;
         case 3 -> SPECTATOR;
      };
   }
}
