package de.theredend2000.advancedhunt.menu.common;

public final class SkullInfo {
   private final String texture;
   private final String ownerName;

   public SkullInfo(String texture, String ownerName) {
      this.texture = texture;
      this.ownerName = ownerName;
   }

   public String texture() {
      return this.texture;
   }

   public String ownerName() {
      return this.ownerName;
   }
}
