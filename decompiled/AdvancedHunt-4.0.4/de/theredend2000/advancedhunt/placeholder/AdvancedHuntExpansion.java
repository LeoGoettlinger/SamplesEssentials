package de.theredend2000.advancedhunt.placeholder;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.managers.CollectionManager;
import de.theredend2000.advancedhunt.managers.LeaderboardManager;
import de.theredend2000.advancedhunt.managers.PlayerManager;
import de.theredend2000.advancedhunt.managers.TreasureManager;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.TreasureCore;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdvancedHuntExpansion extends PlaceholderExpansion {
   private final Main plugin;
   private final PlayerManager playerManager;
   private final TreasureManager treasureManager;
   private final CollectionManager collectionManager;
   private final LeaderboardManager leaderboardManager;

   public AdvancedHuntExpansion(Main plugin) {
      this.plugin = plugin;
      this.playerManager = plugin.getPlayerManager();
      this.treasureManager = plugin.getTreasureManager();
      this.collectionManager = plugin.getCollectionManager();
      this.leaderboardManager = plugin.getLeaderboardManager();
   }

   @NotNull
   public String getIdentifier() {
      return "advancedhunt";
   }

   @NotNull
   public String getAuthor() {
      return "TheRedEnd2000";
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public boolean persist() {
      return true;
   }

   private Optional<Collection> resolveCollection(String identifier) {
      try {
         UUID uuid = UUID.fromString(identifier);
         return this.collectionManager.getCollectionById(uuid);
      } catch (IllegalArgumentException var3) {
         return this.collectionManager.getCollectionByName(identifier);
      }
   }

   public String onPlaceholderRequest(Player player, @NotNull String params) {
      if (player == null) {
         return "";
      } else {
         PlayerData data = this.playerManager.getPlayerData(player.getUniqueId());
         if (params.equalsIgnoreCase("found_count")) {
            return String.valueOf(data.getFoundTreasures().size());
         } else if (params.equalsIgnoreCase("collection_size")) {
            return String.valueOf(this.collectionManager.getAllCollections().size());
         } else if (params.startsWith("has_found_")) {
            String idStr = params.substring("has_found_".length());

            try {
               UUID id = UUID.fromString(idStr);
               return data.hasFound(id) ? "true" : "false";
            } catch (IllegalArgumentException var12) {
               return "invalid_id";
            }
         } else if (params.startsWith("max_treasures_")) {
            String collectionIdentifier = params.substring("max_treasures_".length());
            Optional<Collection> collectionOpt = this.resolveCollection(collectionIdentifier);
            return collectionOpt.<String>map(collection -> String.valueOf(this.treasureManager.getTreasureCoresInCollection(collection.getId()).size()))
               .orElse("0");
         } else if (params.startsWith("found_treasures_")) {
            String collectionIdentifier = params.substring("found_treasures_".length());
            Optional<Collection> collectionOpt = this.resolveCollection(collectionIdentifier);
            return collectionOpt.<String>map(
                  collection -> String.valueOf(this.treasureManager.countFoundInCollection(data.getFoundTreasures(), collection.getId()))
               )
               .orElse("0");
         } else if (!params.startsWith("remaining_count_") && !params.startsWith("remaining_treasures_")) {
            if (params.startsWith("leaderboard_name_") || params.startsWith("top_player_")) {
               String prefix = params.startsWith("leaderboard_name_") ? "leaderboard_name_" : "top_player_";
               String remaining = params.substring(prefix.length());
               int lastUnderscore = remaining.lastIndexOf(95);
               if (lastUnderscore != -1 && lastUnderscore < remaining.length() - 1) {
                  String collectionIdentifier = remaining.substring(0, lastUnderscore);
                  String rankStr = remaining.substring(lastUnderscore + 1);

                  try {
                     int rank = Integer.parseInt(rankStr);
                     Optional<Collection> collectionOpt = this.resolveCollection(collectionIdentifier);
                     if (collectionOpt.isPresent()) {
                        LeaderboardManager.LeaderboardEntry entry = this.leaderboardManager.getEntry(collectionOpt.get().getName(), rank);
                        return entry != null ? entry.getPlayerName() : "---";
                     }

                     return "---";
                  } catch (NumberFormatException var13) {
                     return "invalid_rank";
                  }
               }
            }

            if (params.startsWith("leaderboard_score_") || params.startsWith("top_score_")) {
               String prefix = params.startsWith("leaderboard_score_") ? "leaderboard_score_" : "top_score_";
               String remaining = params.substring(prefix.length());
               int lastUnderscore = remaining.lastIndexOf(95);
               if (lastUnderscore != -1 && lastUnderscore < remaining.length() - 1) {
                  String collectionIdentifier = remaining.substring(0, lastUnderscore);
                  String rankStr = remaining.substring(lastUnderscore + 1);

                  try {
                     int rank = Integer.parseInt(rankStr);
                     Optional<Collection> collectionOpt = this.resolveCollection(collectionIdentifier);
                     if (collectionOpt.isPresent()) {
                        LeaderboardManager.LeaderboardEntry entry = this.leaderboardManager.getEntry(collectionOpt.get().getName(), rank);
                        return entry != null ? String.valueOf(entry.getScore()) : "0";
                     }

                     return "0";
                  } catch (NumberFormatException var14) {
                     return "invalid_rank";
                  }
               }
            }

            return null;
         } else {
            String prefix = params.startsWith("remaining_count_") ? "remaining_count_" : "remaining_treasures_";
            String collectionIdentifier = params.substring(prefix.length());
            Optional<Collection> collectionOpt = this.resolveCollection(collectionIdentifier);
            if (!collectionOpt.isPresent()) {
               return "0";
            } else {
               UUID collectionId = collectionOpt.get().getId();
               List<TreasureCore> allCores = this.treasureManager.getTreasureCoresInCollection(collectionId);
               if (allCores.isEmpty()) {
                  return "0";
               } else {
                  int foundCount = this.treasureManager.countFoundInCollection(data.getFoundTreasures(), collectionId);
                  return String.valueOf(allCores.size() - foundCount);
               }
            }
         }
      }
   }
}
