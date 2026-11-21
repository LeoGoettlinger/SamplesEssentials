package de.theredend2000.advancedhunt.lib.xseries.profiles.gameprofile.property;

import com.mojang.authlib.properties.Property;

public final class OldProperty extends MojangProperty {
   protected OldProperty(Property object) {
      super(object);
   }

   @Override
   public String name() {
      return this.object.getName();
   }

   @Override
   public String value() {
      return this.object.getValue();
   }

   @Override
   public String signature() {
      return this.object.getSignature();
   }
}
