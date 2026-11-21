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

@Name("Vectors - Vector from Yaw and Pitch")
@Description({"Creates a vector from a yaw and pitch value."})
@Example("set {_v} to vector from yaw 45 and pitch 45")
@Since({"2.2-dev28"})
public class ExprVectorFromYawAndPitch extends SimpleExpression<Vector> {
   private Expression<Number> pitch;
   private Expression<Number> yaw;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.pitch = (Expression<Number>)exprs[matchedPattern ^ 1];
      this.yaw = (Expression<Number>)exprs[matchedPattern];
      return true;
   }

   protected Vector[] get(Event event) {
      Number skriptYaw = this.yaw.getSingle(event);
      Number skriptPitch = this.pitch.getSingle(event);
      if (skriptYaw != null && skriptPitch != null) {
         float yaw = ExprYawPitch.fromSkriptYaw(wrapAngleDeg(skriptYaw.floatValue()));
         float pitch = ExprYawPitch.fromSkriptPitch(wrapAngleDeg(skriptPitch.floatValue()));
         return CollectionUtils.array(ExprYawPitch.fromYawAndPitch(yaw, pitch));
      } else {
         return null;
      }
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
      return (Expression<? extends Vector>)(this.pitch instanceof Literal && this.yaw instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "vector from yaw " + this.yaw.toString(event, debug) + " and pitch " + this.pitch.toString(event, debug);
   }

   public static float wrapAngleDeg(float angle) {
      angle %= 360.0F;
      if (angle <= -180.0F) {
         return angle + 360.0F;
      } else {
         return angle > 180.0F ? angle - 360.0F : angle;
      }
   }

   static {
      Skript.registerExpression(
         ExprVectorFromYawAndPitch.class,
         Vector.class,
         ExpressionType.COMBINED,
         "[a] [new] vector (from|with) yaw %number% and pitch %number%",
         "[a] [new] vector (from|with) pitch %number% and yaw %number%"
      );
   }
}
