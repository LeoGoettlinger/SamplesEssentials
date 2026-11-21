package com.sk89q.worldedit.command.util;

import com.fastasyncworldedit.core.util.TaskManager;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.function.EntityFunction;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class EntityRemover {
   private final EntityRemover.Type type;

   public static EntityRemover fromString(String str) {
      EntityRemover.Type type = EntityRemover.Type.findByPattern(str);
      if (type != null) {
         return new EntityRemover(type);
      } else {
         throw new IllegalArgumentException("Acceptable types: projectiles, items, paintings, itemframes, boats, minecarts, tnt, xp, or all");
      }
   }

   private EntityRemover(EntityRemover.Type type) {
      this.type = type;
   }

   public EntityFunction createFunction() {
      EntityRemover.Type type = this.type;
      Preconditions.checkNotNull(type, "type can't be null");
      return entity -> {
         EntityProperties registryType = entity.getFacet(EntityProperties.class);
         if (registryType != null && type.matches(registryType)) {
            TaskManager.taskManager().sync(entity::remove);
            return true;
         } else {
            return false;
         }
      };
   }

   public static enum Type {
      ALL("all") {
         @Override
         boolean matches(EntityProperties type) {
            for (EntityRemover.Type value : values()) {
               if (value != this && value.matches(type)) {
                  return true;
               }
            }

            return false;
         }
      },
      PROJECTILES("projectiles?|arrows?") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isProjectile();
         }
      },
      ITEMS("items?|drops?") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isItem();
         }
      },
      FALLING_BLOCKS("falling(blocks?|sand|gravel)") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isFallingBlock();
         }
      },
      PAINTINGS("paintings?|art") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isPainting();
         }
      },
      ITEM_FRAMES("(item)frames?") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isItemFrame();
         }
      },
      BOATS("boats?") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isBoat();
         }
      },
      MINECARTS("(mine)?carts?") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isMinecart();
         }
      },
      TNT("tnt") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isTNT();
         }
      },
      XP_ORBS("xp") {
         @Override
         boolean matches(EntityProperties type) {
            return type.isExperienceOrb();
         }
      };

      private final Pattern pattern;

      private Type(String pattern) {
         this.pattern = Pattern.compile(pattern);
      }

      public boolean matches(String str) {
         return this.pattern.matcher(str).matches();
      }

      abstract boolean matches(EntityProperties type);

      @Nullable
      public static EntityRemover.Type findByPattern(String str) {
         for (EntityRemover.Type type : values()) {
            if (type.matches(str)) {
               return type;
            }
         }

         return null;
      }
   }
}
