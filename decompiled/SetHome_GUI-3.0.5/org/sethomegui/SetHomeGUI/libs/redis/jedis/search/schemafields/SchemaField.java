package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.schemafields;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.FieldName;

public abstract class SchemaField implements IParams {
   protected final FieldName fieldName;

   public SchemaField(String fieldName) {
      this.fieldName = new FieldName(fieldName);
   }

   public SchemaField(FieldName fieldName) {
      this.fieldName = fieldName;
   }

   public SchemaField as(String attribute) {
      this.fieldName.as(attribute);
      return this;
   }

   public final FieldName getFieldName() {
      return this.fieldName;
   }

   public final String getName() {
      return this.fieldName.getName();
   }
}
