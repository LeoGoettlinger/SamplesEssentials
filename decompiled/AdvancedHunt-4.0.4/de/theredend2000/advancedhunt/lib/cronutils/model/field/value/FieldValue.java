package de.theredend2000.advancedhunt.lib.cronutils.model.field.value;

import java.io.Serializable;

public abstract class FieldValue<T extends Serializable> implements Serializable {
   private static final long serialVersionUID = 2392311922703946889L;

   public abstract T getValue();

   @Override
   public final String toString() {
      return String.format("%s", this.getValue());
   }
}
