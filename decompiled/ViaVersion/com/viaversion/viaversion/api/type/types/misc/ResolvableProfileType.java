package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.ResolvableProfile;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public final class ResolvableProfileType extends Type<ResolvableProfile> {
   public ResolvableProfileType() {
      super(ResolvableProfile.class);
   }

   public ResolvableProfile read(ByteBuf buffer) {
      GameProfile profile;
      if (buffer.readBoolean()) {
         UUID id = Types.UUID.read(buffer);
         String name = Types.STRING.read(buffer);
         GameProfile.Property[] properties = Types.PROFILE_PROPERTY_ARRAY.read(buffer);
         profile = new GameProfile(name, id, properties, false);
      } else {
         profile = Types.GAME_PROFILE.read(buffer);
      }

      String texture = Types.OPTIONAL_STRING.read(buffer);
      String capeTexture = Types.OPTIONAL_STRING.read(buffer);
      String elytraTexture = Types.OPTIONAL_STRING.read(buffer);
      Boolean encodedModelType = Types.OPTIONAL_BOOLEAN.read(buffer);
      Integer modelType = encodedModelType != null ? encodedModelType ? 0 : 1 : null;
      return new ResolvableProfile(profile, texture, capeTexture, elytraTexture, modelType);
   }

   public void write(ByteBuf buffer, ResolvableProfile value) {
      GameProfile profile = value.profile();
      if (!profile.dynamic()) {
         buffer.writeBoolean(true);
         Types.UUID.write(buffer, profile.id());
         Types.STRING.write(buffer, profile.name());
         Types.PROFILE_PROPERTY_ARRAY.write(buffer, profile.properties());
      } else {
         buffer.writeBoolean(false);
         Types.GAME_PROFILE.write(buffer, profile);
      }

      Types.OPTIONAL_STRING.write(buffer, value.bodyTexture());
      Types.OPTIONAL_STRING.write(buffer, value.capeTexture());
      Types.OPTIONAL_STRING.write(buffer, value.elytraTexture());
      Types.OPTIONAL_BOOLEAN.write(buffer, value.modelType() != null ? value.modelType() == 0 : null);
   }

   public void write(Ops ops, ResolvableProfile value) {
      String modelType = value.modelType() != null ? (value.modelType() == 0 ? "slim" : "wide") : null;
      ops.writeMap(
         map -> map.writeInlinedMap(Types.GAME_PROFILE, value.profile())
            .writeOptional("texture", Types.IDENTIFIER, value.bodyTexture() != null ? Key.of(value.bodyTexture()) : null)
            .writeOptional("cape", Types.IDENTIFIER, value.capeTexture() != null ? Key.of(value.capeTexture()) : null)
            .writeOptional("elytra", Types.IDENTIFIER, value.elytraTexture() != null ? Key.of(value.elytraTexture()) : null)
            .writeOptional("model", Types.OPTIONAL_STRING, modelType)
      );
   }
}
