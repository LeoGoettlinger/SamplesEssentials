package com.earth2me.essentials;

import com.earth2me.essentials.config.ConfigurateUtil;
import com.earth2me.essentials.config.EssentialsUserConfiguration;
import com.earth2me.essentials.config.entities.CommandCooldown;
import com.earth2me.essentials.config.entities.LazyLocation;
import com.earth2me.essentials.config.holders.UserConfigHolder;
import com.earth2me.essentials.userstorage.ModernUserMap;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.google.common.base.Charsets;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.services.mail.MailMessage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class UserData extends PlayerExtension implements IConf {
   protected final transient Essentials ess;
   private final EssentialsUserConfiguration config;
   private UserConfigHolder holder;
   private BigDecimal money;

   protected UserData(final Player base, final net.ess3.api.IEssentials ess) {
      super(base);
      this.ess = (Essentials)ess;
      File folder = new File(ess.getDataFolder(), "userdata");
      if (!folder.exists() && !folder.mkdirs()) {
         throw new RuntimeException("Unable to create userdata folder!");
      } else {
         this.config = new EssentialsUserConfiguration(base.getName(), base.getUniqueId(), new File(folder, base.getUniqueId() + ".yml"));
         this.config.setSaveHook(() -> this.config.setRootHolder(UserConfigHolder.class, this.holder));
         this.reloadConfig();
         if (this.config.getUsername() == null) {
            this.config.setUsername(this.getLastAccountName());
         }
      }
   }

   public final void reset() {
      this.config.blockingSave();
      if (!this.config.getFile().delete()) {
         this.ess.getLogger().warning("Unable to delete data file for " + this.config.getFile().getName());
      }

      if (this.config.getUsername() != null) {
         ModernUserMap users = this.ess.getUsers();
         users.invalidate(this.config.getUuid());
         if (this.isNPC()) {
            String name = this.ess.getSettings().isSafeUsermap() ? StringUtil.safeString(this.config.getUsername()) : this.config.getUsername();
            users.invalidate(UUID.nameUUIDFromBytes(("NPC:" + name).getBytes(Charsets.UTF_8)));
         }
      }
   }

   public final void cleanup() {
      this.config.blockingSave();
      this.ess.getUsers().removeCache(this.getConfigUUID());
   }

   @Override
   public final void reloadConfig() {
      this.config.load();

      try {
         this.holder = this.config.getRootNode().get(UserConfigHolder.class);
      } catch (Throwable var2) {
         this.ess.getLogger().log(Level.SEVERE, "Error while reading user config: " + this.config.getFile().getName(), var2);
         throw new RuntimeException(var2);
      }

      this.money = this._getMoney();
   }

   private BigDecimal _getMoney() {
      BigDecimal result = this.ess.getSettings().getStartingBalance();
      BigDecimal maxMoney = this.ess.getSettings().getMaxMoney();
      BigDecimal minMoney = this.ess.getSettings().getMinMoney();
      if (this.isNPC()) {
         result = BigDecimal.ZERO;
      }

      if (this.holder.money() != null) {
         result = this.holder.money();
      }

      if (result.compareTo(maxMoney) > 0) {
         result = maxMoney;
      }

      if (result.compareTo(minMoney) < 0) {
         result = minMoney;
      }

      this.holder.money(result);
      return this.holder.money();
   }

   public BigDecimal getMoney() {
      return this.money;
   }

   public void setMoney(final BigDecimal value, final boolean throwError) throws MaxMoneyException {
      BigDecimal maxMoney = this.ess.getSettings().getMaxMoney();
      BigDecimal minMoney = this.ess.getSettings().getMinMoney();
      if (value.compareTo(maxMoney) > 0) {
         if (throwError) {
            throw new MaxMoneyException();
         }

         this.money = maxMoney;
      } else {
         this.money = value;
      }

      if (this.money.compareTo(minMoney) < 0) {
         this.money = minMoney;
      }

      this.holder.money(this.money);
      this.stopTransaction();
   }

   private String getHomeName(String search) {
      if (NumberUtil.isInt(search)) {
         try {
            search = this.getHomes().get(Integer.parseInt(search) - 1);
         } catch (IndexOutOfBoundsException | NumberFormatException var3) {
         }
      }

      return search;
   }

   public Location getHome(final String name) {
      String search = this.getHomeName(name);
      LazyLocation loc = this.holder.homes().get(search);
      return loc != null ? loc.location() : null;
   }

   public boolean hasValidHomes() {
      for (LazyLocation loc : this.holder.homes().values()) {
         if (loc != null && loc.location() != null) {
            return true;
         }
      }

      return false;
   }

   public Location getHome(final Location world) {
      if (this.getHomes().isEmpty()) {
         return null;
      } else {
         for (String home : this.getHomes()) {
            Location loc = this.holder.homes().get(home).location();
            if (loc != null && world.getWorld() == loc.getWorld()) {
               return loc;
            }
         }

         return this.holder.homes().get(this.getHomes().get(0)).location();
      }
   }

   public List<String> getHomes() {
      return new ArrayList<>(this.holder.homes().keySet());
   }

   public void setHome(String name, final Location loc) {
      name = StringUtil.safeString(name);
      this.holder.homes().put(name, LazyLocation.fromLocation(loc));
      this.config.save();
   }

   public void delHome(final String name) throws Exception {
      String search = this.getHomeName(name);
      if (!this.holder.homes().containsKey(search)) {
         search = StringUtil.safeString(search);
      }

      if (this.holder.homes().containsKey(search)) {
         this.holder.homes().remove(search);
         this.config.save();
      } else {
         throw new TranslatableException("invalidHome", search);
      }
   }

   public void renameHome(final String name, final String newName) throws Exception {
      LazyLocation location = this.holder.homes().remove(name);
      if (location != null) {
         this.holder.homes().put(StringUtil.safeString(newName), location);
         this.config.save();
      } else {
         throw new TranslatableException("invalidHome", name);
      }
   }

   public boolean hasHome() {
      return !this.holder.homes().isEmpty();
   }

   public boolean hasHome(final String name) {
      return this.holder.homes().containsKey(name);
   }

   public String getNickname() {
      return this.holder.nickname();
   }

   public void setNickname(final String nick) {
      this.holder.nickname(nick);
      this.config.save();
   }

   public Set<Material> getUnlimited() {
      return this.holder.unlimited();
   }

   public boolean hasUnlimited(final ItemStack stack) {
      return this.holder.unlimited().contains(stack.getType());
   }

   public void setUnlimited(final ItemStack stack, final boolean state) {
      boolean wasUpdated;
      if (state) {
         wasUpdated = this.holder.unlimited().add(stack.getType());
      } else {
         wasUpdated = this.holder.unlimited().remove(stack.getType());
      }

      if (wasUpdated) {
         this.config.save();
      }
   }

   public void clearAllPowertools() {
      this.holder.powertools().clear();
      this.config.save();
   }

   public List<String> getPowertool(final ItemStack stack) {
      return this.getPowertool(stack.getType());
   }

   public List<String> getPowertool(final Material material) {
      return this.holder.powertools().get(material.name().toLowerCase(Locale.ENGLISH));
   }

   public void setPowertool(final ItemStack stack, final List<String> commandList) {
      if (commandList != null && !commandList.isEmpty()) {
         this.holder.powertools().put(stack.getType().name().toLowerCase(Locale.ENGLISH), commandList);
      } else {
         this.holder.powertools().remove(stack.getType().name().toLowerCase(Locale.ENGLISH));
      }

      this.config.save();
   }

   public boolean hasPowerTools() {
      return !this.holder.powertools().isEmpty();
   }

   public Map<String, List<String>> getAllPowertools() {
      return this.holder.powertools();
   }

   public Location getLastLocation() {
      LazyLocation lastLocation = this.holder.lastLocation();
      return lastLocation != null ? lastLocation.location() : null;
   }

   public void setLastLocation(final Location loc) {
      if (loc != null && loc.getWorld() != null) {
         this.holder.lastLocation(loc);
         this.config.save();
      }
   }

   public Location getLogoutLocation() {
      LazyLocation logoutLocation = this.holder.logoutLocation();
      return logoutLocation != null ? logoutLocation.location() : null;
   }

   public void setLogoutLocation(final Location loc) {
      if (loc != null && loc.getWorld() != null) {
         this.holder.logoutLocation(loc);
         this.config.save();
      }
   }

   public long getLastTeleportTimestamp() {
      return this.holder.timestamps().lastTeleport();
   }

   public void setLastTeleportTimestamp(final long time) {
      this.holder.timestamps().lastTeleport(time);
      this.config.save();
   }

   public long getLastHealTimestamp() {
      return this.holder.timestamps().lastHeal();
   }

   public void setLastHealTimestamp(final long time) {
      this.holder.timestamps().lastHeal(time);
      this.config.save();
   }

   public String getJail() {
      return this.holder.jail();
   }

   public void setJail(final String jail) {
      this.holder.jail(jail);
      this.config.save();
   }

   @Deprecated
   public List<String> getMails() {
      List<String> list = new ArrayList<>();
      if (this.getMailAmount() != 0) {
         for (MailMessage mail : this.getMailMessages()) {
            list.add(
               mail.isLegacy()
                  ? mail.getMessage()
                  : ChatColor.GOLD + "[" + ChatColor.RESET + mail.getSenderUsername() + ChatColor.GOLD + "] " + ChatColor.RESET + mail.getMessage()
            );
         }
      }

      return list;
   }

   @Deprecated
   public void setMails(List<String> mails) {
      throw new UnsupportedOperationException(
         "UserData#setMails(List<String>) is deprecated and can no longer be used. Please tell the plugin author to update this!"
      );
   }

   public int getMailAmount() {
      if (this.holder.mail() == null) {
         return 0;
      } else {
         int amount = 0;

         for (MailMessage element : this.holder.mail()) {
            if (!element.isExpired()) {
               amount++;
            }
         }

         return amount;
      }
   }

   public int getUnreadMailAmount() {
      if (this.holder.mail() != null && !this.holder.mail().isEmpty()) {
         int unread = 0;

         for (MailMessage element : this.holder.mail()) {
            if (!element.isRead() && !element.isExpired()) {
               unread++;
            }
         }

         return unread;
      } else {
         return 0;
      }
   }

   @Deprecated
   abstract void addMail(final String mail);

   public ArrayList<MailMessage> getMailMessages() {
      return new ArrayList<>(this.holder.mail());
   }

   public void setMailList(ArrayList<MailMessage> messages) {
      this.holder.mail(messages);
      this.config.save();
   }

   public boolean isTeleportEnabled() {
      return this.holder.teleportEnabled();
   }

   public void setTeleportEnabled(final boolean set) {
      this.holder.teleportEnabled(set);
      this.config.save();
   }

   public boolean isAutoTeleportEnabled() {
      return this.holder.teleportAuto();
   }

   public void setAutoTeleportEnabled(final boolean set) {
      this.holder.teleportAuto(set);
      this.config.save();
   }

   @Deprecated
   public void setIgnoredPlayers(final List<String> players) {
      List<UUID> uuids = new ArrayList<>();

      for (String player : players) {
         User user = this.ess.getOfflineUser(player);
         if (user == null) {
            return;
         }

         uuids.add(user.getBase().getUniqueId());
      }

      this.setIgnoredPlayerUUIDs(uuids);
   }

   public void setIgnoredPlayerUUIDs(final List<UUID> players) {
      this.holder.ignore(players);
      this.config.save();
   }

   @Deprecated
   public boolean isIgnoredPlayer(final String userName) {
      IUser user = this.ess.getUser(userName);
      return user != null && user.getBase().isOnline() ? this.isIgnoredPlayer(user) : false;
   }

   public boolean isIgnoredPlayer(final IUser user) {
      return this.holder.ignore().contains(user.getBase().getUniqueId()) && !user.isIgnoreExempt();
   }

   public List<UUID> _getIgnoredPlayers() {
      return this.holder.ignore();
   }

   public void setIgnoredPlayer(final IUser user, final boolean set) {
      UUID uuid = user.getBase().getUniqueId();
      if (set) {
         if (!this.holder.ignore().contains(uuid)) {
            this.holder.ignore().add(uuid);
         }
      } else {
         this.holder.ignore().remove(uuid);
      }

      this.config.save();
   }

   public boolean isGodModeEnabled() {
      return this.holder.godMode();
   }

   public void setGodModeEnabled(final boolean set) {
      this.holder.godMode(set);
      this.config.save();
   }

   public boolean isFlyModeEnabled() {
      return this.holder.flyMode();
   }

   public void setFlyModeEnabled(final boolean set) {
      this.holder.flyMode(set);
      this.config.save();
   }

   public boolean getMuted() {
      return this.holder.muted();
   }

   public boolean isMuted() {
      return this.getMuted();
   }

   public void setMuted(final boolean set) {
      this.holder.muted(set);
      this.config.save();
   }

   public String getMuteReason() {
      return this.holder.muteReason();
   }

   public void setMuteReason(final String reason) {
      this.holder.muteReason(reason);
      this.config.save();
   }

   public boolean hasMuteReason() {
      return this.holder.muteReason() != null;
   }

   public long getMuteTimeout() {
      return this.holder.timestamps().mute();
   }

   public void setMuteTimeout(final long time) {
      this.holder.timestamps().mute(time);
      this.config.save();
   }

   public boolean isJailed() {
      return this.holder.jailed();
   }

   public void setJailed(final boolean set) {
      this.holder.jailed(set);
      this.config.save();
   }

   public boolean toggleJailed() {
      boolean ret = !this.isJailed();
      this.setJailed(ret);
      return ret;
   }

   public long getJailTimeout() {
      return this.holder.timestamps().jail();
   }

   public void setJailTimeout(final long time) {
      this.holder.timestamps().jail(time);
      this.config.save();
   }

   public long getOnlineJailedTime() {
      return this.holder.timestamps().onlineJail();
   }

   public void setOnlineJailedTime(long onlineJailed) {
      this.holder.timestamps().onlineJail(onlineJailed);
      this.config.save();
   }

   public long getLastLogin() {
      return this.holder.timestamps().login();
   }

   public void setLastLogin(final long time) {
      this.holder.timestamps().login(time);
      if (this.base.getAddress() != null && this.base.getAddress().getAddress() != null) {
         this.holder.ipAddress(this.base.getAddress().getAddress().getHostAddress());
      }

      this.config.save();
   }

   public long getLastLogout() {
      return this.holder.timestamps().logout();
   }

   public void setLastLogout(final long time) {
      this.holder.timestamps().logout(time);
      this.config.save();
   }

   public String getLastLoginAddress() {
      return this.holder.ipAddress();
   }

   public boolean isAfk() {
      return this.holder.afk();
   }

   public void _setAfk(final boolean set) {
      this.holder.afk(set);
      this.config.save();
   }

   public String getGeoLocation() {
      return this.holder.geolocation();
   }

   public void setGeoLocation(final String geolocation) {
      this.holder.geolocation(geolocation);
      this.config.save();
   }

   public boolean isSocialSpyEnabled() {
      return this.holder.socialSpy();
   }

   public void setSocialSpyEnabled(final boolean status) {
      this.holder.socialSpy(status);
      this.config.save();
   }

   public boolean isNPC() {
      return this.holder.npc();
   }

   public void setNPC(final boolean set) {
      this.holder.npc(set);
      this.config.save();
   }

   public String getLastAccountName() {
      return this.holder.lastAccountName();
   }

   public void setLastAccountName(final String lastAccountName) {
      if (this.getLastAccountName() != null && !this.getLastAccountName().equals(lastAccountName)) {
         List<String> usernames = this.holder.pastUsernames();
         usernames.add(0, this.getLastAccountName());
         this.holder.pastUsernames(usernames);
      }

      this.holder.lastAccountName(lastAccountName);
      this.config.save();
   }

   public boolean arePowerToolsEnabled() {
      return this.holder.powerToolsEnabled();
   }

   public void setPowerToolsEnabled(final boolean set) {
      this.holder.powerToolsEnabled(set);
      this.config.save();
   }

   public boolean togglePowerToolsEnabled() {
      boolean ret = !this.arePowerToolsEnabled();
      this.setPowerToolsEnabled(ret);
      return ret;
   }

   public long getKitTimestamp(String name) {
      name = name.replace('.', '_').replace('/', '_').toLowerCase(Locale.ENGLISH);
      return this.holder.timestamps().kits() != null && this.holder.timestamps().kits().containsKey(name) ? this.holder.timestamps().kits().get(name) : 0L;
   }

   public void setKitTimestamp(String name, final long time) {
      name = name.replace('.', '_').replace('/', '_').toLowerCase(Locale.ENGLISH);
      this.holder.timestamps().kits().put(name, time);
      this.config.save();
   }

   public List<CommandCooldown> getCooldownsList() {
      return this.holder.timestamps().commandCooldowns();
   }

   public Map<Pattern, Long> getCommandCooldowns() {
      Map<Pattern, Long> map = new HashMap<>();

      for (CommandCooldown c : this.getCooldownsList()) {
         if (c != null && !c.isIncomplete()) {
            map.put(c.pattern(), c.value());
         }
      }

      return map;
   }

   public Date getCommandCooldownExpiry(final String label) {
      for (CommandCooldown cooldown : this.getCooldownsList()) {
         if (cooldown != null && !cooldown.isIncomplete() && cooldown.pattern().matcher(label).matches()) {
            return new Date(cooldown.value());
         }
      }

      return null;
   }

   public void addCommandCooldown(final Pattern pattern, final Date expiresAt, final boolean save) {
      CommandCooldown cooldown = new CommandCooldown();
      cooldown.pattern(pattern);
      cooldown.value(expiresAt.getTime());
      if (!cooldown.isIncomplete()) {
         this.holder.timestamps().commandCooldowns().add(cooldown);
         if (save) {
            this.save();
         }
      }
   }

   public boolean clearCommandCooldown(final Pattern pattern) {
      if (this.holder.timestamps().commandCooldowns().isEmpty()) {
         return false;
      } else if (this.getCooldownsList().removeIf(cooldown -> cooldown != null && !cooldown.isIncomplete() && cooldown.pattern().equals(pattern))) {
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public boolean isAcceptingPay() {
      return this.holder.acceptingPay();
   }

   public void setAcceptingPay(final boolean acceptingPay) {
      this.holder.acceptingPay(acceptingPay);
      this.save();
   }

   public boolean isPromptingPayConfirm() {
      return this.holder.confirmPay() != null ? this.holder.confirmPay() : this.ess.getSettings().isConfirmCommandEnabledByDefault("pay");
   }

   public void setPromptingPayConfirm(final boolean prompt) {
      this.holder.confirmPay(prompt);
      this.save();
   }

   public boolean isPromptingClearConfirm() {
      return this.holder.confirmClear() != null ? this.holder.confirmClear() : this.ess.getSettings().isConfirmCommandEnabledByDefault("clearinventory");
   }

   public void setPromptingClearConfirm(final boolean prompt) {
      this.holder.confirmClear(prompt);
      this.save();
   }

   public boolean isLastMessageReplyRecipient() {
      return this.holder.lastMessageReplyRecipient() != null ? this.holder.lastMessageReplyRecipient() : this.ess.getSettings().isLastMessageReplyRecipient();
   }

   public void setLastMessageReplyRecipient(final boolean enabled) {
      this.holder.lastMessageReplyRecipient(enabled);
      this.save();
   }

   public boolean isBaltopExcludeCache() {
      return this.holder.baltopExempt();
   }

   public void setBaltopExemptCache(boolean baltopExempt) {
      this.holder.baltopExempt(baltopExempt);
      this.config.save();
   }

   public List<String> getPastUsernames() {
      return this.holder.pastUsernames();
   }

   public void addPastUsername(String username) {
      List<String> usernames = this.holder.pastUsernames();
      usernames.add(0, username);
      this.holder.pastUsernames(usernames);
      this.config.save();
   }

   public boolean isShouting() {
      if (this.holder.shouting() == null) {
         this.holder.shouting(this.ess.getSettings().isShoutDefault());
      }

      return this.holder.shouting();
   }

   public void setShouting(boolean shouting) {
      this.holder.shouting(shouting);
      this.config.save();
   }

   public UUID getConfigUUID() {
      return this.config.getUuid();
   }

   public void save() {
      this.config.save();
   }

   public void startTransaction() {
      this.config.startTransaction();
   }

   public void stopTransaction() {
      this.config.stopTransaction();
   }

   public void setConfigProperty(String node, Object object) {
      this.setConfigPropertyRaw("info." + node, object);
   }

   public void setConfigPropertyRaw(String node, Object object) {
      this.config.setRaw(node, object);
      this.config.save();
   }

   public Set<String> getConfigKeys() {
      return ConfigurateUtil.getKeys(this.config.getSection("info"));
   }

   public Map<String, Object> getConfigMap() {
      return ConfigurateUtil.getRawMap(this.config.getSection("info"));
   }

   public Map<String, Object> getConfigMap(final String node) {
      return ConfigurateUtil.getRawMap(this.config.getSection("info." + node));
   }
}
