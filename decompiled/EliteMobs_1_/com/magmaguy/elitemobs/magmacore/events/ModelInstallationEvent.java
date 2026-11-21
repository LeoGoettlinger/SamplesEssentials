package com.magmaguy.elitemobs.magmacore.events;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class ModelInstallationEvent extends Event {
   private static final HandlerList handlers = new HandlerList();

   public ModelInstallationEvent() {
      Logger.info("Models have been installed!");
      Plugin models = Bukkit.getPluginManager().getPlugin("FreeMinecraftModels");
      if (models != null && models.isEnabled()) {
         try {
            models.getClass().getMethod("reloadImportedContent", CommandSender.class).invoke(models, Bukkit.getConsoleSender());
         } catch (ReflectiveOperationException var3) {
            throw new IllegalStateException(
               "FreeMinecraftModels could not reload imported content. Install a current compatible FreeMinecraftModels build.", var3
            );
         }
      }
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}
