package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.manager.datastore.PlayerToggleStore;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.UserConnectEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.UserDisconnectEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.UserLoginEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.ConnectionState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.functions.ObjBooleanConsumer;
import java.util.Objects;
import java.util.UUID;

public class PacketPlayerJoinQuit extends PacketListenerAbstract {
   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
         event.getTasksAfterSend().add(() -> {
            GrimAPI.INSTANCE.getPlayerDataManager().addUser(event.getUser());
            UUID uuid = event.getUser().getUUID();
            if (uuid != null) {
               GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore().prefetch(uuid);
            }
         });
      }
   }

   @Override
   public void onUserConnect(UserConnectEvent event) {
      if (event.getUser().getConnectionState() == ConnectionState.PLAY && !GrimAPI.INSTANCE.getPlayerDataManager().isExemptUser(event.getUser())) {
         event.setCancelled(true);
      }
   }

   @Override
   public void onUserLogin(UserLoginEvent event) {
      if (event.getUser() != null) {
         Object nativePlayerObject = Objects.requireNonNull(event.getPlayer());
         PlatformPlayer platformPlayer = GrimAPI.INSTANCE.getPlatformPlayerFactory().getFromNativePlayerType(nativePlayerObject);
         if (GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("debug-pipeline-on-join", false)) {
            LogUtil.info("Pipeline: " + ChannelHelper.pipelineHandlerNamesAsString(event.getUser().getChannel()));
         }

         PlayerToggleStore toggles = GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore();
         applyToggle(
            platformPlayer,
            toggles,
            "alerts",
            "grim.alerts",
            "grim.alerts.enable-on-join",
            "grim.alerts.enable-on-join.silent",
            (p, silent) -> GrimAPI.INSTANCE.getAlertManager().toggleAlerts(p, silent),
            (p, value) -> GrimAPI.INSTANCE.getAlertManager().setAlertsEnabled(p, value, true)
         );
         applyToggle(
            platformPlayer,
            toggles,
            "verbose",
            "grim.verbose",
            "grim.verbose.enable-on-join",
            "grim.verbose.enable-on-join.silent",
            (p, silent) -> GrimAPI.INSTANCE.getAlertManager().toggleVerbose(p, silent),
            (p, value) -> GrimAPI.INSTANCE.getAlertManager().setVerboseEnabled(p, value, true)
         );
         applyToggle(
            platformPlayer,
            toggles,
            "brands",
            "grim.brand",
            "grim.brand.enable-on-join",
            "grim.brand.enable-on-join.silent",
            (p, silent) -> GrimAPI.INSTANCE.getAlertManager().toggleBrands(p, silent),
            (p, value) -> GrimAPI.INSTANCE.getAlertManager().setBrandsEnabled(p, value, true)
         );
         if (platformPlayer.hasPermission("grim.spectate")
            && GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("spectators.hide-regardless", false)) {
            GrimAPI.INSTANCE.getSpectateManager().onLogin(platformPlayer.getUniqueId());
         }

         GrimAPI.INSTANCE.getDataStoreLifecycle().liveWriteHooks().onJoinFromUserLogin(platformPlayer, event.getUser(), System.currentTimeMillis());
      }
   }

   private static void applyToggle(
      @NotNull PlatformPlayer platformPlayer,
      @NotNull PlayerToggleStore toggles,
      @NotNull String key,
      @NotNull String permTogglePath,
      @NotNull String permEnableOnJoin,
      @NotNull String permSilentJoin,
      @NotNull ObjBooleanConsumer<PlatformPlayer> toggle,
      @NotNull ObjBooleanConsumer<PlatformPlayer> applySilent
   ) {
      if (platformPlayer.hasPermission(permTogglePath)) {
         UUID uuid = platformPlayer.getUniqueId();
         Boolean persisted = toggles.current(uuid, key);
         if (persisted != null) {
            applySilent.accept(platformPlayer, persisted);
         } else {
            boolean enableOnJoin = platformPlayer.hasPermission(permEnableOnJoin);
            boolean silent = platformPlayer.hasPermission(permSilentJoin);
            if (enableOnJoin) {
               toggle.accept(platformPlayer, silent);
               toggles.applyPermissionDefault(uuid, key, true);
            } else {
               toggles.applyPermissionDefault(uuid, key, false);
            }
         }
      }
   }

   @Override
   public void onUserDisconnect(UserDisconnectEvent event) {
      GrimAPI.INSTANCE.getPlayerDataManager().onDisconnect(event.getUser());
   }
}
