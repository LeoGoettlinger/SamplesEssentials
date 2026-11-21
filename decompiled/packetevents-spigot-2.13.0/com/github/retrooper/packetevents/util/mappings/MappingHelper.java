package com.github.retrooper.packetevents.util.mappings;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTLimiter;
import com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.serializer.SequentialNBTReader;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class MappingHelper {
   public static SequentialNBTReader.Compound decompress(final String path) {
      try {
         DataInputStream dataInput = new DataInputStream(
            new GZIPInputStream(new BufferedInputStream(PacketEvents.getAPI().getSettings().getResourceProvider().apply("assets/" + path + ".nbt")))
         );
         return (SequentialNBTReader.Compound)SequentialNBTReader.INSTANCE.deserializeTag(NBTLimiter.noop(), dataInput);
      } catch (IOException var2) {
         throw new RuntimeException("Cannot find resource file " + path + ".nbt", var2);
      }
   }

   public static List<ListDiff<String>> createListDiff(final SequentialNBTReader.Compound compound) {
      List<ListDiff<String>> diffs = new ArrayList<>();

      for (NBT entry : (SequentialNBTReader.List)compound.next().getValue()) {
         SequentialNBTReader.Compound c = (SequentialNBTReader.Compound)entry;
         diffs.add(new ListDiff.Removal<>(((NBTNumber)c.next().getValue()).getAsInt(), ((NBTNumber)c.next().getValue()).getAsInt()));
      }

      for (NBT entry : (SequentialNBTReader.List)compound.next().getValue()) {
         SequentialNBTReader.Compound c = (SequentialNBTReader.Compound)entry;
         diffs.add(
            new ListDiff.Addition<>(
               ((NBTNumber)c.next().getValue()).getAsInt(),
               StreamSupport.stream(((SequentialNBTReader.List)c.next().getValue()).spliterator(), false)
                  .map(nbt -> ((NBTString)nbt).getValue())
                  .collect(Collectors.toList())
            )
         );
      }

      for (NBT entry : (SequentialNBTReader.List)compound.next().getValue()) {
         SequentialNBTReader.Compound c = (SequentialNBTReader.Compound)entry;
         diffs.add(
            new ListDiff.Changed<>(
               ((NBTNumber)c.next().getValue()).getAsInt(),
               ((NBTNumber)c.next().getValue()).getAsInt(),
               StreamSupport.stream(((SequentialNBTReader.List)c.next().getValue()).spliterator(), false)
                  .map(nbt -> ((NBTString)nbt).getValue())
                  .collect(Collectors.toList())
            )
         );
      }

      diffs.sort(Comparator.comparingInt(ListDiff::getIndex));
      return diffs;
   }

   public static List<MapDiff<String, Integer>> createDiff(final SequentialNBTReader.Compound compound) {
      List<MapDiff<String, Integer>> diffs = new ArrayList<>();

      for (Entry<String, NBT> entry : (SequentialNBTReader.Compound)compound.next().getValue()) {
         diffs.add(new MapDiff.Removal<>(entry.getKey()));
      }

      for (Entry<String, NBT> entry : (SequentialNBTReader.Compound)compound.next().getValue()) {
         diffs.add(new MapDiff.Addition<>(entry.getKey(), ((NBTNumber)entry.getValue()).getAsInt()));
      }

      return diffs;
   }

   public static <T extends MappedEntity> void registerMapping(
      TypesBuilder builder, Map<String, T>[] typeNames, Map<Integer, T>[] typeIds, TypesBuilderData typeData, T type
   ) {
      for (ClientVersion version : builder.getVersions()) {
         if (typeData.getVersions().contains(version)) {
            int index = builder.getDataIndex(version);
            Map<String, T> nameMap = typeNames[index];
            if (nameMap == null) {
               typeNames[index] = nameMap = new HashMap<>();
            }

            nameMap.put(typeData.getName().toString(), type);
            Map<Integer, T> idMap = typeIds[index];
            if (idMap == null) {
               typeIds[index] = idMap = new HashMap<>();
            }

            idMap.put(typeData.getId(version), type);
         }
      }
   }

   public static int getId(ClientVersion version, TypesBuilder builder, TypesBuilderData data) {
      return data.getData()[builder.getDataIndex(version)];
   }
}
