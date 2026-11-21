package com.github.retrooper.packetevents.protocol.world.damagetype;

import com.github.retrooper.packetevents.protocol.util.CodecNameable;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import net.kyori.adventure.util.Index;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum DamageEffects implements CodecNameable {
   HURT("hurt"),
   THORNS("thorns"),
   DROWNING("drowning"),
   BURNING("burning"),
   POKING("poking"),
   FREEZING("freezing");

   public static final Index<String, DamageEffects> ID_INDEX = Index.create(DamageEffects.class, DamageEffects::getId);
   public static final NbtCodec<DamageEffects> CODEC = NbtCodecs.forEnum(values());
   private final String id;

   private DamageEffects(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   @Override
   public String getCodecName() {
      return this.id;
   }
}
