package com.sk89q.worldedit.util.task.progress;

import java.util.Arrays;
import java.util.Collection;

public abstract class Progress {
   private static final Progress COMPLETED = of(1.0);
   private static final Progress INDETERMINATE = new Progress() {
      @Override
      public boolean isIndeterminate() {
         return true;
      }

      @Override
      public double getProgress() {
         return 0.0;
      }
   };

   private Progress() {
   }

   public abstract boolean isIndeterminate();

   public abstract double getProgress();

   public static Progress indeterminate() {
      return INDETERMINATE;
   }

   public static Progress completed() {
      return COMPLETED;
   }

   public static Progress of(double value) {
      if (value < 0.0) {
         value = 0.0;
      } else if (value > 1.0) {
         value = 1.0;
      }

      final double finalValue = value;
      return new Progress() {
         @Override
         public boolean isIndeterminate() {
            return false;
         }

         @Override
         public double getProgress() {
            return finalValue;
         }
      };
   }

   public static Progress split(Progress... objects) {
      return split(Arrays.asList(objects));
   }

   public static Progress split(Collection<Progress> progress) {
      int count = 0;
      double total = 0.0;

      for (Progress p : progress) {
         if (p.isIndeterminate()) {
            return indeterminate();
         }

         total += p.getProgress();
      }

      return of(total / count);
   }

   public static Progress splitObservables(ProgressObservable... observables) {
      return splitObservables(Arrays.asList(observables));
   }

   public static Progress splitObservables(Collection<? extends ProgressObservable> observables) {
      int count = 0;
      double total = 0.0;

      for (ProgressObservable observable : observables) {
         Progress p = observable.getProgress();
         if (p.isIndeterminate()) {
            return indeterminate();
         }

         total += p.getProgress();
      }

      return of(total / count);
   }
}
