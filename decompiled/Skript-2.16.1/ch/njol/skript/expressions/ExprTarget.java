package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.function.Predicate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Target")
@Description({"For players this is the entity at the crosshair.", "For mobs and experience orbs this is the entity they are attacking/following (if any).", "The 'ray size' and 'ignoring blocks' options are only valid for players' targets.", "The 'ray size' option effectively increases the area around the crosshair an entity can be in. It does so by expanding the hitboxes of entities by the given amount. Display entities have a hit box of 0, so using the 'ray size' option can be helpful when targeting them.", "May grab entities in unloaded chunks."})
@Example.Examples({@Example("on entity target:\n\tif entity's target is a player:\n\t\tsend \"You're being followed by an %entity%!\" to target of entity\n"), @Example("reset target of entity # Makes the entity target-less"), @Example("delete targeted entity of player # for players it will delete the target"), @Example("delete target of last spawned zombie # for entities it will make them target-less")})
@Since({"1.4.2, 2.7 (Reset), 2.8.0 (ignore blocks, ray size)"})
public class ExprTarget extends PropertyExpression<LivingEntity, Entity> {
   private static boolean ignoreBlocks;
   private static int targetBlockDistance;
   @Nullable
   private Expression<Number> raysize;
   @Nullable
   private EntityData<?> type;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.type = exprs[matchedPattern] == null ? null : (EntityData)exprs[matchedPattern].getSingle(null);
      this.setExpr((Expression<? extends LivingEntity>)exprs[1 - matchedPattern]);
      targetBlockDistance = SkriptConfig.maxTargetBlockDistance.value();
      if (targetBlockDistance < 0) {
         targetBlockDistance = 100;
      }

      ignoreBlocks = parser.hasTag("blocks");
      this.raysize = (Expression<Number>)exprs[2];
      return true;
   }

   protected Entity[] get(Event event, LivingEntity[] source) {
      double raysize = this.raysize != null ? this.raysize.getOptionalSingle(event).orElse(0.0).doubleValue() : 0.0;
      return this.get(source, entity -> {
         if (event instanceof EntityTargetEvent && entity.equals(((EntityTargetEvent)event).getEntity()) && !Delay.isDelayed(event)) {
            Entity target = ((EntityTargetEvent)event).getTarget();
            return target != null && (this.type == null || this.type.isInstance(target)) ? target : null;
         } else {
            return getTarget(entity, (EntityData<? extends Entity>)this.type, raysize);
         }
      });
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case SET:
         case RESET:
         case DELETE:
            return CollectionUtils.array(LivingEntity.class);
         default:
            return super.acceptChange(mode);
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET && mode != Changer.ChangeMode.DELETE) {
         super.change(event, delta, mode);
      } else {
         LivingEntity target = delta == null ? null : (LivingEntity)delta[0];
         if (event instanceof EntityTargetEvent targetEvent) {
            for (LivingEntity entity : this.getExpr().getArray(event)) {
               if (entity.equals(targetEvent.getEntity())) {
                  targetEvent.setTarget(target);
               }
            }
         } else {
            double raysize = this.raysize != null ? this.raysize.getOptionalSingle(event).orElse(0.0).doubleValue() : 0.0;

            for (LivingEntity entityx : this.getExpr().getArray(event)) {
               if (entityx instanceof Mob) {
                  ((Mob)entityx).setTarget(target);
               } else if (entityx instanceof Player && mode == Changer.ChangeMode.DELETE) {
                  Entity playerTarget = getTarget(entityx, (EntityData<Entity>)this.type, raysize);
                  if (playerTarget != null && !(playerTarget instanceof OfflinePlayer)) {
                     playerTarget.remove();
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean setTime(int time) {
      return time != EventValues.TIME_PAST ? super.setTime(time, EntityTargetEvent.class, new Expression[]{this.getExpr()}) : super.setTime(time);
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return (Class<? extends Entity>)(this.type != null ? this.type.getType() : Entity.class);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "target" + (this.type == null ? "" : "ed " + this.type) + (this.getExpr().isDefault() ? "" : " of " + this.getExpr().toString(event, debug));
   }

   @Nullable
   public static <T extends Entity> T getTarget(LivingEntity origin, @Nullable EntityData<T> type, double raysize) {
      if (!(origin instanceof Mob)) {
         Predicate<Entity> predicate = entity -> {
            if (entity.equals(origin)) {
               return false;
            } else {
               return type != null && !type.isInstance(entity) ? false : !(entity instanceof Player) || ((Player)entity).getGameMode() != GameMode.SPECTATOR;
            }
         };
         Location eyes = origin.getEyeLocation();
         Vector direction = origin.getLocation().getDirection();
         double distance = targetBlockDistance;
         if (!ignoreBlocks) {
            RayTraceResult blockResult = origin.getWorld().rayTraceBlocks(eyes, direction, targetBlockDistance);
            if (blockResult != null) {
               Vector hit = blockResult.getHitPosition();
               distance = eyes.toVector().distance(hit);
            }
         }

         RayTraceResult result = origin.getWorld().rayTraceEntities(eyes, direction, distance, raysize, predicate);
         if (result == null) {
            return null;
         } else {
            Entity hitEntity = result.getHitEntity();
            return (T)(hitEntity == null ? null : result.getHitEntity());
         }
      } else {
         return (T)(((Mob)origin).getTarget() != null && (type == null || type.isInstance(((Mob)origin).getTarget())) ? ((Mob)origin).getTarget() : null);
      }
   }

   static {
      Skript.registerExpression(
         ExprTarget.class,
         Entity.class,
         ExpressionType.PROPERTY,
         "[the] target[[ed] %-*entitydata%] [of %livingentities%] [blocks:ignoring blocks] [[with|at] [a] ray[ ]size [of] %-number%]",
         "%livingentities%'[s] target[[ed] %-*entitydata%] [blocks:ignoring blocks] [[with|at] [a] ray[ ]size [of] %-number%]"
      );
   }
}
