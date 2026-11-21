package ac.grim.grimac.checks.impl.crash;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import ac.grim.grimac.utils.inventory.inventory.MenuType;

@CheckData(
   name = "CrashD",
   stableKey = "grim.crash.lectern",
   description = "Clicking slots in lectern window"
)
public class CrashD extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("clickType={clicktype}, button={sint}");
   private MenuType type = MenuType.UNKNOWN;
   private int lecternId = -1;

   public CrashD(GrimPlayer playerData) {
      super(playerData);
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW && this.isSupportedVersion()) {
         WrapperPlayServerOpenWindow window = new WrapperPlayServerOpenWindow(event);
         this.type = MenuType.getMenuType(window.getType());
         if (this.type == MenuType.LECTERN) {
            this.lecternId = window.getContainerId();
         }
      }
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW && this.isSupportedVersion()) {
         WrapperPlayClientClickWindow click = new WrapperPlayClientClickWindow(event);
         int clickType = VerboseCodecs.enumId(click.getWindowClickType());
         int button = click.getButton();
         int windowId = click.getWindowId();
         if (this.type == MenuType.LECTERN && windowId > 0 && windowId == this.lecternId && this.flag(V.write(this.verbose()).uint(clickType).sint(button))) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }

   private boolean isSupportedVersion() {
      return PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_14);
   }
}
