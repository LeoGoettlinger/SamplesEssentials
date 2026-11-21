package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.enginehub.linbus.tree.LinCompoundTag;

public interface NBTCompatibilityHandler {
   @Deprecated
   default <B extends BlockStateHolder<B>> boolean isAffectedBlock(B block) {
      BaseBlock state = block.toBaseBlock();
      BaseBlock updated = this.updateNbt(state);
      return state != updated;
   }

   @Deprecated
   default <B extends BlockStateHolder<B>> BlockStateHolder<?> updateNBT(B block, Map<String, Tag<?, ?>> values) {
      BaseBlock changed = this.updateNbt(block.toBaseBlock(LazyReference.from(() -> {
         LinCompoundTag.Builder builder = LinCompoundTag.builder();

         for (Entry<String, Tag<?, ?>> entry : values.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().toLinTag());
         }

         return builder.build();
      })));
      CompoundTag data = changed.getNbtData();
      values.clear();
      if (data != null) {
         values.putAll(data.getValue());
      }

      return changed;
   }

   @NonAbstractForCompatibility(
      delegateName = "updateNBT",
      delegateParams = {BlockStateHolder.class, Map.class}
   )
   default BaseBlock updateNbt(BaseBlock block) {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      if (!this.isAffectedBlock(block)) {
         return block;
      } else if (block.getNbt() == null) {
         return block;
      } else {
         Map<String, Tag<?, ?>> values = new HashMap<>(new CompoundTag(block.getNbt()).getValue());
         BlockStateHolder<?> changedBlock = this.updateNBT(block, values);
         return changedBlock.toBaseBlock(LazyReference.from(() -> {
            LinCompoundTag.Builder builder = LinCompoundTag.builder();

            for (Entry<String, Tag<?, ?>> entry : values.entrySet()) {
               builder.put(entry.getKey(), entry.getValue().toLinTag());
            }

            return builder.build();
         }));
      }
   }
}
