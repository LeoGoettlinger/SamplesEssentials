package com.github.retrooper.packetevents.protocol.entity.villager.type;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import java.util.Collection;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class VillagerTypes {
   private static final VersionedRegistry<VillagerType> REGISTRY = new VersionedRegistry<>("villager_type");
   public static final VillagerType DESERT = define("desert");
   public static final VillagerType JUNGLE = define("jungle");
   public static final VillagerType PLAINS = define("plains");
   public static final VillagerType SAVANNA = define("savanna");
   public static final VillagerType SNOW = define("snow");
   public static final VillagerType SWAMP = define("swamp");
   public static final VillagerType TAIGA = define("taiga");

   private VillagerTypes() {
   }

   public static VersionedRegistry<VillagerType> getRegistry() {
      return REGISTRY;
   }

   @Deprecated
   @Internal
   public static VillagerType define(int id, String name) {
      return define(name);
   }

   @Internal
   public static VillagerType define(String name) {
      return REGISTRY.define(name, StaticVillagerType::new);
   }

   @Deprecated
   public static VillagerType getById(int id) {
      ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
      return getById(version.toClientVersion(), id);
   }

   public static VillagerType getById(ClientVersion version, int id) {
      return REGISTRY.getById(version, id);
   }

   public static VillagerType getByName(String name) {
      return REGISTRY.getByName(name);
   }

   public static Collection<VillagerType> values() {
      return REGISTRY.getEntries();
   }

   static {
      REGISTRY.unloadMappings();
   }
}
