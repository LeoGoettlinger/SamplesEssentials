package de.theredend2000.advancedhunt.lib.cronutils.model.definition;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CronDefinition implements Serializable {
   private static final long serialVersionUID = 7067112327461432170L;
   private final Map<CronFieldName, FieldDefinition> fieldDefinitions;
   private final Set<CronConstraint> cronConstraints;
   private final Set<CronNicknames> cronNicknames;
   private final boolean matchDayOfWeekAndDayOfMonth;

   public CronDefinition(
      List<FieldDefinition> fieldDefinitions, Set<CronConstraint> cronConstraints, Set<CronNicknames> cronNicknames, boolean matchDayOfWeekAndDayOfMonth
   ) {
      Preconditions.checkNotNull(cronNicknames, "Cron nicknames must not be null");
      if (!cronNicknames.contains(CronNicknames.REBOOT)) {
         Preconditions.checkNotNull(fieldDefinitions, "Field definitions must not be null");
         Preconditions.checkNotNull(cronConstraints, "Cron validations must not be null");
         Preconditions.checkNotNullNorEmpty(fieldDefinitions, "Field definitions must not be empty");
         Preconditions.checkArgument(!fieldDefinitions.get(0).isOptional(), "The first field must not be optional");
      }

      this.fieldDefinitions = new EnumMap<>(CronFieldName.class);

      for (FieldDefinition field : fieldDefinitions) {
         this.fieldDefinitions.put(field.getFieldName(), field);
      }

      this.cronConstraints = Collections.unmodifiableSet(cronConstraints);
      this.cronNicknames = Collections.unmodifiableSet(cronNicknames);
      this.matchDayOfWeekAndDayOfMonth = matchDayOfWeekAndDayOfMonth;
   }

   public boolean isMatchDayOfWeekAndDayOfMonth() {
      return this.matchDayOfWeekAndDayOfMonth;
   }

   public Set<FieldDefinition> getFieldDefinitions() {
      return new HashSet<>(this.fieldDefinitions.values());
   }

   public Map<CronFieldName, FieldDefinition> retrieveFieldDefinitionsAsMap() {
      return Collections.unmodifiableMap(this.fieldDefinitions);
   }

   public FieldDefinition getFieldDefinition(CronFieldName cronFieldName) {
      return this.fieldDefinitions.get(cronFieldName);
   }

   public boolean containsFieldDefinition(CronFieldName cronFieldName) {
      return this.fieldDefinitions.containsKey(cronFieldName);
   }

   public Set<CronConstraint> getCronConstraints() {
      return this.cronConstraints;
   }

   public Set<CronNicknames> getCronNicknames() {
      return this.cronNicknames;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CronDefinition that = (CronDefinition)o;
         return this.matchDayOfWeekAndDayOfMonth == that.matchDayOfWeekAndDayOfMonth
            && this.fieldDefinitions.equals(that.fieldDefinitions)
            && this.cronConstraints.equals(that.cronConstraints)
            && this.cronNicknames.equals(that.cronNicknames);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fieldDefinitions, this.cronConstraints, this.cronNicknames, this.matchDayOfWeekAndDayOfMonth);
   }
}
