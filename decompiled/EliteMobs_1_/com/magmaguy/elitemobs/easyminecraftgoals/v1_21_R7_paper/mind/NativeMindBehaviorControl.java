package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindBehavior;
import com.magmaguy.elitemobs.magmacore.ai.MindControl;
import com.magmaguy.elitemobs.magmacore.ai.MindStopReason;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.Behavior.Status;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

final class NativeMindBehaviorControl implements BehaviorControl<EntityInsentient> {
   private final NativeMindProgramRuntime runtime;
   private final MindBehavior behavior;
   private final NativeMindControlLeaseBook leases;
   private final Set<MindControl> controls;
   private Status status = Status.a;

   NativeMindBehaviorControl(NativeMindProgramRuntime runtime, MindBehavior behavior, NativeMindControlLeaseBook leases) {
      this.runtime = runtime;
      this.behavior = behavior;
      this.leases = leases;
      Set<MindControl> declared = behavior.controls();
      this.controls = declared.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(declared));
   }

   String identifier() {
      return this.behavior.identifier();
   }

   int priority() {
      return this.behavior.priority();
   }

   boolean running() {
      return this.status == Status.b;
   }

   public Status a() {
      return this.status;
   }

   public Set<MemoryModuleType<?>> getRequiredMemories() {
      return Set.of();
   }

   public boolean tryStart(WorldServer level, EntityInsentient body, long timestamp) {
      NativeMindCallbackScheduler.CallbackResult<Boolean> allowed = this.runtime
         .invoke(this.identifier() + ".can_start", () -> this.behavior.canStart(this.runtime.context(this)));
      if (!allowed.successful() || !Boolean.TRUE.equals(allowed.value())) {
         return false;
      } else if (!this.leases.acquire(this, this.controls)) {
         return false;
      } else {
         this.status = Status.b;
         NativeMindCallbackScheduler.CallbackResult<Void> started = this.runtime.invoke(this.identifier() + ".start", () -> {
            this.behavior.start(this.runtime.context(this));
            return null;
         });
         if (started.wasSkipped()) {
            this.status = Status.a;
            this.leases.release(this);
            return false;
         } else if (!started.successful()) {
            this.stop(MindStopReason.CALLBACK_FAILED);
            return false;
         } else {
            return true;
         }
      }
   }

   public void tickOrStop(WorldServer level, EntityInsentient body, long timestamp) {
      if (this.running()) {
         NativeMindCallbackScheduler.CallbackResult<Boolean> continuing = this.runtime
            .invoke(this.identifier() + ".can_continue", () -> this.behavior.canContinue(this.runtime.context(this)));
         if (!continuing.wasSkipped()) {
            if (!continuing.successful()) {
               this.stop(MindStopReason.CALLBACK_FAILED);
            } else if (!Boolean.TRUE.equals(continuing.value())) {
               this.stop(MindStopReason.COMPLETED);
            } else {
               NativeMindCallbackScheduler.CallbackResult<Void> ticked = this.runtime.invoke(this.identifier() + ".tick", () -> {
                  this.behavior.tick(this.runtime.context(this));
                  return null;
               });
               if (!ticked.successful() && !ticked.wasSkipped()) {
                  this.stop(MindStopReason.CALLBACK_FAILED);
               }
            }
         }
      }
   }

   public void doStop(WorldServer level, EntityInsentient body, long timestamp) {
      this.stop(this.runtime.externalStopReason());
   }

   void preempt() {
      this.stop(MindStopReason.PREEMPTED);
   }

   private void stop(MindStopReason reason) {
      if (!this.running()) {
         this.leases.release(this);
      } else {
         try {
            this.runtime.invoke(this.identifier() + ".stop", () -> {
               this.behavior.stop(this.runtime.context(this), reason);
               return null;
            });
         } finally {
            this.runtime.releaseControls(this);
            this.status = Status.a;
            this.leases.release(this);
         }
      }
   }

   public String b() {
      return "MagmaCoreBehavior[" + this.identifier() + "]";
   }
}
