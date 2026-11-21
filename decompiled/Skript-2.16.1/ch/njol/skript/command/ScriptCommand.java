package ch.njol.skript.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;
import com.google.common.base.Preconditions;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.lang.script.Script;

public class ScriptCommand implements TabExecutor {
   public static final Message m_executable_by_players = new Message("commands.executable by players");
   public static final Message m_executable_by_console = new Message("commands.executable by console");
   private static final String DEFAULT_PREFIX = "skript";
   final String name;
   private final String label;
   private final List<String> aliases;
   private List<String> activeAliases;
   private String permission;
   private final Expression<? extends Component> permissionMessage;
   private final String description;
   private final String prefix;
   @Nullable
   private final Timespan cooldown;
   private final Expression<? extends Component> cooldownMessage;
   private final String cooldownBypass;
   @Nullable
   private final Expression<String> cooldownStorage;
   final CommandUsage usage;
   private final Trigger trigger;
   private final String pattern;
   private final List<Argument<?>> arguments;
   public static final int PLAYERS = 1;
   public static final int CONSOLE = 2;
   public static final int BOTH = 3;
   final int executableBy;
   private transient PluginCommand bukkitCommand;
   private Map<UUID, Date> lastUsageMap = new HashMap<>();
   @Nullable
   private transient Command overridden = null;
   private transient Map<String, Command> overriddenAliases = new HashMap<>();
   private transient Collection<HelpTopic> helps = new ArrayList<>();

   public ScriptCommand(
      Script script,
      String name,
      String pattern,
      List<Argument<?>> arguments,
      String description,
      @Nullable String prefix,
      String usage,
      List<String> aliases,
      String permission,
      @Nullable VariableString permissionMessage,
      @Nullable Timespan cooldown,
      @Nullable VariableString cooldownMessage,
      String cooldownBypass,
      @Nullable VariableString cooldownStorage,
      int executableBy,
      SectionNode node
   ) {
      this(
         script,
         name,
         pattern,
         arguments,
         description,
         prefix,
         new CommandUsage(null, usage),
         aliases,
         permission,
         permissionMessage,
         cooldown,
         cooldownMessage,
         cooldownBypass,
         cooldownStorage,
         executableBy,
         node
      );
   }

   public ScriptCommand(
      Script script,
      String name,
      String pattern,
      List<Argument<?>> arguments,
      String description,
      @Nullable String prefix,
      CommandUsage usage,
      List<String> aliases,
      String permission,
      @Nullable VariableString permissionMessage,
      @Nullable Timespan cooldown,
      @Nullable VariableString cooldownMessage,
      String cooldownBypass,
      @Nullable VariableString cooldownStorage,
      int executableBy,
      SectionNode node
   ) {
      Preconditions.checkNotNull(name);
      Preconditions.checkNotNull(pattern);
      Preconditions.checkNotNull(arguments);
      Preconditions.checkNotNull(description);
      Preconditions.checkNotNull(usage);
      Preconditions.checkNotNull(aliases);
      Preconditions.checkNotNull(node);
      this.name = name;
      this.label = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(name.toLowerCase(Locale.ENGLISH));
      this.permission = permission;
      if (permissionMessage == null) {
         VariableString defaultMsg = VariableString.newInstance(Language.get("commands.no permission message"));

         assert defaultMsg != null;

         permissionMessage = defaultMsg;
      }

      this.permissionMessage = permissionMessage.getConvertedExpression(Component.class);
      if (prefix != null) {
         for (char c : prefix.toCharArray()) {
            if (Character.isWhitespace(c)) {
               Skript.warning("command /" + name + " has a whitespace in its prefix. Defaulting to 'skript'.");
               prefix = "skript";
               break;
            }

            if (c == 167) {
               Skript.warning("command /" + name + " has a section character in its prefix. Defaulting to 'skript'.");
               prefix = "skript";
               break;
            }
         }
      } else {
         prefix = "skript";
      }

      this.prefix = prefix;
      this.cooldown = cooldown;
      if (cooldownMessage == null) {
         VariableString defaultMsg = VariableString.newInstance(Language.get("commands.cooldown message"));

         assert defaultMsg != null;

         cooldownMessage = defaultMsg;
      }

      this.cooldownMessage = cooldownMessage.getConvertedExpression(Component.class);
      this.cooldownBypass = cooldownBypass;
      this.cooldownStorage = cooldownStorage;
      aliases.removeIf(this.label::equalsIgnoreCase);
      this.aliases = aliases;
      this.activeAliases = new ArrayList<>(aliases);
      TextComponentParser textComponentParser = TextComponentParser.instance();
      this.description = textComponentParser.toLegacyString(textComponentParser.parseSafe(description));
      this.usage = usage;
      this.executableBy = executableBy;
      this.pattern = pattern;
      this.arguments = arguments;
      HintManager hintManager = ParserInstance.get().getHintManager();

      try {
         hintManager.enterScope(false);

         for (Argument<?> argument : arguments) {
            String hintName = argument.getName();
            if (hintName != null) {
               if (!argument.isSingle()) {
                  hintName = hintName + "::*";
               }

               hintManager.set(hintName, argument.getType());
            }
         }

         this.trigger = new Trigger(script, "command /" + name, new SimpleEvent(), ScriptLoader.loadItems(node));
         this.trigger.setLineNumber(node.getLine());
      } finally {
         hintManager.exitScope();
      }

      this.bukkitCommand = this.setupBukkitCommand();
   }

