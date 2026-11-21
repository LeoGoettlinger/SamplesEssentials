package de.theredend2000.advancedhunt.lib.cronutils.model.field.definition;

import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;

public class FieldQuestionMarkDefinitionBuilder extends FieldDefinitionBuilder {
   public FieldQuestionMarkDefinitionBuilder(CronDefinitionBuilder cronDefinitionBuilder, CronFieldName fieldName) {
      super(cronDefinitionBuilder, fieldName);
   }

   public FieldQuestionMarkDefinitionBuilder supportsQuestionMark() {
      this.constraints.addQuestionMarkSupport();
      return this;
   }
}
