package org.skriptlang.skript.bukkit.bossbar.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Key Of Boss Bar")
@Description({"Returns the key of a keyed boss bar.\nDoes not return anything for normal boss bars.\n"})
@Example("broadcast the boss bar key of {_mybar}\n")
@Since({"2.16"})
public class ExprKeyOfBossBar extends SimplePropertyExpression<BossBar, String> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprKeyOfBossBar.class, String.class, "boss[ ]bar (key|id)", "bossbars", false)
               .supplier(ExprKeyOfBossBar::new))
            .build()
      );
   }

   @Nullable
   public String convert(BossBar bar) {
      return bar instanceof KeyedBossBar keyed ? keyed.getKey().toString() : null;
   }

   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @Override
   protected String getPropertyName() {
      return "boss bar key";
   }
}
