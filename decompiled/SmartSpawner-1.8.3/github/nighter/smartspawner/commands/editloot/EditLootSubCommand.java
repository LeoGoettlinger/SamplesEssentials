package github.nighter.smartspawner.commands.editloot;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.BaseSubCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class EditLootSubCommand extends BaseSubCommand {
   private final LootEditorService service;
   private final LootEditorUI ui;

   public EditLootSubCommand(SmartSpawner plugin, LootEditorService service, LootEditorUI ui) {
      super(plugin);
      this.service = service;
      this.ui = ui;
   }

   @Override
   public String getName() {
      return "editloot";
   }

   @Override
   public String getPermission() {
      return "smartspawner.command.editloot";
   }

   @Override
   public String getDescription() {
      return "Edit a spawner's loot table in game";
   }

   @Override
   public LiteralArgumentBuilder<CommandSourceStack> build() {
      LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(this.getName());
      builder.requires(source -> this.hasPermission(source.getSender()));
      builder.executes(context -> {
         this.logCommandExecution(context);
         return this.execute(context);
      });
      RequiredArgumentBuilder<CommandSourceStack, String> nameArgument = (RequiredArgumentBuilder<CommandSourceStack, String>)Commands.argument(
            "name", StringArgumentType.word()
         )
         .suggests(this.nameSuggestions())
         .executes(context -> {
            this.logCommandExecution(context);
            return this.open(context, StringArgumentType.getString(context, "name"));
         });
      builder.then(nameArgument);
      return builder;
   }

   @Override
   public int execute(CommandContext<CommandSourceStack> context) {
      this.plugin.getMessageService().sendMessage(((CommandSourceStack)context.getSource()).getSender(), "editloot.usage");
      return 0;
   }

   private int open(CommandContext<CommandSourceStack> context, String name) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      if (sender instanceof Player player) {
         LootEditorService.EntryRef ref = this.service.findEntry(name);
         if (ref == null) {
            this.plugin.getMessageService().sendMessage(player, "editloot.unknown_entry", Map.of("name", name));
            return 0;
         } else {
            this.ui.openLootList(player, ref.getTarget(), ref.getEntryKey());
            return 1;
         }
      } else {
         this.plugin.getMessageService().sendMessage(sender, "player_only");
         return 0;
      }
   }

   private SuggestionProvider<CommandSourceStack> nameSuggestions() {
      return (context, builder) -> {
         String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

         for (String name : this.service.listAllEntryNames()) {
            if (name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
               builder.suggest(name);
            }
         }

         return builder.buildFuture();
      };
   }
}
