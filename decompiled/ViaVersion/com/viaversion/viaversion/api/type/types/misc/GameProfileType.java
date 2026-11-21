package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public final class GameProfileType extends Type<GameProfile> {
   private static final GameProfile.Property[] EMPTY_PROPERTIES = new GameProfile.Property[0];

   public GameProfileType() {
      super(GameProfile.class);
   }

   public GameProfile read(ByteBuf buffer) {
      String name = Types.OPTIONAL_STRING.read(buffer);
      UUID id = Types.OPTIONAL_UUID.read(buffer);
      GameProfile.Property[] properties = Types.PROFILE_PROPERTY_ARRAY.read(buffer);
      return new GameProfile(name, id, properties);
   }

   public void write(ByteBuf buffer, GameProfile value) {
      Types.OPTIONAL_STRING.write(buffer, value.name());
      Types.OPTIONAL_UUID.write(buffer, value.id());
      Types.PROFILE_PROPERTY_ARRAY.write(buffer, value.properties());
   }

   public void write(Ops ops, GameProfile value) {
      ops.writeMap(
         map -> map.writeOptional("name", Types.STRING, value.name())
            .writeOptional("id", Types.UUID, value.id())
            .writeOptional("properties", Types.PROFILE_PROPERTY_ARRAY, value.properties(), EMPTY_PROPERTIES)
      );
   }
}
