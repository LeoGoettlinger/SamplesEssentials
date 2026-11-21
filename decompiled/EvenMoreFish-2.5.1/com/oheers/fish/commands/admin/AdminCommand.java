package com.oheers.fish.commands.admin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.utils.ManifestUtil;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.commands.HelpMessage;
import com.oheers.fish.commands.admin.subcommand.BaitSubcommand;
import com.oheers.fish.commands.admin.subcommand.ClearBaitsSubcommand;
import com.oheers.fish.commands.admin.subcommand.CompetitionSubcommand;
import com.oheers.fish.commands.admin.subcommand.CustomRodSubcommand;
import com.oheers.fish.commands.admin.subcommand.DatabaseSubcommand;
import com.oheers.fish.commands.admin.subcommand.DebugSubcommand;
import com.oheers.fish.commands.admin.subcommand.FishSubcommand;
import com.oheers.fish.commands.admin.subcommand.ListSubcommand;
import com.oheers.fish.commands.admin.subcommand.RandomFishSubcommand;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.PrefixType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.jar.Attributes.Name;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.daisylib.command.CommandUtils;
import org.jspecify.annotations.NonNull;

public class AdminCommand {
   public static final HelpMessage HELP_MESSAGE = HelpMessage.helpMessage(MainConfig.getInstance().getAdminSubCommandName())
      .setDefaultRequirement("emf.admin")
      .addEntry("fish", ConfigMessage.HELP_ADMIN_FISH::getMessage)
      .addEntry("custom-rod", ConfigMessage.HELP_ADMIN_CUSTOMROD::getMessage)
      .addEntry("bait", ConfigMessage.HELP_ADMIN_BAIT::getMessage)
      .addEntry("clearbaits", ConfigMessage.HELP_ADMIN_CLEARBAITS::getMessage)
      .addEntry("reload", ConfigMessage.HELP_ADMIN_RELOAD::getMessage)
      .addEntry("version", ConfigMessage.HELP_ADMIN_VERSION::getMessage)
      .addEntry("migrate", ConfigMessage.HELP_ADMIN_MIGRATE::getMessage, "emf.admin.migrate")
      .addEntry("rawItem", ConfigMessage.HELP_ADMIN_RAWITEM::getMessage)
      .addEntry("debug", () -> EMFSingleMessage.fromString("Shows debug information for some features."))
      .addEntry("help", ConfigMessage.HELP_GENERAL_HELP::getMessage)
      .addEntry("competition", ConfigMessage.HELP_ADMIN_COMPETITION::getMessage)
      .addEntry("database", ConfigMessage.HELP_ADMIN_DATABASE::getMessage, "emf.admin.debug.database");
   private final String name;

   public AdminCommand(@NonNull String name) {
      this.name = name;
   }

   @NonNull
   public LiteralCommandNode<CommandSourceStack> get() {
      return this.getAsArgument().build();
   }

