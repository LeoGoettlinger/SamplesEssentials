package org.skriptlang.skript.common.properties.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.doc.Since;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseCondition;
import org.skriptlang.skript.lang.properties.handlers.base.ConditionPropertyHandler;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Empty")
@Description({"Checks whether something is empty."})
@Example("player's inventory is empty")
@Since({"unknown (before 2.1)"})
@RelatedProperty("empty")
public class PropCondIsEmpty extends PropertyBaseCondition<ConditionPropertyHandler<?>> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         PropertyCondition.infoBuilder(PropCondIsEmpty.class, PropertyCondition.PropertyType.BE, "empty", "objects").supplier(PropCondIsEmpty::new).build()
      );
   }

   @NotNull
   @Override
   public Property<ConditionPropertyHandler<?>> getProperty() {
      return Property.IS_EMPTY;
   }
}
