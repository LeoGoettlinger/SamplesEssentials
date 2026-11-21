package ac.grim.grimac.checks.impl.chat;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;

@CheckData(
   name = "ChatA",
   stableKey = "grim.exploit.blank_tab_complete",
   description = "Sent a tab complete packet with no command or input text",
   experimental = true
)
public class ChatA extends Check implements PacketCheck {
   public ChatA(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE
         && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
         WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);
         String text = wrapper.getText();
         if ((text.equals("/") || text.trim().isEmpty()) && this.flag("")) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }
}
