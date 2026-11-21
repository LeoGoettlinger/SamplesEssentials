package com.sk89q.worldedit.command.util;

import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.EntityFunction;

public class CreatureButcher {
   private final Actor player;
   public int flags = 0;

   public CreatureButcher(Actor player) {
      this.player = player;
   }

   public void or(int flag, boolean on) {
      if (on) {
         this.flags |= flag;
      }
   }

   public void or(int flag, boolean on, String permission) {
      this.or(flag, on);
      if ((this.flags & flag) != 0 && !this.player.hasPermission(permission)) {
         this.flags &= ~flag;
      }
   }

   public EntityFunction createFunction() {
      return entity -> {
         boolean killPets = (this.flags & 1) != 0;
         boolean killNPCs = (this.flags & 2) != 0;
         boolean killAnimals = (this.flags & 4) != 0;
         boolean killGolems = (this.flags & 8) != 0;
         boolean killAmbient = (this.flags & 16) != 0;
         boolean killTagged = (this.flags & 32) != 0;
         boolean killArmorStands = (this.flags & 64) != 0;
         boolean killWaterCreatures = (this.flags & 128) != 0;
         EntityProperties type = entity.getFacet(EntityProperties.class);
         if (type == null) {
            return false;
         } else if (type.isPlayerDerived()) {
            return false;
         } else if (!type.isLiving()) {
            return false;
         } else if (!killAnimals && type.isAnimal()) {
            return false;
         } else if (!killPets && type.isTamed()) {
            return false;
         } else if (!killGolems && type.isGolem()) {
            return false;
         } else if (!killNPCs && type.isNPC()) {
            return false;
         } else if (!killAmbient && type.isAmbient()) {
            return false;
         } else if (!killTagged && type.isTagged()) {
            return false;
         } else if (!killArmorStands && type.isArmorStand()) {
            return false;
         } else if (!killWaterCreatures && type.isWaterCreature()) {
            return false;
         } else {
            entity.remove();
            return true;
         }
      };
   }

   public final class Flags {
      public static final int PETS = 1;
      public static final int NPCS = 2;
      public static final int ANIMALS = 4;
      public static final int GOLEMS = 8;
      public static final int AMBIENT = 16;
      public static final int TAGGED = 32;
      public static final int ARMOR_STAND = 64;
      public static final int WATER = 128;
      public static final int FRIENDLY = 191;

      private Flags() {
      }
   }
}
