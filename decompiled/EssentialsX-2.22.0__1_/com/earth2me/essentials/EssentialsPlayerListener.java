package com.earth2me.essentials;

import com.destroystokyo.paper.ClientOption;
import com.earth2me.essentials.adventure.ComponentHolder;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.paperlib.PaperLib;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.TextInput;
import com.earth2me.essentials.textreader.TextPager;
import com.earth2me.essentials.userstorage.ModernUserMap;
import com.earth2me.essentials.utils.CommonPlaceholders;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.VersionUtil;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.ess3.api.events.AfkStatusChangeEvent;
import net.ess3.provider.CommandSendListenerProvider;
import net.ess3.provider.FormattedCommandAliasProvider;
import net.ess3.provider.InventoryViewProvider;
import net.ess3.provider.KnownCommandsProvider;
import net.ess3.provider.TickCountProvider;
import net.ess3.provider.providers.BukkitCommandSendListenerProvider;
import net.ess3.provider.providers.PaperCommandSendListenerProvider;
import net.essentialsx.PaperAdventureSmuggler;
import net.essentialsx.api.v2.events.AsyncUserDataLoadEvent;
import org.bukkit.BanEntry;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.BanList.Type;
import org.bukkit.World.Environment;
import org.bukkit.ban.IpBanList;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.Command;
import org.bukkit.command.FormattedCommandAlias;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EssentialsPlayerListener implements Listener {
   private final transient net.ess3.api.IEssentials ess;
   private final ConcurrentHashMap<UUID, Integer> pendingMotdTasks = new ConcurrentHashMap<>();

   public EssentialsPlayerListener(final net.ess3.api.IEssentials parent) {
      this.ess = parent;
   }

   private static boolean isEntityPickupEvent() {
      try {
         Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   private static boolean isCommandSendEvent() {
      try {
         Class.forName("org.bukkit.event.player.PlayerCommandSendEvent");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   private static boolean isPaperCommandSendEvent() {
      try {
         Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   private static boolean isArrowPickupEvent() {
      try {
         Class.forName("org.bukkit.event.player.PlayerPickupArrowEvent");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   private static boolean isGameEventEvent() {
      try {
         Class.forName("org.bukkit.event.block.BlockReceiveGameEvent");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   public void registerEvents() {
      this.ess.getServer().getPluginManager().registerEvents(this, this.ess);
      if (isArrowPickupEvent()) {
         this.ess.getServer().getPluginManager().registerEvents(new EssentialsPlayerListener.ArrowPickupListener(), this.ess);
      }

      if (isGameEventEvent()) {
         this.ess.getServer().getPluginManager().registerEvents(new EssentialsPlayerListener.SculkListener1_17(), this.ess);
      }

      if (isEntityPickupEvent()) {
         this.ess.getServer().getPluginManager().registerEvents(new EssentialsPlayerListener.PickupListener1_12(), this.ess);
      } else {
         this.ess.getServer().getPluginManager().registerEvents(new EssentialsPlayerListener.PickupListenerPre1_12(), this.ess);
      }

      if (isPaperCommandSendEvent()) {
         this.ess
            .getServer()
            .getPluginManager()
            .registerEvents(new PaperCommandSendListenerProvider(new EssentialsPlayerListener.CommandSendFilter()), this.ess);
      } else if (isCommandSendEvent()) {
         this.ess
            .getServer()
            .getPluginManager()
            .registerEvents(new BukkitCommandSendListenerProvider(new EssentialsPlayerListener.CommandSendFilter()), this.ess);
      }

      if (VersionUtil.isPaper() && VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_21_8_R01)) {
         this.ess.getServer().getPluginManager().registerEvents(new EssentialsPlayerListener.LoginListener1_21(), this.ess);
         this.ess.getServer().getPluginManager().registerEvents(new EssentialsPlayerListener.JoinListener1_21(), this.ess);
      } else {
         this.ess.getServer().getPluginManager().registerEvents(new EssentialsPlayerListener.LoginListenerPre1_21(), this.ess);
         this.ess.getServer().getPluginManager().registerEvents(new EssentialsPlayerListener.JoinListenerPre1_21(), this.ess);
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerRespawn(final PlayerRespawnEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      this.updateCompass(user);
      user.setDisplayNick();
      if (this.ess.getSettings().isTeleportInvulnerability()) {
         user.enableInvulnerabilityAfterTeleport();
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerChat(final AsyncPlayerChatEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      if (user.isMuted()) {
         event.setCancelled(true);
         String dateDiff = user.getMuteTimeout() > 0L ? DateUtil.formatDateDiff(user.getMuteTimeout()) : null;
         if (dateDiff == null) {
            if (user.hasMuteReason()) {
               user.sendTl("voiceSilencedReason", user.getMuteReason());
            } else {
               user.sendTl("voiceSilenced");
            }
         } else if (user.hasMuteReason()) {
            user.sendTl("voiceSilencedReasonTime", dateDiff, user.getMuteReason());
         } else {
            user.sendTl("voiceSilencedTime", dateDiff);
         }

         this.ess.getLogger().info(this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("mutedUserSpeaks", user.getName(), event.getMessage())));
      }

      try {
         Iterator<Player> it = event.getRecipients().iterator();

         while (it.hasNext()) {
            User u = this.ess.getUser(it.next());
            if (u.isIgnoredPlayer(user)) {
               it.remove();
            }
         }
      } catch (UnsupportedOperationException var5) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "Ignore could not block chat due to custom chat plugin event.", (Throwable)var5);
         } else {
            this.ess.getLogger().info("Ignore could not block chat due to custom chat plugin event.");
         }
      }

      user.updateActivityOnChat(true);
      user.setDisplayNick();
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerMove(final PlayerMoveEvent event) {
      if (event.getFrom().getBlockX() != event.getTo().getBlockX()
         || event.getFrom().getBlockZ() != event.getTo().getBlockZ()
         || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
         User user = this.ess.getUser(event.getPlayer());
         if (user.isFreeze()) {
            Location from = event.getFrom();
            Location to = event.getTo().clone();
            to.setX(from.getX());
            to.setY(from.getY());
            to.setZ(from.getZ());

            try {
               event.setTo(LocationUtil.getSafeDestination(this.ess, to));
            } catch (Exception var7) {
               event.setTo(to);
            }
         } else if (this.ess.getSettings().cancelAfkOnMove() || this.ess.getSettings().getFreezeAfkPlayers()) {
            if (user.isAfk() && this.ess.getSettings().getFreezeAfkPlayers()) {
               Location from = event.getFrom();
               Location origTo = event.getTo();
               Location to = origTo.clone();
               if (origTo.getY() >= from.getBlockY() + 1) {
                  user.updateActivityOnMove(true);
               } else {
                  to.setX(from.getX());
                  to.setY(from.getY());
                  to.setZ(from.getZ());

                  try {
                     if (event.getPlayer().getAllowFlight()) {
                        throw new Exception();
                     }

                     event.setTo(LocationUtil.getSafeDestination(this.ess, to));
                  } catch (Exception var8) {
                     event.setTo(to);
                  }
               }
            } else {
               Location afk = user.getAfkPosition();
               if (afk == null || !event.getTo().getWorld().equals(afk.getWorld()) || afk.distanceSquared(event.getTo()) > 9.0) {
                  user.updateActivityOnMove(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerQuit(final PlayerQuitEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      Integer pendingId = this.pendingMotdTasks.remove(user.getUUID());
      if (pendingId != null) {
         this.ess.getScheduler().cancelTask(pendingId);
      }

      if (!this.hideJoinQuitMessages() && (!this.ess.getSettings().allowSilentJoinQuit() || !user.isAuthorized("essentials.silentquit"))) {
         if (this.ess.getSettings().isCustomQuitMessage() && event.getQuitMessage() != null) {
            Player player = event.getPlayer();
            String msg = this.ess
               .getSettings()
               .getCustomQuitMessage()
               .replace("{PLAYER}", player.getDisplayName())
               .replace("{USERNAME}", player.getName())
               .replace("{ONLINE}", NumberFormat.getInstance().format((long)(this.ess.getOnlinePlayers().size() - 1)))
               .replace("{UPTIME}", DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()))
               .replace("{PREFIX}", FormatUtil.replaceFormat(this.ess.getPermissionsHandler().getPrefix(player)))
               .replace("{SUFFIX}", FormatUtil.replaceFormat(this.ess.getPermissionsHandler().getSuffix(player)));
            event.setQuitMessage(msg.isEmpty() ? null : msg);
         }
      } else {
         event.setQuitMessage(null);
      }

      user.startTransaction();
      if (this.ess.getSettings().removeGodOnDisconnect() && user.isGodModeEnabled()) {
         user.setGodModeEnabled(false);
      }

      if (user.isVanished()) {
         user.setLeavingHidden(true);
         user.setVanished(false);
      }

      user.setLogoutLocation();
      if (user.isRecipeSee()) {
         this.ess.provider(InventoryViewProvider.class).getTopInventory(user.getBase().getOpenInventory()).clear();
      }

      for (HumanEntity viewer : new ArrayList(user.getBase().getInventory().getViewers())) {
         if (viewer instanceof Player) {
            User uviewer = this.ess.getUser((Player)viewer);
            if (uviewer.isInvSee()) {
               uviewer.getBase().closeInventory();
            }
         }
      }

      user.updateActivity(false, AfkStatusChangeEvent.Cause.QUIT);
      if (!user.isHidden()) {
         user.setLastLogout(System.currentTimeMillis());
      }

      user.stopTransaction();
      user.dispose();
   }

   private boolean hideJoinQuitMessages() {
      return this.ess.getSettings().hasJoinQuitMessagePlayerCount()
         && this.ess.getServer().getOnlinePlayers().size() > this.ess.getSettings().getJoinQuitMessagePlayerCount();
   }

   private void legacyJoinFlow(final PlayerJoinEvent event) {
      String joinMessage = event.getJoinMessage();
      this.ess.runTaskAsynchronously(() -> this.delayedJoin(event.getPlayer(), joinMessage));
      if (this.hideJoinQuitMessages() || this.ess.getSettings().allowSilentJoinQuit() || this.ess.getSettings().isCustomJoinMessage()) {
         event.setJoinMessage(null);
      }
   }

   private void joinFlow(final User user, final long currentTime, final String message, final Consumer<String> joinMessageConsumer) {
      user.startTransaction();
      String lastAccountName = user.getLastAccountName();
      user.setLastAccountName(user.getBase().getName());
      boolean newUsername = lastAccountName != null && !lastAccountName.equals(user.getBase().getName());
      if (this.ess.getSettings().isResetNickOnNameChange() && newUsername && user.getNickname() != null) {
         user.setNickname(null);
      }

      user.setLastLogin(currentTime);
      user.setDisplayNick();
      this.updateCompass(user);
      user.setLeavingHidden(false);
      if (!this.ess.getVanishedPlayersNew().isEmpty() && !user.isAuthorized("essentials.vanish.see")) {
         for (String p : this.ess.getVanishedPlayersNew()) {
            Player toVanish = this.ess.getServer().getPlayerExact(p);
            if (toVanish != null && toVanish.isOnline()) {
               user.getBase().hidePlayer(toVanish);
               if (this.ess.getSettings().isDebug()) {
                  this.ess.getLogger().info("Hiding vanished player: " + p);
               }
            }
         }
      }

      if (user.isAuthorized("essentials.sleepingignored")) {
         user.getBase().setSleepingIgnored(true);
      }

      String effectiveMessage;
      if (!this.ess.getSettings().allowSilentJoinQuit() || !user.isAuthorized("essentials.silentjoin") && !user.isAuthorized("essentials.silentjoin.vanish")) {
         if (message == null || this.hideJoinQuitMessages()) {
            effectiveMessage = null;
         } else if (this.ess.getSettings().isCustomJoinMessage()) {
            String msg = (newUsername && this.ess.getSettings().isCustomNewUsernameMessage()
                  ? this.ess.getSettings().getCustomNewUsernameMessage()
                  : this.ess.getSettings().getCustomJoinMessage())
               .replace("{PLAYER}", user.getDisplayName())
               .replace("{USERNAME}", user.getName())
               .replace("{UNIQUE}", NumberFormat.getInstance().format((long)this.ess.getUsers().getUserCount()))
               .replace("{ONLINE}", NumberFormat.getInstance().format((long)this.ess.getOnlinePlayers().size()))
               .replace("{UPTIME}", DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()))
               .replace("{PREFIX}", FormatUtil.replaceFormat(this.ess.getPermissionsHandler().getPrefix(user.getBase())))
               .replace("{SUFFIX}", FormatUtil.replaceFormat(this.ess.getPermissionsHandler().getSuffix(user.getBase())))
               .replace("{OLDUSERNAME}", lastAccountName == null ? "" : lastAccountName);
            effectiveMessage = msg.isEmpty() ? null : msg;
         } else if (this.ess.getSettings().allowSilentJoinQuit()) {
            effectiveMessage = message;
         } else {
            effectiveMessage = message;
         }
      } else {
         if (user.isAuthorized("essentials.silentjoin.vanish")) {
            user.setVanished(true);
         }

         effectiveMessage = null;
      }

      if (effectiveMessage != null && !effectiveMessage.equals(message) || message != null && effectiveMessage == null) {
         joinMessageConsumer.accept(effectiveMessage);
      }

      this.ess.runTaskAsynchronously(() -> this.ess.getServer().getPluginManager().callEvent(new AsyncUserDataLoadEvent(user, effectiveMessage)));
      if (this.ess.getSettings().getMotdDelay() >= 0) {
         int motdDelay = this.ess.getSettings().getMotdDelay() / 50;
         Runnable motdTask = () -> this.motdFlow(user);
         if (motdDelay > 0) {
            this.pendingMotdTasks.put(user.getUUID(), this.ess.scheduleSyncDelayedTask(motdTask, motdDelay));
         } else {
            motdTask.run();
         }
      }

      if (!this.ess.getSettings().isCommandDisabled("mail") && user.isAuthorized("essentials.mail")) {
         if (user.getUnreadMailAmount() == 0) {
            if (this.ess.getSettings().isNotifyNoNewMail()) {
               user.sendTl("noNewMail");
            }
         } else {
            user.notifyOfMail();
         }
      }

      if (user.isAuthorized("essentials.updatecheck")) {
         this.ess.runTaskAsynchronously(() -> {
            for (ComponentHolder component : this.ess.getUpdateChecker().getVersionMessages(false, false, user.getSource())) {
               user.sendComponent(component);
            }
         });
      }

      boolean restoreFly = user.isFlyModeEnabled() && user.isAuthorized("essentials.fly");
      if (restoreFly) {
         user.getBase().setAllowFlight(true);
         if (this.ess.getSettings().isSendFlyEnableOnJoin()) {
            user.sendTl("flyMode", CommonPlaceholders.enableDisable(user.getSource(), true), user.getDisplayName());
         }
      }

      if (user.isAuthorized("essentials.fly.safelogin")) {
         user.getBase().setFallDistance(0.0F);
         boolean inWater = VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_17_R01) && user.getBase().isInWater();
         if (!inWater && LocationUtil.shouldFly(this.ess, user.getLocation())) {
            user.getBase().setAllowFlight(true);
            user.getBase().setFlying(true);
            if (!restoreFly && this.ess.getSettings().isSendFlyEnableOnJoin()) {
               user.sendTl("flyMode", CommonPlaceholders.enableDisable(user.getSource(), true), user.getDisplayName());
            }
         }
      }

      if (!user.isAuthorized("essentials.speed")) {
         user.getBase().setFlySpeed(0.1F);
         user.getBase().setWalkSpeed(0.2F);
      }

      if (user.isSocialSpyEnabled() && !user.isAuthorized("essentials.socialspy")) {
         user.setSocialSpyEnabled(false);
         this.ess.getLogger().log(Level.INFO, "Set socialspy to false for {0} because they had it enabled without permission.", user.getName());
      }

      if (user.isFlyModeEnabled() && !user.isAuthorized("essentials.fly")) {
         user.setFlyModeEnabled(false);
         this.ess.getLogger().log(Level.INFO, "Set fly mode to false for {0} because they had it enabled without permission.", user.getName());
      }

      if (user.isGodModeEnabled() && !user.isAuthorized("essentials.god")) {
         user.setGodModeEnabled(false);
         this.ess.getLogger().log(Level.INFO, "Set god mode to false for {0} because they had it enabled without permission.", user.getName());
      }

      user.setConfirmingClearCommand(null);
      user.getConfirmingPayments().clear();
      user.stopTransaction();
   }

   private void motdFlow(final User user) {
      this.pendingMotdTasks.remove(user.getUUID());
      IText tempInput = null;
      if (!this.ess.getSettings().isCommandDisabled("motd")) {
         try {
            tempInput = new TextInput(user.getSource(), "motd", true, this.ess);
         } catch (IOException var6) {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            } else {
               this.ess.getLogger().log(Level.WARNING, var6.getMessage());
            }
         }
      }

      if (tempInput != null && !tempInput.getLines().isEmpty() && user.isAuthorized("essentials.motd")) {
         IText output = new KeywordReplacer(tempInput, user.getSource(), this.ess);
         TextPager pager = new TextPager(output, true);
         pager.showPage("1", null, "motd", user.getSource());
      }
   }

   private void delayedJoin(final Player player, final String message) {
      if (player.isOnline()) {
         this.ess.getBackup().onPlayerJoin();
         User dUser = this.ess.getUser(player);
         ((ModernUserMap)this.ess.getUsers()).getOnlineUserCache().put(player.getUniqueId(), dUser);
         dUser.update(player);
         dUser.startTransaction();
         if (dUser.isNPC()) {
            dUser.setNPC(false);
         }

         long currentTime = System.currentTimeMillis();
         dUser.checkMuteTimeout(currentTime);
         dUser.updateActivity(false, AfkStatusChangeEvent.Cause.JOIN);
         dUser.stopTransaction();
         this.ess.scheduleSyncDelayedTask(() -> {
            User user = this.ess.getUser(player);
            if (user.getBase().isOnline()) {
               this.joinFlow(user, currentTime, message, msg -> {
                  if (msg != null && !msg.isEmpty()) {
                     this.ess.getServer().broadcastMessage(msg);
                  }
               });
            }
         });
      }
   }

   private void updateCompass(final User user) {
      if (!this.ess.getSettings().isCompassTowardsHomePerm() || user.isAuthorized("essentials.home.compass")) {
         Location loc = user.getHome(user.getLocation());
         if (loc == null) {
            PaperLib.getBedSpawnLocationAsync(user.getBase(), false).thenAccept(location -> {
               if (location != null) {
                  user.getBase().setCompassTarget(location);
               }
            });
         } else {
            user.getBase().setCompassTarget(loc);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerTeleport(final PlayerTeleportEvent event) {
      Player player = event.getPlayer();
      if (!player.hasMetadata("NPC") && (event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.COMMAND)) {
         User user = this.ess.getUser(player);
         if (this.ess.getSettings().registerBackInListener() && user.isAuthorized("essentials.back.onteleport")) {
            user.setLastLocation();
         }

         if (this.ess.getSettings().isTeleportInvulnerability()) {
            user.enableInvulnerabilityAfterTeleport();
         }

         TickCountProvider tickCountProvider = this.ess.provider(TickCountProvider.class);
         if (tickCountProvider != null
            && this.ess.getSettings().isWorldChangePreserveFlying()
            && VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_17_R01)
            && user.isAuthorized("essentials.fly")
            && event.getFrom().getWorld() != event.getTo().getWorld()
            && player.getAllowFlight()) {
            int tick = player.isFlying() ? tickCountProvider.getTickCount() : -tickCountProvider.getTickCount();
            user.setFlightTick(tick);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerEggThrow(final PlayerEggThrowEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      ItemStack stack = new ItemStack(Material.EGG, 1);
      if (user.hasUnlimited(stack)) {
         Inventories.addItem(user.getBase(), stack);
         user.getBase().updateInventory();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      if (user.hasUnlimited(new ItemStack(event.getBucket()))) {
         event.getItemStack().setType(event.getBucket());
         this.ess.scheduleSyncDelayedTask(user.getBase()::updateInventory);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
      String cmd = event.getMessage().split(" ")[0].replace("/", "").toLowerCase(Locale.ENGLISH);
      int argStartIndex = event.getMessage().indexOf(" ");
      String args = argStartIndex == -1 ? "" : event.getMessage().substring(argStartIndex);
      if (this.ess.getServer().getPluginCommand(cmd) == null) {
         Command knownCommand = this.ess.provider(KnownCommandsProvider.class).getKnownCommands().get(cmd);
         if (knownCommand instanceof FormattedCommandAlias) {
            FormattedCommandAlias command = (FormattedCommandAlias)knownCommand;

            for (String fullCommand : this.ess.provider(FormattedCommandAliasProvider.class).createCommands(command, event.getPlayer(), args.split(" "))) {
               this.handlePlayerCommandPreprocess(event, fullCommand);
            }

            return;
         }
      }

      this.handlePlayerCommandPreprocess(event, cmd + args);
   }

   public void handlePlayerCommandPreprocess(final PlayerCommandPreprocessEvent event, final String effectiveCommand) {
      Player player = event.getPlayer();
      String cmd = effectiveCommand.split(" ")[0].replace("/", "").toLowerCase(Locale.ENGLISH);
      PluginCommand pluginCommand = this.ess.getServer().getPluginCommand(cmd);
      if ((this.ess.getSettings().getSocialSpyCommands().contains(cmd) || this.ess.getSettings().getSocialSpyCommands().contains("*"))
         && (pluginCommand == null || !pluginCommand.getName().equals("msg") && !pluginCommand.getName().equals("r"))) {
         User user = this.ess.getUser(player);
         if (!user.isAuthorized("essentials.chat.spy.exempt")) {
            String playerName = this.ess.getSettings().isSocialSpyDisplayNames() ? player.getDisplayName() : player.getName();

            for (User spyer : this.ess.getOnlineUsers()) {
               if (spyer.isSocialSpyEnabled() && !player.equals(spyer.getBase())) {
                  ComponentHolder base = user.isMuted() && this.ess.getSettings().getSocialSpyListenMutedPlayers()
                     ? spyer.tlComponent("socialSpyMutedPrefix")
                     : spyer.tlComponent("socialSpyPrefix");
                  ComponentHolder formatted = this.ess
                     .getAdventureFacet()
                     .deserializeMiniMessage(spyer.playerTl("socialSpyCmdFormat", playerName, event.getMessage()));
                  spyer.sendComponent(this.ess.getAdventureFacet().append(base, formatted));
               }
            }
         }
      }

      User user = this.ess.getUser(player);
      if (!user.isMuted() || !this.ess.getSettings().getMuteCommands().contains(cmd) && !this.ess.getSettings().getMuteCommands().contains("*")) {
         boolean broadcast = true;
         boolean update = true;
         if (pluginCommand != null) {
            String var20 = pluginCommand.getName();
            switch (var20) {
               case "afk":
                  update = false;
               case "vanish":
                  broadcast = false;
            }
         }

         if (update) {
            user.updateActivityOnInteract(broadcast);
         }

         if (this.ess.getSettings().isCommandCooldownsEnabled()
            && !user.isAuthorized("essentials.commandcooldowns.bypass")
            && (pluginCommand == null || !user.isAuthorized("essentials.commandcooldowns.bypass." + pluginCommand.getName()))) {
            int argStartIndex = effectiveCommand.indexOf(" ");
            String args = argStartIndex == -1 ? "" : " " + effectiveCommand.substring(argStartIndex);
            String fullCommand = pluginCommand == null ? effectiveCommand : pluginCommand.getName() + args;
            boolean cooldownFound = false;

            for (Entry<Pattern, Long> entry : user.getCommandCooldowns().entrySet()) {
               if (entry.getValue() <= System.currentTimeMillis()) {
                  user.clearCommandCooldown(entry.getKey());
               } else if (entry.getKey().matcher(fullCommand).matches()) {
                  String commandCooldownTime = DateUtil.formatDateDiff(entry.getValue());
                  user.sendTl("commandCooldown", commandCooldownTime);
                  cooldownFound = true;
                  event.setCancelled(true);
               }
            }

            if (!cooldownFound) {
               Entry<Pattern, Long> cooldownEntry = this.ess.getSettings().getCommandCooldownEntry(fullCommand);
               if (cooldownEntry != null) {
                  if (this.ess.getSettings().isDebug()) {
                     this.ess.getLogger().info("Applying " + cooldownEntry.getValue() + "ms cooldown on /" + fullCommand + " for" + user.getName() + ".");
                  }

                  Date expiry = new Date(System.currentTimeMillis() + cooldownEntry.getValue());
                  user.addCommandCooldown(cooldownEntry.getKey(), expiry, this.ess.getSettings().isCommandCooldownPersistent(fullCommand));
               }
            }
         }
      } else {
         event.setCancelled(true);
         String dateDiff = user.getMuteTimeout() > 0L ? DateUtil.formatDateDiff(user.getMuteTimeout()) : null;
         if (dateDiff == null) {
            if (user.hasMuteReason()) {
               user.sendTl("voiceSilencedReason", user.getMuteReason());
            } else {
               user.sendTl("voiceSilenced");
            }
         } else if (user.hasMuteReason()) {
            user.sendTl("voiceSilencedReasonTime", dateDiff, user.getMuteReason());
         } else {
            user.sendTl("voiceSilencedTime", dateDiff);
         }

         this.ess.getLogger().info(this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("mutedUserSpeaks", player.getName(), event.getMessage())));
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerChangedWorldFlyReset(final PlayerChangedWorldEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      if (this.ess.getSettings().isWorldChangeFlyResetEnabled()
         && user.getBase().getGameMode() != GameMode.CREATIVE
         && user.getBase().getGameMode() != GameMode.SPECTATOR
         && !user.isAuthorized("essentials.fly")) {
         user.getBase().setFallDistance(0.0F);
         user.getBase().setAllowFlight(false);
      }

      if (this.ess.getSettings().isWorldChangeSpeedResetEnabled()) {
         if (!user.isAuthorized("essentials.speed")) {
            user.getBase().setFlySpeed(0.1F);
            user.getBase().setWalkSpeed(0.2F);
         } else {
            if (user.getBase().getFlySpeed() > this.ess.getSettings().getMaxFlySpeed() && !user.isAuthorized("essentials.speed.bypass")) {
               user.getBase().setFlySpeed((float)this.ess.getSettings().getMaxFlySpeed());
            } else {
               user.getBase().setFlySpeed(user.getBase().getFlySpeed() * 0.99999F);
            }

            if (user.getBase().getWalkSpeed() > this.ess.getSettings().getMaxWalkSpeed() && !user.isAuthorized("essentials.speed.bypass")) {
               user.getBase().setWalkSpeed((float)this.ess.getSettings().getMaxWalkSpeed());
            }
         }
      }

      TickCountProvider tickCountProvider = this.ess.provider(TickCountProvider.class);
      int flightTick = user.getFlightTick();
      if (tickCountProvider != null && Math.abs(flightTick) == tickCountProvider.getTickCount() && user.isAuthorized("essentials.fly")) {
         user.getBase().setAllowFlight(true);
         if (flightTick > 0) {
            user.getBase().setFlying(true);
         }
      }

      user.setFlightTick(-1);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
      User user = this.ess.getUser(event.getPlayer());
      String newWorld = event.getPlayer().getLocation().getWorld().getName();
      user.setDisplayNick();
      this.updateCompass(user);
      if (this.ess.getSettings().getNoGodWorlds().contains(newWorld) && user.isGodModeEnabledRaw()) {
         user.sendTl("noGodWorldWarning");
      }

      if (!user.getWorld().getName().equals(newWorld)) {
         user.sendTl("currentWorld", newWorld);
      }

      if (user.isVanished()) {
         user.setVanished(user.isAuthorized("essentials.vanish"));
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerInteract(final PlayerInteractEvent event) {
      boolean updateActivity = true;
      switch (event.getAction()) {
         case RIGHT_CLICK_BLOCK:
            if (!event.isCancelled()
               && MaterialUtil.isBed(event.getClickedBlock().getType())
               && this.ess.getSettings().getUpdateBedAtDaytime()
               && (
                  !VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_13_2_R01)
                     || !((Bed)event.getClickedBlock().getBlockData()).isOccupied()
               )) {
               User player = this.ess.getUser(event.getPlayer());
               if (player.isAuthorized("essentials.sethome.bed") && player.getWorld().getEnvironment().equals(Environment.NORMAL)) {
                  player.getBase().setBedSpawnLocation(event.getClickedBlock().getLocation());
                  if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_15_R01)) {
                     player.sendTl(
                        "bedSet",
                        player.getLocation().getWorld().getName(),
                        player.getLocation().getBlockX(),
                        player.getLocation().getBlockY(),
                        player.getLocation().getBlockZ()
                     );
                  }
               }
            }
            break;
         case LEFT_CLICK_AIR:
            if (event.getPlayer().isFlying()) {
               User user = this.ess.getUser(event.getPlayer());
               if (user.isFlyClickJump()) {
                  this.useFlyClickJump(user);
                  break;
               }
            }
         case LEFT_CLICK_BLOCK:
            if (event.getItem() != null && event.getItem().getType() != Material.AIR) {
               User user = this.ess.getUser(event.getPlayer());
               if (user.hasPowerTools() && user.arePowerToolsEnabled() && this.usePowertools(user, event.getItem().getType())) {
                  event.setCancelled(true);
               }
            }
            break;
         case PHYSICAL:
            updateActivity = false;
      }

      if (updateActivity) {
         this.ess.getUser(event.getPlayer()).updateActivityOnInteract(true);
      }
   }

   private void useFlyClickJump(final User user) {
      try {
         final Location otarget = LocationUtil.getTarget(user.getBase());

         class DelayedClickJumpTask implements Runnable {
            @Override
            public void run() {
               Location loc = user.getLocation();
               loc.setX(otarget.getX());
               loc.setZ(otarget.getZ());

               while (LocationUtil.isBlockDamaging(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ())) {
                  loc.setY(loc.getY() + 1.0);
               }

               PaperLib.teleportAsync(user.getBase(), loc, TeleportCause.PLUGIN);
            }
         }

         this.ess.scheduleSyncDelayedTask(new DelayedClickJumpTask());
      } catch (Exception var3) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.WARNING, var3.getMessage(), (Throwable)var3);
         }
      }
   }

   private boolean usePowertools(final User user, final Material material) {
      List<String> commandList = user.getPowertool(material);
      if (commandList != null && !commandList.isEmpty()) {
         boolean used = false;

         for (final String command : commandList) {
            if (!command.contains("{player}")) {
               if (command.startsWith("c:")) {
                  used = true;
                  user.getBase().chat(command.substring(2));
               } else {
                  used = true;

                  class PowerToolUseTask implements Runnable {
                     @Override
                     public void run() {
                        user.getBase().chat("/" + command);
                        EssentialsPlayerListener.this.ess
                           .getLogger()
                           .log(Level.INFO, String.format("[PT] %s issued server command: /%s", user.getName(), command));
                     }
                  }

                  this.ess.scheduleSyncDelayedTask(new PowerToolUseTask());
               }
            }
         }

         return used;
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onInventoryClickEvent(final InventoryClickEvent event) {
      Player refreshPlayer = null;
      InventoryViewProvider provider = this.ess.provider(InventoryViewProvider.class);
      Inventory top = provider.getTopInventory(event.getView());
      InventoryType type = top.getType();
      Inventory clickedInventory;
      if (event.getRawSlot() < 0) {
         clickedInventory = null;
      } else {
         clickedInventory = event.getRawSlot() < top.getSize() ? top : provider.getBottomInventory(event.getView());
      }

      User user = this.ess.getUser((Player)event.getWhoClicked());
      if (type == InventoryType.PLAYER) {
         InventoryHolder invHolder = top.getHolder();
         if (invHolder instanceof HumanEntity) {
            User invOwner = this.ess.getUser((Player)invHolder);
            if (user.isInvSee()
               && (!user.isAuthorized("essentials.invsee.modify") || invOwner.isAuthorized("essentials.invsee.preventmodify") || !invOwner.getBase().isOnline())
               )
             {
               event.setCancelled(true);
               refreshPlayer = user.getBase();
            }
         }
      } else if (type == InventoryType.ENDER_CHEST) {
         if (user.isEnderSee() && !user.isAuthorized("essentials.enderchest.modify")) {
            event.setCancelled(true);
            refreshPlayer = user.getBase();
         }
      } else if (type == InventoryType.WORKBENCH) {
         if (user.isRecipeSee()) {
            event.setCancelled(true);
            refreshPlayer = user.getBase();
         }
      } else if (type == InventoryType.CHEST && top.getSize() == 9) {
         InventoryHolder invHolder = top.getHolder();
         if (invHolder instanceof HumanEntity && user.isInvSee() && event.getClick() != ClickType.MIDDLE) {
            event.setCancelled(true);
            refreshPlayer = user.getBase();
         }
      } else if (clickedInventory != null
         && clickedInventory.getType() == InventoryType.PLAYER
         && this.ess.getSettings().isDirectHatAllowed()
         && event.getClick() == ClickType.LEFT
         && event.getSlot() == 39
         && event.getCursor().getType() != Material.AIR
         && event.getCursor().getType().getMaxDurability() == 0
         && !MaterialUtil.isSkull(event.getCursor().getType())
         && user.isAuthorized("essentials.hat")
         && !user.isAuthorized("essentials.hat.prevent-type." + event.getCursor().getType().name().toLowerCase())
         && !this.isPreventBindingHat(user, (PlayerInventory)clickedInventory)) {
         event.setCancelled(true);
         PlayerInventory inv = (PlayerInventory)clickedInventory;
         ItemStack head = inv.getHelmet();
         inv.setHelmet(event.getCursor());
         event.setCursor(head);
      }

      if (refreshPlayer != null) {
         this.ess.scheduleSyncDelayedTask(refreshPlayer::updateInventory, 1L);
      }
   }

   private boolean isPreventBindingHat(User user, PlayerInventory inventory) {
      if (!VersionUtil.getServerBukkitVersion().isHigherThan(VersionUtil.v1_9_4_R01)) {
         return false;
      } else {
         ItemStack head = inventory.getHelmet();
         return head != null && head.getEnchantments().containsKey(Enchantment.BINDING_CURSE) && !user.isAuthorized("essentials.hat.ignore-binding");
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onInventoryDragEvent(final InventoryDragEvent event) {
      InventoryViewProvider provider = this.ess.provider(InventoryViewProvider.class);
      Inventory top = provider.getTopInventory(event.getView());
      if (top.getType() == InventoryType.PLAYER) {
         User user = this.ess.getUser((Player)event.getWhoClicked());
         if (user.isInvSee()) {
            for (int slot : event.getNewItems().keySet()) {
               if (Inventories.isBottomInventorySlot(slot)) {
                  event.setCancelled(true);
                  break;
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryCloseEvent(final InventoryCloseEvent event) {
      Player refreshPlayer = null;
      InventoryViewProvider provider = this.ess.provider(InventoryViewProvider.class);
      Inventory top = provider.getTopInventory(event.getView());
      InventoryType type = top.getType();
      if (type == InventoryType.PLAYER) {
         User user = this.ess.getUser((Player)event.getPlayer());
         user.setInvSee(false);
         refreshPlayer = user.getBase();
      } else if (type == InventoryType.ENDER_CHEST) {
         User user = this.ess.getUser((Player)event.getPlayer());
         user.setEnderSee(false);
         refreshPlayer = user.getBase();
      } else if (type == InventoryType.WORKBENCH) {
         User user = this.ess.getUser((Player)event.getPlayer());
         if (user.isRecipeSee()) {
            user.setRecipeSee(false);
            provider.getTopInventory(event.getView()).clear();
            refreshPlayer = user.getBase();
         }
      } else if (type == InventoryType.CHEST && top.getSize() == 9) {
         InventoryHolder invHolder = top.getHolder();
         if (invHolder instanceof HumanEntity) {
            User user = this.ess.getUser((Player)event.getPlayer());
            user.setInvSee(false);
            refreshPlayer = user.getBase();
         }
      }

      if (refreshPlayer != null) {
         this.ess.scheduleSyncDelayedTask(refreshPlayer::updateInventory, 1L);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPlayerFishEvent(final PlayerFishEvent event) {
      if (this.ess.getSettings().cancelAfkOnFish()) {
         User user = this.ess.getUser(event.getPlayer());
         user.updateActivityOnInteract(true);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
      if (this.ess.getSettings().isGamemodeChangePreserveFlying()) {
         User user = this.ess.getUser(event.getPlayer());
         if (user.isAuthorized("essentials.fly")) {
            Player player = event.getPlayer();
            if (player.isFlying() && player.getAllowFlight() && user.isAuthorized("essentials.fly")) {
               this.ess.scheduleSyncDelayedTask(() -> {
                  player.setAllowFlight(true);
                  player.setFlying(true);
               }, 1L);
            }
         }
      }
   }

   private static final class ArrowPickupListener implements Listener {
      private ArrowPickupListener() {
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onArrowPickup(final PlayerPickupArrowEvent event) {
         if (event.getItem().hasMetadata("ess_fireball_proj")) {
            event.setCancelled(true);
         }
      }
   }

   private final class CommandSendFilter implements CommandSendListenerProvider.Filter {
      private CommandSendFilter() {
      }

      public Predicate<String> apply(Player player) {
         EssentialsPlayerListener.this.ess.getPermissionsHandler().invalidatePermissionCache(player.getUniqueId());
         User user = EssentialsPlayerListener.this.ess.getUser(player);
         Set<PluginCommand> checked = new HashSet<>();
         Set<PluginCommand> toRemove = new HashSet<>();
         return label -> {
            if (this.isEssentialsCommand(label)) {
               PluginCommand command = EssentialsPlayerListener.this.ess.getServer().getPluginCommand(label);
               if (!checked.contains(command)) {
                  checked.add(command);
                  if (!user.isAuthorized(command.getName().equals("r") ? "essentials.msg" : "essentials." + command.getName())) {
                     toRemove.add(command);
                  }
               }

               return toRemove.contains(command);
            } else {
               return false;
            }
         };
      }

      private boolean isEssentialsCommand(final String label) {
         PluginCommand command = EssentialsPlayerListener.this.ess.getServer().getPluginCommand(label);
         return command != null
            && (
               command.getPlugin() == EssentialsPlayerListener.this.ess
                  || command.getPlugin().getClass().getName().startsWith("com.earth2me.essentials")
                  || command.getPlugin().getClass().getName().startsWith("net.essentialsx")
            )
            && (
               EssentialsPlayerListener.this.ess.getSettings().isCommandOverridden(label)
                  || EssentialsPlayerListener.this.ess.getAlternativeCommandsHandler().getAlternative(label) == null
            );
      }
   }

   private final class JoinListener1_21 implements Listener {
      private final Map<UUID, String> newUserLocales = new ConcurrentHashMap<>();

      private JoinListener1_21() {
      }

      @EventHandler
      public void onPlayerConfigure(final AsyncPlayerConnectionConfigureEvent event) {
         EssentialsPlayerListener.this.ess.getBackup().onPlayerJoin();
         User dUser = EssentialsPlayerListener.this.ess.getUser(event.getConnection().getProfile().getId());
         String locale = (String)event.getConnection().getClientOption(ClientOption.LOCALE);
         EssentialsPlayerListener.this.ess.getI18n().blockingLoadBundle(I18n.getLocale(locale));
         if (dUser == null) {
            this.newUserLocales.put(event.getConnection().getProfile().getId(), locale);
         } else {
            dUser.getPlayerLocale(locale);
         }
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onPlayerJoin(final PlayerJoinEvent event) {
         User user = EssentialsPlayerListener.this.ess.getUser(event.getPlayer());
         if (user == null) {
            EssentialsPlayerListener.this.legacyJoinFlow(event);
         } else {
            user.update(event.getPlayer());
            String locale = this.newUserLocales.remove(user.getUUID());
            if (locale != null) {
               user.getPlayerLocale(locale);
            }

            long currentTime = System.currentTimeMillis();
            user.startTransaction();
            if (user.isNPC()) {
               user.setNPC(false);
            }

            user.checkMuteTimeout(currentTime);
            user.updateActivity(false, AfkStatusChangeEvent.Cause.JOIN);
            user.stopTransaction();
            EssentialsPlayerListener.this.joinFlow(user, currentTime, event.getJoinMessage(), event::setJoinMessage);
         }
      }
   }

   private final class JoinListenerPre1_21 implements Listener {
      private JoinListenerPre1_21() {
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onPlayerJoin(final PlayerJoinEvent event) {
         EssentialsPlayerListener.this.legacyJoinFlow(event);
      }
   }

   private final class LoginListener1_21 implements Listener {
      private LoginListener1_21() {
      }

      @EventHandler(
         priority = EventPriority.HIGH
      )
      public void onPlayerListFull(final PlayerServerFullCheckEvent event) {
         if (EssentialsPlayerListener.this.ess.getPermissionsHandler().isOfflinePermissionSet(event.getPlayerProfile().getId(), "essentials.joinfullserver")) {
            event.allow(true);
         } else {
            if (!event.isAllowed() && EssentialsPlayerListener.this.ess.getSettings().isCustomServerFullMessage()) {
               PaperAdventureSmuggler.smugglePlayerServerFullCheckEvent(
                  event, EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverFull"))
               );
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onPlayerKickBanned(final AsyncPlayerPreLoginEvent event) {
         if (event.getLoginResult() == Result.KICK_BANNED) {
            BanEntry<?> banEntry = ((ProfileBanList)EssentialsPlayerListener.this.ess.getServer().getBanList(BanListType.PROFILE))
               .getBanEntry(event.getPlayerProfile());
            if (banEntry != null) {
               Date banExpiry = banEntry.getExpiration();
               if (banExpiry != null) {
                  String expiry = DateUtil.formatDateDiff(banExpiry.getTime());
                  event.setKickMessage(
                     EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("tempbanJoin", expiry, banEntry.getReason()))
                  );
               } else {
                  event.setKickMessage(EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("banJoin", banEntry.getReason())));
               }
            } else {
               banEntry = ((IpBanList)EssentialsPlayerListener.this.ess.getServer().getBanList(BanListType.IP)).getBanEntry(event.getAddress());
               if (banEntry != null) {
                  Date banExpiry = banEntry.getExpiration();
                  if (banExpiry != null) {
                     String expiry = DateUtil.formatDateDiff(banExpiry.getTime());
                     event.setKickMessage(
                        EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("tempbanIpJoin", expiry, banEntry.getReason()))
                     );
                  } else {
                     event.setKickMessage(EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("banIpJoin", banEntry.getReason())));
                  }
               }
            }
         } else if (event.getLoginResult() == Result.KICK_WHITELIST) {
            if (EssentialsPlayerListener.this.ess.getPermissionsHandler().isOfflinePermissionSet(event.getUniqueId(), "essentials.whitelist.bypass")) {
               event.allow();
               return;
            }

            if (EssentialsPlayerListener.this.ess.getSettings().isCustomWhitelistMessage()) {
               event.setKickMessage(EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("whitelistKick")));
            }
         }
      }
   }

   private final class LoginListenerPre1_21 implements Listener {
      private LoginListenerPre1_21() {
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onPlayerLoginBanned(final PlayerLoginEvent event) {
         if (event.getResult() == org.bukkit.event.player.PlayerLoginEvent.Result.KICK_BANNED) {
            BanEntry banEntry = EssentialsPlayerListener.this.ess.getServer().getBanList(Type.NAME).getBanEntry(event.getPlayer().getName());
            if (banEntry != null) {
               Date banExpiry = banEntry.getExpiration();
               if (banExpiry != null) {
                  String expiry = DateUtil.formatDateDiff(banExpiry.getTime());
                  event.setKickMessage(
                     EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("tempbanJoin", expiry, banEntry.getReason()))
                  );
               } else {
                  event.setKickMessage(EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("banJoin", banEntry.getReason())));
               }
            } else {
               banEntry = EssentialsPlayerListener.this.ess.getServer().getBanList(Type.IP).getBanEntry(event.getAddress().getHostAddress());
               if (banEntry != null) {
                  Date banExpiry = banEntry.getExpiration();
                  if (banExpiry != null) {
                     String expiry = DateUtil.formatDateDiff(banExpiry.getTime());
                     event.setKickMessage(
                        EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("tempbanIpJoin", expiry, banEntry.getReason()))
                     );
                  } else {
                     event.setKickMessage(EssentialsPlayerListener.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("banIpJoin", banEntry.getReason())));
                  }
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.HIGH
      )
      public void onPlayerLogin(final PlayerLoginEvent event) {
         if (event.getResult() == org.bukkit.event.player.PlayerLoginEvent.Result.KICK_FULL) {
            User kfuser = EssentialsPlayerListener.this.ess.getUser(event.getPlayer());
            kfuser.update(event.getPlayer());
            if (kfuser.isAuthorized("essentials.joinfullserver")) {
               event.allow();
               return;
            }

            if (EssentialsPlayerListener.this.ess.getSettings().isCustomServerFullMessage()) {
               event.disallow(org.bukkit.event.player.PlayerLoginEvent.Result.KICK_FULL, I18n.tlLiteral("serverFull"));
            }
         }
      }
   }

   private final class PickupListener1_12 implements Listener {
      private PickupListener1_12() {
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onPlayerPickupItem(final EntityPickupItemEvent event) {
         if (event.getEntity() instanceof Player) {
            User user = EssentialsPlayerListener.this.ess.getUser((Player)event.getEntity());
            if (EssentialsPlayerListener.this.ess.getSettings().getDisableItemPickupWhileAfk() && user.isAfk()
               || user.isVanished() && !user.isAuthorizedCached("essentials.vanish.pickup")) {
               event.setCancelled(true);
            }
         }
      }
   }

   private final class PickupListenerPre1_12 implements Listener {
      private PickupListenerPre1_12() {
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
         if (event.getItem().hasMetadata("ess_fireball_proj")) {
            event.setCancelled(true);
         } else {
            User user = EssentialsPlayerListener.this.ess.getUser(event.getPlayer());
            if (EssentialsPlayerListener.this.ess.getSettings().getDisableItemPickupWhileAfk() && user.isAfk()
               || user.isVanished() && !user.isAuthorizedCached("essentials.vanish.pickup")) {
               event.setCancelled(true);
            }
         }
      }
   }

   private final class SculkListener1_17 implements Listener {
      private SculkListener1_17() {
      }

      @EventHandler
      public void onGameEvent(final BlockReceiveGameEvent event) {
         if (event.getEntity() instanceof Player && EssentialsPlayerListener.this.ess.getUser((Player)event.getEntity()).isVanished()) {
            event.setCancelled(true);
         }
      }
   }
}
