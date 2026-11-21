package org.skriptlang.skript.bukkit.tags.elements.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.Tag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Tagged")
@Description({"Checks whether an item, block, entity, or entitydata is tagged with the given tag."})
@Example.Examples({@Example("if player's tool is tagged with minecraft tag \"enchantable/sharp_weapon\":\n\tenchant player's tool with sharpness 1\n"), @Example("if all logs are tagged with tag \"minecraft:logs\"")})
@Since({"2.10"})
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class CondIsTagged extends Condition {
   private Expression<Tag<Keyed>> tags;
   private Expression<?> elements;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         PropertyCondition.infoBuilder(
               CondIsTagged.class, PropertyCondition.PropertyType.BE, "tagged (as|with) %minecrafttags%", "itemtypes/entities/entitydatas"
            )
            .supplier(CondIsTagged::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.elements = expressions[0];
      this.tags = (Expression<Tag<Keyed>>)expressions[1];
      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public boolean check(Event event) {
      Tag<Keyed>[] tags = this.tags.getAll(event);
      if (tags.length == 0) {
         return this.isNegated();
      } else {
         boolean and = this.tags.getAnd();
         return this.elements.check(event, element -> {
            boolean isAny = element instanceof ItemType itemType && !itemType.isAll();
            Keyed[] values = TagModule.getKeyed(element);
            if (values != null && values.length != 0) {
               Class<? extends Keyed> valueClass = values[0].getClass();

               for (Tag<Keyed> tag : tags) {
                  if (!((Keyed)tag.getValues().iterator().next()).getClass().isAssignableFrom(valueClass)) {
                     return false;
                  }

                  if (this.isTagged(tag, values, !isAny)) {
                     if (!and) {
                        return true;
                     }
                  } else if (and) {
                     return false;
                  }
               }

               return and;
            } else {
               return false;
            }
         }, this.isNegated());
      }
   }

   private boolean isTagged(Tag<Keyed> tag, @NotNull Keyed[] values, boolean allTagged) {
      for (Keyed value : values) {
         if (tag.isTagged(value)) {
            if (!allTagged) {
               return true;
            }
         } else if (allTagged) {
            return false;
         }
      }

      return allTagged;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, debug, this.elements, " tagged as " + this.tags.toString(event, debug));
   }
}
