package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcRemoveEvent;
import de.oliver.fancynpcs.api.events.NpcStopLookingEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum RemoveCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc remove <npc>")
   @Permission({"fancynpcs.command.npc.remove"})
   public void onRemove(@NotNull final CommandSender sender, @NotNull final Npc npc) {
      if (new NpcRemoveEvent(npc, sender).callEvent()) {
         npc.removeForAll();

         for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (npc.getIsLookingAtPlayer().getOrDefault(onlinePlayer.getUniqueId(), false)) {
               npc.getIsLookingAtPlayer().put(onlinePlayer.getUniqueId(), false);
               new NpcStopLookingEvent(npc, onlinePlayer).callEvent();
            }
         }

         FancyNpcs.getInstance().getNpcManagerImpl().removeNpc(npc);
         this.translator.translate("npc_remove_success").withPrefix().replace("npc", npc.getData().getName()).send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
