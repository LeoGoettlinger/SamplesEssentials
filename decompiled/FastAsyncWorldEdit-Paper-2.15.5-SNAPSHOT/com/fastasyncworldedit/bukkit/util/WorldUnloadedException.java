package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.WorldEditException;

public class WorldUnloadedException extends WorldEditException {
   public WorldUnloadedException() {
      super(Caption.of("worldedit.error.world-unloaded"));
   }

   public WorldUnloadedException(String name) {
      super(Caption.of("worldedit.error.named-world-unloaded", name));
   }
}
