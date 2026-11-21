package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.events.NpcCreateEvent;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum CopyCMD {
   INSTANCE;

   private static final Pattern NPC_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9/_-]*$");
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command(
      value = "npc copy <npc> <name>",
      requiredSender = Player.class
   )
   @Permission({"fancynpcs.command.npc.copy"})
   public void onCopy(@NotNull final Player sender, @NotNull final Npc npc, @NotNull final String name) {
      if (!NPC_NAME_PATTERN.matcher(name).find()) {
         this.translator.translate("npc_create_failure_invalid_name").withPrefix().replaceStripped("name", name).send(sender);
      } else {
         Npc copied = FancyNpcs.getInstance()
            .getNpcAdapter()
            .apply(
               new NpcData(
                  UUID.randomUUID().toString(),
                  name,
                  sender.getUniqueId(),
                  npc.getData().getDisplayName(),
                  npc.getData().getSkinData(),
                  sender.getLocation().clone(),
                  npc.getData().isShowInTab(),
                  npc.getData().isSpawnEntity(),
                  npc.getData().isCollidable(),
                  npc.getData().isGlowing(),
                  npc.getData().getGlowingColor(),
                  npc.getData().getType(),
                  new ConcurrentHashMap<>(npc.getData().getEquipment()),
                  npc.getData().isTurnToPlayer(),
                  npc.getData().getTurnToPlayerDistance(),
                  npc.getData().getOnClick(),
                  npc.getData().getActions().entrySet().stream().collect(Collectors.toConcurrentMap(Entry::getKey, e -> new ArrayList<>(e.getValue()))),
                  npc.getData().getInteractionCooldown(),
                  npc.getData().getScale(),
                  npc.getData().getVisibilityDistance(),
                  new ConcurrentHashMap<>(npc.getData().getAttributes()),
                  npc.getData().isMirrorSkin()
               )
            );
         if (new NpcCreateEvent(copied, sender).callEvent()) {
            copied.create();
            FancyNpcs.getInstance().getNpcManagerImpl().registerNpc(copied);
            copied.spawnForAll();
            this.translator
               .translate("npc_copy_success")
               .withPrefix()
               .replace("npc", npc.getData().getName())
               .replace("new_npc", copied.getData().getName())
               .send(sender);
         } else {
            this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
         }
      }
   }
}
