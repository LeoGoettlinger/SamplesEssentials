package me.byteful.plugin.leveltools.api.trigger.impl;

import java.util.EnumSet;
import java.util.Set;
import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.libs.xseries.XMaterial;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;
import org.bukkit.material.NetherWarts;
import org.bukkit.material.CocoaPlant.CocoaPlantSize;
import org.jetbrains.annotations.NotNull;

public final class FarmingTrigger implements Trigger {
   private static final Set<XMaterial> TILLABLE_BLOCKS = EnumSet.of(
      XMaterial.DIRT, XMaterial.GRASS_BLOCK, XMaterial.DIRT_PATH, XMaterial.COARSE_DIRT, XMaterial.ROOTED_DIRT
   );

   @NotNull
   @Override
   public String getTriggerId() {
      return "farming";
   }

   @Override
   public boolean canHandle(@NotNull TriggerContext context) {
      Block block = context.getSourceAs(Block.class);
      if (block == null) {
         return false;
      } else {
         TriggerProfile profile = context.getTriggerProfile();
         Material blockType = block.getType();
         if (context.getOriginalEventAs(PlayerInteractEvent.class) != null) {
            return !TILLABLE_BLOCKS.contains(XMaterial.matchXMaterial(blockType)) ? false : profile.isSourceAllowed("TILL");
         } else if (context.getOriginalEventAs(BlockBreakEvent.class) != null) {
            if (!LevelToolsUtil.supportsBlockData()) {
               MaterialData data = block.getState().getData();
               if (data instanceof Crops) {
                  Crops crop = (Crops)data;
                  if (crop.getState() != CropState.RIPE) {
                     return false;
                  }
               } else if (data instanceof NetherWarts) {
                  NetherWarts wart = (NetherWarts)data;
                  if (wart.getState() != NetherWartsState.RIPE) {
                     return false;
                  }
               } else if (data instanceof CocoaPlant) {
                  CocoaPlant cocoa = (CocoaPlant)data;
                  if (cocoa.getSize() != CocoaPlantSize.LARGE) {
                     return false;
                  }
               }
            } else {
               BlockData data = block.getBlockData();
               if (!(data instanceof Ageable)) {
                  return false;
               }

               Ageable ageable = (Ageable)data;
               if (ageable.getAge() < ageable.getMaximumAge()) {
                  return false;
               }
            }

            return profile.isSourceAllowed(blockType.name());
         } else {
            return false;
         }
      }
   }

   @Override
   public double calculateXpModifier(@NotNull TriggerContext context) {
      Block block = context.getSourceAs(Block.class);
      if (block == null) {
         return 0.0;
      } else {
         TriggerProfile profile = context.getTriggerProfile();
         return context.getOriginalEventAs(PlayerInteractEvent.class) != null
            ? profile.calculateXpModifier("TILL")
            : profile.calculateXpModifier(block.getType().name());
      }
   }
}
