package com.fastasyncworldedit.bukkit.adapter;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

public abstract class CachedBukkitAdapter implements IBukkitAdapter {
   private int[] itemTypes;
   private int[] blockTypes;

   private boolean init() {
      if (this.itemTypes == null) {
         Material[] materials = Material.values();
         this.itemTypes = new int[materials.length];
         this.blockTypes = new int[materials.length];

         for (int i = 0; i < materials.length; i++) {
            Material material = materials[i];
            if (!material.isLegacy()) {
               NamespacedKey key = material.getKey();
               String id = key.getNamespace() + ":" + key.getKey();
               if (material.isBlock()) {
                  this.blockTypes[i] = BlockTypes.get(id).getInternalId();
               }

               if (material.isItem()) {
                  this.itemTypes[i] = ItemTypes.get(id).getInternalId();
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public ItemType asItemType(Material material) {
      try {
         return ItemTypes.get(this.itemTypes[material.ordinal()]);
      } catch (NullPointerException var3) {
         return this.init() ? this.asItemType(material) : ItemTypes.get(this.itemTypes[material.ordinal()]);
      }
   }

   @Override
   public BlockType asBlockType(Material material) {
      try {
         return BlockTypesCache.values[this.blockTypes[material.ordinal()]];
      } catch (NullPointerException var3) {
         if (this.init()) {
            return this.asBlockType(material);
         } else {
            throw var3;
         }
      }
   }

   @Override
   public BlockState adapt(BlockData blockData) {
      try {
         Preconditions.checkNotNull(blockData);
         Material material = blockData.getMaterial();
         BlockType type = BlockTypes.getFromStateId(this.blockTypes[material.ordinal()]);
         List<? extends Property> propList = type.getProperties();
         if (propList.size() == 0) {
            return type.getDefaultState();
         } else {
            String properties = blockData.getAsString();
            return BlockState.get(type, properties, type.getDefaultState());
         }
      } catch (NullPointerException var6) {
         if (this.init()) {
            return this.adapt(blockData);
         } else {
            throw var6;
         }
      }
   }

   protected abstract int[] getIbdToOrdinal();

   protected abstract int[] getOrdinalToIbdID();
}
