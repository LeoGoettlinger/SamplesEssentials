package de.theredend2000.advancedhunt.lib.xseries.profiles.gameprofile.property;

import com.mojang.authlib.properties.Property;
import de.theredend2000.advancedhunt.lib.xseries.AbstractReferencedClass;
import javax.annotation.Nullable;
import org.jspecify.annotations.NonNull;

public abstract class MojangProperty extends AbstractReferencedClass<Property> {
   protected final Property object;

   protected MojangProperty(Property object) {
      this.object = object;
   }

   public final Property object() {
      return this.object;
   }

   @NonNull
   public abstract String name();

   @NonNull
   public abstract String value();

   @Nullable
   public abstract String signature();

   @NonNull
   public final Property copy() {
      return new Property(this.name(), this.value(), this.signature());
   }
}
