package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.mind;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.TransientMovementOverride;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.flee.FleePathfinder;
import com.magmaguy.elitemobs.magmacore.ai.MindActionRequest;
import com.magmaguy.elitemobs.magmacore.ai.MindActionResult;
import com.magmaguy.elitemobs.magmacore.ai.MindFailure;
import com.magmaguy.elitemobs.magmacore.ai.MindHandle;
import com.magmaguy.elitemobs.magmacore.ai.MindMemoryStore;
import com.magmaguy.elitemobs.magmacore.ai.MindPersistentState;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import com.magmaguy.elitemobs.magmacore.ai.MindSnapshot;
import com.magmaguy.elitemobs.magmacore.ai.MindStopReason;
import com.magmaguy.elitemobs.magmacore.ai.MobBody;
import com.magmaguy.elitemobs.magmacore.ai.StateTransfer;
import com.magmaguy.elitemobs.magmacore.ai.SwapReceipt;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.pathfinder.PathEntity;
import org.bukkit.Location;

final class NativeMindSession implements MindHandle {
   private final NativeMindHost host;
   private final UUID logicalOwner;
   private final NativeMindServerBudget serverBudget;
   private final Deque<NativeMindSession.SwapRequest> swaps = new ArrayDeque<>();
   private final Deque<NativeMindFleeOverride> movementOverrides = new ArrayDeque<>();
   private NativeMindPathfindingHandle pathfinding;
   private final NativeMindMovementOverrideGate movementOverrideGate = new NativeMindMovementOverrideGate();
   private MindProgram program;
   private MindMemoryStore memory;
   private MindPersistentState lastWrittenState;
   private long generation = 1L;
   private long lastRequestedGeneration = 1L;
   private NativeMindBody body;
   private NativeMindProgramRuntime runtime;
   private boolean inBrainTick;
   private boolean inLifecycleBoundary;
   private boolean detachRequested;
   private boolean preserveMarkerOnDetach;
   private boolean closeRequested;
   private boolean preserveMarkerOnClose;
   private Boolean requestedPause;
   private boolean paused;
   private long pausedAtGameTick = Long.MIN_VALUE;
   private boolean closed;

   NativeMindSession(NativeMindHost host, UUID logicalOwner, MindProgram program, MindPersistentState restoredState, NativeMindServerBudget serverBudget) {
      this.host = host;
      this.logicalOwner = logicalOwner;
      this.program = program;
      this.serverBudget = serverBudget;
      this.memory = new MindMemoryStore(program.schema());
      this.memory.restore(restoredState);
   }

   UUID logicalOwner() {
      return this.logicalOwner;
   }

   boolean hasBody(UUID bodyId) {
      return this.body != null && this.body.mob().cY().equals(bodyId);
   }

   boolean hasBody(EntityInsentient candidate) {
      return this.body != null && this.body.mob() == candidate;
   }

