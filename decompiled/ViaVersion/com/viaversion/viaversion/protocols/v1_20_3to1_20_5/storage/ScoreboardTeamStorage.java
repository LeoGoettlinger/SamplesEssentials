package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class ScoreboardTeamStorage {
   private final Map<String, Set<String>> teams = new HashMap<>();

   public void createTeam(String name) {
      if (!this.teams.containsKey(name)) {
         this.teams.put(name, new HashSet<>());
      }
   }

   public void removeTeam(String name) {
      this.teams.remove(name);
   }

   public void addPlayerToTeam(String team, String[] player) {
      Set<String> players = this.teams.get(team);
      if (players != null) {
         for (Set<String> allPlayers : this.teams.values()) {
            for (String toRemove : player) {
               allPlayers.remove(toRemove);
            }
         }

         Collections.addAll(players, player);
      }
   }

   public void removeFromTeam(String team, String player) {
      Set<String> players = this.teams.get(team);
      if (players != null) {
         players.remove(player);
      }
   }

   public String getPlayerTeam(String player) {
      for (Entry<String, Set<String>> entry : this.teams.entrySet()) {
         if (entry.getValue().contains(player)) {
            return entry.getKey();
         }
      }

      return null;
   }
}
