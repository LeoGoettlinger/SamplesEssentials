package com.artillexstudios.axrankmenu.commands;

import com.artillexstudios.axrankmenu.AxRankMenu;
import com.artillexstudios.axrankmenu.commands.annotations.Groups;
import com.artillexstudios.axrankmenu.gui.impl.RankGui;
import com.artillexstudios.axrankmenu.hooks.HookManager;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.utils.CommandMessages;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

public class Commands implements OrphanCommand {
   private static BukkitCommandHandler handler = null;

   @DefaultFor({"~", "~ open"})
   @CommandPermission("axrankmenu.use")
   public void open(@NotNull Player sender) {
      new RankGui(sender).open();
   }

   @Subcommand({"reload"})
   @CommandPermission("axrankmenu.reload")
   public void reload(@NotNull CommandSender sender) {
      if (!AxRankMenu.CONFIG.reload()) {
         AxRankMenu.MESSAGEUTILS.sendLang(sender, "reload.failed", Collections.singletonMap("%file%", "tiers.yml"));
      } else if (!AxRankMenu.LANG.reload()) {
         AxRankMenu.MESSAGEUTILS.sendLang(sender, "reload.failed", Collections.singletonMap("%file%", "lang.yml"));
      } else if (!AxRankMenu.RANKS.reload()) {
         AxRankMenu.MESSAGEUTILS.sendLang(sender, "reload.failed", Collections.singletonMap("%file%", "ranks.yml"));
      } else {
         new HookManager().updateHooks();
         AxRankMenu.MESSAGEUTILS.sendLang(sender, "reload.success");
      }
   }

   @Subcommand({"addrank"})
   @CommandPermission("axrankmenu.addrank")
   public void addRank(@NotNull CommandSender sender, @Groups String group) {
      Section section = AxRankMenu.RANKS.getBackingDocument().createSection(group);
      section.set("rank", group);
      section.set("server", "");
      section.set("price", -1.0);
      section.set("currency", "Vault");
      section.set("slot", this.getFirstEmptySlot());
      section.set("item.type", "GRAY_BANNER");
      section.set("item.name", "&#00FF00" + group + " &fRANK");
      section.set(
         "item.lore",
         Arrays.asList(
            " ", " &7- &fPrice: &#00AA00$%price%", " ", "&#00FF00ᴘᴇʀᴍɪssɪᴏɴs", " &7- &f%permission%", " ", "&#00FF00&l(!) &#00FF00Click here to purchase!"
         )
      );
      section.set(
         "buy-actions",
         Arrays.asList(
            "[MESSAGE] &#00FF00You have purchased the &f%name%&#00FF00! &7(%rank%)", "[CONSOLE] lp user %player% parent set " + group, "[CLOSE] menu"
         )
      );
      AxRankMenu.RANKS.save();
      AxRankMenu.MESSAGEUTILS.sendLang(sender, "add-rank", Map.of("%rank%", group));
   }

   private int getFirstEmptySlot() {
      ArrayList<Integer> filled = new ArrayList<>();

      for (String str : AxRankMenu.RANKS.getBackingDocument().getRoutesAsStrings(false)) {
         filled.add(AxRankMenu.RANKS.getInt(str + ".slot", -1));
      }

      for (int i = 0; i < AxRankMenu.RANKS.getInt("rows", 3) * 9; i++) {
         if (!filled.contains(i)) {
            return i;
         }
      }

      return -1;
   }

   public static void registerCommand() {
      if (handler == null) {
         handler = BukkitCommandHandler.create(AxRankMenu.getInstance());
         handler.getAutoCompleter().registerSuggestionFactory(parameter -> parameter.hasAnnotation(Groups.class) ? (args, sender, command) -> {
            LuckPerms luckPerms = LuckPermsProvider.get();
            Set<Group> groups = new HashSet<>(luckPerms.getGroupManager().getLoadedGroups());
            groups.removeIf(group -> {
               for (String str : AxRankMenu.RANKS.getBackingDocument().getRoutesAsStrings(false)) {
                  if (AxRankMenu.RANKS.getString(str + ".rank", "").equalsIgnoreCase(group.getName())) {
                     return true;
                  }
               }

               return false;
            });
            return groups.stream().<String>map(Group::getName).collect(Collectors.toList());
         } : null);
         handler.getTranslator().add(new CommandMessages());
         handler.setLocale(new Locale("en", "US"));
      }

      handler.unregisterAllCommands();
      handler.register(new Object[]{Orphans.path(AxRankMenu.CONFIG.getStringList("command-aliases").toArray(String[]::new)).handler(new Commands())});
      handler.registerBrigadier();
   }
}
