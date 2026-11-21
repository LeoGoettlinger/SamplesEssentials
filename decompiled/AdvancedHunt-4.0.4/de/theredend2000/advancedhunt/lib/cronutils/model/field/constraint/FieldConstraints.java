package de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FieldConstraints implements Serializable {
   private static final long serialVersionUID = -9112124669329704710L;
   private final Map<String, Integer> stringMapping;
   private final Map<Integer, Integer> intMapping;
   private final Set<SpecialChar> specialChars;
   private final Integer startRange;
   private final Integer endRange;
   private final boolean strictRange;

   public FieldConstraints(
      Map<String, Integer> stringMapping, Map<Integer, Integer> intMapping, Set<SpecialChar> specialChars, int startRange, int endRange, boolean strictRange
   ) {
      this.stringMapping = Collections.unmodifiableMap(Preconditions.checkNotNull(stringMapping, "String mapping must not be null"));
      this.intMapping = Collections.unmodifiableMap(Preconditions.checkNotNull(intMapping, "Integer mapping must not be null"));
      this.specialChars = Collections.unmodifiableSet(Preconditions.checkNotNull(specialChars, "Special (non-standard) chars set must not be null"));
      this.startRange = startRange;
      this.endRange = endRange;
      this.strictRange = strictRange;
   }

   public int getStartRange() {
      return this.startRange;
   }

   public int getEndRange() {
      return this.endRange;
   }

   public Set<SpecialChar> getSpecialChars() {
      return this.specialChars;
   }

   public boolean isInRange(int value) {
      return value >= this.getStartRange() && value <= this.getEndRange();
   }

   public boolean isPeriodInRange(int period) {
      return period > 0 && period <= this.getEndRange() - this.getStartRange() + 1 && period <= this.getEndRange();
   }

   public Set<String> getStringMappingKeySet() {
      return this.stringMapping.keySet();
   }

   public Integer getStringMappingValue(String exp) {
      return this.stringMapping.get(exp);
   }

   public Integer getIntMappingValue(Integer exp) {
      return this.intMapping.get(exp);
   }

   public boolean isStrictRange() {
      return this.strictRange;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FieldConstraints that = (FieldConstraints)o;
         return this.strictRange == that.strictRange
            && this.stringMapping.equals(that.stringMapping)
            && this.intMapping.equals(that.intMapping)
            && this.specialChars.equals(that.specialChars)
            && this.startRange.equals(that.startRange)
            && this.endRange.equals(that.endRange);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.stringMapping, this.intMapping, this.specialChars, this.startRange, this.endRange, this.strictRange);
   }
}
