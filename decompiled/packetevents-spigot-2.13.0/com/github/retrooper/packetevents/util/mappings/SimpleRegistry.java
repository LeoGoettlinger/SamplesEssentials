package com.github.retrooper.packetevents.util.mappings;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SimpleRegistry<T extends MappedEntity> implements IRegistry<T> {
   private final ResourceLocation registryKey;
   private final Map<String, T> typeMap = new HashMap<>();
   private final Map<Integer, T> typeIdMap = new HashMap<>();
   private final Map<String, Integer> reverseTypeIdMap = new HashMap<>();

   public SimpleRegistry(String registryKey) {
      this(new ResourceLocation(registryKey));
   }

   public SimpleRegistry(ResourceLocation registryKey) {
      this.registryKey = registryKey;
   }

   @Internal
   public <Z extends T> Z define(String name, int id, Z instance) {
      return this.define(new ResourceLocation(name), id, instance);
   }

   @Internal
   public <Z extends T> Z define(ResourceLocation name, int id, Z instance) {
      String nameStr = name.toString();
      this.typeMap.put(nameStr, (T)instance);
      this.typeIdMap.put(id, (T)instance);
      this.reverseTypeIdMap.put(nameStr, id);
      return instance;
   }

   @Nullable
   @Override
   public T getByName(String name) {
      return this.typeMap.get(ResourceLocation.normString(name));
   }

   @Nullable
   @Override
   public T getById(ClientVersion version, int id) {
      return this.typeIdMap.get(id);
   }

   @Override
   public int getId(String entityName, ClientVersion version) {
      String normedName = ResourceLocation.normString(entityName);
      return this.reverseTypeIdMap.getOrDefault(normedName, -1);
   }

   @Override
   public int getId(MappedEntity entity, ClientVersion version) {
      return this.getId(entity.getName().toString(), version);
   }

   @Override
   public Collection<T> getEntries() {
      return Collections.unmodifiableCollection(this.typeMap.values());
   }

   @Override
   public int size() {
      return this.typeMap.size();
   }

   @Override
   public ResourceLocation getRegistryKey() {
      return this.registryKey;
   }

   @Override
   public String toString() {
      return "SimpleRegistry[" + this.registryKey + ']';
   }
}
