package com.earth2me.essentials.signs;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import java.util.logging.Level;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignBlockListener implements Listener {
   private final transient IEssentials ess;

   public SignBlockListener(final IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSignBlockBreak(final BlockBreakEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         try {
            if (this.protectSignsAndBlocks(event.getBlock(), event.getPlayer())) {
               event.setCancelled(true);
            }
         } catch (MaxMoneyException var3) {
            event.setCancelled(true);
         }
      }
   }

   public boolean protectSignsAndBlocks(final Block block, final Player player) throws MaxMoneyException {
      if (EssentialsSign.checkIfBlockBreaksSigns(block)) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().log(Level.INFO, "Prevented that a block was broken next to a sign.");
         }

         return true;
      } else {
         Material mat = block.getType();
         if (MaterialUtil.isSign(mat)) {
            Sign csign = (Sign)block.getState();

            for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (csign.getLine(0).equalsIgnoreCase(sign.getSuccessName(this.ess)) && !sign.onSignBreak(block, player, this.ess)) {
                  return true;
               }
            }
         }

         for (EssentialsSign signx : this.ess.getSettings().enabledSigns()) {
            if (signx.areHeavyEventRequired() && signx.getBlocks().contains(block.getType()) && !signx.onBlockBreak(block, player, this.ess)) {
               this.ess.getLogger().log(Level.INFO, "A block was protected by a sign.");
               return true;
            }
         }

         return false;
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSignSignChange2(final SignChangeEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         User user = this.ess.getUser(event.getPlayer());

         for (int i = 0; i < 4; i++) {
            event.setLine(i, FormatUtil.formatString(user, "essentials.signs", event.getLine(i)));
         }

         String lColorlessTopLine = ChatColor.stripColor(event.getLine(0)).toLowerCase().trim();
         if (!lColorlessTopLine.isEmpty()) {
            for (Signs signs : Signs.values()) {
               EssentialsSign sign = signs.getSign();
               String successName = sign.getSuccessName(this.ess);
               if (successName == null) {
                  user.sendTl("errorWithMessage", "Please report this error to a staff member.");
                  return;
               }

               String lSuccessName = ChatColor.stripColor(successName.toLowerCase());
               if (lColorlessTopLine.contains(lSuccessName)
                  && (this.ess.getSettings().enabledSigns().contains(sign) || !this.ess.getSettings().getUnprotectedSignNames().contains(sign))) {
                  event.setLine(0, lColorlessTopLine);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSignSignChange(final SignChangeEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
            if (event.getLine(0).equalsIgnoreCase(sign.getSuccessName(this.ess))) {
               event.setCancelled(true);
               return;
            }

            if (event.getLine(0).equalsIgnoreCase(sign.getTemplateName()) && !sign.onSignCreate(event, this.ess)) {
               event.setCancelled(true);
               return;
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onSignBlockPlace(final BlockPlaceEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         Block against = event.getBlockAgainst();
         if (MaterialUtil.isSign(against.getType()) && EssentialsSign.isValidSign(this.ess, new EssentialsSign.BlockSign(against))) {
            event.setCancelled(true);
         } else {
            Block block = event.getBlock();
            if (!MaterialUtil.isSign(block.getType())) {
               for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
                  if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPlace(block, event.getPlayer(), this.ess)) {
                     event.setCancelled(true);
                     return;
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onSignBlockBurn(final BlockBurnEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         Block block = event.getBlock();
         if ((!MaterialUtil.isSign(block.getType()) || !EssentialsSign.isValidSign(this.ess, new EssentialsSign.BlockSign(block)))
            && !EssentialsSign.checkIfBlockBreaksSigns(block)) {
            for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockBurn(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }
         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onSignBlockIgnite(final BlockIgniteEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         Block block = event.getBlock();
         if ((!MaterialUtil.isSign(block.getType()) || !EssentialsSign.isValidSign(this.ess, new EssentialsSign.BlockSign(block)))
            && !EssentialsSign.checkIfBlockBreaksSigns(block)) {
            for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockIgnite(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }
         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onSignBlockPistonExtend(final BlockPistonExtendEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         for (Block block : event.getBlocks()) {
            if (MaterialUtil.isSign(block.getType()) && EssentialsSign.isValidSign(this.ess, new EssentialsSign.BlockSign(block))
               || EssentialsSign.checkIfBlockBreaksSigns(block)) {
               event.setCancelled(true);
               return;
            }

            for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
               if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPush(block, this.ess)) {
                  event.setCancelled(true);
                  return;
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onSignBlockPistonRetract(final BlockPistonRetractEvent event) {
      if (this.ess.getSettings().areSignsDisabled()) {
         event.getHandlers().unregister(this);
      } else {
         if (event.isSticky()) {
            Block pistonBaseBlock = event.getBlock();
            Block[] affectedBlocks = new Block[]{pistonBaseBlock, pistonBaseBlock.getRelative(event.getDirection()), event.getRetractLocation().getBlock()};

            for (Block block : affectedBlocks) {
               if (MaterialUtil.isSign(block.getType()) && EssentialsSign.isValidSign(this.ess, new EssentialsSign.BlockSign(block))
                  || EssentialsSign.checkIfBlockBreaksSigns(block)) {
                  event.setCancelled(true);
                  return;
               }

               for (EssentialsSign sign : this.ess.getSettings().enabledSigns()) {
                  if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()) && !sign.onBlockPush(block, this.ess)) {
                     event.setCancelled(true);
                     return;
                  }
               }
            }
         }
      }
   }
}
