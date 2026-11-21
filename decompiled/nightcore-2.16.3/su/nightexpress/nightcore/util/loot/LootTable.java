package su.nightexpress.nightcore.util.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.Randomizer;

public class LootTable<T, C> {
   private final List<LootEntry<T, C>> entries = new ArrayList<>();
   private final RandomGenerator rnd;

   public LootTable(@NonNull RandomGenerator rnd) {
      this.rnd = rnd;
   }

   @NonNull
   public static <T, C> LootTable<T, C> create() {
      return new LootTable<>(Randomizer.getSource());
   }

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   @NonNull
   public Optional<T> pick(@NonNull C context) {
      if (this.isEmpty()) {
         return Optional.empty();
      } else {
         double totalWeight = 0.0;
         List<LootEntry<T, C>> allowed = new ArrayList<>(this.entries.size());

         for (LootEntry<T, C> entry : this.entries) {
            if (entry.canPick(context)) {
               totalWeight += entry.weight();
               allowed.add(entry);
            }
         }

         double randomValue = this.rnd.nextDouble(totalWeight);

         for (LootEntry<T, C> entryx : allowed) {
            randomValue -= entryx.weight();
            if (randomValue <= 0.0) {
               return Optional.of(entryx.item());
            }
         }

         return Optional.empty();
      }
   }

   @NonNull
   public LootTable<T, C> add(@NonNull T item, double weight) {
      this.entries.add(LootEntry.of(item, weight));
      return this;
   }

   @NonNull
   public LootTable<T, C> add(@NonNull T item, double weight, @NonNull Predicate<C> predicate) {
      this.entries.add(LootEntry.of(item, weight, predicate));
      return this;
   }
}
