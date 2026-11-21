package com.oheers.fish.fishing.broadcast;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.Toggle;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public record FishBroadcast(@NonNull EMFMessage message, @NonNull Player player, @NonNull IFish fish) {
   public void broadcast() {
      if (!this.message.isEmpty()) {
         Stream<? extends Player> players = Bukkit.getOnlinePlayers().stream();
         players = this.filterRarity(players);
         players = this.filterCompetition(players);
         players = this.filterToggled(players);
         players.forEach(this.message::send);
      }
   }

   private Stream<? extends Player> filterRarity(@NonNull Stream<? extends Player> players) {
      IRarity rarity = this.fish.getRarity();
      if (rarity.getBroadcastOnlyRods()) {
         players = players.filter(this::isHoldingRod);
      }

      int rangeSquared = rarity.getBroadcastRange();
      if (rangeSquared > 0) {
         players = players.filter(player -> this.isWithinRange(this.player, player, rangeSquared));
      }

      return players;
   }

   private Stream<? extends Player> filterToggled(@NonNull Stream<? extends Player> players) {
      Toggle toggle = EvenMoreFish.getInstance().getToggle();
      return players.filter(player -> !toggle.isCatchMessageDisabled(player));
   }

   private Stream<? extends Player> filterCompetition(@NonNull Stream<? extends Player> players) {
      Competition active = CompetitionManager.getInstance().getActiveCompetition();
      if (active == null) {
         return players;
      } else {
         CompetitionFile file = active.getCompetitionFile();
         if (!this.fish.getRarity().getBroadcastOnlyRods() && file.shouldBroadcastOnlyRods()) {
            players = players.filter(this::isHoldingRod);
         }

         int rangeSquared = file.getBroadcastRange();
         if (rangeSquared > 0) {
            players = players.filter(player -> this.isWithinRange(this.player, player, rangeSquared));
         }

         return players;
      }
   }

   private boolean isHoldingRod(@NonNull Player player) {
      Material rodMaterial = Material.FISHING_ROD;
      return player.getInventory().getItemInMainHand().getType().equals(rodMaterial) || player.getInventory().getItemInOffHand().getType().equals(rodMaterial);
   }

   private boolean isWithinRange(@NonNull Player sourcePlayer, @NonNull Player targetPlayer, int rangeSquared) {
      return sourcePlayer.getWorld().equals(targetPlayer.getWorld()) && sourcePlayer.getLocation().distanceSquared(targetPlayer.getLocation()) <= rangeSquared;
   }
}
