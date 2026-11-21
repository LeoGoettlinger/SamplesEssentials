package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.expressions;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Allowed Entities")
@Description({"The entities allowed to wear the item.\nNOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.\n"})
@Example.Examples({@Example("set the allowed entities of {_item} to a zombie and a skeleton"), @Example("set {_component} to the equippable component of {_item}\nclear the allowed entities of {_component}\n")})
@RequiredPlugins({"Minecraft 1.21.2+"})
@Since({"2.13"})
public class ExprEquipCompEntities extends PropertyExpression<EquippableWrapper, EntityData> implements EquippableExperimentSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprEquipCompEntities.class, EntityData.class, "allowed entities", "equippablecomponents", true)
               .supplier(ExprEquipCompEntities::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends EquippableWrapper>)exprs[0]);
      return true;
   }

   @Nullable
   protected EntityData[] get(Event event, EquippableWrapper[] source) {
      List<EntityData> types = new ArrayList<>();

      for (EquippableWrapper wrapper : source) {
         Collection<EntityType> allowed = wrapper.getAllowedEntities();
         allowed.forEach(entityType -> types.add(EntityUtils.toSkriptEntityData(entityType)));
      }

      return types.toArray(EntityData[]::new);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE, REMOVE, ADD -> (Class[])CollectionUtils.array(EntityData[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      List<EntityType> converted = new ArrayList<>();
      if (delta != null) {
         for (Object object : delta) {
            converted.add(EntityUtils.toBukkitEntityType((EntityData<?>)object));
         }
      }

      this.getExpr().stream(event).forEach(wrapper -> {
         Collection<EntityType> allowed = wrapper.getAllowedEntities();
         List<EntityType> current = new ArrayList<>(allowed);
         switch (mode) {
            case SET:
               current.clear();
               current.addAll(converted);
               break;
            case DELETE:
               current.clear();
               break;
            case REMOVE:
               current.removeAll(converted);
               break;
            case ADD:
               current.addAll(converted);
         }

         wrapper.editBuilder(builder -> builder.allowedEntities(EquippableWrapper.convertAllowedEntities(current)));
      });
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<EntityData> getReturnType() {
      return EntityData.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the allowed entities of " + this.getExpr().toString(event, debug);
   }
}
