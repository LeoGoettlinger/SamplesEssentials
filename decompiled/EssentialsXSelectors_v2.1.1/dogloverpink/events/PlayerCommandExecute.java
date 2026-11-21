package dogloverpink.events;

import dogloverpink.EssentialsXTargetSelectors;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandExecute implements Listener {
   public static EssentialsXTargetSelectors plugin;
   public static ConversationFactory conversationFactory = new ConversationFactory(plugin);
   public static String lastCommand;
   public static Prompt noChat = new StringPrompt() {
      public String getPromptText(ConversationContext context) {
         return PlayerCommandExecute.lastCommand;
      }

      public Prompt acceptInput(ConversationContext context, String input) {
         return Prompt.END_OF_CONVERSATION;
      }
   };

   @EventHandler
   public void onPlayerCommandExecute(PlayerCommandPreprocessEvent event) {
      String[] args = event.getMessage().split("\\s+");
      PluginCommand command = Bukkit.getPluginCommand(args[0].replaceFirst("/", ""));
      if (command != null && command.getPlugin().getName().contains("Essentials")) {
         Player plr = event.getPlayer();
         if (plr.hasPermission("essxselectors.use")) {
            int argNumWithSelector = -1;

            for (int i = 0; i < args.length; i++) {
               if (args[i].startsWith("@")) {
                  argNumWithSelector = i;
               }
            }

            if (argNumWithSelector != -1) {
               String fullCommand = "";
               StringBuilder sb = new StringBuilder();

               for (int ix = 0; ix < args.length; ix++) {
                  if (ix == argNumWithSelector) {
                     sb.append("%TARGET% ");
                  } else {
                     sb.append(args[ix] + " ");
                  }
               }

               fullCommand = sb.toString().trim();
               if (!args[argNumWithSelector].toLowerCase().startsWith("@p") || plr.hasPermission("essxselectors.closest")) {
                  if (!args[argNumWithSelector].toLowerCase().startsWith("@s") || plr.hasPermission("essxselectors.self")) {
                     if (!args[argNumWithSelector].toLowerCase().startsWith("@a") || plr.hasPermission("essxselectors.all")) {
                        if (!args[argNumWithSelector].toLowerCase().startsWith("@r") || plr.hasPermission("essxselectors.random")) {
                           List<Player> players = new ArrayList<>();

                           try {
                              List<Entity> entities = Bukkit.selectEntities(event.getPlayer(), args[argNumWithSelector]);
                              entities.forEach(entity -> {
                                 if (entity instanceof Player) {
                                    players.add((Player)entity);
                                 }
                              });
                           } catch (IllegalArgumentException var14) {
                              plr.sendMessage("§c" + var14.getCause().getMessage());
                              event.setCancelled(true);
                              return;
                           }

                           if (!players.isEmpty()) {
                              event.setCancelled(true);
                              if (args[argNumWithSelector].startsWith("@a") && players.size() > 1) {
                                 Conversation conversation = conversationFactory.buildConversation(plr);
                                 lastCommand = "§eRan §a" + fullCommand.replace("%TARGET%", args[argNumWithSelector]);
                                 int count = 0;

                                 for (Player target : players) {
                                    String cmdToRun = fullCommand.replace("%TARGET%", target.getName()).trim();
                                    cmdToRun = cmdToRun.substring(1);
                                    plr.performCommand(cmdToRun);
                                    if (count == 0) {
                                       conversationFactory.withFirstPrompt(noChat).withEscapeSequence("cancel");
                                       conversation.begin();
                                    }
                                 }

                                 conversation.abandon();
                              } else {
                                 for (Player targetx : players) {
                                    String cmdToRun = fullCommand.replace("%TARGET%", targetx.getName()).trim();
                                    cmdToRun = cmdToRun.substring(1);
                                    plr.performCommand(cmdToRun);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
