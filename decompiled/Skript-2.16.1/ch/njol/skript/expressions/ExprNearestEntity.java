package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.lang.reflect.Array;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Nearest Entity")
@Description({"Gets the entity nearest to a location or another entity."})
@Example.Examples({@Example("kill the nearest pig and cow relative to player"), @Example("teleport player to the nearest cow relative to player"), @Example("teleport player to the nearest entity relative to player"), @Example("on click:\n\tkill nearest pig\n")})
@Since({"2.7"})
public class ExprNearestEntity extends SimpleExpression<Entity> {
   private EntityData<?>[] entityDatas;
   private Expression<?> relativeTo;
   @Nullable
   private transient Class<? extends Entity> knownReturnType;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entityDatas = (EntityData<?>[])((Literal)exprs[0]).getArray();
      if (this.entityDatas.length != Arrays.stream(this.entityDatas).distinct().count()) {
         Skript.error("Entity list may not contain duplicate entities");
         return false;
      } else {
         this.relativeTo = exprs[1];
         return true;
      }
   }

   protected Entity[] get(Event event) {
      Object relativeTo = this.relativeTo.getSingle(event);
      if (relativeTo != null && (!(relativeTo instanceof Location) || ((Location)relativeTo).getWorld() != null)) {
         Entity[] nearestEntities = (Entity[])Array.newInstance(this.getReturnType(), this.entityDatas.length);

         for (int i = 0; i < nearestEntities.length; i++) {
            if (relativeTo instanceof Entity) {
               nearestEntities[i] = this.getNearestEntity(this.entityDatas[i], ((Entity)relativeTo).getLocation(), (Entity)relativeTo);
            } else {
               nearestEntities[i] = this.getNearestEntity(this.entityDatas[i], (Location)relativeTo, null);
            }
         }

         return nearestEntities;
      } else {
         return (Entity[])Array.newInstance(this.getReturnType(), 0);
      }
   }

   @Override
   public boolean isSingle() {
      return this.entityDatas.length == 1;
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      if (this.knownReturnType != null) {
         return this.knownReturnType;
      } else {
         Class<? extends Entity>[] types = new Class[this.entityDatas.length];

         for (int i = 0; i < types.length; i++) {
            types[i] = (Class<? extends Entity>)this.entityDatas[i].getType();
         }

         return this.knownReturnType = Utils.highestDenominator(Entity.class, types);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "nearest " + StringUtils.join(this.entityDatas) + " relative to " + this.relativeTo.toString(event, debug);
   }

   @Nullable
   private Entity getNearestEntity(EntityData<?> entityData, Location relativePoint, @Nullable Entity excludedEntity) {
      Entity nearestEntity = null;
      double nearestDistance = -1.0;

      for (Entity entity : relativePoint.getWorld().getEntitiesByClass(entityData.getType())) {
         if (entity != excludedEntity && entityData.isInstance(entity)) {
            double distance = entity.getLocation().distance(relativePoint);
            if (nearestEntity == null || distance < nearestDistance) {
               nearestDistance = distance;
               nearestEntity = entity;
            }
         }
      }

      return nearestEntity;
   }

   static {
      Skript.registerExpression(
         ExprNearestEntity.class,
         Entity.class,
         ExpressionType.COMBINED,
         "[the] (nearest|closest) %*entitydatas% [[relative] to %entity/location%]",
         "[the] %*entitydatas% (nearest|closest) [to %entity/location%]"
      );
   }
}
