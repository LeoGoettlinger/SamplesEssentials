package net.thenextlvl.protect.listeners;

import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.flag.ProtectionFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class NexoListener implements Listener {
   private final ProtectPlugin plugin;
   private final ProtectionFlag<Boolean> furnitureBreak;
   private final ProtectionFlag<Boolean> furniturePlace;
   private final ProtectionFlag<Boolean> furnitureInteract;

   public NexoListener(ProtectPlugin plugin, Plugin nexo) {
      this.furnitureBreak = plugin.flagRegistry().register(nexo, "furniture_break", true, false);
      this.furniturePlace = plugin.flagRegistry().register(nexo, "furniture_place", true, false);
      this.furnitureInteract = plugin.flagRegistry().register(nexo, "furniture_interact", true, false);
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onNexoFurnitureBreakEvent(NexoFurnitureBreakEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getBaseEntity());
      event.setCancelled(!this.plugin.protectionService().canPerformAction(event.getPlayer(), area, this.furnitureBreak, "protect.bypass.destroy"));
      this.plugin.failed(event.getPlayer(), event, area, "area.failed.break");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onNexoFurniturePlace(NexoFurniturePlaceEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getBaseEntity());
      event.setCancelled(!this.plugin.protectionService().canPerformAction(event.getPlayer(), area, this.furniturePlace, "protect.bypass.place"));
      this.plugin.failed(event.getPlayer(), event, area, "area.failed.place");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onNexoFurnitureInteract(NexoFurnitureInteractEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getBaseEntity());
      event.setCancelled(!this.plugin.protectionService().canPerformAction(event.getPlayer(), area, this.furnitureInteract, "protect.bypass.interact"));
      this.plugin.failed(event.getPlayer(), event, area, "area.failed.interact");
   }
}
