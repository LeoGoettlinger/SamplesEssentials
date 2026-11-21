package org.lushplugins.lushrewards.libraries.mysql.cj.conf;

public class StringProperty extends AbstractRuntimeProperty<String> {
   private static final long serialVersionUID = -4141084145739428803L;

   protected StringProperty(PropertyDefinition<String> propertyDefinition) {
      super(propertyDefinition);
   }

   @Override
   public String getStringValue() {
      return this.value;
   }
}
