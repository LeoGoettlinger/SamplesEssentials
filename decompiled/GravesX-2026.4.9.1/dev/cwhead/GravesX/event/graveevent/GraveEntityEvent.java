package dev.cwhead.GravesX.event.graveevent;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveEntityEvent extends GraveEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final Entity entity;
   @Nullable
   private final LivingEntity livingEntity;
   @Nullable
   private final LivingEntity targetEntity;

   public GraveEntityEvent(
      @NotNull Grave grave,
      @NotNull Entity entity,
      @Nullable Location location,
      @Nullable BlockData.BlockType blockType,
      @Nullable Block block,
      @Nullable LivingEntity livingEntity,
      @Nullable LivingEntity targetEntity
   ) {
      super(grave, location, blockType, block);
      this.entity = Objects.requireNonNull(entity, "entity");
      this.livingEntity = livingEntity;
      this.targetEntity = targetEntity;
   }

   @NotNull
   public Entity getEntity() {
      return this.entity;
   }

   @NotNull
   public String getEntityName() {
      return this.entity.getName();
   }

   @NotNull
   public String getEntityCustomName() {
      return this.entity.getCustomName();
   }

   @NotNull
   public UUID getEntityUniqueId() {
      return this.entity.getUniqueId();
   }

   @Nullable
   public LivingEntity getTargetEntity() {
      return this.targetEntity;
   }

   @Nullable
   public EntityType getEntityType() {
      return this.targetEntity != null ? this.targetEntity.getType() : null;
   }

   public boolean isEntityActuallyPlayer() {
      return this.entity instanceof Player;
   }

   public boolean hasLivingEntity() {
      return this.livingEntity != null;
   }

   @NotNull
   public LivingEntity getLivingEntity() {
      return this.livingEntity;
   }

   @NotNull
   public String getLivingEntityVictim() {
      if (this.livingEntity == null) {
         throw new GravesXNullPointerException(this, "livingEntity");
      } else {
         return this.livingEntity.getName();
      }
   }

   @NotNull
   public UUID getLivingEntityVictimId() {
      if (this.livingEntity == null) {
         throw new GravesXNullPointerException(this, "livingEntity");
      } else {
         return this.livingEntity.getUniqueId();
      }
   }

   public boolean hasKiller() {
      return this.livingEntity != null && this.livingEntity.getKiller() != null;
   }

   @NotNull
   public LivingEntity getLivingEntityKiller() {
      if (this.livingEntity != null && this.livingEntity.getKiller() != null) {
         return this.livingEntity.getKiller();
      } else {
         throw new GravesXNullPointerException(this, "livingEntity#getKiller()");
      }
   }

   @NotNull
   public String getLivingEntityKillerName() {
      if (this.livingEntity != null && this.livingEntity.getKiller() != null) {
         return this.livingEntity.getKiller().getName();
      } else {
         throw new GravesXNullPointerException(this, "livingEntity#getKiller()");
      }
   }

   @NotNull
   public UUID getLivingEntityKillerUniqueId() {
      if (this.livingEntity != null && this.livingEntity.getKiller() != null) {
         return this.livingEntity.getKiller().getUniqueId();
      } else {
         throw new GravesXNullPointerException(this, "livingEntity#getKiller()");
      }
   }

   @NotNull
   public EntityType getLivingEntityVictimType() {
      if (this.livingEntity == null) {
         throw new GravesXNullPointerException(this, "livingEntity");
      } else {
         return this.livingEntity.getType();
      }
   }

   @NotNull
   public EntityType getLivingEntityKillerType() {
      if (this.livingEntity != null && this.livingEntity.getKiller() != null) {
         return this.livingEntity.getKiller().getType();
      } else {
         throw new GravesXNullPointerException(this, "killer");
      }
   }

   public boolean hasPlayer() {
      return this.entity instanceof Player;
   }

   @Nullable
   public Player getPlayer() {
      if (this.entity instanceof Player p) {
         return p;
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
