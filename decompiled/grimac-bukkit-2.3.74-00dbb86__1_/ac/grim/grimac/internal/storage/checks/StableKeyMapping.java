package ac.grim.grimac.internal.storage.checks;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Internal
public final class StableKeyMapping {
   private static final Map<String, String> MAPPINGS = buildMappings();

   private StableKeyMapping() {
   }

   public static Optional<String> stableKeyFor(String legacyDisplayName) {
      return legacyDisplayName == null ? Optional.empty() : Optional.ofNullable(MAPPINGS.get(legacyDisplayName.toLowerCase(Locale.ROOT)));
   }

   public static String legacyFallback(String legacyDisplayName) {
      return "grim.legacy." + legacyDisplayName.toLowerCase(Locale.ROOT);
   }

   private static Map<String, String> buildMappings() {
      Map<String, String> m = new HashMap<>(160);
      m.put("badpacketsa", "grim.badpackets.duplicate_slot");
      m.put("badpacketsd", "grim.badpackets.invalid_pitch");
      m.put("badpacketse", "grim.badpackets.invalid_position");
      m.put("badpacketsf", "grim.badpackets.duplicate_sprint");
      m.put("badpacketsg", "grim.badpackets.duplicate_sneak");
      m.put("badpacketsi", "grim.badpackets.spoofed_abilities");
      m.put("badpacketsk", "grim.badpackets.invalid_spectate");
      m.put("badpacketsl", "grim.badpackets.invalid_dig");
      m.put("badpacketsm", "grim.badpackets.respawn_alive");
      m.put("badpacketsn", "grim.badpackets.invalid_teleport");
      m.put("badpacketso", "grim.badpackets.invalid_keepalive");
      m.put("badpacketsp", "grim.badpackets.invalid_click");
      m.put("badpacketsq", "grim.badpackets.invalid_horse_jump");
      m.put("badpacketsu", "grim.badpackets.invalid_block_placement");
      m.put("badpacketsv", "grim.badpackets.slow_move");
      m.put("badpacketsy", "grim.badpackets.oob_slot");
      m.put("badpacketsb", "grim.badpackets.ignored_rotation");
      m.put("badpacketsc", "grim.badpackets.wake_not_sleeping");
      m.put("badpacketsh", "grim.badpackets.unexpected_sequence");
      m.put("badpacketsj", "grim.badpackets.use_item_rotation_mismatch");
      m.put("badpacketsr", "grim.badpackets.position_starvation");
      m.put("badpacketss", "grim.badpackets.window_confirmation_not_accepted");
      m.put("badpacketst", "grim.badpackets.invalid_interact_vector");
      m.put("badpacketsw", "grim.badpackets.invalid_entity_target");
      m.put("badpacketsx", "grim.badpackets.extra_input_actions");
      m.put("badpacketsz", "grim.badpackets.duplicate_player_input");
      m.put("selfinteract", "grim.badpackets.self_hit");
      m.put("crasha", "grim.crash.large_position");
      m.put("crashb", "grim.crash.creative_while_not_creative");
      m.put("crashc", "grim.crash.nan_position");
      m.put("crashd", "grim.crash.lectern");
      m.put("crashe", "grim.crash.low_view_distance");
      m.put("crashf", "grim.crash.button_crash");
      m.put("crashg", "grim.crash.negative_sequence");
      m.put("crashh", "grim.crash.invalid_tab_complete");
      m.put("crashi", "grim.crash.invalid_bundle_slot");
      m.put("hitboxes", "grim.combat.hitboxes");
      m.put("reach", "grim.combat.reach");
      m.put("aimduplicatelook", "grim.aim.duplicate_look");
      m.put("aimmodulo360", "grim.aim.modulo_360");
      m.put("aimfold", "grim.legacy.aimfold");
      m.put("aimgold", "grim.legacy.aimgold");
      m.put("aimhold", "grim.legacy.aimhold");
      m.put("airliquidbreak", "grim.breaking.air_liquid_break");
      m.put("farbreak", "grim.breaking.far_break");
      m.put("fastbreak", "grim.breaking.fast_break");
      m.put("invalidbreak", "grim.breaking.invalid_break");
      m.put("multibreak", "grim.breaking.multi_break");
      m.put("noswingbreak", "grim.breaking.no_swing_break");
      m.put("positionbreaka", "grim.breaking.position_break_a");
      m.put("positionbreakb", "grim.breaking.position_break_b");
      m.put("rotationbreak", "grim.breaking.rotation_break");
      m.put("wrongbreak", "grim.breaking.wrong_break");
      m.put("airliquidplace", "grim.scaffolding.air_liquid_place");
      m.put("duplicaterotplace", "grim.scaffolding.duplicate_rot_place");
      m.put("fabricatedplace", "grim.scaffolding.fabricated_place");
      m.put("farplace", "grim.scaffolding.far_place");
      m.put("invalidplacea", "grim.scaffolding.invalid_place_a");
      m.put("invalidplaceb", "grim.scaffolding.invalid_place_b");
      m.put("multiplace", "grim.scaffolding.multi_place");
      m.put("positionplace", "grim.scaffolding.position_place");
      m.put("rotationplace", "grim.scaffolding.rotation_place");
      m.put("chatc", "grim.chat.moving_while_chatting");
      m.put("chata", "grim.exploit.blank_tab_complete");
      m.put("chatb", "grim.exploit.spigot_antispam_bypass");
      m.put("chatd", "grim.exploit.chat_while_hidden");
      m.put("exploita", "grim.exploit.anvil_name_length");
      m.put("exploitb", "grim.exploit.invalid_book_edit");
      m.put("exploitc", "grim.legacy.exploitc");
      m.put("phase", "grim.prediction.phase");
      m.put("noslow", "grim.movement.noslow");
      m.put("groundspoof", "grim.groundspoof.fake");
      m.put("nofall", "grim.groundspoof.no_fall");
      m.put("post", "grim.post.invalid_order");
      m.put("transactionorder", "grim.ping.invalid_transaction_order");
      m.put("baritone", "grim.baritone.baritone");
      m.put("negativetimer", "grim.timer.negative");
      m.put("ticktimer", "grim.timer.tick");
      m.put("timer", "grim.timer.timer");
      m.put("timerlimit", "grim.timer.limit");
      m.put("vehicletimer", "grim.timer.vehicle");
      m.put("elytraa", "grim.elytra.already_gliding");
      m.put("elytrab", "grim.elytra.no_jump");
      m.put("elytrac", "grim.elytra.too_frequent");
      m.put("elytrad", "grim.elytra.no_elytra");
      m.put("elytrae", "grim.elytra.flying");
      m.put("elytraf", "grim.elytra.grounded");
      m.put("elytrag", "grim.elytra.levitation");
      m.put("elytrah", "grim.elytra.vehicle");
      m.put("elytrai", "grim.elytra.water");
      m.put("sprinta", "grim.sprint.hunger");
      m.put("sprintb", "grim.sprint.sneaking");
      m.put("sprintc", "grim.sprint.using_item");
      m.put("sprintd", "grim.sprint.blindness");
      m.put("sprinte", "grim.sprint.wall");
      m.put("sprintf", "grim.sprint.gliding");
      m.put("sprintg", "grim.sprint.water");
      m.put("vehiclea", "grim.vehicle.impossible_input");
      m.put("vehicleb", "grim.vehicle.spoofed_vehicle");
      m.put("vehiclec", "grim.vehicle.vehicle_control");
      m.put("vehicled", "grim.vehicle.spoofed_jump");
      m.put("vehiclee", "grim.vehicle.spoofed_boat");
      m.put("vehiclef", "grim.vehicle.boat_input_mismatch");
      m.put("multiactionsa", "grim.multiactions.attack_while_using");
      m.put("multiactionsb", "grim.multiactions.break_while_using");
      m.put("multiactionsc", "grim.multiactions.inventory_click_while_moving");
      m.put("multiactionsd", "grim.multiactions.inventory_close_while_moving");
      m.put("multiactionse", "grim.multiactions.swing_while_using");
      m.put("multiactionsf", "grim.multiactions.block_and_entity_interact");
      m.put("multiactionsg", "grim.multiactions.action_while_rowing");
      m.put("multiinteracta", "grim.multiinteract.multiple_targets");
      m.put("multiinteractb", "grim.multiinteract.interact_at_position_changed");
      m.put("packetordera", "grim.packetorder.window_click_order");
      m.put("packetorderb", "grim.packetorder.noswing");
      m.put("packetorderc", "grim.packetorder.interact_order");
      m.put("packetorderd", "grim.packetorder.interact_hand_order");
      m.put("packetordere", "grim.packetorder.slot_order");
      m.put("packetorderf", "grim.packetorder.input_tick_to_sneak_sprint_order");
      m.put("packetorderg", "grim.packetorder.hotbar_inventory_manage_order");
      m.put("packetorderh", "grim.packetorder.sneak_sprint_order");
      m.put("packetorderi", "grim.packetorder.input_tick_order");
      m.put("packetorderj", "grim.packetorder.attack_interact_use_order");
      m.put("packetorderk", "grim.packetorder.inventory_open_order");
      m.put("packetorderl", "grim.packetorder.drop_item_order");
      m.put("packetorderm", "grim.packetorder.interact_use_order");
      m.put("packetordern", "grim.packetorder.place_use_order");
      m.put("packetordero", "grim.packetorder.tick_end_order");
      m.put("packetorderp", "grim.packetorder.transaction_response_order");
      m.put("looka", "grim.legacy.looka");
      m.put("clientbrand", "grim.legacy.clientbrand");
      m.put("inventorya", "grim.legacy.inventorya");
      m.put("inventoryb", "grim.legacy.inventoryb");
      m.put("inventoryc", "grim.legacy.inventoryc");
      m.put("inventoryd", "grim.legacy.inventoryd");
      m.put("inventorye", "grim.legacy.inventorye");
      m.put("inventoryf", "grim.legacy.inventoryf");
      m.put("inventoryg", "grim.legacy.inventoryg");
      return Map.copyOf(m);
   }
}
