package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.util.coll.CollectionUtils;
import java.util.function.Function;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Vectors - Length")
@Description({"Gets or sets the length of a vector."})
@Example.Examples({@Example("send \"%standard length of vector 1, 2, 3%\""), @Example("set {_v} to vector 1, 2, 3"), @Example("set standard length of {_v} to 2"), @Example("send \"%standard length of {_v}%\"")})
@Since({"2.2-dev28"})
public class ExprVectorLength extends SimplePropertyExpression<Vector, Number> {
   public Number convert(Vector vector) {
      return vector.length();
   }

   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.ADD && mode != Changer.ChangeMode.REMOVE && mode != Changer.ChangeMode.SET ? null : CollectionUtils.array(Number.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null;

      double deltaLength = ((Number)delta[0]).doubleValue();
      Function<Vector, Vector> changeFunction;
      switch (mode) {
         case REMOVE:
            deltaLength = -deltaLength;
         case ADD:
            double finalDeltaLength = deltaLength;
            double finalDeltaLengthSquared = deltaLength * deltaLength;
            changeFunction = vector -> {
               if (!vector.isZero() && (!(finalDeltaLength < 0.0) || !(vector.lengthSquared() < finalDeltaLengthSquared))) {
                  double newLength = finalDeltaLength + vector.length();
                  if (!vector.isNormalized()) {
                     vector.normalize();
                  }

                  vector.multiply(newLength);
               } else {
                  vector.zero();
               }

               return vector;
            };
            break;
         case SET:
            changeFunction = vector -> {
               if (!(deltaLength < 0.0) && !vector.isZero()) {
                  if (!vector.isNormalized()) {
                     vector.normalize();
                  }

                  vector.multiply(deltaLength);
               } else {
                  vector.zero();
               }

               return vector;
            };
            break;
         default:
            return;
      }

      this.getExpr().changeInPlace(event, changeFunction);
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   public Expression<? extends Number> simplify() {
      return (Expression<? extends Number>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   protected String getPropertyName() {
      return "vector length";
   }

   static {
      register(ExprVectorLength.class, Number.class, "(vector|standard|normal) length[s]", "vectors");
   }
}
