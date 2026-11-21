package io.github.retrooper.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.UserConnectEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.event.simple.PacketConfigReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketConfigSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketLoginSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.TimeStampMode;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketEventsPlugin extends JavaPlugin {
   private int cacheLayer;

   public void onLoad() {
      PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
      PacketEvents.getAPI().load();
   }

   public void onEnable() {
      this.cacheLayer = this.initializeCacheLayer();
      PacketEvents.getAPI().getSettings().debug(false).checkForUpdates(true).timeStampMode(TimeStampMode.MILLIS).reEncodeByDefault(true);
      PacketEvents.getAPI().init();
      new SimplePacketListenerAbstract(PacketListenerPriority.HIGH) {
         @Override
         public void onPacketLoginSend(PacketLoginSendEvent event) {
         }

         @Override
         public void onPacketConfigReceive(PacketConfigReceiveEvent event) {
         }

         @Override
         public void onPacketConfigSend(PacketConfigSendEvent event) {
         }

         @Override
         public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
               WrapperPlayClientInteractEntity interaction = new WrapperPlayClientInteractEntity(event);
               if (interaction.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                  Player player = event.getPlayer();
                  WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(
                     SpigotConversionUtil.fromBukkitLocation(player.getLocation()).getPosition().toVector3i().subtract(0, 1, 0),
                     StateTypes.COAL_BLOCK.createBlockState().getGlobalId()
                  );
                  event.getUser().sendPacket((PacketWrapper<?>)blockChange);
               }
            }
         }

         @Override
         public void onPacketPlaySend(PacketPlaySendEvent event) {
            if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
               WrapperPlayServerBlockChange bc = new WrapperPlayServerBlockChange(event);
               event.<Player>getPlayer().sendMessage("Type: " + bc.getBlockState().getType().getName());
            } else if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
               new WrapperPlayServerSystemChatMessage(event);
            }
         }

         @Override
         public void onUserConnect(UserConnectEvent event) {
            PacketEvents.getAPI().getLogManager().debug("User: (host-name) " + event.getUser().getAddress().getHostString() + " connected...");
         }

         @Override
         public void onUserLogin(UserLoginEvent event) {
            PacketEvents.getAPI().getLogManager().debug("You logged in! User name: " + event.getUser().getProfile().getName());
         }

         @Override
         public void onUserDisconnect(UserDisconnectEvent event) {
            PacketEvents.getAPI().getLogManager().debug("User: (host-name) " + event.getUser().getAddress().getHostString() + " disconnected...");
         }
      };
   }

   public void onDisable() {
      PacketEvents.getAPI().terminate();
   }
}
