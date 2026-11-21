package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Vectors - Cylindrical Shape")
@Description({"Forms a 'cylindrical shaped' vector using yaw to manipulate the current point."})
@Example.Examples({@Example("loop 360 times:\n\tset {_v} to cylindrical vector radius 1, yaw loop-value, height 2\n"), @Example("set {_v} to cylindrical vector radius 1, yaw 90, height 2")})
@Since({"2.2-dev28"})
public class ExprVectorCylindrical extends SimpleExpression<Vector> {
   private static final double DEG_TO_RAD = Math.PI / 180.0;
   private Expression<Number> radius;
   private Expression<Number> yaw;
   private Expression<Number> height;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.radius = (Expression<Number>)exprs[0];
      this.yaw = (Expression<Number>)exprs[1];
      this.height = (Expression<Number>)exprs[2];
      return true;
   }

   protected Vector[] get(Event event) {
      Number radius = this.radius.getSingle(event);
      Number yaw = this.yaw.getSingle(event);
      Number height = this.height.getSingle(event);
      return radius != null && yaw != null && height != null
         ? CollectionUtils.array(fromCylindricalCoordinates(radius.doubleValue(), ExprYawPitch.fromSkriptYaw(yaw.floatValue()), height.doubleValue()))
         : null;
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Vector> getReturnType() {
      return Vector.class;
   }

   @Override
   public Expression<? extends Vector> simplify() {
      return (Expression<? extends Vector>)(this.radius instanceof Literal && this.yaw instanceof Literal && this.height instanceof Literal
         ? SimplifiedLiteral.fromExpression(this)
         : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "cylindrical vector with radius "
         + this.radius.toString(event, debug)
         + ", yaw "
         + this.yaw.toString(event, debug)
         + " and height "
         + this.height.toString(event, debug);
   }

   public static Vector fromCylindricalCoordinates(double radius, double phi, double height) {
      double r = Math.abs(radius);
      double p = phi * (Math.PI / 180.0);
      double x = r * Math.cos(p);
      double z = r * Math.sin(p);
      return new Vector(x, height, z);
   }

   static {
      Skript.registerExpression(
         ExprVectorCylindrical.class,
         Vector.class,
         ExpressionType.SIMPLE,
         "[a] [new] cylindrical vector [from|with] [radius] %number%, [yaw] %number%(,[ and]| and) [height] %number%"
      );
   }
}
