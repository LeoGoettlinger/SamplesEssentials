package vrx.economy.vRXClans;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class VRXClans extends JavaPlugin {
   private static VRXClans instance;
   private static Economy econ = null;
   private ClanManager clanManager;
   private FileConfiguration guiConfig;
   private LanguageManager languageManager;
   private DatabaseManager databaseManager;
   private HologramManager hologramManager;

   public void onEnable() {
      instance = this;
      this.getLogger().info("VRX Clans startet...");
      this.getConfig().addDefault("auto-save-interval", 10);
      this.getConfig().addDefault("season-reward", 50000.0);
      this.saveDefaultConfig();
      this.saveResource("Languages/en_US.yml", false);
      this.saveResource("Languages/de_DE.yml", false);
      this.languageManager = new LanguageManager(this);
      this.loadGuiTheme();
      if (!this.setupEconomy()) {
         this.getLogger().severe("Vault economy not found! Disabling plugin...");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.databaseManager = new DatabaseManager(this);

         try {
            this.databaseManager.connect();
         } catch (Exception var6) {
            this.getLogger().severe("Could not connect to database!");
            var6.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }

         this.clanManager = new ClanManager(this, this.databaseManager);
         this.clanManager.loadClansAsync();
         if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanExpansion(this).register();
            this.getLogger().info("PlaceholderAPI found! Placeholders have been enabled.");
         }

         if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null) {
            this.hologramManager = new HologramManager(this);
            Bukkit.getScheduler().runTaskTimer(this, () -> this.hologramManager.updateHolograms(), 200L, 6000L);
            this.getLogger().info("DecentHolograms gefunden! Leaderboards aktiviert.");
         } else {
            this.getLogger().info("DecentHolograms not found. Leaderboards disabled.");
         }

         this.getServer().getPluginManager().registerEvents(new ClanListener(this), this);
         new CloudCommandBootstrap(this).register();
         Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
               Clan clan = this.clanManager.getClanByPlayer(p);
               if (clan != null) {
                  if (clan.hasHasteBooster()) {
                     p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 100, 1, false, false));
                  }

                  if (clan.hasSpeedBooster()) {
                     p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
                  }

                  if (clan.hasStrengthBooster()) {
                     p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0, false, false));
                  }

                  Location base = clan.getBaseLocation();
                  if (base != null && base.getWorld().equals(p.getWorld())) {
                     double distSq = base.distanceSquared(p.getLocation());
                     int radius = clan.getDynamicRadius(this);
                     if (distSq <= radius * radius) {
                        if (clan.isBaseRegen()) {
                           p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, false, false));
                        }

                        if (clan.isBaseSaturation()) {
                           p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100, 0, false, false));
                        }
                     }
                  }
               }
            }
         }, 20L, 60L);
         Bukkit.getScheduler()
            .runTaskTimer(
               this,
               () -> {
                  long current = System.currentTimeMillis();
                  double upkeepCost = this.getConfig().getDouble("upkeep-cost", 500.0);
                  long upkeepInterval = this.getConfig().getLong("upkeep-interval-hours", 24L) * 3600L * 1000L;
                  boolean dropLevel = this.getConfig().getBoolean("upkeep-penalty-level-drop", true);
                  long questInterval = this.getConfig().getLong("quests.reset-interval-hours", 24L) * 3600L * 1000L;

                  for (Clan clan : this.clanManager.getClans().values()) {
                     if (upkeepCost > 0.0 && upkeepInterval > 0L && current - clan.getLastUpkeepTime() >= upkeepInterval) {
                        clan.setLastUpkeepTime(current);
                        if (clan.withdraw(upkeepCost)) {
                           this.clanBroadcast(clan, "§8[§bClan Tax§8] §aSuccessfully deducted §e" + upkeepCost + "$ §ain taxes from the clan bank.");
                        } else {
                           clan.setBankBalance(0.0);
                           if (dropLevel && clan.getLevel() > 1) {
                              clan.setLevel(clan.getLevel() - 1);
                              this.clanBroadcast(
                                 clan,
                                 "§8[§bClan Tax§8] §cYour clan could not pay the taxes! As a penalty, your clan was downgraded to §eLevel "
                                    + clan.getLevel()
                                    + "§c!"
                              );
                           } else {
                              this.clanBroadcast(clan, "§8[§bClan Tax§8] §cYour clan could not pay the taxes! Your bank is now empty.");
                           }
                        }
                     }

                     if (current - clan.getLastQuestReset() >= questInterval) {
                        clan.setLastQuestReset(current);
                        clan.setQuestZombies(0);
                        clan.setQuestDiamonds(0);
                        clan.setQuestMoney(0.0);
                        clan.setQuestZombiesDone(false);
                        clan.setQuestDiamondsDone(false);
                        clan.setQuestBankDone(false);
                        this.clanBroadcast(clan, this.languageManager.get("msg.quests-reset"));
                     }
                  }
               },
               1200L,
               12000L
            );
         long topRefreshTicks = Math.max(20L, this.getConfig().getLong("top-cache-refresh-seconds", 60L) * 20L);
         Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (this.clanManager != null) {
               this.clanManager.refreshTopCache();
            }
         }, 100L, topRefreshTicks);
         int autoSaveMinutes = this.getConfig().getInt("auto-save-interval", 10);
         if (autoSaveMinutes > 0) {
            long autoSaveTicks = autoSaveMinutes * 60L * 20L;
            Bukkit.getScheduler().runTaskTimer(this, () -> {
               for (Clan clan : this.clanManager.getClans().values()) {
                  if (clan.isVaultDirty()) {
                     clan.updateVaultCache();
                  }
               }

               Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                  if (this.clanManager != null) {
                     this.clanManager.saveClansSync();
                     this.getLogger().info("Auto-save: Clan data has been safely saved in the background.");
                  }
               });
            }, autoSaveTicks, autoSaveTicks);
         }

         this.getLogger().info("VRX Clans erfolgreich geladen!");
      }
   }

   public void onDisable() {
      if (this.clanManager != null) {
         for (Clan clan : this.clanManager.getClans().values()) {
            if (clan.isVaultDirty()) {
               clan.updateVaultCache();
            }
         }

         this.clanManager.saveClansSync();
      }

      if (this.databaseManager != null) {
         this.databaseManager.disconnect();
      }

      this.getLogger().info("VRX Clans gestoppt und Daten gesichert.");
   }

   public boolean isLocationInWorldGuardRegion(Location loc) {
      return this.getServer().getPluginManager().getPlugin("WorldGuard") == null ? false : this.checkWorldGuardInternal(loc);
   }

   private boolean checkWorldGuardInternal(Location loc) {
      try {
         RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
         RegionQuery query = container.createQuery();
         ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
         return !set.getRegions().isEmpty();
      } catch (NoClassDefFoundError | Exception var5) {
         return false;
      }
   }

   public void sendDiscordWebhook(String eventType, String... replacements) {
      String url = this.getConfig().getString("discord.webhook-url");
      if (url != null && !url.isEmpty() && !url.equals("YOUR_WEBHOOK_URL_HERE")) {
         Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
               String title = ChatColor.stripColor(this.languageManager.get("discord." + eventType + "-title", replacements)).replace("\"", "\\\"");
               String desc = ChatColor.stripColor(this.languageManager.get("discord." + eventType + "-desc", replacements)).replace("\"", "\\\"");
               int colorCode = 0;
               if (eventType.equals("creation")) {
                  colorCode = 5814783;
               } else if (eventType.equals("levelup")) {
                  colorCode = 16766720;
               } else if (eventType.equals("war")) {
                  colorCode = 16711680;
               }

               String json = "{\"embeds\":[{\"title\":\"" + title + "\",\"description\":\"" + desc + "\",\"color\":" + colorCode + "}]}";
               HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
               connection.setRequestMethod("POST");
               connection.setRequestProperty("Content-Type", "application/json");
               connection.setRequestProperty("User-Agent", "VRXClans");
               connection.setDoOutput(true);

               try (OutputStream os = connection.getOutputStream()) {
                  os.write(json.getBytes(StandardCharsets.UTF_8));
               }

               connection.getResponseCode();
            } catch (Exception var14) {
               this.getLogger().warning("Discord Webhook Error: " + var14.getMessage());
            }
         });
      }
   }

   public void handleClanLevelUps(Clan clan) {
      if (clan != null) {
         int from = Math.max(1, clan.getLastRewardLevel() + 1);
         int to = clan.getLevel();
         if (from <= to) {
            for (int level = from; level <= to; level++) {
               this.announceLevelUp(clan, level);
               this.applyLevelReward(clan, level);
            }

            clan.setLastRewardLevel(to);
            this.clanManager.evaluateAchievements(clan);
         }
      }
   }

   private void announceLevelUp(Clan clan, int level) {
      int unlockLvl = this.getConfig().getInt("vault-unlock-level", 3);
      int upgradeLvl = this.getConfig().getInt("vault-upgrade-level", 10);

      for (Player p : Bukkit.getOnlinePlayers()) {
         Clan pClan = this.clanManager.getClanByPlayer(p);
         if (pClan != null && pClan.getName().equals(clan.getName())) {
            p.sendTitle(
               this.languageManager.get("msg.level-up-title"), this.languageManager.get("msg.level-up-sub", "%level%", String.valueOf(level)), 10, 70, 20
            );
            p.sendMessage(this.languageManager.get("msg.level-up-chat", "%level%", String.valueOf(level)));
            if (level == unlockLvl) {
               p.sendMessage(this.languageManager.get("msg.level-vault-unlock"));
            }

            if (level == upgradeLvl) {
               p.sendMessage(this.languageManager.get("msg.level-vault-upgrade"));
            }
         }
      }

      if (this.getConfig().getBoolean("discord.enable-levelup", true)) {
         this.sendDiscordWebhook("levelup", "%clan%", clan.getName(), "%level%", String.valueOf(level));
      }
   }

   private void applyLevelReward(Clan clan, int level) {
      String base = "level-rewards." + level;
      if (this.getConfig().isConfigurationSection(base)) {
         double bankReward = this.getConfig().getDouble(base + ".bank", 0.0);
         if (bankReward > 0.0) {
            clan.deposit(bankReward);
            this.clanManager.logClanEvent(clan, ClanLogEntry.LogType.LEVEL_REWARD, "System", "ClanBank", bankReward, "Level " + level + " Belohnung");
         }

         String message = this.getConfig().getString(base + ".message", "");
         if (message != null && !message.isBlank()) {
            this.clanBroadcast(
               clan,
               ChatColor.translateAlternateColorCodes(
                  '&', message.replace("%clan%", clan.getName()).replace("%level%", String.valueOf(level)).replace("%bank%", String.valueOf(bankReward))
               )
            );
         }

         for (String command : this.getConfig().getStringList(base + ".commands")) {
            String parsed = command.replace("%clan%", clan.getName()).replace("%level%", String.valueOf(level)).replace("%bank%", String.valueOf(bankReward));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
         }
      }
   }

   public void reloadPluginConfigs() {
      this.reloadConfig();
      this.loadGuiTheme();
      this.languageManager.reload();
   }

   private void loadGuiTheme() {
      String theme = this.getConfig().getString("gui-theme", "default");
      File guiFile = new File(this.getDataFolder(), "gui_" + theme + ".yml");
      if (!guiFile.exists()) {
         if (theme.equalsIgnoreCase("default")) {
            this.saveResource("gui_default.yml", false);
         } else {
            guiFile.getParentFile().mkdirs();

            try {
               guiFile.createNewFile();
            } catch (IOException var4) {
               var4.printStackTrace();
            }
         }
      }

      this.guiConfig = YamlConfiguration.loadConfiguration(guiFile);
   }

   public void clanBroadcast(Clan clan, String message) {
      for (UUID uuid : clan.getMembers().keySet()) {
         Player p = Bukkit.getPlayer(uuid);
         if (p != null && p.isOnline()) {
            p.sendMessage(message);
         }
      }
   }

   public FileConfiguration getGuiConfig() {
      return this.guiConfig;
   }

   public LanguageManager getLang() {
      return this.languageManager;
   }

   public DatabaseManager getDatabaseManager() {
      return this.databaseManager;
   }

   public HologramManager getHologramManager() {
      return this.hologramManager;
   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return false;
         } else {
            econ = (Economy)rsp.getProvider();
            return econ != null;
         }
      }
   }

   public ClanManager getClanManager() {
      return this.clanManager;
   }

   public static VRXClans getInstance() {
      return instance;
   }

   public static Economy getEconomy() {
      return econ;
   }
}
