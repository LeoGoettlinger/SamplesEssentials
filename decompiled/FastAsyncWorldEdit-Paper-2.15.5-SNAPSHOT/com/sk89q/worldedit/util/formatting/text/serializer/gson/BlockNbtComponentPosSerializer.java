package com.sk89q.worldedit.util.formatting.text.serializer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sk89q.worldedit.util.formatting.text.BlockNbtComponent;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockNbtComponentPosSerializer implements JsonDeserializer<BlockNbtComponent.Pos>, JsonSerializer<BlockNbtComponent.Pos> {
   public static final BlockNbtComponentPosSerializer INSTANCE = new BlockNbtComponentPosSerializer();
   private static final Pattern LOCAL_PATTERN = Pattern.compile("^\\^(\\d+(\\.\\d+)?) \\^(\\d+(\\.\\d+)?) \\^(\\d+(\\.\\d+)?)$");
   private static final Pattern WORLD_PATTERN = Pattern.compile("^(~?)(\\d+) (~?)(\\d+) (~?)(\\d+)$");
   private static final String LOCAL_SYMBOL = "^";
   private static final String RELATIVE_SYMBOL = "~";
   private static final String ABSOLUTE_SYMBOL = "";

   public BlockNbtComponent.Pos deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
      String string = json.getAsString();
      Matcher localMatch = LOCAL_PATTERN.matcher(string);
      if (localMatch.matches()) {
         return BlockNbtComponent.LocalPos.of(
            Double.parseDouble(localMatch.group(1)), Double.parseDouble(localMatch.group(3)), Double.parseDouble(localMatch.group(5))
         );
      } else {
         Matcher worldMatch = WORLD_PATTERN.matcher(string);
         if (worldMatch.matches()) {
            return BlockNbtComponent.WorldPos.of(
               deserializeCoordinate(worldMatch.group(1), worldMatch.group(2)),
               deserializeCoordinate(worldMatch.group(3), worldMatch.group(4)),
               deserializeCoordinate(worldMatch.group(5), worldMatch.group(6))
            );
         } else {
            throw new JsonParseException("Don't know how to turn " + string + " into a Position");
         }
      }
   }

   public JsonElement serialize(final BlockNbtComponent.Pos src, final Type typeOfSrc, final JsonSerializationContext context) {
      if (src instanceof BlockNbtComponent.LocalPos) {
         BlockNbtComponent.LocalPos local = (BlockNbtComponent.LocalPos)src;
         return new JsonPrimitive(serializeLocal(local.left()) + ' ' + serializeLocal(local.up()) + ' ' + serializeLocal(local.forwards()));
      } else if (src instanceof BlockNbtComponent.WorldPos) {
         BlockNbtComponent.WorldPos world = (BlockNbtComponent.WorldPos)src;
         return new JsonPrimitive(serializeCoordinate(world.x()) + ' ' + serializeCoordinate(world.y()) + ' ' + serializeCoordinate(world.z()));
      } else {
         throw new IllegalArgumentException("Don't know how to serialize " + src + " as a Position");
      }
   }

   private static BlockNbtComponent.WorldPos.Coordinate deserializeCoordinate(final String prefix, final String value) {
      int i = Integer.parseInt(value);
      if (prefix.isEmpty()) {
         return BlockNbtComponent.WorldPos.Coordinate.absolute(i);
      } else if (prefix.equals("~")) {
         return BlockNbtComponent.WorldPos.Coordinate.relative(i);
      } else {
         throw new AssertionError();
      }
   }

   private static String serializeLocal(final double value) {
      return "^" + value;
   }

   private static String serializeCoordinate(final BlockNbtComponent.WorldPos.Coordinate coordinate) {
      return (coordinate.type() == BlockNbtComponent.WorldPos.Coordinate.Type.RELATIVE ? "~" : "") + coordinate.value();
   }
}
