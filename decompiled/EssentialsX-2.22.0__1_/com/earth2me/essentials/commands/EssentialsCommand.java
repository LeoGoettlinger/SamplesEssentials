package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ess3.api.IEssentials;
import net.ess3.api.TranslatableException;
import net.ess3.provider.KnownCommandsProvider;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

public abstract class EssentialsCommand implements IEssentialsCommand {
   public static final List<String> COMMON_DURATIONS = ImmutableList.of("1", "60", "600", "3600", "86400");
   protected static final List<String> COMMON_DATE_DIFFS = ImmutableList.of("1m", "15m", "1h", "3h", "12h", "1d", "1w", "1mo", "1y");
   private static final Pattern ARGUMENT_PATTERN = Pattern.compile("([ :>])(([\\[<])[A-Za-z |]+[>\\]])");
   private final transient String name;
   private final transient Map<String, String> usageStrings = new LinkedHashMap<>();
   protected transient IEssentials ess;
   protected transient IEssentialsModule module;

   protected EssentialsCommand(final String name) {
      this.name = name;
      int i = 1;

      try {
         while (true) {
            String baseKey = name + "CommandUsage" + i;
            this.addUsageString(I18n.tlLiteral(baseKey), baseKey + "Description");
            i++;
         }
      } catch (MissingResourceException var4) {
      }
   }

   private void addUsageString(final String usage, final String description) {
      StringBuffer buffer = new StringBuffer();
      Matcher matcher = ARGUMENT_PATTERN.matcher(usage);

      while (matcher.find()) {
         String color = matcher.group(3).equals("<") ? I18n.tlLiteral("commandArgumentRequired") : I18n.tlLiteral("commandArgumentOptional");
         matcher.appendReplacement(buffer, "$1" + color + matcher.group(2).replace("|", I18n.tlLiteral("commandArgumentOr") + "|" + color) + "<reset>");
      }

      matcher.appendTail(buffer);
      this.usageStrings.put(buffer.toString(), description);
   }

   @Override
   public Map<String, String> getUsageStrings() {
      return this.usageStrings;
   }

   public static String getFinalArg(final String[] args, final int start) {
      StringBuilder bldr = new StringBuilder();

      for (int i = start; i < args.length; i++) {
         if (i != start) {
            bldr.append(" ");
         }

         bldr.append(args[i]);
      }

      return bldr.toString();
   }

   private boolean canInteractWith(final User interactor, final User interactee) {
      return this.ess.canInteractWith(interactor, interactee);
   }

   @Override
   public void setEssentials(final IEssentials ess) {
      this.ess = ess;
   }

   @Override
   public void setEssentialsModule(final IEssentialsModule module) {
      this.module = module;
   }

   @Override
   public String getName() {
      return this.name;
   }

   protected User getPlayer(final Server server, final CommandSource sender, final String[] args, final int pos) throws PlayerNotFoundException, NotEnoughArgumentsException {
      return this.getPlayer(server, sender, args, pos, false);
   }

   protected User getPlayer(final Server server, final CommandSource sender, final String[] args, final int pos, final boolean getOffline) throws PlayerNotFoundException, NotEnoughArgumentsException {
      if (sender.isPlayer()) {
         User user = this.ess.getUser(sender.getPlayer());
         return this.getPlayer(server, user, args, pos, getOffline);
      } else {
         return this.getPlayer(server, args, pos, true, getOffline);
      }
   }

   protected User getPlayer(final Server server, final CommandSource sender, final String searchTerm) throws PlayerNotFoundException, NotEnoughArgumentsException {
      if (sender.isPlayer()) {
         User user = this.ess.getUser(sender.getPlayer());
         return this.getPlayer(server, user, searchTerm, user.canInteractVanished(), false);
      } else {
         return this.getPlayer(server, searchTerm, true, false);
      }
   }

   protected User getPlayer(final Server server, final User user, final String[] args, final int pos) throws PlayerNotFoundException, NotEnoughArgumentsException {
      return this.getPlayer(server, user, args, pos, false);
   }

   protected User getPlayer(final Server server, final User user, final String[] args, final int pos, final boolean getOffline) throws PlayerNotFoundException, NotEnoughArgumentsException {
      return this.getPlayer(server, user, args, pos, user.canInteractVanished(), getOffline);
   }

   protected User getPlayer(final Server server, final String[] args, final int pos, final boolean getHidden, final boolean getOffline) throws PlayerNotFoundException, NotEnoughArgumentsException {
      return this.getPlayer(server, null, args, pos, getHidden, getOffline);
   }

   User getPlayer(final Server server, final User sourceUser, final String[] args, final int pos, final boolean getHidden, final boolean getOffline) throws PlayerNotFoundException, NotEnoughArgumentsException {
      if (args.length <= pos) {
         throw new NotEnoughArgumentsException();
      } else if (args[pos].isEmpty()) {
         throw new PlayerNotFoundException();
      } else {
         return this.getPlayer(server, sourceUser, args[pos], getHidden, getOffline);
      }
   }

