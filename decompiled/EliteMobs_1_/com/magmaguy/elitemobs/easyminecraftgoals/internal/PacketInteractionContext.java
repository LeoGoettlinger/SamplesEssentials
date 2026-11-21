package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.Objects;
import org.bukkit.inventory.EquipmentSlot;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PacketInteractionContext(PacketInteractionContext.Action action, @Nullable EquipmentSlot hand) {
   public PacketInteractionContext(PacketInteractionContext.Action action, @Nullable EquipmentSlot hand) {
      Objects.requireNonNull(action, "action");
      if (action == PacketInteractionContext.Action.ATTACK) {
         if (hand != null) {
            throw new IllegalArgumentException("Attack packets do not carry a hand");
         }
      } else if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
         throw new IllegalArgumentException("Interaction packets require a main or off hand");
      }

      this.action = action;
      this.hand = hand;
   }

   public static PacketInteractionContext attack() {
      return new PacketInteractionContext(PacketInteractionContext.Action.ATTACK, null);
   }

   public static PacketInteractionContext interact(EquipmentSlot hand) {
      return new PacketInteractionContext(PacketInteractionContext.Action.INTERACT, hand);
   }

   public static PacketInteractionContext interactAt(EquipmentSlot hand) {
      return new PacketInteractionContext(PacketInteractionContext.Action.INTERACT_AT, hand);
   }

   public boolean isAttack() {
      return this.action == PacketInteractionContext.Action.ATTACK;
   }

   public static enum Action {
      ATTACK,
      INTERACT,
      INTERACT_AT;
   }
}
