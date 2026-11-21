package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.ess3.api.IEssentials;
import org.bukkit.Server;

public class EssentialsTreeCommand extends EssentialsCommand {
   private final Map<String, EssentialsTreeNode> nodes = new HashMap<>();
   private final List<String> publicNodes = new ArrayList<>();

   public EssentialsTreeCommand(final String command) {
      super(command);
   }

   protected void registerNode(final EssentialsTreeNode node) {
      for (String name : node.names()) {
         this.nodes.put(name, node);
         if (!node.hidden()) {
            this.publicNodes.add(name);
         }
      }

      node.setEssentials(this.ess);
      node.setEssentialsModule(this.module);
      node.setParent(this);
   }

   @Override
   public void setEssentials(final IEssentials ess) {
      super.setEssentials(ess);

      for (EssentialsTreeNode node : new HashSet<>(this.nodes.values())) {
         node.setEssentials(ess);
      }
   }

   @Override
   public void setEssentialsModule(final IEssentialsModule module) {
      super.setEssentialsModule(module);

      for (EssentialsTreeNode node : new HashSet<>(this.nodes.values())) {
         node.setEssentialsModule(module);
      }
   }

   public void runDefault(final User user, final String commandLabel) throws Exception {
      this.runDefault(user.getSource(), commandLabel);
   }

   public void runDefault(final CommandSource sender, final String commandLabel) throws Exception {
      throw new NotEnoughArgumentsException();
   }

   @Override
   protected final void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      this.run(server, user.getSource(), commandLabel, args);
   }

   @Override
   protected final void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         if (sender.isPlayer() && sender.getUser() != null) {
            this.runDefault((User)sender.getUser(), commandLabel);
         } else {
            this.runDefault(sender, commandLabel);
         }
      } else {
         EssentialsTreeNode node = this.nodes.get(args[0]);
         if (node == null) {
            throw new NotEnoughArgumentsException();
         }

         String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
         if (sender.isPlayer() && sender.getUser() != null) {
            node.run((User)sender.getUser(), commandLabel, newArgs);
         } else {
            node.run(sender, commandLabel, newArgs);
         }
      }
   }

   @Override
   protected final List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return this.getTabCompleteOptions(server, user.getSource(), commandLabel, args);
   }

   @Override
   protected final List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.publicNodes;
      } else {
         EssentialsTreeNode node = this.nodes.get(args[0]);
         if (node != null) {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            return sender.isPlayer() && sender.getUser() != null
               ? node.tabComplete((User)sender.getUser(), commandLabel, newArgs)
               : node.tabComplete(sender, commandLabel, newArgs);
         } else {
            return Collections.emptyList();
         }
      }
   }
}
