package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Flag;
import de.oliver.cloud.annotations.Permission;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import de.oliver.fancynpcs.api.skins.SkinData;
import de.oliver.fancynpcs.api.skins.SkinLoadException;
import de.oliver.fancynpcs.skins.SkinUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum SkinCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc skin <npc> <skin>")
   @Permission({"fancynpcs.command.npc.skin"})
   public void onSkin(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @Argument(suggestions = "SkinCMD/skin") @NotNull final String skin,
      @Flag("slim") final boolean slim
   ) {
      if (npc.getData().getType() != EntityType.PLAYER) {
         this.translator.translate("command_unsupported_npc_type").withPrefix().send(sender);
      } else {
         boolean isMirror = skin.equalsIgnoreCase("@mirror");
         boolean isNone = skin.equalsIgnoreCase("@none");
         if (isMirror) {
            if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.MIRROR_SKIN, true, sender).callEvent()) {
               npc.getData().setMirrorSkin(true);
               npc.removeForAll();
               npc.create();
               npc.spawnForAll();
               this.translator.translate("npc_skin_set_mirror").replace("npc", npc.getData().getName()).withPrefix().send(sender);
            } else {
               this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
            }
         } else if (isNone) {
            if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.SKIN, false, sender).callEvent()
               && new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.SKIN, null, sender).callEvent()) {
               npc.getData().setMirrorSkin(false);
               npc.getData().setSkinData(null);
               npc.removeForAll();
               npc.create();
               npc.spawnForAll();
               this.translator.translate("npc_skin_set_none").withPrefix().replace("npc", npc.getData().getName()).send(sender);
            } else {
               this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
            }
         } else {
            try {
               SkinData.SkinVariant variant = slim ? SkinData.SkinVariant.SLIM : SkinData.SkinVariant.AUTO;
               SkinData skinData = FancyNpcs.getInstance().getSkinManagerImpl().getByIdentifier(skin, variant);
               skinData.setIdentifier(skin);
               if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.SKIN, false, sender).callEvent()
                  && new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.SKIN, skinData, sender).callEvent()) {
                  this.translator
                     .translate("npc_skin_set")
                     .withPrefix()
                     .replace("npc", npc.getData().getName())
                     .replace("name", skinData.getIdentifier())
                     .send(sender);
                  if (!skinData.hasTexture()) {
                     this.translator.translate("npc_skin_set_later").withPrefix().replace("npc", npc.getData().getName()).send(sender);
                  }

                  npc.getData().setMirrorSkin(false);
                  npc.getData().setSkinData(skinData);
                  npc.removeForAll();
                  npc.create();
                  npc.spawnForAll();
               } else {
                  this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
               }
            } catch (SkinLoadException var9) {
               switch (var9.getReason()) {
                  case INVALID_URL:
                     this.translator.translate("npc_skin_failure_invalid_url").withPrefix().replace("npc", npc.getData().getName()).send(sender);
                     break;
                  case INVALID_FILE:
                     this.translator.translate("npc_skin_failure_invalid_file").withPrefix().replace("npc", npc.getData().getName()).send(sender);
                     break;
                  case INVALID_USERNAME:
                     this.translator.translate("npc_skin_failure_invalid_username").withPrefix().replace("npc", npc.getData().getName()).send(sender);
                     break;
                  case INVALID_PLACEHOLDER:
                     this.translator.translate("npc_skin_failure_invalid_placeholder").withPrefix().replace("npc", npc.getData().getName()).send(sender);
               }
            }
         }
      }
   }

   @Suggestions("SkinCMD/skin")
   public List<String> suggestSkin(final CommandContext<CommandSender> context, final CommandInput input) {
      return new ArrayList<String>() {
         {
            Objects.requireNonNull(SkinCMD.this);
            this.add("@none");
            this.add("@mirror");
            Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(this::add);
            File[] files = new File(FancyNpcs.getInstance().getDataFolder(), "skins").listFiles();
            if (files != null) {
               Arrays.stream(files).map(File::getName).filter(SkinUtils::isFile).forEach(this::add);
            }
         }
      };
   }
}
