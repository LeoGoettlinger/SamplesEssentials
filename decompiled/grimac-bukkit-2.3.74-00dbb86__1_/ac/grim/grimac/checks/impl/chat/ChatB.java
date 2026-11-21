package ac.grim.grimac.checks.impl.chat;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;

@CheckData(
   name = "ChatB",
   stableKey = "grim.exploit.spigot_antispam_bypass",
   description = "Invalid chat message"
)
public class ChatB extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("[message|command]={str}");

   public ChatB(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
         String message = new WrapperPlayClientChatMessage(event).getMessage();
         if ((
               message.isEmpty()
                  || !message.trim().equals(message)
                  || message.startsWith("/") && this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19)
            )
            && this.flag(V.write(this.verbose()).bool(true).str(message))
            && this.shouldModifyPackets()) {
            this.player.onPacketCancel();
            event.setCancelled(true);
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND_UNSIGNED) {
         String command = "/" + new WrapperPlayClientChatCommandUnsigned(event).getCommand();
         if (!command.stripTrailing().equals(command) && this.flag(V.write(this.verbose()).bool(false).str(command))) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND) {
         String command = "/" + new WrapperPlayClientChatCommand(event).getCommand();
         if (!command.trim().equals(command) && this.flag(V.write(this.verbose()).bool(false).str(command))) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }
}
