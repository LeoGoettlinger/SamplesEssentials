package com.github.retrooper.packetevents.protocol.dialog.body;

import com.github.retrooper.packetevents.protocol.util.NbtMapDecoder;
import com.github.retrooper.packetevents.protocol.util.NbtMapEncoder;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DialogBodyTypes {
   private static final VersionedRegistry<DialogBodyType<?>> REGISTRY = new VersionedRegistry<>("dialog_body_type");
   public static final DialogBodyType<ItemDialogBody> ITEM = define("item", ItemDialogBody::decode, ItemDialogBody::encode);
   public static final DialogBodyType<PlainMessageDialogBody> PLAIN_MESSAGE = define(
      "plain_message", PlainMessageDialogBody::decode, PlainMessageDialogBody::encode
   );

   private DialogBodyTypes() {
   }

   @Internal
   public static <T extends DialogBody> DialogBodyType<T> define(String name, NbtMapDecoder<T> decoder, NbtMapEncoder<T> encoder) {
      return REGISTRY.define(name, data -> new StaticDialogBodyType<>(data, decoder, encoder));
   }

   public static VersionedRegistry<DialogBodyType<?>> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
