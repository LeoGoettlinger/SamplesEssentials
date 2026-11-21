package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerDataLoadedEvent;
import com.magmaguy.elitemobs.dungeons.DungeonBossLockout;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestLockout;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.ObjectSerializer;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerData {
   private static final String DATABASE_NAME = "player_data.db";
   private static final String PLAYER_DATA_TABLE_NAME = "PlayerData";
   private static final ConcurrentHashMap<UUID, PlayerData> playerDataHashMap = new ConcurrentHashMap<>();
   private static final Map<UUID, PlayerData> loadingPlayers = new ConcurrentHashMap<>();
   private static final Map<UUID, List<PlayerData.DeferredDatabaseValue>> deferredDatabaseValues = new HashMap<>();
   private long currencyCents;
   private int score;
   private int kills;
   private int highestLevelKilled;
   private int deaths;
   private int questsCompleted;
   private int dungeonsCompleted;
   private List<Quest> quests = new ArrayList<>();
   private PlayerQuestCooldowns playerQuestCooldowns = null;
   private Location backTeleportLocation;
   private boolean useBookMenus;
   private boolean dismissEMStatusScreenMessage;
   private MatchInstance matchInstance = null;
   private DungeonBossLockout dungeonBossLockout = null;
   private QuestLockout questLockout = null;
   private long skillXP_ARMOR = 0L;
   private long skillXP_SWORDS = 0L;
   private long skillXP_AXES = 0L;
   private long skillXP_BOWS = 0L;
   private long skillXP_CROSSBOWS = 0L;
   private long skillXP_TRIDENTS = 0L;
   private long skillXP_HOES = 0L;
   private long skillXP_MACES = 0L;
   private long skillXP_SPEARS = 0L;
   private long skillXP_STAVES = 0L;
   private long skillXP_WANDS = 0L;
   private String skillBonusSelections = "{}";
   private long gamblingDebtCents = 0L;
   private volatile boolean databaseDataLoaded = false;

   public void setSkillXP_ARMOR(long xp) {
      this.skillXP_ARMOR = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_SWORDS(long xp) {
      this.skillXP_SWORDS = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_AXES(long xp) {
      this.skillXP_AXES = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_BOWS(long xp) {
      this.skillXP_BOWS = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_CROSSBOWS(long xp) {
      this.skillXP_CROSSBOWS = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_TRIDENTS(long xp) {
      this.skillXP_TRIDENTS = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_HOES(long xp) {
      this.skillXP_HOES = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_MACES(long xp) {
      this.skillXP_MACES = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_SPEARS(long xp) {
      this.skillXP_SPEARS = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_STAVES(long xp) {
      this.skillXP_STAVES = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public void setSkillXP_WANDS(long xp) {
      this.skillXP_WANDS = xp;
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public PlayerData(final UUID uuid) {
      final Player player = Bukkit.getPlayer(uuid);
      if (player == null) {
         Logger.warn("EliteMobs did not initialize player data for uuid " + uuid + " because Minecraft does not recognize this as a valid player!");
      } else {
         PermissionAttachment permissionAttachment = player.addAttachment(MetadataHandler.PLUGIN);
         permissionAttachment.setPermission("elitequest.*", false);
         final String playerName = player.getName();
         if (loadingPlayers.putIfAbsent(uuid, this) == null) {
            (new BukkitRunnable() {
               public void run() {
                  try {
                     boolean exists = PlayerDataRepository.readPlayer(uuid, resultSet -> PlayerData.this.readExistingData(uuid, resultSet));
                     if (!exists) {
                        PlayerData.this.writeNewData(uuid, playerName);
                     }

                     while (true) {
                        List<PlayerData.DeferredDatabaseValue> deferred;
                        synchronized (PlayerDataRepository.stateMonitor()) {
                           if (PlayerData.loadingPlayers.get(uuid) != PlayerData.this) {
                              return;
                           }

                           deferred = PlayerData.deferredDatabaseValues.remove(uuid);
                           if (deferred == null || deferred.isEmpty()) {
                              PlayerData.this.databaseDataLoaded = true;
                              PlayerData.playerDataHashMap.put(uuid, PlayerData.this);
                              CombatLevelCalculator.invalidateOnlineCombatLevel();
                              PlayerData.loadingPlayers.remove(uuid, PlayerData.this);
                              break;
                           }
                        }

                        try {
                           for (PlayerData.DeferredDatabaseValue value : deferred) {
                              PlayerDataRepository.updateNow(uuid, value.values());
                           }
                        } catch (RuntimeException var10) {
                           synchronized (PlayerDataRepository.stateMonitor()) {
                              List<PlayerData.DeferredDatabaseValue> newer = PlayerData.deferredDatabaseValues.remove(uuid);
                              if (newer != null) {
                                 deferred.addAll(newer);
                              }

                              PlayerData.deferredDatabaseValues.put(uuid, deferred);
                           }

                           throw var10;
                        }

                        PlayerDataRepository.readPlayer(uuid, resultSet -> PlayerData.this.readExistingData(uuid, resultSet));
                     }
                  } catch (Exception var12) {
                     Logger.warn("Something went wrong while generating a new player entry. This is bad! Tell the dev.");
                     Logger.warn(var12.getClass().getName() + ": " + var12.getMessage());
                     if (PlayerData.playerDataHashMap.remove(uuid, PlayerData.this)) {
                        CombatLevelCalculator.invalidateOnlineCombatLevel();
                     }

                     synchronized (PlayerDataRepository.stateMonitor()) {
                        PlayerData.loadingPlayers.remove(uuid, PlayerData.this);
                     }
                  }

                  if (PlayerData.this.databaseDataLoaded) {
                     PlayerData.this.scheduleBukkitInitialization(uuid, player);
                  }
               }
            }).runTaskAsynchronously(MetadataHandler.PLUGIN);
         }
      }
   }

   public static PlayerData getPlayerData(UUID player) {
      return playerDataHashMap.get(player);
   }

   public static void updateQuestStatus(UUID uuid) {
      List<Quest> playerQuests = getQuests(uuid);

      try {
         setDatabaseValue(uuid, "QuestStatus", ObjectSerializer.toString((ArrayList)playerQuests));
         if (playerDataHashMap.containsKey(uuid)) {
            playerDataHashMap.get(uuid).quests = playerQuests;
         }
      } catch (Exception var3) {
         Logger.warn("Failed to serialize player quest data!");
         var3.printStackTrace();
      }
   }

   public static void clearPlayerData(UUID uuid) {
      Quest.releasePlayerSession(uuid);
      playerDataHashMap.remove(uuid);
      CombatLevelCalculator.invalidateOnlineCombatLevel();
   }

   public static boolean isInMemory(Player player) {
      return playerDataHashMap.containsKey(player.getUniqueId());
   }

   public static boolean isInMemory(UUID uuid) {
      return playerDataHashMap.containsKey(uuid);
   }

   public static boolean isDataLoaded(UUID uuid) {
      PlayerData playerData = playerDataHashMap.get(uuid);
      return playerData != null && playerData.databaseDataLoaded;
   }

   public static String getDisplayName(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseString(uuid, "DisplayName") : Bukkit.getPlayer(uuid).getCustomName();
   }

   public static void setDisplayName(UUID uuid, String displayName) {
      setDatabaseValue(uuid, "DisplayName", displayName);
   }

   public static double getCurrency(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseLong(uuid, "CurrencyCents").longValue() / 100.0 : playerDataHashMap.get(uuid).currencyCents / 100.0;
   }

   public static double getCurrency(UUID uuid, boolean databaseAccess) {
      if (!isInMemory(uuid)) {
         return databaseAccess ? getDatabaseLong(uuid, "CurrencyCents").longValue() / 100.0 : 0.0;
      } else {
         return playerDataHashMap.get(uuid).currencyCents / 100.0;
      }
   }

   public static void setCurrency(UUID uuid, double currency) {
      long cents = Math.round(currency * 100.0);
      setDatabaseValues(uuid, Map.of("CurrencyCents", cents, "CurrencyV2", cents / 100.0));
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).currencyCents = cents;
      }
   }

   public static void setCurrencyAndGamblingDebt(UUID uuid, double currency, double debt) {
      long currencyValue = Math.round(currency * 100.0);
      long debtValue = Math.round(Math.max(0.0, Math.min(500.0, debt)) * 100.0);
      setDatabaseValues(
         uuid, Map.of("CurrencyCents", currencyValue, "CurrencyV2", currencyValue / 100.0, "GamblingDebtCents", debtValue, "GamblingDebt", debtValue / 100.0)
      );
      PlayerData loaded = playerDataHashMap.get(uuid);
      if (loaded != null) {
         loaded.currencyCents = currencyValue;
         loaded.gamblingDebtCents = debtValue;
      }
   }

   public static List<Quest> getQuests(UUID uuid) {
      try {
         if (!isInMemory(uuid)) {
            return (List<Quest>)ObjectSerializer.fromString((String)getDatabaseBlob(uuid, "QuestStatus"));
         } else if (playerDataHashMap.get(uuid) == null) {
            return new ArrayList<>();
         } else {
            return (List<Quest>)(playerDataHashMap.get(uuid).quests == null ? new ArrayList<>() : playerDataHashMap.get(uuid).quests);
         }
      } catch (Exception var2) {
         return new ArrayList<>();
      }
   }

   public static Quest getQuest(UUID uuid, String questID) {
      try {
         UUID questUUID = UUID.fromString(questID);
         return getQuest(uuid, questUUID);
      } catch (Exception var3) {
         Logger.warn("Failed to convert quest ID from command into a valid UUID format!");
         return null;
      }
   }

   public static Quest getQuest(UUID uuid, UUID questUUID) {
      List<Quest> questList = null;

      try {
         if (!isInMemory(uuid)) {
            questList = (List<Quest>)ObjectSerializer.fromString((String)getDatabaseBlob(uuid, "QuestStatus"));
         } else {
            questList = playerDataHashMap.get(uuid).quests;
         }
      } catch (Exception var5) {
         return null;
      }

      for (Quest iteratedQuest : questList) {
         if (iteratedQuest.getQuestID().equals(questUUID)) {
            return iteratedQuest;
         }
      }

      return null;
   }

   public static void resetQuests(UUID uuid) {
      Quest.releasePlayerSession(uuid);
      ArrayList<Quest> resetQuests = new ArrayList<>();

      try {
         setDatabaseValue(uuid, "QuestStatus", ObjectSerializer.toString(resetQuests));
         if (playerDataHashMap.containsKey(uuid)) {
            playerDataHashMap.get(uuid).quests = resetQuests;
         }
      } catch (Exception var3) {
         Logger.warn("Failed to reset player quest data!");
         var3.printStackTrace();
      }
   }

   public static void removeQuest(UUID uuid, Quest quest) {
      if (quest != null) {
         if (quest instanceof CustomQuest customQuest) {
            customQuest.releaseTemporaryPermissions();
         }

         playerDataHashMap.get(uuid).quests.removeIf(iteratedQuest -> iteratedQuest.getQuestID().equals(quest.getQuestID()));
         updateQuestStatus(uuid);
      }
   }

   public static void addQuest(UUID uuid, Quest quest) {
      if (!playerDataHashMap.get(uuid).quests.stream().anyMatch(current -> current.getQuestID().equals(quest.getQuestID()))) {
         playerDataHashMap.get(uuid).quests.add(quest);
         updateQuestStatus(uuid);
      }
   }

   public static void updateQuestStatus(UUID uuid, Quest quest) {
      updateQuestStatus(uuid);
   }

   @Nullable
   public static PlayerQuestCooldowns getPlayerQuestCooldowns(UUID uuid) {
      try {
         if (!isInMemory(uuid)) {
            return (PlayerQuestCooldowns)ObjectSerializer.fromString((String)getDatabaseBlob(uuid, "PlayerQuestCooldowns"));
         } else if (playerDataHashMap.get(uuid) == null) {
            return PlayerQuestCooldowns.initializePlayer();
         } else {
            return playerDataHashMap.get(uuid).playerQuestCooldowns == null
               ? PlayerQuestCooldowns.initializePlayer()
               : playerDataHashMap.get(uuid).playerQuestCooldowns;
         }
      } catch (Exception var2) {
         return null;
      }
   }

   public static void resetPlayerQuestCooldowns(UUID uuid) {
      updatePlayerQuestCooldowns(uuid, PlayerQuestCooldowns.initializePlayer());
   }

   public static void updatePlayerQuestCooldowns(UUID uuid, PlayerQuestCooldowns playerQuestCooldowns) {
      try {
         setDatabaseValue(uuid, "PlayerQuestCooldowns", ObjectSerializer.toString(playerQuestCooldowns));
         if (playerDataHashMap.containsKey(uuid)) {
            playerDataHashMap.get(uuid).playerQuestCooldowns = playerQuestCooldowns;
         }
      } catch (Exception var3) {
         Logger.warn("Failed to register player quest cooldowns!");
         var3.printStackTrace();
      }
   }

   @Nullable
   public static DungeonBossLockout getDungeonBossLockout(UUID uuid) {
      try {
         if (!isInMemory(uuid)) {
            return (DungeonBossLockout)ObjectSerializer.fromString((String)getDatabaseBlob(uuid, "DungeonBossLockouts"));
         } else if (playerDataHashMap.get(uuid) == null) {
            return new DungeonBossLockout();
         } else {
            return playerDataHashMap.get(uuid).dungeonBossLockout == null ? new DungeonBossLockout() : playerDataHashMap.get(uuid).dungeonBossLockout;
         }
      } catch (Exception var2) {
         return new DungeonBossLockout();
      }
   }

   public static void updateDungeonBossLockout(UUID uuid, DungeonBossLockout dungeonBossLockout) {
      try {
         dungeonBossLockout.cleanupExpiredLockouts();
         setDatabaseValue(uuid, "DungeonBossLockouts", ObjectSerializer.toString(dungeonBossLockout));
         if (playerDataHashMap.containsKey(uuid)) {
            playerDataHashMap.get(uuid).dungeonBossLockout = dungeonBossLockout;
         }
      } catch (Exception var3) {
         Logger.warn("Failed to update dungeon boss lockouts!");
         var3.printStackTrace();
      }
   }

   @Nullable
   public static QuestLockout getQuestLockout(UUID uuid) {
      try {
         if (!isInMemory(uuid)) {
            return (QuestLockout)ObjectSerializer.fromString((String)getDatabaseBlob(uuid, "QuestLockouts"));
         } else if (playerDataHashMap.get(uuid) == null) {
            return new QuestLockout();
         } else {
            return playerDataHashMap.get(uuid).questLockout == null ? new QuestLockout() : playerDataHashMap.get(uuid).questLockout;
         }
      } catch (Exception var2) {
         return new QuestLockout();
      }
   }

   public static void resetQuestLockouts(UUID uuid) {
      updateQuestLockout(uuid, new QuestLockout());
   }

   public static void updateQuestLockout(UUID uuid, QuestLockout questLockout) {
      try {
         questLockout.cleanupExpiredLockouts();
         setDatabaseValue(uuid, "QuestLockouts", ObjectSerializer.toString(questLockout));
         if (playerDataHashMap.containsKey(uuid)) {
            playerDataHashMap.get(uuid).questLockout = questLockout;
         }
      } catch (Exception var3) {
         Logger.warn("Failed to update quest lockouts!");
         var3.printStackTrace();
      }
   }

   public static void setDatabaseValue(UUID uuid, String key, Object value) {
      setDatabaseValues(uuid, Collections.singletonMap(key, value));
   }

   private static void setDatabaseValues(UUID uuid, Map<String, Object> values) {
      if (values.get("Score") instanceof Number number) {
         PlayerDataRepository.updateCachedScore(uuid, number.intValue());
      }

      Map<String, Object> snapshot = Collections.unmodifiableMap(new LinkedHashMap<>(values));
      synchronized (PlayerDataRepository.stateMonitor()) {
         if (loadingPlayers.containsKey(uuid)) {
            deferredDatabaseValues.computeIfAbsent(uuid, ignored -> new ArrayList<>()).add(new PlayerData.DeferredDatabaseValue(snapshot));
            return;
         }
      }

      PlayerDataRepository.enqueueUpdate(uuid, snapshot);
   }

   private static Object getDatabaseBlob(UUID uuid, String value) {
      byte[] bytes = (byte[])PlayerDataRepository.getBlob(uuid, value);
      return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
   }

   public static int getScore(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseInteger(uuid, "Score") : playerDataHashMap.get(uuid).score;
   }

   public static void incrementScore(UUID uuid, int levelToIncrement) {
      setDatabaseValue(uuid, "Score", getScore(uuid) + levelToIncrement * 2);
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).score += levelToIncrement * 2;
      }
   }

   public static void decrementScore(UUID uuid) {
      setDatabaseValue(uuid, "Score", getScore(uuid) - 100);
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).score -= 100;
      }
   }

   public static int getKills(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseInteger(uuid, "Kills") : playerDataHashMap.get(uuid).kills;
   }

   public static void incrementKills(UUID uuid) {
      setDatabaseValue(uuid, "Kills", getKills(uuid) + 1);
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).kills++;
      }
   }

   public static int getHighestLevelKilled(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseInteger(uuid, "HighestLevelKilled") : playerDataHashMap.get(uuid).highestLevelKilled;
   }

   public static void setHighestLevelKilled(UUID uuid, int tentativeNewLevel) {
      if (tentativeNewLevel >= getHighestLevelKilled(uuid)) {
         setDatabaseValue(uuid, "HighestLevelKilled", tentativeNewLevel);
         if (playerDataHashMap.containsKey(uuid)) {
            playerDataHashMap.get(uuid).highestLevelKilled = tentativeNewLevel;
         }
      }
   }

   public static int getDeaths(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseInteger(uuid, "Deaths") : playerDataHashMap.get(uuid).deaths;
   }

   public static void incrementDeaths(UUID uuid) {
      setDatabaseValue(uuid, "Deaths", getDeaths(uuid) + 1);
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).deaths++;
      }
   }

   public static int getQuestsCompleted(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseInteger(uuid, "QuestsCompleted") : playerDataHashMap.get(uuid).questsCompleted;
   }

   public static void incrementQuestsCompleted(UUID uuid) {
      setDatabaseValue(uuid, "QuestsCompleted", getQuestsCompleted(uuid) + 1);
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).questsCompleted++;
      }
   }

   public static int getDungeonsCompleted(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseInteger(uuid, "DungeonsCompleted") : playerDataHashMap.get(uuid).dungeonsCompleted;
   }

   public static void incrementDungeonsCompleted(UUID uuid) {
      setDatabaseValue(uuid, "DungeonsCompleted", getDungeonsCompleted(uuid) + 1);
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).dungeonsCompleted++;
      }
   }

   public static PlayerData.ScoreRank getScoreRank(UUID uuid) {
      return !isDataLoaded(uuid) ? PlayerData.ScoreRank.unavailable() : PlayerDataRepository.getScoreRank(uuid);
   }

   public static Location getBackTeleportLocation(Player player) {
      return !isInMemory(player.getUniqueId())
         ? ConfigurationLocation.serialize(getDatabaseString(player.getUniqueId(), "BackTeleportLocation"))
         : playerDataHashMap.get(player.getUniqueId()).backTeleportLocation;
   }

   public static void setBackTeleportLocation(Player player, Location location) {
      setDatabaseValue(player.getUniqueId(), "BackTeleportLocation", ConfigurationLocation.deserialize(location));
      playerDataHashMap.get(player.getUniqueId()).backTeleportLocation = location;
   }

   public static boolean getUseBookMenus(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseBoolean(uuid, "UseBookMenus") : playerDataHashMap.get(uuid).useBookMenus;
   }

   public static void setUseBookMenus(Player player, boolean use) {
      setDatabaseValue(player.getUniqueId(), "UseBookMenus", use);
      playerDataHashMap.get(player.getUniqueId()).useBookMenus = use;
   }

   public static boolean getDismissEMStatusScreenMessage(UUID uuid) {
      return !isInMemory(uuid) ? getDatabaseBoolean(uuid, "DismissEMStatusScreenMessage") : playerDataHashMap.get(uuid).dismissEMStatusScreenMessage;
   }

   public static void setDismissEMStatusScreenMessage(Player player, boolean use) {
      setDatabaseValue(player.getUniqueId(), "DismissEMStatusScreenMessage", use);
      playerDataHashMap.get(player.getUniqueId()).dismissEMStatusScreenMessage = use;
   }

   public static MatchInstance getMatchInstance(Player player) {
      return player != null && playerDataHashMap.get(player.getUniqueId()) != null ? playerDataHashMap.get(player.getUniqueId()).matchInstance : null;
   }

   public static void setMatchInstance(Player player, MatchInstance newMatchInstance) {
      if (playerDataHashMap.get(player.getUniqueId()) != null) {
         playerDataHashMap.get(player.getUniqueId()).matchInstance = newMatchInstance;
      }
   }

   public static long getSkillXP(UUID uuid, SkillType skillType) {
      PlayerData data = playerDataHashMap.get(uuid);
      return data == null ? 0L : getSkillXPByType(data, skillType);
   }

   public static void setSkillXP(UUID uuid, SkillType skillType, long xp) {
      String columnName = skillType.getColumnName();
      setDatabaseValue(uuid, columnName, xp);
      if (playerDataHashMap.containsKey(uuid)) {
         setSkillXPByType(playerDataHashMap.get(uuid), skillType, xp);
      }
   }

   public static long addSkillXP(UUID uuid, SkillType skillType, long xpToAdd) {
      if (!isDataLoaded(uuid)) {
         return 0L;
      } else {
         long currentXP = getSkillXP(uuid, skillType);
         long newXP = currentXP + xpToAdd;
         setSkillXP(uuid, skillType, newXP);
         return newXP;
      }
   }

   public static long[] getAllSkillXP(UUID uuid) {
      long[] xpArray = new long[SkillType.values().length];

      for (SkillType skillType : SkillType.values()) {
         xpArray[skillType.ordinal()] = getSkillXP(uuid, skillType);
      }

      return xpArray;
   }

   public static int getPlayerLevel(UUID uuid) {
      return CombatLevelCalculator.calculateCombatLevel(uuid);
   }

   public static int getSkillLevel(UUID uuid, SkillType skillType) {
      long xp = getSkillXP(uuid, skillType);
      return SkillXPCalculator.levelFromTotalXP(xp);
   }

   private static long getSkillXPByType(PlayerData playerData, SkillType skillType) {
      return switch (skillType) {
         case ARMOR -> playerData.skillXP_ARMOR;
         case SWORDS -> playerData.skillXP_SWORDS;
         case AXES -> playerData.skillXP_AXES;
         case BOWS -> playerData.skillXP_BOWS;
         case CROSSBOWS -> playerData.skillXP_CROSSBOWS;
         case TRIDENTS -> playerData.skillXP_TRIDENTS;
         case HOES -> playerData.skillXP_HOES;
         case MACES -> playerData.skillXP_MACES;
         case SPEARS -> playerData.skillXP_SPEARS;
         case STAVES -> playerData.skillXP_STAVES;
         case WANDS -> playerData.skillXP_WANDS;
      };
   }

   private static void setSkillXPByType(PlayerData playerData, SkillType skillType, long xp) {
      switch (skillType) {
         case ARMOR:
            playerData.setSkillXP_ARMOR(xp);
            break;
         case SWORDS:
            playerData.setSkillXP_SWORDS(xp);
            break;
         case AXES:
            playerData.setSkillXP_AXES(xp);
            break;
         case BOWS:
            playerData.setSkillXP_BOWS(xp);
            break;
         case CROSSBOWS:
            playerData.setSkillXP_CROSSBOWS(xp);
            break;
         case TRIDENTS:
            playerData.setSkillXP_TRIDENTS(xp);
            break;
         case HOES:
            playerData.setSkillXP_HOES(xp);
            break;
         case MACES:
            playerData.setSkillXP_MACES(xp);
            break;
         case SPEARS:
            playerData.setSkillXP_SPEARS(xp);
            break;
         case STAVES:
            playerData.setSkillXP_STAVES(xp);
            break;
         case WANDS:
            playerData.setSkillXP_WANDS(xp);
      }
   }

   public static String getSkillBonusSelections(UUID uuid) {
      if (!isInMemory(uuid)) {
         String dbValue = getDatabaseString(uuid, "SkillBonusSelections");
         return dbValue != null ? dbValue : "{}";
      } else {
         PlayerData data = playerDataHashMap.get(uuid);
         return data != null ? data.skillBonusSelections : "{}";
      }
   }

   public static void setSkillBonusSelections(UUID uuid, String selections) {
      setDatabaseValue(uuid, "SkillBonusSelections", selections);
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).skillBonusSelections = selections;
      }
   }

   public static double getGamblingDebt(UUID uuid) {
      if (!isInMemory(uuid)) {
         return getDatabaseLong(uuid, "GamblingDebtCents").longValue() / 100.0;
      } else {
         PlayerData data = playerDataHashMap.get(uuid);
         return data != null ? data.gamblingDebtCents / 100.0 : 0.0;
      }
   }

   public static void setGamblingDebt(UUID uuid, double debt) {
      debt = Math.max(0.0, Math.min(500.0, debt));
      long cents = Math.round(debt * 100.0);
      setDatabaseValues(uuid, Map.of("GamblingDebtCents", cents, "GamblingDebt", cents / 100.0));
      if (playerDataHashMap.containsKey(uuid)) {
         playerDataHashMap.get(uuid).gamblingDebtCents = cents;
      }
   }

   public static void addGamblingDebt(UUID uuid, double amount) {
      double currentDebt = getGamblingDebt(uuid);
      setGamblingDebt(uuid, currentDebt + amount);
   }

   public static void reduceGamblingDebt(UUID uuid, double amount) {
      double currentDebt = getGamblingDebt(uuid);
      setGamblingDebt(uuid, currentDebt - amount);
   }

   public static boolean hasGamblingDebt(UUID uuid) {
      return getGamblingDebt(uuid) > 0.0;
   }

   private static Boolean getDatabaseBoolean(UUID uuid, String value) {
      return PlayerDataRepository.getBoolean(uuid, value);
   }

   private static String getDatabaseString(UUID uuid, String value) {
      return PlayerDataRepository.getString(uuid, value);
   }

   private static Integer getDatabaseInteger(UUID uuid, String value) {
      return PlayerDataRepository.getInteger(uuid, value);
   }

   private static Long getDatabaseLong(UUID uuid, String value) {
      return PlayerDataRepository.getLong(uuid, value);
   }

   public static Connection getConnection() throws Exception {
      return PlayerDataRepository.connection();
   }

   public static void initializeDatabaseConnection() {
      new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + "/data").mkdirs();

      try {
         PlayerDataRepository.beginInitialization();
         GenerateDatabase.generate();
         Logger.info("Opened database successfully");
         PlayerDataRepository.migrateCurrencyToCents();
         new PortOldData();
         PlayerDataRepository.loadScoreRankingCacheAsync();

         for (Player player : Bukkit.getOnlinePlayers()) {
            new PlayerData(player.getUniqueId());
         }
      } catch (Exception var2) {
         Logger.warn(var2.getClass().getName() + ": " + var2.getMessage());
         Logger.warn(
            "Failed to establish a connection to the SQLite database. Player data will not be saved! Is your MySQL configuration valid and is your MySQL server running?"
         );
         var2.printStackTrace();
      }
   }

   public static void closeConnection() {
      Map<UUID, List<PlayerData.DeferredDatabaseValue>> remaining;
      synchronized (PlayerDataRepository.stateMonitor()) {
         playerDataHashMap.clear();
         CombatLevelCalculator.invalidateOnlineCombatLevel();
         loadingPlayers.clear();
         remaining = new HashMap<>(deferredDatabaseValues);
         deferredDatabaseValues.clear();
      }

      remaining.forEach((uuid, updates) -> updates.forEach(update -> PlayerDataRepository.enqueueUpdate(uuid, update.values())));
      PlayerDataRepository.close();
   }

   private void readExistingData(UUID uuid, ResultSet resultSet) throws Exception {
      this.currencyCents = resultSet.getLong("CurrencyCents");
      this.score = resultSet.getInt("Score");
      this.kills = resultSet.getInt("Kills");
      this.highestLevelKilled = resultSet.getInt("HighestLevelKilled");
      this.deaths = resultSet.getInt("Deaths");
      this.questsCompleted = resultSet.getInt("QuestsCompleted");
      this.dungeonsCompleted = resultSet.getInt("DungeonsCompleted");
      this.backTeleportLocation = ConfigurationLocation.serialize(resultSet.getString("BackTeleportLocation"));
      if (resultSet.getBytes("QuestStatus") != null) {
         try {
            this.quests = (List<Quest>)ObjectSerializer.fromString(new String(resultSet.getBytes("QuestStatus"), StandardCharsets.UTF_8));
         } catch (Exception var10) {
            Logger.warn("Failed to deserialize quest data for player " + uuid + "! This player's quest data will be wiped to prevent future errors.");

            try {
               resetQuests(uuid);
            } catch (Exception var9) {
               Logger.warn("Failed to reset quest data! Ironic.");
               var9.printStackTrace();
            }
         }
      }

      if (resultSet.getBytes("PlayerQuestCooldowns") != null) {
         try {
            this.playerQuestCooldowns = (PlayerQuestCooldowns)ObjectSerializer.fromString(
               new String(resultSet.getBytes("PlayerQuestCooldowns"), StandardCharsets.UTF_8)
            );
         } catch (Exception var8) {
            Logger.warn("Failed to get player quest cooldowns!  ! This player's quest cooldowns will be wiped to prevent future errors.");

            try {
               resetPlayerQuestCooldowns(uuid);
            } catch (Exception var7) {
               Logger.warn("Failed to reset quest cooldowns! Ironic.");
               var7.printStackTrace();
            }
         }
      }

      if (resultSet.getObject("UseBookMenus") != null) {
         this.useBookMenus = resultSet.getBoolean("UseBookMenus");
      } else {
         this.useBookMenus = true;
         setDatabaseValue(uuid, "UseBookMenus", true);
      }

      if (resultSet.getObject("DismissEMStatusScreenMessage") != null) {
         this.dismissEMStatusScreenMessage = resultSet.getBoolean("DismissEMStatusScreenMessage");
      } else {
         this.dismissEMStatusScreenMessage = false;
         setDatabaseValue(uuid, "DismissEMStatusScreenMessage", false);
      }

      if (resultSet.getBytes("DungeonBossLockouts") != null) {
         try {
            this.dungeonBossLockout = (DungeonBossLockout)ObjectSerializer.fromString(
               new String(resultSet.getBytes("DungeonBossLockouts"), StandardCharsets.UTF_8)
            );
            this.dungeonBossLockout.cleanupExpiredLockouts();
         } catch (Exception var6) {
            Logger.warn("Failed to get dungeon boss lockouts! This player's lockouts will be reset.");
            this.dungeonBossLockout = new DungeonBossLockout();
         }
      } else {
         this.dungeonBossLockout = new DungeonBossLockout();
      }

      if (resultSet.getBytes("QuestLockouts") != null) {
         try {
            this.questLockout = (QuestLockout)ObjectSerializer.fromString(new String(resultSet.getBytes("QuestLockouts"), StandardCharsets.UTF_8));
            this.questLockout.cleanupExpiredLockouts();
         } catch (Exception var5) {
            Logger.warn("Failed to get quest lockouts! This player's lockouts will be reset.");
            this.questLockout = new QuestLockout();
         }
      } else {
         this.questLockout = new QuestLockout();
      }

      this.skillXP_ARMOR = resultSet.getLong("SkillXP_ARMOR");
      this.skillXP_SWORDS = resultSet.getLong("SkillXP_SWORDS");
      this.skillXP_AXES = resultSet.getLong("SkillXP_AXES");
      this.skillXP_BOWS = resultSet.getLong("SkillXP_BOWS");
      this.skillXP_CROSSBOWS = resultSet.getLong("SkillXP_CROSSBOWS");
      this.skillXP_TRIDENTS = resultSet.getLong("SkillXP_TRIDENTS");
      this.skillXP_HOES = resultSet.getLong("SkillXP_HOES");
      this.skillXP_MACES = readOptionalSkillXp(resultSet, "SkillXP_MACES");
      this.skillXP_SPEARS = readOptionalSkillXp(resultSet, "SkillXP_SPEARS");
      this.skillXP_STAVES = readOptionalSkillXp(resultSet, "SkillXP_STAVES");
      this.skillXP_WANDS = readOptionalSkillXp(resultSet, "SkillXP_WANDS");
      String skillSelections = resultSet.getString("SkillBonusSelections");
      this.skillBonusSelections = skillSelections != null ? skillSelections : "{}";
      this.gamblingDebtCents = resultSet.getLong("GamblingDebtCents");
      Logger.info("User " + uuid + " data successfully read!");
   }

   private static long readOptionalSkillXp(ResultSet resultSet, String columnName) {
      try {
         return resultSet.getLong(columnName);
      } catch (SQLException var3) {
         return 0L;
      }
   }

   private void writeNewData(UUID uuid, String playerName) throws Exception {
      this.currencyCents = 0L;
      this.score = 0;
      this.kills = 0;
      this.highestLevelKilled = 0;
      this.deaths = 0;
      this.questsCompleted = 0;
      this.skillXP_ARMOR = 0L;
      this.skillXP_SWORDS = 0L;
      this.skillXP_AXES = 0L;
      this.skillXP_BOWS = 0L;
      this.skillXP_CROSSBOWS = 0L;
      this.skillXP_TRIDENTS = 0L;
      this.skillXP_HOES = 0L;
      this.skillXP_MACES = 0L;
      this.skillXP_SPEARS = 0L;
      this.skillXP_STAVES = 0L;
      this.skillXP_WANDS = 0L;
      this.skillBonusSelections = "{}";
      this.useBookMenus = true;
      this.dismissEMStatusScreenMessage = false;
      this.dungeonBossLockout = new DungeonBossLockout();
      this.questLockout = new QuestLockout();
      this.gamblingDebtCents = 0L;
      PlayerDataRepository.insertNewPlayer(uuid, playerName);
      Logger.info("No player entry detected, generating new entry!");
   }

   private void scheduleBukkitInitialization(UUID uuid, Player sessionPlayer) {
      if (MetadataHandler.PLUGIN.isEnabled()) {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == sessionPlayer && playerDataHashMap.get(uuid) == this) {
               for (Quest quest : new ArrayList<>(this.quests)) {
                  if (quest instanceof CustomQuest customQuest && quest.isAccepted() && !quest.getQuestObjectives().isTurnedIn()) {
                     customQuest.applyTemporaryPermissions(player);
                  }
               }

               if (this.playerQuestCooldowns != null) {
                  this.playerQuestCooldowns.startCooldowns(uuid);
               }

               Bukkit.getPluginManager().callEvent(new PlayerDataLoadedEvent(player));
            } else {
               if (playerDataHashMap.remove(uuid, this)) {
                  CombatLevelCalculator.invalidateOnlineCombatLevel();
               }
            }
         });
      }
   }

   @Generated
   public static String getDATABASE_NAME() {
      return "player_data.db";
   }

   @Generated
   public static String getPLAYER_DATA_TABLE_NAME() {
      return "PlayerData";
   }

   @Generated
   public static ConcurrentHashMap<UUID, PlayerData> getPlayerDataHashMap() {
      return playerDataHashMap;
   }

   @Generated
   public int getScore() {
      return this.score;
   }

   @Generated
   public void setScore(int score) {
      this.score = score;
   }

   @Generated
   public int getKills() {
      return this.kills;
   }

   @Generated
   public void setKills(int kills) {
      this.kills = kills;
   }

   @Generated
   public int getHighestLevelKilled() {
      return this.highestLevelKilled;
   }

   @Generated
   public void setHighestLevelKilled(int highestLevelKilled) {
      this.highestLevelKilled = highestLevelKilled;
   }

   @Generated
   public int getDeaths() {
      return this.deaths;
   }

   @Generated
   public void setDeaths(int deaths) {
      this.deaths = deaths;
   }

   @Generated
   public int getQuestsCompleted() {
      return this.questsCompleted;
   }

   @Generated
   public void setQuestsCompleted(int questsCompleted) {
      this.questsCompleted = questsCompleted;
   }

   @Generated
   public int getDungeonsCompleted() {
      return this.dungeonsCompleted;
   }

   @Generated
   public void setDungeonsCompleted(int dungeonsCompleted) {
      this.dungeonsCompleted = dungeonsCompleted;
   }

   @Generated
   public List<Quest> getQuests() {
      return this.quests;
   }

   @Generated
   public void setQuests(List<Quest> quests) {
      this.quests = quests;
   }

   @Generated
   public PlayerQuestCooldowns getPlayerQuestCooldowns() {
      return this.playerQuestCooldowns;
   }

   @Generated
   public void setPlayerQuestCooldowns(PlayerQuestCooldowns playerQuestCooldowns) {
      this.playerQuestCooldowns = playerQuestCooldowns;
   }

   @Generated
   public Location getBackTeleportLocation() {
      return this.backTeleportLocation;
   }

   @Generated
   public void setBackTeleportLocation(Location backTeleportLocation) {
      this.backTeleportLocation = backTeleportLocation;
   }

   @Generated
   public boolean isUseBookMenus() {
      return this.useBookMenus;
   }

   @Generated
   public void setUseBookMenus(boolean useBookMenus) {
      this.useBookMenus = useBookMenus;
   }

   @Generated
   public boolean isDismissEMStatusScreenMessage() {
      return this.dismissEMStatusScreenMessage;
   }

   @Generated
   public void setDismissEMStatusScreenMessage(boolean dismissEMStatusScreenMessage) {
      this.dismissEMStatusScreenMessage = dismissEMStatusScreenMessage;
   }

   @Generated
   public MatchInstance getMatchInstance() {
      return this.matchInstance;
   }

   @Generated
   public void setMatchInstance(MatchInstance matchInstance) {
      this.matchInstance = matchInstance;
   }

   @Generated
   public DungeonBossLockout getDungeonBossLockout() {
      return this.dungeonBossLockout;
   }

   @Generated
   public void setDungeonBossLockout(DungeonBossLockout dungeonBossLockout) {
      this.dungeonBossLockout = dungeonBossLockout;
   }

   @Generated
   public QuestLockout getQuestLockout() {
      return this.questLockout;
   }

   @Generated
   public void setQuestLockout(QuestLockout questLockout) {
      this.questLockout = questLockout;
   }

   @Generated
   public long getSkillXP_ARMOR() {
      return this.skillXP_ARMOR;
   }

   @Generated
   public long getSkillXP_SWORDS() {
      return this.skillXP_SWORDS;
   }

   @Generated
   public long getSkillXP_AXES() {
      return this.skillXP_AXES;
   }

   @Generated
   public long getSkillXP_BOWS() {
      return this.skillXP_BOWS;
   }

   @Generated
   public long getSkillXP_CROSSBOWS() {
      return this.skillXP_CROSSBOWS;
   }

   @Generated
   public long getSkillXP_TRIDENTS() {
      return this.skillXP_TRIDENTS;
   }

   @Generated
   public long getSkillXP_HOES() {
      return this.skillXP_HOES;
   }

   @Generated
   public long getSkillXP_MACES() {
      return this.skillXP_MACES;
   }

   @Generated
   public long getSkillXP_SPEARS() {
      return this.skillXP_SPEARS;
   }

   @Generated
   public long getSkillXP_STAVES() {
      return this.skillXP_STAVES;
   }

   @Generated
   public long getSkillXP_WANDS() {
      return this.skillXP_WANDS;
   }

   @Generated
   public String getSkillBonusSelections() {
      return this.skillBonusSelections;
   }

   @Generated
   public void setSkillBonusSelections(String skillBonusSelections) {
      this.skillBonusSelections = skillBonusSelections;
   }

   private record DeferredDatabaseValue(Map<String, Object> values) {
   }

   public static class PlayerDataEvents implements Listener {
      @EventHandler(
         priority = EventPriority.LOWEST
      )
      public void onPlayerLogin(PlayerJoinEvent event) {
         final UUID playerUuid = event.getPlayer().getUniqueId();
         final Player sessionPlayer = event.getPlayer();
         synchronized (PlayerDataRepository.stateMonitor()) {
            PlayerData.loadingPlayers.remove(playerUuid);
            PlayerData.clearPlayerData(playerUuid);
         }

         (new BukkitRunnable() {
            public void run() {
               if (Bukkit.getPlayer(playerUuid) == sessionPlayer) {
                  new PlayerData(playerUuid);
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, 20L);
      }

      @EventHandler
      public void onPlayerLogout(PlayerQuitEvent event) {
         CombatLevelCalculator.invalidateOnlineCombatLevel();
         final UUID playerUuid = event.getPlayer().getUniqueId();
         final String playerName = event.getPlayer().getName();
         (new BukkitRunnable() {
            public void run() {
               if (Bukkit.getPlayer(playerUuid) == null) {
                  PlayerData.clearPlayerData(playerUuid);
                  PlayerData.setDisplayName(playerUuid, playerName);
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, 20L);
      }
   }

   public record ScoreRank(int position, int playerCount) {
      public static PlayerData.ScoreRank unavailable() {
         return new PlayerData.ScoreRank(0, 0);
      }

      public boolean isAvailable() {
         return this.position > 0 && this.playerCount > 0;
      }
   }
}
