package dev.cwhead.GravesX.event.graveevent;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.interfaces.Addon;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GraveEvent extends Event implements Cancellable, Addon {
   private static final HandlerList HANDLERS = new HandlerList();
   public final Grave grave;
   @Nullable
   public Location location;
   @Nullable
   public final BlockData.BlockType blockType;
   @Nullable
   public final Block block;
   private boolean isCancelled;
   private boolean dropItems;
   private boolean isAddon;

   protected GraveEvent(@NotNull Grave grave, @Nullable Location location, @Nullable BlockData.BlockType blockType, @Nullable Block block) {
      this.grave = Objects.requireNonNull(grave, "grave");
      this.location = location;
      this.blockType = blockType;
      this.block = block;
      this.isCancelled = false;
      this.dropItems = true;
      this.isAddon = false;
   }

   public Grave getGrave() {
      return this.grave;
   }

   public int getGraveExperience() {
      return this.grave.getExperience();
   }

   public UUID getGraveUUID() {
      return this.grave.getUUID();
   }

   public String getGraveOwnerDisplayName() {
      return this.grave.getOwnerDisplayName();
   }

   public String getGraveOwnerName() {
      return this.grave.getOwnerName();
   }

   public UUID getGraveOwnerUniqueId() {
      return this.grave.getOwnerUUID();
   }

   public String getGraveOwnerNameDisplay() {
      return this.grave.getOwnerNameDisplay();
   }

   public String getGraveOwnerTexture() {
      return this.grave.getOwnerTexture();
   }

   public String getGraveOwnerTextureSignature() {
      return this.grave.getOwnerTextureSignature();
   }

   public boolean hasLocation() {
      return this.location != null;
   }

   @NotNull
   public Location getLocation() {
      if (this.location != null) {
         return this.location;
      } else {
         throw new GravesXNullPointerException(this, "location");
      }
   }

   public void setLocation(@NotNull Location location) {
      this.location = Objects.requireNonNull(location, "location");
   }

   public boolean hasBlockType() {
      return this.blockType != null;
   }

   @NotNull
   public BlockData.BlockType getBlockType() {
      if (this.blockType != null) {
         return this.blockType;
      } else {
         throw new GravesXNullPointerException(this, "blockType");
      }
   }

   public boolean hasBlock() {
      return this.block != null;
   }

   @NotNull
   public Block getBlock() {
      if (this.block != null) {
         return this.block;
      } else {
         throw new GravesXNullPointerException(this, "block");
      }
   }

   public int getBlockExp() {
      return this.grave.getExperience();
   }

   public void setBlockExp(int experience) {
      this.grave.setExperience(experience);
   }

   public boolean isDropItems() {
      return this.dropItems;
   }

   public void setDropItems(boolean dropItems) {
      this.dropItems = dropItems;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancel) {
      this.isCancelled = cancel;
   }

   @Override
   public boolean isAddon() {
      return this.isAddon;
   }

   @Override
   public void setAddon(boolean addon) {
      this.isAddon = addon;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
