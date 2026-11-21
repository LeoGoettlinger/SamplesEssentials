package com.github.retrooper.packetevents.protocol.world.attributes.timelines;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntityRef;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttributes;
import com.github.retrooper.packetevents.protocol.world.clock.WorldClock;
import com.github.retrooper.packetevents.protocol.world.clock.WorldClocks;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Collections;
import java.util.Map;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Timeline extends MappedEntity, CopyableEntity<Timeline>, DeepComparableEntity {
   NbtCodec<Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>>> TRACK_CODEC = NbtMapCodec.codecOfMap(
         NbtCodecs.forRegistry(EnvironmentAttributes.getRegistry()), EnvironmentAttribute::isSynced, TimelineTrack::codec
      )
      .codec();
   NbtCodec<Map<ResourceLocation, TimeMarkerInfo>> TIME_MARKER_CODEC = NbtMapCodec.codecOfMap(ResourceLocation.CODEC, TimeMarkerInfo.CODEC).codec();
   NbtCodec<Timeline> CODEC = (new NbtMapCodec<Timeline>() {
         private final NbtCodec<MappedEntityRef<WorldClock>> clockCodec = MappedEntityRef.codec(WorldClock.CODEC);

         public Timeline decode(NBTCompound compound, PacketWrapper<?> wrapper) throws NbtCodecException {
            MappedEntityRef<WorldClock> clock = (MappedEntityRef<WorldClock>)(wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)
               ? new MappedEntityRef.Static<>(WorldClocks.OVERWORLD)
               : compound.getOrThrow("clock", this.clockCodec, wrapper));
            Integer periodTicks = compound.getOrNull("period_ticks", NbtCodecs.INT, wrapper);
            Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> tracks = compound.getOr("tracks", Timeline.TRACK_CODEC, Collections.emptyMap(), wrapper);
            Map<ResourceLocation, TimeMarkerInfo> timeMarkers = wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)
               ? Collections.emptyMap()
               : compound.getOr("time_markers", Timeline.TIME_MARKER_CODEC, Collections.emptyMap(), wrapper);
            return new StaticTimeline(clock, periodTicks, tracks, timeMarkers);
         }

         public void encode(NBTCompound compound, PacketWrapper<?> wrapper, Timeline value) throws NbtCodecException {
            if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
               compound.set("clock", value.getClockRef(), this.clockCodec, wrapper);
            }

            Integer periodTicks = value.getPeriodTicks();
            if (periodTicks != null) {
               compound.setTag("period_ticks", new NBTInt(periodTicks));
            }

            Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> tracks = value.getTracks();
            if (!tracks.isEmpty()) {
               compound.set("tracks", tracks, Timeline.TRACK_CODEC, wrapper);
            }

            Map<ResourceLocation, TimeMarkerInfo> timeMarkers = value.getTimeMarkers();
            if (!timeMarkers.isEmpty()) {
               compound.set("time_markers", timeMarkers, Timeline.TIME_MARKER_CODEC, wrapper);
            }
         }
      })
      .codec();

   WorldClock getClock();

   MappedEntityRef<WorldClock> getClockRef();

   @Nullable
   Integer getPeriodTicks();

   Map<EnvironmentAttribute<?>, TimelineTrack<?, ?>> getTracks();

   Map<ResourceLocation, TimeMarkerInfo> getTimeMarkers();
}
