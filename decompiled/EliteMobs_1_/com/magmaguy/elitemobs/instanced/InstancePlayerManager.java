package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.magmacore.match.AdmissionResult;
import com.magmaguy.elitemobs.magmacore.match.LeaveReason;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.bukkit.entity.Player;

public class InstancePlayerManager {
   static void restoreFullHealth(Player player) {
      if (player != null && player.isOnline() && !player.isDead()) {
         player.setHealth(player.getMaxHealth());
      }
   }

   public static boolean addNewPlayer(Player player, MatchInstance matchInstance) {
      return addNewPlayers(List.of(player), matchInstance);
   }

   public static boolean addNewPlayers(Collection<Player> requestedPlayers, MatchInstance matchInstance) {
      return addNewPlayers(requestedPlayers, matchInstance, () -> true);
   }

   public static boolean addNewPlayers(Collection<Player> requestedPlayers, MatchInstance matchInstance, BooleanSupplier authorization) {
      return authorization == null ? false : matchInstance.admit(requestedPlayers, authorization) == AdmissionResult.ADMITTED;
   }

   public static void rollbackAdmissions(Collection<Player> admittedPlayers, MatchInstance matchInstance) {
      for (Player player : admittedPlayers) {
         matchInstance.leave(player, LeaveReason.QUIT);
      }
   }

   public static void removePlayer(Player player, MatchInstance matchInstance) {
      matchInstance.leave(player, LeaveReason.QUIT);
   }

   public static void playerDeath(MatchInstance matchInstance, Player player) {
      matchInstance.playerDeath(player);
   }

   public static void revivePlayer(MatchInstance matchInstance, Player player, InstanceDeathLocation deathLocation) {
      matchInstance.revivePlayer(player, deathLocation);
   }

   public static void addSpectator(MatchInstance matchInstance, Player player, boolean wasPlayer) {
      matchInstance.addSpectator(player, wasPlayer);
   }

   public static void removeSpectator(MatchInstance matchInstance, Player player) {
      matchInstance.leave(player, LeaveReason.QUIT);
   }

   public static void removeAnyKind(MatchInstance matchInstance, Player player) {
      matchInstance.leave(player, LeaveReason.QUIT);
   }
}
