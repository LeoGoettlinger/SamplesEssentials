package com.earth2me.essentials;

import com.earth2me.essentials.api.IAsyncTeleport;
import com.earth2me.essentials.commands.WarpNotFoundException;
import com.earth2me.essentials.paperlib.PaperLib;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.LocationUtil;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.UserWarpEvent;
import net.ess3.api.events.teleport.PreTeleportEvent;
import net.ess3.api.events.teleport.TeleportWarmupEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class AsyncTeleport implements IAsyncTeleport {
   private final net.ess3.api.IUser teleportOwner;
   private final net.ess3.api.IEssentials ess;
   private AsyncTimedTeleport timedTeleport;
   private AsyncTeleport.TeleportType tpType;

   public AsyncTeleport(final net.ess3.api.IUser user, final net.ess3.api.IEssentials ess) {
      this.teleportOwner = user;
      this.ess = ess;
      this.tpType = AsyncTeleport.TeleportType.NORMAL;
   }

   public void cooldown(final boolean check) throws Throwable {
      CompletableFuture<Boolean> exceptionFuture = new CompletableFuture<>();
      if (this.cooldown(check, exceptionFuture)) {
         try {
            exceptionFuture.get();
         } catch (ExecutionException var4) {
            throw var4.getCause();
         }
      }
   }

   public boolean cooldown(final boolean check, final CompletableFuture<Boolean> future) {
      Calendar time = new GregorianCalendar();
      if (this.teleportOwner.getLastTeleportTimestamp() > 0L) {
         double cooldown = this.ess.getSettings().getTeleportCooldown();
         Calendar earliestTime = new GregorianCalendar();
         earliestTime.add(13, -((int)cooldown));
         earliestTime.add(14, -((int)(cooldown * 1000.0 % 1000.0)));
         long earliestLong = earliestTime.getTimeInMillis();
         long lastTime = this.teleportOwner.getLastTeleportTimestamp();
         if (lastTime > time.getTimeInMillis()) {
            this.teleportOwner.setLastTeleportTimestamp(time.getTimeInMillis());
            return false;
         }

         if (lastTime > earliestLong && this.cooldownApplies()) {
            time.setTimeInMillis(lastTime);
            time.add(13, (int)cooldown);
            time.add(14, (int)(cooldown * 1000.0 % 1000.0));
            future.completeExceptionally(new TranslatableException("timeBeforeTeleport", DateUtil.formatDateDiff(time.getTimeInMillis())));
            return true;
         }
      }

      if (!check) {
         this.teleportOwner.setLastTeleportTimestamp(time.getTimeInMillis());
      }

      return false;
   }

   private boolean cooldownApplies() {
      boolean applies = true;
      String globalBypassPerm = "essentials.teleport.cooldown.bypass";
      switch (this.tpType) {
         case TPA:
            applies = !this.teleportOwner.isAuthorized("essentials.teleport.cooldown.bypass")
               || !this.teleportOwner.isAuthorized("essentials.teleport.cooldown.bypass.tpa");
            break;
         case BACK:
            applies = !this.teleportOwner.isAuthorized("essentials.teleport.cooldown.bypass")
               || !this.teleportOwner.isAuthorized("essentials.teleport.cooldown.bypass.back");
            break;
         case NORMAL:
            applies = !this.teleportOwner.isAuthorized("essentials.teleport.cooldown.bypass");
      }

      return applies;
   }

   private void warnUser(final net.ess3.api.IUser user, final double delay) {
      Calendar c = new GregorianCalendar();
      c.add(13, (int)delay);
      c.add(14, (int)(delay * 1000.0 % 1000.0));
      user.sendTl("dontMoveMessage", new Object[]{DateUtil.formatDateDiff(c.getTimeInMillis())});
   }

   @Override
   public void now(final Location loc, final boolean cooldown, final TeleportCause cause, final CompletableFuture<Boolean> future) {
      if (!cooldown || !this.cooldown(false, future)) {
         ITarget target = new LocationTarget(loc);
         this.nowAsync(this.teleportOwner, target, cause, future);
      }
   }

   @Override
   public void now(final Player entity, final boolean cooldown, final TeleportCause cause, final CompletableFuture<Boolean> future) {
      if (cooldown && this.cooldown(false, future)) {
         future.complete(false);
      } else {
         ITarget target = new PlayerTarget(entity);
         this.nowAsync(this.teleportOwner, target, cause, future);
         future.thenAccept(
            success -> {
               if (success) {
                  this.teleportOwner
                     .sendTl(
                        "teleporting",
                        new Object[]{
                           target.getLocation().getWorld().getName(),
                           target.getLocation().getBlockX(),
                           target.getLocation().getBlockY(),
                           target.getLocation().getBlockZ()
                        }
                     );
               }
            }
         );
      }
   }

   @Override
   public void nowUnsafe(Location loc, TeleportCause cause, CompletableFuture<Boolean> future) {
      CompletableFuture<Boolean> paperFuture = PaperLib.teleportAsync(this.teleportOwner.getBase(), loc, cause);
      paperFuture.thenAccept(future::complete);
      paperFuture.exceptionally(future::completeExceptionally);
   }

   private void runOnMain(final Runnable runnable) throws ExecutionException, InterruptedException {
      if (Bukkit.isPrimaryThread()) {
         runnable.run();
      } else {
         CompletableFuture<Object> taskLock = new CompletableFuture<>();
         Bukkit.getScheduler().runTask(this.ess, () -> {
            runnable.run();
            taskLock.complete(new Object());
         });
         taskLock.get();
      }
   }

   protected void nowAsync(final net.ess3.api.IUser teleportee, final ITarget target, final TeleportCause cause, final CompletableFuture<Boolean> future) {
      this.cancel(false);
      PreTeleportEvent event = new PreTeleportEvent(teleportee, cause, target);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (event.isCancelled()) {
         future.complete(false);
      } else {
         if (!this.ess.getSettings().isForcePassengerTeleport() && !teleportee.getBase().isEmpty()) {
            if (!this.ess.getSettings().isTeleportPassengerDismount()) {
               future.completeExceptionally(new TranslatableException("passengerTeleportFail"));
               return;
            }

            try {
               this.runOnMain(() -> teleportee.getBase().eject());
            } catch (InterruptedException | ExecutionException var7) {
               future.completeExceptionally(var7);
               return;
            }
         }

         if (teleportee.isAuthorized("essentials.back.onteleport")) {
            teleportee.setLastLocation();
         }

         Location targetLoc = target.getLocation();
         if (this.ess.getSettings().isTeleportSafetyEnabled()
            && !this.ess.getSettings().isForceDisableTeleportSafety()
            && LocationUtil.isBlockOutsideWorldBorder(targetLoc.getWorld(), targetLoc.getBlockX(), targetLoc.getBlockZ())) {
            targetLoc.setX(LocationUtil.getXInsideWorldBorder(targetLoc.getWorld(), targetLoc.getBlockX()));
            targetLoc.setZ(LocationUtil.getZInsideWorldBorder(targetLoc.getWorld(), targetLoc.getBlockZ()));
         }

         PaperLib.getChunkAtAsync(targetLoc.getWorld(), targetLoc.getBlockX() >> 4, targetLoc.getBlockZ() >> 4, true, true)
            .thenAccept(
               chunk -> {
                  Location loc = targetLoc;
                  if (LocationUtil.isBlockUnsafeForUser(
                     this.ess, teleportee, chunk.getWorld(), targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ()
                  )) {
                     if (!this.ess.getSettings().isTeleportSafetyEnabled()) {
                        future.completeExceptionally(
                           new TranslatableException(
                              "unsafeTeleportDestination", targetLoc.getWorld().getName(), targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ()
                           )
                        );
                        return;
                     }

                     if (this.ess.getSettings().isForceDisableTeleportSafety()) {
                        teleportee.getBase().teleport(targetLoc, cause);
                     } else {
                        try {
                           PaperLib.teleportAsync(teleportee.getBase(), LocationUtil.getSafeDestination(this.ess, teleportee, loc), cause);
                        } catch (Exception var8) {
                           future.completeExceptionally(var8);
                           return;
                        }
                     }
                  } else if (this.ess.getSettings().isForceDisableTeleportSafety()) {
                     teleportee.getBase().teleport(targetLoc, cause);
                  } else {
                     if (this.ess.getSettings().isTeleportToCenterLocation()) {
                        loc = LocationUtil.getRoundedDestination(targetLoc);
                     }

                     PaperLib.teleportAsync(teleportee.getBase(), loc, cause);
                  }

                  future.complete(true);
               }
            )
            .exceptionally(th -> {
               future.completeExceptionally(th);
               return null;
            });
      }
   }

   @Override
   public void teleport(final Location loc, final Trade chargeFor, final TeleportCause cause, final CompletableFuture<Boolean> future) {
      this.teleport(this.teleportOwner, new LocationTarget(loc), chargeFor, cause, future);
   }

   @Override
   public void teleport(final Player entity, final Trade chargeFor, final TeleportCause cause, final CompletableFuture<Boolean> future) {
      this.teleportOwner.sendTl("teleportToPlayer", new Object[]{entity.getDisplayName()});
      this.teleport(this.teleportOwner, new PlayerTarget(entity), chargeFor, cause, future);
   }

   @Override
   public void teleportPlayer(
      final net.ess3.api.IUser otherUser, final Location loc, final Trade chargeFor, final TeleportCause cause, final CompletableFuture<Boolean> future
   ) {
      this.teleport(otherUser, new LocationTarget(loc), chargeFor, cause, future);
   }

   @Override
   public void teleportPlayer(
      final net.ess3.api.IUser otherUser, final Player entity, final Trade chargeFor, final TeleportCause cause, final CompletableFuture<Boolean> future
   ) {
      ITarget target = new PlayerTarget(entity);
      this.teleport(otherUser, target, chargeFor, cause, future);
      future.thenAccept(
         success -> {
            if (success) {
               otherUser.sendTl(
                  "teleporting",
                  new Object[]{
                     target.getLocation().getWorld().getName(),
                     target.getLocation().getBlockX(),
                     target.getLocation().getBlockY(),
                     target.getLocation().getBlockZ()
                  }
               );
               this.teleportOwner
                  .sendTl(
                     "teleporting",
                     new Object[]{
                        target.getLocation().getWorld().getName(),
                        target.getLocation().getBlockX(),
                        target.getLocation().getBlockY(),
                        target.getLocation().getBlockZ()
                     }
                  );
            }
         }
      );
   }

   private void teleport(
      final net.ess3.api.IUser teleportee, final ITarget target, final Trade chargeFor, final TeleportCause cause, final CompletableFuture<Boolean> future
   ) {
      double delay = this.ess.getSettings().getTeleportDelay();
      TeleportWarmupEvent event = new TeleportWarmupEvent(teleportee, cause, target, delay);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (event.isCancelled()) {
         future.complete(false);
      } else {
         delay = event.getDelay();
         Trade cashCharge = chargeFor;
         if (chargeFor != null) {
            chargeFor.isAffordableFor(this.teleportOwner, future);
            if (future.isCompletedExceptionally()) {
               return;
            }

            if (!chargeFor.getCommandCost(this.teleportOwner).equals(BigDecimal.ZERO)) {
               cashCharge = new Trade(chargeFor.getCommandCost(this.teleportOwner), this.ess);
            }
         }

         if (this.cooldown(true, future)) {
            future.complete(false);
         } else if (!(delay <= 0.0)
            && !this.teleportOwner.isAuthorized("essentials.teleport.timer.bypass")
            && !teleportee.isAuthorized("essentials.teleport.timer.bypass")) {
            this.cancel(false);
            this.warnUser(teleportee, delay);
            this.initTimer((long)(delay * 1000.0), teleportee, target, cashCharge, cause, false, future);
         } else if (this.cooldown(false, future)) {
            future.complete(false);
         } else {
            this.nowAsync(teleportee, target, cause, future);
            if (cashCharge != null) {
               cashCharge.charge(this.teleportOwner, future);
               if (future.isCompletedExceptionally()) {
                  return;
               }
            }

            future.complete(true);
         }
      }
   }

   private void teleportOther(
      final net.ess3.api.IUser teleporter,
      final net.ess3.api.IUser teleportee,
      final ITarget target,
      final Trade chargeFor,
      final TeleportCause cause,
      final CompletableFuture<Boolean> future
   ) {
      double delay = this.ess.getSettings().getTeleportDelay();
      TeleportWarmupEvent event = new TeleportWarmupEvent(teleporter, teleportee, cause, target, delay);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         delay = event.getDelay();
         Trade cashCharge = chargeFor;
         if (teleporter != null && chargeFor != null) {
            chargeFor.isAffordableFor(teleporter, future);
            if (future.isCompletedExceptionally()) {
               return;
            }

            if (!chargeFor.getCommandCost(teleporter).equals(BigDecimal.ZERO)) {
               cashCharge = new Trade(chargeFor.getCommandCost(teleporter), this.ess);
            }
         }

         if (!this.cooldown(true, future)) {
            if (!(delay <= 0.0)
               && teleporter != null
               && !teleporter.isAuthorized("essentials.teleport.timer.bypass")
               && !this.teleportOwner.isAuthorized("essentials.teleport.timer.bypass")
               && !teleportee.isAuthorized("essentials.teleport.timer.bypass")) {
               this.cancel(false);
               this.warnUser(teleportee, delay);
               this.initTimer((long)(delay * 1000.0), teleportee, target, cashCharge, cause, false, future);
            } else if (!this.cooldown(false, future)) {
               this.nowAsync(teleportee, target, cause, future);
               if (teleporter != null && cashCharge != null) {
                  cashCharge.charge(teleporter, future);
                  if (future.isCompletedExceptionally()) {
                     return;
                  }
               }

               future.complete(true);
            }
         }
      }
   }

   @Override
   public void respawn(final Trade chargeFor, final TeleportCause cause, final CompletableFuture<Boolean> future) {
      double delay = this.ess.getSettings().getTeleportDelay();
      TeleportWarmupEvent event = new TeleportWarmupEvent(this.teleportOwner, cause, null, delay);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         delay = event.getDelay();
         if (chargeFor != null) {
            chargeFor.isAffordableFor(this.teleportOwner, future);
            if (future.isCompletedExceptionally()) {
               return;
            }
         }

         if (!this.cooldown(true, future)) {
            if (!(delay <= 0.0) && !this.teleportOwner.isAuthorized("essentials.teleport.timer.bypass")) {
               this.cancel(false);
               this.warnUser(this.teleportOwner, delay);
               this.initTimer((long)(delay * 1000.0), this.teleportOwner, null, chargeFor, cause, true, future);
            } else if (!this.cooldown(false, future)) {
               this.respawnNow(this.teleportOwner, cause, future);
               if (chargeFor != null) {
                  chargeFor.charge(this.teleportOwner, future);
               }

               future.complete(true);
            }
         }
      }
   }

   void respawnNow(final net.ess3.api.IUser teleportee, final TeleportCause cause, final CompletableFuture<Boolean> future) {
      Player player = teleportee.getBase();
      PaperLib.getBedSpawnLocationAsync(player, true).thenAccept(location -> {
         if (location != null) {
            this.nowAsync(teleportee, new LocationTarget(location), cause, future);
         } else {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().info("Could not find bed spawn, forcing respawn event.");
            }

            PlayerRespawnEvent pre = new PlayerRespawnEvent(player, player.getWorld().getSpawnLocation(), false);
            this.ess.getServer().getPluginManager().callEvent(pre);
            this.nowAsync(teleportee, new LocationTarget(pre.getRespawnLocation()), cause, future);
         }
      }).exceptionally(th -> {
         future.completeExceptionally(th);
         return null;
      });
   }

   @Override
   public void warp(final net.ess3.api.IUser otherUser, String warp, final Trade chargeFor, final TeleportCause cause, final CompletableFuture<Boolean> future) {
      UserWarpEvent event = new UserWarpEvent(otherUser, warp, chargeFor);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         warp = event.getWarp();

         Location loc;
         try {
            loc = this.ess.getWarps().getWarp(warp);
         } catch (WarpNotFoundException var9) {
            future.completeExceptionally(var9);
            return;
         }

         future.thenAccept(success -> {
            if (success) {
               otherUser.sendTl("warpingTo", new Object[]{warp, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});
               if (!otherUser.equals(this.teleportOwner)) {
                  this.teleportOwner.sendTl("warpingTo", new Object[]{warp, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});
               }
            }
         });
         this.teleport(otherUser, new LocationTarget(loc), chargeFor, cause, future);
      }
   }

   @Override
   public void back(final Trade chargeFor, final CompletableFuture<Boolean> future) {
      this.back(this.teleportOwner, chargeFor, future);
   }

   @Override
   public void back(final net.ess3.api.IUser teleporter, final Trade chargeFor, final CompletableFuture<Boolean> future) {
      this.tpType = AsyncTeleport.TeleportType.BACK;
      Location loc = this.teleportOwner.getLastLocation();
      this.teleportOwner.sendTl("backUsageMsg", new Object[]{loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});
      this.teleportOther(teleporter, this.teleportOwner, new LocationTarget(loc), chargeFor, TeleportCause.COMMAND, future);
   }

   @Override
   public void back(final CompletableFuture<Boolean> future) {
      this.nowAsync(this.teleportOwner, new LocationTarget(this.teleportOwner.getLastLocation()), TeleportCause.COMMAND, future);
   }

   public AsyncTeleport.TeleportType getTpType() {
      return this.tpType;
   }

   public void setTpType(final AsyncTeleport.TeleportType tpType) {
      this.tpType = tpType;
   }

   private void cancel(final boolean notifyUser) {
      if (this.timedTeleport != null) {
         this.timedTeleport.cancelTimer(notifyUser);
         this.timedTeleport = null;
      }
   }

   private void initTimer(
      final long delay,
      final net.ess3.api.IUser teleportUser,
      final ITarget target,
      final Trade chargeFor,
      final TeleportCause cause,
      final boolean respawn,
      CompletableFuture<Boolean> future
   ) {
      this.timedTeleport = new AsyncTimedTeleport(this.teleportOwner, this.ess, this, delay, future, teleportUser, target, chargeFor, cause, respawn);
   }

   public static enum TeleportType {
      TPA,
      BACK,
      NORMAL;
   }
}
