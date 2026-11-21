package org.skriptlang.skript.util;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jetbrains.annotations.UnmodifiableView;

public class IndexTrackingTreeMap<V> extends TreeMap<String, V> {
   private final Set<String> mapIndices = new HashSet<>();
   private final Set<Integer> numericalIndices = new HashSet<>();
   private int nextIndex = 1;
   private int maxIndex = -1;

   public IndexTrackingTreeMap() {
   }

   public IndexTrackingTreeMap(Comparator<? super String> comparator) {
      super(comparator);
   }

   public V put(String key, V value) {
      V previous = super.put(key, value);
      if (previous == null && value != null) {
         this.handleInsert(key, this.parsePositiveInt(key), value);
      } else if (previous != null && value == null) {
         this.handleRemove(key, previous);
      } else if (previous != null) {
         this.handleReplace(key, previous, value);
      }

      return previous;
   }

   public void add(V value) {
      Preconditions.checkNotNull(value, "value");
      String key = String.valueOf(this.nextIndex);
      super.put(key, value);
      this.handleInsert(key, this.nextIndex, value);
   }

   @Override
   public V remove(Object key) {
      V value = super.remove(key);
      if (value != null && key instanceof String index) {
         this.handleRemove(index, value);
      }

      return value;
   }

   @Override
   public void clear() {
      super.clear();
      this.numericalIndices.clear();
      this.mapIndices.clear();
      this.nextIndex = 1;
      this.maxIndex = -1;
   }

   public int nextOpenIndex() {
      return this.nextIndex;
   }

   public boolean consecutive() {
      return this.nextIndex == this.maxIndex + 1;
   }

   @UnmodifiableView
   public Collection<String> mapIndices() {
      return Collections.unmodifiableCollection(this.mapIndices);
   }

   private void handleInsert(String key, int index, V value) {
      if (value instanceof Map) {
         this.mapIndices.add(key);
      }

      if (index >= 0) {
         this.numericalIndices.add(index);
         this.maxIndex = Math.max(this.maxIndex, index);
         this.advanceNextIndex();
      }
   }

   private void handleReplace(String key, V previous, V value) {
      if (value instanceof Map) {
         this.mapIndices.add(key);
      } else if (previous instanceof Map) {
         this.mapIndices.remove(key);
      }
   }

   private void handleRemove(String key, V previous) {
      if (previous instanceof Map) {
         this.mapIndices.remove(key);
      }

      int index = this.parsePositiveInt(key);
      if (index >= 0) {
         this.numericalIndices.remove(index);
         if (index == this.maxIndex) {
            this.recomputeMaxIndex();
         }

         this.nextIndex = Math.min(this.nextIndex, index);
      }
   }

   private void advanceNextIndex() {
      if (this.nextIndex == this.maxIndex) {
         this.nextIndex++;
      } else {
         while (this.numericalIndices.contains(this.nextIndex)) {
            this.nextIndex++;
         }
      }
   }

   private void recomputeMaxIndex() {
      while (this.maxIndex >= 0 && !this.numericalIndices.contains(this.maxIndex)) {
         this.maxIndex--;
      }
   }

   private int parsePositiveInt(String string) {
      if (string != null && !string.isBlank() && string.charAt(0) != '0') {
         int value = 0;

         try {
            for (int i = 0; i < string.length(); i++) {
               char c = string.charAt(i);
               if (!this.isDigit(c)) {
                  return -1;
               }

               value = Math.addExact(value * 10, c - '0');
            }

            return value;
         } catch (ArithmeticException var5) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean isDigit(int codepoint) {
      return codepoint >= 48 && codepoint <= 57;
   }
}
