package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.BundledBlockRegistry;
import com.sk89q.worldedit.world.registry.PassthroughBlockMaterial;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import org.bukkit.Material;

public class BukkitBlockRegistry extends BundledBlockRegistry {
   private BukkitBlockRegistry.BukkitBlockMaterial[] materialMap;

   @Override
   public Component getRichName(BlockType blockType) {
      return WorldEditPlugin.getInstance().getBukkitImplAdapter() != null
         ? WorldEditPlugin.getInstance().getBukkitImplAdapter().getRichBlockName(blockType)
         : super.getRichName(blockType);
   }

   @Nullable
   @Override
   public BlockMaterial getMaterial(BlockType blockType) {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      if (adapter != null) {
         BlockMaterial result = adapter.getMaterial(blockType);
         if (result != null) {
            return result;
         }
      }

      Material mat = BukkitAdapter.adapt(blockType);
      if (mat == null) {
         return new PassthroughBlockMaterial(null);
      } else {
         if (this.materialMap == null) {
            this.materialMap = new BukkitBlockRegistry.BukkitBlockMaterial[Material.values().length];
         }

         BukkitBlockRegistry.BukkitBlockMaterial result = this.materialMap[mat.ordinal()];
         if (result == null) {
            result = new BukkitBlockRegistry.BukkitBlockMaterial(access$001(this, blockType), mat);
            this.materialMap[mat.ordinal()] = result;
         }

         return result;
      }
   }

   @Nullable
   @Override
   public BlockMaterial getMaterial(BlockState state) {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      if (adapter != null) {
         BlockMaterial result = adapter.getMaterial(state);
         if (result != null) {
            return result;
         }
      }

      return super.getMaterial(state);
   }

   @Nullable
   @Override
   public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.getProperties(blockType) : super.getProperties(blockType);
   }

   @Override
   public OptionalInt getInternalBlockStateId(BlockState state) {
      return WorldEditPlugin.getInstance().getBukkitImplAdapter() != null
         ? WorldEditPlugin.getInstance().getBukkitImplAdapter().getInternalBlockStateId(state)
         : OptionalInt.empty();
   }

   @Override
   public Collection<String> values() {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.getRegisteredDefaultBlockStates() : super.values();
   }

   @Override
   public Map<String, ? extends List<Property<?>>> getAllProperties() {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.getAllProperties() : super.getAllProperties();
   }

   public static class BukkitBlockMaterial extends PassthroughBlockMaterial {
      private final Material material;

      public BukkitBlockMaterial(@Nullable BlockMaterial material, Material bukkitMaterial) {
         super(material);
         this.material = bukkitMaterial;
      }

      @Override
      public boolean isAir() {
         switch (this.material) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
               return true;
            default:
               return false;
         }
      }

      @Override
      public boolean isSolid() {
         return this.material.isSolid();
      }

      @Override
      public boolean isBurnable() {
         return this.material.isBurnable();
      }

      @Override
      public boolean isTranslucent() {
         return this.material.isTransparent();
      }
   }
}
