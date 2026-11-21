package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public enum TypeCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc type <npc> <type>")
   @Permission({"fancynpcs.command.npc.type"})
   public void onType(@NotNull final CommandSender sender, @NotNull final Npc npc, @NotNull final EntityType type) {
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.TYPE, type, sender).callEvent()) {
         npc.getData().clearAttributes();
         npc.getData().setType(type);
         if (type != EntityType.PLAYER) {
            npc.getData().setShowInTab(false);
            npc.getData().setSkinData(null);
            npc.getData().setMirrorSkin(false);
         }

         if (!type.isAlive() && npc.getData().getEquipment() != null) {
            npc.getData().getEquipment().clear();
         }

         if (type == EntityType.ENDER_DRAGON) {
            npc.removeForAll();
            npc.create();
            Bukkit.getOnlinePlayers().forEach(npc::checkAndUpdateVisibility);
         } else {
            FancyNpcsPlugin.get().getNpcThread().submit(() -> {
               npc.removeForAll();
               npc.create();
               Bukkit.getOnlinePlayers().forEach(npc::checkAndUpdateVisibility);
            });
         }

         this.translator
            .translate("npc_type_success")
            .withPrefix()
            .replace("npc", npc.getData().getName())
            .replace("type", type.name().toLowerCase())
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }
}
