package ch.njol.skript.expressions;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Coordinate")
@Description({"Represents a given coordinate of a location. "})
@Example("player's y-coordinate is smaller than 40:\n\tmessage \"Watch out for lava!\"\n")
@Since({"1.4.3"})
public class ExprCoordinate extends SimplePropertyExpression<Location, Number> {
   private static final char[] axes;
   private int axis;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      super.init(exprs, matchedPattern, isDelayed, parseResult);
      this.axis = parseResult.mark;
      return true;
   }

   public Number convert(Location l) {
      return this.axis == 0 ? l.getX() : (this.axis == 1 ? l.getY() : l.getZ());
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE)
            && this.getExpr().isSingle()
            && Changer.ChangerUtils.acceptsChange(this.getExpr(), Changer.ChangeMode.SET, Location.class)
         ? new Class[]{Number.class}
         : null;
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      assert delta != null;

      Location l = this.getExpr().getSingle(e);
      if (l != null) {
         double n = ((Number)delta[0]).doubleValue();
         switch (mode) {
            case REMOVE:
               n = -n;
            case ADD:
               if (this.axis == 0) {
                  l.setX(l.getX() + n);
               } else if (this.axis == 1) {
                  l.setY(l.getY() + n);
               } else {
                  l.setZ(l.getZ() + n);
               }

               this.getExpr().change(e, new Location[]{l}, Changer.ChangeMode.SET);
               break;
            case SET:
               if (this.axis == 0) {
                  l.setX(n);
               } else if (this.axis == 1) {
                  l.setY(n);
               } else {
                  l.setZ(n);
               }

               this.getExpr().change(e, new Location[]{l}, Changer.ChangeMode.SET);
               break;
            case DELETE:
            case REMOVE_ALL:
            case RESET:
               assert false;
         }
      }
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
      return "the " + axes[this.axis] + "-coordinate";
   }

   static {
      if (!SkriptConfig.useTypeProperties.value()) {
         register(ExprCoordinate.class, Number.class, "(0¦x|1¦y|2¦z)(-| )(coord[inate]|pos[ition]|loc[ation])[s]", "locations");
      }

      axes = new char[]{'x', 'y', 'z'};
   }
}
