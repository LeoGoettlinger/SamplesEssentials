package com.earth2me.essentials;

import com.earth2me.essentials.config.ConfigurateUtil;
import com.earth2me.essentials.config.EssentialsConfiguration;
import com.earth2me.essentials.config.EssentialsUserConfiguration;
import com.earth2me.essentials.config.entities.LazyLocation;
import com.earth2me.essentials.craftbukkit.BanLookup;
import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.userstorage.ModernUUIDCache;
import com.earth2me.essentials.utils.StringUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.essentialsx.api.v2.services.mail.MailMessage;
import nu.studer.java.util.OrderedProperties;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.BanList.Type;
import org.bukkit.World.Environment;

public class EssentialsUpgrade {
   public static final FileFilter YML_FILTER = pathname -> pathname.isFile() && pathname.getName().endsWith(".yml");
   private static final String PATTERN_CONFIG_UUID_REGEX = "(?mi)^uuid:\\s*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\\s*$";
   private static final Pattern PATTERN_CONFIG_UUID = Pattern.compile("(?mi)^uuid:\\s*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\\s*$");
   private static final String PATTERN_CONFIG_NAME_REGEX = "(?mi)^lastAccountName:\\s*[\"']?(\\w+)[\"']?\\s*$";
   private static final Pattern PATTERN_CONFIG_NAME = Pattern.compile("(?mi)^lastAccountName:\\s*[\"']?(\\w+)[\"']?\\s*$");
   private final transient net.ess3.api.IEssentials ess;
   private final transient EssentialsConfiguration doneFile;

   EssentialsUpgrade(final net.ess3.api.IEssentials essentials) {
      this.ess = essentials;
      if (!this.ess.getDataFolder().exists()) {
         this.ess.getDataFolder().mkdirs();
      }

      this.doneFile = new EssentialsConfiguration(new File(this.ess.getDataFolder(), "upgrades-done.yml"));
      this.doneFile.load();
   }

   private static void uuidFileConvert(final net.ess3.api.IEssentials ess, final Boolean ignoreUFCache) {
      ess.getLogger().info("Starting Essentials UUID userdata conversion");
      File userdir = new File(ess.getDataFolder(), "userdata");
      if (userdir.exists()) {
         int countFiles = 0;
         int countFails = 0;
         int countEssCache = 0;
         int countBukkit = 0;
         ess.getLogger().info("Found " + userdir.list().length + " files to convert...");

         for (String string : userdir.list()) {
            if (string.endsWith(".yml") && string.length() >= 5) {
               int showProgress = countFiles % 250;
               if (showProgress == 0) {
                  ess.getLogger().info("Converted " + countFiles + "/" + userdir.list().length);
               }

               countFiles++;
               String name = string.substring(0, string.length() - 4);
               UUID uuid = null;

               try {
                  UUID.fromString(name);
               } catch (IllegalArgumentException var24) {
                  File file = new File(userdir, string);
                  EssentialsConfiguration conf = new EssentialsConfiguration(file);
                  conf.load();
                  conf.setProperty("lastAccountName", name);
                  conf.save();
                  String uuidConf = ignoreUFCache ? "force-uuid" : "uuid";
                  String uuidString = conf.getString(uuidConf, null);

                  for (int i = 0; i < 4; i++) {
                     try {
                        uuid = UUID.fromString(uuidString);
                        countEssCache++;
                        break;
                     } catch (Exception var23) {
                        if (conf.getBoolean("npc", false)) {
                           uuid = UUID.nameUUIDFromBytes(
                              ("NPC:" + (ess.getSettings().isSafeUsermap() ? StringUtil.safeString(name) : name)).getBytes(Charsets.UTF_8)
                           );
                           break;
                        }

                        OfflinePlayer player = ess.getServer().getOfflinePlayer(name);
                        uuid = player.getUniqueId();
                        if (uuid != null) {
                           countBukkit++;
                           break;
                        }
                     }
                  }

                  if (uuid != null) {
                     conf.blockingSave();
                     EssentialsUserConfiguration config = new EssentialsUserConfiguration(name, uuid, new File(userdir, uuid + ".yml"));
                     config.convertLegacyFile();
                     ess.getUsers().loadUncachedUser(uuid);
                  } else {
                     countFails++;
                  }
               }
            }
         }

         ess.getLogger().info("Converted " + countFiles + "/" + countFiles + ".  Conversion complete.");
         ess.getLogger().info("Converted via cache: " + countEssCache + " :: Converted via lookup: " + countBukkit + " :: Failed to convert: " + countFails);
         ess.getLogger().info("To rerun the conversion type /essentials uuidconvert");
      }
   }

