package dev.cwhead.GravesX.event;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveBlockPlaceEvent extends GraveEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @Nullable
   private final LivingEntity livingEntity;

   public GraveBlockPlaceEvent(
      @NotNull Grave grave, @NotNull Location location, @Nullable BlockData.BlockType blockType, @Nullable Block block, @Nullable LivingEntity livingEntity
   ) {
      super(grave, location, blockType, block);
      this.livingEntity = livingEntity;
   }

   @Deprecated
   public GraveBlockPlaceEvent(@NotNull Grave grave, @NotNull Location location, @Nullable BlockData.BlockType blockType) {
      this(grave, location, blockType, null, null);
   }

   public boolean hasEntity() {
      return this.livingEntity != null;
   }

   @NotNull
   public Entity getEntity() {
      return this.livingEntity;
   }

   @NotNull
   public LivingEntity getKiller() {
      if (this.livingEntity == null) {
         throw new GravesXNullPointerException(this, "livingEntity");
      } else {
         return this.livingEntity.getKiller();
      }
   }

   @NotNull
   public String getKillerName() {
      if (this.livingEntity == null) {
         throw new GravesXNullPointerException(this, "livingEntity");
      } else if (this.livingEntity.getKiller() == null) {
         throw new GravesXNullPointerException(this, "killer");
      } else {
         return this.livingEntity.getKiller().getName();
      }
   }

   @NotNull
   public UUID getKillerUniqueId() {
      if (this.livingEntity == null) {
         throw new GravesXNullPointerException(this, "livingEntity");
      } else if (this.livingEntity.getKiller() == null) {
         throw new GravesXNullPointerException(this, "killer");
      } else {
         return this.livingEntity.getKiller().getUniqueId();
      }
   }

   public boolean hasKiller() {
      return this.livingEntity != null && this.livingEntity.getKiller() != null;
   }

   public boolean hasPlayer() {
      return this.livingEntity instanceof Player p ? p.getPlayer() != null : false;
   }

   public Player getPlayer() {
      if (this.livingEntity instanceof Player p) {
         return p.getPlayer();
      } else {
         throw new GravesXNullPointerException(this, "player");
      }
   }

   @NotNull
   @Override
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
