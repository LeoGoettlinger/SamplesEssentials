package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.IEssentials;
import net.ess3.api.TranslatableException;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class SignWarp extends EssentialsSign {
   public SignWarp() {
      super("Warp");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      this.validateTrade(sign, 3, ess);
      String warpName = sign.getLine(1);
      if (warpName.isEmpty()) {
         sign.setLine(1, "§c<Warp name>");
         throw new SignException("invalidSignLine", 1);
      } else {
         try {
            ess.getWarps().getWarp(warpName);
         } catch (Exception var8) {
            if (var8 instanceof TranslatableException) {
               TranslatableException te = (TranslatableException)var8;
               throw new SignException(var8, te.getTlKey(), te.getArgs());
            }

            throw new SignException(var8, "errorWithMessage", var8.getMessage());
         }

         String group = sign.getLine(2);
         if ("Everyone".equalsIgnoreCase(group) || "Everybody".equalsIgnoreCase(group)) {
            sign.setLine(2, "§2Everyone");
         }

         return true;
      }
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      String warpName = sign.getLine(1);
      String group = sign.getLine(2);
      if (!group.isEmpty()) {
         if (!"§2Everyone".equals(group) && !player.inGroup(group)) {
            throw new SignException("warpUsePermission");
         }
      } else if (ess.getSettings().getPerWarpPermission() && !player.isAuthorized("essentials.warps." + warpName)) {
         throw new SignException("warpUsePermission");
      }

      Trade charge = this.getTrade(sign, 3, ess);
      CompletableFuture<Boolean> future = new CompletableFuture<>();
      player.getAsyncTeleport().warp(player, warpName, charge, TeleportCause.PLUGIN, future);
      future.thenAccept(success -> {
         if (success) {
            Trade.log("Sign", "Warp", "Interact", username, null, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
         }
      });
      future.exceptionally(e -> {
         ess.showError(player.getSource(), e, "\\ sign: " + this.signName);
         return false;
      });
      return true;
   }
}
