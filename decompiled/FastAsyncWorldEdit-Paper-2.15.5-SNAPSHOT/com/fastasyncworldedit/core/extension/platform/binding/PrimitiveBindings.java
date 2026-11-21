package com.fastasyncworldedit.core.extension.platform.binding;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.Locale;
import javax.annotation.Nullable;

public class PrimitiveBindings extends Bindings {
   public PrimitiveBindings(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Binding
   public Expression getExpression(String argument) throws ExpressionException {
      try {
         Expression expression = Expression.compile(argument);
         expression.optimize();
         return expression;
      } catch (EvaluationException var3) {
         throw new InputParseException(String.format("Expected '%s' to be a valid number (or a valid mathematical expression)", argument));
      } catch (ExpressionException var4) {
         throw new InputParseException(String.format("Expected '%s' to be a number or valid math expression (error: %s)", argument, var4.getMessage()));
      }
   }

   @Binding
   public Boolean getBoolean(String argument) {
      String var2 = argument.toLowerCase(Locale.ROOT);

      return switch (var2) {
         case "" -> throw new InputParseException(Caption.of("fawe.error.input-parser-exception"));
         case "true", "yes", "on", "y", "1", "t" -> true;
         case "false", "no", "off", "f", "n", "0" -> false;
         default -> throw new InputParseException(Caption.of("fawe.error.invalid-boolean", TextComponent.of(argument)));
      };
   }

   @Binding
   public Vector3 getVector3(String argument) {
      String[] radii = argument.split(",");
      double radiusX;
      double radiusY;
      double radiusZ;
      switch (radii.length) {
         case 1:
            radiusX = radiusY = radiusZ = parseNumericInput(radii[0]);
            break;
         case 3:
            radiusX = parseNumericInput(radii[0]);
            radiusY = parseNumericInput(radii[1]);
            radiusZ = parseNumericInput(radii[2]);
            break;
         default:
            throw new InputParseException("You must either specify 1 or 3 radius values.");
      }

      return Vector3.at(radiusX, radiusY, radiusZ);
   }

   @Binding
   public Vector2 getVector2(String argument) {
      String[] radii = argument.split(",");
      double radiusX;
      double radiusZ;
      switch (radii.length) {
         case 1:
            radiusX = radiusZ = parseNumericInput(radii[0]);
            break;
         case 2:
            radiusX = parseNumericInput(radii[0]);
            radiusZ = parseNumericInput(radii[1]);
            break;
         default:
            throw new InputParseException("You must either specify 1 or 2 radius values.");
      }

      return Vector2.at(radiusX, radiusZ);
   }

   @Binding
   public BlockVector3 getBlockVector3(String argument) {
      String[] radii = argument.split(",");
      double radiusX;
      double radiusY;
      double radiusZ;
      switch (radii.length) {
         case 1:
            radiusX = radiusY = radiusZ = parseNumericInput(radii[0]);
            break;
         case 3:
            radiusX = parseNumericInput(radii[0]);
            radiusY = parseNumericInput(radii[1]);
            radiusZ = parseNumericInput(radii[2]);
            break;
         default:
            throw new InputParseException("You must either specify 1 or 3 radius values.");
      }

      return BlockVector3.at(radiusX, radiusY, radiusZ);
   }

   @Binding
   public BlockVector2 getBlockVector2(String argument) {
      String[] radii = argument.split(",");
      double radiusX;
      double radiusZ;
      switch (radii.length) {
         case 1:
            radiusX = radiusZ = parseNumericInput(radii[0]);
            break;
         case 2:
            radiusX = parseNumericInput(radii[0]);
            radiusZ = parseNumericInput(radii[1]);
            break;
         default:
            throw new InputParseException("You must either specify 1 or 2 radius values.");
      }

      return BlockVector2.at(radiusX, radiusZ);
   }

   @Nullable
   public static Double parseNumericInput(@Nullable String input) {
      if (input == null) {
         return null;
      } else {
         try {
            return Double.parseDouble(input);
         } catch (NumberFormatException var5) {
            try {
               Expression expression = Expression.compile(input);
               return expression.evaluate();
            } catch (EvaluationException var3) {
               throw new InputParseException(String.format("Expected '%s' to be a valid number (or a valid mathematical expression)", input));
            } catch (ExpressionException var4) {
               throw new InputParseException(String.format("Expected '%s' to be a number or valid math expression (error: %s)", input, var4.getMessage()));
            }
         }
      }
   }
}
