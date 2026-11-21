package eu.kennytv.maintenance.core.util;

public enum ServerType {
   BUNGEE("Bungee"),
   PAPER("Paper"),
   SPONGE("Sponge"),
   VELOCITY("Velocity");

   private final String name;

   private ServerType(final String name) {
      this.name = name;
   }

   public boolean isProxy() {
      return this == BUNGEE || this == VELOCITY;
   }

   @Override
   public String toString() {
      return this.name;
   }
}
