package de.theredend2000.advancedhunt.lib.cronutils.model.field.value;

import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;

public class SpecialCharFieldValue extends FieldValue<SpecialChar> {
   private static final long serialVersionUID = -2177180413324528746L;
   private SpecialChar specialChar = SpecialChar.NONE;

   public SpecialCharFieldValue(SpecialChar specialChar) {
      Preconditions.checkNotNull(specialChar, "special char must not be null");
      this.specialChar = specialChar;
   }

   public SpecialChar getValue() {
      return this.specialChar;
   }
}
