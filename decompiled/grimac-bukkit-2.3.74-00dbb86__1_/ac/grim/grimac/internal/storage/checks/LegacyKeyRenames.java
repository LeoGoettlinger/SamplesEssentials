package ac.grim.grimac.internal.storage.checks;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@ApiStatus.Internal
public final class LegacyKeyRenames {
   public static final Map<String, String> OLD_TO_NEW;

   private LegacyKeyRenames() {
   }

   static {
      Map<String, String> m = new LinkedHashMap<>();
      m.put("grim.legacy.badpacketsb", "grim.badpackets.ignored_rotation");
      m.put("grim.legacy.badpacketsc", "grim.badpackets.wake_not_sleeping");
      m.put("grim.legacy.badpacketsh", "grim.badpackets.unexpected_sequence");
      m.put("grim.legacy.badpacketsj", "grim.badpackets.use_item_rotation_mismatch");
      m.put("grim.legacy.badpacketsr", "grim.badpackets.position_starvation");
      m.put("grim.legacy.badpacketss", "grim.badpackets.window_confirmation_not_accepted");
      m.put("grim.legacy.badpacketst", "grim.badpackets.invalid_interact_vector");
      m.put("grim.legacy.badpacketsw", "grim.badpackets.invalid_entity_target");
      m.put("grim.legacy.badpacketsx", "grim.badpackets.extra_input_actions");
      m.put("grim.legacy.badpacketsz", "grim.badpackets.duplicate_player_input");
      m.put("grim.legacy.chatc", "grim.chat.moving_while_chatting");
      m.put("grim.legacy.exploita", "grim.exploit.anvil_name_length");
      m.put("grim.legacy.groundspoof", "grim.groundspoof.fake");
      m.put("grim.legacy.timerlimit", "grim.timer.limit");
      m.put("grim.legacy.elytraa", "grim.elytra.already_gliding");
      m.put("grim.legacy.elytrab", "grim.elytra.no_jump");
      m.put("grim.legacy.elytrac", "grim.elytra.too_frequent");
      m.put("grim.legacy.elytrad", "grim.elytra.no_elytra");
      m.put("grim.legacy.elytrae", "grim.elytra.flying");
      m.put("grim.legacy.elytraf", "grim.elytra.grounded");
      m.put("grim.legacy.elytrag", "grim.elytra.levitation");
      m.put("grim.legacy.elytrah", "grim.elytra.vehicle");
      m.put("grim.legacy.elytrai", "grim.elytra.water");
      m.put("grim.legacy.multiactionsa", "grim.multiactions.attack_while_using");
      m.put("grim.legacy.multiactionsb", "grim.multiactions.break_while_using");
      m.put("grim.legacy.multiactionsc", "grim.multiactions.inventory_click_while_moving");
      m.put("grim.legacy.multiactionsd", "grim.multiactions.inventory_close_while_moving");
      m.put("grim.legacy.multiactionse", "grim.multiactions.swing_while_using");
      m.put("grim.legacy.multiactionsf", "grim.multiactions.block_and_entity_interact");
      m.put("grim.legacy.multiactionsg", "grim.multiactions.action_while_rowing");
      m.put("grim.legacy.multiinteracta", "grim.multiinteract.multiple_targets");
      m.put("grim.legacy.multiinteractb", "grim.multiinteract.interact_at_position_changed");
      m.put("grim.legacy.packetordera", "grim.packetorder.window_click_order");
      m.put("grim.legacy.packetorderb", "grim.packetorder.noswing");
      m.put("grim.legacy.packetorderc", "grim.packetorder.interact_order");
      m.put("grim.legacy.packetorderd", "grim.packetorder.interact_hand_order");
      m.put("grim.legacy.packetordere", "grim.packetorder.slot_order");
      m.put("grim.legacy.packetorderf", "grim.packetorder.input_tick_to_sneak_sprint_order");
      m.put("grim.legacy.packetorderg", "grim.packetorder.hotbar_inventory_manage_order");
      m.put("grim.legacy.packetorderh", "grim.packetorder.sneak_sprint_order");
      m.put("grim.legacy.packetorderi", "grim.packetorder.input_tick_order");
      m.put("grim.legacy.packetorderj", "grim.packetorder.attack_interact_use_order");
      m.put("grim.legacy.packetorderk", "grim.packetorder.inventory_open_order");
      m.put("grim.legacy.packetorderl", "grim.packetorder.drop_item_order");
      m.put("grim.legacy.packetorderm", "grim.packetorder.interact_use_order");
      m.put("grim.legacy.packetordern", "grim.packetorder.place_use_order");
      m.put("grim.legacy.packetordero", "grim.packetorder.tick_end_order");
      m.put("grim.legacy.packetorderp", "grim.packetorder.transaction_response_order");
      m.put("grim.legacy.sprinta", "grim.sprint.hunger");
      m.put("grim.legacy.sprintb", "grim.sprint.sneaking");
      m.put("grim.legacy.sprintc", "grim.sprint.using_item");
      m.put("grim.legacy.sprintd", "grim.sprint.blindness");
      m.put("grim.legacy.sprinte", "grim.sprint.wall");
      m.put("grim.legacy.sprintf", "grim.sprint.gliding");
      m.put("grim.legacy.sprintg", "grim.sprint.water");
      m.put("grim.legacy.vehiclea", "grim.vehicle.impossible_input");
      m.put("grim.legacy.vehicleb", "grim.vehicle.spoofed_vehicle");
      m.put("grim.legacy.vehiclec", "grim.vehicle.vehicle_control");
      m.put("grim.legacy.vehicled", "grim.vehicle.spoofed_jump");
      m.put("grim.legacy.vehiclee", "grim.vehicle.spoofed_boat");
      m.put("grim.legacy.vehiclef", "grim.vehicle.boat_input_mismatch");
      OLD_TO_NEW = Collections.unmodifiableMap(m);
   }
}
