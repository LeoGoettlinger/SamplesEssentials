package ac.grim.grimac.checks.impl.multiactions;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@CheckData(
   name = "MultiActionsC",
   stableKey = "grim.multiactions.inventory_click_while_moving",
   description = "Clicked in inventory while moving"
)
public class MultiActionsC extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("sprinting={bool}, sneaking={bool}, input={bool}");

   public MultiActionsC(GrimPlayer player) {
      super(player);
   }

   @Contract(
      pure = true
   )
   public static boolean isVerboseSprinting(@NotNull GrimPlayer player) {
      return player.isSprinting && (!player.isSwimming || !player.clientClaimsLastOnGround);
   }

   @Contract(
      pure = true
   )
   public static boolean isVerboseSneaking(@NotNull GrimPlayer player) {
      return player.isSneaking && player.getClientVersion().isOlderThan(ClientVersion.V_1_15);
   }

   @Contract(
      pure = true
   )
   public static boolean isVerboseInput(@NotNull GrimPlayer player) {
      return player.supportsEndTick() && player.packetStateData.knownInput.moving();
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
         if (!this.player.serverOpenedInventoryThisTick) {
            boolean sprinting = isVerboseSprinting(this.player);
            boolean sneaking = isVerboseSneaking(this.player);
            boolean input = isVerboseInput(this.player);
            if (sprinting || sneaking || input) {
               if (this.flag(V.write(this.verbose()).bool(sprinting).bool(sneaking).bool(input)) && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            }
         }
      }
   }
}