   Optional<TransientMovementOverride> beginFlee(Location threatLocation, double speedModifier) {
      this.requireOpen();
      if (Double.isFinite(speedModifier) && !(speedModifier <= 0.0)) {
         if (this.body != null && this.body.mob() instanceof EntityCreature pathfinderMob) {
            PathEntity initialPath = FleePathfinder.findPath(pathfinderMob, threatLocation);
            if (initialPath == null) {
               return Optional.empty();
            } else {
               NativeMindFleeOverride override;
               try {
                  override = new NativeMindFleeOverride(this, pathfinderMob, threatLocation, speedModifier, initialPath, this.movementOverrideGate.acquire());
               } catch (IllegalArgumentException var8) {
                  return Optional.empty();
               }

               this.movementOverrides.addLast(override);
               return Optional.of(override);
            }
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   boolean movementOverridden() {
      return this.movementOverrideGate.isOverridden();
   }

   Optional<PathfindingHandle> openPathfinding() {
      this.requireOpen();
      if (this.body == null) {
         return Optional.empty();
      } else {
         if (this.pathfinding != null) {
            this.pathfinding.close();
         }

         this.pathfinding = new NativeMindPathfindingHandle(this, this.body, this.movementOverrideGate);
         return Optional.of(this.pathfinding);
      }
   }

   void releasePathfinding(NativeMindPathfindingHandle handle) {
      if (this.pathfinding == handle) {
         this.pathfinding = null;
      }
   }

   void removeMovementOverride(NativeMindFleeOverride override) {
      this.movementOverrides.remove(override);
   }

   @Override
   public void attachBody(MobBody nextBody) {
      NativeMindHost.requirePrimaryThread();
      this.requireOpen();
      if (nextBody instanceof NativeMindBody nativeBody && nativeBody.host() == this.host) {
         if (this.body != null && this.body.mob().eh()) {
            RemovalReason reason = this.body.removalReason();
            this.bodyRemoved(this.body.mob(), reason == null ? RemovalReason.b : reason);
            this.requireOpen();
         }

         if (this.body != nativeBody || !nativeBody.canAttach()) {
            if (this.body != null) {
               throw new IllegalStateException("Logical mind session already has a body");
            } else if (nativeBody.attached()) {
               throw new IllegalStateException("Native mind body is already attached");
            } else if (!nativeBody.canAttach()) {
               throw new IllegalArgumentException("Native mind body is not valid");
            } else {
               this.applyDetachedSwaps();
               NativeMindProgramRuntime candidate = new NativeMindProgramRuntime(
                  this, nativeBody, this.program, this.memory, this.generation, this.serverBudget
               );
               candidate.buildBrain();
               this.body = nativeBody;
               this.runtime = candidate;

               try {
                  nativeBody.attach(this);
                  candidate.actuatorStopAll();
                  if (this.paused && this.pausedAtGameTick == Long.MIN_VALUE) {
                     this.pausedAtGameTick = nativeBody.level().au();
                     this.memory.view(this.pausedAtGameTick);
                     nativeBody.stopMovement();
                  }

                  this.writeMarker(nativeBody);
               } catch (RuntimeException var5) {
                  nativeBody.detach(this);
                  this.body = null;
                  this.runtime = null;
                  throw var5;
               }
            }
         }
      } else {
         throw new IllegalArgumentException("Body was not created by this native mind host");
      }
   }

   @Override
   public void detachBody() {
      NativeMindHost.requirePrimaryThread();
      if (!this.closed && this.body != null) {
         if (!this.inBrainTick && !this.inLifecycleBoundary) {
            this.detachNow(MindStopReason.ENTITY_REMOVED, false);
            this.finishDeferredAfterDetach();
         } else {
            this.requestDetach(false);
         }
      }
   }

   @Override
   public void suspendBody() {
      NativeMindHost.requirePrimaryThread();
      if (!this.closed && this.body != null) {
         if (!this.inBrainTick && !this.inLifecycleBoundary) {
            this.detachNow(MindStopReason.ENTITY_REMOVED, true);
            this.finishDeferredAfterDetach();
         } else {
            this.requestDetach(true);
         }
      }
   }

   @Override
   public void setPaused(boolean paused) {
      NativeMindHost.requirePrimaryThread();
      this.requireOpen();
      if (!this.inBrainTick && !this.inLifecycleBoundary) {
         this.applyPause(paused);
      } else {
         this.requestedPause = paused;
      }
   }

   @Override
   public SwapReceipt replace(MindProgram next, StateTransfer transfer) {
      NativeMindHost.requirePrimaryThread();
      this.requireOpen();
      Objects.requireNonNull(next, "next");
      Objects.requireNonNull(transfer, "transfer");
      next.claimForBinding();
      long requested = this.incrementRequestedGeneration();
      this.swaps.addLast(new NativeMindSession.SwapRequest(requested, next, transfer));
      if (this.body == null) {
         this.applyDetachedSwaps();
         return new SwapReceipt(requested, false);
      } else {
         return new SwapReceipt(requested, true);
      }
   }

   @Override
   public MindSnapshot inspect() {
      NativeMindHost.requirePrimaryThread();
      Set<String> active = this.runtime == null ? Set.of() : this.runtime.activeBehaviors();
      return new MindSnapshot(
         this.logicalOwner,
         this.body == null ? Optional.empty() : Optional.of(this.body.entity().getUniqueId()),
         this.program.identifier(),
         this.program.revision(),
         this.generation,
         active,
         this.memory.persistentState(),
         this.requestedPause == null ? this.paused : this.requestedPause,
         this.closed
      );
   }

   @Override
   public void close() {
      NativeMindHost.requirePrimaryThread();
      if (!this.closed) {
         if (!this.inBrainTick && !this.inLifecycleBoundary) {
            this.closeNow(false);
         } else {
            this.requestClose(false);
         }
      }
   }

   void closeForShutdown() {
      NativeMindHost.requirePrimaryThread();
      if (!this.closed) {
         if (!this.inBrainTick && !this.inLifecycleBoundary) {
            this.closeNow(true);
         } else {
            this.requestClose(true);
         }
      }
   }

   void tickBody() {
      if (!this.closed && this.body != null && this.runtime != null) {
         NativeMindBody tickingBody = this.body;
         EntityInsentient mob = tickingBody.mob();
         if (mob.eh()) {
            RemovalReason reason = tickingBody.removalReason();
            this.bodyRemoved(mob, reason == null ? RemovalReason.b : reason);
         } else {
            WorldServer level = tickingBody.level();
            if (level.h(mob.dK())) {
               if (this.inBrainTick) {
                  throw new IllegalStateException("Native mind Brain tick is reentrant");
               } else {
                  this.inBrainTick = true;

                  try {
                     this.applyAttachedSwaps(level);
                     if (this.runtime == null || this.body == null) {
                        return;
                     }

                     NativeMindFleeOverride movementOverride = this.activeMovementOverride();
                     if (this.pathfinding != null) {
                        this.pathfinding.tick(this.paused || movementOverride != null);
                     }

                     if (!this.paused || movementOverride != null) {
                        tickingBody.beforeMindTick();

                        try {
                           if (!this.paused) {
                              this.runtime.beginTick(level);
                              this.runtime.tickBrain(level);
                           }

                           movementOverride = this.activeMovementOverride();
                           if (movementOverride != null) {
                              movementOverride.tick();
                           }
                        } finally {
                           tickingBody.afterMindTick();
                        }

                        this.writeMarkerIfChanged(tickingBody);
                        return;
                     }

                     tickingBody.tickPausedPhysics();
                     this.writeMarkerIfChanged(tickingBody);
                  } finally {
                     this.inBrainTick = false;
                     this.applyRequestedPause();
                     if (this.closeRequested) {
                        this.closeRequested = false;
                        boolean preserveMarker = this.preserveMarkerOnClose;
                        this.preserveMarkerOnClose = false;
                        this.closeNow(preserveMarker);
                     } else if (this.detachRequested) {
                        this.detachRequested = false;
                        boolean preserveMarker = this.preserveMarkerOnDetach;
                        this.preserveMarkerOnDetach = false;
                        this.detachNow(MindStopReason.ENTITY_REMOVED, preserveMarker);
                        this.finishDeferredAfterDetach();
                     }
                  }
               }
            }
         }
      }
   }

   void bodyRemoved(EntityInsentient removedBody, RemovalReason reason) {
      if (!this.closed && this.body != null && this.body.mob() == removedBody) {
         if (!this.inBrainTick && !this.inLifecycleBoundary) {
            this.detachNow(MindStopReason.ENTITY_REMOVED, reason.b());
            this.finishDeferredAfterDetach();
         } else {
            this.requestDetach(reason.b());
         }
      }
   }

   void report(MindFailure failure) {
      this.host.report(failure);
   }

   MindActionResult requestAction(NativeMindBody body, long gameTick, long generation, MindActionRequest request) {
      return this.host.requestAction(this, body, gameTick, generation, request);
   }

   private void applyAttachedSwaps(WorldServer level) {
      if (!this.swaps.isEmpty()) {
         List<NativeMindSession.SwapRequest> pending = new ArrayList<>(this.swaps);
         NativeMindSession.Migration preflight = this.migrate(pending);
         new NativeMindProgramRuntime(this, this.body, preflight.program(), preflight.memory(), preflight.generation(), this.serverBudget).buildBrain();
         this.runtime.retire(level, MindStopReason.PROGRAM_REPLACED);
         this.runtime.actuatorStopAll();
         NativeMindSession.Migration migration = this.migrate(pending);
         NativeMindProgramRuntime candidate = new NativeMindProgramRuntime(
            this, this.body, migration.program(), migration.memory(), migration.generation(), this.serverBudget
         );
         candidate.buildBrain();
         this.program = migration.program();
         this.memory = migration.memory();
         this.generation = migration.generation();
         this.runtime = candidate;
         this.removeAppliedSwaps(pending);
         this.writeMarker(this.body);
      }
   }

   private void applyDetachedSwaps() {
      if (!this.swaps.isEmpty()) {
         NativeMindSession.Migration migration = this.migrate(new ArrayList<>(this.swaps));
         this.program = migration.program();
         this.memory = migration.memory();
         this.generation = migration.generation();
         this.swaps.clear();
      }
   }

   private NativeMindSession.Migration migrate(List<NativeMindSession.SwapRequest> pending) {
      MindProgram nextProgram = this.program;
      MindMemoryStore nextMemory = this.memory;
      long nextGeneration = this.generation;

      for (NativeMindSession.SwapRequest request : pending) {
         nextMemory = nextMemory.transferTo(request.program().schema(), request.transfer());
         nextProgram = request.program();
         nextGeneration = request.generation();
      }

      return new NativeMindSession.Migration(nextProgram, nextMemory, nextGeneration);
   }

   private void detachNow(MindStopReason reason, boolean preserveMarker) {
      NativeMindBody oldBody = this.body;
      NativeMindProgramRuntime oldRuntime = this.runtime;
      if (oldBody != null) {
         this.inLifecycleBoundary = true;

         try {
            this.closeMovementOverrides();
            WorldServer level = (WorldServer)oldBody.mob().ao();
            if (oldRuntime != null) {
               oldRuntime.retire(level, reason);
               oldRuntime.actuatorStopAll();
            }

            this.applyDetachedSwaps();
            oldBody.stopMovement();
            boolean retain = preserveMarker;
            if (this.detachRequested) {
               retain = preserveMarker & this.preserveMarkerOnDetach;
            }

            if (this.closeRequested) {
               retain &= this.preserveMarkerOnClose;
            }

            if (retain) {
               this.writeMarker(oldBody);
            } else {
               MindCarrierState.clear(oldBody.entity(), this.host.hostIdentity());
            }

            oldBody.detach(this);
            this.body = null;
            this.runtime = null;
         } finally {
            this.inLifecycleBoundary = false;
         }
      }
   }

   private void applyRequestedPause() {
      if (this.requestedPause != null && !this.closed) {
         boolean next = this.requestedPause;
         this.requestedPause = null;
         this.applyPause(next);
      }
   }

   private void applyPause(boolean next) {
      if (this.paused != next) {
         if (next) {
            this.paused = true;
            if (this.body != null) {
               this.pausedAtGameTick = this.body.level().au();
               this.memory.view(this.pausedAtGameTick);
               if (this.runtime != null) {
                  this.runtime.actuatorStopAll();
               }

               this.body.stopMovement();
            }
         } else {
            if (this.pausedAtGameTick != Long.MIN_VALUE && this.body != null) {
               long currentGameTick = this.body.level().au();
               this.memory.shiftClock(Math.max(0L, currentGameTick - this.pausedAtGameTick));
            }

            this.pausedAtGameTick = Long.MIN_VALUE;
            this.paused = false;
         }
      }
   }

   private void closeNow(boolean preserveMarker) {
      if (!this.closed) {
         if (this.body != null) {
            this.detachNow(MindStopReason.HANDLE_CLOSED, preserveMarker);
         } else {
            this.closeMovementOverrides();
            this.applyDetachedSwaps();
         }

         this.swaps.clear();
         this.requestedPause = null;
         this.pausedAtGameTick = Long.MIN_VALUE;
         this.closeRequested = false;
         this.detachRequested = false;
         this.closed = true;
         this.host.sessionClosed(this);
      }
   }

   private void finishDeferredAfterDetach() {
      if (this.closeRequested) {
         boolean preserveMarker = this.preserveMarkerOnClose;
         this.closeRequested = false;
         this.preserveMarkerOnClose = false;
         this.closeNow(preserveMarker);
      } else {
         this.detachRequested = false;
         this.preserveMarkerOnDetach = false;
      }
   }

   private void requestDetach(boolean preserveMarker) {
      if (!this.detachRequested) {
         this.preserveMarkerOnDetach = preserveMarker;
      } else {
         this.preserveMarkerOnDetach &= preserveMarker;
      }

      this.detachRequested = true;
   }

   private void requestClose(boolean preserveMarker) {
      if (!this.closeRequested) {
         this.preserveMarkerOnClose = preserveMarker;
      } else {
         this.preserveMarkerOnClose &= preserveMarker;
      }

      this.closeRequested = true;
   }

   private void removeAppliedSwaps(List<NativeMindSession.SwapRequest> applied) {
      for (NativeMindSession.SwapRequest expected : applied) {
         if (this.swaps.peekFirst() != expected) {
            throw new IllegalStateException("Mind swap queue order changed during a safe boundary");
         }

         this.swaps.removeFirst();
      }
   }

   private void writeMarker(NativeMindBody target) {
      this.writeMarker(target, this.memory.persistentState());
   }

   private void writeMarker(NativeMindBody target, MindPersistentState state) {
      MindCarrierState.write(
         target.entity(), this.host.hostIdentity(), target.profile(), this.logicalOwner, this.program.identifier(), this.program.revision(), state
      );
      this.lastWrittenState = state;
   }

   private void writeMarkerIfChanged(NativeMindBody target) {
      MindPersistentState current = this.memory.persistentState();
      if (!current.equals(this.lastWrittenState)) {
         this.writeMarker(target, current);
      }
   }

   private long incrementRequestedGeneration() {
      if (this.lastRequestedGeneration == Long.MAX_VALUE) {
         throw new IllegalStateException("Mind generation is exhausted");
      } else {
         return ++this.lastRequestedGeneration;
      }
   }

   private NativeMindFleeOverride activeMovementOverride() {
      while (!this.movementOverrides.isEmpty()) {
         NativeMindFleeOverride active = this.movementOverrides.peekLast();
         if (active.isActive()) {
            return active;
         }

         this.movementOverrides.removeLast();
      }

      return null;
   }

   private void closeMovementOverrides() {
      if (this.pathfinding != null) {
         this.pathfinding.close();
      }

      for (NativeMindFleeOverride movementOverride : new ArrayList<>(this.movementOverrides)) {
         movementOverride.close();
      }

      this.movementOverrides.clear();
   }

   private void requireOpen() {
      if (this.closed || this.closeRequested) {
         throw new IllegalStateException("Logical mind session is closed");
      }
   }

   private record Migration(MindProgram program, MindMemoryStore memory, long generation) {
   }

   private record SwapRequest(long generation, MindProgram program, StateTransfer transfer) {
   }
}
