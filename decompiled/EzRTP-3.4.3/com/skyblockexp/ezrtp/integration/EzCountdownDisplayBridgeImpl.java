package com.skyblockexp.ezrtp.integration;

import com.skyblockexp.ezcountdown.EzCountdownPlugin;
import com.skyblockexp.ezcountdown.api.EzCountdownApi;
import com.skyblockexp.ezcountdown.api.event.CountdownEndEvent;
import com.skyblockexp.ezcountdown.api.event.CountdownTickEvent;
import com.skyblockexp.ezcountdown.api.model.Countdown;
import com.skyblockexp.ezcountdown.api.model.CountdownBuilder;
import com.skyblockexp.ezcountdown.api.model.CountdownType;
import com.skyblockexp.ezcountdown.display.DisplayType;
import com.skyblockexp.ezrtp.config.EzCountdownIntegrationSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.pvptag.PvpTagService;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

public final class EzCountdownDisplayBridgeImpl implements EzCountdownDisplayBridge, Listener {
   private static final String COUNTDOWN_NAME_PREFIX = "ezrtp-";
   private static final String VISIBILITY_PERM_PREFIX = "ezcountdown.visible.ezrtp.";
   private final Plugin plugin;
   private final EzCountdownApi api;
   private final Map<UUID, EzCountdownDisplayBridgeImpl.PendingRtpCountdown> pending = new HashMap<>();

   public EzCountdownDisplayBridgeImpl(Plugin plugin) {
      this.plugin = plugin;
      this.api = EzCountdownPlugin.getApi();
   }

   @Override
   public boolean startCountdown(
      Player player,
      int seconds,
      EzCountdownIntegrationSettings settings,
      RandomTeleportSettings teleportSettings,
      PvpTagService pvpTagService,
      MessageProvider messageProvider,
      Consumer<Boolean> callback,
      Runnable onComplete
   ) {
      UUID uuid = player.getUniqueId();
      this.cancelCountdownInternal(uuid, player, false);
      String name = "ezrtp-" + uuid;
      String visibilityPerm = "ezcountdown.visible.ezrtp." + uuid;
      PermissionAttachment attachment = player.addAttachment(this.plugin);
      attachment.setPermission(visibilityPerm, true);
      player.recalculatePermissions();
      EnumSet<DisplayType> displayTypes = EnumSet.noneOf(DisplayType.class);

      for (String typeName : settings.getDisplayTypes()) {
         try {
            displayTypes.add(DisplayType.valueOf(typeName));
         } catch (IllegalArgumentException var17) {
            this.plugin.getLogger().warning("Unknown EzCountdown display type '" + typeName + "' — skipping.");
         }
      }

      if (displayTypes.isEmpty()) {
         displayTypes.add(DisplayType.ACTION_BAR);
      }

      Countdown countdown = CountdownBuilder.builder(name)
         .ephemeral(true)
         .type(CountdownType.DURATION)
         .durationSeconds(seconds)
         .displayTypes(displayTypes)
         .formatMessage(settings.getFormatMessage())
         .visibilityPermission(visibilityPerm)
         .build();
      boolean created = this.api.createCountdown(countdown);
      if (!created) {
         attachment.remove();
         player.recalculatePermissions();
         this.plugin.getLogger().log(Level.WARNING, "EzCountdown rejected createCountdown for player " + player.getName());
         return false;
      } else {
         this.api.startCountdown(name);
         Location startLocation = player.getLocation().clone();
         this.pending
            .put(
               uuid,
               new EzCountdownDisplayBridgeImpl.PendingRtpCountdown(
                  uuid, startLocation, callback, onComplete, teleportSettings, pvpTagService, messageProvider, attachment, new boolean[]{false}
               )
            );
         return true;
      }
   }

   @Override
   public void cancelCountdown(UUID playerUuid, Player player) {
      this.cancelCountdownInternal(playerUuid, player, true);
   }

   @Override
   public void shutdown() {
      for (EzCountdownDisplayBridgeImpl.PendingRtpCountdown p : this.pending.values()) {
         cleanupPermission(p.attachment(), null);
         String name = "ezrtp-" + p.playerUuid();
         this.api.stopCountdown(name);
         this.api.deleteCountdown(name);
      }

      this.pending.clear();
   }

