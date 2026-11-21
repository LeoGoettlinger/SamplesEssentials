package ac.grim.grimac.checks.impl.multiactions;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(
   name = "MultiActionsD",
   stableKey = "grim.multiactions.inventory_close_while_moving",
   description = "Closed inventory while moving"
)
public class MultiActionsD extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("sprinting={bool}, sneaking={bool}, input={bool}");

   public MultiActionsD(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
         if (!this.player.serverOpenedInventoryThisTick) {
            boolean sprinting = MultiActionsC.isVerboseSprinting(this.player);
            boolean sneaking = MultiActionsC.isVerboseSneaking(this.player);
            boolean input = MultiActionsC.isVerboseInput(this.player);
            if (sprinting || sneaking || input) {
               if (!this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_12_2) || !this.player.isInNetherPortal) {
                  this.flag(V.write(this.verbose()).bool(sprinting).bool(sneaking).bool(input));
               }
            }
         }
      }
   }
}
