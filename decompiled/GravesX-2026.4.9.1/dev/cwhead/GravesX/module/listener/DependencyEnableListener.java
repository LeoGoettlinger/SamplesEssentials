package dev.cwhead.GravesX.module.listener;

import dev.cwhead.GravesX.module.ModuleManager;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public final class DependencyEnableListener implements Listener {
   private final ModuleManager manager;

   public DependencyEnableListener(ModuleManager manager) {
      this.manager = Objects.requireNonNull(manager, "manager");
   }

   @EventHandler
   public void onPluginEnable(PluginEnableEvent e) {
      this.manager.tryEnablePending();
   }
}
