package vrx.economy.vRXClans.libs.cloud.context;

import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.Command;
import vrx.economy.vRXClans.libs.cloud.CommandManager;
import vrx.economy.vRXClans.libs.cloud.caption.Caption;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionFormatter;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionRegistry;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.key.CloudKey;
import vrx.economy.vRXClans.libs.cloud.key.MutableCloudKeyContainer;
import vrx.economy.vRXClans.libs.cloud.parser.flag.FlagContext;
import vrx.economy.vRXClans.libs.cloud.permission.Permission;
import vrx.economy.vRXClans.libs.cloud.util.annotation.AnnotationAccessor;

@API(
   status = API.Status.STABLE
)
public class CommandContext<C> implements MutableCloudKeyContainer {
   private final List<ParsingContext<C>> parsingContexts = new LinkedList<>();
   private final FlagContext flagContext = FlagContext.create();
   private final Map<CloudKey<?>, Object> internalStorage = new HashMap<>();
   private final C commandSender;
   private final boolean suggestions;
   private final CaptionRegistry<C> captionRegistry;
   private final CommandManager<C> commandManager;
   @MonotonicNonNull
   private volatile Command<C> currentCommand = null;

   @API(
      status = API.Status.STABLE
   )
   public CommandContext(@NonNull final C commandSender, @NonNull final CommandManager<C> commandManager) {
      this(false, commandSender, commandManager);
   }

   @API(
      status = API.Status.STABLE
   )
   public CommandContext(final boolean suggestions, @NonNull final C commandSender, @NonNull final CommandManager<C> commandManager) {
      this.commandSender = commandSender;
      this.suggestions = suggestions;
      this.commandManager = commandManager;
      this.captionRegistry = commandManager.captionRegistry();
   }

   @NonNull
   public String formatCaption(@NonNull final Caption caption, @NonNull final CaptionVariable... variables) {
      return this.formatCaption(this.commandManager.captionFormatter(), caption, variables);
   }

   @NonNull
   public String formatCaption(@NonNull final Caption caption, @NonNull final List<CaptionVariable> variables) {
      return this.formatCaption(this.commandManager.captionFormatter(), caption, variables);
   }

   @NonNull
   public <T> T formatCaption(@NonNull final CaptionFormatter<C, T> formatter, @NonNull final Caption caption, @NonNull final CaptionVariable... variables) {
      return formatter.formatCaption(caption, this.commandSender, this.captionRegistry.caption(caption, this.commandSender), variables);
   }

   @NonNull
   public <T> T formatCaption(@NonNull final CaptionFormatter<C, T> formatter, @NonNull final Caption caption, @NonNull final List<CaptionVariable> variables) {
      return formatter.formatCaption(caption, this.commandSender, this.captionRegistry.caption(caption, this.commandSender), variables);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public C sender() {
      return this.commandSender;
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean hasPermission(@NonNull final Permission permission) {
      return this.commandManager.testPermission(this.commandSender, permission).allowed();
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean hasPermission(@NonNull final String permission) {
      return this.commandManager.hasPermission(this.commandSender, permission);
   }

   public boolean isSuggestions() {
      return this.suggestions;
   }

   @Override
   public <T> void store(@NonNull final String key, final T value) {
      this.internalStorage.put(CloudKey.of(key), value);
   }

   @Override
   public <T> void store(@NonNull final CloudKey<T> key, final T value) {
      this.internalStorage.put(key, value);
   }

   @Override
   public boolean contains(@NonNull final CloudKey<?> key) {
      return this.internalStorage.containsKey(key);
   }

   @NonNull
   @Override
   public <T> Optional<T> optional(@NonNull final CloudKey<T> key) {
      Object value = this.internalStorage.get(key);
      return value != null ? Optional.of((T)value) : Optional.empty();
   }

   @NonNull
   @Override
   public <T> Optional<T> optional(@NonNull final String key) {
      Object value = this.internalStorage.get(CloudKey.of(key));
      return value != null ? Optional.of((T)value) : Optional.empty();
   }

   @Override
   public void remove(@NonNull final CloudKey<?> key) {
      this.internalStorage.remove(key);
   }

   @Override
   public <T> T computeIfAbsent(@NonNull final CloudKey<T> key, @NonNull final Function<CloudKey<T>, T> defaultFunction) {
      return (T)this.internalStorage.computeIfAbsent(key, k -> defaultFunction.apply((CloudKey<T>)k));
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public CommandInput rawInput() {
      return this.getOrDefault("__raw_input__", CommandInput.empty()).copy();
   }

   @API(
      status = API.Status.MAINTAINED
   )
   @NonNull
   public ParsingContext<C> createParsingContext(@NonNull final CommandComponent<C> component) {
      ParsingContext<C> parsingContext = new ParsingContext<>(component);
      this.parsingContexts.add(parsingContext);
      return parsingContext;
   }

   @API(
      status = API.Status.MAINTAINED
   )
   @NonNull
   public ParsingContext<C> parsingContext(@NonNull final CommandComponent<C> component) {
      return this.parsingContexts.stream().filter(context -> context.component().equals(component)).findFirst().orElseThrow(NoSuchElementException::new);
   }

   @API(
      status = API.Status.MAINTAINED
   )
   @NonNull
   public ParsingContext<C> parsingContext(final int position) {
      return this.parsingContexts.get(position);
   }

   @API(
      status = API.Status.MAINTAINED
   )
   @NonNull
   public ParsingContext<C> parsingContext(final String name) {
      return this.parsingContexts.stream().filter(context -> context.component().name().equals(name)).findFirst().orElseThrow(NoSuchElementException::new);
   }

   @API(
      status = API.Status.MAINTAINED
   )
   @NonNull
   public List<ParsingContext<C>> parsingContexts() {
      return Collections.unmodifiableList(this.parsingContexts);
   }

   @NonNull
   public FlagContext flags() {
      return this.flagContext;
   }

   @NonNull
   public Command<C> command() {
      if (this.currentCommand == null) {
         throw new IllegalStateException(
            "The current command is only available once a command has been parsed. Mainly from execution handlers and post processors."
         );
      } else {
         return this.currentCommand;
      }
   }

   @API(
      status = API.Status.INTERNAL
   )
   public void command(@NonNull final Command<C> command) {
      this.currentCommand = Objects.requireNonNull(command, "command");
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public <T> Optional<T> inject(@NonNull final Class<T> clazz) {
      if (this.commandManager == null) {
         throw new UnsupportedOperationException("Cannot retrieve injectable values from a command context that is not associated with a command manager");
      } else {
         return this.commandManager.parameterInjectorRegistry().getInjectable(clazz, this, AnnotationAccessor.empty());
      }
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public <T> Optional<T> inject(@NonNull final TypeToken<T> type) {
      if (this.commandManager == null) {
         throw new UnsupportedOperationException("Cannot retrieve injectable values from a command context that is not associated with a command manager");
      } else {
         return this.commandManager.parameterInjectorRegistry().getInjectable(type, this, AnnotationAccessor.empty());
      }
   }

   @NonNull
   @Override
   public final Map<CloudKey<?>, ? extends Object> all() {
      return Collections.unmodifiableMap(this.internalStorage);
   }
}
