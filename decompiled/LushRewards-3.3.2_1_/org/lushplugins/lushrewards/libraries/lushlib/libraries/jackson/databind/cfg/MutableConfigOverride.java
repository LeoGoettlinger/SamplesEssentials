package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg;

import java.io.Serializable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonAutoDetect;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonFormat;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonIgnoreProperties;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonInclude;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonSetter;

public class MutableConfigOverride extends ConfigOverride implements Serializable {
   private static final long serialVersionUID = 1L;

   public MutableConfigOverride() {
   }

   protected MutableConfigOverride(MutableConfigOverride src) {
      super(src);
   }

   public MutableConfigOverride copy() {
      return new MutableConfigOverride(this);
   }

   public MutableConfigOverride setFormat(JsonFormat.Value v) {
      this._format = v;
      return this;
   }

   public MutableConfigOverride setInclude(JsonInclude.Value v) {
      this._include = v;
      return this;
   }

   public MutableConfigOverride setIncludeAsProperty(JsonInclude.Value v) {
      this._includeAsProperty = v;
      return this;
   }

   public MutableConfigOverride setIgnorals(JsonIgnoreProperties.Value v) {
      this._ignorals = v;
      return this;
   }

   public MutableConfigOverride setIsIgnoredType(Boolean v) {
      this._isIgnoredType = v;
      return this;
   }

   public MutableConfigOverride setSetterInfo(JsonSetter.Value v) {
      this._setterInfo = v;
      return this;
   }

   public MutableConfigOverride setVisibility(JsonAutoDetect.Value v) {
      this._visibility = v;
      return this;
   }

   public MutableConfigOverride setMergeable(Boolean v) {
      this._mergeable = v;
      return this;
   }
}