   @EventHandler
   public void onCountdownTick(CountdownTickEvent event) {
      EzCountdownDisplayBridgeImpl.PendingRtpCountdown p = this.pendingByCountdownName(event.getCountdown().getName());
      if (p != null) {
         Player player = Bukkit.getPlayer(p.playerUuid());
         if (player != null && player.isOnline()) {
            RandomTeleportSettings settings = p.teleportSettings();
            if (settings.isCancelOnMove()) {
               double movedDistSq = player.getLocation().distanceSquared(p.startLocation());
               double cancelDistSq = settings.getCancelDistance() * settings.getCancelDistance();
               double warnDistSq = settings.getWarnDistance() * settings.getWarnDistance();
               if (movedDistSq >= cancelDistSq) {
                  String name = event.getCountdown().getName();
                  this.pending.remove(p.playerUuid());
                  cleanupPermission(p.attachment(), player);
                  this.api.stopCountdown(name);
                  this.api.deleteCountdown(name);
                  if (!settings.isSuppressPlayerMessages()) {
                     MessageUtil.send(player, p.messageProvider().format(MessageKey.COUNTDOWN_MOVE_CANCEL, player));
                  }

                  if (p.callback() != null) {
                     p.callback().accept(false);
                  }

                  return;
               }

               if (!p.warningSent()[0] && warnDistSq > 0.0 && movedDistSq >= warnDistSq) {
                  p.warningSent()[0] = true;
                  if (!settings.isSuppressPlayerMessages()) {
                     MessageUtil.send(player, p.messageProvider().format(MessageKey.COUNTDOWN_MOVE_WARN, player));
                  }
               }
            }

            if (p.pvpTagService() != null && settings.getPvpTagIntegrationSettings().isCancelCountdownOnPvpTag() && p.pvpTagService().isInCombat(player)) {
               String namex = event.getCountdown().getName();
               this.pending.remove(p.playerUuid());
               cleanupPermission(p.attachment(), player);
               this.api.stopCountdown(namex);
               this.api.deleteCountdown(namex);
               if (!settings.isSuppressPlayerMessages()) {
                  MessageUtil.send(player, p.messageProvider().format(MessageKey.COUNTDOWN_PVP_CANCEL, player));
               }

               if (p.callback() != null) {
                  p.callback().accept(false);
               }
            }
         } else {
            this.pending.remove(p.playerUuid());
         }
      }
   }

   @EventHandler
   public void onCountdownEnd(CountdownEndEvent event) {
      EzCountdownDisplayBridgeImpl.PendingRtpCountdown p = this.pendingByCountdownName(event.getCountdown().getName());
      if (p != null) {
         this.pending.remove(p.playerUuid());
         Player player = Bukkit.getPlayer(p.playerUuid());
         cleanupPermission(p.attachment(), player);
         p.onComplete().run();
      }
   }

   private void cancelCountdownInternal(UUID playerUuid, Player player, boolean fireCallback) {
      EzCountdownDisplayBridgeImpl.PendingRtpCountdown p = this.pending.remove(playerUuid);
      if (p != null) {
         cleanupPermission(p.attachment(), player);
         String name = "ezrtp-" + playerUuid;
         this.api.stopCountdown(name);
         this.api.deleteCountdown(name);
         if (fireCallback && p.callback() != null) {
            p.callback().accept(false);
         }
      }
   }

   private EzCountdownDisplayBridgeImpl.PendingRtpCountdown pendingByCountdownName(String name) {
      if (!name.startsWith("ezrtp-")) {
         return null;
      } else {
         String uuidStr = name.substring("ezrtp-".length());

         UUID uuid;
         try {
            uuid = UUID.fromString(uuidStr);
         } catch (IllegalArgumentException var5) {
            return null;
         }

         return this.pending.get(uuid);
      }
   }

   private static void cleanupPermission(PermissionAttachment attachment, Player player) {
      try {
         attachment.remove();
      } catch (Throwable var4) {
      }

      if (player != null) {
         try {
            player.recalculatePermissions();
         } catch (Throwable var3) {
         }
      }
   }

   private record PendingRtpCountdown(
      UUID playerUuid,
      Location startLocation,
      Consumer<Boolean> callback,
      Runnable onComplete,
      RandomTeleportSettings teleportSettings,
      PvpTagService pvpTagService,
      MessageProvider messageProvider,
      PermissionAttachment attachment,
      boolean[] warningSent
   ) {
   }
}
