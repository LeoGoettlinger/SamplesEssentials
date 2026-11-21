package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum FixCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc fix <npc>")
   @Permission({"fancynpcs.command.npc.fix"})
   public void onFix(@NotNull final CommandSender sender, @NotNull final Npc npc) {
      npc.removeForAll();
      npc.create();
      Bukkit.getOnlinePlayers().forEach(npc::checkAndUpdateVisibility);
      this.translator.translate("npc_fix_success").withPrefix().replace("npc", npc.getData().getName()).send(sender);
   }
}
