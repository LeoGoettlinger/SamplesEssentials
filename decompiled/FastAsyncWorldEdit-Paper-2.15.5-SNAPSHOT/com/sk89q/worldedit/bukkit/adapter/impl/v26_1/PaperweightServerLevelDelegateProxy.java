package com.sk89q.worldedit.bukkit.adapter.impl.v26_1;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableTable.Builder;
import com.google.errorprone.annotations.Keep;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.entity.EntityTypes;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.jetbrains.annotations.Nullable;

public class PaperweightServerLevelDelegateProxy implements InvocationHandler, AutoCloseable {
   private final EditSession editSession;
   private final ServerLevel serverLevel;
   private final PaperweightAdapter adapter;
   private final Map<BlockVector3, BlockEntity> createdBlockEntities = new HashMap<>();
   private static final Table<String, MethodType, MethodHandle> METHOD_MAP;

   private static BlockVector3 adapt(BlockPos blockPos) {
      return BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ());
   }

   private PaperweightServerLevelDelegateProxy(EditSession editSession, ServerLevel serverLevel, PaperweightAdapter adapter) {
      this.editSession = editSession;
      this.serverLevel = serverLevel;
      this.adapter = adapter;
   }

   public static PaperweightServerLevelDelegateProxy.LevelAndProxy newInstance(EditSession editSession, ServerLevel serverLevel, PaperweightAdapter adapter) {
      PaperweightServerLevelDelegateProxy proxy = new PaperweightServerLevelDelegateProxy(editSession, serverLevel, adapter);
      return new PaperweightServerLevelDelegateProxy.LevelAndProxy(
         (WorldGenLevel)Proxy.newProxyInstance(serverLevel.getClass().getClassLoader(), serverLevel.getClass().getInterfaces(), proxy), proxy
      );
   }

   @Keep
   @Nullable
   private BlockEntity getBlockEntity(BlockPos blockPos) {
      BlockVector3 pos = adapt(blockPos);
      return this.createdBlockEntities.get(pos);
   }

   private BlockState getBlockState(BlockPos blockPos) {
      return this.adapter.adapt(this.editSession.getBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
   }

   @Keep
   private boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
      return predicate.test(this.getBlockState(blockPos));
   }

   private boolean setBlock(BlockPos blockPos, BlockState blockState) {
      try {
         this.handleBlockEntity(blockPos, blockState);
         return this.editSession.setBlock(adapt(blockPos), this.adapter.adapt(blockState));
      } catch (MaxChangedBlocksException var4) {
         throw new RuntimeException(var4);
      }
   }

   private void handleBlockEntity(BlockPos blockPos, BlockState blockState) {
      BlockVector3 pos = adapt(blockPos);
      if (blockState.hasBlockEntity()) {
         if (!(blockState.getBlock() instanceof EntityBlock entityBlock)) {
            throw new AssertionError("BlockState has block entity but block is not an EntityBlock: " + blockState);
         }

         BlockEntity newEntity = entityBlock.newBlockEntity(blockPos, blockState);
         if (newEntity != null) {
            newEntity.setBlockState(blockState);
            this.createdBlockEntities.put(pos, newEntity);
            return;
         }
      }

      this.createdBlockEntities.remove(pos);
   }

   @Keep
   private boolean removeBlock(BlockPos blockPos) {
      return this.setBlock(blockPos, Blocks.AIR.defaultBlockState());
   }

   @Keep
   private boolean addEntity(Entity entity) {
      Vec3 pos = entity.getPosition(0.0F);
      Location location = new Location(BukkitAdapter.adapt(this.serverLevel.getWorld()), pos.x(), pos.y(), pos.z());
      Identifier id = this.serverLevel.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE).getKey(entity.getType());
      CompoundTag tag = PaperweightLoggingProblemReporter.with(() -> "serializing entity " + entity.getStringUUID(), reporter -> {
         TagValueOutput tagValueOutput = TagValueOutput.createWithContext(reporter, this.serverLevel.registryAccess());
         entity.saveWithoutId(tagValueOutput);
         return tagValueOutput.buildResult();
      });
      BaseEntity baseEntity = new BaseEntity(EntityTypes.get(id.toString()), LazyReference.from(() -> (LinCompoundTag)this.adapter.toNativeLin((Tag)tag)));
      return this.editSession.createEntity(location, baseEntity) != null;
   }

   @Override
   public void close() throws MaxChangedBlocksException {
      for (Entry<BlockVector3, BlockEntity> entry : this.createdBlockEntities.entrySet()) {
         BlockVector3 blockPos = entry.getKey();
         BlockEntity blockEntity = entry.getValue();
         CompoundTag tag = PaperweightLoggingProblemReporter.with(() -> "saving block entity at " + blockPos, reporter -> {
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(reporter, this.serverLevel.registryAccess());
            blockEntity.saveWithId(tagValueOutput);
            return tagValueOutput.buildResult();
         });
         this.editSession
            .setBlock(
               blockPos,
               this.adapter.adapt(blockEntity.getBlockState()).toBaseBlock(LazyReference.from(() -> (LinCompoundTag)this.adapter.toNativeLin((Tag)tag)))
            );
      }
   }

   private static void addMethodHandleToTable(Builder<String, MethodType, MethodHandle> table, String methodName, MethodHandle methodHandle) {
      MethodHandle spreader = methodHandle.asSpreader(1, Object[].class, methodHandle.type().parameterCount() - 1);
      table.put(methodName, methodHandle.type().dropParameterTypes(0, 1).changeReturnType(void.class), spreader);
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      MethodHandle delegate = (MethodHandle)METHOD_MAP.get(method.getName(), MethodType.methodType(void.class, method.getParameterTypes()));
      return delegate != null ? (Object)delegate.invoke((PaperweightServerLevelDelegateProxy)this, (Object[])args) : method.invoke(this.serverLevel, args);
   }

   static {
      Lookup lookup = MethodHandles.lookup();
      Builder<String, MethodType, MethodHandle> builder = ImmutableTable.builder();

      try {
         addMethodHandleToTable(
            builder,
            StaticRefraction.GET_BLOCK_STATE,
            lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("getBlockState", BlockPos.class))
         );
         addMethodHandleToTable(
            builder,
            StaticRefraction.IS_STATE_AT_POSITION,
            lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("isStateAtPosition", BlockPos.class, Predicate.class))
         );
         MethodHandle addEntity = lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("addEntity", Entity.class));
         addMethodHandleToTable(builder, StaticRefraction.ADD_FRESH_ENTITY_WITH_PASSENGERS_ENTITY, addEntity);
         addMethodHandleToTable(
            builder, StaticRefraction.ADD_FRESH_ENTITY_WITH_PASSENGERS_ENTITY_SPAWN_REASON, MethodHandles.dropArguments(addEntity, 2, SpawnReason.class)
         );
         addMethodHandleToTable(builder, StaticRefraction.ADD_FRESH_ENTITY, addEntity);
         addMethodHandleToTable(builder, StaticRefraction.ADD_FRESH_ENTITY_SPAWN_REASON, MethodHandles.dropArguments(addEntity, 2, SpawnReason.class));
         addMethodHandleToTable(
            builder,
            StaticRefraction.GET_BLOCK_ENTITY,
            lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("getBlockEntity", BlockPos.class))
         );
         MethodHandle setBlock = lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("setBlock", BlockPos.class, BlockState.class));
         addMethodHandleToTable(builder, StaticRefraction.SET_BLOCK, MethodHandles.dropArguments(setBlock, 3, int.class));
         addMethodHandleToTable(builder, StaticRefraction.SET_BLOCK_MAX_UPDATE, MethodHandles.dropArguments(setBlock, 3, int.class, int.class));
         MethodHandle removeBlock = lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("removeBlock", BlockPos.class));
         addMethodHandleToTable(builder, StaticRefraction.REMOVE_BLOCK, MethodHandles.dropArguments(removeBlock, 2, boolean.class));
         addMethodHandleToTable(builder, StaticRefraction.DESTROY_BLOCK, MethodHandles.dropArguments(removeBlock, 2, boolean.class));
         addMethodHandleToTable(
            builder, StaticRefraction.DESTROY_BLOCK_BREAKING_ENTITY, MethodHandles.dropArguments(removeBlock, 2, boolean.class, Entity.class)
         );
         addMethodHandleToTable(
            builder,
            StaticRefraction.DESTROY_BLOCK_BREAKING_ENTITY_MAX_UPDATE,
            MethodHandles.dropArguments(removeBlock, 2, boolean.class, Entity.class, int.class)
         );
      } catch (NoSuchMethodException | IllegalAccessException var5) {
         throw new RuntimeException("Failed to bind to own methods", var5);
      }

      METHOD_MAP = builder.build();
   }

   public record LevelAndProxy(WorldGenLevel level, PaperweightServerLevelDelegateProxy proxy) implements AutoCloseable {
      @Override
      public void close() throws MaxChangedBlocksException {
         this.proxy.close();
      }
   }
}
