package com.github.retrooper.packetevents.protocol.mapper;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

@Deprecated
public class GenericMappedEntity implements StaticMappedEntity {
   private final int id;

   public GenericMappedEntity(ClientVersion ignoredVersion, int id) {
      this(id);
   }

   public GenericMappedEntity(int id) {
      this.id = id;
   }

   public static GenericMappedEntity getById(ClientVersion version, int id) {
      return new GenericMappedEntity(version, id);
   }

   public static GenericMappedEntity read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(GenericMappedEntity::new);
   }

   public static void write(PacketWrapper<?> wrapper, GenericMappedEntity entity) {
      wrapper.writeMappedEntity(entity);
   }

   @Override
   public int getId() {
      return this.id;
   }

   @Override
   public ResourceLocation getName() {
      throw new UnsupportedOperationException();
   }
}
