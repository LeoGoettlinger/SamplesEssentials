package me.byteful.plugin.leveltools.api.trigger.impl;

import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public final class BlockBreakTrigger implements Trigger {
   @NotNull
   @Override
   public String getTriggerId() {
      return "block_break";
   }

   @Override
   public boolean canHandle(@NotNull TriggerContext context) {
      Block block = context.getSourceAs(Block.class);
      if (block == null) {
         return false;
      } else {
         TriggerProfile profile = context.getTriggerProfile();
         String blockType = block.getType().name();
         return profile.isSourceAllowed(blockType);
      }
   }

   @Override
   public double calculateXpModifier(@NotNull TriggerContext context) {
      Block block = context.getSourceAs(Block.class);
      if (block == null) {
         return 0.0;
      } else {
         TriggerProfile profile = context.getTriggerProfile();
         return profile.calculateXpModifier(block.getType().name());
      }
   }
}
