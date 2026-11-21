package de.theredend2000.advancedhunt.lib.cronutils.model.field.definition;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class FieldDefinition implements Serializable {
   private static final long serialVersionUID = 7285200909397193383L;
   private final CronFieldName fieldName;
   private final FieldConstraints constraints;
   private final boolean optional;

   public FieldDefinition(CronFieldName fieldName, FieldConstraints constraints) {
      this(fieldName, constraints, false);
   }

   public FieldDefinition(CronFieldName fieldName, FieldConstraints constraints, boolean optional) {
      this.fieldName = Preconditions.checkNotNull(fieldName, "CronFieldName must not be null");
      this.constraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
      this.optional = optional;
   }

   public CronFieldName getFieldName() {
      return this.fieldName;
   }

   public FieldConstraints getConstraints() {
      return this.constraints;
   }

   public boolean isOptional() {
      return this.optional;
   }

   public static Comparator<FieldDefinition> createFieldDefinitionComparator() {
      return Comparator.comparingInt(o -> o.getFieldName().getOrder());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FieldDefinition that = (FieldDefinition)o;
         return this.optional == that.optional && this.fieldName == that.fieldName && this.constraints.equals(that.constraints);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fieldName, this.constraints, this.optional);
   }
}
