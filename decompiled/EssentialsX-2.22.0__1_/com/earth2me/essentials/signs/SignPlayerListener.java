package com.earth2me.essentials.signs;

import com.earth2me.essentials.utils.MaterialUtil;
import java.util.logging.Level;
import net.ess3.api.IEssentials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignPlayerListener implements Listener {
   private final transient IEssentials ess;

   public SignPlayerListener(final IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onSignPlayerInteract(final PlayerInteractEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
         Block block;
         if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
            Block targetBlock = null;

            try {
               targetBlock = this.ess.getUser(event.getPlayer()).getTargetBlock(5);
            } catch (IllegalStateException var7) {
               if (this.ess.getSettings().isDebug()) {
                  this.ess.getLogger().log(Level.WARNING, var7.getMessage(), (Throwable)var7);
               }
            }

            block = targetBlock;
         } else {
            block = event.getClickedBlock();
         }

         if (block != null) {
            Material mat = block.getType();
            if (MaterialUtil.isSign(mat)) {
               String csign = ((Sign)block.getState()).getLine(0);

               for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
                  if (csign.equalsIgnoreCase(sign.getSuccessName(this.ess))) {
                     sign.onSignInteract(block, event.getPlayer(), this.ess);
                     event.setCancelled(true);
                     return;
                  }
               }
            } else {
               for (EssentialsSign signx : this.ess.getSettings().enabledSigns()) {
                  if (signx.areHeavyEventRequired()
                     && signx.getBlocks().contains(block.getType())
                     && !signx.onBlockInteract(block, event.getPlayer(), this.ess)) {
                     event.setCancelled(true);
                     return;
                  }
               }
            }
         }
      }
   }
}
