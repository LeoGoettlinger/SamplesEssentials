package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.schemafields;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.FieldName;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.SearchProtocol;

public class NumericField extends SchemaField {
   private boolean indexMissing;
   private boolean sortable;
   private boolean noIndex;

   public NumericField(String fieldName) {
      super(fieldName);
   }

   public NumericField(FieldName fieldName) {
      super(fieldName);
   }

   public static NumericField of(String fieldName) {
      return new NumericField(fieldName);
   }

   public static NumericField of(FieldName fieldName) {
      return new NumericField(fieldName);
   }

   public NumericField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public NumericField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public NumericField sortable() {
      this.sortable = true;
      return this;
   }

   public NumericField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName);
      args.add((Rawable)SearchProtocol.SearchKeyword.NUMERIC);
      if (this.indexMissing) {
         args.add((Rawable)SearchProtocol.SearchKeyword.INDEXMISSING);
      }

      if (this.sortable) {
         args.add((Rawable)SearchProtocol.SearchKeyword.SORTABLE);
      }

      if (this.noIndex) {
         args.add((Rawable)SearchProtocol.SearchKeyword.NOINDEX);
      }
   }
}
