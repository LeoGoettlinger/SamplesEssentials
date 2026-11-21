package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Looted Entity of Loot Context")
@Description({"Returns the looted entity of a loot context."})
@Example.Examples({@Example("set {_entity} to looted entity of {_context}"), @Example("set {_context} to a loot context at player:\n\tset loot luck value to 10\n\tset looter to player\n\tset looted entity to last spawned pig\n")})
@Since({"2.10"})
public class ExprLootContextEntity extends SimplePropertyExpression<LootContext, Entity> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprLootContextEntity.class, Entity.class, "looted entity", "lootcontexts", true)
               .supplier(ExprLootContextEntity::new))
            .build()
      );
   }

   @Nullable
   public Entity convert(LootContext context) {
      return context.getLootedEntity();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.getParser().isCurrentEvent(LootContextCreateEvent.class)) {
         Skript.error("You cannot set the looted entity of an existing loot context.");
         return null;
      } else {
         return switch (mode) {
            case SET, DELETE, RESET -> (Class[])CollectionUtils.array(Entity.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof LootContextCreateEvent createEvent) {
         Entity entity = delta != null ? (Entity)delta[0] : null;
         createEvent.getContextWrapper().setEntity(entity);
      }
   }

   @Override
   public Class<? extends Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   protected String getPropertyName() {
      return "looted entity";
   }
}
