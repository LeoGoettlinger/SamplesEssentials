package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R3.mind;

import com.google.common.collect.ImmutableList;
import com.magmaguy.elitemobs.magmacore.ai.MindActionRequest;
import com.magmaguy.elitemobs.magmacore.ai.MindActionResult;
import com.magmaguy.elitemobs.magmacore.ai.MindBehavior;
import com.magmaguy.elitemobs.magmacore.ai.MindContext;
import com.magmaguy.elitemobs.magmacore.ai.MindControl;
import com.magmaguy.elitemobs.magmacore.ai.MindFailure;
import com.magmaguy.elitemobs.magmacore.ai.MindMemoryStore;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import com.magmaguy.elitemobs.magmacore.ai.MindSensor;
import com.magmaguy.elitemobs.magmacore.ai.MindStopReason;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.schedule.Activity;

final class NativeMindProgramRuntime {
   private final NativeMindSession session;
   private final NativeMindBody body;
   private final MindProgram program;
   private final MindMemoryStore memory;
   private final long generation;
   private final NativeMindControlLeaseBook leases = new NativeMindControlLeaseBook();
   private final NativeMindNavigationTracker navigation;
   private final NativeMindActuator actuator;
   private final NativeMindPerception perception;
   private final NativeMindCallbackScheduler callbacks;
   private final List<NativeMindBehaviorControl> behaviors = new ArrayList<>();
   private BehaviorController<EntityInsentient> brain;
   private long gameTick;
   private int actionRequestCount;
   private MindStopReason externalStopReason;

   NativeMindProgramRuntime(
      NativeMindSession session, NativeMindBody body, MindProgram program, MindMemoryStore memory, long generation, NativeMindServerBudget serverBudget
   ) {
      this.session = session;
      this.body = body;
      this.program = program;
      this.memory = memory;
      this.generation = generation;
      this.navigation = new NativeMindNavigationTracker(body.mob());
      this.actuator = new NativeMindActuator(body, this.navigation, session::movementOverridden);
      this.perception = new NativeMindPerception(body.mob(), this.navigation);
      this.callbacks = new NativeMindCallbackScheduler(program.executionPolicy(), serverBudget, this::report);
   }

   BehaviorController<EntityInsentient> buildBrain() {
      BehaviorController<EntityInsentient> next = NativeMindVersion.newBrain();
      List<Pair<Integer, ? extends BehaviorControl<? super EntityInsentient>>> sensorControls = new ArrayList<>();

      for (MindSensor sensor : this.program.sensors()) {
         sensorControls.add(Pair.of(Integer.MIN_VALUE, new NativeMindSensorControl(this, sensor)));
      }

      List<Pair<Integer, ? extends BehaviorControl<? super EntityInsentient>>> behaviorControls = new ArrayList<>();

      for (MindBehavior behavior : this.program.behaviors()) {
         NativeMindBehaviorControl control = new NativeMindBehaviorControl(this, behavior, this.leases);
         this.behaviors.add(control);
         behaviorControls.add(Pair.of(behavior.priority(), control));
      }

      NativeMindVersion.addActivity(next, Activity.a, ImmutableList.copyOf(sensorControls));
      NativeMindVersion.addActivity(next, Activity.b, ImmutableList.copyOf(behaviorControls));
      next.a(Set.of(Activity.a));
      next.b(Activity.b);
      next.f();
      this.brain = next;
      return next;
   }

   void tickBrain(WorldServer currentLevel) {
      if (this.brain == null) {
         throw new IllegalStateException("Native Mind Brain is not built");
      } else {
         this.brain.a(currentLevel, this.body.mob());
      }
   }

   void beginTick(WorldServer currentLevel) {
      this.gameTick = currentLevel.ad();
      this.actionRequestCount = 0;
      this.callbacks.beginTick(currentLevel.p().ak());
      this.memory.view(this.gameTick);
      this.navigation.beginTick(this.gameTick);
   }

   MindContext context(NativeMindBehaviorControl owner) {
      return new MindContext(
         this.body.entity(),
         this.gameTick,
         this.generation,
         this.memory.view(this.gameTick),
         this.perception,
         this.actuator.guarded(owner, this.leases),
         request -> this.requestAction(owner, request)
      );
   }

   private MindActionResult requestAction(NativeMindBehaviorControl owner, MindActionRequest request) {
      NativeMindHost.requirePrimaryThread();
      if (owner != null && this.leases.holds(owner, MindControl.ACTION)) {
         if (this.actionRequestCount >= this.program.executionPolicy().maxActionRequestsPerEntityTick()) {
            return MindActionResult.DEFERRED;
         } else {
            this.actionRequestCount++;
            return this.session.requestAction(this.body, this.gameTick, this.generation, request);
         }
      } else {
         throw new IllegalStateException("Mind behavior must declare and hold the ACTION control");
      }
   }

   <T> NativeMindCallbackScheduler.CallbackResult<T> invoke(String callbackIdentifier, Supplier<T> callback) {
      return this.callbacks.invoke(callbackIdentifier, callback);
   }

   void retire(WorldServer currentLevel, MindStopReason reason) {
      if (this.brain != null) {
         this.gameTick = currentLevel.ad();
         this.callbacks.beginTick(currentLevel.p().ak());
         this.externalStopReason = reason;

         try {
            this.brain.b(currentLevel, this.body.mob());
         } finally {
            this.brain.h();
            this.externalStopReason = null;
            this.leases.clear();
            this.actuator.stopAll();
         }
      }
   }

   void actuatorStopAll() {
      this.actuator.stopAll();
   }

   void releaseControls(NativeMindBehaviorControl owner) {
      this.actuator.guarded(owner, this.leases).stopAll();
   }

   MindStopReason externalStopReason() {
      return this.externalStopReason == null ? MindStopReason.COMPLETED : this.externalStopReason;
   }

   Set<String> activeBehaviors() {
      Set<String> active = new LinkedHashSet<>();

      for (NativeMindBehaviorControl behavior : this.behaviors) {
         if (behavior.running()) {
            active.add(behavior.identifier());
         }
      }

      return active;
   }

   private void report(String callbackIdentifier, MindFailure.Kind kind, Throwable cause) {
      this.session.report(new MindFailure(this.session.logicalOwner(), this.program.identifier(), callbackIdentifier, kind, cause));
   }
}
