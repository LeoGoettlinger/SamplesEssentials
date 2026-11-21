package ac.grim.grimac.utils.lists;

import ac.grim.grimac.shaded.fastutil.doubles.Double2IntMap;
import ac.grim.grimac.shaded.fastutil.doubles.Double2IntOpenHashMap;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.utils.data.Pair;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import lombok.Generated;

public class RunningMode {
   private static final double threshold = 0.001;
   private final Queue<Double> addList;
   private final Double2IntMap popularityMap = new Double2IntOpenHashMap();
   private final int maxSize;

   public RunningMode(int maxSize) {
      if (maxSize == 0) {
         throw new IllegalArgumentException("There's no mode to a size 0 list!");
      } else {
         this.addList = new ArrayBlockingQueue<>(maxSize);
         this.maxSize = maxSize;
      }
   }

   public int size() {
      return this.addList.size();
   }

   public void add(double value) {
      this.pop();

      for (Double2IntMap.Entry entry : this.popularityMap.double2IntEntrySet()) {
         if (Math.abs(entry.getDoubleKey() - value) < 0.001) {
            entry.setValue(entry.getIntValue() + 1);
            this.addList.add(entry.getDoubleKey());
            return;
         }
      }

      this.popularityMap.put(value, 1);
      this.addList.add(value);
   }

   private void pop() {
      if (this.addList.size() >= this.maxSize) {
         double type = this.addList.remove();
         int popularity = this.popularityMap.get(type);
         if (popularity == 1) {
            this.popularityMap.remove(type);
         } else {
            this.popularityMap.put(type, popularity - 1);
         }
      }
   }

   @NotNull
   public Pair<Double, Integer> getMode() {
      int max = 0;
      Double mostPopular = null;

      for (Double2IntMap.Entry entry : this.popularityMap.double2IntEntrySet()) {
         if (entry.getIntValue() > max) {
            max = entry.getIntValue();
            mostPopular = entry.getDoubleKey();
         }
      }

      return new Pair<>(mostPopular, max);
   }

   @Generated
   public int getMaxSize() {
      return this.maxSize;
   }
}
