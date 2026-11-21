package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import java.util.Locale;
import java.util.function.Function;

@Name("Alpha/Red/Green/Blue Color Value")
@Description({"The alpha, red, green, or blue value of colors. Ranges from 0 to 255.", "Alpha represents opacity."})
@Example.Examples({@Example("broadcast red value of rgb(100, 0, 50) # sends '100'"), @Example("set {_red} to red's red value + 10")})
@Keywords({"ARGB", "RGB", "color", "colour"})
@Since({"2.10"})
public class ExprARGB extends SimplePropertyExpression<Color, Integer> {
   private ExprARGB.RGB color;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.color = ExprARGB.RGB.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   public Integer convert(Color from) {
      return this.color.getValue(from);
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return this.color.name().toLowerCase(Locale.ENGLISH);
   }

   @Override
   public Expression<? extends Integer> simplify() {
      return (Expression<? extends Integer>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   static {
      register(ExprARGB.class, Integer.class, "(:alpha|:red|:green|:blue) (value|component)", "colors");
   }

   private static enum RGB {
      ALPHA(Color::getAlpha),
      RED(Color::getRed),
      GREEN(Color::getGreen),
      BLUE(Color::getBlue);

      private final Function<Color, Integer> get;

      private RGB(Function<Color, Integer> get) {
         this.get = get;
      }

      public int getValue(Color from) {
         return this.get.apply(from);
      }
   }
}
