package org.skriptlang.skript.bukkit.tags.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Keyed;
import org.bukkit.Tag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.bukkit.tags.TagType;
import org.skriptlang.skript.bukkit.tags.sources.TagOrigin;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Tags of X")
@Description({"Returns all the tags of an item, block, or entity.", "`minecraft tag` will return only the vanilla tags, `datapack tag` will return only datapack-provided tags, `paper tag` will return only Paper's custom tags (if you are running Paper), and `custom tag` will look in the \"skript\" namespace for custom tags you've registered.", "You can also filter by tag types using \"item\", \"block\", or \"entity\"."})
@Example.Examples({@Example("broadcast minecraft tags of dirt"), @Example("send true if paper item tags of target block contains paper tag \"doors\""), @Example("broadcast the block tags of player's tool")})
@Since({"2.10"})
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class ExprTagsOf extends PropertyExpression<Object, Tag> {
   TagType<?>[] types;
   TagOrigin origin;
   boolean datapackOnly;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(ExprTagsOf.class, Tag.class)
                  .addPatterns(
                     new String[]{
                        "[all [[of] the]|the] " + TagOrigin.getFullPattern() + " " + TagType.getFullPattern() + " tags of %itemtype/entity/entitydata%",
                        "%itemtype/entity/entitydata%'[s] " + TagOrigin.getFullPattern() + " " + TagType.getFullPattern() + " tags"
                     }
                  ))
               .supplier(ExprTagsOf::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Object>)expressions[0]);
      this.types = TagType.fromParseMark(parseResult.mark);
      this.origin = TagOrigin.fromParseTags(parseResult.tags);
      this.datapackOnly = this.origin == TagOrigin.BUKKIT && parseResult.hasTag("datapack");
      return true;
   }

   @Nullable
   protected Tag<?>[] get(Event event, @NotNull Object[] source) {
      if (source.length == 0) {
         return new Tag[0];
      } else {
         boolean isAny = source[0] instanceof ItemType itemType && !itemType.isAll();
         Keyed[] values = TagModule.getKeyed(source[0]);
         if (values == null) {
            return new Tag[0];
         } else {
            if (isAny) {
               ThreadLocalRandom random = ThreadLocalRandom.current();
               values = new Keyed[]{values[random.nextInt(0, values.length)]};
            }

            Set<Tag<?>> tags = new TreeSet<>(Comparator.comparing(Keyed::key));

            for (Keyed value : values) {
               tags.addAll(this.getTags(value));
            }

            return tags.stream()
               .filter(tag -> this.origin != TagOrigin.BUKKIT || this.datapackOnly ^ tag.getKey().getNamespace().equals("minecraft"))
               .toArray(Tag[]::new);
         }
      }
   }

   public <T extends Keyed> Collection<Tag<T>> getTags(@NotNull T value) {
      List<Tag<T>> tags = new ArrayList<>();
      Class<T> clazz = value.getClass();

      for (Tag<T> tag : TagModule.tagRegistry.getTags(this.origin, clazz, this.types)) {
         if (tag.isTagged(value)) {
            tags.add(tag);
         }
      }

      return tags;
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<Tag> getReturnType() {
      return Tag.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String registry = this.types.length > 1 ? "" : " " + this.types[0].toString();
      return this.origin.toString(this.datapackOnly) + registry + " tags of " + this.getExpr().toString(event, debug);
   }
}
