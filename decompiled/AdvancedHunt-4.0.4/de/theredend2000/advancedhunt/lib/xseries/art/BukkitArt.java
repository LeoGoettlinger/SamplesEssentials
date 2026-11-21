package de.theredend2000.advancedhunt.lib.xseries.art;

import de.theredend2000.advancedhunt.lib.xseries.AbstractReferencedClass;
import org.bukkit.Art;

public abstract class BukkitArt extends AbstractReferencedClass<Art> {
   public abstract int getBlockWidth();

   public abstract int getBlockHeight();

   public abstract String getKey();

   public abstract int getId();
}
