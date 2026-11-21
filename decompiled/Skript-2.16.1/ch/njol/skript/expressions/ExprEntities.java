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
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.LogHandler;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Entities")
@Description({"All entities in all worlds, in a specific world, in a chunk, in a radius around a certain location or within two locations. e.g. <code>all players</code>, <code>all creepers in the player's world</code>, or <code>players in radius 100 of the player</code>."})
@Example.Examples({@Example("kill all creepers in the player's world"), @Example("send \"Psst!\" to all players within 100 meters of the player"), @Example("give a diamond to all ops"), @Example("heal all tamed wolves in radius 2000 around {town center}"), @Example("delete all monsters in chunk at player"), @Example("size of all players within {_corner::1} and {_corner::2}}")})
@Since({"1.2.1, 2.5 (chunks), 2.10 (within)"})
public class ExprEntities extends SimpleExpression<Entity> {
   Expression<? extends EntityData<?>> types;
   @UnknownNullability
   private Expression<?> worldsOrChunks;
   @UnknownNullability
   private Expression<Number> radius;
   @UnknownNullability
   private Expression<Location> center;
   @UnknownNullability
   private Expression<Location> from;
   @UnknownNullability
   private Expression<Location> to;
   private Class<? extends Entity> returnType = Entity.class;
   private boolean isUsingRadius;
   private boolean isUsingCuboid;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.types = (Expression<? extends EntityData<?>>)exprs[0];
      if (matchedPattern % 2 == 0) {
         for (EntityData<?> entityType : ((Literal)this.types).getAll()) {
            if (entityType.isPlural().isFalse() || entityType.isPlural().isUnknown() && !StringUtils.startsWithIgnoreCase(parseResult.expr, "all")) {
               return false;
            }
         }
      }

      this.isUsingRadius = matchedPattern == 2 || matchedPattern == 3;
      this.isUsingCuboid = matchedPattern >= 4;
      if (this.isUsingRadius) {
         this.radius = (Expression<Number>)exprs[1];
         this.center = (Expression<Location>)exprs[2];
      } else if (this.isUsingCuboid) {
         this.from = (Expression<Location>)exprs[1];
         this.to = (Expression<Location>)exprs[2];
      } else if (parseResult.mark == 1) {
         this.worldsOrChunks = exprs[2];
      } else {
         this.worldsOrChunks = exprs[1];
      }

      if (this.types instanceof Literal && ((EntityData[])((Literal)this.types).getAll()).length == 1) {
         this.returnType = (Class<? extends Entity>)((Literal)this.types).getSingle().getType();
      }

