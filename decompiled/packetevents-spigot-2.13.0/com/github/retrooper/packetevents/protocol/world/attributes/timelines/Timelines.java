package com.github.retrooper.packetevents.protocol.world.attributes.timelines;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.serializer.SequentialNBTReader;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.MappingHelper;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Timelines {
   private static final VersionedRegistry<Timeline> REGISTRY = new VersionedRegistry<>("timeline");
   private static final Map<ResourceLocation, NBTCompound> TIMELINE_DATA = new HashMap<>();
   public static final Timeline DAY;
   public static final Timeline EARLY_GAME;
   public static final Timeline MOON;
   public static final Timeline VILLAGER_SCHEDULE;

   private Timelines() {
   }

   @Internal
   public static Timeline define(String name) {
      return REGISTRY.define(name, data -> {
         NBTCompound dataTag = TIMELINE_DATA.get(data.getName());
         if (dataTag != null) {
            PacketWrapper<?> wrapper = PacketWrapper.createDummyWrapper(ClientVersion.getLatest());
            return Timeline.CODEC.decode(dataTag, wrapper).copy(data);
         } else {
            throw new IllegalArgumentException("Can't define timeline " + data.getName() + ", no data found");
         }
      });
   }

   public static VersionedRegistry<Timeline> getRegistry() {
      return REGISTRY;
   }

   static {
      try {
         SequentialNBTReader.Compound dataTag = MappingHelper.decompress("mappings/data/timeline");

         try {
            dataTag.skipOne();

            for (Entry<String, NBT> entry : (SequentialNBTReader.Compound)dataTag.next().getValue()) {
               ResourceLocation timelineKey = new ResourceLocation(entry.getKey());
               TIMELINE_DATA.put(timelineKey, ((SequentialNBTReader.Compound)entry.getValue()).readFully());
            }
         } catch (Throwable var5) {
            if (dataTag != null) {
               try {
                  dataTag.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (dataTag != null) {
            dataTag.close();
         }
      } catch (IOException var6) {
         throw new RuntimeException("Error while reading timeline data", var6);
      }

      DAY = define("day");
      EARLY_GAME = define("early_game");
      MOON = define("moon");
      VILLAGER_SCHEDULE = define("villager_schedule");
      REGISTRY.unloadMappings();
   }
}
