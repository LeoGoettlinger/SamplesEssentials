package com.sk89q.worldedit.bukkit;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Golem;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.minecart.ExplosiveMinecart;

class BukkitEntityProperties implements EntityProperties {
   private static final boolean HAS_ABSTRACT_VILLAGER;
   private final Entity entity;

   BukkitEntityProperties(Entity entity) {
      Preconditions.checkNotNull(entity);
      this.entity = entity;
   }

   @Override
   public boolean isPlayerDerived() {
      return this.entity instanceof HumanEntity;
   }

   @Override
   public boolean isProjectile() {
      return this.entity instanceof Projectile;
   }

   @Override
   public boolean isItem() {
      return this.entity instanceof Item;
   }

   @Override
   public boolean isFallingBlock() {
      return this.entity instanceof FallingBlock;
   }

   @Override
   public boolean isPainting() {
      return this.entity instanceof Painting;
   }

   @Override
   public boolean isItemFrame() {
      return this.entity instanceof ItemFrame;
   }

   @Override
   public boolean isBoat() {
      return this.entity instanceof Boat;
   }

   @Override
   public boolean isMinecart() {
      return this.entity instanceof Minecart;
   }

   @Override
   public boolean isTNT() {
      return this.entity instanceof TNTPrimed || this.entity instanceof ExplosiveMinecart;
   }

   @Override
   public boolean isExperienceOrb() {
      return this.entity instanceof ExperienceOrb;
   }

   @Override
   public boolean isLiving() {
      return this.entity instanceof LivingEntity;
   }

   @Override
   public boolean isAnimal() {
      return this.entity instanceof Animals;
   }

   @Override
   public boolean isAmbient() {
      return this.entity instanceof Ambient;
   }

   @Override
   public boolean isNPC() {
      return HAS_ABSTRACT_VILLAGER ? this.entity instanceof AbstractVillager : this.entity instanceof Villager;
   }

   @Override
   public boolean isGolem() {
      return this.entity instanceof Golem;
   }

   @Override
   public boolean isTamed() {
      return this.entity instanceof Tameable && ((Tameable)this.entity).isTamed();
   }

   @Override
   public boolean isTagged() {
      return this.entity instanceof LivingEntity && this.entity.getCustomName() != null;
   }

   @Override
   public boolean isArmorStand() {
      return this.entity instanceof ArmorStand;
   }

   @Override
   public boolean isPasteable() {
      return !(this.entity instanceof Player) && !(this.entity instanceof ComplexEntityPart);
   }

   @Override
   public boolean isWaterCreature() {
      return this.entity instanceof WaterMob;
   }

   static {
      boolean temp;
      try {
         Class.forName("org.bukkit.entity.AbstractVillager");
         temp = true;
      } catch (ClassNotFoundException var2) {
         temp = false;
      }

      HAS_ABSTRACT_VILLAGER = temp;
   }
}
