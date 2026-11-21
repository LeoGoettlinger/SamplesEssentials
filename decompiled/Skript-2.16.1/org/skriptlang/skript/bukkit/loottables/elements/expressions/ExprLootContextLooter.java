package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextCreateEvent;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Looter of Loot Context")
@Description({"Returns the looter of a loot context. Note that setting the looter will read the looter's tool enchantments (e.g. looting) when generating loot."})
@Example.Examples({@Example("set {_killer} to looter of {_context}"), @Example("set {_context} to a loot context at player:\n\tset loot luck value to 10\n\tset looter to player\n\tset looted entity to last spawned pig\n")})
@Since({"2.10"})
public class ExprLootContextLooter extends SimplePropertyExpression<LootContext, Player> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprLootContextLooter.class, Player.class, "(looter|looting player)", "lootcontexts", true)
               .supplier(ExprLootContextLooter::new))
            .build()
      );
   }

   @Nullable
   public Player convert(LootContext context) {
      return context.getKiller() instanceof Player player ? player : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.getParser().isCurrentEvent(LootContextCreateEvent.class)) {
         Skript.error("You cannot set the looting player of an existing loot context.");
         return null;
      } else {
         return switch (mode) {
            case SET, DELETE, RESET -> (Class[])CollectionUtils.array(Player.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof LootContextCreateEvent createEvent) {
         Player player = delta != null ? (Player)delta[0] : null;
         createEvent.getContextWrapper().setKiller(player);
      }
   }

   @Override
   public Class<? extends Player> getReturnType() {
      return Player.class;
   }

   @Override
   protected String getPropertyName() {
      return "looting player";
   }
}
