package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item with Enchantment Glint")
@Description({"Get an item with or without enchantment glint."})
@Example.Examples({@Example("set {_item with glint} to diamond with enchantment glint"), @Example("set {_item without glint} to diamond without enchantment glint")})
@Since({"2.10"})
public class ExprItemWithEnchantmentGlint extends PropertyExpression<ItemType, ItemType> {
   private boolean glint;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprItemWithEnchantmentGlint.class, ItemType.class
                     )
                     .addPattern("%itemtypes% with[:out] [enchant[ment]] glint"))
                  .supplier(ExprItemWithEnchantmentGlint::new))
               .priority(PropertyExpression.DEFAULT_PRIORITY))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends ItemType>)expressions[0]);
      this.glint = !parseResult.hasTag("out");
      return true;
   }

   protected ItemType[] get(Event event, ItemType[] source) {
      return this.get(source, itemType -> {
         itemType = itemType.clone();
         ItemMeta meta = itemType.getItemMeta();
         meta.setEnchantmentGlintOverride(this.glint);
         itemType.setItemMeta(meta);
         return itemType;
      });
   }

   @Override
   public Class<? extends ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.getExpr().toString(event, debug) + (this.glint ? " with" : " without") + " enchantment glint";
   }
}
