package dev.vatten.baserad.commands;

import dev.vatten.baserad.Plugin;
import dev.vatten.baserad.TagStore;
import dev.vatten.baserad.TextFormatter;
import dev.vatten.baserad.VattenPlayer;
import dev.vatten.baserad.interfaces.RenderableComponent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TagsCommand extends Command {
   private final GenerateCommand generateCommand;
   private final Plugin plugin;

   public TagsCommand(Plugin plugin, Path importDirectory) {
      super("tags", List.of("fancytags"));
      this.generateCommand = new GenerateCommand(plugin, importDirectory);
      this.plugin = plugin;
   }

   @Override
   public void execute(VattenPlayer player, String[] args) {
      TagStore tagStore = this.plugin.getTagStore();
      if (args.length >= 1) {
         if (args[0].equalsIgnoreCase("reload")) {
            this.plugin.getApi().reload();
            player.sendMessage(this.plugin.getPluginTextFormatter().format(TextFormatter.SUCCESS.format("Tags plugin has been reloaded!")));
            return;
         }

         if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage(this.plugin.getPluginTextFormatter().format(TextFormatter.INFO.format("Listing all registered tags:")));
            player.sendMessage(this.plugin.getInterfaces().createTagListInterface(tagStore).asComponent());
            return;
         }

         if (args[0].equalsIgnoreCase("test")) {
            player.sendMessage(Component.empty());
            player.sendMessage(
               tagStore.getTag("team")
                  .asComponent()
                  .color(TextColor.color(16711680))
                  .append(Component.text(" Vattendroppen236: I'm on the red team").color(NamedTextColor.WHITE))
            );
            player.sendMessage(
               tagStore.getTag("team")
                  .asComponent()
                  .color(TextColor.color(65280))
                  .append(Component.text(" Vattendroppen236: I'm on the green team").color(NamedTextColor.WHITE))
            );
            player.sendMessage(
               tagStore.getTag("team")
                  .asComponent()
                  .color(TextColor.color(255))
                  .append(Component.text(" Vattendroppen236: I'm on the blue team").color(NamedTextColor.WHITE))
            );
            player.sendMessage(
               tagStore.getTag("team")
                  .asComponent()
                  .color(TextColor.color(16776960))
                  .append(Component.text(" Vattendroppen236: I'm on the yellow team").color(NamedTextColor.WHITE))
            );
            player.sendMessage(Component.empty());
            player.sendMessage(Component.empty());
            player.sendMessage(tagStore.getTag("owner").asComponent().append(Component.text(" Vattendroppen236: i'm the owner")));
            player.sendMessage(tagStore.getTag("helper").asComponent().append(Component.text(" Vattendroppen237: W")));
            player.sendMessage(
               tagStore.getTag("twitch")
                  .asComponent()
                  .append(tagStore.getTag("dev").asComponent())
                  .append(Component.text(" Vattendroppen238: yo nice rank Vattendroppen239"))
            );
            player.sendMessage(tagStore.getTag("admin").asComponent().append(Component.text(" Vattendroppen239: thanks")));
            player.sendMessage(tagStore.getTag("player").asComponent().append(Component.text(" Steve: Wait how are there so many of you")));
            player.sendMessage(
               tagStore.getTag("player")
                  .asComponent()
                  .append(Component.text(" Steve was banned by "))
                  .append(tagStore.getTag("mod").asComponent().append(Component.text(" Vattendroppen240")))
            );
            player.sendMessage(tagStore.getTag("builder").asComponent().append(Component.text(" Vattendroppen241: lmao")));
            player.sendMessage(tagStore.getTag("youtube").asComponent().append(Component.text(" Vattendroppen242: say hi to youtube")));
            player.sendMessage(Component.empty());
            player.sendMessage(Component.empty());
            player.sendMessage(
               RenderableComponent.centered().component(this.plugin.getInternalTagStore().getTag("fancytags_logo").asComponent()).build().asComponent()
            );
            player.sendMessage(Component.empty());
            player.sendMessage(
               RenderableComponent.centered()
                  .component(
                     ((Builder)((Builder)((Builder)((Builder)Component.text()
                                    .append(this.plugin.getInternalTagStore().getTag("simple").asComponent().shadowColor(ShadowColor.shadowColor(0, 0, 0, 255))))
                                 .append(Component.text(" & ")))
                              .append(this.plugin.getInternalTagStore().getTag("stylish").asComponent()))
                           .append(Component.text(" tags")))
                        .build()
                  )
                  .build()
                  .asComponent()
            );
            player.sendMessage(
               RenderableComponent.centered()
                  .component(
                     ((Builder)((Builder)Component.text().append(Component.text("100%", Style.style(TextDecoration.BOLD)))).append(Component.text(" Vanilla")))
                        .build()
                  )
                  .build()
                  .asComponent()
            );
            player.sendMessage(Component.empty());
            return;
         }

         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("view")) {
               if (!tagStore.getTagNames().contains(args[1])) {
                  player.sendMessage(TextFormatter.WARN.format("No tag corresponding to the name '" + args[1] + "' was found."));
                  return;
               }

               player.sendMessage(
                  this.plugin.getPluginTextFormatter().format(TextFormatter.INFO.format("Viewing information about the tag '" + args[1] + "'."))
               );
               player.sendMessage(this.plugin.getInterfaces().createTagInterface(args[1]).asComponent());
               return;
            }

            if (args[0].equalsIgnoreCase("delete")) {
               if (!tagStore.getTagNames().contains(args[1])) {
                  player.sendMessage(TextFormatter.WARN.format("No tag corresponding to the name '" + args[1] + "' was found."));
                  return;
               }

               player.sendMessage(
                  this.plugin
                     .getInterfaces()
                     .createConfirmInterface(
                        Component.textOfChildren(
                           new ComponentLike[]{
                              Component.text("delete '" + args[1] + "' ("),
                              TextFormatter.RESET.format(tagStore.getTag(args[1]).asComponent()),
                              Component.text(")")
                           }
                        ),
                        audience -> {
                           tagStore.removeTag(args[1]);
                           player.sendMessage(this.plugin.getPluginTextFormatter().format(TextFormatter.SUCCESS.format("Tag '" + args[1] + "' was deleted.")));
                        }
                     )
                     .asComponent()
               );
               return;
            }
         }

         if (args.length == 3 && args[0].equalsIgnoreCase("rename")) {
            if (!tagStore.getTagNames().contains(args[1])) {
               player.sendMessage(TextFormatter.WARN.format("No tag corresponding to the name '" + args[1] + "' was found."));
               return;
            }

            if (tagStore.getTagNames().contains(args[2])) {
               player.sendMessage(TextFormatter.WARN.format("A tag with the name '" + args[2] + "' already exists."));
               return;
            }

            player.sendMessage(
               this.plugin
                  .getInterfaces()
                  .createConfirmInterface(
                     Component.textOfChildren(
                        new ComponentLike[]{
                           Component.text("rename '" + args[1] + "' ("),
                           TextFormatter.RESET.format(tagStore.getTag(args[1]).asComponent()),
                           Component.text(") to '" + args[2] + "'")
                        }
                     ),
                     audience -> {
                        if (!this.plugin.getTagStore().renameTag(args[1], args[2])) {
                           player.sendMessage(TextFormatter.WARN.format("Tags changed before confirmation; nothing was renamed."));
                        } else {
                           player.sendMessage(
                              this.plugin
                                 .getPluginTextFormatter()
                                 .format(TextFormatter.SUCCESS.format("Tag '" + args[1] + "' was renamed to '" + args[2] + "'."))
                           );
                        }
                     }
                  )
                  .asComponent()
            );
            return;
         }

         if (args[0].equalsIgnoreCase("generate")) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            this.generateCommand.execute(player, newArgs);
            return;
         }
      }

      player.sendMessage(TextFormatter.SEVERE.format(Component.text("You're it!")));
   }

   @Override
   public List<String> onTabComplete(VattenPlayer player, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length <= 1) {
         completions.addAll(List.of("reload", "list", "view", "rename", "delete"));
         completions.add(this.generateCommand.getName());
      }

      if (args.length == 2 && List.of("view", "rename", "delete").contains(args[0].toLowerCase())) {
         completions.addAll(this.plugin.getTagStore().getTagNames());
      }

      if (args.length > 1) {
         List<String> check = new ArrayList<>();
         check.add(this.generateCommand.getName());
         if (check.contains(args[0])) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            return this.generateCommand.onTabComplete(player, newArgs);
         }
      }

      return completions.stream().filter(s -> args.length == 0 || s.startsWith(args[args.length - 1].toLowerCase())).toList();
   }
}
