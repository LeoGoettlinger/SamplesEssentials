package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.MessageUtils;
import de.theredend2000.advancedhunt.util.PlayerSnapshot;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class PlaceModeManager {
   private final Map<UUID, UUID> placeModePlayers = new ConcurrentHashMap<>();
   private final Main plugin;

   public PlaceModeManager(Main plugin) {
      this.plugin = plugin;
   }

   public void setPlaceMode(Player player, Collection collection) {
      this.placeModePlayers.put(player.getUniqueId(), collection.getId());
   }

   public void removePlaceMode(Player player) {
      this.placeModePlayers.remove(player.getUniqueId());
   }

   public boolean isInPlaceMode(Player player) {
      return this.placeModePlayers.containsKey(player.getUniqueId());
   }

   public UUID getCollectionId(Player player) {
      return this.placeModePlayers.get(player.getUniqueId());
   }

   public void processTick(List<PlayerSnapshot> snapshots) {
      for (PlayerSnapshot snapshot : snapshots) {
         Player player = snapshot.player();
         if (this.isInPlaceMode(player)) {
            MessageUtils.sendActionBar(snapshot.player(), this.plugin.getMessageManager().getMessage("proximity.in_place_mode"));
         }
      }
   }
}
