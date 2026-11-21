package de.theredend2000.advancedhunt.lib.cronutils.model;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.CronMapper;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronConstraint;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.ValidationFieldExpressionVisitor;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RebootCron implements Cron {
   private static final long serialVersionUID = 7487370826825439099L;
   private final CronDefinition cronDefinition;

   public RebootCron(CronDefinition cronDefinition) {
      this.cronDefinition = Preconditions.checkNotNull(cronDefinition, "CronDefinition must not be null");
   }

   @Override
   public CronField retrieve(CronFieldName name) {
      Preconditions.checkNotNull(name, "CronFieldName must not be null");
      return null;
   }

   @Override
   public Map<CronFieldName, CronField> retrieveFieldsAsMap() {
      return Collections.unmodifiableMap(new HashMap<>());
   }

   @Override
   public String asString() {
      return "@reboot";
   }

   @Override
   public CronDefinition getCronDefinition() {
      return this.cronDefinition;
   }

   @Override
   public Cron validate() {
      for (Entry<CronFieldName, CronField> field : this.retrieveFieldsAsMap().entrySet()) {
         CronFieldName fieldName = field.getKey();
         field.getValue().getExpression().accept(new ValidationFieldExpressionVisitor(this.getCronDefinition().getFieldDefinition(fieldName).getConstraints()));
      }

      for (CronConstraint constraint : this.getCronDefinition().getCronConstraints()) {
         if (!constraint.validate(this)) {
            throw new IllegalArgumentException(String.format("Invalid cron expression: %s. %s", this.asString(), constraint.getDescription()));
         }
      }

      return this;
   }

   @Override
   public boolean equivalent(CronMapper cronMapper, Cron cron) {
      return this.asString().equals(cronMapper.map(cron).asString());
   }

   @Override
   public boolean equivalent(Cron cron) {
      return this.asString().equals(cron.asString());
   }
}
