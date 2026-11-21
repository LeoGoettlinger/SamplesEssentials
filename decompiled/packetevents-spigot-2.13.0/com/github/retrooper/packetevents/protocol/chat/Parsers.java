package com.github.retrooper.packetevents.protocol.chat;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Parsers {
   private static final VersionedRegistry<Parsers.Parser> REGISTRY = new VersionedRegistry<>("command_argument_type");
   public static final Parsers.Parser BRIGADIER_BOOL = define("brigadier:bool", null, null);
   public static final Parsers.Parser BRIGADIER_FLOAT = define("brigadier:float", packetWrapper -> {
      byte flags = packetWrapper.readByte();
      float min = (flags & 1) != 0 ? packetWrapper.readFloat() : -Float.MAX_VALUE;
      float max = (flags & 2) != 0 ? packetWrapper.readFloat() : Float.MAX_VALUE;
      return Arrays.asList(flags, min, max);
   }, (packetWrapper, properties) -> {
      byte flags = (Byte)properties.get(0);
      packetWrapper.writeByte(flags);
      if ((flags & 1) != 0) {
         packetWrapper.writeFloat((Float)properties.get(1));
      }

      if ((flags & 2) != 0) {
         packetWrapper.writeFloat((Float)properties.get(2));
      }
   });
   public static final Parsers.Parser BRIGADIER_DOUBLE = define("brigadier:double", packetWrapper -> {
      byte flags = packetWrapper.readByte();
      double min = (flags & 1) != 0 ? packetWrapper.readDouble() : -Double.MAX_VALUE;
      double max = (flags & 2) != 0 ? packetWrapper.readDouble() : Double.MAX_VALUE;
      return Arrays.asList(flags, min, max);
   }, (packetWrapper, properties) -> {
      byte flags = (Byte)properties.get(0);
      packetWrapper.writeByte(flags);
      if ((flags & 1) != 0) {
         packetWrapper.writeDouble((Double)properties.get(1));
      }

      if ((flags & 2) != 0) {
         packetWrapper.writeDouble((Double)properties.get(2));
      }
   });
   public static final Parsers.Parser BRIGADIER_INTEGER = define("brigadier:integer", packetWrapper -> {
      byte flags = packetWrapper.readByte();
      int min = (flags & 1) != 0 ? packetWrapper.readInt() : Integer.MIN_VALUE;
      int max = (flags & 2) != 0 ? packetWrapper.readInt() : Integer.MAX_VALUE;
      return Arrays.asList(flags, min, max);
   }, (packetWrapper, properties) -> {
      byte flags = (Byte)properties.get(0);
      packetWrapper.writeByte(flags);
      if ((flags & 1) != 0) {
         packetWrapper.writeInt((Integer)properties.get(1));
      }

      if ((flags & 2) != 0) {
         packetWrapper.writeInt((Integer)properties.get(2));
      }
   });
   public static final Parsers.Parser BRIGADIER_LONG = define("brigadier:long", packetWrapper -> {
      byte flags = packetWrapper.readByte();
      long min = (flags & 1) != 0 ? packetWrapper.readLong() : Long.MIN_VALUE;
      long max = (flags & 2) != 0 ? packetWrapper.readLong() : Long.MAX_VALUE;
      return Arrays.asList(flags, min, max);
   }, (packetWrapper, properties) -> {
      byte flags = (Byte)properties.get(0);
      packetWrapper.writeByte(flags);
      if ((flags & 1) != 0) {
         packetWrapper.writeLong((Long)properties.get(1));
      }

      if ((flags & 2) != 0) {
         packetWrapper.writeLong((Long)properties.get(2));
      }
   });
   public static final Parsers.Parser BRIGADIER_STRING = define(
      "brigadier:string",
      packetWrapper -> Collections.singletonList(packetWrapper.readVarInt()),
      (packetWrapper, properties) -> packetWrapper.writeVarInt((Integer)properties.get(0))
   );
   public static final Parsers.Parser ENTITY = define(
      "entity",
      packetWrapper -> Collections.singletonList(packetWrapper.readByte()),
      (packetWrapper, properties) -> packetWrapper.writeByte(((Byte)properties.get(0)).intValue())
   );
   public static final Parsers.Parser GAME_PROFILE = define("game_profile", null, null);
   public static final Parsers.Parser BLOCK_POS = define("block_pos", null, null);
   public static final Parsers.Parser COLUMN_POS = define("column_pos", null, null);
   public static final Parsers.Parser VEC3 = define("vec3", null, null);
   public static final Parsers.Parser VEC2 = define("vec2", null, null);
   public static final Parsers.Parser BLOCK_STATE = define("block_state", null, null);
   public static final Parsers.Parser BLOCK_PREDICATE = define("block_predicate", null, null);
   public static final Parsers.Parser ITEM_STACK = define("item_stack", null, null);
   public static final Parsers.Parser ITEM_PREDICATE = define("item_predicate", null, null);
   @Obsolete
   public static final Parsers.Parser COLOR = define("color", null, null);
   public static final Parsers.Parser COMPONENT = define("component", null, null);
   public static final Parsers.Parser STYLE = define("style", null, null);
   public static final Parsers.Parser MESSAGE = define("message", null, null);
   public static final Parsers.Parser NBT_COMPOUND_TAG = define("nbt_compound_tag", null, null);
   @Obsolete
   public static final Parsers.Parser NBT = define("nbt", null, null);
   public static final Parsers.Parser NBT_TAG = define("nbt_tag", null, null);
   public static final Parsers.Parser NBT_PATH = define("nbt_path", null, null);
   public static final Parsers.Parser OBJECTIVE = define("objective", null, null);
   public static final Parsers.Parser OBJECTIVE_CRITERIA = define("objective_criteria", null, null);
   public static final Parsers.Parser OPERATION = define("operation", null, null);
   public static final Parsers.Parser PARTICLE = define("particle", null, null);
   public static final Parsers.Parser ANGLE = define("angle", null, null);
   public static final Parsers.Parser ROTATION = define("rotation", null, null);
   public static final Parsers.Parser SCOREBOARD_SLOT = define("scoreboard_slot", null, null);
   public static final Parsers.Parser SCORE_HOLDER = define(
      "score_holder",
      packetWrapper -> Collections.singletonList(packetWrapper.readByte()),
      (packetWrapper, properties) -> packetWrapper.writeByte(((Byte)properties.get(0)).intValue())
   );
   public static final Parsers.Parser SWIZZLE = define("swizzle", null, null);
   public static final Parsers.Parser TEAM = define("team", null, null);
   public static final Parsers.Parser ITEM_SLOT = define("item_slot", null, null);
   public static final Parsers.Parser ITEM_SLOTS = define("item_slots", null, null);
   public static final Parsers.Parser RESOURCE_LOCATION = define("resource_location", null, null);
   public static final Parsers.Parser MOB_EFFECT = define("mob_effect", null, null);
   public static final Parsers.Parser FUNCTION = define("function", null, null);
   public static final Parsers.Parser ENTITY_ANCHOR = define("entity_anchor", null, null);
   public static final Parsers.Parser INT_RANGE = define("int_range", null, null);
   public static final Parsers.Parser FLOAT_RANGE = define("float_range", null, null);
   public static final Parsers.Parser ITEM_ENCHANTMENT = define("item_enchantment", null, null);
   public static final Parsers.Parser ENTITY_SUMMON = define("entity_summon", null, null);
   public static final Parsers.Parser DIMENSION = define("dimension", null, null);
   public static final Parsers.Parser GAMEMODE = define("gamemode", null, null);
   public static final Parsers.Parser TIME = define(
      "time",
      wrapper -> Collections.singletonList(wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4) ? wrapper.readInt() : 0),
      (wrapper, properties) -> {
         if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)) {
            wrapper.writeInt((Integer)properties.get(0));
         }
      }
   );
   public static final Parsers.Parser RESOURCE_OR_TAG = define(
      "resource_or_tag",
      packetWrapper -> Collections.singletonList(packetWrapper.readIdentifier()),
      (packetWrapper, properties) -> packetWrapper.writeIdentifier((ResourceLocation)properties.get(0))
   );
   public static final Parsers.Parser RESOURCE_OR_TAG_KEY = define(
      "resource_or_tag_key",
      packetWrapper -> Collections.singletonList(packetWrapper.readIdentifier()),
      (packetWrapper, properties) -> packetWrapper.writeIdentifier((ResourceLocation)properties.get(0))
   );
   public static final Parsers.Parser RESOURCE = define(
      "resource",
      packetWrapper -> Collections.singletonList(packetWrapper.readIdentifier()),
      (packetWrapper, properties) -> packetWrapper.writeIdentifier((ResourceLocation)properties.get(0))
   );
   public static final Parsers.Parser RESOURCE_KEY = define(
      "resource_key",
      packetWrapper -> Collections.singletonList(packetWrapper.readIdentifier()),
      (packetWrapper, properties) -> packetWrapper.writeIdentifier((ResourceLocation)properties.get(0))
   );
   public static final Parsers.Parser TEMPLATE_MIRROR = define("template_mirror", null, null);
   public static final Parsers.Parser TEMPLATE_ROTATION = define("template_rotation", null, null);
   public static final Parsers.Parser HEIGHTMAP = define("heightmap", null, null);
   public static final Parsers.Parser LOOT_TABLE = define("loot_table", null, null);
   public static final Parsers.Parser LOOT_PREDICATE = define("loot_predicate", null, null);
   public static final Parsers.Parser LOOT_MODIFIER = define("loot_modifier", null, null);
   public static final Parsers.Parser UUID = define("uuid", null, null);
   public static final Parsers.Parser RESOURCE_SELECTOR = define(
      "resource_selector",
      wrapper -> Collections.singletonList(wrapper.readIdentifier()),
      (wrapper, value) -> wrapper.writeIdentifier((ResourceLocation)value.get(0))
   );
   public static final Parsers.Parser HEX_COLOR = define("hex_color", null, null);
   public static final Parsers.Parser DIALOG = define("dialog", null, null);
   public static final Parsers.Parser TEAM_COLOR = define("team_color", null, null);

   private Parsers() {
   }

   @Internal
   public static Parsers.Parser define(String key) {
      return define(key, null, null);
   }

   @Internal
   public static Parsers.Parser define(String key, @Nullable Parsers.Reader reader, @Nullable Parsers.Writer writer) {
      return REGISTRY.define(key, data -> new Parsers.Parser(data, reader, writer));
   }

   @Nullable
   public static Parsers.Parser getByName(String name) {
      return REGISTRY.getByName(name);
   }

   @Nullable
   public static Parsers.Parser getById(ClientVersion version, int id) {
      return REGISTRY.getById(version, id);
   }

   public static List<Parsers.Parser> getParsers() {
      return new ArrayList<>(REGISTRY.getEntries());
   }

   public static VersionedRegistry<Parsers.Parser> getRegistry() {
      return REGISTRY;
   }

   static {
      REGISTRY.unloadMappings();
   }

   public static final class Parser extends AbstractMappedEntity {
      @Nullable
      private final Parsers.Reader reader;
      @Nullable
      private final Parsers.Writer writer;

      @Deprecated
      public Parser(String name, @Nullable Function<PacketWrapper<?>, List<Object>> read, @Nullable BiConsumer<PacketWrapper<?>, List<Object>> write) {
         this(new TypesBuilderData(new ResourceLocation(name), new int[0]), read == null ? null : read::apply, write == null ? null : write::accept);
      }

      @Internal
      public Parser(@Nullable TypesBuilderData data, @Nullable Parsers.Reader reader, @Nullable Parsers.Writer writer) {
         super(data);
         this.reader = reader;
         this.writer = writer;
      }

      public Optional<List<Object>> readProperties(PacketWrapper<?> wrapper) {
         return this.reader != null ? Optional.of(this.reader.apply(wrapper)) : Optional.empty();
      }

      public void writeProperties(PacketWrapper<?> wrapper, List<Object> properties) {
         if (this.writer != null) {
            this.writer.accept(wrapper, properties);
         }
      }
   }

   @FunctionalInterface
   public interface Reader extends Function<PacketWrapper<?>, List<Object>> {
   }

   @FunctionalInterface
   public interface Writer extends BiConsumer<PacketWrapper<?>, List<Object>> {
   }
}
