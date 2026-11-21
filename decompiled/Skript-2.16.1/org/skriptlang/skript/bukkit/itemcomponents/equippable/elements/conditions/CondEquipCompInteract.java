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

@Name("Equippable Component - Can Equip On Entities")
@Description({"Whether an entity should equip the item when right clicking on the entity with the item.\nNOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.\n"})
@Example("if {_item} can be equipped on entities:")
@Since({"2.13"})
@RequiredPlugins({"Minecraft 1.21.5+"})
public class CondEquipCompInteract extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         infoBuilder(CondEquipCompInteract.class, PropertyCondition.PropertyType.CAN, "be (equipped|put) on[to] entities", "equippablecomponents")
            .supplier(CondEquipCompInteract::new)
            .build()
      );
   }

   public boolean check(EquippableWrapper wrapper) {
      return wrapper.getComponent().equipOnInteract();
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.CAN;
   }

   @Override
   protected String getPropertyName() {
      return "be equipped onto entities";
   }
}
