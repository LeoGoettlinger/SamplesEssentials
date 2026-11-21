package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTString;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.serializer.SequentialNBTReader;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.VersionMapper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.VersionRange;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.jetbrains.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

@ApiStatus.Internal
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
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      //
      // Bytecode:
      // 00: aload 1
      // 01: invokeinterface java/util/Map$Entry.getKey ()Ljava/lang/Object; 1
      // 06: checkcast java/lang/String
      // 09: invokestatic ac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/player/ClientVersion.valueOf (Ljava/lang/String;)Lac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/player/ClientVersion;
      // 0c: astore 4
      // 0e: aload 3
      // 0f: bipush 0
      // 10: aload 4
      // 12: aastore
      // 13: aload 1
      // 14: invokeinterface java/util/Map$Entry.getValue ()Ljava/lang/Object; 1
      // 19: checkcast ac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/nbt/serializer/SequentialNBTReader$Compound
      // 1c: invokevirtual ac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/nbt/serializer/SequentialNBTReader$Compound.spliterator ()Ljava/util/Spliterator;
      // 1f: bipush 0
      // 20: invokestatic java/util/stream/StreamSupport.stream (Ljava/util/Spliterator;Z)Ljava/util/stream/Stream;
      // 23: invokedynamic apply ()Ljava/util/function/Function; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ (Ljava/lang/Object;)Ljava/lang/Object;, java/util/Map$Entry.getKey ()Ljava/lang/Object;, (Ljava/util/Map$Entry;)Ljava/lang/String; ]
      // 28: invokedynamic apply ()Ljava/util/function/Function; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ (Ljava/lang/Object;)Ljava/lang/Object;, ac/grim/grimac/shaded/com/github/retrooper/packetevents/util/mappings/TypesBuilder.lambda$loadAsMap$0 (Ljava/util/Map$Entry;)Ljava/lang/Integer;, (Ljava/util/Map$Entry;)Ljava/lang/Integer; ]
      // 2d: invokestatic java/util/stream/Collectors.toMap (Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/util/stream/Collector;
      // 30: invokeinterface java/util/stream/Stream.collect (Ljava/util/stream/Collector;)Ljava/lang/Object; 2
      // 35: checkcast java/util/Map
      // 38: astore 5
      // 3a: aload 0
      // 3b: aload 5
      // 3d: invokedynamic accept (Lac/grim/grimac/shaded/com/github/retrooper/packetevents/util/mappings/TypesBuilder;Ljava/util/Map;)Ljava/util/function/Consumer; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ (Ljava/lang/Object;)V, ac/grim/grimac/shaded/com/github/retrooper/packetevents/util/mappings/TypesBuilder.lambda$loadAsMap$1 (Ljava/util/Map;Lac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/player/ClientVersion;)V, (Lac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/player/ClientVersion;)V ]
      // 42: astore 6
      // 44: aload 6
      // 46: aload 4
      // 48: invokeinterface java/util/function/Consumer.accept (Ljava/lang/Object;)V 2
      // 4d: bipush 1
      // 4e: istore 7
      // 50: aload 2
      // 51: invokevirtual ac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/nbt/serializer/SequentialNBTReader$Compound.iterator ()Ljava/util/Iterator;
      // 54: astore 8
      // 56: aload 8
      // 58: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 5d: ifeq c8
      // 60: aload 8
      // 62: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 67: checkcast java/util/Map$Entry
      // 6a: astore 9
      // 6c: aload 9
      // 6e: invokeinterface java/util/Map$Entry.getKey ()Ljava/lang/Object; 1
      // 73: checkcast java/lang/String
      // 76: invokestatic ac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/player/ClientVersion.valueOf (Ljava/lang/String;)Lac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/player/ClientVersion;
      // 79: astore 10
      // 7b: aload 3
      // 7c: iload 7
      // 7e: iinc 7 1
      // 81: aload 10
      // 83: aastore
      // 84: aload 9
      // 86: invokeinterface java/util/Map$Entry.getValue ()Ljava/lang/Object; 1
      // 8b: checkcast ac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/nbt/serializer/SequentialNBTReader$Compound
      // 8e: invokestatic ac/grim/grimac/shaded/com/github/retrooper/packetevents/util/mappings/MappingHelper.createDiff (Lac/grim/grimac/shaded/com/github/retrooper/packetevents/protocol/nbt/serializer/SequentialNBTReader$Compound;)Ljava/util/List;
      // 91: astore 11
      // 93: aload 11
      // 95: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
      // 9a: astore 12
      // 9c: aload 12
      // 9e: invokeinterface java/util/Iterator.hasNext ()Z 1
      // a3: ifeq bc
      // a6: aload 12
      // a8: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // ad: checkcast ac/grim/grimac/shaded/com/github/retrooper/packetevents/util/mappings/MapDiff
      // b0: astore 13
      // b2: aload 13
      // b4: aload 5
      // b6: invokevirtual ac/grim/grimac/shaded/com/github/retrooper/packetevents/util/mappings/MapDiff.applyTo (Ljava/util/Map;)V
      // b9: goto 9c
      // bc: aload 6
      // be: aload 10
      // c0: invokeinterface java/util/function/Consumer.accept (Ljava/lang/Object;)V 2
      // c5: goto 56
      // c8: return
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
