package net.coreprotect.worldedit;

import java.util.Collection;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public final class WorldEditBlockState implements BlockState {
   protected Location location;
   protected Material material;
   protected BlockData blockData;

   public WorldEditBlockState(Location loc) {
      this.location = loc;
   }

   public WorldEditBlockState(Location loc, Material type, BlockData data) {
      this.location = loc;
      this.material = type;
      this.blockData = data;
   }

   public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
   }

   public List<MetadataValue> getMetadata(String metadataKey) {
      return null;
   }

   public boolean hasMetadata(String metadataKey) {
      return false;
   }

   public void removeMetadata(String metadataKey, Plugin owningPlugin) {
   }

   public Block getBlock() {
      return null;
   }

   public MaterialData getData() {
      return null;
   }

   public BlockData getBlockData() {
      return this.blockData;
   }

   public Material getType() {
      return this.material;
   }

   public byte getLightLevel() {
      return 0;
   }

   public World getWorld() {
      return this.location.getWorld();
   }

   public int getX() {
      return this.location.getBlockX();
   }

   public int getY() {
      return this.location.getBlockY();
   }

   public int getZ() {
      return this.location.getBlockZ();
   }

   public Location getLocation() {
      return this.location;
   }

   public Location getLocation(Location loc) {
      if (loc != null) {
         loc.setWorld(this.location.getWorld());
         loc.setX(this.location.getX());
         loc.setY(this.location.getY());
         loc.setZ(this.location.getZ());
         loc.setYaw(this.location.getYaw());
         loc.setPitch(this.location.getPitch());
      }

      return loc;
   }

   public Chunk getChunk() {
      return null;
   }

   public void setData(MaterialData data) {
   }

   public void setBlockData(BlockData data) {
      this.blockData = data;
   }

   public void setType(Material type) {
      this.material = type;
   }

   public boolean update() {
      return false;
   }

   public boolean update(boolean force) {
      return false;
   }

   public boolean update(boolean force, boolean applyPhysics) {
      return false;
   }

   public byte getRawData() {
      return 0;
   }

   public void setRawData(byte data) {
   }

   public boolean isPlaced() {
      return false;
   }

   public boolean isCollidable() {
      return false;
   }

   public Collection<ItemStack> getDrops() {
      return null;
   }

   public Collection<ItemStack> getDrops(ItemStack tool) {
      return null;
   }

   public Collection<ItemStack> getDrops(ItemStack tool, Entity entity) {
      return null;
   }

   public BlockState copy() {
      return null;
   }

   public BlockState copy(Location location) {
      return null;
   }

   public boolean isSuffocating() {
      return false;
   }
}
