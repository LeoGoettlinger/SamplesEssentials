package xyz.lychee.lagfixer.hooks;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import xyz.lychee.lagfixer.modules.AFKOptimizerModule;

class PacketEventsImplementation implements PacketEventsBridge, PacketListener {
   private AFKOptimizerModule module;
   private final Map<PacketTypeCommon, Integer> cancelledPackets = Collections.synchronizedMap(new HashMap<>());
   private PacketListenerCommon listener;

   @Override
   public void registerAfk(AFKOptimizerModule module) {
      this.module = module;
      if (this.listener == null) {
         this.listener = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.LOWEST);
      }

      this.reload();
   }

   @Override
   public void unregisterAfk() {
      if (this.listener != null) {
         PacketEvents.getAPI().getEventManager().unregisterListener(this.listener);
         this.listener = null;
      }
   }

   public void reload() {
      this.cancelledPackets.clear();
      this.module.getCancelled_packets().forEach((name, time) -> {
         try {
            Server type = Server.valueOf(name);
            this.cancelledPackets.put(type, time);
         } catch (IllegalArgumentException var4) {
         }
      });
   }

   public void onPacketSend(PacketSendEvent event) {
      Integer time = this.cancelledPackets.get(event.getPacketType());
      if (time != null) {
         AFKOptimizerModule.AfkPlayer afkPlayer = this.module.getAfk_players().get(event.getUser().getUUID());
         if (afkPlayer != null && afkPlayer.getAfkTime().longValue() > time.intValue()) {
            event.setCancelled(true);
         }
      }
   }
}
