package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.json;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonFactory;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.Version;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.json.JsonReadFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.json.JsonWriteFeature;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ObjectMapper;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperBuilder;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.PackageVersion;

public class JsonMapper extends ObjectMapper {
   private static final long serialVersionUID = 1L;

   public JsonMapper() {
      this(new JsonFactory());
   }

   public JsonMapper(JsonFactory f) {
      super(f);
   }

   protected JsonMapper(JsonMapper src) {
      super(src);
   }

   public JsonMapper copy() {
      this._checkInvalidCopy(JsonMapper.class);
      return new JsonMapper(this);
   }

   public static JsonMapper.Builder builder() {
      return new JsonMapper.Builder(new JsonMapper());
   }

   public static JsonMapper.Builder builder(JsonFactory streamFactory) {
      return new JsonMapper.Builder(new JsonMapper(streamFactory));
   }

   public JsonMapper.Builder rebuild() {
      return new JsonMapper.Builder(this.copy());
   }

   @Override
   public Version version() {
      return PackageVersion.VERSION;
   }

   @Override
   public JsonFactory getFactory() {
      return this._jsonFactory;
   }

   public boolean isEnabled(JsonReadFeature f) {
      return this.isEnabled(f.mappedFeature());
   }

   public boolean isEnabled(JsonWriteFeature f) {
      return this.isEnabled(f.mappedFeature());
   }

   public static class Builder extends MapperBuilder<JsonMapper, JsonMapper.Builder> {
      public Builder(JsonMapper m) {
         super(m);
      }

      public JsonMapper.Builder enable(JsonReadFeature... features) {
         for (JsonReadFeature f : features) {
            this._mapper.enable(new JsonParser.Feature[]{f.mappedFeature()});
         }

         return this;
      }

      public JsonMapper.Builder disable(JsonReadFeature... features) {
         for (JsonReadFeature f : features) {
            this._mapper.disable(new JsonParser.Feature[]{f.mappedFeature()});
         }

         return this;
      }

      public JsonMapper.Builder configure(JsonReadFeature f, boolean state) {
         if (state) {
            this._mapper.enable(new JsonParser.Feature[]{f.mappedFeature()});
         } else {
            this._mapper.disable(new JsonParser.Feature[]{f.mappedFeature()});
         }

         return this;
      }

      public JsonMapper.Builder enable(JsonWriteFeature... features) {
         for (JsonWriteFeature f : features) {
            this._mapper.enable(new JsonGenerator.Feature[]{f.mappedFeature()});
         }

         return this;
      }

      public JsonMapper.Builder disable(JsonWriteFeature... features) {
         for (JsonWriteFeature f : features) {
            this._mapper.disable(new JsonGenerator.Feature[]{f.mappedFeature()});
         }

         return this;
      }

      public JsonMapper.Builder configure(JsonWriteFeature f, boolean state) {
         if (state) {
            this._mapper.enable(new JsonGenerator.Feature[]{f.mappedFeature()});
         } else {
            this._mapper.disable(new JsonGenerator.Feature[]{f.mappedFeature()});
         }

         return this;
      }
   }
}
