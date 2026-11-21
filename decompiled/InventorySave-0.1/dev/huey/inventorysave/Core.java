package dev.huey.inventorysave;

import Commands.Delete;
import Commands.List_;
import Commands.Load;
import Commands.Save;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.crypto.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Core {
   public static Plugin plugin;

   public static boolean isValidString(String input) {
      Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
      Matcher matcher = pattern.matcher(input);
      return matcher.matches();
   }

   public static class CommandBody {
      String name;
      List<String> arguments;
      CommandSender sender;
      boolean _isPlayer = false;
      boolean _isConsole = false;

      CommandBody(CommandSender sender, String name, String[] args) {
         this.sender = sender;
         if (sender instanceof Player) {
            this._isPlayer = true;
         }

         if (sender instanceof ConsoleCommandSender) {
            this._isConsole = true;
         }

         this.name = name;
         this.arguments = Arrays.asList(args);
      }

      public String getName() {
         return this.name;
      }

      public List<String> getArguments() {
         return this.arguments;
      }

      public void reply(Component component) {
         this.sender.sendMessage(component);
      }

      public void reply(String text) {
         this.sender.sendMessage(Component.text(text));
      }

      public void reply(String text, NamedTextColor color) {
         this.sender.sendMessage(Component.text(text).color(color));
      }

      @Nullable
      public CommandSender getSender() {
         return this.sender;
      }

      public boolean isPlayer() {
         return this._isPlayer;
      }

      @Nullable
      public Player getPlayer() {
         return !this._isPlayer ? null : (Player)this.sender;
      }

      public boolean isConsole() {
         return this._isConsole;
      }
   }

   public static class CommandTabCompleteInfo {
      public String commandName;
      public Integer index;
      public List<String> arguments;
   }

   public static class MainCommandsToAPI implements CommandExecutor, TabCompleter {
      List<Core.Subcommand> subcommands = Arrays.asList(new Save(), new Load(), new List_(), new Delete());

      public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if (args.length < 1) {
            return false;
         } else {
            Core.CommandBody body = new Core.CommandBody(sender, args[0], Arrays.copyOfRange(args, 1, args.length));

            for (Core.Subcommand subcommand : this.subcommands) {
               if (subcommand.shouldExecute(body)) {
                  try {
                     subcommand.execute(body);
                  } catch (Exception var9) {
                     sender.sendMessage(Component.text("Error occurred while executing this command.").color(NamedTextColor.RED));
                     var9.printStackTrace();
                     return true;
                  }
               }
            }

            return true;
         }
      }

      @Nullable
      public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if (args.length < 1) {
            return null;
         } else {
            List<String> result = new ArrayList<>();
            if (args.length != 1) {
               Core.CommandTabCompleteInfo info = new Core.CommandTabCompleteInfo();
               info.commandName = args[0];
               info.arguments = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
               info.index = args.length - 2;

               for (Core.Subcommand subcommand : this.subcommands) {
                  if (subcommand instanceof Core.SubcommandTabCompleter completer && completer.shouldComplete(info)) {
                     return completer.complete(info);
                  }
               }

               return null;
            } else {
               List<String> listOfSubcommands = new ArrayList<>();

               for (Core.Subcommand subcommandx : this.subcommands) {
                  listOfSubcommands.add(subcommandx.getName());
               }

               StringUtil.copyPartialMatches(args[0], listOfSubcommands, result);
               return result;
            }
         }
      }
   }

   public interface Subcommand {
      @NotNull
      String getName();

      boolean shouldExecute(Core.CommandBody var1);

      void execute(Core.CommandBody var1) throws IOException;
   }

   public interface SubcommandTabCompleter {
      boolean shouldComplete(Core.CommandTabCompleteInfo var1);

      @Nullable
      List<String> complete(Core.CommandTabCompleteInfo var1);
   }

   public static class fs {
      public boolean write(@NotNull String path, @Nullable Object data) {
         try {
            BukkitObjectOutputStream stream = new BukkitObjectOutputStream(new GZIPOutputStream(Files.newOutputStream(Paths.get(path))));
            stream.writeObject(data);
            stream.close();
            return true;
         } catch (IOException var4) {
            var4.printStackTrace();
            return false;
         }
      }

      @Nullable
      public Data read(@NotNull String path) {
         try {
            BukkitObjectInputStream stream = new BukkitObjectInputStream(new GZIPInputStream(Files.newInputStream(Paths.get(path))));
            Data data = (Data)stream.readObject();
            stream.close();
            return data;
         } catch (IOException | ClassNotFoundException var4) {
            var4.printStackTrace();
            return null;
         }
      }
   }

   public static class inventory {
      public static void save(String id, PlayerInventory inventory) throws IOException {
         File file = new File(Core.plugin.getDataFolder().getAbsolutePath(), id + ".yml");
         FileConfiguration config = YamlConfiguration.loadConfiguration(file);
         config.set("inventory.armors", inventory.getArmorContents());
         config.set("inventory.extras", inventory.getExtraContents());
         config.set("inventory.contents", inventory.getContents());
         config.save(file);
      }

      public static void load(String id, Player pl) throws IOException {
         File file = new File(Core.plugin.getDataFolder().getAbsolutePath(), id + ".yml");
         if (!file.exists()) {
            throw new FileNotFoundException("Failed to load inventory file \"" + id + "\".");
         } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            ItemStack[] armorContents = ((List)config.get("inventory.armors")).toArray(new ItemStack[0]);
            pl.getInventory().setArmorContents(armorContents);
            ItemStack[] extraContents = ((List)config.get("inventory.extras")).toArray(new ItemStack[0]);
            pl.getInventory().setExtraContents(extraContents);
            ItemStack[] contents = ((List)config.get("inventory.contents")).toArray(new ItemStack[0]);
            pl.getInventory().setContents(contents);
         }
      }

      public static boolean delete(String id) throws IOException {
         File file = new File(Core.plugin.getDataFolder().getAbsolutePath(), id + ".yml");
         if (!file.exists()) {
            throw new FileNotFoundException("Failed to load inventory file \"" + id + "\".");
         } else {
            return file.delete();
         }
      }

      public static List<String> list() {
         File[] files = new File(Core.plugin.getDataFolder().getAbsolutePath()).listFiles();
         List<String> result = new ArrayList<>();
         if (files == null) {
            return result;
         } else {
            for (File file : files) {
               result.add(file.getName().lastIndexOf(".") == -1 ? file.getName() : file.getName().substring(0, file.getName().lastIndexOf(".")));
            }

            return result;
         }
      }
   }
}
