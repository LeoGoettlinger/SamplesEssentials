package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import de.theredend2000.advancedhunt.lib.cloud.help.CommandPredicate;
import de.theredend2000.advancedhunt.lib.cloud.help.HelpHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import org.apiguardian.api.API;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@API(
   status = API.Status.STABLE,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
@Generated(
   from = "MinecraftHelp",
   generator = "Immutables"
)
public final class ImmutableMinecraftHelp<C> extends MinecraftHelp<C> {
   @NonNull
   private final CommandManager<C> commandManager;
   @NonNull
   private final AudienceProvider<C> audienceProvider;
   @NonNull
   private final String commandPrefix;
   @NonNull
   private final transient HelpHandler<C> helpHandler;
   @NonNull
   private final CommandPredicate<C> commandFilter;
   @NonNull
   private final MinecraftHelp.DescriptionDecorator<C> descriptionDecorator;
   @NonNull
   private final Map<String, String> messages;
   @NonNull
   private final MinecraftHelp.MessageProvider<C> messageProvider;
   @NonNull
   private final MinecraftHelp.HelpColors colors;
   @NonNegative
   private final int headerFooterLength;
   @NonNegative
   private final int maxResultsPerPage;
   private static final byte STAGE_INITIALIZING = -1;
   private static final byte STAGE_UNINITIALIZED = 0;
   private static final byte STAGE_INITIALIZED = 1;
   private transient volatile ImmutableMinecraftHelp<C>.InitShim initShim = new ImmutableMinecraftHelp.InitShim();

   private ImmutableMinecraftHelp(
      @NonNull CommandManager<C> commandManager,
      @NonNull AudienceProvider<C> audienceProvider,
      @NonNull String commandPrefix,
      @NonNull CommandPredicate<C> commandFilter,
      @NonNull MinecraftHelp.DescriptionDecorator<C> descriptionDecorator,
      Map<String, ? extends String> messages,
      @NonNull MinecraftHelp.MessageProvider<C> messageProvider,
      @NonNull MinecraftHelp.HelpColors colors,
      @NonNegative int headerFooterLength,
      @NonNegative int maxResultsPerPage
   ) {
      this.commandManager = Objects.requireNonNull(commandManager, "commandManager");
      this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
      this.commandPrefix = Objects.requireNonNull(commandPrefix, "commandPrefix");
      this.initShim.commandFilter(Objects.requireNonNull(commandFilter, "commandFilter"));
      this.initShim.descriptionDecorator(Objects.requireNonNull(descriptionDecorator, "descriptionDecorator"));
      this.messages = createUnmodifiableMap(true, false, messages);
      this.initShim.messageProvider(Objects.requireNonNull(messageProvider, "messageProvider"));
      this.initShim.colors(Objects.requireNonNull(colors, "colors"));
      this.initShim.headerFooterLength(headerFooterLength);
      this.initShim.maxResultsPerPage(maxResultsPerPage);
      this.helpHandler = this.initShim.helpHandler();
      this.commandFilter = this.initShim.commandFilter();
      this.descriptionDecorator = this.initShim.descriptionDecorator();
      this.messageProvider = this.initShim.messageProvider();
      this.colors = this.initShim.colors();
      this.headerFooterLength = this.initShim.headerFooterLength();
      this.maxResultsPerPage = this.initShim.maxResultsPerPage();
      this.initShim = null;
   }

   private ImmutableMinecraftHelp(ImmutableMinecraftHelp.Builder<C> builder) {
      this.commandManager = builder.commandManager;
      this.audienceProvider = builder.audienceProvider;
      this.commandPrefix = builder.commandPrefix;
      this.messages = createUnmodifiableMap(false, false, builder.messages);
      if (builder.commandFilterIsSet()) {
         this.initShim.commandFilter(builder.commandFilter);
      }

      if (builder.descriptionDecoratorIsSet()) {
         this.initShim.descriptionDecorator(builder.descriptionDecorator);
      }

      if (builder.messageProviderIsSet()) {
         this.initShim.messageProvider(builder.messageProvider);
      }

      if (builder.colorsIsSet()) {
         this.initShim.colors(builder.colors);
      }

      if (builder.headerFooterLengthIsSet()) {
         this.initShim.headerFooterLength(builder.headerFooterLength);
      }

      if (builder.maxResultsPerPageIsSet()) {
         this.initShim.maxResultsPerPage(builder.maxResultsPerPage);
      }

      this.helpHandler = this.initShim.helpHandler();
      this.commandFilter = this.initShim.commandFilter();
      this.descriptionDecorator = this.initShim.descriptionDecorator();
      this.messageProvider = this.initShim.messageProvider();
      this.colors = this.initShim.colors();
      this.headerFooterLength = this.initShim.headerFooterLength();
      this.maxResultsPerPage = this.initShim.maxResultsPerPage();
      this.initShim = null;
   }

   private ImmutableMinecraftHelp(
      ImmutableMinecraftHelp<C> original,
      @NonNull CommandManager<C> commandManager,
      @NonNull AudienceProvider<C> audienceProvider,
      @NonNull String commandPrefix,
      @NonNull CommandPredicate<C> commandFilter,
      @NonNull MinecraftHelp.DescriptionDecorator<C> descriptionDecorator,
      @NonNull Map<String, String> messages,
      @NonNull MinecraftHelp.MessageProvider<C> messageProvider,
      @NonNull MinecraftHelp.HelpColors colors,
      @NonNegative int headerFooterLength,
      @NonNegative int maxResultsPerPage
   ) {
      this.commandManager = commandManager;
      this.audienceProvider = audienceProvider;
      this.commandPrefix = commandPrefix;
      this.initShim.commandFilter(commandFilter);
      this.initShim.descriptionDecorator(descriptionDecorator);
      this.messages = messages;
      this.initShim.messageProvider(messageProvider);
      this.initShim.colors(colors);
      this.initShim.headerFooterLength(headerFooterLength);
      this.initShim.maxResultsPerPage(maxResultsPerPage);
      this.helpHandler = this.initShim.helpHandler();
      this.commandFilter = this.initShim.commandFilter();
      this.descriptionDecorator = this.initShim.descriptionDecorator();
      this.messageProvider = this.initShim.messageProvider();
      this.colors = this.initShim.colors();
      this.headerFooterLength = this.initShim.headerFooterLength();
      this.maxResultsPerPage = this.initShim.maxResultsPerPage();
      this.initShim = null;
   }

   @NonNull
   @Override
   public CommandManager<C> commandManager() {
      return this.commandManager;
   }

   @NonNull
   @Override
   public AudienceProvider<C> audienceProvider() {
      return this.audienceProvider;
   }

   @NonNull
   @Override
   public String commandPrefix() {
      return this.commandPrefix;
   }

   @NonNull
   @Override
   public HelpHandler<C> helpHandler() {
      ImmutableMinecraftHelp<C>.InitShim shim = this.initShim;
      return shim != null ? shim.helpHandler() : this.helpHandler;
   }

   @NonNull
   @Override
   public CommandPredicate<C> commandFilter() {
      ImmutableMinecraftHelp<C>.InitShim shim = this.initShim;
      return shim != null ? shim.commandFilter() : this.commandFilter;
   }

   @NonNull
   @Override
   public MinecraftHelp.DescriptionDecorator<C> descriptionDecorator() {
      ImmutableMinecraftHelp<C>.InitShim shim = this.initShim;
      return shim != null ? shim.descriptionDecorator() : this.descriptionDecorator;
   }

   @NonNull
   @Override
   public Map<String, String> messages() {
      return this.messages;
   }

   @NonNull
   @Override
   public MinecraftHelp.MessageProvider<C> messageProvider() {
      ImmutableMinecraftHelp<C>.InitShim shim = this.initShim;
      return shim != null ? shim.messageProvider() : this.messageProvider;
   }

   @NonNull
   @Override
   public MinecraftHelp.HelpColors colors() {
      ImmutableMinecraftHelp<C>.InitShim shim = this.initShim;
      return shim != null ? shim.colors() : this.colors;
   }

   @NonNegative
   @Override
   public int headerFooterLength() {
      ImmutableMinecraftHelp<C>.InitShim shim = this.initShim;
      return shim != null ? shim.headerFooterLength() : this.headerFooterLength;
   }

   @NonNegative
   @Override
   public int maxResultsPerPage() {
      ImmutableMinecraftHelp<C>.InitShim shim = this.initShim;
      return shim != null ? shim.maxResultsPerPage() : this.maxResultsPerPage;
   }

   public final ImmutableMinecraftHelp<C> withCommandManager(CommandManager<C> value) {
      if (this.commandManager == value) {
         return this;
      } else {
         CommandManager<C> newValue = Objects.requireNonNull(value, "commandManager");
         return new ImmutableMinecraftHelp<>(
            this,
            newValue,
            this.audienceProvider,
            this.commandPrefix,
            this.commandFilter,
            this.descriptionDecorator,
            this.messages,
            this.messageProvider,
            this.colors,
            this.headerFooterLength,
            this.maxResultsPerPage
         );
      }
   }

   public final ImmutableMinecraftHelp<C> withAudienceProvider(AudienceProvider<C> value) {
      if (this.audienceProvider == value) {
         return this;
      } else {
         AudienceProvider<C> newValue = Objects.requireNonNull(value, "audienceProvider");
         return new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            newValue,
            this.commandPrefix,
            this.commandFilter,
            this.descriptionDecorator,
            this.messages,
            this.messageProvider,
            this.colors,
            this.headerFooterLength,
            this.maxResultsPerPage
         );
      }
   }

   public final ImmutableMinecraftHelp<C> withCommandPrefix(String value) {
      String newValue = Objects.requireNonNull(value, "commandPrefix");
      return this.commandPrefix.equals(newValue)
         ? this
         : new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            this.audienceProvider,
            newValue,
            this.commandFilter,
            this.descriptionDecorator,
            this.messages,
            this.messageProvider,
            this.colors,
            this.headerFooterLength,
            this.maxResultsPerPage
         );
   }

   public final ImmutableMinecraftHelp<C> withCommandFilter(CommandPredicate<C> value) {
      if (this.commandFilter == value) {
         return this;
      } else {
         CommandPredicate<C> newValue = Objects.requireNonNull(value, "commandFilter");
         return new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            this.audienceProvider,
            this.commandPrefix,
            newValue,
            this.descriptionDecorator,
            this.messages,
            this.messageProvider,
            this.colors,
            this.headerFooterLength,
            this.maxResultsPerPage
         );
      }
   }

   public final ImmutableMinecraftHelp<C> withDescriptionDecorator(MinecraftHelp.DescriptionDecorator<C> value) {
      if (this.descriptionDecorator == value) {
         return this;
      } else {
         MinecraftHelp.DescriptionDecorator<C> newValue = Objects.requireNonNull(value, "descriptionDecorator");
         return new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            this.audienceProvider,
            this.commandPrefix,
            this.commandFilter,
            newValue,
            this.messages,
            this.messageProvider,
            this.colors,
            this.headerFooterLength,
            this.maxResultsPerPage
         );
      }
   }

   public final ImmutableMinecraftHelp<C> withMessages(Map<String, ? extends String> entries) {
      if (this.messages == entries) {
         return this;
      } else {
         Map<String, String> newValue = createUnmodifiableMap(true, false, entries);
         return new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            this.audienceProvider,
            this.commandPrefix,
            this.commandFilter,
            this.descriptionDecorator,
            newValue,
            this.messageProvider,
            this.colors,
            this.headerFooterLength,
            this.maxResultsPerPage
         );
      }
   }

   public final ImmutableMinecraftHelp<C> withMessageProvider(MinecraftHelp.MessageProvider<C> value) {
      if (this.messageProvider == value) {
         return this;
      } else {
         MinecraftHelp.MessageProvider<C> newValue = Objects.requireNonNull(value, "messageProvider");
         return new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            this.audienceProvider,
            this.commandPrefix,
            this.commandFilter,
            this.descriptionDecorator,
            this.messages,
            newValue,
            this.colors,
            this.headerFooterLength,
            this.maxResultsPerPage
         );
      }
   }

   public final ImmutableMinecraftHelp<C> withColors(MinecraftHelp.HelpColors value) {
      if (this.colors == value) {
         return this;
      } else {
         MinecraftHelp.HelpColors newValue = Objects.requireNonNull(value, "colors");
         return new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            this.audienceProvider,
            this.commandPrefix,
            this.commandFilter,
            this.descriptionDecorator,
            this.messages,
            this.messageProvider,
            newValue,
            this.headerFooterLength,
            this.maxResultsPerPage
         );
      }
   }

   public final ImmutableMinecraftHelp<C> withHeaderFooterLength(@NonNegative int value) {
      return this.headerFooterLength == value
         ? this
         : new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            this.audienceProvider,
            this.commandPrefix,
            this.commandFilter,
            this.descriptionDecorator,
            this.messages,
            this.messageProvider,
            this.colors,
            value,
            this.maxResultsPerPage
         );
   }

   public final ImmutableMinecraftHelp<C> withMaxResultsPerPage(@NonNegative int value) {
      return this.maxResultsPerPage == value
         ? this
         : new ImmutableMinecraftHelp<>(
            this,
            this.commandManager,
            this.audienceProvider,
            this.commandPrefix,
            this.commandFilter,
            this.descriptionDecorator,
            this.messages,
            this.messageProvider,
            this.colors,
            this.headerFooterLength,
            value
         );
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof ImmutableMinecraftHelp && this.equalsByValue((ImmutableMinecraftHelp<?>)another);
   }

   private boolean equalsByValue(ImmutableMinecraftHelp<?> another) {
      return this.commandManager.equals(another.commandManager)
         && this.audienceProvider.equals(another.audienceProvider)
         && this.commandPrefix.equals(another.commandPrefix)
         && this.helpHandler.equals(another.helpHandler)
         && this.commandFilter.equals(another.commandFilter)
         && this.descriptionDecorator.equals(another.descriptionDecorator)
         && this.messages.equals(another.messages)
         && this.messageProvider.equals(another.messageProvider)
         && this.colors.equals(another.colors)
         && this.headerFooterLength == another.headerFooterLength
         && this.maxResultsPerPage == another.maxResultsPerPage;
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.commandManager.hashCode();
      h += (h << 5) + this.audienceProvider.hashCode();
      h += (h << 5) + this.commandPrefix.hashCode();
      h += (h << 5) + this.helpHandler.hashCode();
      h += (h << 5) + this.commandFilter.hashCode();
      h += (h << 5) + this.descriptionDecorator.hashCode();
      h += (h << 5) + this.messages.hashCode();
      h += (h << 5) + this.messageProvider.hashCode();
      h += (h << 5) + this.colors.hashCode();
      h += (h << 5) + this.headerFooterLength;
      return h + (h << 5) + this.maxResultsPerPage;
   }

   @Override
   public String toString() {
      return "MinecraftHelp{commandManager="
         + this.commandManager
         + ", audienceProvider="
         + this.audienceProvider
         + ", commandPrefix="
         + this.commandPrefix
         + ", helpHandler="
         + this.helpHandler
         + ", commandFilter="
         + this.commandFilter
         + ", descriptionDecorator="
         + this.descriptionDecorator
         + ", messages="
         + this.messages
         + ", messageProvider="
         + this.messageProvider
         + ", colors="
         + this.colors
         + ", headerFooterLength="
         + this.headerFooterLength
         + ", maxResultsPerPage="
         + this.maxResultsPerPage
         + "}";
   }

   public static <C> ImmutableMinecraftHelp<C> of(
      @NonNull CommandManager<C> commandManager,
      @NonNull AudienceProvider<C> audienceProvider,
      @NonNull String commandPrefix,
      @NonNull CommandPredicate<C> commandFilter,
      @NonNull MinecraftHelp.DescriptionDecorator<C> descriptionDecorator,
      Map<String, ? extends String> messages,
      @NonNull MinecraftHelp.MessageProvider<C> messageProvider,
      @NonNull MinecraftHelp.HelpColors colors,
      @NonNegative int headerFooterLength,
      @NonNegative int maxResultsPerPage
   ) {
      return new ImmutableMinecraftHelp<>(
         commandManager,
         audienceProvider,
         commandPrefix,
         commandFilter,
         descriptionDecorator,
         messages,
         messageProvider,
         colors,
         headerFooterLength,
         maxResultsPerPage
      );
   }

   public static <C> ImmutableMinecraftHelp<C> copyOf(MinecraftHelp<C> instance) {
      return instance instanceof ImmutableMinecraftHelp
         ? (ImmutableMinecraftHelp)instance
         : ((ImmutableMinecraftHelp.Builder)builder())
            .commandManager(instance.commandManager())
            .audienceProvider(instance.audienceProvider())
            .commandPrefix(instance.commandPrefix())
            .commandFilter(instance.commandFilter())
            .descriptionDecorator(instance.descriptionDecorator())
            .messages(instance.messages())
            .messageProvider(instance.messageProvider())
            .colors(instance.colors())
            .headerFooterLength(instance.headerFooterLength())
            .maxResultsPerPage(instance.maxResultsPerPage())
            .build();
   }

   public static <C> ImmutableMinecraftHelp.CommandManagerBuildStage<C> builder() {
      return new ImmutableMinecraftHelp.Builder<>();
   }

   private static <K, V> Map<K, V> createUnmodifiableMap(boolean checkNulls, boolean skipNulls, Map<? extends K, ? extends V> map) {
      switch (map.size()) {
         case 0:
            return Collections.emptyMap();
         case 1:
            Entry<? extends K, ? extends V> e = map.entrySet().iterator().next();
            K k = (K)e.getKey();
            V v = (V)e.getValue();
            if (checkNulls) {
               Objects.requireNonNull(k, "key");
               Objects.requireNonNull(v, v == null ? "value for key: " + k : null);
            }

            if (!skipNulls || k != null && v != null) {
               return Collections.singletonMap(k, v);
            }

            return Collections.emptyMap();
         default:
            Map<K, V> linkedMap = new LinkedHashMap<>(map.size() * 4 / 3 + 1);
            if (!skipNulls && !checkNulls) {
               linkedMap.putAll(map);
            } else {
               for (Entry<? extends K, ? extends V> ex : map.entrySet()) {
                  K kx = (K)ex.getKey();
                  V vx = (V)ex.getValue();
                  if (skipNulls) {
                     if (kx == null || vx == null) {
                        continue;
                     }
                  } else if (checkNulls) {
                     Objects.requireNonNull(kx, "key");
                     Objects.requireNonNull(vx, vx == null ? "value for key: " + kx : null);
                  }

                  linkedMap.put(kx, vx);
               }
            }

            return Collections.unmodifiableMap(linkedMap);
      }
   }

   @Generated(
      from = "MinecraftHelp",
      generator = "Immutables"
   )
   public interface AudienceProviderBuildStage<C> {
      ImmutableMinecraftHelp.CommandPrefixBuildStage<C> audienceProvider(@NonNull AudienceProvider<C> audienceProvider);
   }

   @Generated(
      from = "MinecraftHelp",
      generator = "Immutables"
   )
   public interface BuildFinal<C> {
      ImmutableMinecraftHelp.BuildFinal<C> commandFilter(@NonNull CommandPredicate<C> commandFilter);

      ImmutableMinecraftHelp.BuildFinal<C> descriptionDecorator(@NonNull MinecraftHelp.DescriptionDecorator<C> descriptionDecorator);

      ImmutableMinecraftHelp.BuildFinal<C> messages(String key, String value);

      ImmutableMinecraftHelp.BuildFinal<C> messages(Entry<String, ? extends String> entry);

      ImmutableMinecraftHelp.BuildFinal<C> messages(Map<String, ? extends String> entries);

      ImmutableMinecraftHelp.BuildFinal<C> messageProvider(@NonNull MinecraftHelp.MessageProvider<C> messageProvider);

      ImmutableMinecraftHelp.BuildFinal<C> colors(@NonNull MinecraftHelp.HelpColors colors);

      ImmutableMinecraftHelp.BuildFinal<C> headerFooterLength(@NonNegative int headerFooterLength);

      ImmutableMinecraftHelp.BuildFinal<C> maxResultsPerPage(@NonNegative int maxResultsPerPage);

      ImmutableMinecraftHelp<C> build();
   }

   @Generated(
      from = "MinecraftHelp",
      generator = "Immutables"
   )
   public interface BuildStart<C> {
      ImmutableMinecraftHelp.AudienceProviderBuildStage<C> commandManager(@NonNull CommandManager<C> commandManager);
   }

   @Generated(
      from = "MinecraftHelp",
      generator = "Immutables"
   )
   public static final class Builder<C>
      implements ImmutableMinecraftHelp.CommandManagerBuildStage<C>,
      ImmutableMinecraftHelp.AudienceProviderBuildStage<C>,
      ImmutableMinecraftHelp.CommandPrefixBuildStage<C>,
      ImmutableMinecraftHelp.BuildFinal<C> {
      private static final long INIT_BIT_COMMAND_MANAGER = 1L;
      private static final long INIT_BIT_AUDIENCE_PROVIDER = 2L;
      private static final long INIT_BIT_COMMAND_PREFIX = 4L;
      private static final long OPT_BIT_COMMAND_FILTER = 1L;
      private static final long OPT_BIT_DESCRIPTION_DECORATOR = 2L;
      private static final long OPT_BIT_MESSAGE_PROVIDER = 4L;
      private static final long OPT_BIT_COLORS = 8L;
      private static final long OPT_BIT_HEADER_FOOTER_LENGTH = 16L;
      private static final long OPT_BIT_MAX_RESULTS_PER_PAGE = 32L;
      private long initBits = 7L;
      private long optBits;
      @NonNull
      private CommandManager<C> commandManager;
      @NonNull
      private AudienceProvider<C> audienceProvider;
      @NonNull
      private String commandPrefix;
      @NonNull
      private CommandPredicate<C> commandFilter;
      @NonNull
      private MinecraftHelp.DescriptionDecorator<C> descriptionDecorator;
      private final Map<String, String> messages = new LinkedHashMap<>();
      @NonNull
      private MinecraftHelp.MessageProvider<C> messageProvider;
      @NonNull
      private MinecraftHelp.HelpColors colors;
      @NonNegative
      private int headerFooterLength;
      @NonNegative
      private int maxResultsPerPage;

      private Builder() {
      }

      public final ImmutableMinecraftHelp.Builder<C> commandManager(@NonNull CommandManager<C> commandManager) {
         checkNotIsSet(this.commandManagerIsSet(), "commandManager");
         this.commandManager = Objects.requireNonNull(commandManager, "commandManager");
         this.initBits &= -2L;
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> audienceProvider(@NonNull AudienceProvider<C> audienceProvider) {
         checkNotIsSet(this.audienceProviderIsSet(), "audienceProvider");
         this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
         this.initBits &= -3L;
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> commandPrefix(@NonNull String commandPrefix) {
         checkNotIsSet(this.commandPrefixIsSet(), "commandPrefix");
         this.commandPrefix = Objects.requireNonNull(commandPrefix, "commandPrefix");
         this.initBits &= -5L;
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> commandFilter(@NonNull CommandPredicate<C> commandFilter) {
         checkNotIsSet(this.commandFilterIsSet(), "commandFilter");
         this.commandFilter = Objects.requireNonNull(commandFilter, "commandFilter");
         this.optBits |= 1L;
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> descriptionDecorator(@NonNull MinecraftHelp.DescriptionDecorator<C> descriptionDecorator) {
         checkNotIsSet(this.descriptionDecoratorIsSet(), "descriptionDecorator");
         this.descriptionDecorator = Objects.requireNonNull(descriptionDecorator, "descriptionDecorator");
         this.optBits |= 2L;
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> messages(String key, String value) {
         this.messages.put(Objects.requireNonNull(key, "messages key"), Objects.requireNonNull(value, value == null ? "messages value for key: " + key : null));
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> messages(Entry<String, ? extends String> entry) {
         String k = entry.getKey();
         String v = entry.getValue();
         this.messages.put(Objects.requireNonNull(k, "messages key"), Objects.requireNonNull(v, v == null ? "messages value for key: " + k : null));
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> messages(Map<String, ? extends String> entries) {
         for (Entry<String, ? extends String> e : entries.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            this.messages.put(Objects.requireNonNull(k, "messages key"), Objects.requireNonNull(v, v == null ? "messages value for key: " + k : null));
         }

         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> messageProvider(@NonNull MinecraftHelp.MessageProvider<C> messageProvider) {
         checkNotIsSet(this.messageProviderIsSet(), "messageProvider");
         this.messageProvider = Objects.requireNonNull(messageProvider, "messageProvider");
         this.optBits |= 4L;
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> colors(@NonNull MinecraftHelp.HelpColors colors) {
         checkNotIsSet(this.colorsIsSet(), "colors");
         this.colors = Objects.requireNonNull(colors, "colors");
         this.optBits |= 8L;
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> headerFooterLength(@NonNegative int headerFooterLength) {
         checkNotIsSet(this.headerFooterLengthIsSet(), "headerFooterLength");
         this.headerFooterLength = headerFooterLength;
         this.optBits |= 16L;
         return this;
      }

      public final ImmutableMinecraftHelp.Builder<C> maxResultsPerPage(@NonNegative int maxResultsPerPage) {
         checkNotIsSet(this.maxResultsPerPageIsSet(), "maxResultsPerPage");
         this.maxResultsPerPage = maxResultsPerPage;
         this.optBits |= 32L;
         return this;
      }

      @Override
      public ImmutableMinecraftHelp<C> build() {
         this.checkRequiredAttributes();
         return new ImmutableMinecraftHelp<>(this);
      }

      private boolean commandFilterIsSet() {
         return (this.optBits & 1L) != 0L;
      }

      private boolean descriptionDecoratorIsSet() {
         return (this.optBits & 2L) != 0L;
      }

      private boolean messageProviderIsSet() {
         return (this.optBits & 4L) != 0L;
      }

      private boolean colorsIsSet() {
         return (this.optBits & 8L) != 0L;
      }

      private boolean headerFooterLengthIsSet() {
         return (this.optBits & 16L) != 0L;
      }

      private boolean maxResultsPerPageIsSet() {
         return (this.optBits & 32L) != 0L;
      }

      private boolean commandManagerIsSet() {
         return (this.initBits & 1L) == 0L;
      }

      private boolean audienceProviderIsSet() {
         return (this.initBits & 2L) == 0L;
      }

      private boolean commandPrefixIsSet() {
         return (this.initBits & 4L) == 0L;
      }

      private static void checkNotIsSet(boolean isSet, String name) {
         if (isSet) {
            throw new IllegalStateException("Builder of MinecraftHelp is strict, attribute is already set: ".concat(name));
         }
      }

      private void checkRequiredAttributes() {
         if (this.initBits != 0L) {
            throw new IllegalStateException(this.formatRequiredAttributesMessage());
         }
      }

      private String formatRequiredAttributesMessage() {
         List<String> attributes = new ArrayList<>();
         if (!this.commandManagerIsSet()) {
            attributes.add("commandManager");
         }

         if (!this.audienceProviderIsSet()) {
            attributes.add("audienceProvider");
         }

         if (!this.commandPrefixIsSet()) {
            attributes.add("commandPrefix");
         }

         return "Cannot build MinecraftHelp, some of required attributes are not set " + attributes;
      }
   }

   @Generated(
      from = "MinecraftHelp",
      generator = "Immutables"
   )
   public interface CommandManagerBuildStage<C> extends ImmutableMinecraftHelp.BuildStart<C> {
   }

   @Generated(
      from = "MinecraftHelp",
      generator = "Immutables"
   )
   public interface CommandPrefixBuildStage<C> {
      ImmutableMinecraftHelp.BuildFinal<C> commandPrefix(@NonNull String commandPrefix);
   }

   @Generated(
      from = "MinecraftHelp",
      generator = "Immutables"
   )
   private final class InitShim {
      private byte helpHandlerBuildStage = 0;
      @NonNull
      private HelpHandler<C> helpHandler;
      private byte commandFilterBuildStage = 0;
      @NonNull
      private CommandPredicate<C> commandFilter;
      private byte descriptionDecoratorBuildStage = 0;
      @NonNull
      private MinecraftHelp.DescriptionDecorator<C> descriptionDecorator;
      private byte messageProviderBuildStage = 0;
      @NonNull
      private MinecraftHelp.MessageProvider<C> messageProvider;
      private byte colorsBuildStage = 0;
      @NonNull
      private MinecraftHelp.HelpColors colors;
      private byte headerFooterLengthBuildStage = 0;
      @NonNegative
      private int headerFooterLength;
      private byte maxResultsPerPageBuildStage = 0;
      @NonNegative
      private int maxResultsPerPage;

      private InitShim() {
      }

      HelpHandler<C> helpHandler() {
         if (this.helpHandlerBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.helpHandlerBuildStage == 0) {
               this.helpHandlerBuildStage = -1;
               HelpHandler<C> computedValue = ImmutableMinecraftHelp.super.helpHandler();
               this.helpHandler = Objects.requireNonNull(computedValue, "helpHandler");
               this.helpHandlerBuildStage = 1;
            }

            return this.helpHandler;
         }
      }

      CommandPredicate<C> commandFilter() {
         if (this.commandFilterBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.commandFilterBuildStage == 0) {
               this.commandFilterBuildStage = -1;
               CommandPredicate<C> computedValue = ImmutableMinecraftHelp.super.commandFilter();
               this.commandFilter = Objects.requireNonNull(computedValue, "commandFilter");
               this.commandFilterBuildStage = 1;
            }

            return this.commandFilter;
         }
      }

      void commandFilter(@NonNull CommandPredicate<C> commandFilter) {
         this.commandFilter = commandFilter;
         this.commandFilterBuildStage = 1;
      }

      MinecraftHelp.DescriptionDecorator<C> descriptionDecorator() {
         if (this.descriptionDecoratorBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.descriptionDecoratorBuildStage == 0) {
               this.descriptionDecoratorBuildStage = -1;
               MinecraftHelp.DescriptionDecorator<C> computedValue = ImmutableMinecraftHelp.super.descriptionDecorator();
               this.descriptionDecorator = Objects.requireNonNull(computedValue, "descriptionDecorator");
               this.descriptionDecoratorBuildStage = 1;
            }

            return this.descriptionDecorator;
         }
      }

      void descriptionDecorator(@NonNull MinecraftHelp.DescriptionDecorator<C> descriptionDecorator) {
         this.descriptionDecorator = descriptionDecorator;
         this.descriptionDecoratorBuildStage = 1;
      }

      MinecraftHelp.MessageProvider<C> messageProvider() {
         if (this.messageProviderBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.messageProviderBuildStage == 0) {
               this.messageProviderBuildStage = -1;
               MinecraftHelp.MessageProvider<C> computedValue = ImmutableMinecraftHelp.super.messageProvider();
               this.messageProvider = Objects.requireNonNull(computedValue, "messageProvider");
               this.messageProviderBuildStage = 1;
            }

            return this.messageProvider;
         }
      }

      void messageProvider(@NonNull MinecraftHelp.MessageProvider<C> messageProvider) {
         this.messageProvider = messageProvider;
         this.messageProviderBuildStage = 1;
      }

      MinecraftHelp.HelpColors colors() {
         if (this.colorsBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.colorsBuildStage == 0) {
               this.colorsBuildStage = -1;
               MinecraftHelp.HelpColors computedValue = ImmutableMinecraftHelp.super.colors();
               this.colors = Objects.requireNonNull(computedValue, "colors");
               this.colorsBuildStage = 1;
            }

            return this.colors;
         }
      }

      void colors(@NonNull MinecraftHelp.HelpColors colors) {
         this.colors = colors;
         this.colorsBuildStage = 1;
      }

      int headerFooterLength() {
         if (this.headerFooterLengthBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.headerFooterLengthBuildStage == 0) {
               this.headerFooterLengthBuildStage = -1;
               int computedValue = ImmutableMinecraftHelp.super.headerFooterLength();
               this.headerFooterLength = computedValue;
               this.headerFooterLengthBuildStage = 1;
            }

            return this.headerFooterLength;
         }
      }

      void headerFooterLength(@NonNegative int headerFooterLength) {
         this.headerFooterLength = headerFooterLength;
         this.headerFooterLengthBuildStage = 1;
      }

      int maxResultsPerPage() {
         if (this.maxResultsPerPageBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.maxResultsPerPageBuildStage == 0) {
               this.maxResultsPerPageBuildStage = -1;
               int computedValue = ImmutableMinecraftHelp.super.maxResultsPerPage();
               this.maxResultsPerPage = computedValue;
               this.maxResultsPerPageBuildStage = 1;
            }

            return this.maxResultsPerPage;
         }
      }

      void maxResultsPerPage(@NonNegative int maxResultsPerPage) {
         this.maxResultsPerPage = maxResultsPerPage;
         this.maxResultsPerPageBuildStage = 1;
      }

      private String formatInitCycleMessage() {
         List<String> attributes = new ArrayList<>();
         if (this.helpHandlerBuildStage == -1) {
            attributes.add("helpHandler");
         }

         if (this.commandFilterBuildStage == -1) {
            attributes.add("commandFilter");
         }

         if (this.descriptionDecoratorBuildStage == -1) {
            attributes.add("descriptionDecorator");
         }

         if (this.messageProviderBuildStage == -1) {
            attributes.add("messageProvider");
         }

         if (this.colorsBuildStage == -1) {
            attributes.add("colors");
         }

         if (this.headerFooterLengthBuildStage == -1) {
            attributes.add("headerFooterLength");
         }

         if (this.maxResultsPerPageBuildStage == -1) {
            attributes.add("maxResultsPerPage");
         }

         return "Cannot build MinecraftHelp, attribute initializers form cycle " + attributes;
      }
   }
}
