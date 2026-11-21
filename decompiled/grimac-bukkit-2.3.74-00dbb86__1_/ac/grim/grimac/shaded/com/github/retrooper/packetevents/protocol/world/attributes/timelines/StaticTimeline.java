package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.attributes.timelines;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntityRef;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.clock.WorldClock;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.clock.WorldClocks;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticTimeline extends AbstractMappedEntity implements Timeline {
   private final MappedEntityRef<WorldClock> clock;
   @Nullable
   private final Integer periodTicks;
   private final Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> tracks;
   private final Map<ResourceLocation, TimeMarkerInfo> timeMarkers;

   @ApiStatus.Obsolete
   public StaticTimeline(@Nullable Integer periodTicks, Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> tracks) {
      this(null, new MappedEntityRef.Static<>(WorldClocks.OVERWORLD), periodTicks, tracks, Collections.emptyMap());
   }

   public StaticTimeline(
      WorldClock clock,
      @Nullable Integer periodTicks,
      Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> tracks,
      Map<ResourceLocation, TimeMarkerInfo> timeMarkers
   ) {
      this(null, new MappedEntityRef.Static<>(clock), periodTicks, tracks, timeMarkers);
   }

   public StaticTimeline(
      MappedEntityRef<WorldClock> clock,
      @Nullable Integer periodTicks,
      Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> tracks,
      Map<ResourceLocation, TimeMarkerInfo> timeMarkers
   ) {
      this(null, clock, periodTicks, tracks, timeMarkers);
   }

   @ApiStatus.Internal
   public StaticTimeline(
      @Nullable TypesBuilderData data,
      MappedEntityRef<WorldClock> clock,
      @Nullable Integer periodTicks,
      Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> tracks,
      Map<ResourceLocation, TimeMarkerInfo> timeMarkers
   ) {
      super(data);
      this.clock = clock;
      this.periodTicks = periodTicks;
      this.tracks = Collections.unmodifiableMap(tracks);
      this.timeMarkers = Collections.unmodifiableMap(timeMarkers);
   }

   public Timeline copy(@Nullable TypesBuilderData newData) {
      return new StaticTimeline(newData, this.clock, this.periodTicks, this.tracks, this.timeMarkers);
   }

   @Override
   public WorldClock getClock() {
      return this.clock.get();
   }

   @Override
   public MappedEntityRef<WorldClock> getClockRef() {
      return this.clock;
   }

   @Nullable
   @Override
   public Integer getPeriodTicks() {
      return this.periodTicks;
   }

   @Override
   public Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> getTracks() {
      return this.tracks;
   }

   @Override
   public Map<ResourceLocation, TimeMarkerInfo> getTimeMarkers() {
      return this.timeMarkers;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         StaticTimeline that = (StaticTimeline)obj;
         if (!this.clock.equals(that.clock)) {
            return false;
         } else if (!Objects.equals(this.periodTicks, that.periodTicks)) {
            return false;
         } else {
            return !this.tracks.equals(that.tracks) ? false : this.timeMarkers.equals(that.timeMarkers);
         }
      } else {
         return false;
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.clock, this.periodTicks, this.tracks, this.timeMarkers);
   }
}
