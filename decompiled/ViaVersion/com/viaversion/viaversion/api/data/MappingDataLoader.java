package com.viaversion.viaversion.api.data;

import com.google.common.annotations.Beta;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.io.TagReader;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonIOException;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonSyntaxException;
import com.viaversion.viaversion.util.GsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingDataLoader {
   public static final MappingDataLoader INSTANCE = new MappingDataLoader(MappingDataLoader.class, "assets/viaversion/data/");
   public static final TagReader<CompoundTag> MAPPINGS_READER = NBTIO.reader(CompoundTag.class).named();
   private static final Map<String, String[]> GLOBAL_IDENTIFIER_INDEXES = new HashMap<>();
   private static final int VERSION = 2;
   private static final byte DIRECT_ID = 0;
   private static final byte SHIFTS_ID = 1;
   private static final byte CHANGES_ID = 2;
   private static final byte IDENTITY_ID = 3;
   private final Map<String, CompoundTag> mappingsCache = new ConcurrentHashMap<>();
   private final Class<?> dataLoaderClass;
   private final String dataPath;
   private volatile boolean cacheValid = true;

   public MappingDataLoader(Class<?> dataLoaderClass, String dataPath) {
      this.dataLoaderClass = dataLoaderClass;
      this.dataPath = dataPath;
   }

   public static void loadGlobalIdentifiers() {
      CompoundTag globalIdentifiers = INSTANCE.loadNBT("identifier-table.nbt");

      for (Entry<String, Tag> entry : globalIdentifiers.entrySet()) {
         ListTag<StringTag> value = (ListTag<StringTag>)entry.getValue();
         int size = value.size();
         String[] array = new String[size];

         for (int i = 0; i < size; i++) {
            array[i] = value.get(i).getValue();
         }

         GLOBAL_IDENTIFIER_INDEXES.put(entry.getKey(), array);
      }
   }

   @Nullable
   public String identifierFromGlobalId(String registry, int globalId) {
      String[] array = GLOBAL_IDENTIFIER_INDEXES.get(registry);
      if (array == null) {
         throw new IllegalArgumentException("Unknown global identifier key: " + registry);
      } else if (globalId >= 0 && globalId < array.length) {
         return array[globalId];
      } else {
         throw new IllegalArgumentException("Unknown global identifier index: " + registry + "/" + globalId);
      }
   }

   public void clearCache() {
      this.cacheValid = false;
      this.mappingsCache.clear();
   }

   @Nullable
   public JsonObject loadFromDataDir(String name) {
      File file = new File(this.getDataFolder(), name);
      if (!file.exists()) {
         return this.loadData(name);
      } else {
         try {
            JsonObject var4;
            try (FileReader reader = new FileReader(file)) {
               var4 = GsonUtil.getGson().fromJson(reader, JsonObject.class);
            }

            return var4;
         } catch (JsonSyntaxException var8) {
            this.getLogger().warning(name + " is badly formatted!");
            throw new RuntimeException(var8);
         } catch (JsonIOException | IOException var9) {
            throw new RuntimeException(var9);
         }
      }
   }

   @Nullable
   public JsonObject loadData(String name) {
      InputStream stream = this.getResource(name);
      if (stream == null) {
         return null;
      } else {
         try {
            JsonObject var4;
            try (InputStreamReader reader = new InputStreamReader(stream)) {
               var4 = GsonUtil.getGson().fromJson(reader, JsonObject.class);
            }

            return var4;
         } catch (IOException var8) {
            throw new RuntimeException(var8);
         }
      }
   }

   @Nullable
   public CompoundTag loadNBT(String name, boolean cache) {
      if (!this.cacheValid) {
         return this.loadNBTFromFile(name);
      } else if (cache) {
         return this.mappingsCache.computeIfAbsent(name, this::loadNBTFromFile);
      } else {
         CompoundTag data = this.mappingsCache.get(name);
         return data != null ? data : this.loadNBTFromFile(name);
      }
   }

   @Nullable
   public CompoundTag loadNBT(String name) {
      return this.loadNBT(name, false);
   }

   @Nullable
   public CompoundTag loadNBTFromFile(String name) {
      InputStream resource = this.getResource(name);
      if (resource == null) {
         return null;
      } else {
         try {
            CompoundTag var4;
            try (InputStream stream = new BufferedInputStream(resource)) {
               var4 = MAPPINGS_READER.read(stream);
            }

            return var4;
         } catch (IOException var8) {
            throw new RuntimeException(var8);
         }
      }
   }

   @Nullable
   public Mappings loadMappings(CompoundTag mappingsTag, String key) {
      return this.loadMappings(mappingsTag, key, size -> {
         int[] array = new int[size];
         Arrays.fill(array, -1);
         return array;
      }, (array, id, mappedId) -> array[id] = mappedId, IntArrayMappings::of);
   }

   @Beta
   @Nullable
   public <M extends Mappings, V> Mappings loadMappings(
      CompoundTag mappingsTag,
      String key,
      MappingDataLoader.MappingHolderSupplier<V> holderSupplier,
      MappingDataLoader.AddConsumer<V> addConsumer,
      MappingDataLoader.MappingsSupplier<M, V> mappingsSupplier
   ) {
      CompoundTag tag = mappingsTag.getCompoundTag(key);
      if (tag == null) {
         return null;
      } else {
         int version = mappingsTag.getInt("version", -1);
         if (version != 2) {
            throw new IllegalArgumentException("Invalid version: " + version);
         } else {
            int mappedSize = tag.getInt("mappedSize", -1);
            byte strategy = tag.getByteTag("id").asByte();
            if (strategy == 0) {
               int size = tag.getIntTag("size").asInt();
               ByteBuf buf = Unpooled.wrappedBuffer(tag.getByteArrayTag("val").getValue());
               int[] values = new int[size];
               int prev = 0;

               for (int i = 0; i < size; i++) {
                  prev += readZigZagVarInt(buf);
                  values[i] = prev;
               }

               return IntArrayMappings.of(values, mappedSize);
            } else {
               V mappings;
               if (strategy == 1) {
                  MappingDataLoader.ValuePairs pairs = readAtValuePairs(tag);
                  int[] shiftsAt = pairs.first;
                  int[] shiftsTo = pairs.second;
                  int size = tag.getIntTag("size").asInt();
                  mappings = holderSupplier.get(size);
                  if (shiftsAt[0] != 0) {
                     int to = shiftsAt[0];

                     for (int id = 0; id < to; id++) {
                        addConsumer.addTo(mappings, id, id);
                     }
                  }

                  for (int i = 0; i < shiftsAt.length; i++) {
                     boolean isLast = i == shiftsAt.length - 1;
                     int from = shiftsAt[i];
                     int to = isLast ? size : shiftsAt[i + 1];
                     int mappedId = shiftsTo[i];

                     for (int id = from; id < to; id++) {
                        addConsumer.addTo(mappings, id, mappedId++);
                     }
                  }
               } else {
                  if (strategy != 2) {
                     if (strategy == 3) {
                        IntTag sizeTag = tag.getIntTag("size");
                        return new IdentityMappings(sizeTag.asInt(), mappedSize);
                     }

                     throw new IllegalArgumentException("Unknown serialization strategy: " + strategy);
                  }

                  MappingDataLoader.ValuePairs pairs = readAtValuePairs(tag);
                  int[] changesAt = pairs.first;
                  int[] values = pairs.second;
                  int size = tag.getIntTag("size").asInt();
                  boolean fillBetween = tag.get("nofill") == null;
                  mappings = holderSupplier.get(size);
                  int nextUnhandledId = 0;

                  for (int i = 0; i < changesAt.length; i++) {
                     int changedId = changesAt[i];
                     if (fillBetween) {
                        for (int id = nextUnhandledId; id < changedId; id++) {
                           addConsumer.addTo(mappings, id, id);
                        }

                        nextUnhandledId = changedId + 1;
                     }

                     addConsumer.addTo(mappings, changedId, values[i]);
                  }

                  if (fillBetween && nextUnhandledId != size) {
                     for (int id = nextUnhandledId; id < size; id++) {
                        addConsumer.addTo(mappings, id, id);
                     }
                  }
               }

               return mappingsSupplier.create(mappings, mappedSize);
            }
         }
      }
   }

   private static MappingDataLoader.ValuePairs readAtValuePairs(CompoundTag tag) {
      ByteBuf buf = Unpooled.wrappedBuffer(tag.getByteArrayTag("val").getValue());
      IntList at = new IntArrayList();
      IntList values = new IntArrayList();
      int prevAt = -1;
      int prevValue = 0;

      while (buf.isReadable()) {
         prevAt = prevAt + 1 + Types.VAR_INT.readPrimitive(buf);
         prevValue += readZigZagVarInt(buf);
         at.add(prevAt);
         values.add(prevValue);
      }

      return new MappingDataLoader.ValuePairs(at.toIntArray(), values.toIntArray());
   }

   private static int readZigZagVarInt(ByteBuf buf) {
      int value = Types.VAR_INT.readPrimitive(buf);
      return value >>> 1 ^ -(value & 1);
   }

   @Nullable
   public List<String> identifiersFromGlobalIds(CompoundTag mappingsTag, String key) {
      Mappings mappings = this.loadMappings(mappingsTag, key);
      return mappings != null ? this.identifiers(mappings, key) : null;
   }

   @Nullable
   public MappingDataLoader.IdentifiersPair identifiersFromGlobalIds(CompoundTag unmappedTag, CompoundTag mappedTag, String key) {
      Mappings unmapped = this.loadMappings(unmappedTag, key);
      if (unmapped == null) {
         return null;
      } else {
         Mappings mapped = this.loadMappings(mappedTag, key);
         if (mapped == null) {
            return null;
         } else {
            int size = unmapped.size();
            if (size != mapped.size()) {
               return new MappingDataLoader.IdentifiersPair(this.identifiers(unmapped, key), this.identifiers(mapped, key), false);
            } else {
               List<String> unmappedIdentifiers = new ArrayList<>(size);
               List<String> mappedIdentifiers = new ArrayList<>(size);
               boolean identity = true;

               for (int i = 0; i < size; i++) {
                  int unmappedGlobalId = unmapped.getNewId(i);
                  int mappedGlobalId = mapped.getNewId(i);
                  String unmappedIdentifier = this.identifierFromGlobalId(key, unmappedGlobalId);
                  if (unmappedGlobalId == mappedGlobalId) {
                     unmappedIdentifiers.add(unmappedIdentifier);
                     mappedIdentifiers.add(unmappedIdentifier);
                  } else {
                     if (mappedGlobalId == -1) {
                        throw new IllegalArgumentException(
                           "Mapped global id -1 for unmapped global id: " + key + "/" + unmappedIdentifier + " (" + unmappedGlobalId + ")"
                        );
                     }

                     identity = false;
                     unmappedIdentifiers.add(unmappedIdentifier);
                     mappedIdentifiers.add(this.identifierFromGlobalId(key, mappedGlobalId));
                  }
               }

               return identity
                  ? new MappingDataLoader.IdentifiersPair(unmappedIdentifiers)
                  : new MappingDataLoader.IdentifiersPair(unmappedIdentifiers, mappedIdentifiers);
            }
         }
      }
   }

   private List<String> identifiers(Mappings mappings, String key) {
      int size = mappings.size();
      List<String> identifiers = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         int globalId = mappings.getNewId(i);
         identifiers.add(this.identifierFromGlobalId(key, globalId));
      }

      return identifiers;
   }

   public Object2IntMap<String> indexedObjectToMap(JsonObject object) {
      Object2IntMap<String> map = new Object2IntOpenHashMap<>(object.size());
      map.defaultReturnValue(-1);

      for (Entry<String, JsonElement> entry : object.entrySet()) {
         map.put(entry.getValue().getAsString(), Integer.parseInt(entry.getKey()));
      }

      return map;
   }

   public Object2IntMap<String> arrayToMap(JsonArray array) {
      Object2IntMap<String> map = new Object2IntOpenHashMap<>(array.size());
      map.defaultReturnValue(-1);

      for (int i = 0; i < array.size(); i++) {
         map.put(array.get(i).getAsString(), i);
      }

      return map;
   }

   public Logger getLogger() {
      return Via.getPlatform().getLogger();
   }

   public File getDataFolder() {
      return Via.getPlatform().getDataFolder();
   }

   @Nullable
   public InputStream getResource(String name) {
      return this.dataLoaderClass.getClassLoader().getResourceAsStream(this.dataPath + name);
   }

   @FunctionalInterface
   public interface AddConsumer<T> {
      void addTo(T var1, int var2, int var3);
   }

   public record IdentifiersPair(List<String> unmapped, List<String> mapped, boolean identity) {
      public IdentifiersPair(List<String> unmapped, List<String> mapped) {
         this(unmapped, mapped, false);
      }

      public IdentifiersPair(List<String> identifiers) {
         this(identifiers, identifiers, true);
      }
   }

   @FunctionalInterface
   public interface MappingHolderSupplier<T> {
      T get(int var1);
   }

   @FunctionalInterface
   public interface MappingsSupplier<T extends Mappings, V> {
      T create(V var1, int var2);
   }

   private record ValuePairs(int[] first, int[] second) {
   }
}
