package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialCharFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;

public class On extends FieldExpression {
   private static final long serialVersionUID = 8746471281123327324L;
   private static final int DEFAULT_NTH_VALUE = -1;
   private final IntegerFieldValue time;
   private final IntegerFieldValue nth;
   private final SpecialCharFieldValue specialChar;

   public On(SpecialCharFieldValue specialChar) {
      this(new IntegerFieldValue(-1), specialChar);
   }

   public On(IntegerFieldValue time) {
      this(time, new SpecialCharFieldValue(SpecialChar.NONE));
   }

   public On(IntegerFieldValue time, SpecialCharFieldValue specialChar) {
      this(time, specialChar, new IntegerFieldValue(-1));
      Preconditions.checkArgument(!specialChar.getValue().equals(SpecialChar.HASH), "value missing for a#b cron expression");
   }

   public On(IntegerFieldValue time, SpecialCharFieldValue specialChar, IntegerFieldValue nth) {
      Preconditions.checkNotNull(time, "time must not be null");
      Preconditions.checkNotNull(specialChar, "special char must not null");
      Preconditions.checkNotNull(nth, "nth value must not be null");
      this.time = time;
      this.specialChar = specialChar;
      this.nth = nth;
   }

   public IntegerFieldValue getTime() {
      return this.time;
   }

   public IntegerFieldValue getNth() {
      return this.nth;
   }

   public SpecialCharFieldValue getSpecialChar() {
      return this.specialChar;
   }

   @Override
   public FieldExpression accept(FieldExpressionVisitor visitor) {
      return visitor.visit(this);
   }

   @Override
   public String asString() {
      switch (this.specialChar.getValue()) {
         case NONE:
            return this.getTime().toString();
         case HASH:
            return String.format("%s#%s", this.getTime(), this.getNth());
         case W:
            return this.isDefault(this.getTime()) ? "W" : String.format("%sW", this.getTime());
         case L:
            return this.isDefault(this.getTime()) ? "L" + this.getNthStringRepresentation() : String.format("%sL", this.getTime());
         case QUESTION_MARK:
            return "?";
         default:
            return this.specialChar.toString();
      }
   }

   private String getNthStringRepresentation() {
      return this.isDefault(this.getNth()) ? "" : String.format("-%s", this.getNth());
   }

   private boolean isDefault(IntegerFieldValue fieldValue) {
      return fieldValue.getValue() == -1;
   }
}
