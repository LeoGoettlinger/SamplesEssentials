package github.nighter.smartspawner.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.clear.ClearSubCommand;
import github.nighter.smartspawner.commands.config.FolderConfigSubCommand;
import github.nighter.smartspawner.commands.editloot.EditLootSubCommand;
import github.nighter.smartspawner.commands.give.GiveSubCommand;
import github.nighter.smartspawner.commands.hologram.HologramSubCommand;
import github.nighter.smartspawner.commands.list.ListSubCommand;
import github.nighter.smartspawner.commands.near.NearSubCommand;
import github.nighter.smartspawner.commands.prices.PricesSubCommand;
import github.nighter.smartspawner.commands.reload.ReloadSubCommand;
import github.nighter.smartspawner.commands.set.SetSubCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import lombok.Generated;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MainCommand {
   private final List<BaseSubCommand> subCommands;

   public MainCommand(SmartSpawner plugin) {
      this.subCommands = List.of(
         new ReloadSubCommand(plugin),
         new GiveSubCommand(plugin),
         new ListSubCommand(plugin),
         new HologramSubCommand(plugin),
         new PricesSubCommand(plugin),
         new ClearSubCommand(plugin),
         new NearSubCommand(plugin, plugin.getSpawnerHighlightManager()),
         new SetSubCommand(plugin),
         new FolderConfigSubCommand(plugin, FolderConfigSubCommand.ConfigOption.LANGUAGE),
         new FolderConfigSubCommand(plugin, FolderConfigSubCommand.ConfigOption.GUI_LAYOUT),
         new EditLootSubCommand(plugin, plugin.getLootEditorService(), plugin.getLootEditorUI())
      );
   }

   public LiteralCommandNode<CommandSourceStack> buildCommand() {
      return this.buildCommandWithName("smartspawner");
   }

   public LiteralCommandNode<CommandSourceStack> buildAliasCommand() {
      return this.buildCommandWithName("spawner");
   }

   public LiteralCommandNode<CommandSourceStack> buildAliasCommand2() {
      return this.buildCommandWithName("ss");
   }

   private LiteralCommandNode<CommandSourceStack> buildCommandWithName(String name) {
      LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(name);
      builder.requires(
         source -> {
            CommandSender sender = source.getSender();
            if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
               return true;
            } else {
               return !(sender instanceof Player player)
                  ? sender.hasPermission("smartspawner.command.use")
                  : player.hasPermission("smartspawner.command.use") || player.isOp();
            }
         }
      );

      for (BaseSubCommand subCommand : this.subCommands) {
         builder.then(subCommand.build());
      }

      return builder.build();
   }

   @Generated
   public MainCommand(List<BaseSubCommand> subCommands) {
      this.subCommands = subCommands;
   }
}
