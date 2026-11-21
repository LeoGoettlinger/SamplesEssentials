package de.theredend2000.advancedhunt.lib.cronutils.model.field.definition;

import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraintsBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;

public class FieldDefinitionBuilder {
   protected CronDefinitionBuilder cronDefinitionBuilder;
   protected final CronFieldName fieldName;
   protected FieldConstraintsBuilder constraints;
   protected boolean optional;

   public FieldDefinitionBuilder(CronDefinitionBuilder cronDefinitionBuilder, CronFieldName fieldName) {
      this.cronDefinitionBuilder = Preconditions.checkNotNull(cronDefinitionBuilder, "ParserBuilder must not be null");
      this.fieldName = Preconditions.checkNotNull(fieldName, "CronFieldName must not be null");
      this.constraints = FieldConstraintsBuilder.instance().forField(fieldName);
   }

   public FieldDefinitionBuilder withIntMapping(int source, int dest) {
      this.constraints.withIntValueMapping(source, dest);
      return this;
   }

   public FieldDefinitionBuilder withValidRange(int startRange, int endRange) {
      this.constraints.withValidRange(startRange, endRange);
      return this;
   }

   public FieldDefinitionBuilder withStrictRange() {
      this.constraints.withStrictRange();
      return this;
   }

   public FieldDefinitionBuilder optional() {
      this.optional = true;
      return this;
   }

   public CronDefinitionBuilder and() {
      this.cronDefinitionBuilder.register(new FieldDefinition(this.fieldName, this.constraints.createConstraintsInstance(), this.optional));
      return this.cronDefinitionBuilder;
   }
}
