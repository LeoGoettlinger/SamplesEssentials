package ac.grim.grimac.utils.anticheat;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.event.events.GrimJoinEvent;
import ac.grim.grimac.api.event.events.GrimQuitEvent;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.utils.reflection.GeyserUtil;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
   private final Set<User> exemptUsers = ConcurrentHashMap.newKeySet();
   private final ConcurrentHashMap<User, GrimPlayer> playerDataMap = new ConcurrentHashMap<>();

   public boolean isExemptUser(@Nullable User user) {
      return user != null && this.exemptUsers.contains(user);
   }

   public void exemptUser(@Nullable User user) {
      if (user != null) {
         this.exemptUsers.add(user);
      }
   }

   public boolean clearExemptions(@Nullable User user) {
      return user == null ? false : this.exemptUsers.remove(user);
   }

   @Nullable
   public GrimPlayer getPlayer(@NotNull UUID uuid) {
      Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(uuid);
      if (channel == null) {
         return null;
      } else {
         User user = PacketEvents.getAPI().getProtocolManager().getUser(channel);
         return user == null ? null : this.getPlayer(user);
      }
   }

   @Nullable
   public GrimPlayer getPlayer(@NotNull User user) {
      GrimPlayer player = this.playerDataMap.get(user);
      return player != null && player.platformPlayer != null && player.platformPlayer.isExternalPlayer() ? null : player;
   }

   public boolean shouldCheck(@NotNull User user) {
      if (this.isExemptUser(user)) {
         return false;
      } else if (!ChannelHelper.isOpen(user.getChannel())) {
         return false;
      } else {
         if (user.getUUID() != null) {
            if (GeyserUtil.isBedrockPlayer(user.getUUID())) {
               this.exemptUser(user);
               return false;
            }

            GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(user);
            if (grimPlayer != null && grimPlayer.hasPermission("grim.exempt")) {
               this.exemptUser(user);
               return false;
            }

            if (user.getUUID().toString().startsWith("00000000-0000-0000-0009")) {
               this.exemptUser(user);
               return false;
            }
         }

         return true;
      }
   }

   public void addUser(@NotNull User user) {
      if (this.shouldCheck(user)) {
         GrimPlayer player = new GrimPlayer(user);
         this.playerDataMap.put(user, player);
         PlayerDataManager.Channels.JOIN.fire(player);
      }
   }

   public GrimPlayer remove(@NotNull User user) {
      return this.playerDataMap.remove(user);
   }

   public void onDisconnect(User user) {
      GrimPlayer grimPlayer = this.remove(user);
      if (grimPlayer != null) {
         PlayerDataManager.Channels.QUIT.fire(grimPlayer);
      }

      this.clearExemptions(user);
      UUID uuid = user.getProfile().getUUID();
      GrimAPI.INSTANCE.getDataStoreLifecycle().liveWriteHooks().onQuitFromUserDisconnect(user, grimPlayer, System.currentTimeMillis());
      if (uuid != null) {
         GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore().evict(uuid);
      }

      if (uuid != null) {
         GrimAPI.INSTANCE.getAlertManager().handlePlayerQuit(GrimAPI.INSTANCE.getPlatformPlayerFactory().getFromUUID(uuid));
         GrimAPI.INSTANCE.getSpectateManager().onQuit(uuid);
         GrimAPI.INSTANCE.getPlatformPlayerFactory().invalidatePlayer(uuid);
      }
   }

   public Collection<GrimPlayer> getEntries() {
      return this.playerDataMap.values();
   }

   public int size() {
      return this.playerDataMap.size();
   }

   private static final class Channels {
      static final GrimJoinEvent.Channel JOIN = GrimAPI.INSTANCE.getEventBus().get(GrimJoinEvent.class);
      static final GrimQuitEvent.Channel QUIT = GrimAPI.INSTANCE.getEventBus().get(GrimQuitEvent.class);
   }
}