   protected User getPlayer(final Server server, final String searchTerm, final boolean getHidden, final boolean getOffline) throws PlayerNotFoundException {
      return this.getPlayer(server, null, searchTerm, getHidden, getOffline);
   }

   private User getPlayer(final Server server, final User sourceUser, final String searchTerm, final boolean getHidden, final boolean getOffline) throws PlayerNotFoundException {
      return this.ess.matchUser(server, sourceUser, searchTerm, getHidden, getOffline);
   }

   @Override
   public final void run(final Server server, final User user, final String commandLabel, final Command cmd, final String[] args) throws Exception {
      Trade charge = new Trade(this.getName(), this.ess);
      charge.isAffordableFor(user);
      this.run(server, user, commandLabel, args);
      charge.charge(user);
   }

   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      this.run(server, user.getSource(), commandLabel, args);
   }

   @Override
   public final void run(final Server server, final CommandSource sender, final String commandLabel, final Command cmd, final String[] args) throws Exception {
      this.run(server, sender, commandLabel, args);
   }

   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      throw new TranslatableException("onlyPlayers", commandLabel);
   }

   @Override
   public final List<String> tabComplete(final Server server, final User user, final String commandLabel, final Command cmd, final String[] args) {
      if (args.length == 0) {
         return Collections.emptyList();
      } else {
         List<String> options = this.getTabCompleteOptions(server, user, commandLabel, args);
         return options == null ? null : (List)StringUtil.copyPartialMatches(args[args.length - 1], options, Lists.newArrayList());
      }
   }

   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return this.getTabCompleteOptions(server, user.getSource(), commandLabel, args);
   }

   @Override
   public final List<String> tabComplete(final Server server, final CommandSource sender, final String commandLabel, final Command cmd, final String[] args) {
      if (args.length == 0) {
         return Collections.emptyList();
      } else {
         List<String> options = this.getTabCompleteOptions(server, sender, commandLabel, args);
         return options == null ? null : (List)StringUtil.copyPartialMatches(args[args.length - 1], options, Lists.newArrayList());
      }
   }

   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return this.getPlayers(sender);
   }

   boolean canInteractWith(final CommandSource interactor, final User interactee) {
      return this.ess.canInteractWith(interactor, interactee);
   }

   protected List<String> getPlayers(final CommandSource interactor) {
      List<String> players = Lists.newArrayList();

      for (User user : this.ess.getOnlineUsers()) {
         if (this.canInteractWith(interactor, user)) {
            players.add(this.ess.getSettings().changeTabCompleteName() ? FormatUtil.stripFormat(user.getDisplayName()) : user.getName());
         }
      }

      return players;
   }

   protected List<String> getPlayers(final User interactor) {
      return this.getPlayers(interactor.getSource());
   }

   protected List<String> getItems() {
      return new ArrayList<>(this.ess.getItemDb().listNames());
   }

   protected List<String> getMatchingItems(final String arg) {
      List<String> items = Lists.newArrayList(new String[]{"hand", "inventory", "blocks"});
      if (!arg.isEmpty()) {
         items.addAll(this.getItems());
      }

      return items;
   }

   protected final List<String> getCommands(Server server) {
      Map<String, Command> commandMap = Maps.newHashMap(this.ess.provider(KnownCommandsProvider.class).getKnownCommands());
      List<String> commands = Lists.newArrayListWithCapacity(commandMap.size());

      for (Command command : commandMap.values()) {
         if (command instanceof PluginIdentifiableCommand) {
            commands.add(command.getName());
         }
      }

      return commands;
   }

   protected final List<String> getPlugins(final Server server) {
      List<String> plugins = Lists.newArrayList();

      for (Plugin p : server.getPluginManager().getPlugins()) {
         plugins.add(p.getName());
      }

      return plugins;
   }

   protected final List<String> tabCompleteCommand(final CommandSource sender, final Server server, final String label, final String[] args, final int index) {
      Command command = server.getPluginCommand(label);
      if (command == null) {
         return Collections.emptyList();
      } else {
         int numArgs = args.length - index - 1;
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info(numArgs + " " + index + " " + Arrays.toString((Object[])args));
         }

         String[] effectiveArgs = new String[numArgs];
         System.arraycopy(args, index, effectiveArgs, 0, numArgs);
         if (effectiveArgs.length == 0) {
            effectiveArgs = new String[]{""};
         }

         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info(command + " -- " + Arrays.toString((Object[])effectiveArgs));
         }

         return command.tabComplete(sender.getSender(), label, effectiveArgs);
      }
   }

   @Override
   public void showError(final CommandSender sender, final Throwable throwable, final String commandLabel) {
      this.ess.showError(new CommandSource((Essentials)this.ess, sender), throwable, commandLabel);
   }

   public CompletableFuture<Boolean> getNewExceptionFuture(final CommandSource sender, final String commandLabel) {
      CompletableFuture<Boolean> future = new CompletableFuture<>();
      future.exceptionally(e -> {
         this.showError(sender.getSender(), e, commandLabel);
         return false;
      });
      return future;
   }
}
