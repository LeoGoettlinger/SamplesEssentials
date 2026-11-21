package ac.grim.grimac.manager;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import ac.grim.grimac.utils.math.GrimMath;

public class AttackCooldownHandler extends Check implements PacketCheck {
   private int ticksSinceLastSwing;
   private ItemStack stack = ItemStack.EMPTY;
   private boolean stackChanged;

   public AttackCooldownHandler(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
         this.reset();
      }

      if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
         WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
         if (packet.getAction() == DiggingAction.CANCELLED_DIGGING && packet.getBlockFace() == BlockFace.DOWN) {
            this.reset();
         }
      }

      if (this.isTickPacket(event.getPacketType())) {
         if (!this.stackChanged) {
            this.ticksSinceLastSwing++;
         }

         this.updateHeldItem();
         this.stackChanged = false;
      }
   }

   public void reset() {
      this.ticksSinceLastSwing = 0;
   }

   public void updateHeldItem() {
      ItemStack held = this.player.inventory.getHeldItem().copy();
      if ((!this.stack.isEmpty() || !held.isEmpty())
         && (this.stack.getType() != held.getType() || !this.stack.isDamageableItem() && this.stack.getLegacyData() != held.getLegacyData())) {
         this.reset();
         this.stackChanged = true;
      }

      this.stack = held;
   }

   public float getMinimumProgress() {
      return GrimMath.clamp(
         (this.ticksSinceLastSwing + 0.5F) / (float)(1.0 / this.player.compensatedEntities.self.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0), 0.0F, 1.0F
      );
   }
}
