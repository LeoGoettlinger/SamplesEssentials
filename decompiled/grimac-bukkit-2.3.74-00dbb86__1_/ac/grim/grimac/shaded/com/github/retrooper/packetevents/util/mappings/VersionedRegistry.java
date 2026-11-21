package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntityBuilder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.MapUtil;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.VersionRange;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.jetbrains.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class VersionedRegistry<T extends MappedEntity> implements IRegistry<T> {
   private static final String REGISTRY_MAPPINGS_PREFIX = "registries/";
   private final ResourceLocation registryKey;
   private final TypesBuilder typesBuilder;
   private final ClientVersion[] extraSteps;
   private final Map<String, T>[] typeNames;
   private final Map<Integer, T>[] typeIds;
   private final Set<T> entries = new HashSet<>();

   public VersionedRegistry(String registry) {
      this(registry);
   }

   public VersionedRegistry(String registry, ClientVersion... extraSteps) {
      this(registry, "registries/" + registry, extraSteps);
   }

   public VersionedRegistry(String registry, String mappingsPath) {
      this(registry, mappingsPath);
   }

   public VersionedRegistry(String registry, String mappingsPath, ClientVersion... extraSteps) {
      this(new ResourceLocation(registry), mappingsPath, extraSteps);
   }

   public VersionedRegistry(ResourceLocation registryKey, String mappingsPath) {
      this(registryKey, mappingsPath);
   }

   public VersionedRegistry(ResourceLocation registryKey, String mappingsPath, ClientVersion... extraSteps) {
      this.registryKey = registryKey;
      this.typesBuilder = new TypesBuilder(mappingsPath);
      this.extraSteps = extraSteps;
      this.postLoadMappings();
      int versions = this.typesBuilder.getVersionMapper().size();
      this.typeNames = new Map[versions];
      this.typeIds = new Map[versions];
   }

   @ApiStatus.Internal
   public <Z extends T> Z defineWithBuilder(String name, MappedEntityBuilder<Z> builder) {
      return this.define(name, VersionRange.ALL_VERSIONS, builder::build);
   }

   @ApiStatus.Internal
   public <Z extends T> Z define(String name, Function<TypesBuilderData, Z> builder) {
      return this.define(name, VersionRange.ALL_VERSIONS, builder);
   }

   @ApiStatus.Internal
   public <Z extends T> Z define(String name, VersionRange range, Function<TypesBuilderData, Z> builder) {
      TypesBuilderData typeData = this.typesBuilder.define(name, range);
      Z instance = (Z)builder.apply(typeData);
      MappingHelper.registerMapping(this.typesBuilder, this.typeNames, this.typeIds, typeData, (T)instance);
      return instance;
   }

   @VisibleForTesting
   @ApiStatus.Internal
   public TypesBuilder getTypesBuilder() {
      return this.typesBuilder;
   }

   @VisibleForTesting
   @ApiStatus.Internal
   public void postLoadMappings() {
      this.typesBuilder.registry = this;

      for (ClientVersion extraStep : this.extraSteps) {
         this.typesBuilder.addExtraVersionStep(extraStep);
      }
   }

   @ApiStatus.Internal
   public void unloadMappings() {
      this.typesBuilder.unloadFileMappings();
      Map<String, T> lastNameMap = this.typeNames[0];

      for (int i = 1; i < this.typeNames.length; i++) {
         Map<String, T> nameMap = this.typeNames[i];
         if (MapUtil.isDeepEqual(lastNameMap, nameMap)) {
            this.typeNames[i] = lastNameMap;
         } else {
            lastNameMap = nameMap;
         }
      }

      Set<String> entryNames = new HashSet<>();

      for (int ix = this.typeNames.length - 1; ix >= 0; ix--) {
         for (Entry<String, T> entry : this.typeNames[ix].entrySet()) {
            if (entryNames.add(entry.getKey())) {
               this.entries.add(entry.getValue());
            }
         }
      }
   }

   @Nullable
   @Override
   public T getByName(ClientVersion version, ResourceLocation name) {
      int index = this.typesBuilder.getDataIndex(version);
      return this.typeNames[index].get(name.toString());
   }

   @Nullable
   @Override
   public T getByName(ClientVersion version, String name) {
      int index = this.typesBuilder.getDataIndex(version);
      return this.typeNames[index].get(ResourceLocation.normString(name));
   }

   @Nullable
   @Override
   public T getByName(ResourceLocation name) {
      ClientVersion version = PacketEvents.getAPI().getServerManager().getVersion().toClientVersion();
      return this.getByName(version, name);
   }

   @Nullable
   @Override
   public T getByName(String name) {
      ClientVersion version = PacketEvents.getAPI().getServerManager().getVersion().toClientVersion();
      return this.getByName(version, name);
   }

   @Nullable
   @Override
   public T getById(ClientVersion version, int id) {
      int index = this.typesBuilder.getDataIndex(version);
      return this.typeIds[index].get(id);
   }

   @Override
   public int getId(MappedEntity entity, ClientVersion version) {
      return entity.getId(version);
   }

   @Override
   public Collection<T> getEntries() {
      return Collections.unmodifiableCollection(this.entries);
   }

   @Override
   public int size() {
      return this.entries.size();
   }

   @Override
   public ResourceLocation getRegistryKey() {
      return this.registryKey;
   }

   @Override
   public String toString() {
      return "VersionedRegistry[" + this.registryKey + ']';
   }
}
