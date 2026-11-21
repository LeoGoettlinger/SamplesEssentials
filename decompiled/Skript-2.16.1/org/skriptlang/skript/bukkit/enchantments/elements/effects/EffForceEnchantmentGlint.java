package org.skriptlang.skript.bukkit.enchantments.elements.effects;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Force Enchantment Glint")
@Description({"Forces the items to glint or not, or removes its existing enchantment glint enforcement."})
@Example.Examples({@Example("force {_items::*} to glint"), @Example("force the player's tool to stop glinting")})
@Since({"2.10"})
public class EffForceEnchantmentGlint extends Effect {
   private Expression<ItemType> itemTypes;
   private int pattern;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffForceEnchantmentGlint.class)
            .addPatterns(
               "(force|make) %itemtypes% [to] [start] glint[ing]",
               "(force|make) %itemtypes% [to] (not|stop) glint[ing]",
               "(clear|delete|reset) [the] enchantment glint override of %itemtypes%",
               "(clear|delete|reset) %itemtypes%'[s] enchantment glint override"
            )
            .supplier(EffForceEnchantmentGlint::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.itemTypes = (Expression<ItemType>)expressions[0];
      this.pattern = matchedPattern;
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (ItemType itemType : this.itemTypes.getArray(event)) {
         ItemMeta meta = itemType.getItemMeta();

         Boolean glint = switch (this.pattern) {
            case 0 -> true;
            case 1 -> false;
            default -> null;
         };
         meta.setEnchantmentGlintOverride(glint);
         itemType.setItemMeta(meta);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.pattern > 1
         ? "clear the enchantment glint override of " + this.itemTypes.toString(event, debug)
         : "force " + this.itemTypes.toString(event, debug) + " to " + (this.pattern == 0 ? "start" : "stop") + " glinting";
   }
}
