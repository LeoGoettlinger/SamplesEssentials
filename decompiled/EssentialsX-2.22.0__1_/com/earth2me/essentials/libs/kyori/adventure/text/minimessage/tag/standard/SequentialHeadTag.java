package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.standard;

import com.earth2me.essentials.libs.kyori.adventure.key.Key;
import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import com.earth2me.essentials.libs.kyori.adventure.text.ObjectComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.Context;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.ParsingException;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.Tag;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import com.earth2me.essentials.libs.kyori.adventure.text.object.ObjectContents;
import com.earth2me.essentials.libs.kyori.adventure.text.object.PlayerHeadObjectContents;
import com.earth2me.essentials.libs.kyori.adventure.util.TriState;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

final class SequentialHeadTag {
   private static final String HEAD = "head";
   private static final Pattern UUIDv4_PATTERN = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ABCD][0-9a-f]{3}-[0-9a-f]{12}", 2);
   static final TagResolver RESOLVER = SerializableResolver.claimingComponent("head", SequentialHeadTag::create, SequentialHeadTag::claimComponent);

   private SequentialHeadTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      if (!args.hasNext()) {
         return Tag.selfClosingInserting((Component)Component.object(ObjectContents.playerHead().build()));
      } else {
         Tag.Argument rawArgument = args.pop();
         String argument = rawArgument.value();
         TriState outerLayer;
         if (!args.hasNext()) {
            outerLayer = argumentToTriState(rawArgument);
            if (outerLayer != TriState.NOT_SET) {
               return Tag.selfClosingInserting((Component)Component.object(ObjectContents.playerHead().hat(outerLayer.toBooleanOrElse(true)).build()));
            }
         } else {
            outerLayer = argumentToTriState(args.pop());
         }

         if (args.hasNext()) {
            throw ctx.newException("Too many arguments present", args);
         } else if (UUIDv4_PATTERN.matcher(argument).matches()) {
            return Tag.selfClosingInserting(
               (Component)Component.object(ObjectContents.playerHead().id(UUID.fromString(argument)).hat(outerLayer.toBooleanOrElse(true)).build())
            );
         } else {
            return argument.contains("/") && Key.parseable(argument)
               ? Tag.selfClosingInserting(
                  (Component)Component.object(ObjectContents.playerHead().texture(Key.key(argument)).hat(outerLayer.toBooleanOrElse(true)).build())
               )
               : Tag.selfClosingInserting((Component)Component.object(ObjectContents.playerHead().name(argument).hat(outerLayer.toBooleanOrElse(true)).build()));
         }
      }
   }

   @Nullable
   static Emitable claimComponent(final Component input) {
      if (!(input instanceof ObjectComponent)) {
         return null;
      } else {
         ObjectContents contents = ((ObjectComponent)input).contents();
         if (!(contents instanceof PlayerHeadObjectContents)) {
            return null;
         } else {
            PlayerHeadObjectContents playerHead = (PlayerHeadObjectContents)contents;
            SequentialHeadTag.PresentType present = null;
            if (playerHead.name() != null) {
               present = SequentialHeadTag.PresentType.NAME;
            }

            if (playerHead.id() != null) {
               if (present != null) {
                  return null;
               }

               present = SequentialHeadTag.PresentType.ID;
            }

            if (playerHead.texture() != null) {
               if (present != null) {
                  return null;
               }

               present = SequentialHeadTag.PresentType.TEXTURE;
            }

            if (present == null) {
               return emit -> {
                  emit.tag("head");
                  if (!playerHead.hat()) {
                     emit.argument(Boolean.toString(playerHead.hat()));
                  }
               };
            } else {
               SequentialHeadTag.PresentType finalPresent = present;
               return emit -> {
                  emit.tag("head");
                  String value = finalPresent.map(playerHead);
                  emit.argument(value);
                  if (!playerHead.hat()) {
                     emit.argument(Boolean.toString(playerHead.hat()));
                  }
               };
            }
         }
      }
   }

   private static TriState argumentToTriState(final Tag.Argument argument) {
      if (argument.isTrue()) {
         return TriState.TRUE;
      } else {
         return argument.isFalse() ? TriState.FALSE : TriState.NOT_SET;
      }
   }

   private static enum PresentType {
      NAME(PlayerHeadObjectContents::name),
      ID(obj -> Objects.requireNonNull(obj.id()).toString()),
      TEXTURE(obj -> Objects.requireNonNull(obj.texture()).asMinimalString());

      private final Function<PlayerHeadObjectContents, String> mappingFunction;

      private PresentType(final Function<PlayerHeadObjectContents, String> mappingFunction) {
         this.mappingFunction = mappingFunction;
      }

      public String map(final PlayerHeadObjectContents obj) {
         return this.mappingFunction.apply(obj);
      }
   }
}
