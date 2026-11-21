package com.earth2me.essentials.perm.impl;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.context.ImmutableContextSet.Builder;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHandler extends ModernVaultHandler {
   private LuckPerms luckPerms;
   private Essentials ess;
   private LuckPermsHandler.CombinedCalculator calculator;
   private final Map<UUID, Map<String, Boolean>> permissionCache = new ConcurrentHashMap<>();
   private EventSubscription<UserDataRecalculateEvent> recalculateSubscription;

   @Override
   public void registerContext(final String context, final Function<User, Iterable<String>> calculator, final Supplier<Iterable<String>> suggestions) {
      if (this.calculator == null) {
         this.calculator = new LuckPermsHandler.CombinedCalculator();
         this.luckPerms.getContextManager().registerCalculator(this.calculator);
      }

      this.calculator.calculators.add(new LuckPermsHandler.Calculator(context, calculator, suggestions));
   }

   @Override
   public void unregisterContexts() {
      if (this.calculator != null) {
         this.luckPerms.getContextManager().unregisterCalculator(this.calculator);
         this.calculator = null;
      }

      if (this.recalculateSubscription != null) {
         this.recalculateSubscription.close();
         this.recalculateSubscription = null;
      }

      this.permissionCache.clear();
   }

   @Override
   public boolean hasPermissionCached(final Player base, final String node) {
      UUID uuid = base.getUniqueId();
      Map<String, Boolean> userCache = this.permissionCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
      return userCache.computeIfAbsent(node, k -> this.hasPermission(base, node));
   }

   @Override
   public void invalidatePermissionCache(final UUID uuid) {
      this.invalidateCache(uuid);
   }

   public void invalidateCache(final UUID uuid) {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "Invalidating permission cache for " + uuid);
      }

      this.permissionCache.remove(uuid);
   }

   @Override
   public List<String> getGroups() {
      List<String> groups = new ArrayList<>();

      for (Group group : this.luckPerms.getGroupManager().getLoadedGroups()) {
         groups.add(group.getName());
      }

      for (String group : super.getGroups()) {
         if (!groups.contains(group)) {
            groups.add(group);
         }
      }

      return groups;
   }

   @Override
   public boolean isOfflinePermissionSet(UUID uuid, String node) {
      net.luckperms.api.model.user.User user = (net.luckperms.api.model.user.User)this.luckPerms.getUserManager().loadUser(uuid).join();
      if (user == null) {
         return false;
      } else {
         QueryOptions options = this.luckPerms.getContextManager().getStaticQueryOptions();
         return user.getCachedData().getPermissionData(options).checkPermission(node).asBoolean();
      }
   }

   @Override
   public boolean tryProvider(Essentials ess) {
      RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
      if (provider != null) {
         this.luckPerms = (LuckPerms)provider.getProvider();
         this.ess = ess;
         this.recalculateSubscription = this.luckPerms
            .getEventBus()
            .subscribe(ess, UserDataRecalculateEvent.class, event -> this.invalidateCache(event.getUser().getUniqueId()));
      }

      return this.luckPerms != null && super.tryProvider(ess);
   }

   private static final class Calculator {
      private final String id;
      private final Function<User, Iterable<String>> function;
      private final Supplier<Iterable<String>> suggestions;

      private Calculator(String id, Function<User, Iterable<String>> function, Supplier<Iterable<String>> suggestions) {
         this.id = id;
         this.function = function;
         this.suggestions = suggestions;
      }
   }

   private final class CombinedCalculator implements ContextCalculator<Player> {
      private final Set<LuckPermsHandler.Calculator> calculators = new HashSet<>();

      private CombinedCalculator() {
      }

      public void calculate(final Player target, final ContextConsumer consumer) {
         if (LuckPermsHandler.this.ess.getUsers().getAllUserUUIDs().contains(target.getUniqueId())) {
            User user = LuckPermsHandler.this.ess.getUsers().loadUncachedUser(target.getUniqueId());
            if (user != null) {
               for (LuckPermsHandler.Calculator calculator : this.calculators) {
                  calculator.function.apply(user).forEach(value -> consumer.accept(calculator.id, value));
               }
            }
         }
      }

      public ContextSet estimatePotentialContexts() {
         Builder builder = ImmutableContextSet.builder();

         for (LuckPermsHandler.Calculator calculator : this.calculators) {
            calculator.suggestions.get().forEach(value -> builder.add(calculator.id, value));
         }

         return builder.build();
      }
   }
}
