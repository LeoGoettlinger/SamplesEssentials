package de.oliver.fancynpcs.commands.npc;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancylib.translations.message.SimpleMessage;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.utils.Interval;
import de.oliver.fancynpcs.utils.GlowingColor;
import java.text.DecimalFormat;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum InfoCMD {
   INSTANCE;

   private static final DecimalFormat COORDS_FORMAT = new DecimalFormat("#.##");
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc info <npc>")
   @Permission({"fancynpcs.command.npc.info"})
   public void onInfo(@NotNull final CommandSender sender, @NotNull final Npc npc) {
      Location loc = npc.getData().getLocation();
      Interval interactionCooldown = Interval.of((double)npc.getData().getInteractionCooldown(), Interval.Unit.SECONDS);
      int actionsTotal = npc.getData().getActions().values().stream().mapToInt(Collection::size).sum();
      String glowingStateTranslated = npc.getData().isGlowing() && npc.getData().getGlowingColor() != null
         ? ((SimpleMessage)this.translator.translate(GlowingColor.fromAdventure(npc.getData().getGlowingColor()).getTranslationKey())).getMessage()
         : ((SimpleMessage)this.translator.translate("disabled")).getMessage();
      String visibilityDistanceTranslated = npc.getData().getVisibilityDistance() == -1
         ? ((SimpleMessage)this.translator
               .translate("default")
               .replace("value", String.valueOf(FancyNpcs.getInstance().getFancyNpcConfig().getVisibilityDistance())))
            .getMessage()
         : (
            npc.getData().getVisibilityDistance() == 0
               ? ((SimpleMessage)this.translator.translate("not_visible")).getMessage()
               : (
                  npc.getData().getVisibilityDistance() == Integer.MAX_VALUE
                     ? ((SimpleMessage)this.translator.translate("always_visible")).getMessage()
                     : String.valueOf(npc.getData().getVisibilityDistance())
               )
         );
      PlayerProfile creatorProfile = Bukkit.createProfile(npc.getData().getCreator());
      this.translator
         .translate("npc_info_general")
         .replace("name", npc.getData().getName())
         .replace("id", npc.getData().getId())
         .replace("id_short", npc.getData().getId().substring(0, 13) + "...")
         .replace("creator_uuid", npc.getData().getCreator().toString())
         .replace(
            "creator_uuid_short",
            creatorProfile.completeFromCache()
               ? npc.getData().getCreator().toString().substring(0, 13) + "..."
               : npc.getData().getCreator().toString().substring(0, 13)
         )
         .replace(
            "creator_name", creatorProfile.getName() != null ? creatorProfile.getName() : ((SimpleMessage)this.translator.translate("unknown")).getMessage()
         )
         .replace("displayname", npc.getData().getDisplayName())
         .replace("type", "<lang:" + npc.getData().getType().translationKey() + ">")
         .replace("location_x", COORDS_FORMAT.format(loc.x()))
         .replace("location_y", COORDS_FORMAT.format(loc.y()))
         .replace("location_z", COORDS_FORMAT.format(loc.z()))
         .replace("world", loc.getWorld().getName())
         .replace("glow", glowingStateTranslated)
         .replace("is_collidable", this.getTranslatedBoolean(npc.getData().isCollidable()))
         .replace("is_turn_to_player", this.getTranslatedBoolean(npc.getData().isTurnToPlayer()))
         .replace("is_show_in_tab", this.getTranslatedBoolean(npc.getData().isShowInTab()))
         .replace("is_skin_mirror", this.getTranslatedBoolean(npc.getData().isMirrorSkin()))
         .replace("interaction_cooldown", npc.getData().getInteractionCooldown() <= 0.0F ? this.getTranslatedState(false) : interactionCooldown.toString())
         .replace("scale", String.valueOf(npc.getData().getScale()))
         .replace("visibility_distance", visibilityDistanceTranslated)
         .replace("actions_total", String.valueOf(actionsTotal))
         .send(sender);
   }

   @NotNull
   private String getTranslatedBoolean(final boolean bool) {
      return bool ? ((SimpleMessage)this.translator.translate("true")).getMessage() : ((SimpleMessage)this.translator.translate("false")).getMessage();
   }

   @NotNull
   private String getTranslatedState(final boolean bool) {
      return bool ? ((SimpleMessage)this.translator.translate("enabled")).getMessage() : ((SimpleMessage)this.translator.translate("disabled")).getMessage();
   }
}
