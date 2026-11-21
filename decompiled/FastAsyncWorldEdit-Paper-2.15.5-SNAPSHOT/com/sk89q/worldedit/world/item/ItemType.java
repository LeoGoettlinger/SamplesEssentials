package com.sk89q.worldedit.world.item;

import com.fastasyncworldedit.core.registry.RegistryItem;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.util.GuavaUtil;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.ItemMaterial;
import javax.annotation.Nullable;

public class ItemType implements RegistryItem, Keyed {
   public static final NamespacedRegistry<ItemType> REGISTRY = new NamespacedRegistry<>("item type", "item_type", "minecraft", true);
   private final String id;
   private final transient LazyReference<String> name = LazyReference.from(
      () -> {
         String name = GuavaUtil.firstNonNull(
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getItemRegistry().getName(this), ""
         );
         return name.isEmpty() ? this.id() : name;
      }
   );
   private final transient LazyReference<Component> richName = LazyReference.from(
      () -> WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getItemRegistry().getRichName(this)
   );
   private final transient LazyReference<ItemMaterial> itemMaterial = LazyReference.from(
      () -> WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getItemRegistry().getMaterial(this)
   );
   private transient BlockType blockType;
   private transient boolean initBlockType;
   private transient BaseItem defaultState;
   private transient int internalId;

   public ItemType(String id) {
      if (!id.contains(":")) {
         id = "minecraft:" + id;
      }

      this.id = id;
   }

   @Override
   public String id() {
      return this.id;
   }

   @Override
   public void setInternalId(int internalId) {
      this.internalId = internalId;
   }

   @Override
   public int getInternalId() {
      return this.internalId;
   }

   public Component getRichName() {
      return this.richName.getValue();
   }

   @Deprecated
   public String getName() {
      return this.name.getValue();
   }

   public boolean hasBlockType() {
      return this.getBlockType() != null;
   }

   @Nullable
   public BlockType getBlockType() {
      if (!this.initBlockType) {
         this.initBlockType = true;
         this.blockType = BlockTypes.get(this.id);
      }

      return this.blockType;
   }

   public BaseItem getDefaultState() {
      if (this.defaultState == null) {
         this.defaultState = new BaseItemStack(this);
      }

      return this.defaultState;
   }

   public ItemMaterial getMaterial() {
      return this.itemMaterial.getValue();
   }

   @Override
   public String toString() {
      return this.id();
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof ItemType && this.id.equals(((ItemType)obj).id);
   }
}
