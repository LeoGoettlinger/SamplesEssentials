package xyz.lychee.lagfixer.objects;

import lombok.Generated;
import xyz.lychee.lagfixer.LagFixer;

public abstract class AbstractManager {
   private final LagFixer plugin;

   public AbstractManager(LagFixer plugin) {
      this.plugin = plugin;
   }

   public abstract void load() throws Exception;

   public abstract void disable() throws Exception;

   public abstract boolean isEnabled();

   @Generated
   public LagFixer getPlugin() {
      return this.plugin;
   }
}
