package com.earth2me.essentials;

import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.adventure.ComponentHolder;
import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.economy.EconomyLayer;
import com.earth2me.essentials.economy.EconomyLayers;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.messaging.IMessageRecipient;
import com.earth2me.essentials.messaging.SimpleMessageRecipient;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.TriState;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.AfkStatusChangeEvent;
import net.ess3.api.events.JailStatusChangeEvent;
import net.ess3.api.events.MuteStatusChangeEvent;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.ess3.provider.PlayerLocaleProvider;
import net.essentialsx.api.v2.events.PreTransactionEvent;
import net.essentialsx.api.v2.events.TransactionEvent;
import net.essentialsx.api.v2.services.mail.MailSender;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class User extends UserData implements Comparable<User>, IMessageRecipient, net.ess3.api.IUser {
   private static final Statistic PLAY_ONE_TICK = EnumUtil.getStatistic("PLAY_ONE_MINUTE", "PLAY_ONE_TICK");
   private final IMessageRecipient messageRecipient;
   private final transient AsyncTeleport teleport;
   private final Map<User, BigDecimal> confirmingPayments = new WeakHashMap<>();
   private String confirmingClearCommand;
   private String lastHomeConfirmation;
   private final transient LinkedHashMap<String, IUser.TpaRequest> teleportRequestQueue = new LinkedHashMap<>();
   private transient boolean vanished;
   private boolean hidden = false;
   private boolean leavingHidden = false;
   private boolean rightClickJump = false;
   private boolean invSee = false;
   private boolean recipeSee = false;
   private boolean enderSee = false;
   private boolean ignoreMsg = false;
   private Boolean toggleShout;
   private boolean freeze = false;
   private String afkMessage;
   private long afkSince;
   private transient Location afkPosition = null;
   private transient long lastOnlineActivity;
   private transient long lastThrottledAction;
   private transient long lastActivity = System.currentTimeMillis();
   private transient long teleportInvulnerabilityTimestamp = 0L;
   private long lastNotifiedAboutMailsMs;
   private long lastHomeConfirmationTimestamp;
   private final transient List<String> signCopy = Lists.newArrayList(new String[]{"", "", "", ""});
   private transient long lastVanishTime = System.currentTimeMillis();
   private transient int flightTick = -1;
   private String lastLocaleString;
   private Locale playerLocale;

   public User(final Player base, final net.ess3.api.IEssentials ess) {
      super(base, ess);
      this.teleport = new AsyncTeleport(this, ess);
      if (this.isAfk()) {
         this.afkPosition = this.getLocation();
      }

      if (this.getBase().isOnline()) {
         this.lastOnlineActivity = System.currentTimeMillis();
      }

      this.messageRecipient = new SimpleMessageRecipient(ess, this);
   }

   public void update(final Player base) {
      this.setBase(base);
   }

   public net.ess3.api.IEssentials getEssentials() {
      return this.ess;
   }

   @Override
   public boolean isAuthorized(final IEssentialsCommand cmd) {
      return this.isAuthorized(cmd, "essentials.");
   }

   @Override
   public boolean isAuthorized(final IEssentialsCommand cmd, final String permissionPrefix) {
      return this.isAuthorized(permissionPrefix + (cmd.getName().equals("r") ? "msg" : cmd.getName()));
   }

   @Override
   public boolean isAuthorized(final String node) {
      if (Essentials.TESTING) {
         return false;
      } else {
         boolean result = this.isAuthorizedCheck(node);
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "checking if " + this.base.getName() + " has " + node + " - " + result);
         }

         return result;
      }
   }

   @Override
   public boolean isAuthorizedCached(final String node) {
      if (Essentials.TESTING) {
         return false;
      } else {
         boolean result = this.isAuthorizedCachedCheck(node);
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "checking if " + this.base.getName() + " has " + node + " (cached) - " + result);
         }

         return result;
      }
   }

   @Override
   public boolean isPermissionSet(final String node) {
      if (Essentials.TESTING) {
         return false;
      } else {
         boolean result = this.isPermSetCheck(node);
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "checking if " + this.base.getName() + " has " + node + " (set-explicit) - " + result);
         }

         return result;
      }
   }

   public TriState isAuthorizedExact(final String node) {
      return this.isAuthorizedExactCheck(node);
   }

   private boolean isAuthorizedCheck(final String node) {
      if (this.base instanceof OfflinePlayerStub) {
         return false;
      } else {
         try {
            return this.ess.getPermissionsHandler().hasPermission(this.base, node);
         } catch (Exception var3) {
            if (this.ess.getSettings().isDebug()) {
               this.ess
                  .getLogger()
                  .log(
                     Level.SEVERE,
                     "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + var3.getMessage(),
                     (Throwable)var3
                  );
            } else {
               this.ess
                  .getLogger()
                  .log(Level.SEVERE, "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + var3.getMessage());
            }

            return false;
         }
      }
   }

   private boolean isAuthorizedCachedCheck(final String node) {
      if (this.base instanceof OfflinePlayerStub) {
         return false;
      } else {
         try {
            return this.ess.getPermissionsHandler().hasPermissionCached(this.base, node);
         } catch (Exception var3) {
            if (this.ess.getSettings().isDebug()) {
               this.ess
                  .getLogger()
                  .log(
                     Level.SEVERE,
                     "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + var3.getMessage(),
                     (Throwable)var3
                  );
            } else {
               this.ess
                  .getLogger()
                  .log(Level.SEVERE, "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + var3.getMessage());
            }

            return false;
         }
      }
   }

   private boolean isPermSetCheck(final String node) {
      if (this.base instanceof OfflinePlayerStub) {
         return false;
      } else {
         try {
            return this.ess.getPermissionsHandler().isPermissionSet(this.base, node);
         } catch (Exception var3) {
            if (this.ess.getSettings().isDebug()) {
               this.ess
                  .getLogger()
                  .log(
                     Level.SEVERE,
                     "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + var3.getMessage(),
                     (Throwable)var3
                  );
            } else {
               this.ess
                  .getLogger()
                  .log(Level.SEVERE, "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + var3.getMessage());
            }

            return false;
         }
      }
   }

   private TriState isAuthorizedExactCheck(final String node) {
      if (this.base instanceof OfflinePlayerStub) {
         return TriState.UNSET;
      } else {
         try {
            return this.ess.getPermissionsHandler().isPermissionSetExact(this.base, node);
         } catch (Exception var3) {
            if (this.ess.getSettings().isDebug()) {
               this.ess
                  .getLogger()
                  .log(
                     Level.SEVERE,
                     "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + var3.getMessage(),
                     (Throwable)var3
                  );
            } else {
               this.ess
                  .getLogger()
                  .log(Level.SEVERE, "Permission System Error: " + this.ess.getPermissionsHandler().getName() + " returned: " + var3.getMessage());
            }

            return TriState.UNSET;
         }
      }
   }

   @Override
   public void healCooldown() throws Exception {
      Calendar now = new GregorianCalendar();
      if (this.getLastHealTimestamp() > 0L) {
         double cooldown = this.ess.getSettings().getHealCooldown();
         Calendar cooldownTime = new GregorianCalendar();
         cooldownTime.setTimeInMillis(this.getLastHealTimestamp());
         cooldownTime.add(13, (int)cooldown);
         cooldownTime.add(14, (int)(cooldown * 1000.0 % 1000.0));
         if (cooldownTime.after(now) && !this.isAuthorized("essentials.heal.cooldown.bypass")) {
            throw new TranslatableException("timeBeforeHeal", DateUtil.formatDateDiff(cooldownTime.getTimeInMillis()));
         }
      }

      this.setLastHealTimestamp(now.getTimeInMillis());
   }

   @Override
   public void giveMoney(final BigDecimal value) throws MaxMoneyException {
      this.giveMoney(value, null);
   }

   @Override
   public void giveMoney(final BigDecimal value, final CommandSource initiator) throws MaxMoneyException {
      this.giveMoney(value, initiator, UserBalanceUpdateEvent.Cause.UNKNOWN);
   }

   public void giveMoney(final BigDecimal value, final CommandSource initiator, final UserBalanceUpdateEvent.Cause cause) throws MaxMoneyException {
      if (value.signum() != 0) {
         this.setMoney(this.getMoney().add(value), cause);
         this.sendTl("addedToAccount", AdventureUtil.parsed(NumberUtil.displayCurrency(value, this.ess)));
         if (initiator != null) {
            initiator.sendTl(
               "addedToOthersAccount",
               AdventureUtil.parsed(NumberUtil.displayCurrency(value, this.ess)),
               this.getDisplayName(),
               AdventureUtil.parsed(NumberUtil.displayCurrency(this.getMoney(), this.ess))
            );
         }
      }
   }

   @Override
   public void payUser(final User reciever, final BigDecimal value) throws Exception {
      this.payUser(reciever, value, UserBalanceUpdateEvent.Cause.UNKNOWN);
   }

   public void payUser(final User reciever, BigDecimal value, final UserBalanceUpdateEvent.Cause cause) throws Exception {
      if (value.compareTo(BigDecimal.ZERO) < 1) {
         throw new Exception(I18n.tlLocale(this.playerLocale, "payMustBePositive"));
      } else if (this.canAfford(value)) {
         PreTransactionEvent preTransactionEvent = new PreTransactionEvent(this.getSource(), reciever, value);
         this.ess.getServer().getPluginManager().callEvent(preTransactionEvent);
         if (!preTransactionEvent.isCancelled()) {
            value = preTransactionEvent.getAmount();
            this.setMoney(this.getMoney().subtract(value), cause);
            reciever.setMoney(reciever.getMoney().add(value), cause);
            this.sendTl("moneySentTo", AdventureUtil.parsed(NumberUtil.displayCurrency(value, this.ess)), reciever.getDisplayName());
            reciever.sendTl("moneyRecievedFrom", AdventureUtil.parsed(NumberUtil.displayCurrency(value, this.ess)), this.getDisplayName());
            TransactionEvent transactionEvent = new TransactionEvent(this.getSource(), reciever, value);
            this.ess.getServer().getPluginManager().callEvent(transactionEvent);
         }
      } else {
         throw new ChargeException("notEnoughMoney", AdventureUtil.parsed(NumberUtil.displayCurrency(value, this.ess)));
      }
   }

   @Override
   public void takeMoney(final BigDecimal value) {
      this.takeMoney(value, null);
   }

   @Override
   public void takeMoney(final BigDecimal value, final CommandSource initiator) {
      this.takeMoney(value, initiator, UserBalanceUpdateEvent.Cause.UNKNOWN);
   }

   public void takeMoney(final BigDecimal value, final CommandSource initiator, final UserBalanceUpdateEvent.Cause cause) {
      if (value.signum() != 0) {
         try {
            this.setMoney(this.getMoney().subtract(value), cause);
         } catch (MaxMoneyException var5) {
            this.ess.getLogger().log(Level.WARNING, "Invalid call to takeMoney, total balance can't be more than the max-money limit.", (Throwable)var5);
         }

         this.sendTl("takenFromAccount", AdventureUtil.parsed(NumberUtil.displayCurrency(value, this.ess)));
         if (initiator != null) {
            initiator.sendTl(
               "takenFromOthersAccount",
               AdventureUtil.parsed(NumberUtil.displayCurrency(value, this.ess)),
               this.getDisplayName(),
               AdventureUtil.parsed(NumberUtil.displayCurrency(this.getMoney(), this.ess))
            );
         }
      }
   }

   @Override
   public boolean canAfford(final BigDecimal cost) {
      return this.canAfford(cost, true);
   }

   public boolean canAfford(final BigDecimal cost, final boolean permcheck) {
      if (cost.signum() <= 0) {
         return true;
      } else {
         BigDecimal remainingBalance = this.getMoney().subtract(cost);
         return permcheck && !this.isAuthorized("essentials.eco.loan")
            ? remainingBalance.signum() >= 0
            : remainingBalance.compareTo(this.ess.getSettings().getMinMoney()) >= 0;
      }
   }

   public void dispose() {
      this.ess.runTaskAsynchronously(this::_dispose);
   }

   private void _dispose() {
      if (!this.base.isOnline()) {
         this.base = new OfflinePlayerStub(this.getConfigUUID(), this.ess.getServer());
      }

      this.cleanup();
   }

   @Override
   public Boolean canSpawnItem(final Material material) {
      if (this.ess.getSettings().permissionBasedItemSpawn()) {
         String name = material.toString().toLowerCase(Locale.ENGLISH).replace("_", "");
         if (this.isAuthorized("essentials.itemspawn.item-all") || this.isAuthorized("essentials.itemspawn.item-" + name)) {
            return true;
         } else if (VersionUtil.PRE_FLATTENING) {
            int id = material.getId();
            return this.isAuthorized("essentials.itemspawn.item-" + id);
         } else {
            return false;
         }
      } else {
         return this.isAuthorized("essentials.itemspawn.exempt") || !this.ess.getSettings().itemSpawnBlacklist().contains(material);
      }
   }

   @Override
   public void setLastLocation() {
      this.setLastLocation(this.getLocation());
   }

   @Override
   public void setLogoutLocation() {
      this.setLogoutLocation(this.getLocation());
   }

   @Override
   public void requestTeleport(final User player, final boolean here) {
      IUser.TpaRequest request = this.teleportRequestQueue.getOrDefault(player.getName(), new IUser.TpaRequest(player.getName(), player.getUUID()));
      request.setTime(System.currentTimeMillis());
      request.setHere(here);
      request.setLocation(here ? player.getLocation() : this.getLocation());
      this.teleportRequestQueue.remove(request.getName());
      if (this.teleportRequestQueue.size() >= this.ess.getSettings().getTpaMaxRequests()) {
         List<String> keys = new ArrayList<>(this.teleportRequestQueue.keySet());
         this.teleportRequestQueue.remove(keys.get(keys.size() - 1));
      }

      this.teleportRequestQueue.put(request.getName(), request);
   }

   public Collection<String> getPendingTpaKeys() {
      return this.teleportRequestQueue.keySet();
   }

   @Override
   public boolean hasPendingTpaRequests(boolean inform, boolean excludeHere) {
      return this.getNextTpaRequest(inform, false, excludeHere) != null;
   }

   public boolean hasOutstandingTpaRequest(String playerUsername, boolean here) {
      IUser.TpaRequest request = this.getOutstandingTpaRequest(playerUsername, false);
      return request != null && request.isHere() == here;
   }

   @Nullable
   public IUser.TpaRequest getOutstandingTpaRequest(String playerUsername, boolean inform) {
      if (!this.teleportRequestQueue.containsKey(playerUsername)) {
         return null;
      } else {
         long timeout = this.ess.getSettings().getTpaAcceptCancellation();
         IUser.TpaRequest request = this.teleportRequestQueue.get(playerUsername);
         if (timeout >= 1L && System.currentTimeMillis() - request.getTime() > timeout * 1000L) {
            this.teleportRequestQueue.remove(playerUsername);
            if (inform) {
               this.sendTl("requestTimedOutFrom", this.ess.getUser(request.getRequesterUuid()).getDisplayName());
            }

            return null;
         } else {
            return request;
         }
      }
   }

   public IUser.TpaRequest removeTpaRequest(String playerUsername) {
      return this.teleportRequestQueue.remove(playerUsername);
   }

   @Override
   public IUser.TpaRequest getNextTpaRequest(boolean inform, boolean ignoreExpirations, boolean excludeHere) {
      if (this.teleportRequestQueue.isEmpty()) {
         return null;
      } else {
         long timeout = this.ess.getSettings().getTpaAcceptCancellation();
         List<String> keys = new ArrayList<>(this.teleportRequestQueue.keySet());
         Collections.reverse(keys);
         IUser.TpaRequest nextRequest = null;

         for (String key : keys) {
            IUser.TpaRequest request = this.teleportRequestQueue.get(key);
            if (timeout >= 1L && System.currentTimeMillis() - request.getTime() > TimeUnit.SECONDS.toMillis(timeout)) {
               if (inform) {
                  this.sendTl("requestTimedOutFrom", this.ess.getUser(request.getRequesterUuid()).getDisplayName());
               }

               this.teleportRequestQueue.remove(key);
            } else if (!excludeHere || !request.isHere()) {
               if (ignoreExpirations) {
                  return request;
               }

               if (nextRequest == null) {
                  nextRequest = request;
               }
            }
         }

         return nextRequest;
      }
   }

   public String getNick() {
      return this.getNick(true, true);
   }

   public String getNick(final boolean longNick) {
      return this.getNick(true, true);
   }

   public String getNick(final boolean longNick, final boolean withPrefix, final boolean withSuffix) {
      return this.getNick(withPrefix, withSuffix);
   }

   public String getNick(final boolean withPrefix, final boolean withSuffix) {
      StringBuilder prefix = new StringBuilder();
      String suffix = "";
      String nick = this.getNickname();
      String nickname;
      if (this.ess.getSettings().isCommandDisabled("nick") || nick == null || nick.isEmpty() || nick.equals(this.getName())) {
         nickname = this.getName();
      } else if (nick.equalsIgnoreCase(this.getName())) {
         nickname = nick;
      } else {
         if (this.isAuthorized("essentials.nick.hideprefix")) {
            nickname = nick;
         } else {
            nickname = FormatUtil.replaceFormat(this.ess.getSettings().getNicknamePrefix()) + nick;
         }

         suffix = "§r";
      }

      if (this.getBase().isOp()) {
         try {
            String opPrefix = this.ess.getSettings().getOperatorColor();
            if (opPrefix != null && !opPrefix.isEmpty()) {
               prefix.insert(0, opPrefix);
               suffix = "§r";
            }
         } catch (Exception var9) {
            if (this.ess.getSettings().isDebug()) {
               var9.printStackTrace();
            }
         }
      }

      if (this.ess.getSettings().addPrefixSuffix()) {
         if (withPrefix || !this.ess.getSettings().disablePrefix()) {
            String ptext = FormatUtil.replaceFormat(this.ess.getPermissionsHandler().getPrefix(this.base));
            prefix.insert(0, ptext);
            suffix = "§r";
         }

         if (withSuffix || !this.ess.getSettings().disableSuffix()) {
            String stext = FormatUtil.replaceFormat(this.ess.getPermissionsHandler().getSuffix(this.base));
            suffix = stext + "§r";
            suffix = suffix.replace("§f§r", "§r").replace("§r§r", "§r");
         }
      }

      String strPrefix = prefix.toString();
      String output = strPrefix + nickname + suffix;
      if (output.charAt(output.length() - 1) == 167) {
         output = output.substring(0, output.length() - 1);
      }

      return output;
   }

   public void setDisplayNick() {
      if (this.base.isOnline() && this.ess.getSettings().changeDisplayName()) {
         this.getBase().setDisplayName(this.getNick(true));
         if (this.isAfk()) {
            this.updateAfkListName();
         } else if (this.ess.getSettings().changePlayerListName()) {
            String name = this.getNick(this.ess.getSettings().isAddingPrefixInPlayerlist(), this.ess.getSettings().isAddingSuffixInPlayerlist());

            try {
               this.getBase().setPlayerListName(name);
            } catch (IllegalArgumentException var3) {
               if (this.ess.getSettings().isDebug()) {
                  this.ess.getLogger().log(Level.INFO, "Playerlist for " + name + " was not updated. Name clashed with another online player.");
               }
            }
         }
      }
   }

   @Override
   public String getDisplayName() {
      return super.getBase().getDisplayName() != null && (!this.ess.getSettings().hideDisplayNameInVanish() || !this.isHidden())
         ? super.getBase().getDisplayName()
         : super.getBase().getName();
   }

   @Override
   public String getFormattedNickname() {
      String rawNickname = this.getNickname();
      return rawNickname == null ? null : FormatUtil.replaceFormat(this.ess.getSettings().getNicknamePrefix() + rawNickname);
   }

   public AsyncTeleport getAsyncTeleport() {
      return this.teleport;
   }

   public long getLastOnlineActivity() {
      return this.lastOnlineActivity;
   }

   public void setLastOnlineActivity(final long timestamp) {
      this.lastOnlineActivity = timestamp;
   }

   @Override
   public BigDecimal getMoney() {
      long start = System.nanoTime();
      BigDecimal value = this._getMoney();
      long elapsed = System.nanoTime() - start;
      if (elapsed > this.ess.getSettings().getEconomyLagWarning()) {
         this.ess.getLogger().log(Level.INFO, "Lag Notice - Slow Economy Response - Request took over {0}ms!", elapsed / 1000000.0);
      }

      return value;
   }

   @Override
   public void setMoney(final BigDecimal value) throws MaxMoneyException {
      this.setMoney(value, UserBalanceUpdateEvent.Cause.UNKNOWN);
   }

   private BigDecimal _getMoney() {
      if (this.ess.getSettings().isEcoDisabled()) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info("Internal economy functions disabled, aborting balance check.");
         }

         return BigDecimal.ZERO;
      } else {
         EconomyLayer layer = EconomyLayers.getSelectedLayer();
         return layer == null || !layer.hasAccount(this.getBase()) && !layer.createPlayerAccount(this.getBase())
            ? super.getMoney()
            : layer.getBalance(this.getBase());
      }
   }

   public void setMoney(final BigDecimal value, final UserBalanceUpdateEvent.Cause cause) throws MaxMoneyException {
      if (this.ess.getSettings().isEcoDisabled()) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info("Internal economy functions disabled, aborting balance change.");
         }
      } else {
         BigDecimal oldBalance = this._getMoney();
         UserBalanceUpdateEvent updateEvent = new UserBalanceUpdateEvent(this.getBase(), oldBalance, value, cause);
         this.ess.getServer().getPluginManager().callEvent(updateEvent);
         BigDecimal newBalance = updateEvent.getNewBalance();
         EconomyLayer layer = EconomyLayers.getSelectedLayer();
         if (layer != null && (layer.hasAccount(this.getBase()) || layer.createPlayerAccount(this.getBase()))) {
            layer.set(this.getBase(), newBalance);
         }

         super.setMoney(newBalance, true);
         Trade.log("Update", "Set", "API", this.getName(), new Trade(newBalance, this.ess), null, null, null, newBalance, this.ess);
      }
   }

   public void updateMoneyCache(final BigDecimal value) {
      if (!this.ess.getSettings().isEcoDisabled() && EconomyLayers.isLayerSelected() && !super.getMoney().equals(value)) {
         try {
            super.setMoney(value, false);
         } catch (MaxMoneyException var3) {
         }
      }
   }

   @Override
   public void setAfk(final boolean set) {
      this.setAfk(set, AfkStatusChangeEvent.Cause.UNKNOWN);
   }

   @Override
   public void setAfk(final boolean set, final AfkStatusChangeEvent.Cause cause) {
      AfkStatusChangeEvent afkEvent = new AfkStatusChangeEvent(this, set, cause);
      this.ess.getServer().getPluginManager().callEvent(afkEvent);
      if (!afkEvent.isCancelled()) {
         this.getBase().setSleepingIgnored(this.isAuthorized("essentials.sleepingignored") || set && this.ess.getSettings().sleepIgnoresAfkPlayers());
         if (set && !this.isAfk()) {
            this.afkPosition = this.getLocation();
            this.afkSince = System.currentTimeMillis();
         } else if (!set && this.isAfk()) {
            this.afkPosition = null;
            this.afkMessage = null;
            this.afkSince = 0L;
         }

         this._setAfk(set);
         this.updateAfkListName();
      }
   }

   private void updateAfkListName() {
      if (this.ess.getSettings().isAfkListName()) {
         if (this.isAfk()) {
            String afkName = this.ess.getSettings().getAfkListName().replace("{PLAYER}", this.getDisplayName()).replace("{USERNAME}", this.getName());
            this.getBase().setPlayerListName(afkName);
         } else {
            this.getBase().setPlayerListName(null);
            this.setDisplayNick();
         }
      }
   }

   @Deprecated
   public boolean toggleAfk() {
      return this.toggleAfk(AfkStatusChangeEvent.Cause.UNKNOWN);
   }

   public boolean toggleAfk(final AfkStatusChangeEvent.Cause cause) {
      this.setAfk(!this.isAfk(), cause);
      return this.isAfk();
   }

   @Override
   public boolean isHiddenFrom(Player player) {
      return !(this.getBase() instanceof OfflinePlayerStub) && !(player instanceof OfflinePlayerStub) ? !player.canSee(this.getBase()) : true;
   }

   @Override
   public boolean isHidden() {
      return this.hidden;
   }

   @Override
   public boolean isLeavingHidden() {
      return this.leavingHidden;
   }

   @Override
   public void setLeavingHidden(boolean leavingHidden) {
      this.leavingHidden = leavingHidden;
   }

   @Override
   public void setHidden(final boolean hidden) {
      this.hidden = hidden;
      if (hidden) {
         this.setLastLogout(this.getLastOnlineActivity());
      }
   }

   public boolean isHidden(final Player player) {
      return this.hidden || this.isHiddenFrom(player);
   }

   @Override
   public String getFormattedJailTime() {
      return DateUtil.formatDateDiff(this.getOnlineJailedTime() > 0L ? this.getOnlineJailExpireTime() : this.getJailTimeout());
   }

   private long getOnlineJailExpireTime() {
      return (this.getOnlineJailedTime() - this.getBase().getStatistic(PLAY_ONE_TICK)) * 50L + System.currentTimeMillis();
   }

   public boolean checkJailTimeout(final long currentTime) {
      if (this.getJailTimeout() > 0L) {
         if (this.getOnlineJailedTime() > 0L && this.getOnlineJailedTime() > this.getBase().getStatistic(PLAY_ONE_TICK)) {
            return false;
         }

         if (this.getJailTimeout() < currentTime && this.isJailed()) {
            JailStatusChangeEvent event = new JailStatusChangeEvent(this, null, false);
            this.ess.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               this.setJailTimeout(0L);
               this.setOnlineJailedTime(0L);
               this.setJailed(false);
               this.sendTl("haveBeenReleased");
               this.setJail(null);
               if (this.ess.getSettings().getTeleportWhenFreePolicy() == ISettings.TeleportWhenFreePolicy.BACK) {
                  CompletableFuture<Boolean> future = new CompletableFuture<>();
                  this.getAsyncTeleport().back(future);
                  future.exceptionally(e -> {
                     this.getAsyncTeleport().respawn(null, TeleportCause.PLUGIN, new CompletableFuture<>());
                     return false;
                  });
               } else if (this.ess.getSettings().getTeleportWhenFreePolicy() == ISettings.TeleportWhenFreePolicy.SPAWN) {
                  this.getAsyncTeleport().respawn(null, TeleportCause.PLUGIN, new CompletableFuture<>());
               }

               return true;
            }
         }
      }

      return false;
   }

   public boolean checkMuteTimeout(final long currentTime) {
      if (this.getMuteTimeout() > 0L && this.getMuteTimeout() < currentTime && this.isMuted()) {
         MuteStatusChangeEvent event = new MuteStatusChangeEvent(this, null, false, this.getMuteTimeout(), this.getMuteReason());
         this.ess.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.setMuteTimeout(0L);
            this.sendTl("canTalkAgain");
            this.setMuted(false);
            this.setMuteReason(null);
            return true;
         }
      }

      return false;
   }

   @Override
   public long getLastActivityTime() {
      return this.lastActivity;
   }

   @Deprecated
   public void updateActivity(final boolean broadcast) {
      this.updateActivity(broadcast, AfkStatusChangeEvent.Cause.UNKNOWN);
   }

   public void updateActivity(final boolean broadcast, final AfkStatusChangeEvent.Cause cause) {
      if (this.isAfk()) {
         this.setAfk(false, cause);
         if (broadcast && !this.isHidden() && !this.isAfk()) {
            this.setDisplayNick();
            if (this.ess.getSettings().broadcastAfkMessage()) {
               this.ess.broadcastTl(this, u -> u == this, "userIsNotAway", this.getDisplayName());
            }

            this.sendTl("userIsNotAwaySelf", this.getDisplayName());
         }
      }

      this.lastActivity = System.currentTimeMillis();
   }

   public void updateActivityOnMove(final boolean broadcast) {
      if (this.ess.getSettings().cancelAfkOnMove()) {
         this.updateActivity(broadcast, AfkStatusChangeEvent.Cause.MOVE);
      }
   }

   public void updateActivityOnInteract(final boolean broadcast) {
      if (this.ess.getSettings().cancelAfkOnInteract()) {
         this.updateActivity(broadcast, AfkStatusChangeEvent.Cause.INTERACT);
      }
   }

   public void updateActivityOnChat(final boolean broadcast) {
      if (this.ess.getSettings().cancelAfkOnChat()) {
         this.ess.scheduleSyncDelayedTask(() -> this.updateActivity(broadcast, AfkStatusChangeEvent.Cause.CHAT));
      }
   }

   public void checkActivity() {
      if (System.currentTimeMillis() - this.lastActivity > 10000L) {
         long autoafktimeout = this.ess.getSettings().getAutoAfkTimeout();
         if (autoafktimeout > 0L
            && this.lastActivity > 0L
            && this.lastActivity + autoafktimeout * 1000L < System.currentTimeMillis()
            && !this.isAuthorized("essentials.kick.exempt")
            && !this.isAuthorized("essentials.afk.kickexempt")) {
            this.lastActivity = 0L;
            double kickTime = autoafktimeout / 60.0;
            if (this.ess.getSettings().getAfkTimeoutCommands().isEmpty()) {
               this.getBase().kickPlayer(this.ess.getAdventureFacet().miniToLegacy(this.playerTl("autoAfkKickReason", kickTime)));

               for (User user : this.ess.getOnlineUsers()) {
                  if (user.isAuthorized("essentials.kick.notify")) {
                     user.sendTl("playerKicked", Console.displayName(), this.getName(), user.playerTl("autoAfkKickReason", kickTime));
                  }
               }
            } else {
               for (String command : this.ess.getSettings().getAfkTimeoutCommands()) {
                  if (command != null && !command.isEmpty()) {
                     String cmd = command.replace("{USERNAME}", this.getName()).replace("{KICKTIME}", String.valueOf(kickTime));
                     this.ess.getServer().dispatchCommand(this.ess.getServer().getConsoleSender(), cmd);
                  }
               }
            }
         }

         long autoafk = this.ess.getSettings().getAutoAfk();
         if (!this.isAfk()
            && autoafk > 0L
            && this.lastActivity + autoafk * 1000L < System.currentTimeMillis()
            && this.isAuthorizedCached("essentials.afk.auto")) {
            this.setAfk(true, AfkStatusChangeEvent.Cause.ACTIVITY);
            if (this.isAfk() && !this.isHidden()) {
               this.setDisplayNick();
               if (this.ess.getSettings().broadcastAfkMessage()) {
                  this.ess.broadcastTl(this, u -> u == this, "userIsAway", this.getDisplayName());
               }

               this.sendTl("userIsAwaySelf", this.getDisplayName());
            }
         }
      }
   }

   public Location getAfkPosition() {
      return this.afkPosition;
   }

   @Override
   public boolean isGodModeEnabled() {
      if (super.isGodModeEnabled() && !this.ess.getSettings().getNoGodWorlds().contains(this.getLocation().getWorld().getName())) {
         return true;
      } else {
         return this.isAfk() ? this.ess.getSettings().getFreezeAfkPlayers() : false;
      }
   }

   public boolean isGodModeEnabledRaw() {
      return super.isGodModeEnabled();
   }

   @Override
   public String getGroup() {
      String result = this.ess.getPermissionsHandler().getGroup(this.base);
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "looking up groupname of " + this.base.getName() + " - " + result);
      }

      return result;
   }

   @Override
   public boolean inGroup(final String group) {
      boolean result = this.ess.getPermissionsHandler().inGroup(this.base, group);
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "checking if " + this.base.getName() + " is in group " + group + " - " + result);
      }

      return result;
   }

   @Override
   public boolean canBuild() {
      return this.getBase().isOp() ? true : this.ess.getPermissionsHandler().canBuild(this.base, this.getGroup());
   }

   @Deprecated
   @Override
   public long getTeleportRequestTime() {
      IUser.TpaRequest request = this.getNextTpaRequest(false, false, false);
      return request == null ? 0L : request.getTime();
   }

   public boolean isInvSee() {
      return this.invSee;
   }

   public void setInvSee(final boolean set) {
      this.invSee = set;
   }

   public boolean isEnderSee() {
      return this.enderSee;
   }

   public void setEnderSee(final boolean set) {
      this.enderSee = set;
   }

   @Override
   public void enableInvulnerabilityAfterTeleport() {
      long time = this.ess.getSettings().getTeleportInvulnerability();
      if (time > 0L) {
         this.teleportInvulnerabilityTimestamp = System.currentTimeMillis() + time;
      }
   }

   @Override
   public void resetInvulnerabilityAfterTeleport() {
      if (this.teleportInvulnerabilityTimestamp != 0L && this.teleportInvulnerabilityTimestamp < System.currentTimeMillis()) {
         this.teleportInvulnerabilityTimestamp = 0L;
      }
   }

   @Override
   public boolean hasInvulnerabilityAfterTeleport() {
      return this.teleportInvulnerabilityTimestamp != 0L && this.teleportInvulnerabilityTimestamp >= System.currentTimeMillis();
   }

   public boolean canInteractVanished() {
      return this.isAuthorized("essentials.vanish.interact");
   }

   @Override
   public boolean isIgnoreMsg() {
      return this.ignoreMsg;
   }

   @Override
   public void setIgnoreMsg(final boolean ignoreMsg) {
      this.ignoreMsg = ignoreMsg;
   }

   @Override
   public boolean isVanished() {
      return this.vanished;
   }

   @Override
   public void setVanished(final boolean set) {
      this.vanished = set;
      if (set) {
         for (User user : this.ess.getOnlineUsers()) {
            if (!user.isAuthorized("essentials.vanish.see")) {
               user.getBase().hidePlayer(this.getBase());
            }
         }

         this.setHidden(true);
         this.lastVanishTime = System.currentTimeMillis();
         this.ess.getVanishedPlayersNew().add(this.getName());
         this.getBase().setMetadata("vanished", new FixedMetadataValue(this.ess, true));
         if (this.isAuthorized("essentials.vanish.effect")) {
            this.getBase().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false));
         }

         if (this.ess.getSettings().sleepIgnoresVanishedPlayers()) {
            this.getBase().setSleepingIgnored(true);
         }
      } else {
         for (Player p : this.ess.getOnlinePlayers()) {
            p.showPlayer(this.getBase());
         }

         this.setHidden(false);
         this.ess.getVanishedPlayersNew().remove(this.getName());
         this.getBase().setMetadata("vanished", new FixedMetadataValue(this.ess, false));
         if (this.isAuthorized("essentials.vanish.effect")) {
            this.getBase().removePotionEffect(PotionEffectType.INVISIBILITY);
         }

         if (this.ess.getSettings().sleepIgnoresVanishedPlayers() && !this.isAuthorized("essentials.sleepingignored")) {
            this.getBase().setSleepingIgnored(false);
         }
      }
   }

   public boolean checkSignThrottle() {
      if (this.isSignThrottled()) {
         return true;
      } else {
         this.updateThrottle();
         return false;
      }
   }

   public boolean isSignThrottled() {
      long minTime = this.lastThrottledAction + 1000 / this.ess.getSettings().getSignUsePerSecond();
      return System.currentTimeMillis() < minTime;
   }

   public void updateThrottle() {
      this.lastThrottledAction = System.currentTimeMillis();
   }

   public boolean isFlyClickJump() {
      return this.rightClickJump;
   }

   public void setRightClickJump(final boolean rightClickJump) {
      this.rightClickJump = rightClickJump;
   }

   @Override
   public boolean isIgnoreExempt() {
      return this.isAuthorized("essentials.chat.ignoreexempt");
   }

   public boolean isRecipeSee() {
      return this.recipeSee;
   }

   public void setRecipeSee(final boolean recipeSee) {
      this.recipeSee = recipeSee;
   }

   @Override
   public void sendMessage(final String message) {
      if (!message.isEmpty()) {
         this.base.sendMessage(message);
      }
   }

   @Override
   public void sendComponent(ComponentHolder component) {
      this.ess.getAdventureFacet().send(this.base, component);
   }

   @Override
   public ComponentHolder tlComponent(String tlKey, Object... args) {
      String translation = this.playerTl(tlKey, args);
      return this.ess.getAdventureFacet().deserializeMiniMessage(translation);
   }

   @Override
   public void sendTl(String tlKey, Object... args) {
      String translation = this.playerTl(tlKey, args);
      if (!translation.trim().isEmpty()) {
         this.sendComponent(this.ess.getAdventureFacet().deserializeMiniMessage(translation));
      }
   }

   @Override
   public String playerTl(String tlKey, Object... args) {
      if (this.ess.getSettings().isPerPlayerLocale()) {
         PlayerLocaleProvider provider = this.ess.provider(PlayerLocaleProvider.class);
         Locale locale = this.base != null ? this.getPlayerLocale(provider.getLocale(this.base)) : this.playerLocale;
         if (locale != null) {
            return I18n.tlLocale(locale, tlKey, args);
         }
      }

      return I18n.tlLiteral(tlKey, args);
   }

   @Override
   public String tlSender(String tlKey, Object... args) {
      return this.playerTl(tlKey, args);
   }

   public Locale getPlayerLocale(final String locale) {
      if (locale != null && !locale.equals(this.lastLocaleString)) {
         this.lastLocaleString = locale;
         return this.playerLocale = I18n.getLocale(locale);
      } else {
         return this.playerLocale;
      }
   }

   public int compareTo(final User other) {
      return FormatUtil.stripFormat(this.getDisplayName()).compareToIgnoreCase(FormatUtil.stripFormat(other.getDisplayName()));
   }

   @Override
   public boolean equals(final Object object) {
      return !(object instanceof User) ? false : this.getName().equalsIgnoreCase(((User)object).getName());
   }

   @Override
   public int hashCode() {
      return this.getName().hashCode();
   }

   @Override
   public CommandSource getSource() {
      return new CommandSource(this.ess, this.getBase());
   }

   @Override
   public String getName() {
      return this.getBase().getName();
   }

   @Override
   public UUID getUUID() {
      return this.getBase().getUniqueId();
   }

   @Override
   public boolean isReachable() {
      return this.getBase().isOnline();
   }

   @Override
   public IMessageRecipient.MessageResponse sendMessage(final IMessageRecipient recipient, final String message) {
      return this.messageRecipient.sendMessage(recipient, message);
   }

   @Override
   public IMessageRecipient.MessageResponse onReceiveMessage(final IMessageRecipient sender, final String message) {
      return this.messageRecipient.onReceiveMessage(sender, message);
   }

   @Override
   public IMessageRecipient getReplyRecipient() {
      return this.messageRecipient.getReplyRecipient();
   }

   @Override
   public void setReplyRecipient(final IMessageRecipient recipient) {
      this.messageRecipient.setReplyRecipient(recipient);
   }

   @Override
   public String getAfkMessage() {
      return this.afkMessage;
   }

   @Override
   public void setAfkMessage(final String message) {
      if (this.isAfk()) {
         this.afkMessage = message;
      }
   }

   @Override
   public long getAfkSince() {
      return this.afkSince;
   }

   @Override
   public Map<User, BigDecimal> getConfirmingPayments() {
      return this.confirmingPayments;
   }

   public String getConfirmingClearCommand() {
      return this.confirmingClearCommand;
   }

   public void setConfirmingClearCommand(final String command) {
      this.confirmingClearCommand = command;
   }

   public ItemStack getItemInHand() {
      return Inventories.getItemInHand(this.getBase());
   }

   @Override
   public void sendMail(MailSender sender, String message) {
      this.sendMail(sender, message, 0L);
   }

   @Override
   public void sendMail(MailSender sender, String message, long expireAt) {
      this.ess.getMail().sendMail(this, sender, message, expireAt);
   }

   @Deprecated
   @Override
   public void addMail(String mail) {
      this.ess.getMail().sendLegacyMail(this, mail);
   }

   public void notifyOfMail() {
      int unread = this.getUnreadMailAmount();
      if (unread != 0) {
         int notifyPlayerOfMailCooldown = this.ess.getSettings().getNotifyPlayerOfMailCooldown() * 1000;
         if (System.currentTimeMillis() - this.lastNotifiedAboutMailsMs >= notifyPlayerOfMailCooldown) {
            this.sendTl("youHaveNewMail", unread);
            this.lastNotifiedAboutMailsMs = System.currentTimeMillis();
         }
      }
   }

   public String getLastHomeConfirmation() {
      return this.lastHomeConfirmation;
   }

   public void setLastHomeConfirmation(final String lastHomeConfirmation) {
      this.lastHomeConfirmation = lastHomeConfirmation;
   }

   public long getLastHomeConfirmationTimestamp() {
      return this.lastHomeConfirmationTimestamp;
   }

   public void setLastHomeConfirmationTimestamp() {
      this.lastHomeConfirmationTimestamp = System.currentTimeMillis();
   }

   public List<String> getSignCopy() {
      return this.signCopy;
   }

   @Override
   public boolean isFreeze() {
      return this.freeze;
   }

   @Override
   public void setFreeze(boolean freeze) {
      this.freeze = freeze;
   }

   public boolean isBaltopExempt() {
      if (this.getBase().isOnline()) {
         boolean exempt = this.isAuthorized("essentials.balancetop.exclude");
         this.setBaltopExemptCache(exempt);
         return exempt;
      } else {
         return this.isBaltopExcludeCache();
      }
   }

   public long getLastVanishTime() {
      return this.lastVanishTime;
   }

   @Override
   public Block getTargetBlock(int maxDistance) {
      Block block;
      return !VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_13_2_R01) && (block = this.base.getTargetBlockExact(maxDistance)) != null
         ? block
         : this.base.getTargetBlock(null, maxDistance);
   }

   @Override
   public void setToggleShout(boolean toggleShout) {
      this.toggleShout = toggleShout;
      if (this.ess.getSettings().isPersistShout()) {
         this.setShouting(toggleShout);
      }
   }

   @Override
   public boolean isToggleShout() {
      return this.ess.getSettings().isPersistShout()
         ? this.toggleShout = this.isShouting()
         : this.toggleShout == null ? (this.toggleShout = this.ess.getSettings().isShoutDefault()) : this.toggleShout;
   }

   public int getFlightTick() {
      return this.flightTick;
   }

   public void setFlightTick(int flightTick) {
      this.flightTick = flightTick;
   }
}
