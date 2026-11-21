package su.nightexpress.nightcore.ui.inventory.condition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.LowerCase;

@Deprecated
public class ConditionRegistry {
   private final Map<String, ItemStateCondition> conditionMap = new HashMap<>();

   public void register(@NonNull String name, @NonNull ItemStateCondition condition) {
      String id = LowerCase.INTERNAL.apply(name);
      if (!this.conditionMap.containsKey(id)) {
         this.conditionMap.put(id, condition);
      }
   }

   @Nullable
   public ItemStateCondition getById(@NonNull String id) {
      return this.conditionMap.get(LowerCase.INTERNAL.apply(id));
   }

   @NonNull
   public Optional<ItemStateCondition> byId(@NonNull String id) {
      return Optional.ofNullable(this.getById(id));
   }
}
