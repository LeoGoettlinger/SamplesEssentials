package net.luckperms.api.metastacking;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface DuplicateRemovalFunction {
   DuplicateRemovalFunction RETAIN_ALL = new DuplicateRemovalFunction() {
      @Override
      public <T> void processDuplicates(@NonNull List<T> list) {
      }

      @Override
      public String toString() {
         return "DuplicateRemovalFunction#RETAIN_ALL";
      }
   };
   DuplicateRemovalFunction FIRST_ONLY = new DuplicateRemovalFunction() {
      @Override
      public <T> void processDuplicates(@NonNull List<T> list) {
         Set<T> seen = new HashSet<>(list.size());
         ListIterator<T> it = list.listIterator();

         while (it.hasNext()) {
            T next = it.next();
            if (!seen.add(next)) {
               it.remove();
            }
         }
      }

      @Override
      public String toString() {
         return "DuplicateRemovalFunction#FIRST_ONLY";
      }
   };
   DuplicateRemovalFunction LAST_ONLY = new DuplicateRemovalFunction() {
      @Override
      public <T> void processDuplicates(@NonNull List<T> list) {
         Set<T> seen = new HashSet<>(list.size());
         ListIterator<T> it = list.listIterator(list.size());

         while (it.hasPrevious()) {
            T next = it.previous();
            if (!seen.add(next)) {
               it.remove();
            }
         }
      }

      @Override
      public String toString() {
         return "DuplicateRemovalFunction#LAST_ONLY";
      }
   };

   <T> void processDuplicates(@NonNull List<T> var1);
}
