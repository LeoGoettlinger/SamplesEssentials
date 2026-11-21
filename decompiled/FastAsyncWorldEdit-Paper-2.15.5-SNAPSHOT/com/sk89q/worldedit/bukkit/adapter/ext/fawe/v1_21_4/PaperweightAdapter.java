package com.sk89q.worldedit.bukkit.adapter.ext.fawe.v1_21_4;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_4.PaperweightFaweAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeCategory;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import com.sk89q.worldedit.world.item.ItemType;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.CoralTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.PrimaryLevelData.SpecialWorldProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.spigotmc.SpigotConfig;
import org.spigotmc.WatchdogThread;

public final class PaperweightAdapter implements BukkitImplAdapter<Tag> {
   private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
   private final Field serverWorldsField;
   private final Method getChunkFutureMethod;
   private final Field chunkProviderExecutorField;
   private final PaperweightDataConverters dataFixer;
   private final Watchdog watchdog;
   private static final RandomSource random = RandomSource.create();
   private static final HashMap<BiomeType, Holder<Biome>> biomeTypeToNMSCache = new HashMap<>();
   private static final HashMap<Holder<Biome>, BiomeType> biomeTypeFromNMSCache = new HashMap<>();
   private static final LoadingCache<net.minecraft.world.level.block.state.properties.Property<?>, Property<?>> PROPERTY_CACHE = CacheBuilder.newBuilder()
      .build(CacheLoader.from(PaperweightFaweAdapter::adaptProperty));
   private static final Codec<DataComponentPatch> COMPONENTS_CODEC = DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).codec();
   private final LoadingCache<ServerLevel, PaperweightFakePlayer> fakePlayers = CacheBuilder.newBuilder()
      .weakKeys()
      .softValues()
      .build(CacheLoader.from(PaperweightFakePlayer::new));
   private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS = Sets.immutableEnumSet(
      SideEffect.HISTORY, new SideEffect[]{SideEffect.HEIGHTMAPS, SideEffect.LIGHTING, SideEffect.NEIGHBORS, SideEffect.ENTITY_EVENTS}
   );

   public PaperweightAdapter() throws NoSuchFieldException, NoSuchMethodException {
      CraftServer.class.cast(Bukkit.getServer());
      int dataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
      if (dataVersion != 4189) {
         throw new UnsupportedClassVersionError("Not 1.21.4!");
      } else {
         this.serverWorldsField = CraftServer.class.getDeclaredField("worlds");
         this.serverWorldsField.setAccessible(true);
         this.getChunkFutureMethod = ServerChunkCache.class
            .getDeclaredMethod(StaticRefraction.GET_CHUNK_FUTURE_MAIN_THREAD, int.class, int.class, ChunkStatus.class, boolean.class);
         this.getChunkFutureMethod.setAccessible(true);
         this.chunkProviderExecutorField = ServerChunkCache.class.getDeclaredField(StaticRefraction.MAIN_THREAD_PROCESSOR);
         this.chunkProviderExecutorField.setAccessible(true);
         this.dataFixer = new PaperweightDataConverters(dataVersion, this);

         Watchdog watchdog;
         try {
            Class.forName("org.spigotmc.WatchdogThread");
            watchdog = new PaperweightAdapter.SpigotWatchdog();
         } catch (NoSuchFieldException | ClassNotFoundException var7) {
            try {
               watchdog = new PaperweightAdapter.MojangWatchdog(((CraftServer)Bukkit.getServer()).getServer());
            } catch (NoSuchFieldException var6) {
               watchdog = null;
            }
         }

         this.watchdog = watchdog;

         try {
            Class.forName("org.spigotmc.SpigotConfig");
            SpigotConfig.config.set("world-settings.worldeditregentempworld.verbose", false);
         } catch (ClassNotFoundException var5) {
         }
      }
   }

   @Override
   public DataFixer getDataFixer() {
      return this.dataFixer;
   }

   static void readTagIntoTileEntity(CompoundTag tag, BlockEntity tileEntity) {
      tileEntity.loadWithComponents(tag, MinecraftServer.getServer().registryAccess());
      tileEntity.setChanged();
   }

   private static String getEntityId(Entity entity) {
      return EntityType.getKey(entity.getType()).toString();
   }

   @Nullable
   private static Entity createEntityFromId(String id, Level world) {
      return EntityType.byString(id).map(t -> t.create(world, EntitySpawnReason.COMMAND)).orElse(null);
   }

   private static void readTagIntoEntity(CompoundTag tag, Entity entity) {
      entity.load(tag);
   }

   private static boolean readEntityIntoTag(Entity entity, CompoundTag tag) {
      return entity.save(tag);
   }

   private static Block getBlockFromType(BlockType blockType) {
      return (Block)DedicatedServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK).getValue(ResourceLocation.tryParse(blockType.id()));
   }

   private static Item getItemFromType(ItemType itemType) {
      return (Item)DedicatedServer.getServer().registryAccess().lookupOrThrow(Registries.ITEM).getValue(ResourceLocation.tryParse(itemType.id()));
   }

   @Override
   public OptionalInt getInternalBlockStateId(BlockData data) {
      BlockState state = ((CraftBlockData)data).getState();
      int combinedId = Block.getId(state);
      return combinedId == 0 && state.getBlock() != Blocks.AIR ? OptionalInt.empty() : OptionalInt.of(combinedId);
   }

   @Override
   public OptionalInt getInternalBlockStateId(com.sk89q.worldedit.world.block.BlockState state) {
      Block mcBlock = getBlockFromType(state.getBlockType());
      BlockState newState = mcBlock.defaultBlockState();
      Map<Property<?>, Object> states = state.getStates();
      newState = this.applyProperties(mcBlock.getStateDefinition(), newState, states);
      int combinedId = Block.getId(newState);
      return combinedId == 0 && state.getBlockType() != BlockTypes.AIR ? OptionalInt.empty() : OptionalInt.of(combinedId);
   }

   public com.sk89q.worldedit.world.block.BlockState adapt(BlockState blockState) {
      int internalId = Block.getId(blockState);
      com.sk89q.worldedit.world.block.BlockState state = BlockStateIdAccess.getBlockStateById(internalId);
      if (state == null) {
         state = BukkitAdapter.adapt(CraftBlockData.createData(blockState));
      }

      return state;
   }

   public BiomeType adapt(Biome biome) {
      ResourceLocation mcBiome = ((CraftServer)Bukkit.getServer()).getServer().registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
      return mcBiome == null ? null : BiomeType.REGISTRY.get(mcBiome.toString());
   }

   public BlockState adapt(com.sk89q.worldedit.world.block.BlockState blockState) {
      int internalId = BlockStateIdAccess.getBlockStateId(blockState);
      return Block.stateById(internalId);
   }

   @Override
   public com.sk89q.worldedit.world.block.BlockState getBlock(Location location) {
      Preconditions.checkNotNull(location);
      CraftWorld craftWorld = (CraftWorld)location.getWorld();
      int x = location.getBlockX();
      int y = location.getBlockY();
      int z = location.getBlockZ();
      ServerLevel handle = craftWorld.getHandle();
      LevelChunk chunk = handle.getChunk(x >> 4, z >> 4);
      BlockPos blockPos = new BlockPos(x, y, z);
      BlockState blockData = chunk.getBlockState(blockPos);
      return this.adapt(blockData);
   }

   @Override
   public BaseBlock getFullBlock(Location location) {
      com.sk89q.worldedit.world.block.BlockState state = this.getBlock(location);
      CraftWorld craftWorld = (CraftWorld)location.getWorld();
      int x = location.getBlockX();
      int y = location.getBlockY();
      int z = location.getBlockZ();
      ServerLevel handle = craftWorld.getHandle();
      LevelChunk chunk = handle.getChunk(x >> 4, z >> 4);
      BlockPos blockPos = new BlockPos(x, y, z);
      BlockEntity te = chunk.getBlockEntity(blockPos);
      if (te != null) {
         CompoundTag tag = te.saveWithId(MinecraftServer.getServer().registryAccess());
         return state.toBaseBlock(LazyReference.from(() -> (LinCompoundTag)this.toNativeLin((Tag)tag)));
      } else {
         return state.toBaseBlock();
      }
   }

   @Override
   public BiomeType getBiome(Location location) {
      Preconditions.checkNotNull(location);
      CraftWorld craftWorld = (CraftWorld)location.getWorld();
      int x = location.getBlockX();
      int y = location.getBlockY();
      int z = location.getBlockZ();
      ServerLevel handle = craftWorld.getHandle();
      LevelChunk chunk = handle.getChunk(x >> 4, z >> 4);
      return biomeTypeFromNMSCache.computeIfAbsent(
         chunk.getNoiseBiome(x >> 2, y >> 2, z >> 2), b -> BiomeType.REGISTRY.get(((ResourceKey)b.unwrapKey().orElseThrow()).location().toString())
      );
   }

   @Override
   public void setBiome(Location location, BiomeType biome) {
      Preconditions.checkNotNull(location);
      Preconditions.checkNotNull(biome);
      CraftWorld craftWorld = (CraftWorld)location.getWorld();
      int x = location.getBlockX();
      int y = location.getBlockY();
      int z = location.getBlockZ();
      ServerLevel handle = craftWorld.getHandle();
      LevelChunk chunk = handle.getChunk(x >> 4, z >> 4);
      chunk.setBiome(
         x >> 2,
         y >> 2,
         z >> 2,
         biomeTypeToNMSCache.computeIfAbsent(
            biome,
            b -> handle.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(b.id())))
         )
      );
      chunk.markUnsaved();
   }

   @Override
   public WorldNativeAccess<?, ?, ?> createWorldNativeAccess(World world) {
      return new PaperweightWorldNativeAccess(this, new WeakReference<>(((CraftWorld)world).getHandle()));
   }

   private static Direction adapt(com.sk89q.worldedit.util.Direction face) {
      return switch (face) {
         case NORTH -> Direction.NORTH;
         case SOUTH -> Direction.SOUTH;
         case WEST -> Direction.WEST;
         case EAST -> Direction.EAST;
         case DOWN -> Direction.DOWN;
         default -> Direction.UP;
      };
   }

   private BlockState applyProperties(StateDefinition<Block, BlockState> stateContainer, BlockState newState, Map<Property<?>, Object> states) {
      for (Entry<Property<?>, Object> state : states.entrySet()) {
         net.minecraft.world.level.block.state.properties.Property<?> property = stateContainer.getProperty(state.getKey().getName());
         Comparable<?> value = (Comparable<?>)state.getValue();
         if (property instanceof EnumProperty enumProperty) {
            if (enumProperty.getValueClass() == Direction.class) {
               value = adapt((com.sk89q.worldedit.util.Direction)value);
            } else {
               String enumName = (String)value;
               value = (Comparable<?>)((EnumProperty)property)
                  .getValue(enumName)
                  .orElseThrow(() -> new IllegalStateException("Enum property " + property.getName() + " does not contain " + enumName));
            }
         }

         newState = (BlockState)newState.setValue(property, value);
      }

      return newState;
   }

   @Override
   public BaseEntity getEntity(org.bukkit.entity.Entity entity) {
      Preconditions.checkNotNull(entity);
      CraftEntity craftEntity = (CraftEntity)entity;
      Entity mcEntity = craftEntity.getHandle();
      String id = getEntityId(mcEntity);
      CompoundTag tag = new CompoundTag();
      return !readEntityIntoTag(mcEntity, tag)
         ? null
         : new BaseEntity(EntityTypes.get(id), LazyReference.from(() -> (LinCompoundTag)this.toNativeLin((Tag)tag)));
   }

   @Override
   public org.bukkit.entity.Entity createEntity(Location location, BaseEntity state) {
      Preconditions.checkNotNull(location);
      Preconditions.checkNotNull(state);
      CraftWorld craftWorld = (CraftWorld)location.getWorld();
      ServerLevel worldServer = craftWorld.getHandle();
      Entity createdEntity = createEntityFromId(state.getType().id(), craftWorld.getHandle());
      if (createdEntity == null) {
         return null;
      } else {
         LinCompoundTag nativeTag = state.getNbt();
         if (nativeTag != null) {
            CompoundTag tag = (CompoundTag)this.fromNativeLin(nativeTag);

            for (String name : Constants.NO_COPY_ENTITY_NBT_FIELDS) {
               tag.remove(name);
            }

            readTagIntoEntity(tag, createdEntity);
         }

         createdEntity.absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
         worldServer.addFreshEntity(createdEntity, SpawnReason.CUSTOM);
         return createdEntity.getBukkitEntity();
      }
   }

   @Override
   public Component getRichBlockName(BlockType blockType) {
      return TranslatableComponent.of(getBlockFromType(blockType).getDescriptionId());
   }

   @Override
   public Component getRichItemName(ItemType itemType) {
      return TranslatableComponent.of(getItemFromType(itemType).getDescriptionId());
   }

   @Override
   public Component getRichItemName(BaseItemStack itemStack) {
      return TranslatableComponent.of(CraftItemStack.asNMSCopy(BukkitAdapter.adapt(itemStack)).getItem().getDescriptionId());
   }

   @Override
   public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
      Map<String, Property<?>> properties = new TreeMap<>();
      Block block = getBlockFromType(blockType);
      StateDefinition<Block, BlockState> blockStateList = block.getStateDefinition();

      for (net.minecraft.world.level.block.state.properties.Property state : blockStateList.getProperties()) {
         Property<?> property = (Property<?>)PROPERTY_CACHE.getUnchecked(state);
         properties.put(property.getName(), property);
      }

      return properties;
   }

   @Override
   public void sendFakeNBT(Player player, BlockVector3 pos, LinCompoundTag nbtData) {
      StructureBlockEntity structureBlock = new StructureBlockEntity(new BlockPos(pos.x(), pos.y(), pos.z()), Blocks.STRUCTURE_BLOCK.defaultBlockState());
      structureBlock.setLevel(((CraftPlayer)player).getHandle().level());
      ((CraftPlayer)player)
         .getHandle()
         .connection
         .send(ClientboundBlockEntityDataPacket.create(structureBlock, (blockEntity, registryAccess) -> (CompoundTag)this.fromNativeLin(nbtData)));
   }

   @Override
   public void sendFakeNBT(Player player, BlockVector3 pos, TileState tileState, LinCompoundTag nbtData) {
      CraftBlockEntityState<?> craftState = (CraftBlockEntityState<?>)tileState;
      CompoundTag vanillaNBT = (CompoundTag)this.fromNativeLin(nbtData);
      ((CraftPlayer)player)
         .getHandle()
         .connection
         .send(new ClientboundBlockEntityDataPacket(new BlockPos(pos.x(), pos.y(), pos.z()), craftState.getTileEntity().getType(), vanillaNBT));
   }

   @Override
   public void sendFakeOP(Player player) {
      ((CraftPlayer)player).getHandle().connection.send(new ClientboundEntityEventPacket(((CraftPlayer)player).getHandle(), (byte)28));
   }

   @Override
   public ItemStack adapt(BaseItemStack baseItemStack) {
      Frozen registryAccess = DedicatedServer.getServer().registryAccess();
      net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(
         registryAccess.lookupOrThrow(Registries.ITEM).getOrThrow(ResourceKey.create(Registries.ITEM, ResourceLocation.parse(baseItemStack.getType().id()))),
         baseItemStack.getAmount()
      );
      LinCompoundTag nbt = baseItemStack.getNbt();
      if (nbt != null) {
         DataComponentPatch componentPatch = (DataComponentPatch)COMPONENTS_CODEC.parse(
               registryAccess.createSerializationContext(NbtOps.INSTANCE), this.fromNativeLin(nbt)
            )
            .getOrThrow();
         stack.applyComponents(componentPatch);
      }

      return CraftItemStack.asCraftMirror(stack);
   }

   @Override
   public BaseItemStack adapt(ItemStack itemStack) {
      Frozen registryAccess = DedicatedServer.getServer().registryAccess();
      net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
      CompoundTag tag = (CompoundTag)COMPONENTS_CODEC.encodeStart(registryAccess.createSerializationContext(NbtOps.INSTANCE), nmsStack.getComponentsPatch())
         .getOrThrow();
      return new BaseItemStack(
         BukkitAdapter.asItemType(itemStack.getType()), LazyReference.from(() -> (LinCompoundTag)this.toNativeLin((Tag)tag)), itemStack.getAmount()
      );
   }

   @Override
   public boolean simulateItemUse(World world, BlockVector3 position, BaseItem item, com.sk89q.worldedit.util.Direction face) {
      CraftWorld craftWorld = (CraftWorld)world;
      ServerLevel worldServer = craftWorld.getHandle();
      net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(
         this.adapt(item instanceof BaseItemStack ? (BaseItemStack)item : new BaseItemStack(item.getType(), item.getNbtReference(), 1))
      );

      PaperweightFakePlayer fakePlayer;
      try {
         fakePlayer = (PaperweightFakePlayer)this.fakePlayers.get(worldServer);
      } catch (ExecutionException var15) {
         return false;
      }

      fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);
      fakePlayer.absMoveTo(position.x(), position.y(), position.z(), (float)face.toVector().toYaw(), (float)face.toVector().toPitch());
      BlockPos blockPos = new BlockPos(position.x(), position.y(), position.z());
      Vec3 blockVec = Vec3.atLowerCornerOf(blockPos);
      Direction enumFacing = adapt(face);
      BlockHitResult rayTrace = new BlockHitResult(blockVec, enumFacing, blockPos, false);
      UseOnContext context = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, rayTrace);
      InteractionResult result = stack.useOn(context);
      if (result != InteractionResult.SUCCESS) {
         if (worldServer.getBlockState(blockPos).useItemOn(stack, worldServer, fakePlayer, InteractionHand.MAIN_HAND, rayTrace).consumesAction()) {
            result = InteractionResult.SUCCESS;
         } else {
            result = stack.getItem().use(worldServer, fakePlayer, InteractionHand.MAIN_HAND);
         }
      }

      return result == InteractionResult.SUCCESS;
   }

   @Override
   public boolean canPlaceAt(World world, BlockVector3 position, com.sk89q.worldedit.world.block.BlockState blockState) {
      int internalId = BlockStateIdAccess.getBlockStateId(blockState);
      BlockState blockData = Block.stateById(internalId);
      return blockData.canSurvive(((CraftWorld)world).getHandle(), new BlockPos(position.x(), position.y(), position.z()));
   }

   @Override
   public boolean regenerate(World bukkitWorld, Region region, Extent extent, RegenOptions options) {
      try {
         this.doRegen(bukkitWorld, region, extent, options);
         return true;
      } catch (Exception var6) {
         throw new IllegalStateException("Regen failed.", var6);
      }
   }

   private void doRegen(World bukkitWorld, Region region, Extent extent, RegenOptions options) throws Exception {
      Environment env = bukkitWorld.getEnvironment();
      ChunkGenerator gen = bukkitWorld.getGenerator();
      Path tempDir = Files.createTempDirectory("WorldEditWorldGen");
      LevelStorageSource levelStorage = LevelStorageSource.createDefault(tempDir);
      ResourceKey<LevelStem> worldDimKey = this.getWorldDimKey(env);

      try {
         LevelStorageAccess session = levelStorage.createAccess("worldeditregentempworld", worldDimKey);

         try {
            ServerLevel originalWorld = ((CraftWorld)bukkitWorld).getHandle();
            PrimaryLevelData levelProperties = (PrimaryLevelData)originalWorld.getServer().getWorldData().overworldData();
            WorldOptions originalOpts = levelProperties.worldGenOptions();
            long seed = options.getSeed().orElse(originalWorld.getSeed());
            WorldOptions newOpts = options.getSeed().isPresent() ? originalOpts.withSeed(OptionalLong.of(seed)) : originalOpts;
            LevelSettings newWorldSettings = new LevelSettings(
               "worldeditregentempworld",
               levelProperties.settings.gameType(),
               levelProperties.settings.hardcore(),
               levelProperties.settings.difficulty(),
               levelProperties.settings.allowCommands(),
               levelProperties.settings.gameRules(),
               levelProperties.settings.getDataConfiguration()
            );
            SpecialWorldProperty specialWorldProperty = levelProperties.isFlatWorld()
               ? SpecialWorldProperty.FLAT
               : (levelProperties.isDebugWorld() ? SpecialWorldProperty.DEBUG : SpecialWorldProperty.NONE);
            PrimaryLevelData newWorldData = new PrimaryLevelData(newWorldSettings, newOpts, specialWorldProperty, Lifecycle.stable());
            ServerLevel freshWorld = new ServerLevel(
               originalWorld.getServer(),
               originalWorld.getServer().executor,
               session,
               newWorldData,
               originalWorld.dimension(),
               new LevelStem(originalWorld.dimensionTypeRegistration(), originalWorld.getChunkSource().getGenerator()),
               new PaperweightAdapter.NoOpWorldLoadListener(),
               originalWorld.isDebug(),
               seed,
               ImmutableList.of(),
               false,
               originalWorld.getRandomSequences(),
               env,
               gen,
               bukkitWorld.getBiomeProvider()
            );

            try {
               this.regenForWorld(region, extent, freshWorld, options);
            } finally {
               freshWorld.getChunkSource().close(false);
            }
         } catch (Throwable var38) {
            if (session != null) {
               try {
                  session.close();
               } catch (Throwable var36) {
                  var38.addSuppressed(var36);
               }
            }

            throw var38;
         }

         if (session != null) {
            session.close();
         }
      } finally {
         try {
            Map<String, World> map = (Map<String, World>)this.serverWorldsField.get(Bukkit.getServer());
            map.remove("worldeditregentempworld");
         } catch (IllegalAccessException var35) {
         }

         SafeFiles.tryHardToDeleteDir(tempDir);
      }
   }

   private BiomeType adapt(ServerLevel serverWorld, Biome origBiome) {
      ResourceLocation key = serverWorld.registryAccess().lookupOrThrow(Registries.BIOME).getKey(origBiome);
      return key == null ? null : BiomeTypes.get(key.toString());
   }

   private void regenForWorld(Region region, Extent extent, ServerLevel serverWorld, RegenOptions options) throws WorldEditException {
      List<CompletableFuture<ChunkAccess>> chunkLoadings = this.submitChunkLoadTasks(region, serverWorld);

      BlockableEventLoop<Runnable> executor;
      try {
         executor = (BlockableEventLoop<Runnable>)this.chunkProviderExecutorField.get(serverWorld.getChunkSource());
      } catch (IllegalAccessException var18) {
         throw new IllegalStateException("Couldn't get executor for chunk loading.", var18);
      }

      executor.managedBlock(
         () -> chunkLoadings.stream().anyMatch(ftr -> ftr.isDone() && Futures.getUnchecked(ftr) == null)
            ? false
            : chunkLoadings.stream().allMatch(CompletableFuture::isDone)
      );
      Map<ChunkPos, ChunkAccess> chunks = new HashMap<>();

      for (CompletableFuture<ChunkAccess> future : chunkLoadings) {
         ChunkAccess chunk = future.getNow(null);
         Preconditions.checkState(chunk != null, "Failed to generate a chunk, regen failed.");
         chunks.put(chunk.getPos(), chunk);
      }

      for (BlockVector3 vec : region) {
         BlockPos pos = new BlockPos(vec.x(), vec.y(), vec.z());
         ChunkAccess chunk = chunks.get(new ChunkPos(pos));
         BlockState blockData = chunk.getBlockState(pos);
         int internalId = Block.getId(blockData);
         BlockStateHolder<?> state = BlockStateIdAccess.getBlockStateById(internalId);
         Objects.requireNonNull(state);
         BlockEntity blockEntity = chunk.getBlockEntity(pos);
         if (blockEntity != null) {
            CompoundTag tag = blockEntity.saveWithId(serverWorld.registryAccess());
            state = state.toBaseBlock(LazyReference.from(() -> (LinCompoundTag)this.toNativeLin((Tag)tag)));
         }

         extent.setBlock(vec, state.toBaseBlock());
         if (options.shouldRegenBiomes()) {
            Biome origBiome = (Biome)chunk.getNoiseBiome(vec.x(), vec.y(), vec.z()).value();
            BiomeType adaptedBiome = this.adapt(serverWorld, origBiome);
            if (adaptedBiome != null) {
               extent.setBiome(vec, adaptedBiome);
            }
         }
      }
   }

   private List<CompletableFuture<ChunkAccess>> submitChunkLoadTasks(Region region, ServerLevel serverWorld) {
      ServerChunkCache chunkManager = serverWorld.getChunkSource();
      List<CompletableFuture<ChunkAccess>> chunkLoadings = new ArrayList<>();

      for (BlockVector2 chunk : region.getChunks()) {
         try {
            chunkLoadings.add(
               ((CompletableFuture)this.getChunkFutureMethod.invoke(chunkManager, chunk.x(), chunk.z(), ChunkStatus.FEATURES, true))
                  .thenApply(either -> (ChunkAccess)either.orElse(null))
            );
         } catch (InvocationTargetException | IllegalAccessException var8) {
            throw new IllegalStateException("Couldn't load chunk for regen.", var8);
         }
      }

      return chunkLoadings;
   }

   private ResourceKey<LevelStem> getWorldDimKey(Environment env) {
      switch (env) {
         case NETHER:
            return LevelStem.NETHER;
         case THE_END:
            return LevelStem.END;
         case NORMAL:
         default:
            return LevelStem.OVERWORLD;
      }
   }

   @Override
   public Set<SideEffect> getSupportedSideEffects() {
      return SUPPORTED_SIDE_EFFECTS;
   }

   @Override
   public boolean clearContainerBlockContents(World world, BlockVector3 pt) {
      ServerLevel originalWorld = ((CraftWorld)world).getHandle();
      BlockEntity entity = originalWorld.getBlockEntity(new BlockPos(pt.x(), pt.y(), pt.z()));
      if (entity instanceof Clearable) {
         ((Clearable)entity).clearContent();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void initializeRegistries() {
      DedicatedServer server = ((CraftServer)Bukkit.getServer()).getServer();

      for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.BIOME).keySet()) {
         if (BiomeType.REGISTRY.get(name.toString()) == null) {
            BiomeType.REGISTRY.register(name.toString(), new BiomeType(name.toString()));
         }
      }

      for (ResourceLocation namex : server.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).keySet()) {
         if (ConfiguredFeatureType.REGISTRY.get(namex.toString()) == null) {
            ConfiguredFeatureType.REGISTRY.register(namex.toString(), new ConfiguredFeatureType(namex.toString()));
         }
      }

      for (ResourceLocation namexx : server.registryAccess().lookupOrThrow(Registries.STRUCTURE).keySet()) {
         if (StructureType.REGISTRY.get(namexx.toString()) == null) {
            StructureType.REGISTRY.register(namexx.toString(), new StructureType(namexx.toString()));
         }
      }

      Registry<PlacedFeature> placedFeatureRegistry = server.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);

      for (ResourceLocation namexxx : placedFeatureRegistry.keySet()) {
         Feature<?> underlyingFeature = ((ConfiguredFeature)((PlacedFeature)((Reference)placedFeatureRegistry.get(namexxx).get()).value()).feature().value())
            .feature();
         if (underlyingFeature instanceof TreeFeature || underlyingFeature instanceof CoralTreeFeature) {
            String key = namexxx.toString();
            if (TreeType.REGISTRY.get(key) == null) {
               TreeType.REGISTRY.register(key, new TreeType(key));
            }
         }
      }

      Registry<Biome> biomeRegistry = server.registryAccess().lookupOrThrow(Registries.BIOME);
      biomeRegistry.getTags()
         .forEach(
            tag -> {
               String keyx = tag.key().location().toString();
               if (BiomeCategory.REGISTRY.get(keyx) == null) {
                  BiomeCategory.REGISTRY
                     .register(
                        keyx,
                        new BiomeCategory(
                           keyx,
                           () -> biomeRegistry.get(tag.key())
                              .stream()
                              .flatMap(ListBacked::stream)
                              .<Biome>map(Holder::value)
                              .map(this::adapt)
                              .collect(Collectors.toSet())
                        )
                     );
               }
            }
         );
   }

   @Override
   public void sendBiomeUpdates(World world, Iterable<BlockVector2> chunks) {
      ServerLevel originalWorld = ((CraftWorld)world).getHandle();
      List<ChunkAccess> nativeChunks = chunks instanceof Collection<BlockVector2> chunkCollection
         ? Lists.newArrayListWithCapacity(chunkCollection.size())
         : Lists.newArrayList();

      for (BlockVector2 chunk : chunks) {
         nativeChunks.add(originalWorld.getChunk(chunk.x(), chunk.z(), ChunkStatus.BIOMES, false));
      }

      originalWorld.getChunkSource().chunkMap.resendBiomesForChunks(nativeChunks);
   }

   @Override
   public boolean generateTree(TreeType treeType, World world, EditSession session, BlockVector3 pt) throws MaxChangedBlocksException {
      ServerLevel originalWorld = ((CraftWorld)world).getHandle();
      PlacedFeature feature = (PlacedFeature)originalWorld.registryAccess()
         .lookupOrThrow(Registries.PLACED_FEATURE)
         .getValue(ResourceLocation.tryParse(treeType.id()));
      ServerChunkCache chunkManager = originalWorld.getChunkSource();

      boolean var9;
      try (PaperweightServerLevelDelegateProxy.LevelAndProxy proxyLevel = PaperweightServerLevelDelegateProxy.newInstance(session, originalWorld, this)) {
         var9 = feature != null && feature.place(proxyLevel.level(), chunkManager.getGenerator(), random, new BlockPos(pt.x(), pt.y(), pt.z()));
      }

      return var9;
   }

   @Override
   public boolean generateFeature(ConfiguredFeatureType type, World world, EditSession session, BlockVector3 pt) {
      ServerLevel originalWorld = ((CraftWorld)world).getHandle();
      ConfiguredFeature<?, ?> feature = (ConfiguredFeature<?, ?>)originalWorld.registryAccess()
         .lookupOrThrow(Registries.CONFIGURED_FEATURE)
         .getValue(ResourceLocation.tryParse(type.id()));
      ServerChunkCache chunkManager = originalWorld.getChunkSource();

      try {
         boolean var9;
         try (PaperweightServerLevelDelegateProxy.LevelAndProxy proxyLevel = PaperweightServerLevelDelegateProxy.newInstance(session, originalWorld, this)) {
            var9 = feature != null && feature.place(proxyLevel.level(), chunkManager.getGenerator(), random, new BlockPos(pt.x(), pt.y(), pt.z()));
         }

         return var9;
      } catch (MaxChangedBlocksException var13) {
         throw new RuntimeException(var13);
      }
   }

   @Override
   public boolean generateStructure(StructureType type, World world, EditSession session, BlockVector3 pt) {
      ServerLevel originalWorld = ((CraftWorld)world).getHandle();
      Registry<Structure> structureRegistry = originalWorld.registryAccess().lookupOrThrow(Registries.STRUCTURE);
      Structure structure = (Structure)structureRegistry.getValue(ResourceLocation.tryParse(type.id()));
      if (structure == null) {
         return false;
      } else {
         ServerChunkCache chunkManager = originalWorld.getChunkSource();

         try {
            boolean var15;
            try (PaperweightServerLevelDelegateProxy.LevelAndProxy proxyLevel = PaperweightServerLevelDelegateProxy.newInstance(session, originalWorld, this)) {
               ChunkPos chunkPos = new ChunkPos(new BlockPos(pt.x(), pt.y(), pt.z()));
               StructureStart structureStart = structure.generate(
                  structureRegistry.wrapAsHolder(structure),
                  originalWorld.dimension(),
                  originalWorld.registryAccess(),
                  chunkManager.getGenerator(),
                  chunkManager.getGenerator().getBiomeSource(),
                  chunkManager.randomState(),
                  originalWorld.getStructureManager(),
                  originalWorld.getSeed(),
                  chunkPos,
                  0,
                  proxyLevel.level(),
                  biome -> true
               );
               if (!structureStart.isValid()) {
                  return false;
               }

               BoundingBox boundingBox = structureStart.getBoundingBox();
               ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
               ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
               ChunkPos.rangeClosed(min, max)
                  .forEach(
                     chunkPosx -> structureStart.placeInChunk(
                        proxyLevel.level(),
                        originalWorld.structureManager(),
                        chunkManager.getGenerator(),
                        originalWorld.getRandom(),
                        new BoundingBox(
                           chunkPosx.getMinBlockX(),
                           originalWorld.getMinY(),
                           chunkPosx.getMinBlockZ(),
                           chunkPosx.getMaxBlockX(),
                           originalWorld.getMaxY(),
                           chunkPosx.getMaxBlockZ()
                        ),
                        chunkPosx
                     )
                  );
               var15 = true;
            }

            return var15;
         } catch (MaxChangedBlocksException var18) {
            throw new RuntimeException(var18);
         }
      }
   }

   public LinTag<?> toNativeLin(Tag foreign) {
      if (foreign == null) {
         return null;
      } else if (foreign instanceof CompoundTag) {
         Map<String, LinTag<?>> values = new HashMap<>();

         for (String str : ((CompoundTag)foreign).getAllKeys()) {
            Tag base = ((CompoundTag)foreign).get(str);
            values.put(str, this.toNativeLin(base));
         }

         return LinCompoundTag.of(values);
      } else if (foreign instanceof ByteTag) {
         return LinByteTag.of(((ByteTag)foreign).getAsByte());
      } else if (foreign instanceof ByteArrayTag) {
         return LinByteArrayTag.of(((ByteArrayTag)foreign).getAsByteArray());
      } else if (foreign instanceof DoubleTag) {
         return LinDoubleTag.of(((DoubleTag)foreign).getAsDouble());
      } else if (foreign instanceof FloatTag) {
         return LinFloatTag.of(((FloatTag)foreign).getAsFloat());
      } else if (foreign instanceof IntTag) {
         return LinIntTag.of(((IntTag)foreign).getAsInt());
      } else if (foreign instanceof IntArrayTag) {
         return LinIntArrayTag.of(((IntArrayTag)foreign).getAsIntArray());
      } else if (foreign instanceof LongArrayTag) {
         return LinLongArrayTag.of(((LongArrayTag)foreign).getAsLongArray());
      } else {
         if (foreign instanceof ListTag) {
            try {
               return this.toNativeLinList((ListTag)foreign);
            } catch (Throwable var7) {
               this.logger.log(java.util.logging.Level.WARNING, "Failed to convert net.minecraft.nbt.ListTag", var7);
            }
         } else {
            if (foreign instanceof LongTag) {
               return LinLongTag.of(((LongTag)foreign).getAsLong());
            }

            if (foreign instanceof ShortTag) {
               return LinShortTag.of(((ShortTag)foreign).getAsShort());
            }

            if (foreign instanceof StringTag) {
               return LinStringTag.of(foreign.getAsString());
            }

            if (foreign instanceof EndTag) {
               return LinEndTag.instance();
            }
         }

         throw new IllegalArgumentException("Don't know how to make native " + foreign.getClass().getCanonicalName());
      }
   }

   private LinListTag<?> toNativeLinList(ListTag foreign) throws SecurityException, IllegalArgumentException {
      LinListTag.Builder<LinTag<?>> builder = LinListTag.builder(LinTagType.fromId(LinTagId.fromId(foreign.getElementType())));

      for (Tag tag : foreign) {
         builder.add(this.toNativeLin(tag));
      }

      return builder.build();
   }

   public Tag fromNativeLin(LinTag<?> foreign) {
      if (foreign == null) {
         return null;
      } else if (foreign instanceof LinCompoundTag compoundTag) {
         CompoundTag tag = new CompoundTag();

         for (Entry<String, LinTag<?>> entry : compoundTag.value().entrySet()) {
            tag.put(entry.getKey(), this.fromNativeLin(entry.getValue()));
         }

         return tag;
      } else if (foreign instanceof LinByteTag byteTag) {
         return ByteTag.valueOf(byteTag.valueAsByte());
      } else if (foreign instanceof LinByteArrayTag byteArrayTag) {
         return new ByteArrayTag(byteArrayTag.value());
      } else if (foreign instanceof LinDoubleTag doubleTag) {
         return DoubleTag.valueOf(doubleTag.valueAsDouble());
      } else if (foreign instanceof LinFloatTag floatTag) {
         return FloatTag.valueOf(floatTag.valueAsFloat());
      } else if (foreign instanceof LinIntTag intTag) {
         return IntTag.valueOf(intTag.valueAsInt());
      } else if (foreign instanceof LinIntArrayTag intArrayTag) {
         return new IntArrayTag(intArrayTag.value());
      } else if (foreign instanceof LinLongArrayTag longArrayTag) {
         return new LongArrayTag(longArrayTag.value());
      } else if (!(foreign instanceof LinListTag<?> listTag)) {
         if (foreign instanceof LinLongTag longTag) {
            return LongTag.valueOf(longTag.valueAsLong());
         } else if (foreign instanceof LinShortTag shortTag) {
            return ShortTag.valueOf(shortTag.valueAsShort());
         } else if (foreign instanceof LinStringTag stringTag) {
            return StringTag.valueOf(stringTag.value());
         } else if (foreign instanceof LinEndTag) {
            return EndTag.INSTANCE;
         } else {
            throw new IllegalArgumentException("Don't know how to make NMS " + foreign.getClass().getCanonicalName());
         }
      } else {
         ListTag tag = new ListTag();

         for (LinTag<?> t : listTag.value()) {
            tag.add(this.fromNativeLin(t));
         }

         return tag;
      }
   }

   @Override
   public boolean supportsWatchdog() {
      return this.watchdog != null;
   }

   @Override
   public void tickWatchdog() {
      this.watchdog.tick();
   }

   private static class MojangWatchdog implements Watchdog {
      private final DedicatedServer server;
      private final Field tickField;

      MojangWatchdog(DedicatedServer server) throws NoSuchFieldException {
         this.server = server;
         Field tickField = MinecraftServer.class.getDeclaredField(StaticRefraction.NEXT_TICK_TIME);
         if (tickField.getType() != long.class) {
            throw new IllegalStateException("nextTickTime is not a long field, mapping is likely incorrect");
         } else {
            tickField.setAccessible(true);
            this.tickField = tickField;
         }
      }

      @Override
      public void tick() {
         try {
            this.tickField.set(this.server, Util.getMillis());
         } catch (IllegalAccessException var2) {
         }
      }
   }

   private static class NoOpWorldLoadListener implements ChunkProgressListener {
      public void updateSpawnPos(ChunkPos spawnPos) {
      }

      public void onStatusChange(ChunkPos pos, @org.jetbrains.annotations.Nullable ChunkStatus status) {
      }

      public void start() {
      }

      public void stop() {
      }
   }

   private class SpigotWatchdog implements Watchdog {
      private final Field instanceField;
      private final Field lastTickField;

      SpigotWatchdog() throws NoSuchFieldException {
         Field instanceField = WatchdogThread.class.getDeclaredField("instance");
         instanceField.setAccessible(true);
         this.instanceField = instanceField;
         Field lastTickField = WatchdogThread.class.getDeclaredField("lastTick");
         lastTickField.setAccessible(true);
         this.lastTickField = lastTickField;
      }

      @Override
      public void tick() {
         try {
            WatchdogThread instance = (WatchdogThread)this.instanceField.get(null);
            if ((Long)this.lastTickField.get(instance) != 0L) {
               WatchdogThread.tick();
            }
         } catch (IllegalAccessException var2) {
            PaperweightAdapter.this.logger.log(java.util.logging.Level.WARNING, "Failed to tick watchdog", (Throwable)var2);
         }
      }
   }
}
