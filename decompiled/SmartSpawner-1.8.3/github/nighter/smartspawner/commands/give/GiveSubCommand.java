package github.nighter.smartspawner.commands.give;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.BaseSubCommand;
import github.nighter.smartspawner.spawner.config.ItemSpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.config.SpawnerNames;
import github.nighter.smartspawner.spawner.config.SpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.item.SpawnerItemFactory;
import github.nighter.smartspawner.utils.DynamicEntityValidator;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GiveSubCommand extends BaseSubCommand {
   private final SpawnerItemFactory spawnerItemFactory;
   private static final int MAX_AMOUNT = 6400;

   public GiveSubCommand(SmartSpawner plugin) {
      super(plugin);
      this.spawnerItemFactory = plugin.getSpawnerItemFactory();
   }

   @Override
   public String getName() {
      return "give";
   }

   @Override
   public String getPermission() {
      return "smartspawner.command.give";
   }

   @Override
   public String getDescription() {
      return "Give spawners to players";
   }

   @Override
   public LiteralArgumentBuilder<CommandSourceStack> build() {
      LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(this.getName());
      builder.requires(source -> this.hasPermission(source.getSender()));
      builder.then(
         ((RequiredArgumentBuilder)Commands.argument("player", ArgumentTypes.player())
               .suggests(this.createPlayerSuggestions())
               .then(this.buildVanillaGiveCommand()))
            .then(this.buildSpawnerGiveCommand())
      );
      return builder;
   }

   private LiteralArgumentBuilder<CommandSourceStack> buildVanillaGiveCommand() {
      return (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("vanilla")
         .then(
            ((RequiredArgumentBuilder)Commands.argument("mobType", StringArgumentType.word())
                  .suggests(this.createMobSuggestions())
                  .executes(context -> this.giveVanilla(context, StringArgumentType.getString(context, "mobType"), 1)))
               .then(
                  Commands.argument("amount", IntegerArgumentType.integer(1, 6400))
                     .executes(
                        context -> this.giveVanilla(
                           context, StringArgumentType.getString(context, "mobType"), IntegerArgumentType.getInteger(context, "amount")
                        )
                     )
               )
         );
   }

   private RequiredArgumentBuilder<CommandSourceStack, String> buildSpawnerGiveCommand() {
      return (RequiredArgumentBuilder<CommandSourceStack, String>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                  "spawner", StringArgumentType.word()
               )
               .suggests(this.createSpawnerSuggestions())
               .executes(context -> this.giveByName(context, StringArgumentType.getString(context, "spawner"), 1)))
            .then(
               Commands.argument("amount", IntegerArgumentType.integer(1, 6400))
                  .executes(
                     context -> this.giveByName(context, StringArgumentType.getString(context, "spawner"), IntegerArgumentType.getInteger(context, "amount"))
                  )
            ))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("legacy", StringArgumentType.greedyString())
                  .requires(source -> !(source.getSender() instanceof Player)))
               .suggests(this.createLegacySuggestions())
               .executes(this::giveLegacy)
         );
   }

   private SuggestionProvider<CommandSourceStack> createPlayerSuggestions() {
      return (context, builder) -> {
         String input = builder.getRemaining().toLowerCase(Locale.ROOT);
         this.plugin
            .getServer()
            .getOnlinePlayers()
            .stream()
            .<String>map(Player::getName)
            .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
            .forEach(builder::suggest);
         return builder.buildFuture();
      };
   }

   private SuggestionProvider<CommandSourceStack> createSpawnerSuggestions() {
      return (context, builder) -> {
         String input = builder.getRemaining().toLowerCase(Locale.ROOT);
         SpawnerNames.suggestionNames(this.plugin).stream().filter(name -> name.startsWith(input)).forEach(builder::suggest);
         return builder.buildFuture();
      };
   }

   private SuggestionProvider<CommandSourceStack> createMobSuggestions() {
      return (context, builder) -> {
         String input = builder.getRemaining().toLowerCase(Locale.ROOT);
         DynamicEntityValidator.getValidEntities()
            .stream()
            .map(type -> type.name().toLowerCase())
            .filter(name -> name.startsWith(input))
            .sorted()
            .forEach(builder::suggest);
         return builder.buildFuture();
      };
   }

   private SuggestionProvider<CommandSourceStack> createLegacySuggestions() {
      return (context, builder) -> {
         String keyword = StringArgumentType.getString(context, "spawner").toLowerCase(Locale.ROOT);
         String input = builder.getRemaining().toLowerCase(Locale.ROOT);
         if (input.contains(" ")) {
            return builder.buildFuture();
         } else {
            Set<String> names = switch (keyword) {
               case "smart_spawner" -> this.plugin.getSpawnerSettingsConfig().getDefinitionNames();
               case "item_spawner" -> this.plugin.getItemSpawnerSettingsConfig().getDefinitionNames();
               default -> Set.of();
            };
            names.stream().filter(name -> name.startsWith(input)).sorted().forEach(builder::suggest);
            return builder.buildFuture();
         }
      };
   }

   @Override
   public int execute(CommandContext<CommandSourceStack> context) {
      return 0;
   }

   private int giveByName(CommandContext<CommandSourceStack> context, String name, int amount) {
      SpawnerNames.Resolved resolved = SpawnerNames.resolve(this.plugin, name);
      if (resolved == null) {
         return this.fail(context, "give.invalid_mob_type");
      } else {
         return resolved.mob() != null ? this.giveMob(context, resolved.mob(), amount) : this.giveItem(context, resolved.item(), amount);
      }
   }

   private int giveVanilla(CommandContext<CommandSourceStack> context, String mobType, int amount) {
      EntityType entityType;
      try {
         entityType = EntityType.valueOf(mobType.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var6) {
         return this.fail(context, "give.invalid_mob_type");
      }

      return this.give(
         context,
         this.spawnerItemFactory.createVanillaSpawnerItem(entityType, amount),
         this.plugin.getLanguageManager().getFormattedMobName(entityType),
         amount
      );
   }

   private int giveLegacy(CommandContext<CommandSourceStack> context) {
      String keyword = StringArgumentType.getString(context, "spawner").toLowerCase(Locale.ROOT);
      String[] parts = StringArgumentType.getString(context, "legacy").trim().split(" +");
      if (parts.length <= 2 && keyword.endsWith("_spawner") && SpawnerNames.RESERVED.contains(keyword)) {
         int amount = 1;
         if (parts.length == 2) {
            try {
               amount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException var9) {
               amount = 0;
            }

            if (amount < 1 || amount > 6400) {
               return this.fail(context, "give.invalid_mob_type");
            }
         }

         String name = parts[0];
         switch (keyword) {
            case "vanilla_spawner":
               return this.giveVanilla(context, name, amount);
            case "item_spawner":
               ItemSpawnerSettingsConfig.ItemDefinition item = this.plugin.getItemSpawnerSettingsConfig().resolve(name);
               return item == null ? this.fail(context, "give.invalid_item_spawner") : this.giveItem(context, item, amount);
            default:
               SpawnerSettingsConfig.MobDefinition mob = this.plugin.getSpawnerSettingsConfig().resolve(name);
               return mob == null ? this.fail(context, "give.invalid_mob_type") : this.giveMob(context, mob, amount);
         }
      } else {
         return this.fail(context, "give.invalid_mob_type");
      }
   }

   private int giveMob(CommandContext<CommandSourceStack> context, SpawnerSettingsConfig.MobDefinition mob, int amount) {
      String displayName = mob.displayName() != null ? mob.displayName() : this.plugin.getLanguageManager().getFormattedMobName(mob.entityType());
      return this.give(context, this.spawnerItemFactory.createSmartSpawnerItem(mob.name(), amount), displayName, amount);
   }

   private int giveItem(CommandContext<CommandSourceStack> context, ItemSpawnerSettingsConfig.ItemDefinition item, int amount) {
      String displayName = item.displayName() != null ? item.displayName() : this.plugin.getLanguageManager().getVanillaItemName(item.material());
      return this.give(context, this.spawnerItemFactory.createItemSpawnerItem(item.name(), amount), displayName, amount);
   }

   private int fail(CommandContext<CommandSourceStack> context, String messageKey) {
      this.logCommandExecution(context);
      this.plugin.getMessageService().sendMessage(((CommandSourceStack)context.getSource()).getSender(), messageKey);
      return 0;
   }

   private int give(CommandContext<CommandSourceStack> context, ItemStack spawnerItem, String displayName, int amount) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      this.logCommandExecution(context);

      try {
         PlayerSelectorArgumentResolver playerSelector = (PlayerSelectorArgumentResolver)context.getArgument("player", PlayerSelectorArgumentResolver.class);
         List<Player> players = (List<Player>)playerSelector.resolve((CommandSourceStack)context.getSource());
         if (players.isEmpty()) {
            this.plugin.getMessageService().sendMessage(sender, "give.player_not_found");
            return 0;
         } else {
            Player target = players.get(0);
            this.giveOrDropOverflow(target, spawnerItem);
            target.playSound(target.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
            String smallCapsName = this.plugin.getLanguageManager().getSmallCaps(displayName);
            HashMap<String, String> senderPlaceholders = new HashMap<>();
            senderPlaceholders.put("player", target.getName());
            senderPlaceholders.put("entity", displayName);
            senderPlaceholders.put("ᴇɴᴛɪᴛʏ", smallCapsName);
            senderPlaceholders.put("amount", String.valueOf(amount));
            HashMap<String, String> targetPlaceholders = new HashMap<>();
            targetPlaceholders.put("amount", String.valueOf(amount));
            targetPlaceholders.put("entity", displayName);
            targetPlaceholders.put("ᴇɴᴛɪᴛʏ", smallCapsName);
            this.plugin.getMessageService().sendMessage(sender, "give.spawner_given", senderPlaceholders);
            this.plugin.getMessageService().sendMessage(target, "give.spawner_received", targetPlaceholders);
            return 1;
         }
      } catch (Exception var12) {
         this.plugin.getLogger().severe("Error executing give command: " + var12.getMessage());
         return 0;
      }
   }

   private void giveOrDropOverflow(Player target, ItemStack itemStack) {
      boolean droppedItems = false;

      for (ItemStack stack : this.splitIntoValidStacks(itemStack)) {
         Map<Integer, ItemStack> leftovers = target.getInventory().addItem(new ItemStack[]{stack.clone()});
         if (!leftovers.isEmpty()) {
            droppedItems = true;

            for (ItemStack leftover : leftovers.values()) {
               for (ItemStack dropStack : this.splitIntoValidStacks(leftover)) {
                  target.getWorld().dropItem(target.getLocation(), dropStack);
               }
            }
         }
      }

      if (droppedItems) {
         this.plugin.getMessageService().sendMessage(target, "give.inventory_full");
      }

      target.updateInventory();
   }

   private List<ItemStack> splitIntoValidStacks(ItemStack itemStack) {
      List<ItemStack> stacks = new ArrayList<>();
      int maxStackSize = Math.max(1, itemStack.getMaxStackSize());
      int remainingAmount = itemStack.getAmount();

      while (remainingAmount > 0) {
         int stackAmount = Math.min(maxStackSize, remainingAmount);
         ItemStack stack = itemStack.clone();
         stack.setAmount(stackAmount);
         stacks.add(stack);
         remainingAmount -= stackAmount;
      }

      return stacks;
   }
}
