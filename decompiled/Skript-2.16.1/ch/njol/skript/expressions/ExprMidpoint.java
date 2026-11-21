package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Midpoint")
@Description({"Get the midpoint between two vectors or two locations in the same world."})
@Example.Examples({@Example("set {_center} to the midpoint between location(0, 0, 0) and location(10, 10, 10)\nset {_centerBlock} to the block at {_center}\n"), @Example("set {_midpoint} to the mid-point of vector(20, 10, 5) and vector(3, 6, 9)")})
@Since({"2.13"})
public class ExprMidpoint extends SimpleExpression<Object> implements SyntaxRuntimeErrorProducer {
   private Expression<?> object1;
   private Expression<?> object2;
   private Class<?>[] classTypes = null;
   private Class<?> superType;
   private Node node;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.object1 = exprs[0];
      this.object2 = exprs[1];
      Class<?>[] type1 = this.checkExpressionType(this.object1);
      Class<?>[] type2 = this.checkExpressionType(this.object2);
      if (type1.length == 1 && type2.length == 1) {
         if (type1[0] != type2[0]) {
            Skript.error("You can only get the midpoint between two locations or two vectors.");
            return false;
         }

         this.classTypes = type1;
         this.superType = type1[0];
      } else {
         this.classTypes = type1.length > type2.length ? type1 : type2;
         this.superType = Classes.getSuperClassInfo(this.classTypes).getC();
      }

      this.node = this.getParser().getNode();
      return true;
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      Object object1 = this.object1.getSingle(event);
      Object object2 = this.object2.getSingle(event);
      if (object1 != null && object2 != null) {
         if (object1 instanceof Location loc1 && object2 instanceof Location loc2) {
            if (loc1.getWorld() != loc2.getWorld()) {
               this.error("Cannot get the midpoint of two locations in different worlds.");
               return null;
            } else {
               World world = loc1.getWorld();
               Vector vector = loc1.toVector().getMidpoint(loc2.toVector());
               return new Location[]{vector.toLocation(world)};
            }
         } else if (object1 instanceof Vector vector1 && object2 instanceof Vector vector2) {
            return new Vector[]{vector1.getMidpoint(vector2)};
         } else {
            this.error("You can only get the midpoint between two locations or two vectors.");
            return null;
         }
      } else {
         return null;
      }
   }

   private Class<?>[] checkExpressionType(Expression<?> expr) {
      if (expr.canReturn(Location.class)) {
         if (!expr.canReturn(Vector.class)) {
            return new Class[]{Location.class};
         }
      } else if (expr.canReturn(Vector.class)) {
         return new Class[]{Vector.class};
      }

      return new Class[]{Location.class, Vector.class};
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<?> getReturnType() {
      return this.superType;
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.classTypes;
   }

   @Override
   public Node getNode() {
      return this.node;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug).append("the midpoint between").append(this.object1).append("and").append(this.object2).toString();
   }

   static {
      Skript.registerExpression(ExprMidpoint.class, Object.class, ExpressionType.COMBINED, "[the] mid[-]point (of|between) %object% and %object%");
   }
}
