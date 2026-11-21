package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.lang.invoke.StringConcatFactory;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SequencedCollection;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.structure.StructureInfo;

@Deprecated(
   since = "2.14",
   forRemoval = true
)
public sealed class SkriptEventInfo<E extends SkriptEvent> extends StructureInfo<E> permits SkriptEventInfo.ModernSkriptEventInfo {
   public Class<? extends Event>[] events;
   public final String name;
   private SkriptEvent.ListeningBehavior listeningBehavior;
   @Nullable
   private String documentationID = null;
   @Nullable
   private String[] since = null;
   @Nullable
   private String[] description = null;
   @Nullable
   private String[] examples = null;
   @Nullable
   private String[] keywords = null;
   @Nullable
   private String[] requiredPlugins = null;
   private final String id;
   public static final String[] NO_DOC = new String[0];

   public SkriptEventInfo(String name, String[] patterns, Class<E> eventClass, String originClassPath, Class<? extends Event>[] events) {
      super(patterns, eventClass, originClassPath);
      validateEvents(name, eventClass, events);
      this.events = events;
      if (name.startsWith("*")) {
         this.name = name = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(name.substring(1));
      } else {
         this.name = "On " + name;
      }

      this.id = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(
         name.toLowerCase(Locale.ENGLISH).replaceAll("[#'\"<>/&]", "").replaceAll("\\s+", "_")
      );
      this.listeningBehavior = SkriptConfig.listenCancelledByDefault.value() ? SkriptEvent.ListeningBehavior.ANY : SkriptEvent.ListeningBehavior.UNCANCELLED;
   }

   @Internal
   protected SkriptEventInfo(BukkitSyntaxInfos.Event<E> source) {
      super(source);
      this.events = source.events().toArray(new Class[0]);
      this.name = source.name();
      validateEvents(this.name, source.type(), this.events);
      this.id = source.id();
      if (source.documentationId() != null) {
         this.documentationID(source.documentationId());
      }

      this.listeningBehavior(source.listeningBehavior())
         .since(source.since().toArray(new String[0]))
         .description(source.description().toArray(new String[0]))
         .examples(source.examples().toArray(new String[0]))
         .keywords(source.keywords().toArray(new String[0]))
         .requiredPlugins(source.requiredPlugins().toArray(new String[0]));
   }

   private static void validateEvents(String name, Class<? extends SkriptEvent> eventClass, Class<? extends Event>[] events) {
      for (int i = 0; i < events.length; i++) {
         for (int j = i + 1; j < events.length; j++) {
            if ((events[i].isAssignableFrom(events[j]) || events[j].isAssignableFrom(events[i]))
               && !events[i].equals(PlayerInteractAtEntityEvent.class)
               && !events[j].equals(PlayerInteractAtEntityEvent.class)) {
               throw new SkriptAPIException(
                  "The event "
                     + name
                     + " ("
                     + eventClass.getName()
                     + ") registers with super/subclasses "
                     + events[i].getName()
                     + " and "
                     + events[j].getName()
               );
            }
         }
      }
   }

   public SkriptEventInfo<E> listeningBehavior(SkriptEvent.ListeningBehavior listeningBehavior) {
      this.listeningBehavior = listeningBehavior;
      return this;
   }

   public SkriptEventInfo<E> description(String... description) {
      this.description = description;
      return this;
   }

   public SkriptEventInfo<E> examples(String... examples) {
      this.examples = examples;
      return this;
   }

   public SkriptEventInfo<E> keywords(String... keywords) {
      this.keywords = keywords;
      return this;
   }

   public SkriptEventInfo<E> since(String since) {
      return this.since(since);
   }

   public SkriptEventInfo<E> since(String... since) {
      assert this.since == null;

      this.since = since;
      return this;
   }

   public SkriptEventInfo<E> documentationID(String id) {
      assert this.documentationID == null;

      this.documentationID = id;
      return this;
   }

