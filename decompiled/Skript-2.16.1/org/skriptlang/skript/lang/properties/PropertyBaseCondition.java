package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;

@Experimental
public abstract class PropertyBaseCondition<Handler extends ConditionPropertyHandler<?>> extends Condition implements PropertyBaseSyntax<Handler> {
   protected Expression<?> propertyHolder;
   private PropertyMap<Handler> properties;
   private final Property<Handler> property = this.getProperty();

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.propertyHolder = PropertyBaseSyntax.asProperty(this.property, expressions[0]);
      if (this.propertyHolder == null) {
         Skript.error(this.getBadTypesErrorMessage(expressions[0]));
         return false;
      } else {
         this.properties = PropertyBaseSyntax.getPossiblePropertyInfos(this.property, this.propertyHolder);
         if (this.properties.isEmpty()) {
            Skript.error(this.getBadTypesErrorMessage(this.propertyHolder));
            return false;
         } else {
            this.setNegated(matchedPattern == 1);
            return true;
         }
      }
   }

   @Override
   public boolean check(Event event) {
      return this.propertyHolder.check(event, element -> {
         ConditionPropertyHandler<Object> handler = this.properties.getHandler(element.getClass());
         return handler == null ? false : handler.check(element);
      }, this.isNegated());
   }

   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.BE;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return PropertyCondition.toString(this, this.getPropertyType(), event, debug, this.propertyHolder, this.getPropertyName());
   }
}
