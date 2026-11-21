package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Seed of Loot Table")
@Description({"Returns the seed of a loot table. Setting the seed of a block or entity that does not have a loot table will not do anything."})
@Example.Examples({@Example("set {_seed} loot table seed of block"), @Example("set loot table seed of entity to 123456789")})
@Since({"2.10"})
public class ExprLootTableSeed extends SimplePropertyExpression<Object, Long> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprLootTableSeed.class, Long.class, "loot[[ ]table] seed[s]", "entities/blocks", false)
               .supplier(ExprLootTableSeed::new))
            .build()
      );
   }

   @Nullable
   public Long convert(Object object) {
      Lootable lootable = LootTableUtils.getAsLootable(object);
      return lootable != null ? lootable.getSeed() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Number.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert delta != null;

      long seedValue = ((Number)delta[0]).longValue();

      for (Object object : this.getExpr().getArray(event)) {
         if (LootTableUtils.isLootable(object)) {
            Lootable lootable = LootTableUtils.getAsLootable(object);
            lootable.setSeed(seedValue);
            LootTableUtils.updateState(lootable);
         }
      }
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   protected String getPropertyName() {
      return "loot table seed";
   }
}
