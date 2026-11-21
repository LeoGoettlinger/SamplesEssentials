package net.luckperms.api.actionlog.filter;

import java.util.UUID;
import java.util.function.Predicate;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.actionlog.Action;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ActionFilter extends Predicate<Action> {
   static ActionFilter any() {
      return LuckPermsProvider.get().getActionFilterFactory().any();
   }

   static ActionFilter source(UUID uniqueId) {
      return LuckPermsProvider.get().getActionFilterFactory().source(uniqueId);
   }

   static ActionFilter user(UUID uniqueId) {
      return LuckPermsProvider.get().getActionFilterFactory().user(uniqueId);
   }

   static ActionFilter group(String name) {
      return LuckPermsProvider.get().getActionFilterFactory().group(name);
   }

   static ActionFilter track(String name) {
      return LuckPermsProvider.get().getActionFilterFactory().track(name);
   }

   static ActionFilter search(String query) {
      return LuckPermsProvider.get().getActionFilterFactory().search(query);
   }

   boolean test(Action var1);
}
