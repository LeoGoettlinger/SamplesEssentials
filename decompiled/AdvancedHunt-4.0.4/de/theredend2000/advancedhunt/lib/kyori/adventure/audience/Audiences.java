package de.theredend2000.advancedhunt.lib.kyori.adventure.audience;

import de.theredend2000.advancedhunt.lib.kyori.adventure.resource.ResourcePackCallback;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class Audiences {
   static final Collector<? super Audience, ?, ForwardingAudience> COLLECTOR = Collectors.collectingAndThen(
      Collectors.toCollection(ArrayList::new), audiences -> Audience.audience(List.copyOf(audiences))
   );

   private Audiences() {
   }

   public static Consumer<? super Audience> sendingMessage(final ComponentLike message) {
      return audience -> audience.sendMessage(message);
   }

   static ResourcePackCallback unwrapCallback(final Audience forwarding, final Audience dest, final ResourcePackCallback cb) {
      return cb == ResourcePackCallback.noOp() ? cb : (uuid, status, audience) -> cb.packEventReceived(uuid, status, audience == dest ? forwarding : audience);
   }
}
