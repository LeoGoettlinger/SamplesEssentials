package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import org.bukkit.util.Vector;

@Name("Vectors - Squared Length")
@Description({"Gets the squared length of a vector."})
@Example("send \"%squared length of vector 1, 2, 3%\"")
@Since({"2.2-dev28"})
public class ExprVectorSquaredLength extends SimplePropertyExpression<Vector, Number> {
   public Number convert(Vector vector) {
      return vector.lengthSquared();
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
      return "squared length of vector";
   }

   static {
      register(ExprVectorSquaredLength.class, Number.class, "squared length[s]", "vectors");
   }
}
