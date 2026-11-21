package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.expressions;

import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Equip Sound")
@Description({"The sound to be played when the item is equipped.\nNOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.\n"})
@Example.Examples({@Example("set the equip sound of {_item} to \"entity.experience_orb.pickup\""), @Example("set {_component} to the equippable component of {_item}\nset the equip sound of {_component} to \"block.note_block.pling\"\n")})
@RequiredPlugins({"Minecraft 1.21.2+"})
@Since({"2.13"})
public class ExprEquipCompEquipSound extends SimplePropertyExpression<EquippableWrapper, String> implements EquippableExperimentSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprEquipCompEquipSound.class, String.class, "equip sound", "equippablecomponents", true)
               .supplier(ExprEquipCompEquipSound::new))
            .build()
      );
   }

   @Nullable
   public String convert(EquippableWrapper wrapper) {
      return wrapper.getComponent().equipSound().toString();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.DELETE ? null : CollectionUtils.array(String.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Sound enumSound = null;
      if (delta != null) {
         String soundString = (String)delta[0];
         enumSound = SoundUtils.getSound(soundString);
         if (enumSound == null) {
            this.error("Could not find a sound with the id '" + soundString + "'.");
            return;
         }
      }

      Key key;
      if (enumSound != null) {
         key = Registry.SOUNDS.getKey(enumSound);
      } else {
         key = null;
      }

      this.getExpr().stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.equipSound(key)));
   }

   @Override
   public Class<String> getReturnType() {
      return String.class;
   }

   @Override
   protected String getPropertyName() {
      return "equip sound";
   }
}
