package com.viaversion.viaversion.api.minecraft.codec;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import org.checkerframework.checker.nullness.qual.Nullable;

final class RegistryAccessImpl implements CodecContext.RegistryAccess {
   private final EntityTracker entityTracker;
   private final MappingData mappingData;
   private final UserConnection connection;
   private final boolean mapped;

   RegistryAccessImpl(Protocol<?, ?, ?, ?> protocol, UserConnection connection) {
      this.mappingData = (MappingData)Preconditions.checkNotNull(protocol.getMappingData(), "No mapping data present for protocol");
      this.entityTracker = (EntityTracker)Preconditions.checkNotNull(protocol.getEntityRewriter().tracker(connection), "Entity tracker has to be added first");
      this.connection = connection;
      this.mapped = false;
   }

   private RegistryAccessImpl(MappingData mappingData, UserConnection connection, EntityTracker entityTracker, boolean mapped) {
      this.mappingData = mappingData;
      this.connection = connection;
      this.entityTracker = entityTracker;
      this.mapped = mapped;
   }

   @Override
   public UserConnection connection() {
      return this.connection;
   }

   @Override
   public Key item(int id) {
      return this.key(this.mappingData.getFullItemMappings(), id);
   }

   @Override
   public Key attributeModifier(int id) {
      return this.key(this.mappingData.getAttributeMappings(), id);
   }

   @Override
   public Key dataComponentType(int id) {
      return this.key(this.mappingData.getDataComponentSerializerMappings(), id);
   }

   @Override
   public Key entity(int id) {
      return this.key(this.mappingData.getEntityMappings(), id);
   }

   @Override
   public Key blockEntity(int id) {
      return this.key(this.mappingData.getBlockEntityMappings(), id);
   }

   @Override
   public Key sound(int id) {
      return this.key(this.mappingData.getFullSoundMappings(), id);
   }

   @Override
   public Key key(MappingData.MappingType mappingType, int id) {
      return this.key(this.mappingData.getFullMappings(mappingType), id);
   }

   @Override
   public int id(MappingData.MappingType mappingType, String identifier) {
      FullMappings mappings = this.mappingData.getFullMappings(mappingType);
      return this.mapped ? mappings.mappedId(identifier) : mappings.id(identifier);
   }

   @Override
   public Key registryKey(String registry, int id) {
      KeyMappings mappings = this.entityTracker.registryKeys(registry);
      Preconditions.checkNotNull(mappings, "No registry mappings for registry: %s", new Object[]{registry});
      return this.keyOrUnknown(mappings.key(id), id);
   }

   private Key key(FullMappings mappings, int id) {
      Key key = this.mapped ? mappings.mappedKeyFromMappedId(id) : mappings.keyFromId(id);
      return this.keyOrUnknown(key, id);
   }

   private Key keyOrUnknown(@Nullable Key key, int id) {
      return key != null ? key : Key.of("viaversion", "unknown/" + id);
   }

   @Override
   public CodecContext.RegistryAccess withMapped(boolean mapped) {
      return this.mapped == mapped ? this : new RegistryAccessImpl(this.mappingData, this.connection, this.entityTracker, mapped);
   }
}
