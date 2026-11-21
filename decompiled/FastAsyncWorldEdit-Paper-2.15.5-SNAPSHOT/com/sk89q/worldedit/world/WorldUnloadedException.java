package com.sk89q.worldedit.world;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.WorldEditException;

public class WorldUnloadedException extends WorldEditException {
   public WorldUnloadedException() {
      super(Caption.of("worldedit.error.world-unloaded"));
   }
}
