package org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries;

import java.util.Map;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.KeyValue;

public class TSMGetElement extends KeyValue<String, TSElement> {
   private final Map<String, String> labels;

   public TSMGetElement(String key, Map<String, String> labels, TSElement value) {
      super(key, value);
      this.labels = labels;
   }

   public Map<String, String> getLabels() {
      return this.labels;
   }

   public TSElement getElement() {
      return this.getValue();
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{key=" + this.getKey() + ", labels=" + this.labels + ", element=" + this.getElement() + '}';
   }
}
