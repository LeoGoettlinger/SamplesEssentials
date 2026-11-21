package org.skriptlang.skript.common.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Quaternionf;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@Internal
public class QuaternionClassInfo extends ClassInfo<Quaternionf> {
   public QuaternionClassInfo() {
      super(Quaternionf.class, "quaternion");
      this.user(new String[]{"quaternionf?s?"})
         .name("Quaternion")
         .description("Quaternions are four dimensional vectors, often used for representing rotations.")
         .since("2.10")
         .parser(new QuaternionClassInfo.QuaternionParser())
         .defaultExpression(new EventValueExpression<>(Quaternionf.class))
         .cloner(quaternion -> {
            try {
               return (Quaternionf)quaternion.clone();
            } catch (CloneNotSupportedException var2) {
               return null;
            }
         })
         .property(Property.WXYZ, "W, X, Y, or Z component of the quaternion.", Skript.instance(), new QuaternionClassInfo.QuaternionWXYZHandler());
   }

   private static class QuaternionParser extends Parser<Quaternionf> {
      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(Quaternionf quaternion, int flags) {
         return "w:"
            + Skript.toString(quaternion.w())
            + ", x:"
            + Skript.toString(quaternion.x())
            + ", y:"
            + Skript.toString(quaternion.y())
            + ", z:"
            + Skript.toString(quaternion.z());
      }

      public String toVariableNameString(Quaternionf quaternion) {
         return quaternion.w() + "," + quaternion.x() + "," + quaternion.y() + "," + quaternion.z();
      }
   }

   private static class QuaternionWXYZHandler extends WXYZHandler<Quaternionf, Float> {
      @Override
      public PropertyHandler<Quaternionf> newInstance() {
         QuaternionClassInfo.QuaternionWXYZHandler instance = new QuaternionClassInfo.QuaternionWXYZHandler();
         instance.axis(this.axis);
         return instance;
      }

      @NotNull
      public Float convert(Quaternionf propertyHolder) {
         return switch (this.axis) {
            case W -> propertyHolder.w;
            case X -> propertyHolder.x;
            case Y -> propertyHolder.y;
            case Z -> propertyHolder.z;
         };
      }

      @Override
      public boolean supportsAxis(WXYZHandler.Axis axis) {
         return true;
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case ADD, SET, REMOVE -> new Class[]{Float.class};
            default -> null;
         };
      }

      public void change(Quaternionf propertyHolder, @Nullable Object[] delta, Changer.ChangeMode mode) {
         assert delta != null;

         float x;
         float y;
         float z;
         float w;
         float value = (Float)delta[0];
         x = propertyHolder.x();
         y = propertyHolder.y();
         z = propertyHolder.z();
         w = propertyHolder.w();
         label25:
         switch (mode) {
            case SET:
               switch (this.axis) {
                  case W:
                     w = value;
                     break label25;
                  case X:
                     x = value;
                     break label25;
                  case Y:
                     y = value;
                     break label25;
                  case Z:
                     z = value;
                  default:
                     break label25;
               }
            case REMOVE:
               value = -value;
            case ADD:
               switch (this.axis) {
                  case W:
                     w += value;
                     break;
                  case X:
                     x += value;
                     break;
                  case Y:
                     y += value;
                     break;
                  case Z:
                     z += value;
               }
         }

         propertyHolder.set(x, y, z, w);
      }

      @NotNull
      @Override
      public Class<Float> returnType() {
         return Float.class;
      }

      @Override
      public boolean requiresSourceExprChange() {
         return true;
      }
   }
}
