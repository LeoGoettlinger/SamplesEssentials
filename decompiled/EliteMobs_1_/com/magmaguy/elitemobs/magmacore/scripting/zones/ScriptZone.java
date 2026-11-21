package com.magmaguy.elitemobs.magmacore.scripting.zones;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScriptZone {
   private final Shape shape;
   private final Set<UUID> insidePlayers = new HashSet<>();
   private BiConsumer<Player, ScriptZone> onEnter;
   private BiConsumer<Player, ScriptZone> onLeave;

   public ScriptZone(Shape shape) {
      this.shape = shape;
   }

   public void setOnEnter(BiConsumer<Player, ScriptZone> callback) {
      this.onEnter = callback;
   }

   public void setOnLeave(BiConsumer<Player, ScriptZone> callback) {
      this.onLeave = callback;
   }

   public Shape getShape() {
      return this.shape;
   }

   public void tick(Collection<? extends Player> nearbyPlayers) {
      Set<UUID> currentlyInside = new HashSet<>();

      for (Player player : nearbyPlayers) {
         if (this.shape.contains(player.getLocation())) {
            currentlyInside.add(player.getUniqueId());
            if (!this.insidePlayers.contains(player.getUniqueId()) && this.onEnter != null) {
               this.onEnter.accept(player, this);
            }
         }
      }

      for (UUID uuid : this.insidePlayers) {
         if (!currentlyInside.contains(uuid) && this.onLeave != null) {
            Player playerx = Bukkit.getPlayer(uuid);
            if (playerx != null) {
               this.onLeave.accept(playerx, this);
            }
         }
      }

      this.insidePlayers.clear();
      this.insidePlayers.addAll(currentlyInside);
   }

   public void shutdown() {
      this.insidePlayers.clear();
   }
}
