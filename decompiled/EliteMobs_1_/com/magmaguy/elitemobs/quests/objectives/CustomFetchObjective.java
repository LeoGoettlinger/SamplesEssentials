package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.api.QuestRewardEvent;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class CustomFetchObjective extends Objective {
   private static final long serialVersionUID = -3846169815392092956L;
   private static final Map<UUID, CustomFetchObjective.PendingRefresh> pendingRefreshes = new HashMap<>();
   private final String key;
   private boolean readyToPickUp = false;
   private boolean requireItemTurnIn;

   public CustomFetchObjective(int targetAmount, String objectiveName, String customItemFilename) {
      super(targetAmount, objectiveName);
      this.key = customItemFilename;
   }

   private static void checkEvent(@NotNull Player player, @NotNull ItemStack itemStack) {
      if (PlayerData.isInMemory(player)) {
         for (Quest quest : PlayerData.getQuests(player.getUniqueId())) {
            for (Objective objective : quest.getQuestObjectives().getObjectives()) {
               if (objective instanceof CustomFetchObjective) {
                  ((CustomFetchObjective)objective).checkItem(player, itemStack, quest.getQuestObjectives());
               }
            }
         }
      }
   }

   private void checkItem(Player player, @NotNull ItemStack itemStack, QuestObjectives questObjectives) {
      if (ItemTagger.hasKey(itemStack, this.key)) {
         this.progressNonlinearObjective(questObjectives, player);
      }
   }

   public static boolean prepareTurnIn(QuestObjectives objectives, Player player, boolean consume) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Quest turn-in must run on the server thread");
      } else {
         ItemStack[] planned = player.getInventory().getContents();

         for (int slot = 0; slot < planned.length; slot++) {
            if (planned[slot] != null) {
               planned[slot] = planned[slot].clone();
            }
         }

         boolean hasFetch = false;

         for (Objective objective : objectives.getObjectives()) {
            if (objective instanceof CustomFetchObjective fetch) {
               hasFetch = true;
               fetch.fullUpdate(player);
               fetch.objectiveCompleted = fetch.currentAmount >= fetch.targetAmount;
               int remaining = fetch.targetAmount;

               for (int slotx = 0; slotx < planned.length && remaining > 0; slotx++) {
                  ItemStack item = planned[slotx];
                  if (ItemTagger.hasKey(item, fetch.key)) {
                     int taken = Math.min(remaining, item.getAmount());
                     remaining -= taken;
                     item.setAmount(item.getAmount() - taken);
                     if (item.getAmount() == 0) {
                        planned[slotx] = null;
                     }
                  }
               }

               if (remaining > 0) {
                  return false;
               }
            }
         }

         if (consume && hasFetch) {
            player.getInventory().setContents(planned);
         }

         return true;
      }
   }

   @Override
   public void progressNonlinearObjective(QuestObjectives questObjectives, Player player) {
      if (PlayerData.isInMemory(player) && !questObjectives.isTurnedIn()) {
         CustomFetchObjective.PendingRefresh pending = pendingRefreshes.get(player.getUniqueId());
         if (pending == null) {
            pending = new CustomFetchObjective.PendingRefresh(player, PlayerData.getPlayerData(player.getUniqueId()));
            pendingRefreshes.put(player.getUniqueId(), pending);
            pending.task = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> refresh(pending), 1L);
         }

         pending.objectives.computeIfAbsent(questObjectives, ignored -> new HashSet<>()).add(this);
      }
   }

   public static void cancelRefresh(UUID playerId) {
      CustomFetchObjective.PendingRefresh pending = pendingRefreshes.remove(playerId);
      if (pending != null && pending.task != null) {
         pending.task.cancel();
      }
   }

   private static void refresh(CustomFetchObjective.PendingRefresh pending) {
      Player player = pending.player;
      if (pendingRefreshes.remove(player.getUniqueId(), pending)
         && player.isOnline()
         && Bukkit.getPlayer(player.getUniqueId()) == player
         && PlayerData.getPlayerData(player.getUniqueId()) == pending.owner) {
         List<Quest> active = PlayerData.getQuests(player.getUniqueId());
         pending.objectives.keySet().removeIf(objectivesx -> objectivesx.isTurnedIn() || !active.contains(objectivesx.getQuest()));
         Map<NamespacedKey, Integer> counts = new HashMap<>();

         for (Set<CustomFetchObjective> objectives : pending.objectives.values()) {
            for (CustomFetchObjective objective : objectives) {
               counts.put(new NamespacedKey(MetadataHandler.PLUGIN, objective.key), 0);
            }
         }

         if (!counts.isEmpty()) {
            for (ItemStack item : player.getInventory().getContents()) {
               if (item != null && item.hasItemMeta()) {
                  PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();

                  for (NamespacedKey key : data.getKeys()) {
                     if (counts.containsKey(key) && data.has(key, PersistentDataType.STRING)) {
                        counts.computeIfPresent(key, (ignored, count) -> count + item.getAmount());
                     }
                  }
               }
            }

            List<QuestProgressionEvent> changes = new ArrayList<>();
            pending.objectives.forEach((questObjectives, objectivesx) -> {
               for (CustomFetchObjective objective : objectivesx) {
                  int amount = counts.get(new NamespacedKey(MetadataHandler.PLUGIN, objective.key));
                  boolean completed = amount >= objective.targetAmount;
                  if (objective.currentAmount != amount || objective.objectiveCompleted != completed) {
                     objective.currentAmount = amount;
                     objective.objectiveCompleted = completed;
                     changes.add(new QuestProgressionEvent(player, questObjectives.getQuest(), objective));
                  }
               }
            });
            QuestProgressionEvent.fireBatch(player, changes);
         }
      }
   }

   private void fullUpdate(Player player) {
      super.currentAmount = 0;

      for (ItemStack itemStack : player.getInventory()) {
         if (ItemTagger.hasKey(itemStack, this.key)) {
            super.currentAmount = super.currentAmount + itemStack.getAmount();
         }
      }
   }

   @Generated
   public String getKey() {
      return this.key;
   }

   @Generated
   public boolean isReadyToPickUp() {
      return this.readyToPickUp;
   }

   @Generated
   public void setReadyToPickUp(boolean readyToPickUp) {
      this.readyToPickUp = readyToPickUp;
   }

   @Generated
   public boolean isRequireItemTurnIn() {
      return this.requireItemTurnIn;
   }

   public static class CustomFetchObjectiveEvents implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onItemDrop(PlayerDropItemEvent event) {
         CustomFetchObjective.checkEvent(event.getPlayer(), event.getItemDrop().getItemStack());
      }

      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onItemPickup(EntityPickupItemEvent event) {
         if (event.getEntity().getType() == EntityType.PLAYER) {
            CustomFetchObjective.checkEvent((Player)event.getEntity(), event.getItem().getItemStack());
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onQuestAcceptEvent(QuestAcceptEvent event) {
         for (Objective objective : event.getQuest().getQuestObjectives().getObjectives()) {
            if (objective instanceof CustomFetchObjective) {
               objective.progressNonlinearObjective(event.getQuest().getQuestObjectives(), event.getPlayer());
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onQuestCompleteEvent(QuestCompleteEvent event) {
         if (!CustomFetchObjective.prepareTurnIn(event.getQuest().getQuestObjectives(), event.getPlayer(), false)) {
            event.setCancelled(true);
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onQuestRewardEvent(QuestRewardEvent event) {
         for (Quest quest : PlayerData.getQuests(event.getPlayer().getUniqueId())) {
            for (Objective objective : quest.getQuestObjectives().getObjectives()) {
               if (objective instanceof CustomFetchObjective) {
                  objective.progressNonlinearObjective(quest.getQuestObjectives(), event.getPlayer());
               }
            }
         }
      }
   }

   private static final class PendingRefresh {
      private final Player player;
      private final PlayerData owner;
      private final Map<QuestObjectives, Set<CustomFetchObjective>> objectives = new LinkedHashMap<>();
      private BukkitTask task;

      private PendingRefresh(Player player, PlayerData owner) {
         this.player = player;
         this.owner = owner;
      }
   }
}
