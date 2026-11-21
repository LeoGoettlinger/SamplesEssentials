package github.nighter.smartspawner.commands.near;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.BaseSubCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class NearSubCommand extends BaseSubCommand {
   private static final int DEFAULT_RADIUS = 50;
   private static final int[] SUGGESTED_RADII = new int[]{100, 1000, 10000};
   private final SpawnerHighlightManager highlightManager;
   private final NearResultGUI nearResultGUI;

   public NearSubCommand(SmartSpawner plugin, SpawnerHighlightManager highlightManager) {
      super(plugin);
      this.highlightManager = highlightManager;
      this.nearResultGUI = plugin.getNearResultGUI();
   }

   @Override
   public String getName() {
      return "near";
   }

   @Override
   public String getPermission() {
      return "smartspawner.command.near";
   }

   @Override
   public String getDescription() {
      return "Highlight nearby spawners through walls";
   }

   @Override
   public LiteralArgumentBuilder<CommandSourceStack> build() {
      LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(this.getName());
      builder.requires(source -> this.hasPermission(source.getSender()));
      builder.executes(this::execute);
      builder.then(Commands.argument("radius", IntegerArgumentType.integer(1, 10000)).suggests((ctx, suggestions) -> {
         for (int v : SUGGESTED_RADII) {
            suggestions.suggest(v);
         }

         return suggestions.buildFuture();
      }).executes(context -> this.executeScan(context, IntegerArgumentType.getInteger(context, "radius"))));
      builder.then(Commands.argument("action", StringArgumentType.word()).suggests((ctx, suggestions) -> {
         if (((CommandSourceStack)ctx.getSource()).getSender() instanceof Player p && this.highlightManager.hasActiveSession(p.getUniqueId())) {
            suggestions.suggest("cancel");
            suggestions.suggest("gui");
         }

         return suggestions.buildFuture();
      }).executes(this::executeAction));
      return builder;
   }

   @Override
   public int execute(CommandContext<CommandSourceStack> context) {
      return this.executeScan(context, 50);
   }

   private int executeAction(CommandContext<CommandSourceStack> context) {
      String action = StringArgumentType.getString(context, "action");
      String var3 = action.toLowerCase();

      return switch (var3) {
         case "cancel" -> this.executeCancel(context);
         case "gui" -> this.executeGui(context);
         default -> this.executeScan(context, 50);
      };
   }

   private int executeScan(CommandContext<CommandSourceStack> context, int radius) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      this.logCommandExecution(context);
      if (sender instanceof Player player) {
         this.highlightManager.startScan(player, radius);
         return 1;
      } else {
         this.plugin.getMessageService().sendMessage(sender, "player_only");
         return 0;
      }
   }

   private int executeCancel(CommandContext<CommandSourceStack> context) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      this.logCommandExecution(context);
      if (sender instanceof Player player) {
         this.highlightManager.cancelScan(player);
         return 1;
      } else {
         this.plugin.getMessageService().sendMessage(sender, "player_only");
         return 0;
      }
   }

   private int executeGui(CommandContext<CommandSourceStack> context) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      this.logCommandExecution(context);
      if (sender instanceof Player player) {
         this.nearResultGUI.openNearResultGUI(player, 1);
         return 1;
      } else {
         this.plugin.getMessageService().sendMessage(sender, "player_only");
         return 0;
      }
   }
}
