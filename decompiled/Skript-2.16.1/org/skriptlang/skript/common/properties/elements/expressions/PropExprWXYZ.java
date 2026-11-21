package org.skriptlang.skript.common.properties.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("WXYZ Component/Coordinate")
@Description({"Gets or changes the W, X, Y or Z component of anything with these components/coordinates, like locations, vectors, or quaternions.", "The W axis is only used for quaternions, currently."})
@Example.Examples({@Example("set {_v} to vector(1, 2, 3)\nsend \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"\nadd 1 to x of {_v}\nadd 2 to y of {_v}\nadd 3 to z of {_v}\nsend \"%x of {_v}%, %y of {_v}%, %z of {_v}%\"\nset x component of {_v} to 1\nset y component of {_v} to 2\nset z component of {_v} to 3\nsend \"%x component of {_v}%, %y component of {_v}%, %z component of {_v}%\"\n"), @Example("set {_x} to x of player\nset {_z} to z of player\nif:\n\t{_x} is between 0 and 100\n\t{_z} is between 0 and 100\nthen:\n\tset y component of player's velocity to 10\n")})
@Since({"2.2-dev28, 2.10 (quaternions)"})
@Keywords({"component", "coord", "coordinate", "x", "y", "z", "xyz"})
@RelatedProperty("wxyz component")
public class PropExprWXYZ extends PropertyBaseExpression<WXYZHandler<?, ?>> {
   private WXYZHandler.Axis axis;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)PropertyExpression.infoBuilder(
                  PropExprWXYZ.class, Object.class, "(:x|:y|:z|:w)( |-)[component[s]|coord[inate][s]|dep:(pos[ition[s]]|loc[ation][s])]", "objects", false
               )
               .supplier(PropExprWXYZ::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.axis = WXYZHandler.Axis.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
      if (!super.init(expressions, matchedPattern, isDelayed, parseResult)) {
         return false;
      } else {
         for (Entry<Class<?>, Property.PropertyInfo<WXYZHandler<?, ?>>> entry : new ArrayList<>(this.properties.entrySet())) {
            Property.PropertyInfo<WXYZHandler<?, ?>> propertyInfo = entry.getValue();
            Class<?> type = entry.getKey();
            WXYZHandler<?, ?> handler = propertyInfo.handler();
            if (!handler.supportsAxis(this.axis)) {
               this.properties.remove(type);
            } else {
               propertyInfo.handler().axis(this.axis);
            }
         }

         if (this.properties.isEmpty()) {
            Skript.error("None of the types returned by " + this.expr + " have an " + this.axis.name().toLowerCase(Locale.ENGLISH) + " axis component.");
            return false;
         } else {
            if (parseResult.hasTag("dep")) {
               Skript.warning(
                  "Using 'pos[ition]' or 'loc[ation]' to refer to specific coordinates is deprecated and will be removed. Please use 'coord[inate]', 'component[s]' or just the axis name 'x of {loc}' instead."
               );
            }

            return true;
         }
      }
   }

   public WXYZHandler.Axis axis() {
      return this.axis;
   }

   @NotNull
   @Override
   public Property<WXYZHandler<?, ?>> getProperty() {
      return Property.WXYZ;
   }

   @Override
   public String getPropertyName() {
      return this.axis.name();
   }
}
