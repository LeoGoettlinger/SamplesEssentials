package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import io.papermc.paper.entity.LookAnchor;
import java.util.EnumSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class PaperEntityUtils {
   private static final boolean LOOK_ANCHORS = Skript.classExists("io.papermc.paper.entity.LookAnchor");
   private static final boolean LOOK_AT = Skript.methodExists(Mob.class, "lookAt", Entity.class);

   private static void mobLookAt(Object target, @Nullable Float headRotationSpeed, @Nullable Float maxHeadPitch, Mob mob) {
      Bukkit.getMobGoals().getRunningGoals(mob, GoalType.LOOK).forEach(goal -> Bukkit.getMobGoals().removeGoal(mob, goal));
      float speed = headRotationSpeed != null ? headRotationSpeed : mob.getHeadRotationSpeed();
      float maxPitch = maxHeadPitch != null ? maxHeadPitch : mob.getMaxHeadPitch();
      if (target instanceof Location && !((Location)target).isWorldLoaded()) {
         Location location = (Location)target;
         target = new Location(mob.getWorld(), location.getX(), location.getY(), location.getZ());
      }

      Bukkit.getMobGoals().addGoal(mob, 0, new PaperEntityUtils.LookGoal(target, mob, speed, maxPitch));
   }

   public static void lookAt(Object target, LivingEntity... entities) {
      lookAt(target, null, null, entities);
   }

   public static void lookAt(Object target, @Nullable Float headRotationSpeed, @Nullable Float maxHeadPitch, LivingEntity... entities) {
      if (target != null && LOOK_AT) {
         if (LOOK_ANCHORS) {
            lookAt(LookAnchor.EYES, headRotationSpeed, maxHeadPitch, entities);
         } else {
            for (LivingEntity entity : entities) {
               if (entity instanceof Mob) {
                  mobLookAt(target, headRotationSpeed, maxHeadPitch, (Mob)entity);
               }
            }
         }
      }
   }

   public static void lookAt(LookAnchor entityAnchor, Object target, @Nullable Float headRotationSpeed, @Nullable Float maxHeadPitch, LivingEntity... entities) {
      if (target != null && LOOK_AT && LOOK_ANCHORS) {
         for (LivingEntity entity : entities) {
            if (target instanceof Location && !((Location)target).isWorldLoaded()) {
               Location location = (Location)target;
               target = new Location(entity.getWorld(), location.getX(), location.getY(), location.getZ());
            }

            if (entity instanceof Player player) {
               if (target instanceof Vector vector) {
                  player.lookAt(player.getEyeLocation().add(vector), LookAnchor.EYES);
                  player.lookAt(player.getEyeLocation().add(vector), LookAnchor.FEET);
               } else if (target instanceof Location) {
                  player.lookAt((Location)target, LookAnchor.EYES);
                  player.lookAt((Location)target, LookAnchor.FEET);
               } else if (target instanceof Entity) {
                  player.lookAt((Entity)target, LookAnchor.EYES, entityAnchor);
                  player.lookAt((Entity)target, LookAnchor.FEET, entityAnchor);
               }
            } else if (entity instanceof Mob) {
               mobLookAt(target, headRotationSpeed, maxHeadPitch, (Mob)entity);
            }
         }
      }
   }

   public static class LookGoal implements Goal<Mob> {
      private static final GoalKey<Mob> SKRIPT_LOOK_KEY = GoalKey.of(Mob.class, new NamespacedKey(Skript.getInstance(), "skript_entity_look"));
      private static final EnumSet<GoalType> LOOK_GOAL = EnumSet.of(GoalType.LOOK);
      private static final int VECTOR = 0;
      private static final int LOCATION = 1;
      private static final int ENTITY = 2;
      private final float speed;
      private final float maxPitch;
      private final Object target;
      private int ticks = 0;
      private int type;
      private final Mob mob;

      LookGoal(Object target, Mob mob, float speed, float maxPitch) {
         this.type = target instanceof Vector ? 0 : (target instanceof Location ? 1 : 2);
         this.maxPitch = maxPitch;
         this.target = target;
         this.speed = speed;
         this.mob = mob;
      }

      public boolean shouldActivate() {
         return this.ticks < 50;
      }

      public void tick() {
         switch (this.type) {
            case 0:
               Vector vector = (Vector)this.target;
               this.mob.lookAt(this.mob.getEyeLocation().add(vector), this.speed, this.maxPitch);
               break;
            case 1:
               this.mob.lookAt((Location)this.target, this.speed, this.maxPitch);
               break;
            case 2:
               this.mob.lookAt((Entity)this.target, this.speed, this.maxPitch);
         }

         this.ticks++;
      }

      public GoalKey<Mob> getKey() {
         return SKRIPT_LOOK_KEY;
      }

      public EnumSet<GoalType> getTypes() {
         return LOOK_GOAL;
      }
   }
}
