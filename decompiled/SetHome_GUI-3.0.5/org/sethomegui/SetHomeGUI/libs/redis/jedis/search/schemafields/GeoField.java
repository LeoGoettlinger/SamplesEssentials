package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.schemafields;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.FieldName;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.SearchProtocol;

public class GeoField extends SchemaField {
   private boolean indexMissing;
   private boolean sortable;
   private boolean noIndex;

   public GeoField(String fieldName) {
      super(fieldName);
   }

   public GeoField(FieldName fieldName) {
      super(fieldName);
   }

   public static GeoField of(String fieldName) {
      return new GeoField(fieldName);
   }

   public static GeoField of(FieldName fieldName) {
      return new GeoField(fieldName);
   }

   public GeoField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public GeoField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public GeoField sortable() {
      this.sortable = true;
      return this;
   }

   public GeoField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName);
      args.add((Rawable)SearchProtocol.SearchKeyword.GEO);
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