   private PluginCommand setupBukkitCommand() {
      try {
         Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
         c.setAccessible(true);
         PluginCommand bukkitCommand = c.newInstance(this.name, Skript.getInstance());
         bukkitCommand.setAliases(this.aliases);
         bukkitCommand.setDescription(this.description);
         bukkitCommand.setLabel(this.label);
         bukkitCommand.setPermission(this.permission);
         bukkitCommand.setUsage(this.usage.getUsage());
         bukkitCommand.setExecutor(this);
         return bukkitCommand;
      } catch (Exception var3) {
         Skript.outdatedError(var3);
         throw new EmptyStacktraceException();
      }
   }

   public boolean onCommand(@Nullable CommandSender sender, @Nullable Command command, @Nullable String label, @Nullable String[] args) {
      if (sender != null && label != null && args != null) {
         this.execute(sender, label, StringUtils.join(args, " "));
         return true;
      } else {
         return false;
      }
   }

   public boolean execute(CommandSender sender, String commandLabel, String rest) {
      if (sender instanceof Player) {
         if ((this.executableBy & 1) == 0) {
            sender.sendMessage(m_executable_by_console + "");
            return false;
         }
      } else if ((this.executableBy & 2) == 0) {
         sender.sendMessage(m_executable_by_players + "");
         return false;
      }

      ScriptCommandEvent event = new ScriptCommandEvent(this, sender, commandLabel, rest);
      if (!this.checkPermissions(sender, event)) {
         return false;
      } else {
         if (sender instanceof Player && this.cooldown != null) {
            Player player = (Player)sender;
            UUID uuid = player.getUniqueId();
            if (!this.cooldownBypass.isEmpty() && player.hasPermission(this.cooldownBypass)) {
               this.setLastUsage(uuid, event, null);
            } else if (this.getLastUsage(uuid, event) != null) {
               if (this.getRemainingMilliseconds(uuid, event) > 0L) {
                  Component msg = this.cooldownMessage.getSingle(event);
                  if (msg != null) {
                     sender.sendMessage(msg);
                  }

                  return false;
               }

               if (!SkriptConfig.keepLastUsageDates.value()) {
                  this.setLastUsage(uuid, event, null);
               }
            }
         }

         Runnable runnable = () -> {
            Date previousLastUsage = null;
            if (sender instanceof Player) {
               previousLastUsage = this.getLastUsage(((Player)sender).getUniqueId(), event);
            }

            this.execute2(event, sender, commandLabel, rest);
            if (sender instanceof Player && !event.isCooldownCancelled()) {
               Date lastUsage = this.getLastUsage(((Player)sender).getUniqueId(), event);
               if (Objects.equals(lastUsage, previousLastUsage)) {
                  this.setLastUsage(((Player)sender).getUniqueId(), event, new Date());
               }
            }
         };
         if (Bukkit.isPrimaryThread()) {
            runnable.run();
         } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), runnable);
         }

         return true;
      }
   }

   boolean execute2(ScriptCommandEvent event, CommandSender sender, String commandLabel, String rest) {
      ParseLogHandler log = SkriptLogger.startParseLogHandler();

      try {
         boolean ok = SkriptParser.parseArguments(rest, this, event);
         if (!ok) {
            LogEntry e = log.getError();
            if (e != null) {
               sender.sendMessage(ChatColor.DARK_RED + e.toString());
            }

            sender.sendMessage(this.usage.getUsageComponent(event));
            log.clear();
            return false;
         }

         log.clearError();
      } finally {
         log.stop();
      }

      if (Skript.log(Verbosity.VERY_HIGH)) {
         Skript.info("# /" + this.name + " " + rest);
      }

      long startTrigger = System.nanoTime();
      if (!this.trigger.execute(event)) {
         sender.sendMessage(Commands.m_internal_error.toString());
      }

      if (Skript.log(Verbosity.VERY_HIGH)) {
         Skript.info("# " + this.name + " took " + 1.0 * (System.nanoTime() - startTrigger) / 1000000.0 + " milliseconds");
      }

      return true;
   }

   public boolean checkPermissions(CommandSender sender, String commandLabel, String arguments) {
      return this.checkPermissions(sender, new ScriptCommandEvent(this, sender, commandLabel, arguments));
   }

   public boolean checkPermissions(CommandSender sender, Event event) {
      if (!this.permission.isEmpty() && !sender.hasPermission(this.permission)) {
         Component message = this.permissionMessage.getSingle(event);

         assert message != null;

         sender.sendMessage(message);
         return false;
      } else {
         return true;
      }
   }

   public void sendHelp(CommandSender sender) {
      if (!this.description.isEmpty()) {
         sender.sendMessage(this.description);
      }

      sender.sendMessage(ChatColor.GOLD + "Usage" + ChatColor.RESET + ": " + this.usage.getUsage());
   }

   public List<Argument<?>> getArguments() {
      return this.arguments;
   }

   public String getPattern() {
      return this.pattern;
   }

   public void register(SimpleCommandMap commandMap, Map<String, Command> knownCommands, @Nullable Set<String> aliases) {
      synchronized (commandMap) {
         this.overriddenAliases.clear();
         this.overridden = knownCommands.put(this.label, this.bukkitCommand);
         if (aliases != null) {
            aliases.remove(this.label);
         }

         Iterator<String> as = this.activeAliases.iterator();

         while (as.hasNext()) {
            String lowerAlias = as.next().toLowerCase(Locale.ENGLISH);
            if (!knownCommands.containsKey(lowerAlias) || aliases != null && aliases.contains(lowerAlias)) {
               this.overriddenAliases.put(lowerAlias, knownCommands.put(lowerAlias, this.bukkitCommand));
               if (aliases != null) {
                  aliases.add(lowerAlias);
               }
            } else {
               as.remove();
            }
         }

         this.bukkitCommand.setAliases(this.activeAliases);
         commandMap.register(this.prefix, this.bukkitCommand);
      }
   }

   public void unregister(SimpleCommandMap commandMap, Map<String, Command> knownCommands, @Nullable Set<String> aliases) {
      synchronized (commandMap) {
         knownCommands.remove(this.label);
         knownCommands.remove(this.prefix + ":" + this.label);
         if (aliases != null) {
            aliases.removeAll(this.activeAliases);
         }

         for (String alias : this.activeAliases) {
            knownCommands.remove(alias);
            knownCommands.remove(this.prefix + ":" + alias);
         }

         this.activeAliases = new ArrayList<>(this.aliases);
         this.bukkitCommand.unregister(commandMap);
         this.bukkitCommand.setAliases(this.aliases);
         if (this.overridden != null) {
            knownCommands.put(this.label, this.overridden);
            this.overridden = null;
         }

         for (Entry<String, Command> e : this.overriddenAliases.entrySet()) {
            if (e.getValue() != null) {
               knownCommands.put(e.getKey(), e.getValue());
               if (aliases != null) {
                  aliases.add(e.getKey());
               }
            }
         }

         this.overriddenAliases.clear();
      }
   }

   public void registerHelp() {
      this.helps.clear();
      HelpMap help = Bukkit.getHelpMap();
      HelpTopic t = new GenericCommandHelpTopic(this.bukkitCommand);
      help.addTopic(t);
      this.helps.add(t);
      HelpTopic aliases = help.getHelpTopic("Aliases");
      if (aliases instanceof IndexHelpTopic) {
         aliases.getFullText(Bukkit.getConsoleSender());

         try {
            Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
            topics.setAccessible(true);
            ArrayList<HelpTopic> as = new ArrayList<>((Collection<? extends HelpTopic>)topics.get(aliases));

            for (String alias : this.activeAliases) {
               HelpTopic at = new Commands.CommandAliasHelpTopic("/" + alias, "/" + this.getLabel(), help);
               as.add(at);
               this.helps.add(at);
            }

            Collections.sort(as, HelpTopicComparator.helpTopicComparatorInstance());
            topics.set(aliases, as);
         } catch (Exception var9) {
            Skript.outdatedError(var9);
         }
      }
   }

   public void unregisterHelp() {
      Bukkit.getHelpMap().getHelpTopics().removeAll(this.helps);
      HelpTopic aliases = Bukkit.getHelpMap().getHelpTopic("Aliases");
      if (aliases != null && aliases instanceof IndexHelpTopic) {
         try {
            Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
            topics.setAccessible(true);
            ArrayList<HelpTopic> as = new ArrayList<>((Collection<? extends HelpTopic>)topics.get(aliases));
            as.removeAll(this.helps);
            topics.set(aliases, as);
         } catch (Exception var4) {
            Skript.outdatedError(var4);
         }
      }

      this.helps.clear();
   }

   public String getName() {
      return this.name;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public String getLabel() {
      return this.label;
   }

   @Nullable
   public Timespan getCooldown() {
      return this.cooldown;
   }

   @Nullable
   private String getStorageVariableName(Event event) {
      assert this.cooldownStorage != null;

      String variableString = this.cooldownStorage.getSingle(event);
      if (variableString == null) {
         return null;
      } else {
         if (variableString.startsWith("{")) {
            variableString = variableString.substring(1);
         }

         if (variableString.endsWith("}")) {
            variableString = variableString.substring(0, variableString.length() - 1);
         }

         return variableString;
      }
   }

   @Nullable
   public Date getLastUsage(UUID uuid, Event event) {
      if (this.cooldownStorage == null) {
         return this.lastUsageMap.get(uuid);
      } else {
         String name = this.getStorageVariableName(event);

         assert name != null;

         Object variable = Variables.getVariable(name, null, false);
         if (variable != null && !(variable instanceof Date)) {
            Skript.warning(
               "Variable {"
                  + name
                  + "} was not a date! You may be using this variable elsewhere. This warning is letting you know that this variable is now overridden for the command storage."
            );
            return null;
         } else {
            return (Date)variable;
         }
      }
   }

   public void setLastUsage(UUID uuid, Event event, @Nullable Date date) {
      if (this.cooldownStorage != null) {
         String name = this.getStorageVariableName(event);

         assert name != null;

         Variables.setVariable(name, date, null, false);
      } else if (date == null) {
         this.lastUsageMap.remove(uuid);
      } else {
         this.lastUsageMap.put(uuid, date);
      }
   }

   public long getRemainingMilliseconds(UUID uuid, Event event) {
      Date lastUsage = this.getLastUsage(uuid, event);
      if (lastUsage == null) {
         return 0L;
      } else {
         Timespan cooldown = this.cooldown;

         assert cooldown != null;

         long remaining = cooldown.getAs(Timespan.TimePeriod.MILLISECOND) - this.getElapsedMilliseconds(uuid, event);
         if (remaining < 0L) {
            remaining = 0L;
         }

         return remaining;
      }
   }

   public void setRemainingMilliseconds(UUID uuid, Event event, long milliseconds) {
      Timespan cooldown = this.cooldown;

      assert cooldown != null;

      long cooldownMs = cooldown.getAs(Timespan.TimePeriod.MILLISECOND);
      if (milliseconds > cooldownMs) {
         milliseconds = cooldownMs;
      }

      this.setElapsedMilliSeconds(uuid, event, cooldownMs - milliseconds);
   }

   public long getElapsedMilliseconds(UUID uuid, Event event) {
      Date lastUsage = this.getLastUsage(uuid, event);
      return lastUsage == null ? 0L : Date.now().getTime() - lastUsage.getTime();
   }

   public void setElapsedMilliSeconds(UUID uuid, Event event, long milliseconds) {
      Date date = Date.now();
      date.subtract(new Timespan(milliseconds));
      this.setLastUsage(uuid, event, date);
   }

   public String getCooldownBypass() {
      return this.cooldownBypass;
   }

   public List<String> getAliases() {
      return this.aliases;
   }

   public List<String> getActiveAliases() {
      return this.activeAliases;
   }

   public PluginCommand getBukkitCommand() {
      return this.bukkitCommand;
   }

   @Nullable
   public Script getScript() {
      return this.trigger.getScript();
   }

   @Nullable
   public List<String> onTabComplete(@Nullable CommandSender sender, @Nullable Command command, @Nullable String alias, @Nullable String[] args) {
      assert args != null;

      int argIndex = args.length - 1;
      if (argIndex >= this.arguments.size()) {
         return Collections.emptyList();
      } else {
         Argument<?> arg = this.arguments.get(argIndex);
         Class<?> argType = arg.getType();
         return !argType.equals(Player.class) && !argType.equals(OfflinePlayer.class) ? Collections.emptyList() : null;
      }
   }
}
