package vrx.economy.vRXClans.libs.cloud.bukkit.parser;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.StringRange;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vrx.economy.vRXClans.libs.cloud.CommandManager;
import vrx.economy.vRXClans.libs.cloud.brigadier.parser.WrappedBrigadierParser;
import vrx.economy.vRXClans.libs.cloud.bukkit.data.ItemStackPredicate;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.CommandBuildContextSupplier;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.CraftBukkitReflection;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.MinecraftArgumentTypes;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProvider;

public final class ItemStackPredicateParser<C> implements ArgumentParser.FutureArgumentParser<C, ItemStackPredicate> {
   private static final Class<?> CRAFT_ITEM_STACK_CLASS = CraftBukkitReflection.needOBCClass("inventory.CraftItemStack");
   private static final Supplier<Class<?>> ARGUMENT_ITEM_PREDICATE_CLASS = Suppliers.memoize(
      () -> MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("item_predicate"))
   );
   private static final Class<?> ARGUMENT_ITEM_PREDICATE_RESULT_CLASS = CraftBukkitReflection.firstNonNullOrNull(
      CraftBukkitReflection.findNMSClass("ArgumentItemPredicate$b"),
      CraftBukkitReflection.findMCClass("commands.arguments.item.ArgumentItemPredicate$b"),
      CraftBukkitReflection.findMCClass("commands.arguments.item.ItemPredicateArgument$Result")
   );
   @Nullable
   private static final Method CREATE_PREDICATE_METHOD = ARGUMENT_ITEM_PREDICATE_RESULT_CLASS == null
      ? null
      : CraftBukkitReflection.firstNonNullOrNull(
         CraftBukkitReflection.findMethod(ARGUMENT_ITEM_PREDICATE_RESULT_CLASS, "create", com.mojang.brigadier.context.CommandContext.class),
         CraftBukkitReflection.findMethod(ARGUMENT_ITEM_PREDICATE_RESULT_CLASS, "a", com.mojang.brigadier.context.CommandContext.class)
      );
   private static final Method AS_NMS_COPY_METHOD = CraftBukkitReflection.needMethod(CRAFT_ITEM_STACK_CLASS, "asNMSCopy", ItemStack.class);
   private final ArgumentParser<C, ItemStackPredicate> parser = this.createParser();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, ItemStackPredicate> itemStackPredicateParser() {
      return ParserDescriptor.of(new ItemStackPredicateParser<>(), ItemStackPredicate.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, ItemStackPredicate> itemStackPredicateComponent() {
      return CommandComponent.<C, ItemStackPredicate>builder().parser(itemStackPredicateParser());
   }

   private ArgumentParser<C, ItemStackPredicate> createParser() {
      Supplier<ArgumentType<Object>> inst = () -> {
         Constructor<?> ctr = ARGUMENT_ITEM_PREDICATE_CLASS.get().getDeclaredConstructors()[0];

         try {
            return ctr.getParameterCount() == 0
               ? (ArgumentType)ctr.newInstance()
               : (ArgumentType)ctr.newInstance(CommandBuildContextSupplier.commandBuildContext());
         } catch (ReflectiveOperationException var2) {
            throw new RuntimeException("Failed to initialize ItemPredicate parser.", var2);
         }
      };
      return new WrappedBrigadierParser<C, Object>(inst).flatMapSuccess((ctx, result) -> {
         if (result instanceof Predicate) {
            return ArgumentParseResult.successFuture(new ItemStackPredicateParser.ItemStackPredicateImpl((Predicate<Object>)result));
         } else {
            Object commandSourceStack = ctx.get("_cloud_brigadier_native_sender");
            com.mojang.brigadier.context.CommandContext<Object> dummy = createDummyContext(ctx, commandSourceStack);
            Objects.requireNonNull(CREATE_PREDICATE_METHOD, "ItemPredicateArgument$Result#create");

            try {
               Predicate<Object> predicate = (Predicate<Object>)CREATE_PREDICATE_METHOD.invoke(result, dummy);
               return ArgumentParseResult.successFuture(new ItemStackPredicateParser.ItemStackPredicateImpl(predicate));
            } catch (ReflectiveOperationException var5) {
               throw new RuntimeException(var5);
            }
         }
      });
   }

   @NonNull
   private static <C> com.mojang.brigadier.context.CommandContext<Object> createDummyContext(
      @NonNull final CommandContext<C> ctx, @NonNull final Object commandSourceStack
   ) {
      return new com.mojang.brigadier.context.CommandContext(
         commandSourceStack, ctx.rawInput().input(), Collections.emptyMap(), null, null, Collections.emptyList(), StringRange.at(0), null, null, false
      );
   }

   private static <C> void registerParserSupplier(@NonNull final CommandManager<C> commandManager) {
      commandManager.parserRegistry().registerParser(itemStackPredicateParser());
   }

   @NonNull
   @Override
   public CompletableFuture<ArgumentParseResult<ItemStackPredicate>> parseFuture(
      @NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput
   ) {
      return this.parser.parseFuture(commandContext, commandInput);
   }

   @NonNull
   @Override
   public SuggestionProvider<C> suggestionProvider() {
      return this.parser.suggestionProvider();
   }

   private static final class ItemStackPredicateImpl implements ItemStackPredicate {
      private final Predicate<Object> predicate;

      ItemStackPredicateImpl(@NonNull final Predicate<Object> predicate) {
         this.predicate = predicate;
      }

      public boolean test(@NonNull final ItemStack itemStack) {
         try {
            return this.predicate.test(ItemStackPredicateParser.AS_NMS_COPY_METHOD.invoke(null, itemStack));
         } catch (ReflectiveOperationException var3) {
            throw new RuntimeException(var3);
         }
      }
   }
}