      return true;
   }

   @Override
   public boolean isLoopOf(String s) {
      if (!(this.types instanceof Literal)) {
         return false;
      } else {
         try (LogHandler ignored = new BlockingLogHandler().start()) {
            EntityData<?> entityData = EntityData.parseWithoutIndefiniteArticle(s);
            if (entityData != null) {
               for (EntityData<?> entityType : ((Literal)this.types).getAll()) {
                  assert entityType != null;

                  if (!entityData.isSupertypeOf(entityType)) {
                     return false;
                  }
               }

               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   protected Entity[] get(Event event) {
      if (!this.isUsingRadius && !this.isUsingCuboid) {
         EntityData<?>[] types = (EntityData<?>[])this.types.getAll(event);
         if (this.worldsOrChunks == null) {
            return EntityData.getAll(types, this.returnType, (World[])null);
         } else {
            List<Chunk> chunks = new ArrayList<>();
            List<World> worlds = new ArrayList<>();

            for (Object obj : this.worldsOrChunks.getArray(event)) {
               if (obj instanceof Chunk chunk) {
                  chunks.add(chunk);
               } else if (obj instanceof World world) {
                  worlds.add(world);
               }
            }

            Set<Entity> entities = new HashSet<>();
            if (!chunks.isEmpty()) {
               entities.addAll(Arrays.asList(EntityData.getAll(types, this.returnType, chunks.toArray(new Chunk[0]))));
            }

            if (!worlds.isEmpty()) {
               entities.addAll(Arrays.asList(EntityData.getAll(types, this.returnType, worlds.toArray(new World[0]))));
            }

            return entities.toArray((Entity[])Array.newInstance(this.returnType, entities.size()));
         }
      } else {
         Iterator<? extends Entity> iter = this.iterator(event);
         if (iter != null && iter.hasNext()) {
            List<Entity> list = new ArrayList<>();

            while (iter.hasNext()) {
               list.add(iter.next());
            }

            return list.toArray((Entity[])Array.newInstance(this.returnType, list.size()));
         } else {
            return null;
         }
      }
   }

   @Nullable
   @Override
   public Iterator<? extends Entity> iterator(Event event) {
      if (this.isUsingRadius) {
         Location location = this.center.getSingle(event);
         if (location == null) {
            return null;
         } else {
            Number number = this.radius.getSingle(event);
            if (number == null) {
               return null;
            } else {
               double rad = number.doubleValue();
               if (location.getWorld() == null) {
                  return null;
               } else {
                  Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, rad, rad, rad);
                  double radiusSquared = rad * rad * 1.00001;
                  EntityData<?>[] entityTypes = (EntityData<?>[])this.types.getAll(event);
                  return new CheckedIterator<>(nearbyEntities.iterator(), entity -> {
                     if (entity != null && !(entity.getLocation().distanceSquared(location) > radiusSquared)) {
                        for (EntityData<?> entityType : entityTypes) {
                           if (entityType.isInstance(entity)) {
                              return true;
                           }
                        }

                        return false;
                     } else {
                        return false;
                     }
                  });
               }
            }
         }
      } else if (this.isUsingCuboid) {
         Location corner1 = this.from.getSingle(event);
         if (corner1 == null) {
            return null;
         } else {
            Location corner2 = this.to.getSingle(event);
            if (corner2 == null) {
               return null;
            } else {
               EntityData<?>[] entityTypes = (EntityData<?>[])this.types.getAll(event);
               World world = corner1.getWorld();
               if (world == null) {
                  world = corner2.getWorld();
               }

               if (world == null) {
                  return null;
               } else {
                  Collection<Entity> entities = corner1.getWorld().getNearbyEntities(BoundingBox.of(corner1, corner2));
                  return new CheckedIterator<>(entities.iterator(), entity -> {
                     if (entity == null) {
                        return false;
                     } else {
                        for (EntityData<?> entityType : entityTypes) {
                           if (entityType.isInstance(entity)) {
                              return true;
                           }
                        }

                        return false;
                     }
                  });
               }
            }
         }
      } else {
         return super.iterator(event);
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return this.returnType;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String message = "all entities of type " + this.types.toString(event, debug);
      if (this.worldsOrChunks != null) {
         message = message + " in " + this.worldsOrChunks.toString(event, debug);
      } else if (this.radius != null && this.center != null) {
         message = message + " in radius " + this.radius.toString(event, debug) + " around " + this.center.toString(event, debug);
      } else if (this.from != null && this.to != null) {
         message = message + " within " + this.from.toString(event, debug) + " and " + this.to.toString(event, debug);
      }

      return message;
   }

   static {
      Skript.registerExpression(
         ExprEntities.class,
         Entity.class,
         ExpressionType.PATTERN_MATCHES_EVERYTHING,
         "[(all [[of] the]|the)] %*entitydatas% [(in|of) (world[s] %-worlds%|1:%-worlds/chunks%)]",
         "[(all [[of] the]|the)] entities of type[s] %entitydatas% [(in|of) (world[s] %-worlds%|1:%-worlds/chunks%)]",
         "[(all [[of] the]|the)] %*entitydatas% (within|[with]in radius) %number% [(block[s]|met(er|re)[s])] (of|around) %location%",
         "[(all [[of] the]|the)] entities of type[s] %entitydatas% in radius %number% (of|around) %location%",
         "[(all [[of] the]|the)] %*entitydatas% within %location% and %location%",
         "[(all [[of] the]|the)] entities of type[s] %entitydatas% within %location% and %location%"
      );
   }
}
