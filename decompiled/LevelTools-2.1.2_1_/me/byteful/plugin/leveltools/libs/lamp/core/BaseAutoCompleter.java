package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.AutoCompleter;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.SuggestionProvider;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.SuggestionProviderFactory;
import me.byteful.plugin.leveltools.libs.lamp.command.ArgumentStack;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandCategory;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import me.byteful.plugin.leveltools.libs.lamp.util.Collections;
import me.byteful.plugin.leveltools.libs.lamp.util.Preconditions;
import me.byteful.plugin.leveltools.libs.lamp.util.Primitives;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

final class BaseAutoCompleter implements AutoCompleter {
   private final BaseCommandHandler handler;
   final Map<String, SuggestionProvider> suggestionKeys = new HashMap<>();
   final List<SuggestionProviderFactory> factories = new ArrayList<>();
   private boolean filterToClosestInput = true;

   public BaseAutoCompleter(BaseCommandHandler handler) {
      this.handler = handler;
      this.registerSuggestion("nothing", SuggestionProvider.EMPTY);
      this.registerSuggestion("empty", SuggestionProvider.EMPTY);
      this.registerParameterSuggestions(boolean.class, SuggestionProvider.of("true", "false"));
      this.registerSuggestionFactory(new AutoCompleterAnnotationFactory(this.suggestionKeys));
      this.registerSuggestionFactory(EitherSuggestionProviderFactory.INSTANCE);
   }

