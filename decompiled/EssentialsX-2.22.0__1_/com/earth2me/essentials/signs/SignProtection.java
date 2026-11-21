package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class SignProtection extends EssentialsSign {
   private final transient Set<Material> protectedBlocks = EnumUtil.getAllMatching(Material.class, "CHEST", "FURNACE", "BURNING_FURNACE", "DISPENSER");

   public SignProtection() {
      super("Protection");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      sign.setLine(3, "§4" + username);
      if (this.hasAdjacentBlock(sign.getBlock())) {
         SignProtection.SignProtectionState state = this.isBlockProtected(sign.getBlock(), player, username, true);
         if (state == SignProtection.SignProtectionState.NOSIGN
            || state == SignProtection.SignProtectionState.OWNER
            || player.isAuthorized("essentials.signs.protection.override")) {
            sign.setLine(3, "§1" + username);
            return true;
         }
      }

      player.sendTl("signProtectInvalidLocation");
      return false;
   }

   @Override
   protected boolean onSignBreak(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      SignProtection.SignProtectionState state = this.checkProtectionSign(sign, player, username);
      return state == SignProtection.SignProtectionState.OWNER;
   }

   public boolean hasAdjacentBlock(final Block block, final Block... ignoredBlocks) {
      Block[] faces = this.getAdjacentBlocks(block);

      for (Block b : faces) {
         for (Block ignoredBlock : ignoredBlocks) {
            if (b.getLocation().equals(ignoredBlock.getLocation())) {
            }
         }

         if (this.protectedBlocks.contains(b.getType())) {
            return true;
         }
      }

      return false;
   }

   private void checkIfSignsAreBroken(final Block block, final User player, final String username, final IEssentials ess) throws MaxMoneyException {
      Map<Location, SignProtection.SignProtectionState> signs = this.getConnectedSigns(block, player, username, false);

      for (Entry<Location, SignProtection.SignProtectionState> entry : signs.entrySet()) {
         if (entry.getValue() != SignProtection.SignProtectionState.NOSIGN) {
            Block sign = entry.getKey().getBlock();
            if (!this.hasAdjacentBlock(sign, block)) {
               block.setType(Material.AIR);
               Trade trade = new Trade(new ItemStack(sign.getType(), 1), ess);
               trade.pay(player, Trade.OverflowType.DROP);
            }
         }
      }
   }

   private Map<Location, SignProtection.SignProtectionState> getConnectedSigns(final Block block, final User user, final String username, final boolean secure) {
      Map<Location, SignProtection.SignProtectionState> signs = new HashMap<>();
      this.getConnectedSigns(block, signs, user, username, secure ? 4 : 2);
      return signs;
   }

   private void getConnectedSigns(
      final Block block, final Map<Location, SignProtection.SignProtectionState> signs, final User user, final String username, final int depth
   ) {
      Block[] faces = this.getAdjacentBlocks(block);

      for (Block b : faces) {
         Location loc = b.getLocation();
         if (!signs.containsKey(loc)) {
            SignProtection.SignProtectionState check = this.checkProtectionSign(b, user, username);
            signs.put(loc, check);
            if (this.protectedBlocks.contains(b.getType()) && depth > 0) {
               this.getConnectedSigns(b, signs, user, username, depth - 1);
            }
         }
      }
   }

   private SignProtection.SignProtectionState checkProtectionSign(final Block block, final User user, final String username) {
      if (MaterialUtil.isSign(block.getType())) {
         EssentialsSign.BlockSign sign = new EssentialsSign.BlockSign(block);
         if (sign.getLine(0).equals(this.getSuccessName())) {
            return this.checkProtectionSign(sign, user, username);
         }
      }

      return SignProtection.SignProtectionState.NOSIGN;
   }

   private SignProtection.SignProtectionState checkProtectionSign(final EssentialsSign.ISign sign, final User user, final String username) {
      if (user != null && username != null) {
         if (user.isAuthorized("essentials.signs.protection.override")) {
            return SignProtection.SignProtectionState.OWNER;
         } else if (this.isOwner(user.getEssentials(), user, sign, 3, "§1")) {
            return SignProtection.SignProtectionState.OWNER;
         } else {
            for (int i = 1; i <= 2; i++) {
               String line = sign.getLine(i);
               if (line.startsWith("(") && line.endsWith(")") && user.inGroup(line.substring(1, line.length() - 1))) {
                  return SignProtection.SignProtectionState.ALLOWED;
               }

               if (line.equalsIgnoreCase(username)) {
                  return SignProtection.SignProtectionState.ALLOWED;
               }
            }

            return SignProtection.SignProtectionState.NOT_ALLOWED;
         }
      } else {
         return SignProtection.SignProtectionState.NOT_ALLOWED;
      }
   }

   private Block[] getAdjacentBlocks(final Block block) {
      return new Block[]{
         block.getRelative(BlockFace.NORTH),
         block.getRelative(BlockFace.SOUTH),
         block.getRelative(BlockFace.EAST),
         block.getRelative(BlockFace.WEST),
         block.getRelative(BlockFace.DOWN),
         block.getRelative(BlockFace.UP)
      };
   }

   public SignProtection.SignProtectionState isBlockProtected(final Block block, final User user, final String username, final boolean secure) {
      Map<Location, SignProtection.SignProtectionState> signs = this.getConnectedSigns(block, user, username, secure);
      SignProtection.SignProtectionState retstate = SignProtection.SignProtectionState.NOSIGN;

      for (SignProtection.SignProtectionState state : signs.values()) {
         if (state == SignProtection.SignProtectionState.ALLOWED) {
            retstate = state;
         } else if (state == SignProtection.SignProtectionState.NOT_ALLOWED && retstate != SignProtection.SignProtectionState.ALLOWED) {
            retstate = state;
         }
      }

      if (!secure || retstate == SignProtection.SignProtectionState.NOSIGN) {
         for (SignProtection.SignProtectionState statex : signs.values()) {
            if (statex == SignProtection.SignProtectionState.OWNER) {
               return statex;
            }
         }
      }

      return retstate;
   }

   public boolean isBlockProtected(final Block block) {
      Block[] faces = this.getAdjacentBlocks(block);

      for (Block b : faces) {
         if (MaterialUtil.isSign(b.getType())) {
            Sign sign = (Sign)b.getState();
            if (sign.getLine(0).equalsIgnoreCase("§1[Protection]")) {
               return true;
            }
         }

         if (this.protectedBlocks.contains(b.getType())) {
            Block[] faceChest = this.getAdjacentBlocks(b);

            for (Block a : faceChest) {
               if (MaterialUtil.isSign(a.getType())) {
                  Sign sign = (Sign)a.getState();
                  if (sign.getLine(0).equalsIgnoreCase("§1[Protection]")) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   @Override
   public Set<Material> getBlocks() {
      return this.protectedBlocks;
   }

   @Override
   public boolean areHeavyEventRequired() {
      return true;
   }

   @Override
   protected boolean onBlockPlace(final Block block, final User player, final String username, final IEssentials ess) throws SignException {
      for (Block adjBlock : this.getAdjacentBlocks(block)) {
         SignProtection.SignProtectionState state = this.isBlockProtected(adjBlock, player, username, true);
         if ((state == SignProtection.SignProtectionState.ALLOWED || state == SignProtection.SignProtectionState.NOT_ALLOWED)
            && !player.isAuthorized("essentials.signs.protection.override")) {
            player.sendTl("noPlacePermission", block.getType().toString().toLowerCase(Locale.ENGLISH));
            return false;
         }
      }

      return true;
   }

   @Override
   protected boolean onBlockInteract(final Block block, final User player, final String username, final IEssentials ess) throws SignException {
      SignProtection.SignProtectionState state = this.isBlockProtected(block, player, username, false);
      if (state == SignProtection.SignProtectionState.OWNER
         || state == SignProtection.SignProtectionState.NOSIGN
         || state == SignProtection.SignProtectionState.ALLOWED) {
         return true;
      } else if (state == SignProtection.SignProtectionState.NOT_ALLOWED && player.isAuthorized("essentials.signs.protection.override")) {
         return true;
      } else {
         player.sendTl("noAccessPermission", block.getType().toString().toLowerCase(Locale.ENGLISH));
         return false;
      }
   }

   @Override
   protected boolean onBlockBreak(final Block block, final User player, final String username, final IEssentials ess) throws SignException, MaxMoneyException {
      SignProtection.SignProtectionState state = this.isBlockProtected(block, player, username, false);
      if (state != SignProtection.SignProtectionState.OWNER && state != SignProtection.SignProtectionState.NOSIGN) {
         if ((state == SignProtection.SignProtectionState.ALLOWED || state == SignProtection.SignProtectionState.NOT_ALLOWED)
            && player.isAuthorized("essentials.signs.protection.override")) {
            this.checkIfSignsAreBroken(block, player, username, ess);
            return true;
         } else {
            player.sendTl("noDestroyPermission", block.getType().toString().toLowerCase(Locale.ENGLISH));
            return false;
         }
      } else {
         this.checkIfSignsAreBroken(block, player, username, ess);
         return true;
      }
   }

   @Override
   public boolean onBlockBreak(final Block block, final IEssentials ess) {
      SignProtection.SignProtectionState state = this.isBlockProtected(block, null, null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   @Override
   public boolean onBlockExplode(final Block block, final IEssentials ess) {
      SignProtection.SignProtectionState state = this.isBlockProtected(block, null, null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   @Override
   public boolean onBlockBurn(final Block block, final IEssentials ess) {
      SignProtection.SignProtectionState state = this.isBlockProtected(block, null, null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   @Override
   public boolean onBlockIgnite(final Block block, final IEssentials ess) {
      SignProtection.SignProtectionState state = this.isBlockProtected(block, null, null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   @Override
   public boolean onBlockPush(final Block block, final IEssentials ess) {
      SignProtection.SignProtectionState state = this.isBlockProtected(block, null, null, false);
      return state == SignProtection.SignProtectionState.NOSIGN;
   }

   public static enum SignProtectionState {
      NOT_ALLOWED,
      ALLOWED,
      NOSIGN,
      OWNER;
   }
}
