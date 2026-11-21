package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitBrigadier;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.core.BukkitActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.util.Preconditions;
import revxrsal.commands.util.Primitives;

public final class CommodoreBukkitBrigadier implements BukkitBrigadier {
   private final BukkitCommandHandler handler;
   private boolean nativePlayerCompletions = true;
   private boolean showEnumsAsNativeLiterals = false;
   private final Commodore commodore;
   private final List<ArgumentTypeResolver> resolvers = new ArrayList<>();

   public CommodoreBukkitBrigadier(BukkitCommandHandler handler) {
      this.handler = handler;
      this.commodore = CommodoreProvider.getCommodore(handler);
      if (CommodoreProvider.isSupported()) {
         this.bind(String.class, DefaultArgTypeResolvers.STRING);
         this.registerArgumentTypeResolver(DefaultArgTypeResolvers.NUMBER);
         this.bind(Boolean.class, DefaultArgTypeResolvers.BOOLEAN);
         this.bind(Player.class, DefaultArgTypeResolvers.PLAYER);
         this.bind(OfflinePlayer.class, DefaultArgTypeResolvers.PLAYER);
         this.bind(EntitySelector.class, DefaultArgTypeResolvers.ENTITY_SELECTOR);
      }
   }

   @Override
   public void registerArgumentTypeResolver(@NotNull ArgumentTypeResolver resolver) {
      Preconditions.notNull(resolver, "resolver");
      this.resolvers.add(resolver);
   }

   @Override
   public void registerArgumentTypeResolver(int priority, @NotNull ArgumentTypeResolver resolver) {
      Preconditions.notNull(resolver, "resolver");
      this.resolvers.add(priority, resolver);
   }

   @Override
   public void bind(@NotNull Class<?> type, @NotNull ArgumentTypeResolver resolver) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(resolver, "resolver");
      this.resolvers.add(parameter -> Primitives.wrap(parameter.getType()) == type ? resolver.getArgumentType(parameter) : null);
   }

   @Override
   public void bind(@NotNull Class<?> type, @NotNull ArgumentType<?> argumentType) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(argumentType, "argument type");
      this.resolvers.add(ArgumentTypeResolver.forType(type, argumentType));
   }

   @Override
   public void bind(@NotNull Class<?> type, @NotNull MinecraftArgumentType argumentType) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(argumentType, "argument type");
      argumentType.getIfPresent().ifPresent(c -> this.resolvers.add(ArgumentTypeResolver.forType(type, (ArgumentType<?>)c)));
   }

   @NotNull
   @Override
   public ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter) {
      for (ArgumentTypeResolver resolver : this.resolvers) {
         ArgumentType<?> type = resolver.getArgumentType(parameter);
         if (type != null) {
            return type;
         }
      }

      return StringArgumentType.string();
   }

   private void checkSupported() {
      if (this.commodore == null) {
         throw new IllegalArgumentException("Brigadier is not supported on this version.");
      }
   }

   @NotNull
   @Override
   public CommandActor wrapSource(@NotNull Object commandSource) {
      this.checkSupported();
      return new BukkitActor(BrigadierUtil.getBukkitSender(commandSource), this.handler);
   }

   @Override
   public void disableNativePlayerCompletion() {
      this.nativePlayerCompletions = false;
   }

   @Override
   public boolean isNativePlayerCompletionEnabled() {
      return this.nativePlayerCompletions;
   }

   @Override
   public void showEnumsAsNativeLiterals(boolean show) {
      this.showEnumsAsNativeLiterals = show;
   }

   @Override
   public boolean isShowEnumsAsNativeLiterals() {
      return this.showEnumsAsNativeLiterals;
   }

   @Override
   public void register() {
      if (CommodoreProvider.isSupported()) {
         NodeParser parser = new NodeParser(this);
         parser.parse(this.handler).forEach(n -> this.register(n.getNode()));
      }
   }

   @NotNull
   @Override
   public BukkitCommandHandler getCommandHandler() {
      return this.handler;
   }

   private void register(@NotNull LiteralCommandNode<?> node) {
      Command command = ((JavaPlugin)this.handler.getPlugin()).getCommand(node.getLiteral());
      if (command == null) {
         this.commodore.register(node);
      } else {
         this.commodore.register(command, node);
      }
   }
}
