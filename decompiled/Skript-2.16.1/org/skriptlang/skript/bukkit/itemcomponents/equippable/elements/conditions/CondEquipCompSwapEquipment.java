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

@Name("Equippable Component - Can Swap Equipment")
@Description({"Whether an item can swap equipment by right clicking with it in your hand.\nThe item will swap places of the set 'equipment slot' of the item. If an equipment slot is not set, defaults to helmet.\nNOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work aas intended.\n"})
@Example.Examples({@Example("if {_item} can swap equipment:\n\tadd \"Swappable\" to lore of {_item}\n"), @Example("set {_component} to the equippable component of {_item}\nif {_component} can not be equipped when right clicked:\n\tmake {_component} swappable\n")})
@RequiredPlugins({"Minecraft 1.21.2+"})
@Since({"2.13"})
public class CondEquipCompSwapEquipment extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         infoBuilder(
               CondEquipCompSwapEquipment.class,
               PropertyCondition.PropertyType.CAN,
               "swap equipment [on right click|when right clicked]",
               "equippablecomponents"
            )
            .supplier(CondEquipCompSwapEquipment::new)
            .build()
      );
   }

   public boolean check(EquippableWrapper wrapper) {
      return wrapper.getComponent().swappable();
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.CAN;
   }

   @Override
   protected String getPropertyName() {
      return "swap equipment";
   }
}
