package de.theredend2000.advancedhunt.lib.xseries.profiles.gameprofile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import java.util.UUID;
import java.util.function.Consumer;

public final class OldGameProfile extends MojangGameProfile {
   protected OldGameProfile(GameProfile object) {
      super(object);
   }

   @Override
   public UUID id() {
      return this.object.getId();
   }

   @Override
   public String name() {
      return this.object.getName();
   }

   @Override
   public PropertyMap properties() {
      return this.object.getProperties();
   }

   @Override
   public MojangGameProfile copy(Consumer<PropertyModifier> propertyModifier) {
      GameProfile clone = new GameProfile(this.id(), this.name());
      PropertyMap properties = clone.getProperties();
      properties.putAll(this.properties());
      if (propertyModifier != null) {
         propertyModifier.accept(new PropertyModifier(properties));
      }

      return new OldGameProfile(clone);
   }
}
