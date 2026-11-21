package org.enginehub.piston.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.reflect.TypeToken;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.converter.SuggestionHelper;
import org.enginehub.piston.exception.NoSuchCommandException;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MemoizingValueAccess;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.suggestion.Suggestion;

public class CommandManagerImpl implements CommandManager {
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private final Map<String, Command> commands = new HashMap<>();
   private final Map<Key<?>, ArgumentConverter<?>> converters = new HashMap<>();
   private final CommandInfoCache commandInfoCache = new CommandInfoCache();

   public CommandManagerImpl() {
      this.registerConverter(Key.of(String.class), ArgumentConverters.forString());
      UnmodifiableIterator var1 = ImmutableList.of(
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class, Boolean.class
         )
         .iterator();

      while (var1.hasNext()) {
         Class<?> wrapperType = (Class<?>)var1.next();
         this.registerConverter(Key.of(wrapperType), ArgumentConverters.get(TypeToken.of(wrapperType)));
      }
   }

   @Override
   public Command.Builder newCommand(String name) {
      return CommandImpl.builder(name);
   }

   @Override
   public void register(Command command) {
      this.validateAndCache(command, new HashSet<>());
      this.lock.writeLock().lock();

      try {
         this.registerIfAvailable(command.getName(), command);
         UnmodifiableIterator var2 = command.getAliases().iterator();

         while (var2.hasNext()) {
            String alias = (String)var2.next();
            this.registerIfAvailable(alias, command);
         }
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   private void validateAndCache(Command command, Set<Command> seen) {
      if (!seen.add(command)) {
         throw new IllegalStateException("Self-referential command");
      } else {
         this.commandInfoCache.getInfo(command);
         command.getParts()
            .stream()
            .filter(p -> p instanceof SubCommandPart)
            .flatMap(p -> ((SubCommandPart)p).getCommands().stream())
            .forEach(c -> this.validateAndCache(c, seen));
      }
   }

   private void registerIfAvailable(String name, Command command) {
      Command existing = this.commands.put(name, command);
      if (existing != null) {
         this.commands.put(name, existing);
         throw new IllegalArgumentException("A command is already registered under " + name + "; existing=" + existing + ",rejected=" + command);
      }
   }

   @Override
   public <T> void registerConverter(Key<T> key, ArgumentConverter<T> converter) {
      this.lock.writeLock().lock();

      try {
         this.converters.put(key, converter);
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   @Override
   public Set<Key<?>> keySet() {
      this.lock.readLock().lock();

      Set var1;
      try {
         var1 = this.converters.keySet();
      } finally {
         this.lock.readLock().unlock();
      }

      return var1;
   }

   @Override
   public <T> Optional<ArgumentConverter<T>> getConverter(Key<T> key) {
      ArgumentConverter<T> converter = (ArgumentConverter<T>)this.getArgumentConverter(key);
      return Optional.ofNullable(converter);
   }

   @Nullable
   private <T> ArgumentConverter<?> getArgumentConverter(Key<T> key) {
      this.lock.readLock().lock();

      ArgumentConverter var2;
      try {
         var2 = this.converters.get(key);
      } finally {
         this.lock.readLock().unlock();
      }

      return var2;
   }

   @Override
   public Stream<Command> getAllCommands() {
      this.lock.readLock().lock();

      ImmutableSet<Command> allCommands;
      try {
         allCommands = ImmutableSet.copyOf(this.commands.values());
      } finally {
         this.lock.readLock().unlock();
      }

      return allCommands.stream();
   }

   @Override
   public Optional<Command> getCommand(String name) {
      this.lock.readLock().lock();

      Optional var2;
      try {
         var2 = Optional.ofNullable(this.commands.get(name));
      } finally {
         this.lock.readLock().unlock();
      }

      return var2;
   }

   @Override
   public ImmutableSet<Suggestion> getSuggestions(InjectedValueAccess context, List<String> args) {
      this.lock.readLock().lock();

      Command command;
      CommandParseResult parseResult;
      try {
         String name = args.get(0);
         command = this.commands.get(name);
         if (command == null) {
            return this.suggestCommands(context, name);
         }

         try {
            parseResult = this.parse(context, args);
         } catch (UsageException var10) {
            parseResult = var10.getCommandParseResult();
         }
      } finally {
         this.lock.readLock().unlock();
      }

      List<String> reconstructedArguments = parseResult.getOriginalArguments();
      Preconditions.checkState(reconstructedArguments.size() <= args.size(), "Reconstructed arguments list bigger than original args list");
      return ImmutableSet.copyOf(
         command.getSuggester()
            .provideSuggestions(args.subList(1, args.size()), parseResult)
            .stream()
            .map(s -> s.toBuilder().replacedArgument(s.getReplacedArgument() + 1).build())
            .iterator()
      );
   }

   private ImmutableSet<Suggestion> suggestCommands(InjectedValueAccess context, String name) {
      Predicate<String> nameFilter = SuggestionHelper.byPrefix(name);
      return ImmutableSet.copyOf(
         this.getAllCommands()
            .filter(c -> nameFilter.test(c.getName()) && c.getCondition().satisfied(context))
            .map(Command::getName)
            .map(s -> Suggestion.builder().suggestion(s).replacedArgument(0).build())
            .iterator()
      );
   }

   @Override
   public CommandParseResult parse(InjectedValueAccess context, List<String> args) {
      this.lock.readLock().lock();

      CommandParseResult var7;
      try {
         String name = args.get(0);
         Command command = this.commands.get(name);
         if (command == null) {
            throw new NoSuchCommandException(name);
         }

         InjectedValueAccess cachedContext = MemoizingValueAccess.wrap(context);
         CommandMetadata metadata = CommandMetadataImpl.builder().calledName(name).arguments(ImmutableList.copyOf(args.subList(1, args.size()))).build();
         var7 = new CommandParser(this, this.commandInfoCache, command, metadata, cachedContext).parse();
      } finally {
         this.lock.readLock().unlock();
      }

      return var7;
   }
}
