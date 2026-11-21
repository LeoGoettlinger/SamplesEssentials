package ac.grim.grimac.checks.impl.crash;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

@CheckData(
   name = "CrashF",
   stableKey = "grim.crash.button_crash",
   description = "Sent an inventory click with an invalid button or slot value"
)
public class CrashF extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("clickType={clicktype}, button={sint}[, slot={sint}]");

   public CrashF(GrimPlayer playerData) {
      super(playerData);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
         WrapperPlayClientClickWindow click = new WrapperPlayClientClickWindow(event);
         WrapperPlayClientClickWindow.WindowClickType clickType = click.getWindowClickType();
         int button = click.getButton();
         int windowId = click.getWindowId();
         int slot = click.getSlot();
         if ((clickType == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE || clickType == WrapperPlayClientClickWindow.WindowClickType.SWAP)
            && windowId >= 0
            && button < 0) {
            int clickTypeId = VerboseCodecs.enumId(clickType);
            if (this.flag(V.write(this.verbose()).uint(clickTypeId).sint(button).bool(false).sint(0))) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else if (windowId >= 0 && clickType == WrapperPlayClientClickWindow.WindowClickType.SWAP && slot < 0) {
            int clickTypeId = VerboseCodecs.enumId(clickType);
            if (this.flag(V.write(this.verbose()).uint(clickTypeId).sint(button).bool(true).sint(slot))) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         }
      }
   }
}
