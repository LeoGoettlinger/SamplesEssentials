package org.skriptlang.skript.bukkit.tags.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.bukkit.tags.TagType;
import org.skriptlang.skript.bukkit.tags.sources.TagOrigin;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Tag")
@Description({"Represents a tag which can be used to classify items, blocks, or entities.", "Tags are composed of a value and an optional namespace: \"minecraft:oak_logs\".", "If you omit the namespace, one will be provided for you, depending on what kind of tag you're using. For example, `tag \"doors\"` will be the tag \"minecraft:doors\", while `paper tag \"doors\"` will be \"paper:doors\".", "`minecraft tag` will search through the vanilla tags, `datapack tag` will search for datapack-provided tags (a namespace is required here!), `paper tag` will search for Paper's custom tags if you are running Paper, and `custom tag` will look in the \"skript\" namespace for custom tags you've registered.", "You can also filter by tag types using \"item\", \"block\", or \"entity\"."})
@Example.Examples({@Example("minecraft tag \"dirt\" # minecraft:dirt"), @Example("paper tag \"doors\" # paper:doors"), @Example("tag \"skript:custom_dirt\" # skript:custom_dirt"), @Example("custom tag \"dirt\" # skript:dirt"), @Example("datapack block tag \"dirt\" # minecraft:dirt"), @Example("datapack tag \"my_pack:custom_dirt\" # my_pack:custom_dirt"), @Example("tag \"minecraft:mineable/pickaxe\" # minecraft:mineable/pickaxe"), @Example("custom item tag \"blood_magic_sk/can_sacrifice_with\" # skript:blood_magic_sk/can_sacrifice_with")})
@Since({"2.10"})
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class ExprTag extends SimpleExpression<Tag> {
   private Expression<String> names;
   TagType<?>[] types;
   private TagOrigin origin;
   private boolean datapackOnly;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(ExprTag.class, Tag.class)
                  .addPattern(TagOrigin.getFullPattern() + " " + TagType.getFullPattern() + " tag %strings%"))
               .supplier(ExprTag::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.names = (Expression<String>)expressions[0];
      this.types = TagType.fromParseMark(parseResult.mark);
      this.origin = TagOrigin.fromParseTags(parseResult.tags);
      this.datapackOnly = this.origin == TagOrigin.BUKKIT && parseResult.hasTag("datapack");
      return true;
   }

   @Nullable
   protected Tag<?>[] get(Event event) {
      List<Tag<?>> tags = new ArrayList<>();

      String[] namespaces = switch (this.origin) {
         case ANY -> new String[]{"minecraft", "paper", "skript"};
         case BUKKIT -> new String[]{"minecraft"};
         case PAPER -> new String[]{"paper"};
         case SKRIPT -> new String[]{"skript"};
      };

      label57:
      for (String name : this.names.getArray(event)) {
         boolean invalidKey = false;

         try {
            if (name.contains(":")) {
               NamespacedKey key = NamespacedKey.fromString(name);
               invalidKey = key == null;
               if (!invalidKey) {
                  tags.add(this.findTag(key));
               }
            } else {
               for (String namespace : namespaces) {
                  Tag<?> tag = this.findTag(new NamespacedKey(namespace, name));
                  if (tag != null) {
                     tags.add(tag);
                     continue label57;
                  }
               }
            }
         } catch (IllegalArgumentException var14) {
            invalidKey = true;
         }

         if (invalidKey) {
            this.error("Invalid tag key: '" + name + "'. Tags may only contain a-z, 0-9, _, ., /, or - characters.");
         }
      }

      return tags.toArray(Tag[]::new);
   }

   @Nullable
   private Tag<?> findTag(NamespacedKey key) {
      for (TagType<?> type : this.types) {
         Tag<?> tag = TagModule.tagRegistry.getTag(this.origin, type, key);
         if (tag != null && (this.origin != TagOrigin.BUKKIT || this.datapackOnly ^ tag.getKey().getNamespace().equals("minecraft"))) {
            return tag;
         }
      }

      return null;
   }

   @Override
   public boolean isSingle() {
      return this.names.isSingle();
   }

   @Override
   public Class<? extends Tag> getReturnType() {
      return Tag.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String registry = this.types.length > 1 ? "" : " " + this.types[0].toString();
      return this.origin.toString(this.datapackOnly) + registry + " tag " + this.names.toString(event, debug);
   }
}
