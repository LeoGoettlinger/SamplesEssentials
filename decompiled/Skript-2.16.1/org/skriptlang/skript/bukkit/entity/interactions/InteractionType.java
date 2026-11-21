package org.skriptlang.skript.bukkit.entity.interactions;

import org.bukkit.entity.Interaction;
import org.bukkit.entity.Interaction.PreviousInteraction;
import org.jetbrains.annotations.Nullable;

public enum InteractionType {
   ATTACK,
   INTERACT,
   BOTH;

   @Nullable
   public static PreviousInteraction getLatest(Interaction interaction) {
      PreviousInteraction attack = interaction.getLastAttack();
      PreviousInteraction interact = interaction.getLastInteraction();
      if (attack == null) {
         return interact;
      } else if (interact == null) {
         return attack;
      } else {
         return attack.getTimestamp() > interact.getTimestamp() ? attack : interact;
      }
   }
}
