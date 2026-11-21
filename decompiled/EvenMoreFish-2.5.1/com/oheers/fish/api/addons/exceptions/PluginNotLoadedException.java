package com.oheers.fish.api.addons.exceptions;

public class PluginNotLoadedException extends Exception {
   private final String pluginName;

   public PluginNotLoadedException(String pluginName) {
      super(String.format("%s has not loaded yet.", pluginName));
      this.pluginName = pluginName;
   }

   public String getPluginName() {
      return this.pluginName;
   }
}
