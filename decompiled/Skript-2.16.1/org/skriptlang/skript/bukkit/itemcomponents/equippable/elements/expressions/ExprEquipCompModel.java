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

@Name("Equippable Component - Model")
@Description({"The model of the item when equipped.\nThe model key is represented as a namespaced key.\nA namespaced key can be formatted as 'namespace:id' or 'id'. It can only contain one ':' to separate the namespace and the id. Only alphanumeric characters, periods, underscores, and dashes can be used.\nNOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.\n"})
@Example.Examples({@Example("set the equipped model key of {_item} to \"custom_model\""), @Example("set {_component} to the equippable component of {_item}\nset the equipped model id of {_component} to \"custom_model\"\n")})
@RequiredPlugins({"Minecraft 1.21.2+"})
@Since({"2.13"})
public class ExprEquipCompModel extends SimplePropertyExpression<EquippableWrapper, String> implements EquippableExperimentSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprEquipCompModel.class, String.class, "equipped (model|asset) (key|id)", "equippablecomponents", true
               )
               .supplier(ExprEquipCompModel::new))
            .build()
      );
   }

   @Nullable
   public String convert(EquippableWrapper wrapper) {
      Key key = wrapper.getAssetId();
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
      this.getExpr().stream(event).forEach(wrapper -> wrapper.setAssetId(finalKey));
   }

   @Override
   public Class<String> getReturnType() {
      return String.class;
   }

   @Override
   protected String getPropertyName() {
      return "equipped model key";
   }
}
