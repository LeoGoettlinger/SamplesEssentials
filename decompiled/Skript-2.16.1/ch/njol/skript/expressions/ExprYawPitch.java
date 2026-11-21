package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Name("Yaw / Pitch")
@Description({"The yaw or pitch of a location or vector.", "A yaw of 0 or 360 represents the positive z direction. Adding a positive number to the yaw of a player will rotate it clockwise.", "A pitch of 90 represents the negative y direction, or downward facing. A pitch of -90 represents upward facing. Adding a positive number to the pitch will rotate the direction downwards.", "Only Paper 1.19+ users may directly change the yaw/pitch of players."})
@Example.Examples({@Example("log \"%player%: %location of player%, %player's yaw%, %player's pitch%\" to \"playerlocs.log\""), @Example("set {_yaw} to yaw of player"), @Example("set {_p} to pitch of target entity"), @Example("set pitch of player to -90 # Makes the player look upwards, Paper 1.19+ only"), @Example("add 180 to yaw of target of player # Makes the target look behind themselves")})
@Since({"2.0, 2.2-dev28 (vector yaw/pitch), 2.9.0 (entity changers)"})
public class ExprYawPitch extends SimplePropertyExpression<Object, Float> {
   private static final double DEG_TO_RAD = Math.PI / 180.0;
   private static final double RAD_TO_DEG = 180.0 / Math.PI;
   private boolean usesYaw;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.usesYaw = parseResult.hasTag("yaw");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   public Float convert(Object object) {
      if (object instanceof Entity entity) {
         Location location = entity.getLocation();
         return this.usesYaw ? normalizeYaw(location.getYaw()) : location.getPitch();
      } else if (object instanceof Location location) {
         return this.usesYaw ? normalizeYaw(location.getYaw()) : location.getPitch();
      } else {
         return object instanceof Vector vector ? this.usesYaw ? skriptYaw(getYaw(vector)) : skriptPitch(getPitch(vector)) : null;
      }
   }

   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, RESET -> (Class[])CollectionUtils.array(Number.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      float value = delta == null ? 0.0F : ((Number)delta[0]).floatValue();
      if (Float.isFinite(value)) {
         for (Object object : this.getExpr().getArray(event)) {
            if (object instanceof Entity entity) {
               this.changeForEntity(entity, value, mode);
            } else if (object instanceof Location location) {
               this.changeForLocation(location, value, mode);
            } else if (object instanceof Vector vector) {
               this.changeForVector(vector, value, mode);
            }
         }
      }
   }

   private void changeForEntity(Entity entity, float value, Changer.ChangeMode mode) {
      Location location = entity.getLocation();
      this.changeForLocation(location, value, mode);
      entity.setRotation(location.getYaw(), location.getPitch());
   }

   private void changeForLocation(Location location, float value, Changer.ChangeMode mode) {
      switch (mode) {
         case SET:
            if (this.usesYaw) {
               location.setYaw(value);
            } else {
               location.setPitch(value);
            }
            break;
         case REMOVE:
            value = -value;
         case ADD:
            if (this.usesYaw) {
               location.setYaw(location.getYaw() + value);
            } else {
               location.setPitch(location.getPitch() - value);
            }
            break;
         case RESET:
            if (this.usesYaw) {
               location.setYaw(0.0F);
            } else {
               location.setPitch(0.0F);
            }
      }
   }

   private void changeForVector(Vector vector, float value, Changer.ChangeMode mode) {
      float yaw = getYaw(vector);
      float pitch = getPitch(vector);
      switch (mode) {
         case SET:
            if (this.usesYaw) {
               yaw = fromSkriptYaw(value);
            } else {
               pitch = fromSkriptPitch(value);
            }
            break;
         case REMOVE:
            value = -value;
         case ADD:
            if (this.usesYaw) {
               yaw += value;
            } else {
               pitch -= value;
            }
      }

      Vector newVector = fromYawAndPitch(yaw, pitch).multiply(vector.length());
      vector.copy(newVector);
   }

   private static float normalizeYaw(float yaw) {
      yaw = Location.normalizeYaw(yaw);
      return yaw < 0.0F ? yaw + 360.0F : yaw;
   }

   @Override
   public Class<? extends Float> getReturnType() {
      return Float.class;
   }

   @Override
   protected String getPropertyName() {
      return this.usesYaw ? "yaw" : "pitch";
   }

   @Internal
   public static Vector fromYawAndPitch(float yaw, float pitch) {
      double y = Math.sin(pitch * (Math.PI / 180.0));
      double div = Math.cos(pitch * (Math.PI / 180.0));
      double x = Math.cos(yaw * (Math.PI / 180.0));
      double z = Math.sin(yaw * (Math.PI / 180.0));
      x *= div;
      z *= div;
      return new Vector(x, y, z);
   }

   private static float getYaw(Vector vector) {
      return Double.valueOf(vector.getX()).equals(0.0) && Double.valueOf(vector.getZ()).equals(0.0)
         ? 0.0F
         : (float)(Math.atan2(vector.getZ(), vector.getX()) * (180.0 / Math.PI));
   }

   private static float getPitch(Vector vector) {
      double xy = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
      return (float)(Math.atan(vector.getY() / xy) * (180.0 / Math.PI));
   }

   private static float skriptYaw(float yaw) {
      return yaw < 90.0F ? yaw + 270.0F : yaw - 90.0F;
   }

   private static float skriptPitch(float pitch) {
      return -pitch;
   }

   @Internal
   public static float fromSkriptYaw(float yaw) {
      return yaw > 270.0F ? yaw - 270.0F : yaw + 90.0F;
   }

   @Internal
   public static float fromSkriptPitch(float pitch) {
      return -pitch;
   }

   static {
      register(ExprYawPitch.class, Float.class, "(:yaw|pitch)", "entities/locations/vectors");
   }
}
