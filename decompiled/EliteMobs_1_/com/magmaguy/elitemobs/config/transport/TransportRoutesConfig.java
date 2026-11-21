package com.magmaguy.elitemobs.config.transport;

import com.magmaguy.elitemobs.config.EliteMobsConfigInheritance;
import com.magmaguy.elitemobs.magmacore.config.CustomConfig;

public final class TransportRoutesConfig extends CustomConfig {
   public TransportRoutesConfig() {
      super("transport_routes", "com.magmaguy.elitemobs.config.transport.premade", TransportRoutesConfigFields.class, EliteMobsConfigInheritance.POLICY);
   }
}
