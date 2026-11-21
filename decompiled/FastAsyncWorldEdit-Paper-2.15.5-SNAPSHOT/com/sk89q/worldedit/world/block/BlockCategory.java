package com.sk89q.worldedit.world.block;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import java.util.Set;

public class BlockCategory extends Category<BlockType> implements Keyed {
   private boolean[] flatMap;
   public static final NamespacedRegistry<BlockCategory> REGISTRY = new NamespacedRegistry<>("block tag", "block_tag", "minecraft", true);

   public BlockCategory(final String id) {
      super(id);
   }

   @Override
   protected Set<BlockType> load() {
      Set<BlockType> result = WorldEdit.getInstance()
         .getPlatformManager()
         .queryCapability(Capability.GAME_HOOKS)
         .getRegistries()
         .getBlockCategoryRegistry()
         .getAll(this);
      int max = -1;

      for (BlockType type : result) {
         max = Math.max(max, type.getInternalId());
      }

      this.flatMap = new boolean[max + 1];

      for (BlockType type : result) {
         this.flatMap[type.getInternalId()] = true;
      }

      return result;
   }

   public <B extends BlockStateHolder<B>> boolean contains(B blockStateHolder) {
      int typeId = blockStateHolder.getBlockType().getInternalId();
      return this.flatMap.length > typeId && this.flatMap[typeId];
   }
}
