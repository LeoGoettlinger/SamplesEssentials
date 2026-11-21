package com.artillexstudios.axtrade.libs.axapi.utils;

import com.artillexstudios.axtrade.libs.axapi.AxPlugin;

public final class PaperNameable implements Nameable {
   private final AxPlugin plugin;

   public PaperNameable(AxPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String getName() {
      return this.plugin.getName();
   }
}