   @NonNull
   public LiteralArgumentBuilder<CommandSourceStack> getAsArgument() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                         this.name
                                                      )
                                                      .requires(source -> source.getSender().hasPermission("emf.admin")))
                                                   .executes(ctx -> {
                                                      sendHelpMessage(((CommandSourceStack)ctx.getSource()).getSender());
                                                      return 1;
                                                   }))
                                                .then(this.database()))
                                             .then(this.fish()))
                                          .then(this.randomFish()))
                                       .then(this.list()))
                                    .then(this.competition()))
                                 .then(this.customRod()))
                              .then(this.debug()))
                           .then(this.bait()))
                        .then(this.clearBaits()))
                     .then(this.reload()))
                  .then(this.version()))
               .then(this.rawItem()))
            .then(this.migrate()))
         .then(this.help());
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> database() {
      return new DatabaseSubcommand("database").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> fish() {
      return new FishSubcommand("fish").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> randomFish() {
      return new RandomFishSubcommand("random-fish").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> list() {
      return new ListSubcommand("list").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> competition() {
      return new CompetitionSubcommand("competition").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> customRod() {
      return new CustomRodSubcommand("custom-rod").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> debug() {
      return new DebugSubcommand("debug").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> bait() {
      return new BaitSubcommand("bait").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> clearBaits() {
      return new ClearBaitsSubcommand("clearbaits").get();
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> reload() {
      return Commands.literal("reload").executes(ctx -> {
         EvenMoreFish.getInstance().reload(((CommandSourceStack)ctx.getSource()).getSender());
         return 1;
      });
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> version() {
      return Commands.literal("version").executes(ctx -> {
         this.getVersionMessage().send(((CommandSourceStack)ctx.getSource()).getSender());
         return 1;
      });
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> rawItem() {
      return Commands.literal("rawItem").executes(ctx -> {
         Player player = CommandUtils.requirePlayer(ctx);
         ItemStack handItem = player.getInventory().getItemInMainHand();
         if (handItem.isEmpty()) {
            return 1;
         } else {
            String handItemNbt = EvenMoreFish.getInstance().getVersionProvider().serializeItemStack(handItem);
            YamlDocument document = new ConfigBase().getConfig();
            document.set("rawItem", handItemNbt);
            handItemNbt = document.dump().replaceFirst("rawItem: ", "");
            Builder builder = Component.text().content(handItemNbt);
            builder.hoverEvent(HoverEvent.hoverEvent(Action.SHOW_TEXT, Component.text("Click to copy to clipboard.")));
            builder.clickEvent(ClickEvent.copyToClipboard(handItemNbt));
            player.sendMessage(builder.build());
            return 1;
         }
      });
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> migrate() {
      return Commands.literal("migrate")
         .executes(
            ctx -> {
               CommandSender sender = ((CommandSourceStack)ctx.getSource()).getSender();
               if (!DatabaseUtil.isDatabaseOnline()) {
                  sender.sendPlainMessage("You cannot run migrations when the database is disabled. Please set database.enabled: true. And restart the server.");
                  return 1;
               } else {
                  EvenMoreFish.getInstance()
                     .getPluginDataManager()
                     .getDatabaseWorker()
                     .execute(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().migrateLegacy(sender));
                  return 1;
               }
            }
         );
   }

   @NonNull
   protected ArgumentBuilder<CommandSourceStack, ?> help() {
      return Commands.literal("help").executes(ctx -> {
         sendHelpMessage(((CommandSourceStack)ctx.getSource()).getSender());
         return 1;
      });
   }

   private EMFSingleMessage getVersionMessage() {
      int fishCount = FishManager.getInstance().getRarityMap().values().stream().mapToInt(rarity -> rarity.getFishList().size()).sum();
      String databaseEngine = "N/A";
      String databaseType = "N/A";
      Database database = EvenMoreFish.getInstance().getPluginDataManager().getDatabase();
      if (database != null) {
         databaseEngine = database.getDatabaseVersion();
         databaseType = database.getType();
      }

      String msgString = "{prefix} EvenMoreFish by Oheers {version} \n{prefix} Feature Branch: {branch} \n{prefix} Feature Build/Date: {build-date} \n{prefix} MCV: {mcv} \n{prefix} SSV: {ssv} \n{prefix} Online: {online} \n{prefix} Loaded Rarities({rarities}) Fish({fish}) Baits({baits}) Competitions({competitions}) \n{prefix} Database Engine: {engine} \n{prefix} Database Type: {type} \n";
      EMFSingleMessage message = EMFSingleMessage.fromString(
         "{prefix} EvenMoreFish by Oheers {version} \n{prefix} Feature Branch: {branch} \n{prefix} Feature Build/Date: {build-date} \n{prefix} MCV: {mcv} \n{prefix} SSV: {ssv} \n{prefix} Online: {online} \n{prefix} Loaded Rarities({rarities}) Fish({fish}) Baits({baits}) Competitions({competitions}) \n{prefix} Database Engine: {engine} \n{prefix} Database Type: {type} \n"
      );
      message.setVariable("{prefix}", PrefixType.DEFAULT.getPrefix());
      message.setVariable("{version}", EvenMoreFish.getInstance().getPluginMeta().getVersion());
      message.setVariable("{branch}", this.getFeatureBranchName());
      message.setVariable("{build-date}", this.getFeatureBranchBuildOrDate());
      message.setVariable("{mcv}", Bukkit.getServer().getVersion());
      message.setVariable("{ssv}", Bukkit.getServer().getBukkitVersion());
      message.setVariable("{online}", String.valueOf(Bukkit.getServer().getOnlineMode()));
      message.setVariable("{rarities}", String.valueOf(FishManager.getInstance().getRarityMap().size()));
      message.setVariable("{fish}", String.valueOf(fishCount));
      message.setVariable("{baits}", String.valueOf(BaitManager.getInstance().getItemMap().size()));
      message.setVariable("{competitions}", String.valueOf(CompetitionManager.getInstance().getSize()));
      message.setVariable("{engine}", databaseEngine);
      message.setVariable("{type}", databaseType);
      return message;
   }

   private String getFeatureBranchName() {
      return ManifestUtil.getAttributeFromManifest(Name.IMPLEMENTATION_TITLE.toString(), "main");
   }

   private String getFeatureBranchBuildOrDate() {
      return ManifestUtil.getAttributeFromManifest(Name.IMPLEMENTATION_VERSION.toString(), "");
   }

   public static void sendHelpMessage(CommandSender sender) {
      HELP_MESSAGE.send(sender);
   }
}
