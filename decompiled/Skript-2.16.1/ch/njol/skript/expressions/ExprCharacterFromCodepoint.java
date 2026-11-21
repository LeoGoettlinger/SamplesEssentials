package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Character from Codepoint")
@Description({"Returns the character at the specified codepoint"})
@Example("function chars_between(lower: string, upper: string) :: strings:\n\tset {_lower} to codepoint of {_lower}\n\treturn {_none} if {_lower} is not set\n\n\tset {_upper} to codepoint of {_upper}\n\treturn {_none} if {_upper} is not set\n\n\tloop integers between {_lower} and {_upper}:\n\t\tadd character from codepoint loop-value to {_chars::*}\n\treturn {_chars::*}\n")
@Since({"2.9.0"})
public class ExprCharacterFromCodepoint extends SimplePropertyExpression<Integer, String> {
   @Nullable
   public String convert(Integer integer) {
      return String.valueOf((char)integer.intValue());
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   public Expression<? extends String> simplify() {
      return (Expression<? extends String>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "character at codepoint " + this.getExpr().toString(event, debug);
   }

   @Override
   protected String getPropertyName() {
      assert false;

      return null;
   }

   static {
      Skript.registerExpression(
         ExprCharacterFromCodepoint.class, String.class, ExpressionType.PROPERTY, "character (from|at|with) code([ ]point| position) %integer%"
      );
   }
}
