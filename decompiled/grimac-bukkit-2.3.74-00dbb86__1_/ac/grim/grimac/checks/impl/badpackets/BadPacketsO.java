package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import java.util.LinkedList;

@CheckData(
   name = "BadPacketsO",
   stableKey = "grim.badpackets.invalid_keepalive",
   description = "Responded with a keepalive ID that was not sent by the server"
)
public class BadPacketsO extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("id={slong}");
   private final LinkedList<Long> keepalives = new LinkedList<>();

   public BadPacketsO(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
         this.keepalives.add(new WrapperPlayServerKeepAlive(event).getId());
      }
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
         long id = new WrapperPlayClientKeepAlive(event).getId();

         for (long keepalive : this.keepalives) {
            if (keepalive == id) {
               Long data;
               do {
                  data = this.keepalives.poll();
               } while (data != null && data != id);

               return;
            }
         }

         if (this.flag(V.write(this.verbose()).slong(id)) && this.shouldModifyPackets()) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }
}
