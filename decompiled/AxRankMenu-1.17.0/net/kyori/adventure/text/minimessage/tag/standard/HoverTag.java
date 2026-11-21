package net.kyori.adventure.text.minimessage.tag.standard;

import java.util.UUID;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class HoverTag {
   private static final String HOVER = "hover";
   static final TagResolver RESOLVER = SerializableResolver.claimingStyle(
      "hover", HoverTag::create, StyleClaim.claim("hover", Style::hoverEvent, HoverTag::emit)
   );

   private HoverTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      String actionName = args.popOr("Hover event requires an action as its first argument").value();
      Action<Object> action = (Action<Object>)Action.NAMES.value(actionName);
      HoverTag.ActionHandler<Object> value = actionHandler(action);
      if (value == null) {
         throw ctx.newException("Don't know how to turn '" + args + "' into a hover event", args);
      } else {
         return Tag.styling(HoverEvent.hoverEvent(action, value.parse(args, ctx)));
      }
   }

   static void emit(final HoverEvent<?> event, final TokenEmitter emitter) {
      HoverTag.ActionHandler<Object> handler = actionHandler(event.action());
      emitter.tag("hover").argument((String)Action.NAMES.key(event.action()));
      handler.emit(event.value(), emitter);
   }

   @Nullable
   static <V> HoverTag.ActionHandler<V> actionHandler(final Action<V> action) {
      HoverTag.ActionHandler<?> ret = null;
      if (action == Action.SHOW_TEXT) {
         ret = HoverTag.ShowText.INSTANCE;
      } else if (action == Action.SHOW_ITEM) {
         ret = HoverTag.ShowItem.INSTANCE;
      } else if (action == Action.SHOW_ENTITY) {
         ret = HoverTag.ShowEntity.INSTANCE;
      }

      return (HoverTag.ActionHandler<V>)ret;
   }

   @NotNull
   static String compactAsString(@NotNull final Key key) {
      return key.namespace().equals("minecraft") ? key.value() : key.asString();
   }

   interface ActionHandler<V> {
      @NotNull
      V parse(@NotNull final ArgumentQueue args, @NotNull final Context ctx) throws ParsingException;

      void emit(final V event, final TokenEmitter emit);
   }

   static final class ShowEntity implements HoverTag.ActionHandler<net.kyori.adventure.text.event.HoverEvent.ShowEntity> {
      static final HoverTag.ShowEntity INSTANCE = new HoverTag.ShowEntity();

      private ShowEntity() {
      }

      @NotNull
      public net.kyori.adventure.text.event.HoverEvent.ShowEntity parse(@NotNull final ArgumentQueue args, @NotNull final Context ctx) throws ParsingException {
         try {
            Key key = Key.key(args.popOr("Show entity needs a type argument").value());
            UUID id = UUID.fromString(args.popOr("Show entity needs an entity UUID").value());
            if (args.hasNext()) {
               Component name = ctx.deserialize(args.pop().value());
               return net.kyori.adventure.text.event.HoverEvent.ShowEntity.showEntity(key, id, name);
            } else {
               return net.kyori.adventure.text.event.HoverEvent.ShowEntity.showEntity(key, id);
            }
         } catch (InvalidKeyException | IllegalArgumentException var6) {
            throw ctx.newException("Exception parsing show_entity hover", var6, args);
         }
      }

      public void emit(final net.kyori.adventure.text.event.HoverEvent.ShowEntity event, final TokenEmitter emit) {
         emit.argument(HoverTag.compactAsString(event.type())).argument(event.id().toString());
         if (event.name() != null) {
            emit.argument(event.name());
         }
      }
   }

   static final class ShowItem implements HoverTag.ActionHandler<net.kyori.adventure.text.event.HoverEvent.ShowItem> {
      private static final HoverTag.ShowItem INSTANCE = new HoverTag.ShowItem();

      private ShowItem() {
      }

      @NotNull
      public net.kyori.adventure.text.event.HoverEvent.ShowItem parse(@NotNull final ArgumentQueue args, @NotNull final Context ctx) throws ParsingException {
         try {
            Key key = Key.key(args.popOr("Show item hover needs at least an item ID").value());
            int count = args.hasNext() ? args.pop().asInt().orElseThrow(() -> ctx.newException("The count argument was not a valid integer")) : 1;
            return args.hasNext()
               ? net.kyori.adventure.text.event.HoverEvent.ShowItem.showItem(key, count, BinaryTagHolder.binaryTagHolder(args.pop().value()))
               : net.kyori.adventure.text.event.HoverEvent.ShowItem.showItem(key, count);
         } catch (NumberFormatException | InvalidKeyException var5) {
            throw ctx.newException("Exception parsing show_item hover", var5, args);
         }
      }

      public void emit(final net.kyori.adventure.text.event.HoverEvent.ShowItem event, final TokenEmitter emit) {
         emit.argument(HoverTag.compactAsString(event.item()));
         if (event.count() != 1 || event.nbt() != null) {
            emit.argument(Integer.toString(event.count()));
            if (event.nbt() != null) {
               emit.argument(event.nbt().string());
            }
         }
      }
   }

   static final class ShowText implements HoverTag.ActionHandler<Component> {
      private static final HoverTag.ShowText INSTANCE = new HoverTag.ShowText();

      private ShowText() {
      }

      @NotNull
      public Component parse(@NotNull final ArgumentQueue args, @NotNull final Context ctx) throws ParsingException {
         return ctx.deserialize(args.popOr("show_text action requires a message").value());
      }

      public void emit(final Component event, final TokenEmitter emit) {
         emit.argument(event);
      }
   }
}
