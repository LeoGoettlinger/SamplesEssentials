package org.skriptlang.skript.bukkit.worldborder.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Center of World Border")
@Description({"The center of a world border."})
@Example("set world border center of {_worldborder} to location(10, 0, 20)")
@Since({"2.11"})
public class ExprWorldBorderCenter extends SimplePropertyExpression<WorldBorder, Location> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprWorldBorderCenter.class, Location.class, "world[ ]border (center|middle)", "worldborders", true
               )
               .supplier(ExprWorldBorderCenter::new))
            .build()
      );
   }

   @Nullable
   public Location convert(WorldBorder worldBorder) {
      return worldBorder.getCenter();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, RESET -> (Class[])CollectionUtils.array(Location.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Location location = mode == Changer.ChangeMode.SET ? (Location)delta[0] : new Location(null, 0.0, 0.0, 0.0);
      if (!Double.isNaN(location.getX()) && !Double.isNaN(location.getZ())) {
         for (WorldBorder worldBorder : this.getExpr().getArray(event)) {
            switch (mode) {
               case SET:
                  if (Math.abs(location.getX()) > worldBorder.getMaxCenterCoordinate()) {
                     location.setX(worldBorder.getMaxCenterCoordinate() * Math.signum(location.getX()));
                  }

                  if (Math.abs(location.getZ()) > worldBorder.getMaxCenterCoordinate()) {
                     location.setZ(worldBorder.getMaxCenterCoordinate() * Math.signum(location.getZ()));
                  }

                  worldBorder.setCenter(location);
                  break;
               case RESET:
                  worldBorder.setCenter(0.0, 0.0);
            }
         }
      } else {
         this.error("Your location can't have a NaN value as one of its components");
      }
   }

   @Override
   public Class<? extends Location> getReturnType() {
      return Location.class;
   }

   @Override
   protected String getPropertyName() {
      return "world border center";
   }
}
