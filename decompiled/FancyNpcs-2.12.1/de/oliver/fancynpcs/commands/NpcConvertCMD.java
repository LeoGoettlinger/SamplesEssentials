package de.oliver.fancynpcs.commands;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancyanalytics.sdk.events.Event;
import de.oliver.fancylib.ReflectionUtils;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.utils.NpcEquipmentSlot;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.trait.CommandTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.ScoreboardTrait;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.trait.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public final class NpcConvertCMD {
   public static final NpcConvertCMD INSTANCE = new NpcConvertCMD();
   private final FancyNpcs plugin = FancyNpcs.getInstance();
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   private NpcConvertCMD() {
   }

   @Command("npcconvert citizens")
   @Permission({"fancynpcs.command.npcconvert.citizens"})
   public void onCitizens(final CommandSender sender) {
      if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
         this.translator.translate("npcconvert_plugin_not_found").withPrefix().replace("source_plugin", "Citizens").send(sender);
      } else {
         NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
         int convertedCount = 0;

         for (NPC npc : npcRegistry) {
            Owner ownerTrait = (Owner)npc.getTraitNullable(Owner.class);
            UUID ownerUUID = ownerTrait != null ? ownerTrait.getOwnerId() : null;
            String name = npc.getName().replaceAll(" ", "_");
            Location loc = npc.getStoredLocation();
            if (loc == null) {
               loc = npc.getEntity().getLocation();
            }

            NpcData data = new NpcData(name, ownerUUID, loc);
            MobType mobTypeTrait = (MobType)npc.getTraitNullable(MobType.class);
            if (mobTypeTrait != null) {
               data.setType(mobTypeTrait.getType());
            }

            SkinTrait skinTrait = (SkinTrait)npc.getTraitNullable(SkinTrait.class);
            if (skinTrait != null && skinTrait.getSkinName() != null) {
               String skinName = skinTrait.getSkinName();
               data.setSkin(skinName);
            }

            LookClose lookCloseTrait = (LookClose)npc.getTraitNullable(LookClose.class);
            if (lookCloseTrait != null && lookCloseTrait.isEnabled()) {
               data.setTurnToPlayer(true);
               data.setTurnToPlayerDistance((int)lookCloseTrait.getRange());
            }

            ScoreboardTrait scoreboardTrait = (ScoreboardTrait)npc.getTraitNullable(ScoreboardTrait.class);
            if (scoreboardTrait != null && scoreboardTrait.getColor() != null) {
               NamedTextColor color = (NamedTextColor)NamedTextColor.NAMES.value(scoreboardTrait.getColor().name().toLowerCase());
               if (color != null) {
                  data.setGlowing(true);
                  data.setGlowingColor(color);
               }
            }

            Equipment equipmentTrait = (Equipment)npc.getTraitNullable(Equipment.class);
            if (equipmentTrait != null) {
               for (Entry<EquipmentSlot, ItemStack> entry : equipmentTrait.getEquipmentBySlot().entrySet()) {
                  if (entry.getValue() != null) {
                     NpcEquipmentSlot fnSlot = switch ((EquipmentSlot)entry.getKey()) {
                        case HAND -> NpcEquipmentSlot.MAINHAND;
                        case OFF_HAND -> NpcEquipmentSlot.OFFHAND;
                        case BOOTS -> NpcEquipmentSlot.FEET;
                        case LEGGINGS -> NpcEquipmentSlot.LEGS;
                        case CHESTPLATE, BODY -> NpcEquipmentSlot.CHEST;
                        case HELMET -> NpcEquipmentSlot.HEAD;
                        default -> null;
                     };
                     if (fnSlot != null) {
                        data.addEquipment(fnSlot, entry.getValue().clone());
                     }
                  }
               }
            }

            int actionIndex = 1;
            Text textTrait = (Text)npc.getTraitNullable(Text.class);
            if (textTrait != null) {
               NpcAction action = FancyNpcs.getInstance().getActionManager().getActionByName("message");

               for (String text : textTrait.getTexts()) {
                  data.addAction(ActionTrigger.ANY_CLICK, actionIndex++, action, text);
               }
            }

            CommandTrait commandTrait = (CommandTrait)npc.getTraitNullable(CommandTrait.class);
            if (commandTrait != null) {
               NpcAction action = FancyNpcs.getInstance().getActionManager().getActionByName("console_command");
               Map<Integer, Object> commands = (Map<Integer, Object>)ReflectionUtils.getValue(commandTrait, "commands");

               for (Object commandObj : commands.values()) {
                  String command = (String)ReflectionUtils.getValue(commandObj, "command");
                  Enum<?> hand = (Enum<?>)ReflectionUtils.getValue(commandObj, "hand");
                  String var26 = hand.name();

                  ActionTrigger trigger = switch (var26) {
                     case "LEFT", "SHIT_LEFT" -> ActionTrigger.LEFT_CLICK;
                     case "RIGHT", "SHIFT_RIGHT" -> ActionTrigger.RIGHT_CLICK;
                     default -> ActionTrigger.ANY_CLICK;
                  };
                  data.addAction(trigger, actionIndex++, action, command);
               }
            }

            Npc fnNpc = FancyNpcs.getInstance().getNpcAdapter().apply(data);
            fnNpc.create();
            FancyNpcs.getInstance().getNpcManager().registerNpc(fnNpc);
            convertedCount++;
         }

         if (convertedCount == 0) {
            this.translator.translate("npcconvert_no_npcs_found").withPrefix().replace("source_plugin", "Citizens").send(sender);
         } else {
            Event event = new Event("NpcsConverted", new HashMap<>())
               .withProperty("source_plugin", "Citizens")
               .withProperty("count", String.valueOf(convertedCount));
            FancyNpcs.getInstance().getFancyAnalytics().sendEvent(event);
            this.translator
               .translate("npcconvert_success")
               .withPrefix()
               .replace("source_plugin", "Citizens")
               .replace("converted_count", String.valueOf(convertedCount))
               .send(sender);
         }
      }
   }
}
