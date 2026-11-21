package com.sk89q.worldedit.bukkit.adapter.ext.fawe.v1_21_R1;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PaperweightServerLevelDelegateProxy implements InvocationHandler {
   private final EditSession editSession;
   private final ServerLevel serverLevel;
   private final PaperweightAdapter adapter;

   private PaperweightServerLevelDelegateProxy(EditSession editSession, ServerLevel serverLevel, PaperweightAdapter adapter) {
      this.editSession = editSession;
      this.serverLevel = serverLevel;
      this.adapter = adapter;
   }

   public static WorldGenLevel newInstance(EditSession editSession, ServerLevel serverLevel, PaperweightAdapter adapter) {
      return (WorldGenLevel)Proxy.newProxyInstance(
         serverLevel.getClass().getClassLoader(),
         serverLevel.getClass().getInterfaces(),
         new PaperweightServerLevelDelegateProxy(editSession, serverLevel, adapter)
      );
   }

   @Nullable
   private BlockEntity getBlockEntity(BlockPos blockPos) {
      BlockEntity tileEntity = this.serverLevel.getChunkAt(blockPos).getBlockEntity(blockPos);
      if (tileEntity == null) {
         return null;
      } else {
         tileEntity.loadWithComponents(
            (CompoundTag)this.adapter
               .fromNativeLin(this.editSession.getFullBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ()).getNbtReference().getValue()),
            this.serverLevel.registryAccess()
         );
         return tileEntity;
      }
   }

   private BlockState getBlockState(BlockPos blockPos) {
      return this.adapter.adapt(this.editSession.getBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
   }

   private boolean setBlock(BlockPos blockPos, BlockState blockState) {
      try {
         return this.editSession.setBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ()), this.adapter.adapt(blockState));
      } catch (MaxChangedBlocksException var4) {
         throw new RuntimeException(var4);
      }
   }

   private boolean removeBlock(BlockPos blockPos, boolean bl) {
      try {
         return this.editSession.setBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ()), BlockTypes.AIR.getDefaultState());
      } catch (MaxChangedBlocksException var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String var4 = method.getName();
      switch (var4) {
         case "a_":
         case "getBlockState":
            if (args.length == 1 && args[0] instanceof BlockPos blockPos) {
               return this.getBlockState(blockPos);
            }
            break;
         case "c_":
         case "getBlockEntity":
            if (args.length == 1 && args[0] instanceof BlockPos blockPos) {
               return this.getBlockEntity(blockPos);
            }
            break;
         case "a":
         case "setBlock":
         case "removeBlock":
         case "destroyBlock":
            if (args.length >= 2 && args[0] instanceof BlockPos blockPos && args[1] instanceof BlockState blockState) {
               return this.setBlock(blockPos, blockState);
            }

            if (args.length >= 2 && args[0] instanceof BlockPos blockPos && args[1] instanceof Boolean bl) {
               return this.removeBlock(blockPos, bl);
            }
      }

      return method.invoke(this.serverLevel, args);
   }
}
