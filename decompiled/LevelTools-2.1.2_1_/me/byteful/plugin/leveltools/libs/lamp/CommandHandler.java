package me.byteful.plugin.leveltools.libs.lamp;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import me.byteful.plugin.leveltools.libs.lamp.annotation.dynamic.AnnotationReplacer;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.AutoCompleter;
import me.byteful.plugin.leveltools.libs.lamp.command.ArgumentStack;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandCategory;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import me.byteful.plugin.leveltools.libs.lamp.core.CommandPath;
import me.byteful.plugin.leveltools.libs.lamp.core.reflect.MethodCallerFactory;
import me.byteful.plugin.leveltools.libs.lamp.exception.CommandExceptionHandler;
import me.byteful.plugin.leveltools.libs.lamp.help.CommandHelpWriter;
import me.byteful.plugin.leveltools.libs.lamp.locales.Translator;
import me.byteful.plugin.leveltools.libs.lamp.process.CommandCondition;
import me.byteful.plugin.leveltools.libs.lamp.process.ContextResolver;
import me.byteful.plugin.leveltools.libs.lamp.process.ContextResolverFactory;
import me.byteful.plugin.leveltools.libs.lamp.process.ParameterNamingStrategy;
import me.byteful.plugin.leveltools.libs.lamp.process.ParameterValidator;
import me.byteful.plugin.leveltools.libs.lamp.process.PermissionReader;
import me.byteful.plugin.leveltools.libs.lamp.process.ResponseHandler;
import me.byteful.plugin.leveltools.libs.lamp.process.SenderResolver;
import me.byteful.plugin.leveltools.libs.lamp.process.ValueResolver;
import me.byteful.plugin.leveltools.libs.lamp.process.ValueResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public interface CommandHandler {
   @NotNull
   CommandHandler register(@NotNull Object... var1);

   @NotNull
   Locale getLocale();

   void setLocale(@NotNull Locale var1);

   @NotNull
   Translator getTranslator();

   @NotNull
   CommandHandler setMethodCallerFactory(@NotNull MethodCallerFactory var1);

   @NotNull
   CommandHandler setExceptionHandler(@NotNull CommandExceptionHandler var1);

   @NotNull
   <T extends Throwable> CommandHandler registerExceptionHandler(@NotNull Class<T> var1, @NotNull BiConsumer<CommandActor, T> var2);

   @NotNull
   CommandHandler setSwitchPrefix(@NotNull String var1);

   @NotNull
   CommandHandler setFlagPrefix(@NotNull String var1);

   @NotNull
   CommandHandler setMessagePrefix(@NotNull String var1);

   @NotNull
   <T> CommandHandler setHelpWriter(@NotNull CommandHelpWriter<T> var1);

   @NotNull
   CommandHandler setParameterNamingStrategy(@NotNull ParameterNamingStrategy var1);

   @NotNull
   ParameterNamingStrategy getParameterNamingStrategy();

   @NotNull
   CommandHandler disableStackTraceSanitizing();

   @NotNull
   CommandHandler failOnTooManyArguments();

   @NotNull
   CommandHandler registerSenderResolver(@NotNull SenderResolver var1);

   @NotNull
   CommandHandler registerPermissionReader(@NotNull PermissionReader var1);

   @NotNull
   <T> CommandHandler registerValueResolver(@NotNull Class<T> var1, @NotNull ValueResolver<T> var2);

   @NotNull
   <T> CommandHandler registerValueResolver(int var1, @NotNull Class<T> var2, @NotNull ValueResolver<T> var3);

   @NotNull
   <T> CommandHandler registerContextResolver(@NotNull Class<T> var1, @NotNull ContextResolver<T> var2);

   @NotNull
   <T> CommandHandler registerContextResolver(int var1, @NotNull Class<T> var2, @NotNull ContextResolver<T> var3);

   @NotNull
   <T> CommandHandler registerContextValue(@NotNull Class<T> var1, @Nullable T var2);

   @NotNull
   <T> CommandHandler registerContextValue(int var1, @NotNull Class<T> var2, @NotNull T var3);

   @NotNull
   CommandHandler registerValueResolverFactory(@NotNull ValueResolverFactory var1);

   @NotNull
   CommandHandler registerValueResolverFactory(int var1, @NotNull ValueResolverFactory var2);

   @NotNull
   CommandHandler registerContextResolverFactory(@NotNull ContextResolverFactory var1);

   @NotNull
   CommandHandler registerContextResolverFactory(int var1, @NotNull ContextResolverFactory var2);

   @NotNull
   CommandHandler registerCondition(@NotNull CommandCondition var1);

   @NotNull
   <T> CommandHandler registerDependency(@NotNull Class<T> var1, @NotNull Supplier<T> var2);

   @NotNull
   <T> CommandHandler registerDependency(@NotNull Class<T> var1, T var2);

   @NotNull
   <T> CommandHandler registerParameterValidator(@NotNull Class<T> var1, @NotNull ParameterValidator<T> var2);

   @NotNull
   <T> CommandHandler registerResponseHandler(@NotNull Class<T> var1, @NotNull ResponseHandler<T> var2);

   @NotNull
   <T extends Annotation> CommandHandler registerAnnotationReplacer(@NotNull Class<T> var1, @NotNull AnnotationReplacer<T> var2);

   @NotNull
   CommandHandler accept(@NotNull CommandHandlerVisitor var1);

   @NotNull
   AutoCompleter getAutoCompleter();

   ExecutableCommand getCommand(@NotNull CommandPath var1);

   CommandCategory getCategory(@NotNull CommandPath var1);

   @NotNull
   @UnmodifiableView
   Map<CommandPath, ExecutableCommand> getCommands();

   @NotNull
   @UnmodifiableView
   Map<CommandPath, CommandCategory> getCategories();

   @NotNull
   CommandExceptionHandler getExceptionHandler();

   @NotNull
   MethodCallerFactory getMethodCallerFactory();

   <T> CommandHelpWriter<T> getHelpWriter();

   boolean unregister(@NotNull CommandPath var1);

   boolean unregister(@NotNull String var1);

   void unregisterAllCommands();

   @NotNull
   Set<CommandPath> getRootPaths();

   @NotNull
   String getSwitchPrefix();

   @NotNull
   String getFlagPrefix();

   @NotNull
   String getMessagePrefix();

   <T> Supplier<T> getDependency(@NotNull Class<T> var1);

   <T> Supplier<T> getDependency(@NotNull Class<T> var1, Supplier<T> var2);

   @NotNull
   <T> Optional<T> dispatch(@NotNull CommandActor var1, @NotNull ArgumentStack var2);

   @NotNull
   <T> Optional<T> dispatch(@NotNull CommandActor var1, @NotNull String var2);
}
