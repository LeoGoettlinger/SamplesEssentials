package org.skriptlang.skript.bukkit.enchantments.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Enchant/Disenchant")
@Description({"Enchant or disenchant an existing item. Enchanting at a specific level will act as if an enchanting table was used, and will apply the enchantments randomly chosen at that level. Treasure enchantments, like mending, can optionally be allowed. Note that enchanting a book at a specific level will turn it into an enchanted book, rather than a book with enchantments."})
@Example.Examples({@Example("enchant the player's tool with sharpness 5"), @Example("enchant the player's tool at level 30"), @Example("disenchant the player's tool")})
@Since({"2.0, 2.13 (at level)"})
public class EffEnchant extends Effect {
   private static final Patterns<EffEnchant.Operation> PATTERNS = new Patterns<>(
      new Object[][]{
         {"enchant %~itemtypes% with %enchantmenttypes%", EffEnchant.Operation.ENCHANT},
         {"[naturally|randomly] enchant %~itemtypes% at level %number%[treasure:[,] allowing treasure enchant[ment]s]", EffEnchant.Operation.ENCHANT_AT_LEVEL},
         {"disenchant %~itemtypes%", EffEnchant.Operation.DISENCHANT}
      }
   );
   private Expression<ItemType> items;
   private Expression<EnchantmentType> enchantments;
   private Expression<Number> level;
   private boolean treasure;
   private EffEnchant.Operation operation;

   public static void register(SyntaxRegistry registry) {
      registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEnchant.class).addPatterns(PATTERNS.getPatterns()).supplier(EffEnchant::new).build());
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.items = (Expression<ItemType>)exprs[0];
      if (!Changer.ChangerUtils.acceptsChange(this.items, Changer.ChangeMode.SET, ItemStack.class)) {
         Skript.error(this.items + " cannot be changed, thus it cannot be (dis)enchanted");
         return false;
      } else {
         if (matchedPattern == 0) {
            this.enchantments = (Expression<EnchantmentType>)exprs[1];
         } else if (matchedPattern == 1) {
            this.level = (Expression<Number>)exprs[1];
            this.treasure = parseResult.hasTag("treasure");
         }

         this.operation = PATTERNS.getInfo(matchedPattern);
         return true;
      }
   }

   @Override
   protected void execute(Event event) {
      Function<ItemType, ItemType> changeFunction;
      switch (this.operation) {
         case ENCHANT:
            EnchantmentType[] types = this.enchantments.getArray(event);
            if (types.length == 0) {
               return;
            }

            changeFunction = item -> {
               item.addEnchantments(types);
               return item;
            };
            break;
         case ENCHANT_AT_LEVEL:
            Number levelValue = this.level.getSingle(event);
            if (levelValue == null || levelValue.intValue() < 0) {
               return;
            }

            ItemFactory factory = Bukkit.getItemFactory();
            changeFunction = item -> {
               ItemStack itemstack = item.getRandom();
               if (itemstack == null) {
                  return item;
               } else {
                  itemstack = factory.enchantWithLevels(itemstack, levelValue.intValue(), this.treasure, ThreadLocalRandom.current());
                  return new ItemType(itemstack);
               }
            };
            break;
         case DISENCHANT:
            changeFunction = item -> {
               item.clearEnchantments();
               return item;
            };
            break;
         default:
            throw new IllegalStateException("Unexpected operation: " + this.operation);
      }

      this.items.changeInPlace(event, changeFunction);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return switch (this.operation) {
         case ENCHANT -> "enchant " + this.items.toString(event, debug) + " with " + this.enchantments.toString(event, debug);
         case ENCHANT_AT_LEVEL -> "enchant "
            + this.items.toString(event, debug)
            + " at level "
            + this.level.toString(event, debug)
            + (this.treasure ? " allowing treasure enchantments" : "");
         case DISENCHANT -> "disenchant " + this.items.toString(event, debug);
      };
   }

   private static enum Operation {
      ENCHANT,
      ENCHANT_AT_LEVEL,
      DISENCHANT;
   }
}
