package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardType;
import de.theredend2000.advancedhunt.util.HexColor;
import de.theredend2000.advancedhunt.util.ItemSerializer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardManager {
   private final Main plugin;
   private final Random random = new Random();
   private final Map<String, ItemStack> itemCache = new ConcurrentHashMap<>();
   private volatile List<String> cachedCommandBlacklist = Collections.emptyList();
   private volatile List<Pattern> cachedCommandBlacklistPatterns = Collections.emptyList();

   public RewardManager(Main plugin) {
      this.plugin = plugin;
   }

   public void giveRewards(Player player, List<Reward> rewards, Collection collection) {
      if (rewards != null && !rewards.isEmpty()) {
         RewardManager.CollectionStats stats = this.calculateCollectionStats(player, collection);

         for (Reward reward : rewards) {
            if (!(this.random.nextDouble() * 100.0 > reward.getChance())) {
               String itemName = this.processReward(player, reward, stats);
               this.sendCustomMessages(player, reward, stats, itemName);
            }
         }
      }
   }

   private String processReward(Player player, Reward reward, RewardManager.CollectionStats stats) {
      switch (reward.getType()) {
         case COMMAND:
            this.executeCommand(player, reward, stats);
            return null;
         case ITEM:
            return this.giveItem(player, reward);
         case CHAT_MESSAGE:
            this.sendChatMessage(player, reward, stats);
            return null;
         case CHAT_MESSAGE_BROADCAST:
            this.broadcastMessage(player, reward, stats);
            return null;
         default:
            this.plugin.getLogger().warning("Unknown reward type: " + reward.getType());
            return null;
      }
   }

   private void executeCommand(Player player, Reward reward, RewardManager.CollectionStats stats) {
      String cmd = this.replacePlaceholders(reward.getValue(), player.getName(), stats, 0.0, null);
      if (this.plugin.getConfig().getBoolean("rewards.block-execution", true) && this.isCommandBlacklisted(cmd)) {
         this.plugin.getLogger().warning("Blocked execution of blacklisted command reward: " + cmd);
      } else {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
      }
   }

   private String giveItem(Player player, Reward reward) {
      ItemStack cachedItem = this.itemCache.computeIfAbsent(reward.getValue(), value -> {
         ItemStack itemx = ItemSerializer.deserialize(value);
         if (itemx == null) {
            this.plugin.getLogger().warning("Failed to deserialize item reward: " + value);
         }

         return itemx;
      });
      if (cachedItem == null) {
         return null;
      } else {
         ItemStack item = cachedItem.clone();
         HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{item});
         if (!leftover.isEmpty()) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.instance_dropped"));

            for (ItemStack drop : leftover.values()) {
               player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
         }

         return this.getItemDisplayName(cachedItem);
      }
   }

   private void sendChatMessage(Player player, Reward reward, RewardManager.CollectionStats stats) {
      String message = this.replacePlaceholders(reward.getValue(), player.getName(), stats, reward.getChance(), null);
      player.sendMessage(HexColor.color(message, '&'));
   }

   private void broadcastMessage(Player player, Reward reward, RewardManager.CollectionStats stats) {
      String message = this.replacePlaceholders(reward.getValue(), player.getName(), stats, reward.getChance(), null);
      Bukkit.broadcastMessage(HexColor.color(message, '&'));
   }

   private void sendCustomMessages(Player player, Reward reward, RewardManager.CollectionStats stats, String itemName) {
      if (reward.getType() == RewardType.COMMAND || reward.getType() == RewardType.ITEM) {
         String finalItemName = reward.getType() == RewardType.ITEM ? itemName : null;
         if (reward.getMessage() != null && !reward.getMessage().isEmpty()) {
            String message = this.replacePlaceholders(reward.getMessage(), player.getName(), stats, reward.getChance(), finalItemName);
            player.sendMessage(HexColor.color(message, '&'));
         }

         if (reward.getBroadcast() != null && !reward.getBroadcast().isEmpty()) {
            String broadcast = this.replacePlaceholders(reward.getBroadcast(), player.getName(), stats, reward.getChance(), finalItemName);
            Bukkit.broadcastMessage(HexColor.color(broadcast, '&'));
         }
      }
   }

   private RewardManager.CollectionStats calculateCollectionStats(Player player, Collection collection) {
      if (collection == null) {
         return new RewardManager.CollectionStats(0, 0, 0);
      } else {
         try {
            int maxTreasures = this.plugin.getTreasureManager().getTreasureCoresInCollection(collection.getId()).size();
            PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            int foundTreasures = this.plugin.getTreasureManager().countFoundInCollection(playerData.getFoundTreasures(), collection.getId());
            int remainingTreasures = Math.max(0, maxTreasures - foundTreasures);
            return new RewardManager.CollectionStats(foundTreasures, maxTreasures, remainingTreasures);
         } catch (Exception var7) {
            this.plugin.getLogger().warning("Failed to calculate collection stats: " + var7.getMessage());
            return new RewardManager.CollectionStats(0, 0, 0);
         }
      }
   }

   private List<Pattern> getCommandBlacklistPatterns() {
      List<String> blacklist = this.plugin.getConfig().getStringList("rewards.command-blacklist");
      if (blacklist != null && !blacklist.isEmpty()) {
         if (blacklist.equals(this.cachedCommandBlacklist)) {
            return this.cachedCommandBlacklistPatterns;
         } else {
            List<Pattern> compiled = new ArrayList<>(blacklist.size());

            for (String regex : blacklist) {
               if (regex != null && !regex.trim().isEmpty()) {
                  try {
                     compiled.add(Pattern.compile(regex, 2));
                  } catch (Exception var6) {
                     this.plugin.getLogger().warning("Invalid regex pattern in command-blacklist: " + regex);
                  }
               }
            }

            this.cachedCommandBlacklist = Collections.unmodifiableList(new ArrayList<>(blacklist));
            this.cachedCommandBlacklistPatterns = Collections.unmodifiableList(new ArrayList<>(compiled));
            return this.cachedCommandBlacklistPatterns;
         }
      } else {
         this.cachedCommandBlacklist = Collections.emptyList();
         this.cachedCommandBlacklistPatterns = Collections.emptyList();
         return this.cachedCommandBlacklistPatterns;
      }
   }

   private String replacePlaceholders(String message, String playerName, RewardManager.CollectionStats stats, double chance, String itemName) {
      String prefix = this.plugin.getConfig().getString("prefix", "[AdvancedHunt]");
      String result = message.replace("%player%", playerName)
         .replace("%prefix%", prefix)
         .replace("%found_treasures%", String.valueOf(stats.foundTreasures))
         .replace("%max_treasures%", String.valueOf(stats.maxTreasures))
         .replace("%remaining_treasures%", String.valueOf(stats.remainingTreasures))
         .replace("%chance%", this.formatChance(chance))
         .replace("%item%", itemName != null ? itemName : "")
         .replace("\\n", System.lineSeparator());
      result = HexColor.color(result, '&');
      return result.replace("&", "§");
   }

   private String getItemDisplayName(ItemStack item) {
      if (item == null) {
         return "Unknown Item";
      } else {
         if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
               return ChatColor.stripColor(meta.getDisplayName());
            }
         }

         String materialName = item.getType().name().toLowerCase().replace("_", " ");
         return Character.toUpperCase(materialName.charAt(0)) + materialName.substring(1);
      }
   }

   private String formatChance(double chance) {
      return chance == (int)chance ? String.valueOf((int)chance) : String.format("%.3f", chance);
   }

   public boolean isCommandBlacklisted(String command) {
      List<Pattern> patterns = this.getCommandBlacklistPatterns();
      if (patterns.isEmpty()) {
         return false;
      } else {
         String checkCmd = command.startsWith("/") ? command.substring(1) : command;

         for (Pattern pattern : patterns) {
            if (pattern.matcher(checkCmd).find()) {
               return true;
            }
         }

         return false;
      }
   }

   public void clearItemCache() {
      this.itemCache.clear();
      this.plugin.getLogger().info("Item reward cache cleared (" + this.itemCache.size() + " items)");
   }

   public int getCacheSize() {
      return this.itemCache.size();
   }

   private static class CollectionStats {
      final int foundTreasures;
      final int maxTreasures;
      final int remainingTreasures;

      CollectionStats(int foundTreasures, int maxTreasures, int remainingTreasures) {
         this.foundTreasures = foundTreasures;
         this.maxTreasures = maxTreasures;
         this.remainingTreasures = remainingTreasures;
      }
   }
}
