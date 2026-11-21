package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;

public class PlayerMessageSignatureType extends Type<PlayerMessageSignature> {
   public PlayerMessageSignatureType() {
      super(PlayerMessageSignature.class);
   }

   public PlayerMessageSignature read(ByteBuf buffer) {
      return new PlayerMessageSignature(Types.UUID.read(buffer), Types.BYTE_ARRAY_PRIMITIVE.read(buffer));
   }

   public void write(ByteBuf buffer, PlayerMessageSignature value) {
      Types.UUID.write(buffer, value.uuid());
      Types.BYTE_ARRAY_PRIMITIVE.write(buffer, value.signatureBytes());
   }

   public static final class OptionalPlayerMessageSignatureType extends OptionalType<PlayerMessageSignature> {
      public OptionalPlayerMessageSignatureType() {
         super(Types.PLAYER_MESSAGE_SIGNATURE);
      }
   }
}
