package de.theredend2000.advancedhunt.lib.xseries.art;

import org.bukkit.Art;

public class XArt {
   private static final boolean USE_INTERFACE = Art.class.isInterface();

   private XArt() {
   }

   public static BukkitArt of(Art art) {
      return (BukkitArt)(USE_INTERFACE ? new NewArt(art) : new OldArt(art));
   }
}
