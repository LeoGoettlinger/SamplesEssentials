package com.earth2me.essentials.signs;

import com.earth2me.essentials.utils.MaterialUtil;
import net.ess3.api.IEssentials;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SignEntityListener implements Listener {
   private final transient IEssentials ess;

   public SignEntityListener(final IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onSignEntityExplode(final EntityExplodeEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         for (Block block : event.blockList()) {
            if (MaterialUtil.isSign(block.getType()) && EssentialsSign.isValidSign(this.ess, new EssentialsSign.BlockSign(block))
               || EssentialsSign.checkIfBlockBreaksSigns(block)) {
               event.setCancelled(true);
               return;
            }

            for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType())) {
                  event.setCancelled(!sign.onBlockExplode(block, this.ess));
                  return;
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onSignEntityChangeBlock(final EntityChangeBlockEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         Block block = event.getBlock();
         if ((!MaterialUtil.isSign(block.getType()) || !EssentialsSign.isValidSign(this.ess, new EssentialsSign.BlockSign(block)))
            && !EssentialsSign.checkIfBlockBreaksSigns(block)) {
            for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBreak(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }
         } else {
            event.setCancelled(true);
         }
      }
   }
}
