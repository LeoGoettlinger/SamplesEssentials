package com.artillexstudios.axtrade.libs.lamp.bukkit.core;

import com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProvider;
import com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProviderFactory;
import com.artillexstudios.axtrade.libs.lamp.bukkit.BukkitCommandActor;
import com.artillexstudios.axtrade.libs.lamp.bukkit.EntitySelector;
import com.artillexstudios.axtrade.libs.lamp.bukkit.exception.InvalidPlayerException;
import com.artillexstudios.axtrade.libs.lamp.bukkit.exception.MalformedEntitySelectorException;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.exception.CommandErrorException;
import com.artillexstudios.axtrade.libs.lamp.process.ValueResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ValueResolverFactory;
import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import com.artillexstudios.axtrade.libs.lamp.util.Primitives;
import com.google.common.collect.ForwardingList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EntitySelectorResolver implements ValueResolverFactory {
   INSTANCE;

   private boolean supportComplexSelectors;

   private EntitySelectorResolver() {
      try {
         Bukkit.getServer().selectEntities(Bukkit.getConsoleSender(), "@a");
         this.supportComplexSelectors = false;
      } catch (Throwable var4) {
         this.supportComplexSelectors = false;
      }
   }

   @Nullable
   @Override
   public ValueResolver<?> create(@NotNull CommandParameter parameter) {
      if (EntitySelector.class.isAssignableFrom(parameter.getType())) {
         Class<?> entityType = (Class<?>)Primitives.getInsideGeneric(parameter.getFullType(), Entity.class);
         return Player.class.isAssignableFrom(entityType) ? this::resolvePlayerSelector : context -> {
            String selector = context.pop();

            try {
               BukkitCommandActor actor = context.actor();
               List<Entity> c = new ArrayList<>(Bukkit.getServer().selectEntities(actor.getSender(), selector));
               c.removeIf(obj -> !entityType.isInstance(obj));
               return new EntitySelectorResolver.EntitySelectorImpl<>(c);
            } catch (IllegalArgumentException var5) {
               throw new MalformedEntitySelectorException(context.actor(), selector, var5.getCause().getMessage());
            } catch (NoSuchMethodError var6) {
               throw new CommandErrorException("Entity selectors on legacy versions are not supported yet!");
            }
         };
      } else {
         return null;
      }
   }

   private EntitySelector<Player> resolvePlayerSelector(ValueResolver.ValueResolverContext context) {
      String selector = context.pop().toLowerCase();

      try {
         BukkitCommandActor bActor = context.actor();
         if (this.supportComplexSelectors) {
            List<Player> coll = Bukkit.getServer()
               .selectEntities(bActor.getSender(), selector)
               .stream()
               .filter(c -> c instanceof Player)
               .map(Player.class::cast)
               .collect(Collectors.toList());
            return new EntitySelectorResolver.EntitySelectorImpl(coll);
         } else {
            List<Player> coll = new ArrayList<>();
            Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
            switch (selector) {
               case "@r":
                  coll.add(players[ThreadLocalRandom.current().nextInt(players.length)]);
                  return new EntitySelectorResolver.EntitySelectorImpl(coll);
               case "@a":
                  Collections.addAll(coll, players);
                  return new EntitySelectorResolver.EntitySelectorImpl(coll);
               case "@s":
               case "@p":
                  coll.add(bActor.requirePlayer());
                  return new EntitySelectorResolver.EntitySelectorImpl(coll);
               default:
                  Player player = Bukkit.getPlayer(selector);
                  if (player == null) {
                     throw new InvalidPlayerException(context.parameter(), selector);
                  } else {
                     coll.add(player);
                     return new EntitySelectorResolver.EntitySelectorImpl(coll);
                  }
            }
         }
      } catch (IllegalArgumentException var9) {
         throw new MalformedEntitySelectorException(context.actor(), selector, var9.getCause().getMessage());
      }
   }

   public boolean supportsComplexSelectors() {
      return this.supportComplexSelectors;
   }

   private static class EntitySelectorImpl<E extends Entity> extends ForwardingList<E> implements EntitySelector<E> {
      private final List<E> entities;

      public EntitySelectorImpl(List<E> entities) {
         this.entities = entities;
      }

      protected List<E> delegate() {
         return this.entities;
      }

      @Override
      public boolean containsExactly(@NotNull Entity... entities) {
         Preconditions.notNull(entities, "entities");
         if (entities.length != this.entities.size()) {
            return false;
         } else {
            Set<Entity> set = new HashSet<>(entities.length);
            Collections.addAll(set, entities);
            return set.containsAll(this.entities);
         }
      }
   }

   public static enum SelectorSuggestionFactory implements SuggestionProviderFactory {
      INSTANCE;

      private boolean supportComplexSelectors;

      private SelectorSuggestionFactory() {
         try {
            Bukkit.getServer().selectEntities(Bukkit.getConsoleSender(), "@a");
            this.supportComplexSelectors = true;
         } catch (Throwable var4) {
            this.supportComplexSelectors = false;
         }
      }

      @Nullable
      @Override
      public SuggestionProvider createSuggestionProvider(@NotNull CommandParameter parameter) {
         if (parameter.getType().isAssignableFrom(EntitySelector.class)) {
            Class<? extends Entity> type = BukkitHandler.getSelectedEntity(parameter.getFullType());
            if (Player.class.isAssignableFrom(type) && !this.supportComplexSelectors) {
               return SuggestionProvider.of("@a", "@p", "@r", "@s").compose(parameter.getCommandHandler().getAutoCompleter().getSuggestionProvider("players"));
            }

            if (!EntitySelectorResolver.INSTANCE.supportComplexSelectors) {
               return SuggestionProvider.EMPTY;
            }
         }

         return null;
      }
   }
}
