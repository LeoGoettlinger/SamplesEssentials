package ch.njol.skript.command;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.SkriptColor;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public class CommandHelp {
   private static final String DEFAULTENTRY = "description";
   private static final ArgsMessage m_invalid_argument = new ArgsMessage("commands.invalid argument");
   private static final Message m_usage = new Message("skript command.usage");
   private final String actualCommand;
   private final String actualNode;
   private final String argsColor;
   private String command;
   private String langNode;
   private boolean revalidate = true;
   @Nullable
   private Message description = null;
   private final Map<String, Object> arguments = new LinkedHashMap<>();
   @Nullable
   private CommandHelp.ArgumentHolder wildcardArg = null;

   public CommandHelp(String command, SkriptColor argsColor, String langNode) {
      this(command, argsColor.getFormattedChat(), langNode);
   }

   public CommandHelp(String command, SkriptColor argsColor) {
      this(command, argsColor.getFormattedChat(), command);
   }

   private CommandHelp(String command, String argsColor, String node) {
      this.actualCommand = this.command = command;
      this.actualNode = this.langNode = node;
      this.argsColor = argsColor;
   }

   public CommandHelp add(String argument) {
      CommandHelp.ArgumentHolder holder = new CommandHelp.ArgumentHolder(argument);
      if (argument.startsWith("<") && argument.endsWith(">")) {
         argument = ChatColor.GRAY + "<" + this.argsColor + argument.substring(1, argument.length() - 1) + ChatColor.GRAY + ">";
         this.wildcardArg = holder;
      }

      this.arguments.put(argument, holder);
      return this;
   }

   public CommandHelp add(CommandHelp help) {
      this.arguments.put(help.command, help);
      help.onAdd(this);
      return this;
   }

   protected void onAdd(CommandHelp parent) {
      this.langNode = parent.langNode + "." + this.actualNode;
      this.command = parent.command + " " + parent.argsColor + this.actualCommand;
      this.revalidate = true;

      for (Entry<String, Object> entry : this.arguments.entrySet()) {
         if (entry.getValue() instanceof CommandHelp) {
            ((CommandHelp)entry.getValue()).onAdd(this);
         } else {
            ((CommandHelp.ArgumentHolder)entry.getValue()).revalidate = true;
         }
      }
   }

   public boolean test(CommandSender sender, String[] args) {
      return this.test(sender, args, 0);
   }

   private boolean test(CommandSender sender, String[] args, int index) {
      if (index >= args.length) {
         this.showHelp(sender);
         return false;
      } else {
         Object help = this.arguments.get(args[index].toLowerCase(Locale.ENGLISH));
         if (help == null && this.wildcardArg == null) {
            this.showHelp(sender, m_invalid_argument.toString(this.argsColor + args[index]));
            return false;
         } else {
            return !(help instanceof CommandHelp) || ((CommandHelp)help).test(sender, args, index + 1);
         }
      }
   }

   public void showHelp(CommandSender sender) {
      this.showHelp(sender, m_usage.toString());
   }

   private void showHelp(CommandSender sender, String pre) {
      Skript.message(sender, pre + " " + this.command + " " + this.argsColor + "...");

      for (Entry<String, Object> entry : this.arguments.entrySet()) {
         Skript.message(sender, "  " + this.argsColor + entry.getKey() + " " + ChatColor.GRAY + "-" + ChatColor.RESET + " " + entry.getValue());
      }
   }

   @Override
   public String toString() {
      if (this.revalidate) {
         this.description = new Message(this.langNode + ".description");
         this.revalidate = false;
      }

      return this.description + "";
   }

   private class ArgumentHolder {
      private final String argument;
      private boolean revalidate = true;
      @Nullable
      private Message description = null;

      private ArgumentHolder(String argument) {
         this.argument = argument;
      }

      @Override
      public String toString() {
         if (this.revalidate) {
            this.description = new Message(CommandHelp.this.langNode + "." + this.argument);
            this.revalidate = false;
         }

         return this.description + "";
      }
   }
}