   private void updateRandomTeleport() {
      if (!this.doneFile.getBoolean("updateRandomTeleport", false)) {
         EssentialsConfiguration config = this.ess.getRandomTeleport().getConfig();
         if (config.getRootNode() != null) {
            LazyLocation center = config.getLocation("center");
            Location centerLoc = center != null ? center.location() : null;
            if (center != null && centerLoc != null) {
               double minRange = config.getDouble("min-range", Double.MIN_VALUE);
               double maxRange = config.getDouble("max-range", Double.MIN_VALUE);

               for (World world : this.ess.getServer().getWorlds()) {
                  String propPrefix = "locations." + world.getName() + ".";
                  config.setProperty(propPrefix + "center", centerLoc);
                  if (minRange != Double.MIN_VALUE) {
                     config.setProperty(propPrefix + "min-range", minRange);
                  }

                  if (maxRange != Double.MIN_VALUE) {
                     config.setProperty(propPrefix + "max-range", maxRange);
                  }
               }
            }

            config.removeProperty("center");
            config.blockingSave();
         }

         this.doneFile.setProperty("updateRandomTeleport", true);
         this.doneFile.save();
         this.ess.getLogger().info("Done converting random teleport config.");
      }
   }

   private void convertMailList() {
      if (!this.doneFile.getBoolean("updateUsersMailList", false)) {
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();

            for (File file : userFiles) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  EssentialsConfiguration config = new EssentialsConfiguration(file);

                  try {
                     config.load();
                     if (config.hasProperty("mail") && config.isList("mail")) {
                        ArrayList<MailMessage> messages = new ArrayList<>();

                        for (String mailStr : Collections.synchronizedList(config.getList("mail", String.class))) {
                           if (mailStr != null) {
                              messages.add(new MailMessage(false, true, null, null, 0L, 0L, mailStr));
                           }
                        }

                        config.removeProperty("mail");
                        config.setExplicitList("mail", messages, (new TypeToken<List<MailMessage>>() {}).getType());
                        config.blockingSave();
                     }
                  } catch (RuntimeException var11) {
                     this.ess.getLogger().log(Level.INFO, "File: " + file);
                     throw var11;
                  }
               }
            }

