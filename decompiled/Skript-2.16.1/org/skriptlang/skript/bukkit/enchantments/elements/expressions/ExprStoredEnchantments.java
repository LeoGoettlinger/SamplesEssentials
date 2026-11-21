package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Stored Enchantments")
@Description({"The enchantments stored inside an enchanted book. This is different from enchanting the book, as for example a book\nof sharpness does not deal more damage when used as a weapon. Corresponds to the <code>minecraft:stored_enchantments</code> data component.\nNote that for example resetting the stored enchantments of an enchanted book of Sharpness III removes the sharpness enchantment, since\nit is impossible to know what the original enchantments of the item were. Note also that only one entry can exist per enchantment type;\nadding Sharpness III to a book with Sharpness V will convert it to a book of Sharpness III.\n"})
@Example("command /godbook:\n\ttrigger:\n\t\tset {_item} to minecraft:enchanted_book\n\t\tadd mending to stored enchants of {_item} # adds mending 1\n\t\tadd knockback 12 to stored enchants of {_item}\n\t\tadd fire aspect 3 to stored enchants of {_item}\n\t\tgive {_item} to player\n")
@Since({"2.16"})
public class ExprStoredEnchantments extends PropertyExpression<ItemType, EnchantmentType> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprStoredEnchantments.class, EnchantmentType.class, "stored enchant[ment]s", "itemtypes", false)
               .supplier(ExprStoredEnchantments::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends ItemType>)expressions[0]);
      return true;
   }

   protected EnchantmentType[] get(Event event, ItemType[] source) {
      List<EnchantmentType> types = new ArrayList<>();

      for (ItemType type : source) {
         if (type.getItemMeta() instanceof EnchantmentStorageMeta storageMeta) {
            for (Entry<Enchantment, Integer> entry : storageMeta.getStoredEnchants().entrySet()) {
               types.add(new EnchantmentType(entry.getKey(), entry.getValue()));
            }
         }
      }

      return types.toArray(EnchantmentType[]::new);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, SET, DELETE, REMOVE, REMOVE_ALL -> (Class[])CollectionUtils.array(EnchantmentType[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ItemType[] targets = this.getExpr().getArray(event);

      for (ItemType target : targets) {
         if (target.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            Map<Enchantment, Integer> adjusted = new HashMap<>(meta.getStoredEnchants());
            if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE) {
               adjusted.clear();
            }

            switch (mode) {
               case ADD:
               case SET:
                  assert delta != null;

                  for (Object changex : delta) {
                     if (changex instanceof EnchantmentType typex) {
                        adjusted.put(typex.getType(), typex.getLevel());
                     }
                  }
               case DELETE:
                  break;
               case REMOVE:
               case REMOVE_ALL:
                  assert delta != null;

                  for (Object change : delta) {
                     if (change instanceof EnchantmentType type) {
                        if (type.getInternalLevel() == -1) {
                           adjusted.remove(type.getType());
                        } else {
                           adjusted.remove(type.getType(), type.getLevel());
                        }
                     }
                  }
                  break;
               default:
                  throw new IllegalArgumentException("Invalid change mode " + mode);
            }

            for (Enchantment existing : meta.getStoredEnchants().keySet()) {
               meta.removeStoredEnchant(existing);
            }

            for (Entry<Enchantment, Integer> adjustedEntry : adjusted.entrySet()) {
               meta.addStoredEnchant(adjustedEntry.getKey(), adjustedEntry.getValue(), true);
            }

            target.setItemMeta(meta);
         }
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends EnchantmentType> getReturnType() {
      return EnchantmentType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "stored enchantments of " + this.getExpr().toString(event, debug);
   }
}
