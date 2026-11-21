package org.skriptlang.skript.bukkit.registration;

import ch.njol.skript.lang.SkriptEvent;
import java.util.Collection;
import java.util.SequencedCollection;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public final class BukkitSyntaxInfos {
   private BukkitSyntaxInfos() {
   }

   public static String fixPattern(String pattern) {
      char[] chars = pattern.toCharArray();
      StringBuilder stringBuilder = new StringBuilder();
      boolean inType = false;

      for (int i = 0; i < chars.length; i++) {
         char character = chars[i];
         stringBuilder.append(character);
         if (character == '%') {
            inType = !inType;
            if (inType && i + 2 < chars.length && chars[i + 1] != '-' && chars[i + 2] != '-') {
               stringBuilder.append('-');
            }
         } else if (character == '\\' && i + 1 < chars.length) {
            stringBuilder.append(chars[i + 1]);
            i++;
         }
      }

      return stringBuilder.toString();
   }

   public interface Event<E extends SkriptEvent> extends SyntaxInfo<E> {
      SyntaxRegistry.Key<BukkitSyntaxInfos.Event<?>> KEY = SyntaxRegistry.Key.of("event");

      @Contract("_, _, _, _, _ -> new")
      static <E extends SkriptEvent> BukkitSyntaxInfos.Event<E> simple(
         Class<E> eventClass, Supplier<E> instanceSupplier, String name, Class<? extends org.bukkit.event.Event> bukkitEventClass, String... patterns
      ) {
         return builder(eventClass, name).supplier(instanceSupplier).addEvent(bukkitEventClass).addPatterns(patterns).build();
      }

      static <E extends SkriptEvent> BukkitSyntaxInfos.Event.Builder<? extends BukkitSyntaxInfos.Event.Builder<?, E>, E> builder(
         Class<E> eventClass, String name
      ) {
         return (BukkitSyntaxInfos.Event.Builder<? extends BukkitSyntaxInfos.Event.Builder<?, E>, E>)(new BukkitSyntaxInfosImpl.EventImpl.BuilderImpl<>(
            eventClass, name
         ));
      }

      @Contract("-> new")
      BukkitSyntaxInfos.Event.Builder<? extends BukkitSyntaxInfos.Event.Builder<?, E>, E> toBuilder();

      SkriptEvent.ListeningBehavior listeningBehavior();

      String name();

      String id();

      @Nullable
      String documentationId();

      SequencedCollection<String> since();

      SequencedCollection<String> description();

      Collection<String> examples();

      Collection<String> keywords();

      Collection<String> requiredPlugins();

      Collection<Class<? extends org.bukkit.event.Event>> events();

      public interface Builder<B extends BukkitSyntaxInfos.Event.Builder<B, E>, E extends SkriptEvent> extends SyntaxInfo.Builder<B, E> {
         @Contract("_ -> this")
         B listeningBehavior(SkriptEvent.ListeningBehavior var1);

         @Contract("_ -> this")
         B documentationId(String var1);

         @Contract("_ -> this")
         B addSince(String var1);

         @Contract("_ -> this")
         B addSince(String... var1);

         @Contract("_ -> this")
         B addSince(Collection<String> var1);

         @Contract("-> this")
         B clearSince();

         @Contract("_ -> this")
         B addDescription(String var1);

         @Contract("_ -> this")
         B addDescription(String... var1);

         @Contract("_ -> this")
         B addDescription(Collection<String> var1);

         @Contract("-> this")
         B clearDescription();

         @Contract("_ -> this")
         B addExample(String var1);

         @Contract("_ -> this")
         B addExamples(String... var1);

         @Contract("_ -> this")
         B addExamples(Collection<String> var1);

         @Contract("-> this")
         B clearExamples();

         @Contract("_ -> this")
         B addKeyword(String var1);

         @Contract("_ -> this")
         B addKeywords(String... var1);

         @Contract("_ -> this")
         B addKeywords(Collection<String> var1);

         @Contract("-> this")
         B clearKeywords();

         @Contract("_ -> this")
         B addRequiredPlugin(String var1);

         @Contract("_ -> this")
         B addRequiredPlugins(String... var1);

         @Contract("_ -> this")
         B addRequiredPlugins(Collection<String> var1);

         B clearRequiredPlugins();

         @Contract("_ -> this")
         B addEvent(Class<? extends org.bukkit.event.Event> var1);

         @Contract("_ -> this")
         B addEvents(Class<? extends org.bukkit.event.Event>[] var1);

         @Contract("_ -> this")
         B addEvents(Collection<Class<? extends org.bukkit.event.Event>> var1);

         @Contract("-> this")
         B clearEvents();

         @Contract("-> new")
         BukkitSyntaxInfos.Event<E> build();
      }
   }
}
