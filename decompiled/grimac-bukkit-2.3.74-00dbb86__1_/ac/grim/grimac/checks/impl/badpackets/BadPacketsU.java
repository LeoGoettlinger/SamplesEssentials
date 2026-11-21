package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3f;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;

@CheckData(
   name = "BadPacketsU",
   stableKey = "grim.badpackets.invalid_block_placement",
   description = "Sent impossible use item packet"
)
public class BadPacketsU extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("xyz={mcpos}, cursor={cursor}, item={bool}, sequence={sint}");

   public BadPacketsU(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
         WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);
         if (packet.getFace() == BlockFace.OTHER) {
            int expectedY = this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8) ? 4095 : 255;
            boolean failedItemCheck = packet.getItemStack().isPresent()
               && this.isEmpty(packet.getItemStack().get())
               && this.player.getClientVersion().isOlderThan(ClientVersion.V_1_9);
            Vector3i pos = packet.getBlockPosition();
            Vector3f cursor = packet.getCursorPosition();
            if (failedItemCheck
               || pos.x != -1
               || pos.y != expectedY
               || pos.z != -1
               || cursor.x != 0.0F
               || cursor.y != 0.0F
               || cursor.z != 0.0F
               || packet.getSequence() != 0) {
               Verbose.Writer buf = V.write(this.verbose())
                  .mcPos(pos.x, pos.y, pos.z)
                  .cursor(cursor.x, cursor.y, cursor.z)
                  .bool(!failedItemCheck)
                  .sint(packet.getSequence());
               if (this.flag(buf) && this.shouldModifyPackets()) {
                  this.player.onPacketCancel();
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   private boolean isEmpty(ItemStack itemStack) {
      return itemStack.getType() == null || itemStack.getType() == ItemTypes.AIR;
   }
}
