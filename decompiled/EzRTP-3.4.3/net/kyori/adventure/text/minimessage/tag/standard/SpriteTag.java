package net.kyori.adventure.text.minimessage.tag.standard;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import org.jetbrains.annotations.Nullable;

final class SpriteTag {
   private static final String SPRITE = "sprite";
   static final TagResolver RESOLVER = SerializableResolver.claimingComponent("sprite", SpriteTag::create, SpriteTag::claimComponent);

   private SpriteTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      String firstArg = args.popOr("An atlas id and or a sprite id is required to produce a sprite object component").value();
      String secondArg = args.hasNext() ? args.pop().value() : null;
      return secondArg == null
         ? Tag.selfClosingInserting((Component)Component.object((ObjectContents)ObjectContents.sprite(Key.key(firstArg))))
         : Tag.selfClosingInserting((Component)Component.object((ObjectContents)ObjectContents.sprite(Key.key(firstArg), Key.key(secondArg))));
   }

   @Nullable
   static Emitable claimComponent(final Component input) {
      if (!(input instanceof ObjectComponent)) {
         return null;
      } else {
         ObjectContents contents = ((ObjectComponent)input).contents();
         if (!(contents instanceof SpriteObjectContents)) {
            return null;
         } else {
            SpriteObjectContents sprite = (SpriteObjectContents)contents;
            Key atlas = sprite.atlas();
            Key key = sprite.sprite();
            return atlas == SpriteObjectContents.DEFAULT_ATLAS
               ? emit -> emit.tag("sprite").argument(key.asString())
               : emit -> emit.tag("sprite").argument(atlas.asString()).argument(key.asString());
         }
      }
   }
}
