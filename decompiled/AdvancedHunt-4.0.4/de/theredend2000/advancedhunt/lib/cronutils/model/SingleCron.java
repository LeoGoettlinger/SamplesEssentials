package de.theredend2000.advancedhunt.lib.cronutils.model;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.CronMapper;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronConstraint;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.ValidationFieldExpressionVisitor;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SingleCron implements Cron {
   private static final long serialVersionUID = 7487370826825439098L;
   private final CronDefinition cronDefinition;
   private final Map<CronFieldName, CronField> fields;
   private String asString;

   public SingleCron(CronDefinition cronDefinition, List<CronField> fields) {
      this.cronDefinition = Preconditions.checkNotNull(cronDefinition, "CronDefinition must not be null");
      Preconditions.checkNotNull(fields, "CronFields cannot be null");
      this.fields = new EnumMap<>(CronFieldName.class);

      for (CronField field : fields) {
         this.fields.put(field.getField(), field);
      }
   }

   @Override
   public CronField retrieve(CronFieldName name) {
      return this.fields.get(Preconditions.checkNotNull(name, "CronFieldName must not be null"));
   }

   @Override
   public Map<CronFieldName, CronField> retrieveFieldsAsMap() {
      return Collections.unmodifiableMap(this.fields);
   }

   @Override
   public String asString() {
      if (this.asString == null) {
         ArrayList<CronField> temporaryFields = new ArrayList<>(this.fields.values());
         temporaryFields.sort(CronField.createFieldComparator());
         StringBuilder builder = new StringBuilder();

         for (CronField field : temporaryFields) {
            builder.append(String.format("%s ", field.getExpression().asString()));
         }

         this.asString = builder.toString().trim();
      }

      return this.asString;
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
