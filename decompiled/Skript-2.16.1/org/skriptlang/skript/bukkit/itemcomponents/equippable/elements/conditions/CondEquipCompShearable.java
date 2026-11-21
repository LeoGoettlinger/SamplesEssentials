package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Can Be Sheared Off")
@Description({"Whether an item can be sheared off of an entity.\nNOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.\n"})
@Example.Examples({@Example("if {_item} can be sheared off:\n\tadd \"Shearable\" to lore of {_item}\n"), @Example("set {_component} to the equippable component of {_item}\nif {_component} can not be sheared off:\n\tallow {_component} to be sheared off\n")})
@RequiredPlugins({"Minecraft 1.21.6+"})
@Since({"2.13"})
public class CondEquipCompShearable extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {
   public static void register(SyntaxRegistry registry) {
      if (EquippableWrapper.HAS_CAN_BE_SHEARED) {
         registry.register(
            SyntaxRegistry.CONDITION,
            infoBuilder(CondEquipCompShearable.class, PropertyCondition.PropertyType.CAN, "be sheared off [of entities]", "equippablecomponents")
               .supplier(CondEquipCompShearable::new)
               .build()
         );
      }
   }

   public boolean check(EquippableWrapper wrapper) {
      return wrapper.getComponent().canBeSheared();
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.CAN;
   }

   @Override
   protected String getPropertyName() {
      return "be sheared off of entities";
   }
}
