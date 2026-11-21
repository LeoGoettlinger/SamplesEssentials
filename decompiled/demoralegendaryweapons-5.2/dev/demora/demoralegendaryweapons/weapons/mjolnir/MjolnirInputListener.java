package dev.demora.demoralegendaryweapons.weapons.mjolnir;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Input;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MjolnirInputListener implements Listener {
   private final Mjolnir mjolnir;
   private final DemoraLegendaryWeapons plugin;
   private final Map<UUID, Input> throwChargeInputCache = new HashMap<>();

   public MjolnirInputListener(Mjolnir mjolnir, DemoraLegendaryWeapons plugin) {
      this.mjolnir = mjolnir;
      this.plugin = plugin;
   }

   public void putThrowChargeInput(UUID playerId, Input input) {
      if (playerId != null && input != null) {
         this.throwChargeInputCache.put(playerId, input);
      }
   }

   public Input getThrowChargeInput(UUID playerId) {
      return this.throwChargeInputCache.get(playerId);
   }

   public void clearThrowChargeInput(UUID playerId) {
      this.throwChargeInputCache.remove(playerId);
   }

   @EventHandler
   public void onThrowChargeUpdate(PlayerInputEvent e) {
      this.putThrowChargeInput(e.getPlayer().getUniqueId(), e.getInput());
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.clearThrowChargeInput(e.getPlayer().getUniqueId());
   }
}
