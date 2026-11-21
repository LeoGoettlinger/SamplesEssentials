package com.github.retrooper.packetevents.protocol.entity.villager.profession;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticVillagerProfession extends AbstractMappedEntity implements VillagerProfession {
   @Internal
   public StaticVillagerProfession(@Nullable TypesBuilderData data) {
      super(data);
   }

   @Override
   public int getId() {
      ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
      return this.getId(version.toClientVersion());
   }
}
