package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.standard;

import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import com.earth2me.essentials.libs.kyori.adventure.text.KeybindComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.Context;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.ParsingException;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.Tag;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;

final class KeybindTag {
   public static final String KEYBIND = "key";
   static final TagResolver RESOLVER = SerializableResolver.claimingComponent("key", KeybindTag::create, KeybindTag::emit);

   private KeybindTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      return Tag.inserting((Component)Component.keybind(args.popOr("A keybind id is required").value()));
   }

   @Nullable
   static Emitable emit(final Component component) {
      if (!(component instanceof KeybindComponent)) {
         return null;
      } else {
         String key = ((KeybindComponent)component).keybind();
         return emit -> emit.tag("key").argument(key);
      }
   }
}
