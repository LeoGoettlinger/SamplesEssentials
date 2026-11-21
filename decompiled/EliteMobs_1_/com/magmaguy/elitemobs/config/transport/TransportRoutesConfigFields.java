package com.magmaguy.elitemobs.config.transport;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.transport.TransportRoute;

public final class TransportRoutesConfigFields extends CustomConfigFields {
   private TransportRoute route;

   public TransportRoutesConfigFields(String filename, boolean enabled) {
      super(filename, enabled);
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.fileConfiguration.getBoolean("isEnabled", true);
      if (this.isEnabled) {
         this.route = TransportRoute.read(this.filename.replaceFirst("\\.yml$", ""), this.fileConfiguration);
      }
   }

   public TransportRoute getRoute() {
      return this.route;
   }
}
