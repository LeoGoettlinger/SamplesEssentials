package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.clock.ClockNetworkState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.clock.WorldClock;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.clock.WorldClocks;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class WrapperPlayServerTimeUpdate extends PacketWrapper<WrapperPlayServerTimeUpdate> {
   private static final ClockNetworkState FALLBACK_CLOCK = new ClockNetworkState(0L, true);
   private long gameTime;
   @MonotonicNonNull
   private Map<WorldClock, ClockNetworkState> clockUpdates;

   public WrapperPlayServerTimeUpdate(PacketSendEvent event) {
      super(event);
   }

   @ApiStatus.Obsolete
   public WrapperPlayServerTimeUpdate(long gameTime, long timeOfDay) {
      this(gameTime, timeOfDay, timeOfDay >= 0L);
   }

   @ApiStatus.Obsolete
   public WrapperPlayServerTimeUpdate(long gameTime, long timeOfDay, boolean tickTime) {
      this(gameTime, Collections.emptyMap());
      this.clockUpdates.put(WorldClocks.OVERWORLD, new ClockNetworkState(timeOfDay, tickTime));
   }

   public WrapperPlayServerTimeUpdate(long gameTime, Map<WorldClock, ClockNetworkState> clockUpdates) {
      super(PacketType.Play.Server.TIME_UPDATE);
      this.gameTime = gameTime;
      this.clockUpdates = new HashMap<>(clockUpdates);
   }

   @Override
   public void read() {
      this.gameTime = this.readLong();
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_26_1)) {
         this.clockUpdates = this.readMap(WorldClock::read, ClockNetworkState::read);
      } else {
         this.clockUpdates = new HashMap<>(1);
         this.clockUpdates.put(WorldClocks.OVERWORLD, ClockNetworkState.read(this));
      }
   }

   @Override
   public void write() {
      this.writeLong(this.gameTime);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_26_1)) {
         this.writeMap(this.clockUpdates, WorldClock::write, ClockNetworkState::write);
      } else {
         ClockNetworkState state = this.clockUpdates.getOrDefault(WorldClocks.OVERWORLD, FALLBACK_CLOCK);
         ClockNetworkState.write(this, state);
      }
   }

   public void copy(WrapperPlayServerTimeUpdate wrapper) {
      this.gameTime = wrapper.gameTime;
      this.clockUpdates = new HashMap<>(wrapper.clockUpdates);
   }

   public long getWorldAge() {
      return this.gameTime;
   }

   public void setWorldAge(long gameTime) {
      this.gameTime = gameTime;
   }

   public Map<WorldClock, ClockNetworkState> getClockUpdates() {
      return this.clockUpdates;
   }

   public void setClockUpdates(Map<WorldClock, ClockNetworkState> clockUpdates) {
      this.clockUpdates = clockUpdates;
   }

   @Nullable
   public ClockNetworkState getClockState(WorldClock clock) {
      return this.clockUpdates.get(this.replaceRegistry(WorldClocks.getRegistry(), clock));
   }

   public void setClockState(WorldClock clock, ClockNetworkState state) {
      this.clockUpdates.put(this.replaceRegistry(WorldClocks.getRegistry(), clock), state);
   }

   @ApiStatus.Obsolete
   public ClockNetworkState getClockState() {
      WorldClock clock = this.replaceRegistry(WorldClocks.getRegistry(), WorldClocks.OVERWORLD);
      return this.clockUpdates.computeIfAbsent(clock, __ -> FALLBACK_CLOCK);
   }

   @ApiStatus.Obsolete
   public void setClockState(ClockNetworkState state) {
      WorldClock clock = this.replaceRegistry(WorldClocks.getRegistry(), WorldClocks.OVERWORLD);
      this.clockUpdates.put(clock, state);
   }

   @ApiStatus.Obsolete
   public long getTimeOfDay() {
      return this.getClockState().getTotalTicks();
   }

   @ApiStatus.Obsolete
   public void setTimeOfDay(long timeOfDay) {
      this.setClockState(this.getClockState().withTotalTicks(timeOfDay));
   }

   @ApiStatus.Obsolete
   public boolean isTickTime() {
      return !this.getClockState().isPaused();
   }

   @ApiStatus.Obsolete
   public void setTickTime(boolean tickTime) {
      this.setClockState(this.getClockState().withPaused(!tickTime));
   }
}
