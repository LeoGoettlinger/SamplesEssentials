package de.theredend2000.advancedhunt.lib.kyori.adventure.identity;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.pointer.Pointer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import java.util.Locale;
import java.util.UUID;

public sealed interface Identity extends Identified permits IdentityImpl, NilIdentity {
   Pointer<String> NAME = Pointer.pointer(String.class, Key.key("adventure", "name"));
   Pointer<UUID> UUID = Pointer.pointer(UUID.class, Key.key("adventure", "uuid"));
   Pointer<Component> DISPLAY_NAME = Pointer.pointer(Component.class, Key.key("adventure", "display_name"));
   Pointer<Locale> LOCALE = Pointer.pointer(Locale.class, Key.key("adventure", "locale"));

   static Identity nil() {
      return NilIdentity.INSTANCE;
   }

   static Identity identity(final UUID uuid) {
      return (Identity)(uuid.equals(NilIdentity.NIL_UUID) ? NilIdentity.INSTANCE : new IdentityImpl(uuid));
   }

   UUID uuid();

   @Override
   default Identity identity() {
      return this;
   }
}
