package de.theredend2000.advancedhunt.lib.cronutils.model.time;

import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class TimeNode {
   protected List<Integer> values;

   public TimeNode(List<Integer> values) {
      this.values = Preconditions.checkNotNullNorEmpty(values, "Values must not be empty");
      Collections.sort(this.values);
   }

   public NearestValue getNextValue(int reference, int shifts) {
      return this.getNearestForwardValue(reference, shifts);
   }

   public List<Integer> getValues() {
      return Collections.unmodifiableList(this.values);
   }

   public NearestValue getPreviousValue(int reference, int shifts) {
      return this.getNearestBackwardValue(reference, shifts);
   }

   @VisibleForTesting
   NearestValue getNearestForwardValue(int reference, int shiftsToApply) {
      List<Integer> temporaryValues = new ArrayList<>(this.values);
      int index = 0;
      boolean foundGreater = false;
      AtomicInteger shift = new AtomicInteger(0);
      if (!temporaryValues.contains(reference)) {
         for (Integer value : temporaryValues) {
            if (value > reference) {
               index = temporaryValues.indexOf(value);
               shiftsToApply--;
               foundGreater = true;
               break;
            }
         }

         if (!foundGreater) {
            shift.incrementAndGet();
         }
      } else {
         index = temporaryValues.indexOf(reference);
      }

      int valuex = temporaryValues.get(index);

      for (int j = 0; j < shiftsToApply; j++) {
         valuex = this.getValueFromList(temporaryValues, index + 1, shift);
         index = temporaryValues.indexOf(valuex);
      }

      return new NearestValue(valuex, shift.get());
   }

   @VisibleForTesting
   NearestValue getNearestBackwardValue(int reference, int shiftsToApply) {
      List<Integer> temporaryValues = new ArrayList<>(this.values);
      Collections.reverse(temporaryValues);
      int index = 0;
      boolean foundSmaller = false;
      AtomicInteger shift = new AtomicInteger(0);
      if (!temporaryValues.contains(reference)) {
         for (Integer value : temporaryValues) {
            if (value < reference) {
               index = temporaryValues.indexOf(value);
               shiftsToApply--;
               foundSmaller = true;
               break;
            }
         }

         if (!foundSmaller) {
            shift.incrementAndGet();
         }
      } else {
         index = temporaryValues.indexOf(reference);
      }

      int valuex = temporaryValues.get(index);

      for (int j = 0; j < shiftsToApply; j++) {
         valuex = this.getValueFromList(temporaryValues, index + 1, shift);
         index = temporaryValues.indexOf(valuex);
      }

      return new NearestValue(valuex, shift.get());
   }

   @VisibleForTesting
   int getValueFromList(List<Integer> values, int index, AtomicInteger shift) {
      Preconditions.checkNotNullNorEmpty(values, "List must not be empty");
      if (index < 0) {
         index += values.size();
         shift.incrementAndGet();
         return this.getValueFromList(values, index, shift);
      } else if (index >= values.size()) {
         index -= values.size();
         shift.incrementAndGet();
         return this.getValueFromList(values, index, shift);
      } else {
         return values.get(index);
      }
   }
}
