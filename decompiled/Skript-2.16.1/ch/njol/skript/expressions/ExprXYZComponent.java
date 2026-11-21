package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
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
import ch.njol.util.coll.CollectionUtils;
import java.util.Locale;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;

@Name("Vector/Quaternion - WXYZ Component")
@Description({"Gets or changes the W, X, Y or Z component of <a href='#vector'>vectors</a>/<a href='#quaternion'>quaternions</a>.", "You cannot use the W component with vectors; it is for quaternions only."})
@Example("set {_v} to vector 1, 2, 3\nsend \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"\nadd 1 to x of {_v}\nadd 2 to y of {_v}\nadd 3 to z of {_v}\nsend \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"\nset x component of {_v} to 1\nset y component of {_v} to 2\nset z component of {_v} to 3\nsend \"%x component of {_v}%, %y component of {_v}%, %z component of {_v}%\"\n")
@Since({"2.2-dev28, 2.10 (quaternions)"})
public class ExprXYZComponent extends SimplePropertyExpression<Object, Number> {
   private static final boolean IS_RUNNING_1194 = Skript.isRunningMinecraft(1, 19, 4);
   @UnknownNullability
   private ExprXYZComponent.Axis axis;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.axis = ExprXYZComponent.Axis.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Number convert(Object object) {
      if (object instanceof Vector vector) {
         return switch (this.axis) {
            case W -> null;
            case X -> vector.getX();
            case Y -> vector.getY();
            case Z -> vector.getZ();
         };
      } else if (object instanceof Quaternionf quaternion) {
         return switch (this.axis) {
            case W -> quaternion.w();
            case X -> quaternion.x();
            case Y -> quaternion.y();
            case Z -> quaternion.z();
         };
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE || mode == Changer.ChangeMode.SET) {
         boolean acceptsChange;
         if (IS_RUNNING_1194) {
            acceptsChange = Changer.ChangerUtils.acceptsChange(this.getExpr(), Changer.ChangeMode.SET, Vector.class, Quaternionf.class);
         } else {
            acceptsChange = Changer.ChangerUtils.acceptsChange(this.getExpr(), Changer.ChangeMode.SET, Vector.class);
         }

         if (acceptsChange) {
            return CollectionUtils.array(Number.class);
         }
      }

      return null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null;

      double value = ((Number)delta[0]).doubleValue();
      boolean acceptsVectors = Changer.ChangerUtils.acceptsChange(this.getExpr(), Changer.ChangeMode.SET, Vector.class);
      boolean acceptsQuaternions = Changer.ChangerUtils.acceptsChange(this.getExpr(), Changer.ChangeMode.SET, Quaternionf.class);
      this.getExpr().changeInPlace(event, object -> {
         if (acceptsVectors && object instanceof Vector vector) {
            changeVector(vector, this.axis, value, mode);
         } else if (acceptsQuaternions && object instanceof Quaternionf quaternion) {
            changeQuaternion(quaternion, this.axis, (float)value, mode);
         }

         return object;
      });
   }

   private static void changeVector(Vector vector, ExprXYZComponent.Axis axis, double value, Changer.ChangeMode mode) {
      if (axis != ExprXYZComponent.Axis.W) {
         switch (mode) {
            case REMOVE:
               value = -value;
            case ADD:
               switch (axis) {
                  case X:
                     vector.setX(vector.getX() + value);
                     return;
                  case Y:
                     vector.setY(vector.getY() + value);
                     return;
                  case Z:
                     vector.setZ(vector.getZ() + value);
                     return;
                  default:
                     return;
               }
            case SET:
               switch (axis) {
                  case X:
                     vector.setX(value);
                     return;
                  case Y:
                     vector.setY(value);
                     return;
                  case Z:
                     vector.setZ(value);
                     return;
                  default:
                     return;
               }
            default:
               assert false;
         }
      }
   }

   private static void changeQuaternion(Quaternionf quaternion, ExprXYZComponent.Axis axis, float value, Changer.ChangeMode mode) {
      float x;
      float y;
      float z;
      float w;
      x = quaternion.x();
      y = quaternion.y();
      z = quaternion.z();
      w = quaternion.w();
      label22:
      switch (mode) {
         case REMOVE:
            value = -value;
         case ADD:
            switch (axis) {
               case W:
                  w += value;
                  break label22;
               case X:
                  x += value;
                  break label22;
               case Y:
                  y += value;
                  break label22;
               case Z:
                  z += value;
               default:
                  break label22;
            }
         case SET:
            switch (axis) {
               case W:
                  w = value;
                  break;
               case X:
                  x = value;
                  break;
               case Y:
                  y = value;
                  break;
               case Z:
                  z = value;
            }
      }

      quaternion.set(x, y, z, w);
   }

   @Override
   public Class<Number> getReturnType() {
      return Number.class;
   }

   @Override
   public Expression<? extends Number> simplify() {
      return (Expression<? extends Number>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Override
   protected String getPropertyName() {
      return this.axis.name().toLowerCase(Locale.ENGLISH) + " component";
   }

   static {
      String types = "vectors";
      if (IS_RUNNING_1194) {
         types = types + "/quaternions";
      }

      if (!SkriptConfig.useTypeProperties.value()) {
         register(ExprXYZComponent.class, Number.class, "[vector|quaternion] (:w|:x|:y|:z) [component[s]]", types);
      }
   }

   private static enum Axis {
      W,
      X,
      Y,
      Z;
   }
}
