package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import java.util.Objects;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractAtEntityListener implements Listener {
   private final Graves plugin;

   public PlayerInteractAtEntityListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
      Player player = event.getPlayer();
      Entity clicked = event.getRightClicked();
      if (this.isMainHandInteraction(event)) {
         if (this.isArmorStand(clicked)) {
            if (this.isNotSpectatorMode(player)) {
               Grave grave = this.plugin.getEntityDataManager().getGrave(clicked);
               if (grave != null) {
                  event.setCancelled(this.plugin.getGraveManager().openGrave(player, clicked.getLocation(), grave));
               }
            }
         }
      }
   }

   private boolean isMainHandInteraction(PlayerInteractAtEntityEvent event) {
      return !this.plugin.getVersionManager().hasSecondHand() || event.getHand() == EquipmentSlot.HAND;
   }

   private boolean isArmorStand(Entity entity) {
      return entity instanceof ArmorStand;
   }

   private boolean isNotSpectatorMode(Player player) {
      if (this.plugin.getVersionManager().is_v1_7()) {
         return true;
      } else {
         return this.plugin.getPermissionManager().hasGrantedPermission("graves.spectator.bypass", player.getPlayer())
            ? true
            : Objects.requireNonNull(player.getPlayer()).getGameMode() != GameMode.SPECTATOR;
      }
   }
}
