package com.viaversion.viabackwards.protocol.registration;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocol.shared_registration.SharedRegistrations;

public final class BackwardsRegistrations {
   private static final SharedRegistrations REGISTRATIONS = SharedRegistrations.create();

   public static void apply() {
      REGISTRATIONS.registrations()
         .range(ProtocolVersion.v1_10, ProtocolVersion.v1_19_3, RegistryRegistrations::registerNamedSound1_10)
         .since(ProtocolVersion.v1_14, RegistryRegistrations::registerStopSound1_14)
         .register();
   }

   public static SharedRegistrations registrations() {
      return REGISTRATIONS;
   }
}
