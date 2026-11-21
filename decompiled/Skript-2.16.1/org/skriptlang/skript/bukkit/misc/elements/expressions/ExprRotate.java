package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.bukkit.misc.rotation.NonMutatingQuaternionRotator;
import org.skriptlang.skript.bukkit.misc.rotation.NonMutatingVectorRotator;
import org.skriptlang.skript.bukkit.misc.rotation.Rotator;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Rotated Quaternion/Vector")
@Description({"Rotates a quaternion or vector around an axis a set amount of degrees, or around all 3 axes at once.", "Vectors can only be rotated around the global X/Y/Z axes, or an arbitrary vector axis.", "Quaternions are more flexible, allowing rotation around the global or local X/Y/Z axes, arbitrary vectors, or all 3 local axes at once.", "Global axes are the ones in the Minecraft world. Local axes are relative to how the quaternion is already oriented.", "", "Note that rotating a quaternion around a vector results in a rotation around the local vector, so results may not be what you expect. For example, rotating around vector(1, 0, 0) is the same as rotating around the local X axis.", "The same applies to rotations by all three axes at once. In addition, rotating around all three axes of a quaternion/display at once will rotate in ZYX order, meaning the Z rotation will be applied first and the X rotation last."})
@Example.Examples({@Example("set {_new} to {_quaternion} rotated around x axis by 10 degrees"), @Example("set {_new} to {_vector} rotated around vector(1, 1, 1) by 45"), @Example("set {_new} to {_quaternion} rotated by x 45, y 90, z 135")})
@Since({"2.10"})
public class ExprRotate extends SimpleExpression<Object> {
   private Expression<?> toRotate;
   @UnknownNullability
   private Expression<Number> angle;
   @UnknownNullability
   private Expression<Vector> vector;
   @UnknownNullability
   private Rotator.Axis axis;
   @UnknownNullability
   private Expression<Number> x;
   @UnknownNullability
   private Expression<Number> y;
   @UnknownNullability
   private Expression<Number> z;
   private int matchedPattern;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(ExprRotate.class, Object.class)
                  .supplier(ExprRotate::new))
               .addPatterns(
                  new String[]{
                     "%quaternions/vectors% rotated around [the] [global] (:x|:y|:z)(-| )axis by %number%",
                     "%quaternions% rotated around [the|its|their] local (:x|:y|:z)(-| )ax(i|e)s by %number%",
                     "%quaternions/vectors% rotated around [the] %vector% by %number%",
                     "%quaternions% rotated by x %number%, y %number%(, [and]| and) z %number%"
                  }
               ))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.toRotate = exprs[0];
      this.matchedPattern = matchedPattern;
      switch (matchedPattern) {
         case 0:
         case 1:
            String axisString = parseResult.tags.get(0).toUpperCase(Locale.ENGLISH);
            if (matchedPattern == 1) {
               axisString = "LOCAL_" + axisString;
            }

            this.angle = (Expression<Number>)exprs[1];
            this.axis = Rotator.Axis.valueOf(axisString);
            break;
         case 2:
            this.vector = (Expression<Vector>)exprs[1];
            this.angle = (Expression<Number>)exprs[2];
            this.axis = Rotator.Axis.ARBITRARY;
            break;
         case 3:
            this.x = (Expression<Number>)exprs[1];
            this.y = (Expression<Number>)exprs[2];
            this.z = (Expression<Number>)exprs[3];
      }

      return true;
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      if (this.matchedPattern == 3) {
         Number x = this.x.getSingle(event);
         Number y = this.y.getSingle(event);
         Number z = this.z.getSingle(event);
         if (x != null && y != null && z != null) {
            float radX = (float)(x.floatValue() * Math.PI / 180.0);
            float radY = (float)(y.floatValue() * Math.PI / 180.0);
            float radZ = (float)(z.floatValue() * Math.PI / 180.0);
            return this.toRotate.stream(event).map(quaternion -> quaternion.rotateZYX(radZ, radY, radX)).toArray(Quaternionf[]::new);
         } else {
            return new Quaternionf[0];
         }
      } else {
         Number angle = this.angle.getSingle(event);
         if (angle == null) {
            return new Object[0];
         } else {
            double radAngle = angle.doubleValue() * Math.PI / 180.0;
            if (!Double.isInfinite(radAngle) && !Double.isNaN(radAngle)) {
               Rotator<Vector> vectorRotator;
               Rotator<Quaternionf> quaternionRotator;
               if (this.axis == Rotator.Axis.ARBITRARY) {
                  Vector axis = this.vector.getSingle(event);
                  if (axis == null || axis.isZero()) {
                     return new Object[0];
                  }

                  axis.normalize();
                  Vector3f jomlAxis = axis.toVector3f();
                  vectorRotator = new NonMutatingVectorRotator(Rotator.Axis.ARBITRARY, axis, radAngle);
                  quaternionRotator = new NonMutatingQuaternionRotator(Rotator.Axis.LOCAL_ARBITRARY, jomlAxis, (float)radAngle);
               } else {
                  vectorRotator = new NonMutatingVectorRotator(this.axis, radAngle);
                  quaternionRotator = new NonMutatingQuaternionRotator(this.axis, (float)radAngle);
               }

               return this.toRotate.stream(event).map(object -> {
                  if (object instanceof Vector vectorToRotate) {
                     return (Cloneable)vectorRotator.rotate(vectorToRotate);
                  } else {
                     return object instanceof Quaternionf quaternion ? (Cloneable)quaternionRotator.rotate(quaternion) : null;
                  }
               }).filter(Objects::nonNull).toArray();
            } else {
               return new Object[0];
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.toRotate.isSingle();
   }

   @Override
   public Class<?> getReturnType() {
      return this.matchedPattern != 1 && this.matchedPattern != 3 ? this.toRotate.getReturnType() : Quaternionf.class;
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return new Class[]{Quaternionf.class, Vector.class};
   }

   @Override
   public Expression<?> simplify() {
      if (this.toRotate instanceof Literal) {
         switch (this.matchedPattern) {
            case 0:
            case 1:
               if (this.angle instanceof Literal) {
                  return SimplifiedLiteral.fromExpression(this);
               }
               break;
            case 2:
               if (this.angle instanceof Literal && this.vector instanceof Literal) {
                  return SimplifiedLiteral.fromExpression(this);
               }
               break;
            case 3:
               if (this.x instanceof Literal && this.y instanceof Literal && this.z instanceof Literal) {
                  return SimplifiedLiteral.fromExpression(this);
               }
         }
      }

      return this;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return switch (this.matchedPattern) {
         case 0, 1 -> this.toRotate.toString(event, debug) + " rotated around the " + this.axis + "-axis by " + this.angle.toString(event, debug) + " degrees";
         case 2 -> this.toRotate.toString(event, debug)
            + " rotated around "
            + this.vector.toString(event, debug)
            + "-axis by "
            + this.angle.toString(event, debug)
            + " degrees";
         case 3 -> this.toRotate.toString(event, debug)
            + " rotated by x "
            + this.x.toString(event, debug)
            + ", y "
            + this.y.toString(event, debug)
            + ", and z "
            + this.z.toString(event, debug);
         default -> "invalid";
      };
   }
}
