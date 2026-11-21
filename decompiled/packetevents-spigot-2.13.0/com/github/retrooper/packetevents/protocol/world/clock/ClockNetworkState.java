package com.github.retrooper.packetevents.protocol.world.clock;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ClockNetworkState {
   private final long totalTicks;
   private final float partialTick;
   private final float rate;

   @Obsolete
   public ClockNetworkState(long totalTicks, boolean paused) {
      this(totalTicks, 0.0F, paused ? 0.0F : 1.0F);
   }

   public ClockNetworkState(long totalTicks, float partialTick, float rate) {
      this.totalTicks = totalTicks;
      this.partialTick = partialTick;
      this.rate = rate;
   }

   public static ClockNetworkState read(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         long totalTicks = wrapper.readVarLong();
         float partialTick = wrapper.readFloat();
         float rate = wrapper.readFloat();
         return new ClockNetworkState(totalTicks, partialTick, rate);
      } else {
         long totalTicks = wrapper.readLong();
         boolean tickTime = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2) ? wrapper.readBoolean() : totalTicks >= 0L;
         return new ClockNetworkState(totalTicks, !tickTime);
      }
   }

   public static void write(PacketWrapper<?> wrapper, ClockNetworkState state) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         wrapper.writeVarLong(state.totalTicks);
         wrapper.writeFloat(state.partialTick);
         wrapper.writeFloat(state.rate);
      } else {
         wrapper.writeLong(state.totalTicks);
         if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
            wrapper.writeBoolean(!state.isPaused());
         }
      }
   }

   public long getTotalTicks() {
      return this.totalTicks;
   }

   public ClockNetworkState withTotalTicks(long totalTicks) {
      return new ClockNetworkState(totalTicks, this.partialTick, this.rate);
   }

   public float getPartialTick() {
      return this.partialTick;
   }

   public ClockNetworkState withPartialTick(float partialTick) {
      return new ClockNetworkState(this.totalTicks, partialTick, this.rate);
   }

   public float getRate() {
      return this.rate;
   }

   public ClockNetworkState withRate(float rate) {
      return new ClockNetworkState(this.totalTicks, this.partialTick, rate);
   }

   public boolean isPaused() {
      return Float.compare(this.rate, 0.0F) == 0;
   }

   public ClockNetworkState withPaused(boolean paused) {
      return new ClockNetworkState(this.totalTicks, this.partialTick, paused ? 0.0F : 1.0F);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ClockNetworkState)) {
         return false;
      } else {
         ClockNetworkState that = (ClockNetworkState)obj;
         if (this.totalTicks != that.totalTicks) {
            return false;
         } else {
            return Float.compare(that.partialTick, this.partialTick) != 0 ? false : Float.compare(that.rate, this.rate) == 0;
         }
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.totalTicks, this.partialTick, this.rate);
   }
}
