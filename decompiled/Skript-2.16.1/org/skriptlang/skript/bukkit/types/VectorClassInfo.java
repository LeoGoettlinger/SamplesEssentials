package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import java.io.StreamCorruptedException;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@Internal
public class VectorClassInfo extends ClassInfo<Vector> {
   public VectorClassInfo() {
      super(Vector.class, "vector");
      this.user(new String[]{"vectors?"})
         .name("Vector")
         .description("Vector is a collection of numbers. In Minecraft, 3D vectors are used to express velocities of entities.")
         .usage("vector(x, y, z)")
         .examples("")
         .since("2.2-dev23")
         .defaultExpression(new EventValueExpression<>(Vector.class))
         .parser(new VectorClassInfo.VectorParser())
         .serializer(new VectorClassInfo.VectorSerializer())
         .cloner(Vector::clone)
         .property(Property.WXYZ, "X, Y, or Z component of the vector.", Skript.instance(), new VectorClassInfo.VectorWXYZHandler());
   }

   private static class VectorParser extends Parser<Vector> {
      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(Vector vec, int flags) {
         return "x: " + Skript.toString(vec.getX()) + ", y: " + Skript.toString(vec.getY()) + ", z: " + Skript.toString(vec.getZ());
      }

      public String toVariableNameString(Vector vec) {
         return "vector:" + vec.getX() + "," + vec.getY() + "," + vec.getZ();
      }

      public String getDebugMessage(Vector vec) {
         return "(" + vec.getX() + "," + vec.getY() + "," + vec.getZ() + ")";
      }
   }

   private static class VectorSerializer extends Serializer<Vector> {
      public Fields serialize(Vector o) {
         Fields f = new Fields();
         f.putPrimitive("x", o.getX());
         f.putPrimitive("y", o.getY());
         f.putPrimitive("z", o.getZ());
         return f;
      }

      public void deserialize(Vector o, Fields f) {
         assert false;
      }

      public Vector deserialize(Fields f) throws StreamCorruptedException {
         return new Vector(f.getPrimitive("x", double.class), f.getPrimitive("y", double.class), f.getPrimitive("z", double.class));
      }

      @Override
      public boolean mustSyncDeserialization() {
         return false;
      }

      @Override
      protected boolean canBeInstantiated() {
         return false;
      }
   }

   private static class VectorWXYZHandler extends WXYZHandler<Vector, Double> {
      @Override
      public PropertyHandler<Vector> newInstance() {
         VectorClassInfo.VectorWXYZHandler instance = new VectorClassInfo.VectorWXYZHandler();
         instance.axis(this.axis);
         return instance;
      }

      @Nullable
      public Double convert(Vector propertyHolder) {
         return switch (this.axis) {
            case X -> propertyHolder.getX();
            case Y -> propertyHolder.getY();
            case Z -> propertyHolder.getZ();
            default -> null;
         };
      }

      @Override
      public boolean supportsAxis(WXYZHandler.Axis axis) {
         return axis != WXYZHandler.Axis.W;
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case ADD, SET, REMOVE -> new Class[]{Float.class};
            default -> null;
         };
      }

      public void change(Vector vector, @Nullable Object[] delta, Changer.ChangeMode mode) {
         assert delta != null;

         float value = (Float)delta[0];
         if (this.axis != WXYZHandler.Axis.W) {
            switch (mode) {
               case SET:
                  switch (this.axis) {
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
               case REMOVE:
                  value = -value;
               case ADD:
                  switch (this.axis) {
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
               default:
                  assert false;
            }
         }
      }

      @NotNull
      @Override
      public Class<Double> returnType() {
         return Double.class;
      }

      @Override
      public boolean requiresSourceExprChange() {
         return true;
      }
   }
}
