package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Length")
@Description({"The length of a text, in number of characters."})
@Example("set {_l} to length of the string argument")
@Since({"2.1"})
public class ExprLength extends SimplePropertyExpression<String, Long> {
   public Long convert(String s) {
      return (long)s.length();
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   public Expression<? extends Long> simplify() {
      return (Expression<? extends Long>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   protected String getPropertyName() {
      return "length";
   }

   static {
      register(ExprLength.class, Long.class, "length", "strings");
   }
}
