package com.earth2me.essentials;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.essentialsx.api.v2.events.TeleportWarmupCancelledEvent;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class AsyncTimedTeleport implements Runnable {
   private static final double MOVE_CONSTANT = 0.3;
   private final net.ess3.api.IUser teleportOwner;
   private final net.ess3.api.IEssentials ess;
   private final AsyncTeleport teleport;
   private final UUID timer_teleportee;
   private final long timer_started;
   private final long timer_delay;
   private final CompletableFuture<Boolean> parentFuture;
   private final long timer_initX;
   private final long timer_initY;
   private final long timer_initZ;
   private final ITarget timer_teleportTarget;
   private final boolean timer_respawn;
   private final boolean timer_canMove;
   private final Trade timer_chargeFor;
   private final TeleportCause timer_cause;
   private int timer_task;
   private double timer_health;

   AsyncTimedTeleport(
      final net.ess3.api.IUser user,
      final net.ess3.api.IEssentials ess,
      final AsyncTeleport teleport,
      final long delay,
      final net.ess3.api.IUser teleportUser,
      final ITarget target,
      final Trade chargeFor,
      final TeleportCause cause,
      final boolean respawn
   ) {
      this(user, ess, teleport, delay, null, teleportUser, target, chargeFor, cause, respawn);
   }

   AsyncTimedTeleport(
      final net.ess3.api.IUser user,
      final net.ess3.api.IEssentials ess,
      final AsyncTeleport teleport,
      final long delay,
      final CompletableFuture<Boolean> future,
      final net.ess3.api.IUser teleportUser,
      final ITarget target,
      final Trade chargeFor,
      final TeleportCause cause,
      final boolean respawn
   ) {
      this.teleportOwner = user;
      this.ess = ess;
      this.teleport = teleport;
      this.timer_started = System.currentTimeMillis();
      this.timer_delay = delay;
      this.timer_health = teleportUser.getBase().getHealth();
      this.timer_initX = Math.round(teleportUser.getBase().getLocation().getX() * 0.3);
      this.timer_initY = Math.round(teleportUser.getBase().getLocation().getY() * 0.3);
      this.timer_initZ = Math.round(teleportUser.getBase().getLocation().getZ() * 0.3);
      this.timer_teleportee = teleportUser.getBase().getUniqueId();
      this.timer_teleportTarget = target;
      this.timer_chargeFor = chargeFor;
      this.timer_cause = cause;
      this.timer_respawn = respawn;
      this.timer_canMove = user.isAuthorized("essentials.teleport.timer.move");
      this.timer_task = ess.runTaskTimerAsynchronously(this, 20L, 20L).getTaskId();
      if (future != null) {
         this.parentFuture = future;
      } else {
         CompletableFuture<Boolean> cFuture = new CompletableFuture<>();
         cFuture.exceptionally(e -> {
            ess.showError(this.teleportOwner.getSource(), e, "\\ teleport");
            return false;
         });
         this.parentFuture = cFuture;
      }
   }

   @Override
   public void run() {
      if (this.teleportOwner != null && this.teleportOwner.getBase().isOnline() && this.teleportOwner.getBase().getLocation() != null) {
         final net.ess3.api.IUser teleportUser = this.ess.getUser(this.timer_teleportee);
         if (teleportUser != null && teleportUser.getBase().isOnline()) {
            Location currLocation = teleportUser.getBase().getLocation();
            if (currLocation == null) {
               this.cancelTimer(false);
            } else if (this.timer_canMove
               || Math.round(currLocation.getX() * 0.3) == this.timer_initX
                  && Math.round(currLocation.getY() * 0.3) == this.timer_initY
                  && Math.round(currLocation.getZ() * 0.3) == this.timer_initZ
                  && !(teleportUser.getBase().getHealth() < this.timer_health)) {
               class DelayedTeleportTask implements Runnable {
                  @Override
                  public void run() {
                     AsyncTimedTeleport.this.timer_health = teleportUser.getBase().getHealth();
                     long now = System.currentTimeMillis();
                     if (now > AsyncTimedTeleport.this.timer_started + AsyncTimedTeleport.this.timer_delay) {
                        try {
                           AsyncTimedTeleport.this.teleport.cooldown(false);
                        } catch (Throwable var5) {
                           AsyncTimedTeleport.this.teleportOwner.sendTl("cooldownWithMessage", new Object[]{var5.getMessage()});
                           if (AsyncTimedTeleport.this.teleportOwner != teleportUser) {
                              teleportUser.sendTl("cooldownWithMessage", new Object[]{var5.getMessage()});
                           }
                        }

                        try {
                           AsyncTimedTeleport.this.cancelTimer(false);
                           teleportUser.sendTl("teleportationCommencing", new Object[0]);
                           if (AsyncTimedTeleport.this.timer_chargeFor != null) {
                              AsyncTimedTeleport.this.timer_chargeFor.isAffordableFor(AsyncTimedTeleport.this.teleportOwner);
                           }

                           if (AsyncTimedTeleport.this.timer_respawn) {
                              AsyncTimedTeleport.this.teleport
                                 .respawnNow(teleportUser, AsyncTimedTeleport.this.timer_cause, AsyncTimedTeleport.this.parentFuture);
                           } else {
                              AsyncTimedTeleport.this.teleport
                                 .nowAsync(
                                    teleportUser,
                                    AsyncTimedTeleport.this.timer_teleportTarget,
                                    AsyncTimedTeleport.this.timer_cause,
                                    AsyncTimedTeleport.this.parentFuture
                                 );
                           }

                           AsyncTimedTeleport.this.parentFuture.thenAccept(success -> {
                              if (AsyncTimedTeleport.this.timer_chargeFor != null) {
                                 try {
                                    AsyncTimedTeleport.this.timer_chargeFor.charge(AsyncTimedTeleport.this.teleportOwner);
                                 } catch (ChargeException var3) {
                                    AsyncTimedTeleport.this.ess.showError(AsyncTimedTeleport.this.teleportOwner.getSource(), var3, "\\ teleport");
                                 }
                              }
                           });
                        } catch (Exception var4) {
                           AsyncTimedTeleport.this.ess.showError(AsyncTimedTeleport.this.teleportOwner.getSource(), var4, "\\ teleport");
                        }
                     }
                  }
               }

               this.ess.scheduleSyncDelayedTask(new DelayedTeleportTask());
            } else {
               this.cancelTimer(true);
            }
         } else {
            this.cancelTimer(false);
         }
      } else {
         this.cancelTimer(false);
      }
   }

   void cancelTimer(final boolean notifyUser) {
      if (this.timer_task != -1) {
         try {
            this.ess.getServer().getScheduler().cancelTask(this.timer_task);
            net.ess3.api.IUser teleportUser = this.ess.getUser(this.timer_teleportee);
            if (teleportUser != null && teleportUser.getBase() != null) {
               TeleportWarmupCancelledEvent.CancelReason cancelReason = teleportUser.getBase().isOnline()
                  ? TeleportWarmupCancelledEvent.CancelReason.MOVE
                  : TeleportWarmupCancelledEvent.CancelReason.LEAVE;
               TeleportWarmupCancelledEvent event = new TeleportWarmupCancelledEvent(
                  teleportUser.getBase(), this.teleport.getTpType(), cancelReason, notifyUser
               );
               this.ess.getServer().getPluginManager().callEvent(event);
            }

            if (notifyUser) {
               this.teleportOwner.sendTl("pendingTeleportCancelled", new Object[0]);
               if (this.timer_teleportee != null && !this.timer_teleportee.equals(this.teleportOwner.getBase().getUniqueId())) {
                  this.ess.getUser(this.timer_teleportee).sendTl("pendingTeleportCancelled");
               }
            }
         } finally {
            this.timer_task = -1;
         }
      }
   }
}
