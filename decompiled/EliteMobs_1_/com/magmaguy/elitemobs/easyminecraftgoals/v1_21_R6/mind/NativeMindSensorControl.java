package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R6.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindSensor;
import java.util.Set;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.Behavior.Status;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

final class NativeMindSensorControl implements BehaviorControl<EntityInsentient> {
   private final NativeMindProgramRuntime runtime;
   private final MindSensor sensor;
   private final int intervalTicks;
   private long nextTick = Long.MIN_VALUE;

   NativeMindSensorControl(NativeMindProgramRuntime runtime, MindSensor sensor) {
      this.runtime = runtime;
      this.sensor = sensor;
      this.intervalTicks = sensor.intervalTicks();
      if (this.intervalTicks <= 0) {
         throw new IllegalArgumentException("Mind sensor interval must be positive: " + sensor.identifier());
      }
   }

   public Status a() {
      return Status.a;
   }

   public Set<MemoryModuleType<?>> getRequiredMemories() {
      return Set.of();
   }

   public boolean tryStart(WorldServer level, EntityInsentient body, long timestamp) {
      if (timestamp < this.nextTick) {
         return false;
      } else {
         NativeMindCallbackScheduler.CallbackResult<Void> sensed = this.runtime.invoke(this.sensor.identifier() + ".sense", () -> {
            this.sensor.sense(this.runtime.context(null));
            return null;
         });
         if (!sensed.wasSkipped()) {
            this.nextTick = timestamp > Long.MAX_VALUE - this.intervalTicks ? Long.MAX_VALUE : timestamp + this.intervalTicks;
         }

         return false;
      }
   }

   public void tickOrStop(WorldServer level, EntityInsentient body, long timestamp) {
   }

   public void doStop(WorldServer level, EntityInsentient body, long timestamp) {
   }

   public String b() {
      return "MagmaCoreSensor[" + this.sensor.identifier() + "]";
   }
}
