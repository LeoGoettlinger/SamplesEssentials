package de.theredend2000.advancedhunt.lib.cronutils.model.field.value;

public class IntegerFieldValue extends FieldValue<Integer> {
   private static final long serialVersionUID = -1305795676868267699L;
   private final int value;

   public IntegerFieldValue(int value) {
      this.value = value;
   }

   public Integer getValue() {
      return this.value;
   }
}
