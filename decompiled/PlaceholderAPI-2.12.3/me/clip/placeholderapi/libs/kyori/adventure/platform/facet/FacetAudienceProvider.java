package me.clip.placeholderapi.libs.kyori.adventure.platform.facet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import me.clip.placeholderapi.libs.kyori.adventure.audience.Audience;
import me.clip.placeholderapi.libs.kyori.adventure.audience.ForwardingAudience;
import me.clip.placeholderapi.libs.kyori.adventure.identity.Identity;
import me.clip.placeholderapi.libs.kyori.adventure.key.Key;
import me.clip.placeholderapi.libs.kyori.adventure.permission.PermissionChecker;
import me.clip.placeholderapi.libs.kyori.adventure.platform.AudienceProvider;
import me.clip.placeholderapi.libs.kyori.adventure.pointer.Pointered;
import me.clip.placeholderapi.libs.kyori.adventure.pointer.Pointers;
import me.clip.placeholderapi.libs.kyori.adventure.text.renderer.ComponentRenderer;
import me.clip.placeholderapi.libs.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public abstract class FacetAudienceProvider<V, A extends FacetAudience<V>> implements AudienceProvider, ForwardingAudience {
   protected static final Locale DEFAULT_LOCALE = Locale.US;
   protected final ComponentRenderer<Pointered> componentRenderer;
   private final Audience console;
   private final Audience player;
   protected final Map<V, A> viewers;
   private final Map<UUID, A> players;
   private final Set<A> consoles;
   private A empty;
   private volatile boolean closed;

   protected FacetAudienceProvider(@NotNull final ComponentRenderer<Pointered> componentRenderer) {
      this.componentRenderer = Objects.requireNonNull(componentRenderer, "component renderer");
      this.viewers = new ConcurrentHashMap<>();
      this.players = new ConcurrentHashMap<>();
      this.consoles = new CopyOnWriteArraySet<>();
      this.console = new ForwardingAudience() {
         @NotNull
         @Override
         public Iterable<? extends Audience> audiences() {
            return FacetAudienceProvider.this.consoles;
         }

         @NotNull
         @Override
         public Pointers pointers() {
            return FacetAudienceProvider.this.consoles.size() == 1 ? FacetAudienceProvider.this.consoles.iterator().next().pointers() : Pointers.empty();
         }
      };
      this.player = Audience.audience(this.players.values());
      this.closed = false;
   }

   public void addViewer(@NotNull final V viewer) {
      if (!this.closed) {
         A audience = this.viewers.computeIfAbsent(Objects.requireNonNull(viewer, "viewer"), v -> this.createAudience(Collections.singletonList((V)v)));
         FacetPointers.Type type = audience.getOrDefault(FacetPointers.TYPE, FacetPointers.Type.OTHER);
         if (type == FacetPointers.Type.PLAYER) {
            UUID id = audience.getOrDefault(Identity.UUID, null);
            if (id != null) {
               this.players.putIfAbsent(id, audience);
            }
         } else if (type == FacetPointers.Type.CONSOLE) {
            this.consoles.add(audience);
         }
      }
   }

   public void removeViewer(@NotNull final V viewer) {
      A audience = this.viewers.remove(viewer);
      if (audience != null) {
         FacetPointers.Type type = audience.getOrDefault(FacetPointers.TYPE, FacetPointers.Type.OTHER);
         if (type == FacetPointers.Type.PLAYER) {
            UUID id = audience.getOrDefault(Identity.UUID, null);
            if (id != null) {
               this.players.remove(id);
            }
         } else if (type == FacetPointers.Type.CONSOLE) {
            this.consoles.remove(audience);
         }

         audience.close();
      }
   }

   public void refreshViewer(@NotNull final V viewer) {
      A audience = this.viewers.get(viewer);
      if (audience != null) {
         audience.refresh();
      }
   }

   @NotNull
   protected abstract A createAudience(@NotNull final Collection<V> viewers);

   @NotNull
   @Override
   public Iterable<? extends Audience> audiences() {
      return this.viewers.values();
   }

   @NotNull
   @Override
   public Audience all() {
      return this;
   }

   @NotNull
   @Override
   public Audience console() {
      return this.console;
   }

   @NotNull
   @Override
   public Audience players() {
      return this.player;
   }

   @NotNull
   @Override
   public Audience player(@NotNull final UUID playerId) {
      return this.players.getOrDefault(playerId, this.empty());
   }

   @NotNull
   private A empty() {
      if (this.empty == null) {
         this.empty = this.createAudience(Collections.emptyList());
      }

      return this.empty;
   }

   @NotNull
   public Audience filter(@NotNull final Predicate<V> predicate) {
      return Audience.audience(filter(this.viewers.entrySet(), entry -> predicate.test(entry.getKey()), Entry::getValue));
   }

   @NotNull
   private Audience filterPointers(@NotNull final Predicate<Pointered> predicate) {
      return Audience.audience(filter(this.viewers.entrySet(), entry -> predicate.test(entry.getValue()), Entry::getValue));
   }

   @NotNull
   @Override
   public Audience permission(@NotNull final String permission) {
      return this.filterPointers(pointers -> pointers.get(PermissionChecker.POINTER).orElse(PermissionChecker.always(TriState.FALSE)).test(permission));
   }

   @NotNull
   @Override
   public Audience world(@NotNull final Key world) {
      return this.filterPointers(pointers -> world.equals(pointers.getOrDefault(FacetPointers.WORLD, null)));
   }

   @NotNull
   @Override
   public Audience server(@NotNull final String serverName) {
      return this.filterPointers(pointers -> serverName.equals(pointers.getOrDefault(FacetPointers.SERVER, null)));
   }

   @Override
   public void close() {
      this.closed = true;

      for (V viewer : this.viewers.keySet()) {
         this.removeViewer(viewer);
      }
   }

   @NotNull
   private static <T, V> Iterable<V> filter(@NotNull final Iterable<T> input, @NotNull final Predicate<T> filter, @NotNull final Function<T, V> transformer) {
      return new Iterable<V>() {
         @NotNull
         @Override
         public Iterator<V> iterator() {
            return new Iterator<V>() {
               private final Iterator<T> parent = input.iterator();
               private V next;

               {
                  this.populate();
               }

               private void populate() {
                  this.next = null;

                  while (this.parent.hasNext()) {
                     T next = this.parent.next();
                     if (filter.test(next)) {
                        this.next = transformer.apply(next);
                        return;
                     }
                  }
               }

               @Override
               public boolean hasNext() {
                  return this.next != null;
               }

               @Override
               public V next() {
                  if (this.next == null) {
                     throw new NoSuchElementException();
                  } else {
                     V next = this.next;
                     this.populate();
                     return next;
                  }
               }
            };
         }

         @Override
         public void forEach(final Consumer<? super V> action) {
            for (T each : input) {
               if (filter.test(each)) {
                  action.accept(transformer.apply(each));
               }
            }
         }
      };
   }
}
