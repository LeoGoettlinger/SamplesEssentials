package vrx.economy.vRXClans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class Clan {
   private String name;
   private double bankBalance;
   private Map<UUID, Clan.ClanRank> members;
   private Set<String> allies;
   private Set<String> rivals;
   private String tag;
   private String tagColor;
   private String description;
   private String iconMaterial;
   private String motd;
   private List<String> mailMessages;
   private int extraMemberSlots;
   private int extraHomeSlots;
   private int extraVaultRows;
   private int extraAllySlots;
   private int extraRivalSlots;
   private int teleportDelayReduction;
   private double teleportCostReductionPercent;
   private Map<String, Location> homes;
   private Set<String> completedAchievements;
   private int level;
   private double xp;
   private int lastRewardLevel;
   private Inventory vault;
   private Location baseLocation;
   private int purchasedRadiusBonus;
   private long xpBooster;
   private long hasteBooster;
   private long speedBooster;
   private long strengthBooster;
   private long lastUpkeepTime;
   private int questZombies;
   private int questDiamonds;
   private double questMoney;
   private boolean questZombiesDone;
   private boolean questDiamondsDone;
   private boolean questBankDone;
   private long lastQuestReset;
   private int kills;
   private int deaths;
   private String activeWar;
   private int warKills;
   private double warWager;
   private String cachedVaultBase64 = "";
   private boolean vaultDirty;
   private boolean baseRegen;
   private boolean baseSaturation;
   private Map<Clan.ClanRank, Map<String, Boolean>> rankPerms;
   private Map<Clan.ClanRank, String> rankDisplayNames;

   public Clan(String name, UUID leader) {
      this.name = name;
      this.bankBalance = 0.0;
      this.members = new HashMap<>();
      this.members.put(leader, Clan.ClanRank.LEADER);
      this.allies = new HashSet<>();
      this.rivals = new HashSet<>();
      this.tag = "";
      this.tagColor = "&b";
      this.description = "";
      this.iconMaterial = "SHIELD";
      this.motd = "";
      this.mailMessages = new ArrayList<>();
      this.extraMemberSlots = 0;
      this.extraHomeSlots = 0;
      this.extraVaultRows = 0;
      this.extraAllySlots = 0;
      this.extraRivalSlots = 0;
      this.teleportDelayReduction = 0;
      this.teleportCostReductionPercent = 0.0;
      this.homes = new LinkedHashMap<>();
      this.completedAchievements = new HashSet<>();
      this.level = 1;
      this.xp = 0.0;
      this.lastRewardLevel = 1;
      this.purchasedRadiusBonus = 0;
      this.xpBooster = 0L;
      this.hasteBooster = 0L;
      this.speedBooster = 0L;
      this.strengthBooster = 0L;
      this.lastUpkeepTime = System.currentTimeMillis();
      this.lastQuestReset = System.currentTimeMillis();
      this.kills = 0;
      this.deaths = 0;
      this.activeWar = null;
      this.warKills = 0;
      this.warWager = 0.0;
      this.baseRegen = false;
      this.baseSaturation = false;
      this.rankPerms = new HashMap<>();

      for (Clan.ClanRank r : Clan.ClanRank.values()) {
         Map<String, Boolean> p = new HashMap<>();
         p.put("invite", r == Clan.ClanRank.OFFICER || r == Clan.ClanRank.CO_LEADER || r == Clan.ClanRank.LEADER);
         p.put("bank", r == Clan.ClanRank.CO_LEADER || r == Clan.ClanRank.LEADER);
         p.put("vault", r != Clan.ClanRank.RECRUIT);
         p.put("base", r == Clan.ClanRank.CO_LEADER || r == Clan.ClanRank.LEADER);
         p.put("shop", r == Clan.ClanRank.CO_LEADER || r == Clan.ClanRank.LEADER);
         p.put("war", r == Clan.ClanRank.CO_LEADER || r == Clan.ClanRank.LEADER);
         this.rankPerms.put(r, p);
      }

      this.rankDisplayNames = new EnumMap<>(Clan.ClanRank.class);

      for (Clan.ClanRank rank : Clan.ClanRank.values()) {
         this.rankDisplayNames.put(rank, rank.name());
      }
   }

   public boolean hasPermission(UUID uuid, String perm) {
      Clan.ClanRank rank = this.getRank(uuid);
      if (rank == null) {
         return false;
      } else {
         return rank == Clan.ClanRank.LEADER ? true : this.rankPerms.get(rank).getOrDefault(perm, false);
      }
   }

   public void togglePermission(Clan.ClanRank rank, String perm) {
      if (rank != Clan.ClanRank.LEADER) {
         boolean current = this.rankPerms.get(rank).getOrDefault(perm, false);
         this.rankPerms.get(rank).put(perm, !current);
      }
   }

   public boolean getRawPermission(Clan.ClanRank rank, String perm) {
      return rank == Clan.ClanRank.LEADER ? true : this.rankPerms.get(rank).getOrDefault(perm, false);
   }

   public String serializePerms() {
      StringBuilder sb = new StringBuilder();

      for (Entry<Clan.ClanRank, Map<String, Boolean>> entry : this.rankPerms.entrySet()) {
         sb.append(entry.getKey().name()).append("=");

         for (Entry<String, Boolean> p : entry.getValue().entrySet()) {
            sb.append(p.getKey()).append(":").append(p.getValue()).append(",");
         }

         sb.append("|");
      }

      return sb.toString();
   }

   public void loadPerms(String data) {
      if (data != null && !data.isEmpty()) {
         try {
            for (String rankData : data.split("\\|")) {
               if (!rankData.isEmpty()) {
                  String[] parts = rankData.split("=");
                  Clan.ClanRank rank = Clan.ClanRank.valueOf(parts[0]);

                  for (String pData : parts[1].split(",")) {
                     if (!pData.isEmpty()) {
                        String[] pParts = pData.split(":");
                        this.rankPerms.get(rank).put(pParts[0], Boolean.parseBoolean(pParts[1]));
                     }
                  }
               }
            }
         } catch (Exception var13) {
         }
      }
   }

   public String getRankDisplayName(Clan.ClanRank rank) {
      return this.rankDisplayNames.getOrDefault(rank, rank.name());
   }

   public void setRankDisplayName(Clan.ClanRank rank, String displayName) {
      if (rank != Clan.ClanRank.LEADER) {
         if (displayName != null) {
            String clean = displayName.trim();
            if (!clean.isEmpty()) {
               if (clean.length() > 24) {
                  clean = clean.substring(0, 24);
               }

               this.rankDisplayNames.put(rank, clean);
            }
         }
      }
   }

   public String serializeRankNames() {
      StringBuilder sb = new StringBuilder();

      for (Clan.ClanRank rank : Clan.ClanRank.values()) {
         sb.append(rank.name())
            .append("=")
            .append(Base64.getEncoder().encodeToString(this.getRankDisplayName(rank).getBytes(StandardCharsets.UTF_8)))
            .append("|");
      }

      return sb.toString();
   }

   public void loadRankNames(String data) {
      if (data != null && !data.isBlank()) {
         try {
            for (String rankData : data.split("\\|")) {
               if (!rankData.isBlank() && rankData.contains("=")) {
                  String[] parts = rankData.split("=", 2);
                  Clan.ClanRank rank = Clan.ClanRank.valueOf(parts[0]);
                  String decoded = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                  if (!decoded.isBlank()) {
                     this.rankDisplayNames.put(rank, decoded);
                  }
               }
            }
         } catch (Exception var9) {
         }
      }
   }

   public String getName() {
      return this.name;
   }

   public void setName(String newName) {
      this.name = newName;
   }

   public double getBankBalance() {
      return this.bankBalance;
   }

   public void setBankBalance(double amount) {
      this.bankBalance = Math.max(0.0, amount);
   }

   public Map<UUID, Clan.ClanRank> getMembers() {
      return this.members;
   }

   public void deposit(double amount) {
      if (Double.isFinite(amount) && !(amount <= 0.0)) {
         this.bankBalance += amount;
      }
   }

   public boolean withdraw(double amount) {
      if (!Double.isFinite(amount) || amount <= 0.0) {
         return false;
      } else if (this.bankBalance >= amount) {
         this.bankBalance -= amount;
         return true;
      } else {
         return false;
      }
   }

   public void addMember(UUID uuid) {
      this.members.put(uuid, Clan.ClanRank.RECRUIT);
   }

   public void removeMember(UUID uuid) {
      this.members.remove(uuid);
   }

   public Clan.ClanRank getRank(UUID uuid) {
      return this.members.get(uuid);
   }

   public void setRank(UUID uuid, Clan.ClanRank rank) {
      this.members.put(uuid, rank);
   }

   public Set<String> getAllies() {
      return this.allies;
   }

   public Set<String> getRivals() {
      return this.rivals;
   }

   public String getTag() {
      return this.tag == null ? "" : this.tag;
   }

   public void setTag(String tag) {
      this.tag = tag == null ? "" : tag.trim();
   }

   public String getTagColor() {
      return this.tagColor != null && !this.tagColor.isBlank() ? this.tagColor : "&b";
   }

   public void setTagColor(String tagColor) {
      this.tagColor = tagColor != null && !tagColor.isBlank() ? tagColor.trim() : "&b";
   }

   public String getDescription() {
      return this.description == null ? "" : this.description;
   }

   public void setDescription(String description) {
      String value = description == null ? "" : description.trim();
      this.description = value.length() > 160 ? value.substring(0, 160) : value;
   }

   public String getIconMaterial() {
      return this.iconMaterial != null && !this.iconMaterial.isBlank() ? this.iconMaterial : "SHIELD";
   }

   public void setIconMaterial(String iconMaterial) {
      this.iconMaterial = iconMaterial != null && !iconMaterial.isBlank() ? iconMaterial.trim().toUpperCase(Locale.ROOT) : "SHIELD";
   }

   public String getMotd() {
      return this.motd == null ? "" : this.motd;
   }

   public void setMotd(String motd) {
      String value = motd == null ? "" : motd.trim();
      this.motd = value.length() > 160 ? value.substring(0, 160) : value;
   }

   public List<String> getMailMessages() {
      return this.mailMessages;
   }

   public void addMailMessage(String message) {
      if (message != null && !message.isBlank()) {
         this.mailMessages.add(message.trim());

         while (this.mailMessages.size() > 25) {
            this.mailMessages.remove(0);
         }
      }
   }

   public void clearMailMessages() {
      this.mailMessages.clear();
   }

   public int getExtraMemberSlots() {
      return this.extraMemberSlots;
   }

   public void addExtraMemberSlots(int amount) {
      this.extraMemberSlots = Math.max(0, this.extraMemberSlots + amount);
   }

   public void setExtraMemberSlots(int extraMemberSlots) {
      this.extraMemberSlots = Math.max(0, extraMemberSlots);
   }

   public int getExtraHomeSlots() {
      return this.extraHomeSlots;
   }

   public void addExtraHomeSlots(int amount) {
      this.extraHomeSlots = Math.max(0, this.extraHomeSlots + amount);
   }

   public void setExtraHomeSlots(int extraHomeSlots) {
      this.extraHomeSlots = Math.max(0, extraHomeSlots);
   }

   public int getExtraVaultRows() {
      return this.extraVaultRows;
   }

   public void addExtraVaultRows(int amount) {
      this.extraVaultRows = Math.max(0, this.extraVaultRows + amount);
   }

   public void setExtraVaultRows(int extraVaultRows) {
      this.extraVaultRows = Math.max(0, extraVaultRows);
   }

   public int getExtraAllySlots() {
      return this.extraAllySlots;
   }

   public void addExtraAllySlots(int amount) {
      this.extraAllySlots = Math.max(0, this.extraAllySlots + amount);
   }

   public void setExtraAllySlots(int extraAllySlots) {
      this.extraAllySlots = Math.max(0, extraAllySlots);
   }

   public int getExtraRivalSlots() {
      return this.extraRivalSlots;
   }

   public void addExtraRivalSlots(int amount) {
      this.extraRivalSlots = Math.max(0, this.extraRivalSlots + amount);
   }

   public void setExtraRivalSlots(int extraRivalSlots) {
      this.extraRivalSlots = Math.max(0, extraRivalSlots);
   }

   public int getTeleportDelayReduction() {
      return this.teleportDelayReduction;
   }

   public void addTeleportDelayReduction(int seconds) {
      this.teleportDelayReduction = Math.max(0, this.teleportDelayReduction + seconds);
   }

   public void setTeleportDelayReduction(int teleportDelayReduction) {
      this.teleportDelayReduction = Math.max(0, teleportDelayReduction);
   }

   public double getTeleportCostReductionPercent() {
      return this.teleportCostReductionPercent;
   }

   public void addTeleportCostReductionPercent(double percent) {
      this.teleportCostReductionPercent = Math.max(0.0, Math.min(100.0, this.teleportCostReductionPercent + percent));
   }

   public void setTeleportCostReductionPercent(double percent) {
      this.teleportCostReductionPercent = Math.max(0.0, Math.min(100.0, percent));
   }

   public String serializeMail() {
      StringBuilder sb = new StringBuilder();

      for (String mail : this.mailMessages) {
         sb.append(Base64.getEncoder().encodeToString(mail.getBytes(StandardCharsets.UTF_8))).append("|");
      }

      return sb.toString();
   }

   public void loadMail(String data) {
      this.mailMessages.clear();
      if (data != null && !data.isBlank()) {
         try {
            for (String raw : data.split("\\|")) {
               if (!raw.isBlank()) {
                  this.mailMessages.add(new String(Base64.getDecoder().decode(raw), StandardCharsets.UTF_8));
               }
            }
         } catch (Exception var6) {
         }
      }
   }

   public Map<String, Location> getHomes() {
      return this.homes;
   }

   public Location getHome(String name) {
      return name == null ? null : this.homes.get(name.toLowerCase(Locale.ROOT));
   }

   public void setHome(String name, Location location) {
      if (name != null && location != null) {
         this.homes.put(name.toLowerCase(Locale.ROOT), location);
      }
   }

   public boolean removeHome(String name) {
      return name == null ? false : this.homes.remove(name.toLowerCase(Locale.ROOT)) != null;
   }

   public Set<String> getCompletedAchievements() {
      return this.completedAchievements;
   }

   public boolean hasAchievement(String id) {
      return this.completedAchievements.contains(id.toLowerCase(Locale.ROOT));
   }

   public void addAchievement(String id) {
      if (id != null && !id.isBlank()) {
         this.completedAchievements.add(id.toLowerCase(Locale.ROOT));
      }
   }

   public String serializeAchievements() {
      return String.join(",", this.completedAchievements);
   }

   public void loadAchievements(String data) {
      this.completedAchievements.clear();
      if (data != null && !data.isBlank()) {
         for (String part : data.split(",")) {
            if (!part.isBlank()) {
               this.completedAchievements.add(part.toLowerCase(Locale.ROOT));
            }
         }
      }
   }

   public String serializeHomes() {
      StringBuilder builder = new StringBuilder();

      for (Entry<String, Location> entry : this.homes.entrySet()) {
         Location loc = entry.getValue();
         if (loc != null && loc.getWorld() != null) {
            String encodedName = Base64.getEncoder().encodeToString(entry.getKey().getBytes(StandardCharsets.UTF_8));
            builder.append(encodedName)
               .append(";")
               .append(loc.getWorld().getName())
               .append(";")
               .append(loc.getX())
               .append(";")
               .append(loc.getY())
               .append(";")
               .append(loc.getZ())
               .append(";")
               .append(loc.getYaw())
               .append(";")
               .append(loc.getPitch())
               .append("|");
         }
      }

      return builder.toString();
   }

   public void loadHomes(String data) {
      this.homes.clear();
      if (data != null && !data.isBlank()) {
         for (String rawHome : data.split("\\|")) {
            if (!rawHome.isBlank()) {
               String[] parts = rawHome.split(";");
               if (parts.length >= 7) {
                  try {
                     String name = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
                     World world = Bukkit.getWorld(parts[1]);
                     if (world != null) {
                        Location loc = new Location(
                           world,
                           Double.parseDouble(parts[2]),
                           Double.parseDouble(parts[3]),
                           Double.parseDouble(parts[4]),
                           Float.parseFloat(parts[5]),
                           Float.parseFloat(parts[6])
                        );
                        this.homes.put(name.toLowerCase(Locale.ROOT), loc);
                     }
                  } catch (Exception var10) {
                  }
               }
            }
         }
      }
   }

   public void addAlly(String clanName) {
      this.allies.add(clanName);
   }

   public void removeAlly(String clanName) {
      this.allies.remove(clanName);
   }

   public boolean isAlliedWith(String otherClan) {
      return this.allies.contains(otherClan);
   }

   public void addRival(String clanName) {
      this.rivals.add(clanName);
   }

   public void removeRival(String clanName) {
      this.rivals.remove(clanName);
   }

   public boolean isRivalWith(String otherClan) {
      return this.rivals.contains(otherClan);
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = level;
   }

   public double getXp() {
      return this.xp;
   }

   public void setXp(double xp) {
      this.xp = xp;
   }

   public int getLastRewardLevel() {
      return this.lastRewardLevel;
   }

   public void setLastRewardLevel(int lastRewardLevel) {
      this.lastRewardLevel = Math.max(1, lastRewardLevel);
   }

   public double getRequiredXp(VRXClans plugin) {
      return this.level * plugin.getConfig().getDouble("level-up-multiplier", 1000.0);
   }

   public boolean addXp(double amount, VRXClans plugin) {
      if (Double.isFinite(amount) && !(amount <= 0.0)) {
         this.xp += amount;
         boolean leveled = false;

         for (double required = this.getRequiredXp(plugin); required > 0.0 && this.xp >= required; required = this.getRequiredXp(plugin)) {
            this.xp -= required;
            this.level++;
            leveled = true;
         }

         return leveled;
      } else {
         return false;
      }
   }

   public Inventory getVault(VRXClans plugin) {
      int unlockLevel = plugin.getConfig().getInt("vault-unlock-level", 3);
      int upgradeLevel = plugin.getConfig().getInt("vault-upgrade-level", 10);
      int size = 0;
      if (this.level >= upgradeLevel) {
         size = 54;
      } else if (this.level >= unlockLevel) {
         size = 27;
      }

      if (size > 0 && this.extraVaultRows > 0) {
         size = Math.min(54, size + this.extraVaultRows * 9);
      }

      if (size == 0) {
         return null;
      } else {
         if (this.vault == null) {
            this.vault = Bukkit.createInventory(null, size, plugin.getLang().get("gui.title.vault"));
            this.loadVaultFromCache(this.vault);
         } else if (this.vault.getSize() != size) {
            Inventory newVault = Bukkit.createInventory(null, size, plugin.getLang().get("gui.title.vault"));
            ItemStack[] oldContents = this.vault.getContents();

            for (int i = 0; i < Math.min(oldContents.length, newVault.getSize()); i++) {
               newVault.setItem(i, oldContents[i]);
            }

            this.vault = newVault;
         }

         return this.vault;
      }
   }

   private void loadVaultFromCache(Inventory targetVault) {
      if (this.cachedVaultBase64 != null && !this.cachedVaultBase64.isBlank()) {
         try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(this.cachedVaultBase64))) {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            try {
               int length = dataInput.readInt();

               for (int i = 0; i < length; i++) {
                  if (i < targetVault.getSize() && dataInput.readObject() instanceof ItemStack item) {
                     targetVault.setItem(i, item);
                  }
               }
            } catch (Throwable var10) {
               try {
                  dataInput.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            dataInput.close();
         } catch (ClassNotFoundException | IllegalArgumentException | IOException var12) {
            Bukkit.getLogger().warning("[VRXClans] Could not load clan vault for " + this.name + " could not be loaded: " + var12.getMessage());
         }
      }
   }

   public void markVaultDirty() {
      this.vaultDirty = true;
   }

   public boolean isVaultDirty() {
      return this.vaultDirty;
   }

   public boolean isVaultInventory(Inventory inventory) {
      return this.vault != null && this.vault.equals(inventory);
   }

   public void updateVaultCache() {
      if (this.vault != null) {
         try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            ItemStack[] items = this.vault.getContents();
            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
               dataOutput.writeObject(item);
            }

            dataOutput.close();
            this.cachedVaultBase64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            this.vaultDirty = false;
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      }
   }

   public String getCachedVaultBase64() {
      return this.cachedVaultBase64;
   }

   public void setCachedVaultBase64(String base64) {
      this.cachedVaultBase64 = base64;
   }

   public Location getBaseLocation() {
      return this.baseLocation;
   }

   public void setBaseLocation(Location location) {
      this.baseLocation = location;
   }

   public int getPurchasedRadiusBonus() {
      return this.purchasedRadiusBonus;
   }

   public void addPurchasedRadiusBonus(int amount) {
      this.purchasedRadiusBonus += amount;
   }

   public void setPurchasedRadiusBonus(int amount) {
      this.purchasedRadiusBonus = amount;
   }

   public int getDynamicRadius(VRXClans plugin) {
      if (this.baseLocation == null) {
         return 0;
      } else {
         int baseRadius = plugin.getConfig().getInt("base-base-radius", 10);
         int bonus = plugin.getConfig().getInt("base-bonus-per-member", 5);
         return baseRadius + this.members.size() * bonus + this.purchasedRadiusBonus;
      }
   }

   public boolean hasXpBooster() {
      return System.currentTimeMillis() < this.xpBooster;
   }

   public boolean hasHasteBooster() {
      return System.currentTimeMillis() < this.hasteBooster;
   }

   public boolean hasSpeedBooster() {
      return System.currentTimeMillis() < this.speedBooster;
   }

   public boolean hasStrengthBooster() {
      return System.currentTimeMillis() < this.strengthBooster;
   }

   public void addXpBooster(long millis) {
      this.xpBooster = Math.max(System.currentTimeMillis(), this.xpBooster) + millis;
   }

   public void addHasteBooster(long millis) {
      this.hasteBooster = Math.max(System.currentTimeMillis(), this.hasteBooster) + millis;
   }

   public void addSpeedBooster(long millis) {
      this.speedBooster = Math.max(System.currentTimeMillis(), this.speedBooster) + millis;
   }

   public void addStrengthBooster(long millis) {
      this.strengthBooster = Math.max(System.currentTimeMillis(), this.strengthBooster) + millis;
   }

   public String getBoosterTimeLeft(long expiry, VRXClans plugin) {
      if (System.currentTimeMillis() >= expiry) {
         return plugin.getLang().get("misc.booster-inactive");
      } else {
         long seconds = (expiry - System.currentTimeMillis()) / 1000L;
         long h = seconds / 3600L;
         long m = seconds % 3600L / 60L;
         return plugin.getLang().get("misc.booster-active", "%h%", String.valueOf(h), "%m%", String.valueOf(m));
      }
   }

   public long getXpBooster() {
      return this.xpBooster;
   }

   public long getHasteBooster() {
      return this.hasteBooster;
   }

   public long getSpeedBooster() {
      return this.speedBooster;
   }

   public long getStrengthBooster() {
      return this.strengthBooster;
   }

   public void setXpBooster(long val) {
      this.xpBooster = val;
   }

   public void setHasteBooster(long val) {
      this.hasteBooster = val;
   }

   public void setSpeedBooster(long val) {
      this.speedBooster = val;
   }

   public void setStrengthBooster(long val) {
      this.strengthBooster = val;
   }

   public long getLastUpkeepTime() {
      return this.lastUpkeepTime;
   }

   public void setLastUpkeepTime(long lastUpkeepTime) {
      this.lastUpkeepTime = lastUpkeepTime;
   }

   public int getQuestZombies() {
      return this.questZombies;
   }

   public void setQuestZombies(int questZombies) {
      this.questZombies = questZombies;
   }

   public int getQuestDiamonds() {
      return this.questDiamonds;
   }

   public void setQuestDiamonds(int questDiamonds) {
      this.questDiamonds = questDiamonds;
   }

   public double getQuestMoney() {
      return this.questMoney;
   }

   public void setQuestMoney(double questMoney) {
      this.questMoney = questMoney;
   }

   public boolean isQuestZombiesDone() {
      return this.questZombiesDone;
   }

   public void setQuestZombiesDone(boolean questZombiesDone) {
      this.questZombiesDone = questZombiesDone;
   }

   public boolean isQuestDiamondsDone() {
      return this.questDiamondsDone;
   }

   public void setQuestDiamondsDone(boolean questDiamondsDone) {
      this.questDiamondsDone = questDiamondsDone;
   }

   public boolean isQuestBankDone() {
      return this.questBankDone;
   }

   public void setQuestBankDone(boolean questBankDone) {
      this.questBankDone = questBankDone;
   }

   public long getLastQuestReset() {
      return this.lastQuestReset;
   }

   public void setLastQuestReset(long lastQuestReset) {
      this.lastQuestReset = lastQuestReset;
   }

   public int getKills() {
      return this.kills;
   }

   public void setKills(int kills) {
      this.kills = kills;
   }

   public int getDeaths() {
      return this.deaths;
   }

   public void setDeaths(int deaths) {
      this.deaths = deaths;
   }

   public String getKDR() {
      if (this.deaths == 0) {
         return String.valueOf(this.kills);
      } else {
         double kdr = (double)this.kills / this.deaths;
         return String.format("%.2f", kdr);
      }
   }

   public String getActiveWar() {
      return this.activeWar;
   }

   public void setActiveWar(String activeWar) {
      this.activeWar = activeWar;
   }

   public int getWarKills() {
      return this.warKills;
   }

   public void setWarKills(int warKills) {
      this.warKills = warKills;
   }

   public double getWarWager() {
      return this.warWager;
   }

   public void setWarWager(double warWager) {
      this.warWager = warWager;
   }

   public void resetWar() {
      this.activeWar = null;
      this.warKills = 0;
      this.warWager = 0.0;
   }

   public boolean isBaseRegen() {
      return this.baseRegen;
   }

   public void setBaseRegen(boolean baseRegen) {
      this.baseRegen = baseRegen;
   }

   public boolean isBaseSaturation() {
      return this.baseSaturation;
   }

   public void setBaseSaturation(boolean baseSaturation) {
      this.baseSaturation = baseSaturation;
   }

   public static enum ClanRank {
      RECRUIT,
      MEMBER,
      OFFICER,
      CO_LEADER,
      LEADER;
   }
}
