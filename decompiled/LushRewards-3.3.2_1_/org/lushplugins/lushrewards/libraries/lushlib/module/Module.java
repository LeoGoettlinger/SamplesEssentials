package org.lushplugins.lushrewards.libraries.lushlib.module;

import java.util.logging.Level;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.lushplugins.lushrewards.libraries.lushlib.LushLib;
import org.lushplugins.lushrewards.libraries.lushlib.LushLogger;

public abstract class Module {
   protected final String id;
   private boolean enabled = false;

   public Module(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   protected void onEnable() {
   }

   public final void enable() {
      if (!this.enabled) {
         this.enabled = true;

         try {
            this.onEnable();
         } catch (Throwable var3) {
            LushLogger.getLogger().log(Level.SEVERE, "Error when enabling module '%s' at: ".formatted(this.id), var3);
            LushLogger.getLogger().log(Level.SEVERE, "Disabling module '%s'".formatted(this.id));
            this.disable();
            return;
         }

         if (this.enabled && this instanceof Listener listener) {
            LushLib.getInstance().getPlugin().getServer().getPluginManager().registerEvents(listener, LushLib.getInstance().getPlugin());
         }

         LushLogger.getLogger().log(Level.INFO, "Successfully enabled module '%s'".formatted(this.id));
      }
   }

   protected void onDisable() {
   }

   public final void disable() {
      this.enabled = false;

      try {
         this.onDisable();
      } catch (Throwable var3) {
         LushLogger.getLogger().log(Level.SEVERE, "Error when disabling module '%s' at: ".formatted(this.id), var3);
      }

      if (!this.enabled && this instanceof Listener listener) {
         HandlerList.unregisterAll(listener);
      }
   }

   public final void reload() {
      if (this.enabled) {
         this.disable();
      }

      this.enable();
   }
}
