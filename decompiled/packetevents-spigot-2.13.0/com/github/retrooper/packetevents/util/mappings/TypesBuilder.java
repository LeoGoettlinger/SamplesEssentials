package com.github.retrooper.packetevents.util.mappings;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.github.retrooper.packetevents.protocol.nbt.serializer.SequentialNBTReader;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.VersionMapper;
import com.github.retrooper.packetevents.util.VersionRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class TypesBuilder {
   private final String mapPath;
   private Map<ClientVersion, Map<String, Integer>> entries = new HashMap<>();
   private VersionMapper versionMapper;
   @Nullable
   VersionedRegistry<?> registry;

   public TypesBuilder(String mapPath, boolean lazy) {
      this.mapPath = mapPath;
      if (!lazy) {
         this.load();
      }
   }

   public TypesBuilder(String mapPath) {
      this(mapPath, false);
   }

   public void load() {
      if (this.entries == null) {
         this.entries = new HashMap<>();
      }

      try {
         SequentialNBTReader.Compound rootCompound = MappingHelper.decompress("mappings/" + this.mapPath);

         try {
            rootCompound.skipOne();
            SequentialNBTReader.Compound compound = (SequentialNBTReader.Compound)rootCompound.next().getValue();
            int length = ((NBTNumber)compound.next().getValue()).getAsInt();
            SequentialNBTReader.Compound entries = (SequentialNBTReader.Compound)compound.next().getValue();
            ClientVersion[] versions = new ClientVersion[length];
            Entry<String, NBT> first = entries.next();
            if (first.getValue().getType() == NBTType.LIST) {
               this.loadAsArray(first, entries, versions);
            } else {
               this.loadAsMap(first, entries, versions);
            }

            this.versionMapper = new VersionMapper(versions);
         } catch (Throwable var8) {
            if (rootCompound != null) {
               try {
                  rootCompound.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (rootCompound != null) {
            rootCompound.close();
         }
      } catch (IOException var9) {
         throw new RuntimeException("Unable to load mapping files.", var9);
      }
   }

   private void loadAsArray(final Entry<String, NBT> first, final SequentialNBTReader.Compound entries, final ClientVersion[] versions) {
      ClientVersion start = ClientVersion.valueOf(first.getKey());
      versions[0] = start;
      List<String> lastEntries = new ArrayList<>();

      for (NBT entry : (SequentialNBTReader.List)first.getValue()) {
         lastEntries.add(((NBTString)entry).getValue());
      }

      Consumer<ClientVersion> mapLoader = versionx -> {
         Map<String, Integer> map = new HashMap<>();
         int size = lastEntries.size();

         for (int ix = 0; ix < size; ix++) {
            map.put(lastEntries.get(ix), ix);
         }

         this.entries.put(versionx, map);
      };
      mapLoader.accept(start);
      int i = 1;

      for (Entry<String, NBT> entry : entries) {
         ClientVersion version = ClientVersion.valueOf(entry.getKey());
         versions[i++] = version;
         List<ListDiff<String>> diff = MappingHelper.createListDiff((SequentialNBTReader.Compound)entry.getValue());

         for (int j = diff.size() - 1; j >= 0; j--) {
            diff.get(j).applyTo(lastEntries);
         }

         mapLoader.accept(version);
      }
   }

   private void loadAsMap(final Entry<String, NBT> first, final SequentialNBTReader.Compound entries, final ClientVersion[] versions) {
      ClientVersion start = ClientVersion.valueOf(first.getKey());
      versions[0] = start;
      Map<String, Integer> lastEntries = StreamSupport.stream(((SequentialNBTReader.Compound)first.getValue()).spliterator(), false)
         .collect(Collectors.toMap(Entry::getKey, entryx -> ((NBTNumber)entryx.getValue()).getAsInt()));
      Consumer<ClientVersion> mapLoader = versionx -> {
         Map<String, Integer> map = new HashMap<>(lastEntries);
         this.entries.put(versionx, map);
      };
      mapLoader.accept(start);
      int i = 1;

      for (Entry<String, NBT> entry : entries) {
         ClientVersion version = ClientVersion.valueOf(entry.getKey());
         versions[i++] = version;

         for (MapDiff<String, Integer> d : MappingHelper.createDiff((SequentialNBTReader.Compound)entry.getValue())) {
            d.applyTo(lastEntries);
         }

         mapLoader.accept(version);
      }
   }

   @Nullable
   public VersionedRegistry<?> getRegistry() {
      return this.registry;
   }

   public ClientVersion[] getVersions() {
      return this.versionMapper.getVersions();
   }

   public ClientVersion[] getReversedVersions() {
      return this.versionMapper.getReversedVersions();
   }

   public int getDataIndex(ClientVersion rawVersion) {
      return this.versionMapper.getIndex(rawVersion);
   }

   public VersionMapper getVersionMapper() {
      return this.versionMapper;
   }

   public void addExtraVersionStep(ClientVersion version) {
      VersionMapper newMapper = this.versionMapper.withExtra(version);
      if (this.versionMapper != newMapper) {
         int baseIndex = this.versionMapper.getIndex(version);
         ClientVersion baseVersion = this.versionMapper.getVersions()[baseIndex];
         this.entries.put(version, this.entries.get(baseVersion));
         this.versionMapper = newMapper;
      }
   }

   @VisibleForTesting
   public boolean isMappingDataLoaded() {
      return this.entries != null;
   }

   public void unloadFileMappings() {
      this.entries.clear();
      this.entries = null;
   }

   public TypesBuilderData define(String key, VersionRange range) {
      ResourceLocation name = new ResourceLocation(key);
      int[] ids = new int[this.getVersions().length];
      int index = 0;

      for (ClientVersion v : this.getVersions()) {
         Map<String, Integer> map = this.entries.get(v);
         if (map.containsKey(key)) {
            int id = map.get(key);
            ids[index] = id;
         } else {
            ids[index] = -1;
         }

         index++;
      }

      return new TypesBuilderData(name, ids, this, range);
   }

   @Nullable
   public Map<ClientVersion, Map<String, Integer>> getEntries() {
      return this.entries;
   }
}
