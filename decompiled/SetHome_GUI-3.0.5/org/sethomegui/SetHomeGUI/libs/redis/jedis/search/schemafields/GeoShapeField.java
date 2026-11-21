package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.schemafields;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.FieldName;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.SearchProtocol;

public class GeoShapeField extends SchemaField {
   private final GeoShapeField.CoordinateSystem system;
   private boolean indexMissing;
   private boolean noIndex;

   public GeoShapeField(String fieldName, GeoShapeField.CoordinateSystem system) {
      super(fieldName);
      this.system = system;
   }

   public GeoShapeField(FieldName fieldName, GeoShapeField.CoordinateSystem system) {
      super(fieldName);
      this.system = system;
   }

   public static GeoShapeField of(String fieldName, GeoShapeField.CoordinateSystem system) {
      return new GeoShapeField(fieldName, system);
   }

   public GeoShapeField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public GeoShapeField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public GeoShapeField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName).add((Rawable)SearchProtocol.SearchKeyword.GEOSHAPE).add(this.system);
      if (this.indexMissing) {
         args.add((Rawable)SearchProtocol.SearchKeyword.INDEXMISSING);
      }

      if (this.noIndex) {
         args.add((Rawable)SearchProtocol.SearchKeyword.NOINDEX);
      }
   }

   public static enum CoordinateSystem {
      FLAT,
      SPHERICAL;
   }
}
