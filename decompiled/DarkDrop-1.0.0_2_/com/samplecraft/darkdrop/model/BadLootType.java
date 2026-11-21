package com.samplecraft.darkdrop.model;

public enum BadLootType {
   BAD_ITEM("Schlechtes Item", "Toter Busch als Loot"),
   FIRE("Feuer", "3s Feuer in 2 Block Radius"),
   LIGHTNING("Harmloser Blitz", "Kosmetischer Blitzschlag"),
   EXPLOSION("Kleine Explosion", "Schaden: 4.0 Halbe Herzen"),
   POISON("Vergiftung", "Kurzer Gift-Effekt"),
   VOID_EFFECT("Leere-Effekt", "Kurze Blindheit"),
   WITHER("Wither", "Kurzer Withereffekt"),
   NOTHING("Nichts", "Es passiert nichts");

   private final String displayName;
   private final String description;

   private BadLootType(String displayName, String description) {
      this.displayName = displayName;
      this.description = description;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getDescription() {
      return this.description;
   }

   public BadLootType next() {
      BadLootType[] values = values();
      return values[(this.ordinal() + 1) % values.length];
   }
}
