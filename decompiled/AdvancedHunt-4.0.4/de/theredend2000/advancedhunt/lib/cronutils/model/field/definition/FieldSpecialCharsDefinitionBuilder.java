package de.theredend2000.advancedhunt.lib.cronutils.model.field.definition;

import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;

public class FieldSpecialCharsDefinitionBuilder extends FieldQuestionMarkDefinitionBuilder {
   public FieldSpecialCharsDefinitionBuilder(CronDefinitionBuilder parserBuilder, CronFieldName fieldName) {
      super(parserBuilder, fieldName);
   }

   public FieldSpecialCharsDefinitionBuilder supportsHash() {
      this.constraints.addHashSupport();
      return this;
   }

   public FieldSpecialCharsDefinitionBuilder supportsL() {
      this.constraints.addLSupport();
      return this;
   }

   public FieldSpecialCharsDefinitionBuilder supportsW() {
      this.constraints.addWSupport();
      return this;
   }

   public FieldSpecialCharsDefinitionBuilder supportsLW() {
      this.constraints.addLWSupport();
      return this;
   }

   public FieldSpecialCharsDefinitionBuilder withIntMapping(int source, int dest) {
      super.withIntMapping(source, dest);
      return this;
   }

   public FieldSpecialCharsDefinitionBuilder withValidRange(int startRange, int endRange) {
      super.withValidRange(startRange, endRange);
      return this;
   }
}
