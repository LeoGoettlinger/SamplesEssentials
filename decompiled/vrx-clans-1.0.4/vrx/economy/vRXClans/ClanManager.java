package vrx.economy.vRXClans;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

public class ClanManager {
   private final VRXClans plugin;
   private final ClanRepository repository;
   private final ClanCache cache;
   private final InviteService inviteService;
   private final WarService warService;
   private final RegionProtectionService regionProtectionService;
   private final ClanApplicationService applicationService;
   private final ClanTopCache topCache;
   private final Map<String, Long> activeWarStarts = new ConcurrentHashMap<>();
   private final Set<UUID> spyAdmins = ConcurrentHashMap.newKeySet();
   private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();
   private final Map<UUID, Clan.ClanRank> pendingRankRenames = new ConcurrentHashMap<>();

   public ClanManager(VRXClans plugin, DatabaseManager db) {
      this.plugin = plugin;
      this.repository = new ClanRepository(plugin, db);
      this.cache = new ClanCache();
      this.inviteService = new InviteService();
      this.warService = new WarService();
      this.regionProtectionService = new RegionProtectionService(plugin, this.cache);
      this.applicationService = new ClanApplicationService();
      this.topCache = new ClanTopCache();
   }

   public void loadClansAsync() {
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            ClanRepository.ClanDataSnapshot snapshot = this.repository.loadAll();
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               this.cache.replaceAll(snapshot.clans(), snapshot.playerClans());
               this.refreshTopCache();
               this.plugin.getLogger().info("Alle Clan-Daten erfolgreich geladen!");
            });
         } catch (SQLException var2) {
            this.plugin.getLogger().severe("Could not load clan data: " + var2.getMessage());
            var2.printStackTrace();
         }
      });
   }

   public void saveClansSync() {
      try {
         this.repository.saveAll(this.cache.clans().values());
      } catch (SQLException var2) {
         this.plugin.getLogger().severe("Could not save clan data: " + var2.getMessage());
         var2.printStackTrace();
      }
   }

   public void saveClansAsync() {
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, this::saveClansSync);
   }

   public void addTeleport(Player player, BukkitTask task) {
      this.pendingTeleports.put(player.getUniqueId(), task);
   }

   public boolean isTeleporting(Player player) {
      return this.pendingTeleports.containsKey(player.getUniqueId());
   }

   public void cancelTeleport(Player player) {
      BukkitTask task = this.pendingTeleports.remove(player.getUniqueId());
      if (task != null) {
         task.cancel();
      }
   }

   public void setJoinCooldown(Player player, int seconds) {
      this.inviteService.setJoinCooldown(player, seconds);
   }

   public long getJoinCooldownSeconds(Player player) {
      return this.inviteService.getJoinCooldownSeconds(player);
   }

   public void addInvite(Player player, Clan clan, int timeoutSeconds) {
      this.inviteService.addInvite(player, clan, timeoutSeconds);
   }

   public String getPendingInvite(Player player) {
      return this.inviteService.getPendingInvite(player);
   }

   public void clearInvite(Player player) {
      this.inviteService.clearInvite(player);
   }

   public boolean createClan(String clanName, Player leader) {
      UUID leaderUUID = leader.getUniqueId();
      if (this.cache.playerClans().containsKey(leaderUUID)) {
         return false;
      } else {
         for (String existingName : this.cache.clans().keySet()) {
            if (existingName.equalsIgnoreCase(clanName)) {
               return false;
            }
         }

         Clan newClan = new Clan(clanName, leaderUUID);
         this.cache.addClan(newClan);
         this.cache.addMember(leaderUUID, newClan);
         if (this.plugin.getConfig().getBoolean("clan-tags.auto-generate-from-name", true)) {
            int maxLen = this.plugin.getConfig().getInt("clan-tags.max-length", 5);
            newClan.setTag(clanName.substring(0, Math.min(maxLen, clanName.length())).toUpperCase(Locale.ROOT));
         }

         this.evaluateAchievements(newClan);
         this.updatePlayerClanVisuals(leader);
         if (this.plugin.getConfig().getBoolean("discord.enable-creation", true)) {
            this.plugin.sendDiscordWebhook("creation", "%clan%", clanName, "%player%", leader.getName());
         }

         return true;
      }
   }

   public boolean renameClan(Clan clan, String newName) {
      for (String existingName : this.cache.clans().keySet()) {
         if (existingName.equalsIgnoreCase(newName)) {
            return false;
         }
      }

      String oldName = clan.getName();

      for (Clan other : this.cache.clans().values()) {
         if (other.getAllies().contains(oldName)) {
            other.removeAlly(oldName);
            other.addAlly(newName);
         }

         if (other.getRivals().contains(oldName)) {
            other.removeRival(oldName);
            other.addRival(newName);
         }

         if (oldName.equals(other.getActiveWar())) {
            other.setActiveWar(newName);
         }
      }

      this.cache.renameClan(clan, oldName, newName);
      return true;
   }

   public Clan getClanByPlayer(Player player) {
      return this.cache.getClan(player);
   }

   public Clan getClanByPlayerUUID(UUID uuid) {
      return this.cache.getClan(uuid);
   }

   public Clan getClanByName(String clanName) {
      return this.cache.getClanByName(clanName);
   }

   public void addPlayerToClan(Player player, Clan clan) {
      UUID uuid = player.getUniqueId();
      clan.addMember(uuid);
      this.cache.addMember(uuid, clan);
      this.applicationService.removeAllForPlayer(uuid);
      this.cache.updateClanBase(clan);
      this.evaluateAchievements(clan);
      this.updatePlayerClanVisuals(player);
   }

   public void removePlayerFromClan(Player player) {
      UUID uuid = player.getUniqueId();
      Clan clan = this.cache.getClan(uuid);
      if (clan != null) {
         clan.removeMember(uuid);
         this.cache.removeMember(uuid);
         this.updatePlayerClanVisuals(player);
         this.cache.updateClanBase(clan);
         if (clan.getMembers().isEmpty()) {
            this.disbandClan(clan);
         }
      }
   }

   public void disbandClan(Clan clan) {
      String clanName = clan.getName();
      this.cache.removeClan(clan);
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            this.repository.deleteClan(clanName);
         } catch (SQLException var3) {
            this.plugin.getLogger().severe("Could not delete clan from database: " + clanName + " - " + var3.getMessage());
            var3.printStackTrace();
         }
      });
   }

   public boolean toggleSpy(Player player) {
      UUID uuid = player.getUniqueId();
      if (this.spyAdmins.contains(uuid)) {
         this.spyAdmins.remove(uuid);
         return false;
      } else {
         this.spyAdmins.add(uuid);
         return true;
      }
   }

   public void updateBaseIndex(Clan clan) {
      this.cache.updateClanBase(clan);
   }

   public boolean isProtectedBaseLocation(Location loc, Player player) {
      return this.regionProtectionService.isProtected(loc, player);
   }

   public Clan getClanByVaultInventory(Inventory inventory) {
      return this.cache.getClanByVaultInventory(inventory);
   }

   public Set<UUID> getSpies() {
      return this.spyAdmins;
   }

   public Map<String, Clan> getClans() {
      return this.cache.clans();
   }

   public void refreshTopCache() {
      this.topCache.refresh(this.cache.clans().values());
   }

   public List<Clan> getTopClans(String category, int limit) {
      if (this.topCache.getLastRefresh() <= 0L) {
         this.refreshTopCache();
      }

      return this.topCache.get(category, limit);
   }

   public int getMaxMembers(Clan clan) {
      return this.plugin.getConfig().getInt("max-members", 15) + (clan == null ? 0 : clan.getExtraMemberSlots());
   }

   public int getMaxAllies(Clan clan) {
      return this.plugin.getConfig().getInt("max-allies", 3) + (clan == null ? 0 : clan.getExtraAllySlots());
   }

   public int getMaxRivals(Clan clan) {
      return this.plugin.getConfig().getInt("max-rivals", 3) + (clan == null ? 0 : clan.getExtraRivalSlots());
   }

   public void logClanEvent(Clan clan, ClanLogEntry.LogType type, String actor, String target, double amount, String message) {
      if (clan != null) {
         ClanLogEntry entry = ClanLogEntry.now(clan.getName(), type, actor, target, amount, message);
         Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
               this.repository.insertLog(entry);
            } catch (SQLException var3x) {
               this.plugin.getLogger().warning("Could not save clan log: " + var3x.getMessage());
            }
         });
      }
   }

   public List<ClanLogEntry> getClanLogs(Clan clan, int limit) {
      if (clan == null) {
         return List.of();
      } else {
         try {
            return this.repository.loadLogs(clan.getName(), limit);
         } catch (SQLException var4) {
            this.plugin.getLogger().warning("Could not load clan logs: " + var4.getMessage());
            return List.of();
         }
      }
   }

   public void startRankRename(Player player, Clan.ClanRank rank) {
      this.pendingRankRenames.put(player.getUniqueId(), rank);
   }

   public Clan.ClanRank consumeRankRename(Player player) {
      return this.pendingRankRenames.remove(player.getUniqueId());
   }

   public boolean hasPendingRankRename(Player player) {
      return this.pendingRankRenames.containsKey(player.getUniqueId());
   }

   public boolean canCountWarKill(UUID killer, UUID victim) {
      long cooldownMillis = this.plugin.getConfig().getLong("war.kill-farming-cooldown-seconds", 300L) * 1000L;
      return this.warService.canCountWarKill(killer, victim, cooldownMillis);
   }

   public boolean addApplication(Player applicant, Clan clan) {
      if (!this.plugin.getConfig().getBoolean("applications.enabled", true)) {
         return false;
      } else {
         int timeout = this.plugin.getConfig().getInt("applications.expire-minutes", 60) * 60;
         return this.applicationService.apply(applicant.getUniqueId(), applicant.getName(), clan.getName(), timeout);
      }
   }

   public List<ClanApplication> getApplications(Clan clan) {
      return clan == null ? List.of() : this.applicationService.list(clan.getName());
   }

   public ClanApplication findApplication(Clan clan, String playerName) {
      return clan != null && playerName != null ? this.applicationService.find(clan.getName(), playerName) : null;
   }

   public void removeApplication(Clan clan, UUID uuid) {
      if (clan != null && uuid != null) {
         this.applicationService.remove(clan.getName(), uuid);
      }
   }

   public void beginWar(Clan a, Clan b) {
      if (a != null && b != null) {
         this.activeWarStarts.put(this.warKey(a.getName(), b.getName()), System.currentTimeMillis());
      }
   }

   public void finishWar(Clan winner, Clan loser, int winnerKills, int loserKills, double pot) {
      if (winner != null && loser != null) {
         String key = this.warKey(winner.getName(), loser.getName());
         long started = this.activeWarStarts.getOrDefault(key, System.currentTimeMillis());
         this.activeWarStarts.remove(key);
         WarHistoryEntry entry = new WarHistoryEntry(
            System.currentTimeMillis(),
            winner.getName(),
            loser.getName(),
            winner.getName(),
            loser.getName(),
            winnerKills,
            loserKills,
            pot,
            Math.max(0L, System.currentTimeMillis() - started)
         );
         Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
               this.repository.insertWarHistory(entry);
            } catch (SQLException var3x) {
               this.plugin.getLogger().warning("Could not save war history: " + var3x.getMessage());
            }
         });
      }
   }

   public List<WarHistoryEntry> getWarHistory(Clan clan, int limit) {
      if (clan == null) {
         return List.of();
      } else {
         try {
            return this.repository.loadWarHistory(clan.getName(), limit);
         } catch (SQLException var4) {
            this.plugin.getLogger().warning("Could not load war history: " + var4.getMessage());
            return List.of();
         }
      }
   }

   private String warKey(String a, String b) {
      String left = a.toLowerCase(Locale.ROOT);
      String right = b.toLowerCase(Locale.ROOT);
      return left.compareTo(right) <= 0 ? left + "|" + right : right + "|" + left;
   }

   public void evaluateAchievements(Clan clan) {
      if (clan != null && this.plugin.getConfig().getBoolean("achievements.enabled", true)) {
         ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("achievements.definitions");
         if (section != null) {
            for (String id : section.getKeys(false)) {
               String key = id.toLowerCase(Locale.ROOT);
               if (!clan.hasAchievement(key)) {
                  String condition = this.plugin.getConfig().getString("achievements.definitions." + id + ".condition", "").toLowerCase(Locale.ROOT);
                  int target = this.plugin.getConfig().getInt("achievements.definitions." + id + ".target", 1);
                  if (this.achievementReached(clan, condition, target)) {
                     clan.addAchievement(key);
                     double bank = this.plugin.getConfig().getDouble("achievements.definitions." + id + ".rewards.bank", 0.0);
                     double xp = this.plugin.getConfig().getDouble("achievements.definitions." + id + ".rewards.xp", 0.0);
                     if (bank > 0.0) {
                        clan.deposit(bank);
                     }

                     if (xp > 0.0 && clan.addXp(xp, this.plugin)) {
                        this.plugin.handleClanLevelUps(clan);
                     }

                     String display = this.plugin.getConfig().getString("achievements.definitions." + id + ".name", id);
                     this.plugin.clanBroadcast(clan, "§8[§bClan Achievement§8] §aFreigeschaltet: §e" + display);
                     this.logClanEvent(clan, ClanLogEntry.LogType.ACHIEVEMENT_UNLOCK, "System", display, bank, "Achievement unlocked");

                     for (String command : this.plugin.getConfig().getStringList("achievements.definitions." + id + ".rewards.commands")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%clan%", clan.getName()).replace("%achievement%", display));
                     }
                  }
               }
            }
         }
      }
   }

   private boolean achievementReached(Clan clan, String condition, int target) {
      return switch (condition) {
         case "level" -> clan.getLevel() >= target;
         case "bank" -> clan.getBankBalance() >= target;
         case "kills" -> clan.getKills() >= target;
         case "members" -> clan.getMembers().size() >= target;
         default -> false;
      };
   }

   public void updatePlayerClanVisuals(Player player) {
      if (player != null && this.plugin.getConfig().getBoolean("clan-tags.tab.enabled", true)) {
         Clan clan = this.getClanByPlayer(player);
         if (clan != null && !clan.getTag().isBlank()) {
            String format = this.plugin.getConfig().getString("clan-tags.tab.format", "&8[%tagcolor%%tag%&8] &f%player%");
            String value = format.replace("%tag%", clan.getTag())
               .replace("%tagcolor%", clan.getTagColor())
               .replace("%clan%", clan.getName())
               .replace("%player%", player.getName());
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', value));
         } else {
            player.setPlayerListName(player.getName());
         }
      }
   }

   public void setAllyReq(String target, String requester) {
      this.warService.setAllyReq(target, requester, this.plugin.getConfig().getInt("diplomacy-request-timeout", 120));
   }

   public String getAllyReq(String target) {
      return this.warService.getAllyReq(target);
   }

   public void clearAllyReq(String target) {
      this.warService.clearAllyReq(target);
   }

   public void setRivalReq(String target, String requester) {
      this.warService.setRivalReq(target, requester, this.plugin.getConfig().getInt("diplomacy-request-timeout", 120));
   }

   public String getRivalReq(String target) {
      return this.warService.getRivalReq(target);
   }

   public void clearRivalReq(String target) {
      this.warService.clearRivalReq(target);
   }

   public void setNeutralReq(String target, String requester) {
      this.warService.setNeutralReq(target, requester, this.plugin.getConfig().getInt("diplomacy-request-timeout", 120));
   }

   public String getNeutralReq(String target) {
      return this.warService.getNeutralReq(target);
   }

   public void clearNeutralReq(String target) {
      this.warService.clearNeutralReq(target);
   }

   public void setWarReq(String target, String requester, double wager) {
      this.warService.setWarReq(target, requester, wager);
   }

   public ClanManager.WarRequest getWarReq(String target) {
      WarService.WarRequest req = this.warService.getWarReq(target);
      return req == null ? null : new ClanManager.WarRequest(req.requester(), req.wager());
   }

   public void clearWarReq(String target) {
      this.warService.clearWarReq(target);
   }

   public static class WarRequest {
      public final String requester;
      public final double wager;

      public WarRequest(String requester, double wager) {
         this.requester = requester;
         this.wager = wager;
      }
   }
}
