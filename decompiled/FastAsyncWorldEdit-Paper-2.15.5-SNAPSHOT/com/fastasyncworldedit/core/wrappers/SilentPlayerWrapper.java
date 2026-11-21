package com.fastasyncworldedit.core.wrappers;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.formatting.text.Component;

public class SilentPlayerWrapper extends AsyncPlayer {
   public SilentPlayerWrapper(Player parent) {
      super(parent);
   }

   @Override
   public void print(String msg) {
   }

   @Override
   public void print(Component component) {
   }

   @Override
   public void printDebug(String msg) {
   }

   @Override
   public void printError(String msg) {
   }

   @Override
   public void printRaw(String msg) {
   }
}