   @Override
   public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull SuggestionProvider provider) {
      Preconditions.notNull(provider, "provider ID");
      Preconditions.notNull(provider, "tab suggestion provider");
      this.suggestionKeys.put(providerID, provider);
      return this;
   }

   @Override
   public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull Collection<String> completions) {
      Preconditions.notNull(providerID, "provider ID");
      Preconditions.notNull(completions, "completions");
      this.suggestionKeys.put(providerID, (args, sender, command) -> completions);
      return this;
   }

   @Override
   public AutoCompleter registerSuggestion(@NotNull String providerID, @NotNull String... completions) {
      this.registerSuggestion(providerID, Collections.listOf(completions));
      return this;
   }

   @Override
   public AutoCompleter registerParameterSuggestions(@NotNull Class<?> parameterType, @NotNull SuggestionProvider provider) {
      Preconditions.notNull(parameterType, "parameter type");
      Preconditions.notNull(provider, "provider");
      this.registerSuggestionFactory(SuggestionProviderFactory.forType(parameterType, provider));
      Class<?> wrapped = Primitives.wrap(parameterType);
      if (wrapped != parameterType) {
         this.registerSuggestionFactory(SuggestionProviderFactory.forType(wrapped, provider));
      }

      return this;
   }

   @Override
   public AutoCompleter registerParameterSuggestions(@NotNull Class<?> parameterType, @NotNull String providerID) {
      Preconditions.notNull(parameterType, "parameter type");
      Preconditions.notNull(providerID, "provider ID");
      SuggestionProvider provider = this.suggestionKeys.get(providerID);
      if (provider == null) {
         throw new IllegalArgumentException("No such tab provider: " + providerID + ". Available: " + this.suggestionKeys.keySet());
      } else {
         this.registerParameterSuggestions(parameterType, provider);
         return this;
      }
   }

   @Override
   public AutoCompleter registerSuggestionFactory(@NotNull SuggestionProviderFactory factory) {
      Preconditions.notNull(factory, "suggestion provider factory cannot be null!");
      this.factories.add(factory);
      return this;
   }

   @Override
   public AutoCompleter registerSuggestionFactory(int priority, @NotNull SuggestionProviderFactory factory) {
      Preconditions.notNull(factory, "suggestion provider factory cannot be null!");
      this.factories.add(Preconditions.coerceIn(priority, 0, this.factories.size()), factory);
      return this;
   }

   public SuggestionProvider getProvider(CommandParameter parameter) {
      if (parameter.isSwitch()) {
         return SuggestionProvider.of(this.handler.switchPrefix + parameter.getSwitchName());
      } else {
         for (SuggestionProviderFactory factory : this.factories) {
            SuggestionProvider provider = factory.createSuggestionProvider(parameter);
            if (provider != null) {
               return provider;
            }
         }

         return parameter.getType().isEnum() ? EnumSuggestionProviderFactory.INSTANCE.createSuggestionProvider(parameter) : SuggestionProvider.EMPTY;
      }
   }

   @Override
   public SuggestionProvider getSuggestionProvider(@NotNull String id) {
      return this.suggestionKeys.get(id);
   }

   @Override
   public List<String> complete(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
      CommandPath path = CommandPath.get(arguments.subList(0, arguments.size() - 1));
      int originalSize = arguments.size();
      ExecutableCommand command = this.searchForCommand(path, actor);
      if (command != null) {
         command.getPath().forEach(c -> {
            String var10000 = arguments.removeFirst();
         });
         return this.getCompletions(actor, arguments, command);
      } else {
         CommandCategory category = this.getLastCategory(path);
         if (category == null) {
            return java.util.Collections.emptyList();
         } else {
            category.getPath().forEach(c -> {
               String var10000 = arguments.removeFirst();
            });
            return this.getCompletions(actor, arguments, category, originalSize);
         }
      }
   }

   @Override
   public List<String> complete(@NotNull CommandActor actor, @NotNull String buffer) {
      return this.complete(actor, ArgumentStack.parseForAutoCompletion(buffer));
   }

   @Override
   public void filterToClosestInput(boolean filterToClosestInput) {
      this.filterToClosestInput = filterToClosestInput;
   }

   private ExecutableCommand searchForCommand(CommandPath path, CommandActor actor) {
      ExecutableCommand found = this.handler.getCommand(path);
      if (found != null && !found.isSecret() && found.getPermission().canExecute(actor)) {
         return found;
      } else {
         MutableCommandPath mpath = MutableCommandPath.empty();

         for (String p : path) {
            mpath.add(p);
            found = this.handler.getCommand(mpath);
            if (found != null && !found.isSecret() && found.getPermission().canExecute(actor)) {
               return found;
            }
         }

         return null;
      }
   }

   private CommandCategory getLastCategory(CommandPath path) {
      MutableCommandPath mpath = MutableCommandPath.empty();
      CommandCategory category = null;

      for (String p : path) {
         mpath.add(p);
         CommandCategory c = this.handler.getCategory(mpath);
         if (c == null && category != null) {
            return category;
         }

         if (c != null) {
            category = c;
         }
      }

      return category;
   }

   private List<String> getCompletions(CommandActor actor, ArgumentStack args, @NotNull ExecutableCommand command) {
      try {
         try {
            if (args.isEmpty()) {
               return java.util.Collections.emptyList();
            } else if (command.getValueParameters().isEmpty()) {
               return java.util.Collections.emptyList();
            } else {
               List<CommandParameter> parameters = new ArrayList<>(command.getValueParameters().values());
               java.util.Collections.sort(parameters);

               for (CommandParameter parameter : parameters) {
                  try {
                     if (!parameter.isFlag() && parameter.getCommandIndex() == args.size() - 1) {
                        if (!parameter.getPermission().canExecute(actor)) {
                           return java.util.Collections.emptyList();
                        }

                        SuggestionProvider provider = parameter.getSuggestionProvider();
                        Preconditions.notNull(provider, "provider must not be null!");
                        return this.getParamCompletions(provider.getSuggestions(args, actor, command), args);
                     }
                  } catch (Throwable var9) {
                  }
               }

               parameters.removeIf(c -> !c.isFlag());
               if (parameters.isEmpty()) {
                  return java.util.Collections.emptyList();
               } else {
                  Optional<CommandParameter> currentFlag = parameters.stream().filter(c -> {
                     int indexx = args.indexOf(this.handler.getFlagPrefix() + c.getFlagName());
                     return indexx == args.size() - 2;
                  }).findFirst();
                  if (currentFlag.isPresent()) {
                     SuggestionProvider provider = currentFlag.get().getSuggestionProvider();
                     return this.getParamCompletions(provider.getSuggestions(args, actor, command), args);
                  } else {
                     for (CommandParameter flag : parameters) {
                        int index = args.indexOf(this.handler.getFlagPrefix() + flag.getFlagName());
                        if (index == -1) {
                           return Collections.listOf(this.handler.getFlagPrefix() + flag.getFlagName());
                        }

                        if (index == args.size() - 2) {
                           return this.getParamCompletions(flag.getSuggestionProvider().getSuggestions(args, actor, command), args);
                        }
                     }

                     return java.util.Collections.emptyList();
                  }
               }
            }
         } catch (IndexOutOfBoundsException var10) {
            return java.util.Collections.emptyList();
         }
      } catch (Throwable var11) {
         throw var11;
      }
   }

   @NotNull
   private List<String> getParamCompletions(Collection<String> provider, ArgumentStack args) {
      return provider.stream()
         .filter(c -> !this.filterToClosestInput || c.toLowerCase().startsWith(args.getLast().toLowerCase()))
         .sorted(String.CASE_INSENSITIVE_ORDER)
         .distinct()
         .collect(Collectors.toList());
   }

   private List<String> getCompletions(CommandActor actor, @Unmodifiable ArgumentStack args, CommandCategory category, int originalSize) {
      if (args.isEmpty()) {
         return java.util.Collections.emptyList();
      } else {
         Set<String> suggestions = new HashSet<>();
         if (category.getDefaultAction() != null) {
            ExecutableCommand defaultAction = category.getDefaultAction();
            if (!defaultAction.isSecret() && defaultAction.getPermission().canExecute(actor)) {
               suggestions.addAll(this.getCompletions(actor, args, defaultAction));
            }
         }

         if (originalSize - category.getPath().size() == 1) {
            category.getCommands().values().forEach(c -> {
               if (!c.isSecret() && c.getPermission().canExecute(actor)) {
                  suggestions.add(c.getName());
               }
            });
            category.getCategories().values().forEach(c -> {
               if (!c.isSecret() && c.getPermission().canExecute(actor)) {
                  suggestions.add(c.getName());
               }
            });
         }

         return this.getParamCompletions(suggestions, args);
      }
   }

   @Override
   public CommandHandler and() {
      return this.handler;
   }
}
