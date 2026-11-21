package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.expressions;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Camera Overlay")
@Description({"The camera overlay for the player when the item is equipped.\nExample: The jack-o'-lantern view when having a jack-o'-lantern equipped as a helmet.\nThe camera overlay is represented as a namespaced key.\nA namespaced key can be formatted as 'namespace:id' or 'id'. It can only contain one ':' to separate the namespace and the id. Only alphanumeric characters, periods, underscores, and dashes can be used.\nNOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.\n"})
@Example.Examples({@Example("set the camera overlay of {_item} to \"custom_overlay\""), @Example("set {_component} to the equippable component of {_item}\nset the camera overlay of {_component} to \"custom_overlay\"\n")})
@RequiredPlugins({"Minecraft 1.21.2+"})
@Since({"2.13"})
public class ExprEquipCompCameraOverlay extends SimplePropertyExpression<EquippableWrapper, String> implements EquippableExperimentSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprEquipCompCameraOverlay.class, String.class, "camera overlay", "equippablecomponents", true)
               .supplier(ExprEquipCompCameraOverlay::new))
            .build()
      );
   }

   @Nullable
   public String convert(EquippableWrapper wrapper) {
      Key key = wrapper.getComponent().cameraOverlay();
      return key == null ? null : key.toString();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.DELETE ? null : CollectionUtils.array(String.class);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      NamespacedKey key = null;
      if (delta != null && delta[0] instanceof String string) {
         key = NamespacedUtils.checkValidationAndSend(string, this);
         if (key == null) {
            return;
         }
      }

      NamespacedKey finalKey = key;
      this.getExpr().stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.cameraOverlay(finalKey)));
   }

   @Override
   public Class<String> getReturnType() {
      return String.class;
   }

   @Override
   protected String getPropertyName() {
      return "camera overlay";
   }
}
