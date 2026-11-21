package com.earth2me.essentials.commands;

import java.util.Comparator;
import org.bukkit.World;

class WorldNameComparator implements Comparator<World> {
   public int compare(final World a, final World b) {
      return a.getName().compareTo(b.getName());
   }
}
