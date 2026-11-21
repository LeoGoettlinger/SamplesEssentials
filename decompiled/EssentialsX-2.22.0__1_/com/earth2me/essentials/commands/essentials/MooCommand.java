package com.earth2me.essentials.commands.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.commands.EssentialsTreeNode;
import com.earth2me.essentials.utils.RegistryUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MooCommand extends EssentialsTreeNode {
   private static final Sound MOO_SOUND = RegistryUtil.valueOf(Sound.class, "COW_IDLE", "ENTITY_COW_MILK");
   private static final String[] CONSOLE_MOO = new String[]{
      "         (__)", "         (oo)", "   /------\\/", "  / |    ||", " *  /\\---/\\", "    ~~   ~~", "....\"Have you mooed today?\"..."
   };
   private static final String[] PLAYER_MOO = new String[]{
      "            (__)", "            (oo)", "   /------\\/", "  /  |      | |", " *  /\\---/\\", "    ~~    ~~", "....\"Have you mooed today?\"..."
   };

   public MooCommand() {
      super(new String[]{"moo"}, true);
   }

   @Override
   protected void run(final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1 && args[0].equals("moo")) {
         for (String s : CONSOLE_MOO) {
            this.ess.getLogger().info(s);
         }

         for (Player player : this.ess.getOnlinePlayers()) {
            player.sendMessage(PLAYER_MOO);
            player.playSound(player.getLocation(), MOO_SOUND, 1.0F, 1.0F);
         }
      } else if (sender.isPlayer()) {
         sender.getSender().sendMessage(PLAYER_MOO);
         Player player = sender.getPlayer();
         player.playSound(player.getLocation(), MOO_SOUND, 1.0F, 1.0F);
      } else {
         sender.getSender().sendMessage(CONSOLE_MOO);
      }
   }

   @Override
   protected List<String> tabComplete(CommandSource sender, String commandLabel, String[] args) {
      return (List<String>)(args.length == 1 ? Lists.newArrayList(new String[]{"moo"}) : Collections.emptyList());
   }
}
