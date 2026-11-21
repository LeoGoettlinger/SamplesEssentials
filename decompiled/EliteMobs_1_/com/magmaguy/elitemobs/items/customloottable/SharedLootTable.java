package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.LootItemPolicy;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import com.magmaguy.elitemobs.menus.LootMenu;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.Party;
import com.magmaguy.elitemobs.parties.PartyManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Generated;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SharedLootTable {
   private static final int DURATION_SECONDS = 60;
   private static final long NANOS_PER_SECOND = 1000000000L;
   private static final HashMap<EliteEntity, SharedLootTable> sharedLootTables = new HashMap<>();
   private static final Map<UUID, SharedLootTable> partyLootTables = new HashMap<>();
   private final List<SharedLootTable.LootRollEntry> loot = new ArrayList<>();
   private final EliteEntity eliteEntity;
   private final UUID partyId;
   private final Location fallbackDropLocation;
   private final long hardDeadlineNanos;
   private final LinkedHashMap<UUID, Player> participants = new LinkedHashMap<>();
   private final Map<UUID, LootMenu> lootMenus = new HashMap<>();
   private final HashMap<UUID, SharedLootTable.PlayerTable> playerTables = new HashMap<>();
   private long lastLootAddedNanos;
   private BukkitTask distributionTask;
   private boolean closed;

   public static void shutdown() {
      List<SharedLootTable> tables = new ArrayList<>(sharedLootTables.values());

      for (SharedLootTable table : partyLootTables.values()) {
         if (!tables.contains(table)) {
            tables.add(table);
         }
      }

      tables.forEach(SharedLootTable::closeWithoutDistribution);
      sharedLootTables.clear();
      partyLootTables.clear();
   }

   public static boolean addPartyLoot(EliteEntity eliteEntity, Player contributor, ItemStack itemStack) {
      return addPartyLoot(eliteEntity, contributor, itemStack, "", null);
   }

   public static boolean addPartyLoot(
      EliteEntity eliteEntity, Player contributor, ItemStack itemStack, String permission, Collection<Player> originatingAudience
   ) {
      if (itemStack != null && PartyManager.shouldUsePartyLoot(contributor, eliteEntity)) {
         Party party = PartyManager.getParty(contributor.getUniqueId());
         if (party == null) {
            return false;
         } else {
            List<Player> eligiblePlayers = new ArrayList<>(PartyManager.getNearbyMembers(party, eliteEntity.getLocation()));
            if (eliteEntity instanceof InstancedBossEntity instancedBossEntity) {
               eligiblePlayers.removeIf(instancedBossEntity.getLockoutPlayers()::contains);
            }

            if (eligiblePlayers.size() < 2) {
               return false;
            } else {
               if (originatingAudience != null) {
                  Set<UUID> originalIds = originatingAudience.stream().<UUID>map(OfflinePlayer::getUniqueId).collect(Collectors.toSet());
                  eligiblePlayers.removeIf(player -> !originalIds.contains(player.getUniqueId()));
               }

               eligiblePlayers.removeIf(player -> !mayReceive(itemStack, permission, player));
               if (eligiblePlayers.isEmpty()) {
                  return false;
               } else {
                  SharedLootTable table = partyLootTables.get(party.getId());
                  boolean created = table == null || table.closed;
                  if (created) {
                     table = new SharedLootTable(eliteEntity, eligiblePlayers, party.getId());
                     partyLootTables.put(party.getId(), table);
                  }

                  List<Player> newParticipants = table.addParticipants(eligiblePlayers);
                  if (!table.addLoot(itemStack, permission, eligiblePlayers)) {
                     return false;
                  } else {
                     if (!created && !newParticipants.isEmpty()) {
                        table.messagePlayersLater(newParticipants);
                     }

                     return true;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   public static void onPlayerQuit(UUID playerId) {
      HashSet<SharedLootTable> tables = new HashSet<>(sharedLootTables.values());
      tables.addAll(partyLootTables.values());
      tables.forEach(table -> table.removeParticipant(playerId));
   }

   public static void onPartyMemberLeave(UUID partyId, UUID playerId) {
      SharedLootTable table = partyLootTables.get(partyId);
      if (table != null) {
         table.removeParticipant(playerId);
      }
   }

   public SharedLootTable(EliteEntity eliteEntity) {
      this(eliteEntity, eligibleDungeonParticipants(eliteEntity), null);
      sharedLootTables.put(eliteEntity, this);
   }

   private static Collection<Player> eligibleDungeonParticipants(EliteEntity eliteEntity) {
      List<Player> eligible = new ArrayList<>(eliteEntity.getDamagers().keySet());
      if (eliteEntity instanceof InstancedBossEntity instancedBossEntity) {
         eligible.removeIf(instancedBossEntity.getLockoutPlayers()::contains);
      }

      return eligible;
   }

   private SharedLootTable(EliteEntity eliteEntity, Collection<Player> initialParticipants, UUID partyId) {
      this.eliteEntity = eliteEntity;
      this.partyId = partyId;
      this.fallbackDropLocation = eliteEntity.getLocation() == null ? null : eliteEntity.getLocation().clone();
      this.lastLootAddedNanos = System.nanoTime();
      this.hardDeadlineNanos = partyId == null ? Long.MAX_VALUE : this.lastLootAddedNanos + PartyConfig.getLootVoteMaximumLifetimeSeconds() * 1000000000L;
      this.addParticipants(initialParticipants);
      if (initialParticipants.size() > 1) {
         this.messagePlayersLater(initialParticipants);
      }

      if (initialParticipants.size() < 2) {
         this.distributionTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, this::distribute, 1L);
      } else {
         this.scheduleDistribution(1200L);
      }
   }

   public List<SharedLootTable.LootRollEntry> getLoot(Player player) {
      UUID playerId = player.getUniqueId();
      List<SharedLootTable.LootRollEntry> visibleLoot = new ArrayList<>();

      for (SharedLootTable.LootRollEntry entry : this.loot) {
         if (entry.eligiblePlayers().contains(playerId) && mayReceive(entry.itemStack(), entry.permission(), player)) {
            visibleLoot.add(entry);
         }
      }

      return visibleLoot;
   }

   public List<ItemStack> getLoot() {
      return this.loot.stream().map(SharedLootTable.LootRollEntry::itemStack).toList();
   }

   public boolean addLoot(ItemStack itemStack) {
      return this.addLoot(itemStack, "", this.participants.values());
   }

   public boolean addLoot(ItemStack itemStack, String permission) {
      return this.addLoot(itemStack, permission, this.participants.values());
   }

   public boolean addLoot(ItemStack itemStack, String permission, Collection<Player> originatingAudience) {
      if (itemStack != null && !itemStack.getType().isAir() && itemStack.getAmount() > 0 && !this.closed) {
         Set<UUID> eligible = originatingAudience.stream()
            .filter(player -> this.participants.containsKey(player.getUniqueId()))
            .filter(player -> mayReceive(itemStack, permission, player))
            .<UUID>map(OfflinePlayer::getUniqueId)
            .collect(Collectors.toUnmodifiableSet());
         if (eligible.isEmpty()) {
            return false;
         } else {
            this.loot.add(new SharedLootTable.LootRollEntry(UUID.randomUUID(), itemStack, eligible, permission));
            this.lastLootAddedNanos = System.nanoTime();
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean mayReceive(ItemStack item, String permission, Player player) {
      return (permission.isEmpty() || player.hasPermission(permission)) && LootItemPolicy.canReceive(item, player);
   }

   private List<Player> addParticipants(Collection<Player> players) {
      List<Player> added = new ArrayList<>();

      for (Player player : players) {
         if (player != null && !this.participants.containsKey(player.getUniqueId())) {
            this.participants.put(player.getUniqueId(), player);
            SharedLootTable.PlayerTable playerTable = this.getPlayerTable(player);
            LootMenu lootMenu = new LootMenu(player, this, playerTable);
            this.lootMenus.put(player.getUniqueId(), lootMenu);
            added.add(player);
         }
      }

      return added;
   }

   private void removeParticipant(UUID playerId) {
      this.participants.remove(playerId);
      this.playerTables.remove(playerId);
      LootMenu lootMenu = this.lootMenus.remove(playerId);
      if (lootMenu != null) {
         lootMenu.removeMenu();
      }
   }

   private void messagePlayersLater(Collection<Player> players) {
      List<UUID> playerIds = players.stream().<UUID>map(OfflinePlayer::getUniqueId).toList();
      Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> this.messagePlayers(playerIds), 1L);
   }

   private void messagePlayers(Collection<UUID> playerIds) {
      if (!this.closed) {
         for (UUID playerId : playerIds) {
            Player player = this.activeParticipant(playerId);
            if (player != null) {
               int lootCount = this.getLoot(player).size();
               if (lootCount != 0) {
                  Logger.sendSimpleMessage(player, CommandMessagesConfig.getLootVoteSeparator());
                  player.spigot()
                     .sendMessage(
                        new BaseComponent[]{
                           SpigotMessage.simpleMessage(CommandMessagesConfig.getLootVoteMessage()),
                           SpigotMessage.commandHoverMessage(InitializeConfig.getEmLootDisplay(), InitializeConfig.getEmLootHover(), "/em loot"),
                           SpigotMessage.simpleMessage(CommandMessagesConfig.getLootVoteMessageSuffix().replace("$count", String.valueOf(lootCount)))
                        }
                     );
                  Logger.sendSimpleMessage(player, CommandMessagesConfig.getLootVoteSeparator());
               }
            }
         }
      }
   }

   private void scheduleDistribution(long ticks) {
      if (!this.closed) {
         if (this.distributionTask != null) {
            this.distributionTask.cancel();
         }

         this.distributionTask = (new BukkitRunnable() {
            public void run() {
               SharedLootTable.this.distributionTask = null;
               if (!SharedLootTable.this.closed) {
                  long inactivityDeadline = SharedLootTable.this.lastLootAddedNanos + 60000000000L;
                  long deadline = Math.min(inactivityDeadline, SharedLootTable.this.hardDeadlineNanos);
                  long remainingNanos = deadline - System.nanoTime();
                  if (remainingNanos > 0L) {
                     long remainingTicks = Math.max(1L, (remainingNanos + 49999999L) / 50000000L);
                     SharedLootTable.this.scheduleDistribution(remainingTicks);
                  } else {
                     SharedLootTable.this.distribute();
                  }
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, ticks);
      }
   }

   private void distribute() {
      if (!this.closed) {
         this.closed = true;
         this.cancelDistributionTask();

         try {
            for (SharedLootTable.LootRollEntry entry : this.loot) {
               List<Player> eligiblePlayers = this.activeParticipants(entry.eligiblePlayers());
               eligiblePlayers.removeIf(playerx -> !mayReceive(entry.itemStack(), entry.permission(), playerx));
               List<Player> needPlayers = new ArrayList<>();

               for (Player player : eligiblePlayers) {
                  SharedLootTable.PlayerTable playerTable = this.playerTables.get(player.getUniqueId());
                  if (playerTable != null && playerTable.needs(entry.id())) {
                     needPlayers.add(player);
                  }
               }

               this.rollLoot(entry, needPlayers.isEmpty() ? eligiblePlayers : needPlayers);
            }
         } finally {
            this.removeFromRegistry();
            this.cleanupMenus();
         }
      }
   }

   private void rollLoot(SharedLootTable.LootRollEntry entry, List<Player> players) {
      ItemStack item = entry.itemStack();
      if (players.isEmpty()) {
         if (entry.permission().isEmpty() && !LootItemPolicy.hasRecipientRestriction(item)) {
            if (this.fallbackDropLocation != null && this.fallbackDropLocation.getWorld() != null) {
               World loadedWorld = Bukkit.getWorld(this.fallbackDropLocation.getWorld().getUID());
               if (loadedWorld != null) {
                  loadedWorld.dropItemNaturally(this.fallbackDropLocation, item);
               }
            }
         }
      } else {
         Player winner = players.get(ThreadLocalRandom.current().nextInt(players.size()));
         if (LootItemPolicy.shouldSoulbind(item)) {
            SoulbindEnchantment.addEnchantment(item, winner);
         }

         new EliteItemLore(item, false);
         Map<Integer, ItemStack> pendingItems = winner.getInventory().addItem(new ItemStack[]{item});
         pendingItems.values().forEach(leftover -> {
            Item dropped = winner.getWorld().dropItemNaturally(winner.getLocation(), leftover);
            dropped.setOwner(winner.getUniqueId());
         });
         String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
            ? item.getItemMeta().getDisplayName()
            : item.getType().toString().replace('_', ' ');
         String winnerMessage = ChatColorConverter.convert(
            CommandMessagesConfig.getLootWinnerMessage().replace("$player", winner.getDisplayName()).replace("$item", itemName)
         );

         for (Player participant : this.activeParticipants(null)) {
            participant.sendMessage(winnerMessage);
         }
      }
   }

   private List<Player> activeParticipants(Set<UUID> eligiblePlayerIds) {
      List<Player> active = new ArrayList<>();

      for (UUID playerId : this.participants.keySet()) {
         if (eligiblePlayerIds == null || eligiblePlayerIds.contains(playerId)) {
            Player player = this.activeParticipant(playerId);
            if (player != null) {
               active.add(player);
            }
         }
      }

      return active;
   }

   private Player activeParticipant(UUID playerId) {
      Player player = Bukkit.getPlayer(playerId);
      return player != null && player.isOnline() ? player : null;
   }

   private void removeFromRegistry() {
      if (this.partyId == null) {
         sharedLootTables.remove(this.eliteEntity, this);
      } else {
         partyLootTables.remove(this.partyId, this);
      }
   }

   private void cleanupMenus() {
      new ArrayList<>(this.lootMenus.values()).forEach(LootMenu::removeMenu);
      this.lootMenus.clear();
      this.playerTables.clear();
   }

   private void closeWithoutDistribution() {
      if (!this.closed) {
         this.closed = true;
         this.cancelDistributionTask();
         this.cleanupMenus();
      }
   }

   private void cancelDistributionTask() {
      if (this.distributionTask != null) {
         this.distributionTask.cancel();
         this.distributionTask = null;
      }
   }

   public SharedLootTable.PlayerTable getPlayerTable(Player player) {
      return this.playerTables.computeIfAbsent(player.getUniqueId(), ignored -> new SharedLootTable.PlayerTable(player));
   }

   @Generated
   public static HashMap<EliteEntity, SharedLootTable> getSharedLootTables() {
      return sharedLootTables;
   }

   public record LootRollEntry(UUID id, ItemStack itemStack, Set<UUID> eligiblePlayers, String permission) {
      public LootRollEntry(UUID id, ItemStack itemStack, Set<UUID> eligiblePlayers) {
         this(id, itemStack, eligiblePlayers, "");
      }
   }

   public class PlayerTable {
      private final Set<UUID> needEntries = new HashSet<>();
      private final Player player;

      private PlayerTable(Player player) {
         this.player = player;
      }

      public boolean needs(UUID entryId) {
         return this.needEntries.contains(entryId);
      }

      public List<ItemStack> getNeedItems() {
         return SharedLootTable.this.loot
            .stream()
            .filter(entry -> this.needEntries.contains(entry.id()))
            .map(SharedLootTable.LootRollEntry::itemStack)
            .toList();
      }

      public void setNeed(SharedLootTable.LootRollEntry entry, boolean need) {
         if (entry != null
            && entry.eligiblePlayers().contains(this.player.getUniqueId())
            && SharedLootTable.mayReceive(entry.itemStack(), entry.permission(), this.player)) {
            if (need) {
               this.needEntries.add(entry.id());
            } else {
               this.needEntries.remove(entry.id());
            }

            SharedLootTable.this.activeParticipants(null)
               .forEach(
                  participant -> participant.sendMessage(
                     ChatColorConverter.convert(
                        (need ? CommandMessagesConfig.getLootNeedMessage() : CommandMessagesConfig.getLootGreedMessage())
                           .replace("$player", this.player.getDisplayName())
                           .replace("$item", this.displayName(entry.itemStack()))
                     )
                  )
               );
         }
      }

      public void addNeed(ItemStack itemStack) {
         SharedLootTable.this.loot
            .stream()
            .filter(entry -> entry.eligiblePlayers().contains(this.player.getUniqueId()))
            .filter(entry -> entry.itemStack().isSimilar(itemStack))
            .filter(entry -> !this.needs(entry.id()))
            .findFirst()
            .ifPresent(entry -> this.setNeed(entry, true));
      }

      public void removeNeed(ItemStack itemStack) {
         SharedLootTable.this.loot
            .stream()
            .filter(entry -> this.needs(entry.id()))
            .filter(entry -> entry.itemStack().isSimilar(itemStack))
            .findFirst()
            .ifPresent(entry -> this.setNeed(entry, false));
      }

      private String displayName(ItemStack itemStack) {
         return itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()
            ? itemStack.getItemMeta().getDisplayName()
            : itemStack.getType().toString().replace('_', ' ');
      }
   }
}