            this.doneFile.setProperty("updateUsersMailList", true);
            this.doneFile.save();
            this.ess.getLogger().info("Done converting mail list.");
         }
      }
   }

   private void convertStupidCamelCaseUserdataKeys() {
      if (!this.doneFile.getBoolean("updateUsersStupidLegacyPathNames", false)) {
         this.ess.getLogger().info("Attempting to migrate legacy userdata keys to Configurate");
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();

            for (File file : userFiles) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  EssentialsConfiguration config = new EssentialsConfiguration(file);

                  try {
                     config.load();
                     if (config.hasProperty("muteReason")) {
                        String reason = config.getString("muteReason", null);
                        config.removeProperty("muteReason");
                        config.setProperty("mute-reason", reason);
                     }

                     if (config.hasProperty("ipAddress")) {
                        String ip = config.getString("ipAddress", null);
                        config.removeProperty("ipAddress");
                        config.setProperty("ip-address", ip);
                     }

                     if (config.hasProperty("lastAccountName")) {
                        String name = config.getString("lastAccountName", null);
                        config.removeProperty("lastAccountName");
                        config.setProperty("last-account-name", name);
                     }

                     if (config.hasProperty("acceptingPay")) {
                        boolean isPay = config.getBoolean("acceptingPay", true);
                        config.removeProperty("acceptingPay");
                        config.setProperty("accepting-pay", isPay);
                     }

                     config.blockingSave();
                  } catch (RuntimeException var9) {
                     this.ess.getLogger().log(Level.INFO, "File: " + file);
                     throw var9;
                  }
               }
            }

            this.doneFile.setProperty("updateUsersStupidLegacyPathNames", true);
            this.doneFile.save();
            this.ess.getLogger().info("Done converting legacy userdata keys to Configurate.");
         }
      }
   }

   private void purgeBrokenNpcAccounts() {
      if (!this.doneFile.getBoolean("updatePurgeBrokenNpcAccounts", false)) {
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();
            if (userFiles.length != 0) {
               File backupFolder = new File(this.ess.getDataFolder(), "userdata-npc-backup");
               if (backupFolder.exists()) {
                  this.ess.getLogger().info("NPC backup folder already exists; skipping NPC purge.");
                  this.ess
                     .getLogger()
                     .info("To finish purging broken NPC accounts, rename the \"plugins/Essentials/userdata-npc-backup\" folder and restart your server.");
               } else if (!backupFolder.mkdir()) {
                  this.ess.getLogger().info("Skipping NPC purge due to error creating backup folder.");
               } else {
                  this.ess.getLogger().info("#===========================================================================#");
                  this.ess.getLogger().info(" EssentialsX will now purge any NPC accounts which were incorrectly created.");
                  this.ess.getLogger().info(" Only NPC accounts with the default starting balance will be deleted. If");
                  this.ess.getLogger().info(" they turn out to be valid NPC accounts, they will be re-created as needed.");
                  this.ess.getLogger().info(" Any files deleted here will be backed up to the ");
                  this.ess.getLogger().info(" \"plugins/Essentials/userdata-npc-backup\" folder. If you notice any files");
                  this.ess.getLogger().info(" have been purged incorrectly, you should restore it from the backup and");
                  this.ess.getLogger().info(" report it to us on GitHub:");
                  this.ess.getLogger().info(" https://github.com/EssentialsX/Essentials/issues/new/choose");
                  this.ess.getLogger().info("");
                  this.ess.getLogger().info(" NOTE: This is a one-time process and will take several minutes if you have");
                  this.ess.getLogger().info(" a lot of userdata files! If you interrupt this process, EssentialsX will");
                  this.ess.getLogger().info(" skip the process until you rename or remove the backup folder.");
                  this.ess.getLogger().info("#===========================================================================#");
                  int totalUserFiles = userFiles.length;
                  this.ess.getLogger().info("Found ~" + totalUserFiles + " files under \"plugins/Essentials/userdata\"...");
                  AtomicInteger movedAccounts = new AtomicInteger(0);
                  AtomicInteger totalAccounts = new AtomicInteger(0);
                  long feedbackInterval = Math.min(15L, 1L + Math.round(2.1 * Math.log10(userFiles.length)));
                  ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                  ScheduledFuture<?> feedbackTask = executor.scheduleWithFixedDelay(
                     () -> this.ess
                        .getLogger()
                        .info("Scanned " + totalAccounts.get() + "/" + totalUserFiles + " accounts; moved " + movedAccounts.get() + " accounts"),
                     5L,
                     feedbackInterval,
                     TimeUnit.SECONDS
                  );

                  for (File file : userFiles) {
                     if (file.isFile() && file.getName().endsWith(".yml")) {
                        EssentialsConfiguration config = new EssentialsConfiguration(file);

                        try {
                           totalAccounts.incrementAndGet();
                           config.load();
                           if (config.getKeys().size() <= 4 && config.getBoolean("npc", false)) {
                              BigDecimal money = config.getBigDecimal("money", null);
                              if (money != null
                                 && money.compareTo(this.ess.getSettings().getStartingBalance()) == 0
                                 && (config.getKeys().size() != 4 || config.hasProperty("last-account-name") || !config.hasProperty("mail"))) {
                                 try {
                                    Files.move(file, new File(backupFolder, file.getName()));
                                    movedAccounts.incrementAndGet();
                                 } catch (IOException var18) {
                                    this.ess.getLogger().log(Level.SEVERE, "Error while moving NPC file", (Throwable)var18);
                                 }
                              }
                           }
                        } catch (RuntimeException var19) {
                           this.ess.getLogger().log(Level.INFO, "File: " + file);
                           feedbackTask.cancel(false);
                           executor.shutdown();
                           throw var19;
                        }
                     }
                  }

                  feedbackTask.cancel(false);
                  executor.shutdown();
                  this.doneFile.setProperty("updatePurgeBrokenNpcAccounts", true);
                  this.doneFile.save();
                  this.ess.getLogger().info("#===========================================================================#");
                  this.ess.getLogger().info(" EssentialsX has finished purging NPC accounts.");
                  this.ess.getLogger().info("");
                  this.ess.getLogger().info(" Deleted accounts: " + movedAccounts);
                  this.ess.getLogger().info(" Total accounts processed: " + totalAccounts);
                  this.ess.getLogger().info("");
                  this.ess.getLogger().info(" Purged accounts have been backed up to");
                  this.ess.getLogger().info(" \"plugins/Essentials/userdata-npc-backup\", and can be restored from there");
                  this.ess.getLogger().info(" if needed. Please report any files which have been incorrectly deleted");
                  this.ess.getLogger().info(" to us on GitHub:");
                  this.ess.getLogger().info(" https://github.com/EssentialsX/Essentials/issues/new/choose");
                  this.ess.getLogger().info("#===========================================================================#");
               }
            }
         }
      }
   }

   private void convertIgnoreList() {
      Pattern pattern = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
      if (!this.doneFile.getBoolean("updateUsersIgnoreListUUID", false)) {
         this.ess.getLogger().info("Attempting to migrate ignore list to UUIDs");
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();

            for (File file : userFiles) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  EssentialsConfiguration config = new EssentialsConfiguration(file);

                  try {
                     config.load();
                     if (config.hasProperty("ignore")) {
                        List<String> migratedIgnores = new ArrayList<>();

                        for (String name : Collections.synchronizedList(config.getList("ignore", String.class))) {
                           if (name != null) {
                              if (pattern.matcher(name.trim()).matches()) {
                                 this.ess.getLogger().info("Detected already migrated ignore list!");
                                 return;
                              }

                              User user = this.ess.getOfflineUser(name);
                              if (user != null && user.getBase() != null) {
                                 migratedIgnores.add(user.getBase().getUniqueId().toString());
                              }
                           }
                        }

                        config.removeProperty("ignore");
                        config.setProperty("ignore", migratedIgnores);
                        config.blockingSave();
                     }
                  } catch (RuntimeException var13) {
                     this.ess.getLogger().log(Level.INFO, "File: " + file);
                     throw var13;
                  }
               }
            }

            this.doneFile.setProperty("updateUsersIgnoreListUUID", true);
            this.doneFile.save();
            this.ess.getLogger().info("Done converting ignore list.");
         }
      }
   }

   public void convertKits() {
      if (!Essentials.TESTING) {
         Kits kits = this.ess.getKits();
         EssentialsConfiguration config = kits.getRootConfig();
         if (!this.doneFile.getBoolean("kitsyml", false)) {
            this.ess.getLogger().info("Attempting to convert old kits in config.yml to new kits.yml");
            CommentedConfigurationNode section = this.ess.getSettings().getKitSection();
            if (section == null) {
               this.ess.getLogger().info("No kits found to migrate.");
            } else {
               Map<String, Object> legacyKits = ConfigurateUtil.getRawMap(section);

               for (Entry<String, Object> entry : legacyKits.entrySet()) {
                  this.ess.getLogger().info("Converting " + entry.getKey());
                  config.setRaw("kits." + entry.getKey(), entry.getValue());
               }

               config.save();
               this.doneFile.setProperty("kitsyml", true);
               this.doneFile.save();
               this.ess.getLogger().info("Done converting kits.");
            }
         }
      }
   }

   private void moveMotdRulesToFile(final String name) {
      if (!this.doneFile.getBoolean("move" + name + "ToFile", false)) {
         try {
            File file = new File(this.ess.getDataFolder(), name + ".txt");
            if (file.exists()) {
               return;
            }

            File configFile = new File(this.ess.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
               return;
            }

            EssentialsConfiguration conf = new EssentialsConfiguration(configFile);
            conf.load();
            List<String> lines = conf.getList(name, String.class);
            if (lines != null && !lines.isEmpty()) {
               if (!file.createNewFile()) {
                  throw new IOException("Failed to create file " + file);
               }

               PrintWriter writer = new PrintWriter(file);

               for (String line : lines) {
                  writer.println(line);
               }

               writer.close();
            }

            this.doneFile.setProperty("move" + name + "ToFile", true);
            this.doneFile.save();
         } catch (IOException var9) {
            this.ess.getLogger().log(Level.SEVERE, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("upgradingFilesError")), (Throwable)var9);
         }
      }
   }

   private void removeLinesFromConfig(final File file, final String regex, final String info) throws Exception {
      boolean needUpdate = false;
      BufferedReader bReader = new BufferedReader(new FileReader(file));
      File tempFile = File.createTempFile("essentialsupgrade", ".tmp.yml", this.ess.getDataFolder());
      BufferedWriter bWriter = new BufferedWriter(new FileWriter(tempFile));

      while (true) {
         String line = bReader.readLine();
         if (line == null) {
            bReader.close();
            bWriter.close();
            if (needUpdate) {
               if (!file.renameTo(new File(file.getParentFile(), file.getName().concat("." + System.currentTimeMillis() + ".upgradebackup")))) {
                  throw new Exception(I18n.tlLiteral("configFileMoveError"));
               }

               if (!tempFile.renameTo(file)) {
                  throw new Exception(I18n.tlLiteral("configFileRenameError"));
               }
            } else {
               tempFile.delete();
            }

            return;
         }

         if (line.matches(regex)) {
            if (!needUpdate && info != null) {
               bWriter.write(info, 0, info.length());
               bWriter.newLine();
            }

            needUpdate = true;
         } else {
            if (line.endsWith("\r\n")) {
               bWriter.write(line, 0, line.length() - 2);
            } else if (!line.endsWith("\r") && !line.endsWith("\n")) {
               bWriter.write(line, 0, line.length());
            } else {
               bWriter.write(line, 0, line.length() - 1);
            }

            bWriter.newLine();
         }
      }
   }

   private void updateUsersPowerToolsFormat() {
      if (!this.doneFile.getBoolean("updateUsersPowerToolsFormat", false)) {
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();

            for (File file : userFiles) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  EssentialsConfiguration config = new EssentialsConfiguration(file);

                  try {
                     config.load();
                     if (config.hasProperty("powertools")) {
                        Map<String, Object> powertools = ConfigurateUtil.getRawMap(config.getSection("powertools"));
                        if (!powertools.isEmpty()) {
                           for (Entry<String, Object> entry : powertools.entrySet()) {
                              if (entry.getValue() instanceof String) {
                                 List<String> temp = new ArrayList<>();
                                 temp.add((String)entry.getValue());
                                 powertools.put(entry.getKey(), temp);
                              }
                           }

                           config.setRaw("powertools", powertools);
                           config.blockingSave();
                        }
                     }
                  } catch (RuntimeException var12) {
                     this.ess.getLogger().log(Level.INFO, "File: " + file);
                     throw var12;
                  }
               }
            }

            this.doneFile.setProperty("updateUsersPowerToolsFormat", true);
            this.doneFile.save();
         }
      }
   }

   private void updateUsersHomesFormat() {
      if (!this.doneFile.getBoolean("updateUsersHomesFormat", false)) {
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.exists() && userdataFolder.isDirectory()) {
            File[] userFiles = userdataFolder.listFiles();

            for (File file : userFiles) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  EssentialsConfiguration config = new EssentialsConfiguration(file);

                  try {
                     config.load();
                     if (config.hasProperty("home") && config.hasProperty("home.default")) {
                        String defworld = config.getString("home.default", null);
                        Location defloc = this.getFakeLocation(config.getRootNode(), "home.worlds." + defworld);
                        if (defloc != null) {
                           config.setProperty("homes.home", defloc);
                        }

                        Set<String> worlds = ConfigurateUtil.getKeys(config.getSection("home.worlds"));
                        if (!worlds.isEmpty()) {
                           for (String world : worlds) {
                              if (!defworld.equalsIgnoreCase(world)) {
                                 Location loc = this.getFakeLocation(config.getRootNode(), "home.worlds." + world);
                                 if (loc != null) {
                                    String worldName = loc.getWorld().getName().toLowerCase(Locale.ENGLISH);
                                    config.setProperty("homes." + worldName, loc);
                                 }
                              }
                           }

                           config.removeProperty("home");
                           config.blockingSave();
                        }
                     }
                  } catch (RuntimeException var15) {
                     this.ess.getLogger().log(Level.INFO, "File: " + file);
                     throw var15;
                  }
               }
            }

            this.doneFile.setProperty("updateUsersHomesFormat", true);
            this.doneFile.save();
         }
      }
   }

   private void sanitizeAllUserFilenames() {
      if (!this.doneFile.getBoolean("sanitizeAllUserFilenames", false)) {
         File usersFolder = new File(this.ess.getDataFolder(), "userdata");
         if (usersFolder.exists()) {
            File[] listOfFiles = usersFolder.listFiles();

            for (File listOfFile : listOfFiles) {
               String filename = listOfFile.getName();
               if (listOfFile.isFile() && filename.endsWith(".yml")) {
                  String sanitizedFilename = StringUtil.sanitizeFileName(filename.substring(0, filename.length() - 4)) + ".yml";
                  if (!sanitizedFilename.equals(filename)) {
                     File tmpFile = new File(listOfFile.getParentFile(), sanitizedFilename + ".tmp");
                     File newFile = new File(listOfFile.getParentFile(), sanitizedFilename);
                     if (!listOfFile.renameTo(tmpFile)) {
                        this.ess
                           .getLogger()
                           .log(Level.WARNING, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("userdataMoveError", filename, sanitizedFilename)));
                     } else if (newFile.exists()) {
                        this.ess
                           .getLogger()
                           .log(Level.WARNING, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("duplicatedUserdata", filename, sanitizedFilename)));
                     } else if (!tmpFile.renameTo(newFile)) {
                        this.ess
                           .getLogger()
                           .log(
                              Level.WARNING,
                              this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("userdataMoveBackError", sanitizedFilename, sanitizedFilename))
                           );
                     }
                  }
               }
            }

            this.doneFile.setProperty("sanitizeAllUserFilenames", true);
            this.doneFile.save();
         }
      }
   }

   private World getFakeWorld(final String name) {
      File bukkitDirectory = this.ess.getDataFolder().getParentFile().getParentFile();
      File worldDirectory = new File(bukkitDirectory, name);
      return worldDirectory.exists() && worldDirectory.isDirectory() ? new FakeWorld(worldDirectory.getName(), Environment.NORMAL) : null;
   }

   private Location getFakeLocation(final CommentedConfigurationNode config, final String path) {
      String worldName = config.getString((path != null ? path + "." : "") + "world");
      if (worldName != null && !worldName.isEmpty()) {
         World world = this.getFakeWorld(worldName);
         return world == null
            ? null
            : new Location(
               world,
               config.node(new Object[]{"x"}).getDouble(0.0),
               config.node(new Object[]{"y"}).getDouble(0.0),
               config.node(new Object[]{"z"}).getDouble(0.0),
               config.node(new Object[]{"yaw"}).getFloat(0.0F),
               config.node(new Object[]{"pitch"}).getFloat(0.0F)
            );
      } else {
         return null;
      }
   }

   private void deleteOldItemsCsv() {
      if (!this.doneFile.getBoolean("deleteOldItemsCsv", false)) {
         File file = new File(this.ess.getDataFolder(), "items.csv");
         if (file.exists()) {
            try {
               Set<BigInteger> oldconfigs = new HashSet<>();
               oldconfigs.add(new BigInteger("66ec40b09ac167079f558d1099e39f10", 16));
               oldconfigs.add(new BigInteger("34284de1ead43b0bee2aae85e75c041d", 16));
               oldconfigs.add(new BigInteger("c33bc9b8ee003861611bbc2f48eb6f4f", 16));
               oldconfigs.add(new BigInteger("6ff17925430735129fc2a02f830c1daa", 16));
               MessageDigest digest = ManagedFile.getDigest();
               BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
               byte[] buffer = new byte[1024];
               DigestInputStream dis = new DigestInputStream(bis, digest);

               try {
                  while (dis.read(buffer) != -1) {
                  }
               } catch (Throwable var10) {
                  try {
                     dis.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }

                  throw var10;
               }

               dis.close();
               BigInteger var12 = new BigInteger(1, digest.digest());
               if (oldconfigs.contains(var12) && !file.delete()) {
                  throw new IOException("Could not delete file " + file);
               }

               this.doneFile.setProperty("deleteOldItemsCsv", true);
               this.doneFile.save();
            } catch (IOException var11) {
               this.ess.getLogger().log(Level.SEVERE, var11.getMessage(), (Throwable)var11);
            }
         }
      }
   }

   private void updateSpawnsToNewSpawnsConfig() {
      if (!this.doneFile.getBoolean("updateSpawnsToNewSpawnsConfig", false)) {
         File configFile = new File(this.ess.getDataFolder(), "spawn.yml");
         if (configFile.exists()) {
            EssentialsConfiguration config = new EssentialsConfiguration(configFile);

            try {
               config.load();
               if (!config.hasProperty("spawns")) {
                  for (Entry<String, CommentedConfigurationNode> entry : config.getMap().entrySet()) {
                     Location loc = this.getFakeLocation(entry.getValue(), entry.getKey());
                     config.setProperty(entry.getKey(), loc);
                  }

                  if (!configFile.renameTo(new File(this.ess.getDataFolder(), "spawn.yml.old"))) {
                     throw new Exception(I18n.tlLiteral("fileRenameError", "spawn.yml"));
                  }

                  config.blockingSave();
               }
            } catch (Exception var6) {
               this.ess.getLogger().log(Level.SEVERE, var6.getMessage(), (Throwable)var6);
            }
         }

         this.doneFile.setProperty("updateSpawnsToNewSpawnsConfig", true);
         this.doneFile.save();
      }
   }

   private void updateJailsToNewJailsConfig() {
      if (!this.doneFile.getBoolean("updateJailsToNewJailsConfig", false)) {
         File configFile = new File(this.ess.getDataFolder(), "jail.yml");
         if (configFile.exists()) {
            EssentialsConfiguration config = new EssentialsConfiguration(configFile);

            try {
               config.load();
               if (!config.hasProperty("jails")) {
                  for (Entry<String, CommentedConfigurationNode> entry : config.getMap().entrySet()) {
                     Location loc = this.getFakeLocation(entry.getValue(), entry.getKey());
                     config.setProperty(entry.getKey(), loc);
                  }

                  if (!configFile.renameTo(new File(this.ess.getDataFolder(), "jail.yml.old"))) {
                     throw new Exception(I18n.tlLiteral("fileRenameError", "jail.yml"));
                  }

                  config.blockingSave();
               }
            } catch (Exception var6) {
               this.ess.getLogger().log(Level.SEVERE, var6.getMessage(), (Throwable)var6);
            }
         }

         this.doneFile.setProperty("updateJailsToNewJailsConfig", true);
         this.doneFile.save();
      }
   }

   private void warnMetrics() {
      if (!this.doneFile.getBoolean("warnMetrics", false)) {
         this.doneFile.setProperty("warnMetrics", true);
         this.doneFile.save();
      }
   }

   private void uuidFileChange() {
      if (!this.doneFile.getBoolean("uuidFileChange", false)) {
         Boolean ignoreUFCache = this.doneFile.getBoolean("ignore-userfiles-cache", false);
         File userdir = new File(this.ess.getDataFolder(), "userdata");
         if (userdir.exists()) {
            int countFiles = 0;
            int countReqFiles = 0;

            for (String string : userdir.list()) {
               if (string.endsWith(".yml") && string.length() >= 5) {
                  countFiles++;
                  String name = string.substring(0, string.length() - 4);
                  UUID uuid = null;

                  try {
                     uuid = UUID.fromString(name);
                  } catch (IllegalArgumentException var13) {
                     countReqFiles++;
                  }

                  if (countFiles > 100) {
                     break;
                  }
               }
            }

            if (countReqFiles >= 1) {
               this.ess.getLogger().info("#### Starting Essentials UUID userdata conversion in a few seconds. ####");
               this.ess.getLogger().info("We recommend you take a backup of your server before upgrading from the old username system.");

               try {
                  Thread.sleep(15000L);
               } catch (InterruptedException var12) {
               }

               uuidFileConvert(this.ess, ignoreUFCache);
               this.doneFile.setProperty("uuidFileChange", true);
               this.doneFile.save();
            }
         }
      }
   }

   private void banFormatChange() {
      if (!this.doneFile.getBoolean("banFormatChange", false)) {
         this.ess.getLogger().info("Starting Essentials ban format conversion");
         File userdir = new File(this.ess.getDataFolder(), "userdata");
         if (userdir.exists()) {
            int countFiles = 0;
            this.ess.getLogger().info("Found " + userdir.list().length + " files to convert...");

            for (String string : userdir.list()) {
               if (string.endsWith(".yml") && string.length() >= 5) {
                  int showProgress = countFiles % 250;
                  if (showProgress == 0) {
                     this.ess.getLogger().info("Converted " + countFiles + "/" + userdir.list().length);
                  }

                  countFiles++;
                  File pFile = new File(userdir, string);
                  EssentialsConfiguration conf = new EssentialsConfiguration(pFile);
                  conf.load();
                  String banReason;
                  if (conf.hasProperty("ban.reason")) {
                     banReason = conf.getString("ban.reason", null);
                  } else {
                     banReason = null;
                  }

                  String playerName = conf.getString("lastAccountName", null);
                  if (playerName != null && playerName.length() > 1 && banReason != null && banReason.length() > 1) {
                     long banTimeout;
                     try {
                        if (conf.hasProperty("ban.timeout")) {
                           banTimeout = Long.parseLong(conf.getString("ban.timeout", null));
                        } else {
                           banTimeout = 0L;
                        }
                     } catch (NumberFormatException var15) {
                        banTimeout = 0L;
                     }

                     if (BanLookup.isBanned(this.ess, playerName)) {
                        this.updateBan(playerName, banReason, banTimeout);
                     }
                  }

                  conf.removeProperty("ban");
                  conf.save();
               }
            }

            this.doneFile.setProperty("banFormatChange", true);
            this.doneFile.save();
            this.ess.getLogger().info("Ban format update complete.");
         }
      }
   }

   private void updateBan(final String playerName, final String banReason, final Long banTimeout) {
      Bukkit.getBanList(Type.NAME).addBan(playerName, banReason, banTimeout == 0L ? null : new Date(banTimeout), "Console");
   }

   private void generateUidCache() {
      if (!this.doneFile.getBoolean("newUidCacheBuilt", false)) {
         File usermapFile = new File(this.ess.getDataFolder(), "usermap.bin");
         File uidsFile = new File(this.ess.getDataFolder(), "uuids.bin");
         File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
         if (userdataFolder.isDirectory() && !usermapFile.exists() && !uidsFile.exists()) {
            try {
               if (!usermapFile.createNewFile() || !uidsFile.createNewFile()) {
                  this.ess.getLogger().warning("Couldn't create usermap.bin or uuids.bin, aborting");
                  return;
               }

               Map<UUID, Long> uuids = new HashMap<>();
               Map<String, UUID> nameToUuidMap = new HashMap<>();
               File[] files = userdataFolder.listFiles(YML_FILTER);
               if (files != null) {
                  for (File file : files) {
                     try {
                        String fileName = file.getName();
                        UUID uuid = UUID.fromString(fileName.substring(0, fileName.length() - 4));
                        EssentialsConfiguration config = new EssentialsConfiguration(file);
                        config.load();
                        String name = config.getString("last-account-name", null);
                        name = this.ess.getSettings().isSafeUsermap() ? StringUtil.safeString(name) : name;
                        long time = config.getLong("timestamps.logout", 0L);
                        if (name != null) {
                           if (nameToUuidMap.containsKey(name)) {
                              UUID oldUuid = nameToUuidMap.get(name);
                              if (oldUuid.version() >= uuid.version() && (oldUuid.version() != uuid.version() || uuids.get(oldUuid) >= time)) {
                                 this.ess.getLogger().warning("Found UUID for " + name + ": " + uuid + " (old: " + oldUuid + "). Skipping.");
                                 continue;
                              }

                              this.ess.getLogger().warning("New UUID found for " + name + ": " + uuid + " (old: " + oldUuid + "). Replacing.");
                              uuids.remove(oldUuid);
                           }

                           uuids.put(uuid, time);
                           nameToUuidMap.put(name, uuid);
                        }
                     } catch (IndexOutOfBoundsException | IllegalArgumentException var18) {
                     }
                  }
               }

               if (!nameToUuidMap.isEmpty()) {
                  ModernUUIDCache.writeNameUuidMap(usermapFile, nameToUuidMap);
               }

               if (!uuids.isEmpty()) {
                  ModernUUIDCache.writeUuidCache(uidsFile, uuids.keySet());
               }

               this.doneFile.setProperty("newUidCacheBuilt", true);
               this.doneFile.save();
            } catch (IOException var19) {
               this.ess.getLogger().log(Level.SEVERE, "Error while generating initial uuids/names cache", (Throwable)var19);
            }
         } else {
            this.ess.getLogger().warning("Missing userdata folder, aborting");
            this.doneFile.setProperty("newUidCacheBuilt", true);
            this.doneFile.save();
         }
      }
   }

   public void upgradeLang() {
      if (!this.doneFile.getBoolean("updateLegacyToAdventure", false)) {
         this.ess.getLogger().log(Level.WARNING, "Beginning Adventure locale file conversion.");

         try {
            File dataFolder = this.ess.getDataFolder();
            if (!dataFolder.exists() || !dataFolder.isDirectory()) {
               return;
            }

            File backDir = new File(dataFolder, "msg-backups-" + System.currentTimeMillis());
            if (backDir.exists() || !backDir.mkdir()) {
               this.ess.getLogger().log(Level.SEVERE, "Unable to make msg-backups dir?!");
               return;
            }

            File messagesDir = new File(dataFolder, "messages");
            messagesDir.mkdir();
            File[] files = dataFolder.listFiles();
            boolean isThereAtLeastOneBackup = false;
            if (files != null) {
               for (File file : files) {
                  if (file.getName().endsWith(".properties")) {
                     File newFile = new File(messagesDir, file.getName());
                     File backup = new File(backDir, file.getName());
                     Files.move(file, backup);
                     isThereAtLeastOneBackup = true;
                     OrderedProperties properties = new OrderedProperties();
                     properties.load(Files.newReader(backup, Charsets.UTF_8));

                     for (String key : properties.stringPropertyNames()) {
                        String value = properties.getProperty(key);
                        properties.setProperty(key, this.ess.getAdventureFacet().legacyToMini(this.ess.getAdventureFacet().escapeTags(value), true));
                     }

                     properties.store(Files.newWriter(newFile, Charsets.UTF_8), null);
                  }
               }
            }

            if (!isThereAtLeastOneBackup) {
               backDir.delete();
            }

            this.doneFile.setProperty("updateLegacyToAdventure", true);
            this.doneFile.save();
         } catch (Throwable var16) {
            this.ess.getLogger().log(Level.SEVERE, "Error while upgrading custom locales", var16);
         }
      }
   }

   public void beforeSettings() {
      if (!Essentials.TESTING) {
         if (!this.ess.getDataFolder().exists()) {
            this.ess.getDataFolder().mkdirs();
         }

         this.moveMotdRulesToFile("motd");
         this.moveMotdRulesToFile("rules");
      }
   }

   public void preModules() {
      if (!Essentials.TESTING) {
         this.generateUidCache();
      }
   }

   public void afterSettings() {
      if (!Essentials.TESTING) {
         this.sanitizeAllUserFilenames();
         this.updateUsersPowerToolsFormat();
         this.updateUsersHomesFormat();
         this.deleteOldItemsCsv();
         this.updateSpawnsToNewSpawnsConfig();
         this.updateJailsToNewJailsConfig();
         this.uuidFileChange();
         this.banFormatChange();
         this.warnMetrics();
         this.convertIgnoreList();
         this.convertStupidCamelCaseUserdataKeys();
         this.convertMailList();
         this.purgeBrokenNpcAccounts();
         this.updateRandomTeleport();
      }
   }
}