   public SkriptEventInfo<E> requiredPlugins(String... pluginNames) {
      this.requiredPlugins = pluginNames;
      return this;
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public SkriptEvent.ListeningBehavior getListeningBehavior() {
      return this.listeningBehavior;
   }

   @Nullable
   public String[] getDescription() {
      return this.description;
   }

   @Nullable
   public String[] getExamples() {
      return this.examples;
   }

   @Nullable
   public String[] getKeywords() {
      return this.keywords;
   }

   @Nullable
   public String[] getSince() {
      return this.since;
   }

   @Nullable
   public String[] getRequiredPlugins() {
      return this.requiredPlugins;
   }

   @Nullable
   public String getDocumentationID() {
      return this.documentationID;
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Internal
   public static final class ModernSkriptEventInfo<E extends SkriptEvent> extends SkriptEventInfo<E> implements BukkitSyntaxInfos.Event<E> {
      private final Origin origin;

      public ModernSkriptEventInfo(String name, String[] patterns, Class<E> eventClass, String originClassPath, Class<? extends Event>[] events) {
         super(name, patterns, eventClass, originClassPath, events);
         this.origin = Skript.getSyntaxOrigin(eventClass);
      }

      @Override
      public BukkitSyntaxInfos.Event.Builder<? extends BukkitSyntaxInfos.Event.Builder<?, E>, E> toBuilder() {
         return (BukkitSyntaxInfos.Event.Builder<? extends BukkitSyntaxInfos.Event.Builder<?, E>, E>)BukkitSyntaxInfos.Event.builder(
               this.type(), "*" + this.name()
            )
            .origin(this.origin)
            .addPatterns(this.patterns())
            .priority(this.priority())
            .listeningBehavior(this.listeningBehavior())
            .addSince(this.since())
            .documentationId(this.id())
            .addDescription(this.description())
            .addExamples(this.examples())
            .addKeywords(this.keywords())
            .addRequiredPlugins(this.requiredPlugins())
            .addEvents(this.events());
      }

      @Override
      public Origin origin() {
         return this.origin;
      }

      @Override
      public SkriptEvent.ListeningBehavior listeningBehavior() {
         return this.getListeningBehavior();
      }

      @Override
      public String name() {
         return this.getName();
      }

      @Override
      public String id() {
         return this.getId();
      }

      @Nullable
      @Override
      public String documentationId() {
         return this.getDocumentationID();
      }

      @Override
      public SequencedCollection<String> since() {
         String[] since = this.getSince();
         return since != null ? List.of(since) : List.of();
      }

      @Override
      public SequencedCollection<String> description() {
         String[] description = this.getDescription();
         return description != null ? List.of(description) : List.of();
      }

      @Override
      public Collection<String> examples() {
         String[] examples = this.getExamples();
         if (examples != null && examples.length != 0) {
            com.google.common.collect.ImmutableList.Builder<String> builder = ImmutableList.builder();
            StringBuilder currentExample = new StringBuilder();

            for (String example : examples) {
               if (!example.startsWith("\t")) {
                  String nextExample = currentExample.toString();
                  if (!nextExample.isBlank()) {
                     builder.add(nextExample);
                  }

                  currentExample = new StringBuilder();
                  if (example.contains("\n")) {
                     builder.add(example);
                     continue;
                  }
               }

               currentExample.append(example).append("\n");
            }

            if (!currentExample.isEmpty()) {
               builder.add(currentExample.toString());
            }

            return builder.build();
         } else {
            return List.of();
         }
      }

      @Override
      public Collection<String> keywords() {
         String[] keywords = this.getKeywords();
         return keywords != null ? List.of(keywords) : List.of();
      }

      @Override
      public Collection<String> requiredPlugins() {
         String[] requiredPlugins = this.getRequiredPlugins();
         return requiredPlugins != null ? List.of(requiredPlugins) : List.of();
      }

      @Override
      public Collection<Class<? extends Event>> events() {
         return List.of(this.events);
      }
   }
}
