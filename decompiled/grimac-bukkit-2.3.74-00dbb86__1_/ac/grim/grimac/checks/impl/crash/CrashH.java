package ac.grim.grimac.checks.impl.crash;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;

@CheckData(
   name = "CrashH",
   stableKey = "grim.crash.invalid_tab_complete",
   description = "Sent a tab complete request with invalid or excessive length"
)
public class CrashH extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("[(length)|(invalid)] length={sint}");

   public CrashH(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
         WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);
         String text = wrapper.getText();
         int length = text.length();
         if (length > (!this.player.canUseGameMasterBlocks() ? 256 : 32500)) {
            if (this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }

            this.flag(V.write(this.verbose()).bool(true).sint(length));
            return;
         }

         int index;
         if (length > 64 && ((index = text.indexOf(32)) == -1 || index >= 64)) {
            if (this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }

            this.flag(V.write(this.verbose()).bool(false).sint(length));
         }
      }
   }
}
