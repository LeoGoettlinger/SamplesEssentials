package com.github.retrooper.packetevents.protocol.item.consumables;

import com.github.retrooper.packetevents.protocol.item.consumables.builtin.ApplyEffectsConsumeEffect;
import com.github.retrooper.packetevents.protocol.item.consumables.builtin.ClearAllEffectsConsumeEffect;
import com.github.retrooper.packetevents.protocol.item.consumables.builtin.PlaySoundConsumeEffect;
import com.github.retrooper.packetevents.protocol.item.consumables.builtin.RemoveEffectsConsumeEffect;
import com.github.retrooper.packetevents.protocol.item.consumables.builtin.TeleportRandomlyConsumeEffect;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class ConsumeEffectTypes {
   private static final VersionedRegistry<ConsumeEffectType<?>> REGISTRY = new VersionedRegistry<>("consume_effect_type");
   public static final ConsumeEffectType<ApplyEffectsConsumeEffect> APPLY_EFFECTS = define(
      "apply_effects", ApplyEffectsConsumeEffect::read, ApplyEffectsConsumeEffect::write
   );
   public static final ConsumeEffectType<RemoveEffectsConsumeEffect> REMOVE_EFFECTS = define(
      "remove_effects", RemoveEffectsConsumeEffect::read, RemoveEffectsConsumeEffect::write
   );
   public static final ConsumeEffectType<ClearAllEffectsConsumeEffect> CLEAR_ALL_EFFECTS = define(
      "clear_all_effects", ClearAllEffectsConsumeEffect::read, ClearAllEffectsConsumeEffect::write
   );
   public static final ConsumeEffectType<TeleportRandomlyConsumeEffect> TELEPORT_RANDOMLY = define(
      "teleport_randomly", TeleportRandomlyConsumeEffect::read, TeleportRandomlyConsumeEffect::write
   );
   public static final ConsumeEffectType<PlaySoundConsumeEffect> PLAY_SOUND = define("play_sound", PlaySoundConsumeEffect::read, PlaySoundConsumeEffect::write);

   private ConsumeEffectTypes() {
   }

   @Internal
   public static <T extends ConsumeEffect<?>> ConsumeEffectType<T> define(String name, PacketWrapper.Reader<T> reader, PacketWrapper.Writer<T> writer) {
      return REGISTRY.define(name, data -> new StaticConsumeEffectType<>(data, reader, writer));
   }

   public static VersionedRegistry<ConsumeEffectType<?>> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }
}
