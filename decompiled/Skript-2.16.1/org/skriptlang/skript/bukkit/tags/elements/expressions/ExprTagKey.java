package org.skriptlang.skript.bukkit.tags.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Tag Namespaced Key")
@Description({"The namespaced key of a minecraft tag. This takes the form of \"namespace:key\", e.g. \"minecraft:dirt\"."})
@Example.Examples({@Example("broadcast namespaced keys of the tags of player's tool"), @Example("if the key of {_my-tag} is \"minecraft:stone\":\n\treturn true\n")})
@Since({"2.10"})
@Keywords({"minecraft tag", "type", "key", "namespace"})
public class ExprTagKey extends SimplePropertyExpression<Tag<?>, String> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprTagKey.class, String.class, "[namespace[d]] key[s]", "minecrafttags", false)
               .supplier(ExprTagKey::new))
            .build()
      );
   }

   @Nullable
   public String convert(@NotNull Tag<?> from) {
      return from.getKey().toString();
   }

   @Override
   protected String getPropertyName() {
      return "namespaced key";
   }

   @Override
   public Class<String> getReturnType() {
      return String.class;
   }
}
