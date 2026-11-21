package com.sk89q.worldedit.bukkit.adapter;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.WorldEditException;

public class UnsupportedVersionEditException extends WorldEditException {
   public UnsupportedVersionEditException() {
      super(Caption.of("worldedit.bukkit.no-edit-without-adapter"));
   }
}
