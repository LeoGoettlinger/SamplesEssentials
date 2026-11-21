package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Vectors - Create from Direction")
@Description({"Creates vectors from given directions.", "Relative directions are relative to the origin, (0, 0, 0). Therefore, the vector from the direction 'forwards' is (0, 0, 1)."})
@Example.Examples({@Example("set {_v} to vector from direction upwards"), @Example("set {_v} to vector in direction of player"), @Example("set {_v} to vector in horizontal direction of player"), @Example("set {_v} to vector from facing of player"), @Example("set {_v::*} to vectors from north, south, east, and west")})
@Since({"2.8.0"})
public class ExprVectorFromDirection extends SimpleExpression<Vector> {
   private Expression<Direction> direction;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.direction = (Expression<Direction>)exprs[0];
      if (matchedPattern == 1 && !(this.direction instanceof ExprDirection)) {
         Skript.error("The direction in '%directions% vector[s]' can not be a variable. Use the direction expression instead: 'northwards vector'.");
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   protected Vector[] get(Event event) {
      return this.direction.stream(event).map(Direction::getDirection).toArray(Vector[]::new);
   }

   @Override
   public boolean isSingle() {
      return this.direction.isSingle();
   }

   @Override
   public Class<? extends Vector> getReturnType() {
      return Vector.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "vector " + this.direction.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprVectorFromDirection.class, Vector.class, ExpressionType.PROPERTY, "vector[s] [from] %directions%", "%directions% vector[s]");
   }
}
