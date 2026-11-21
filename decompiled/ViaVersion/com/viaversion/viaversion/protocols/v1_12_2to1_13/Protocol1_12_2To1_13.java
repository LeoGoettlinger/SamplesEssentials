package com.viaversion.viaversion.protocols.v1_12_2to1_13;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.protocols.base.ClientboundStatusPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.ConnectionData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.providers.BlockConnectionProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.providers.PacketBlockConnectionProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.BlockIdData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.MappingData1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.RecipeData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.StatisticData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.StatisticMappings1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.BlockEntityProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.PaintingProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.PlayerLookTargetProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.ComponentRewriter1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.EntityPacketRewriter1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.ItemPacketRewriter1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.WorldPacketRewriter1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.storage.BlockConnectionStorage;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.storage.ProtocolStorables1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.storage.TabCompleteTracker;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ServerboundPackets1_12_1;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.IdAndData;
import com.viaversion.viaversion.util.ProtocolLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

public class Protocol1_12_2To1_13 extends AbstractProtocol<ClientboundPackets1_12_1, ClientboundPackets1_13, ServerboundPackets1_12_1, ServerboundPackets1_13> {
   public static final MappingData1_13 MAPPINGS = new MappingData1_13();
   public static final ProtocolLogger LOGGER = new ProtocolLogger(Protocol1_12_2To1_13.class);
   private static final Map<Character, Character> SCOREBOARD_TEAM_NAME_REWRITE = new HashMap<>();
   private static final Set<Character> FORMATTING_CODES = Sets.newHashSet(new Character[]{'k', 'l', 'm', 'n', 'o', 'r'});
   private final EntityPacketRewriter1_13 entityRewriter = new EntityPacketRewriter1_13(this);
   private final ItemPacketRewriter1_13 itemRewriter = new ItemPacketRewriter1_13(this);
   private final ComponentRewriter1_13<ClientboundPackets1_12_1> componentRewriter = new ComponentRewriter1_13<>(this);
   public static final PacketHandler POS_TO_3_INT = wrapper -> {
      BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_8);
      wrapper.write(Types.INT, position.x());
      wrapper.write(Types.INT, position.y());
      wrapper.write(Types.INT, position.z());
   };
   public static final PacketHandler SEND_DECLARE_COMMANDS_AND_TAGS = w -> {
      w.create(ClientboundPackets1_13.COMMANDS, wrapper -> {
         wrapper.write(Types.VAR_INT, 2);
         wrapper.write(Types.BYTE, (byte)0);
         wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{1});
         wrapper.write(Types.BYTE, (byte)22);
         wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[0]);
         wrapper.write(Types.STRING, "args");
         wrapper.write(Types.STRING, "brigadier:string");
         wrapper.write(Types.VAR_INT, 2);
         wrapper.write(Types.STRING, "minecraft:ask_server");
         wrapper.write(Types.VAR_INT, 0);
      }).scheduleSend(Protocol1_12_2To1_13.class);
      PacketWrapper tagsPacket = w.create(ClientboundPackets1_13.UPDATE_TAGS, wrapper -> {
         wrapper.write(Types.VAR_INT, MAPPINGS.getBlockTags().size());

         for (Entry<String, int[]> tag : MAPPINGS.getBlockTags().entrySet()) {
            wrapper.write(Types.STRING, tag.getKey());
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, (int[])tag.getValue().clone());
         }

         wrapper.write(Types.VAR_INT, MAPPINGS.getItemTags().size());

         for (Entry<String, int[]> tag : MAPPINGS.getItemTags().entrySet()) {
            wrapper.write(Types.STRING, tag.getKey());
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, (int[])tag.getValue().clone());
         }

         wrapper.write(Types.VAR_INT, MAPPINGS.getFluidTags().size());

         for (Entry<String, int[]> tag : MAPPINGS.getFluidTags().entrySet()) {
            wrapper.write(Types.STRING, tag.getKey());
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, (int[])tag.getValue().clone());
         }
      });
      if (w.user().getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_20_5)) {
         tagsPacket.send(Protocol1_12_2To1_13.class);
      } else {
         tagsPacket.scheduleSend(Protocol1_12_2To1_13.class);
      }
   };

   public Protocol1_12_2To1_13() {
      super(ClientboundPackets1_12_1.class, ClientboundPackets1_13.class, ServerboundPackets1_12_1.class, ServerboundPackets1_13.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      WorldPacketRewriter1_13.register(this);
      this.registerClientbound(State.STATUS, ClientboundStatusPackets.STATUS_RESPONSE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.handler(wrapper -> {
               String response = wrapper.get(Types.STRING, 0);

               try {
                  JsonObject json = GsonUtil.getGson().fromJson(response, JsonObject.class);
                  if (json.has("favicon")) {
                     json.addProperty("favicon", json.get("favicon").getAsString().replace("\n", ""));
                  }

                  wrapper.set(Types.STRING, 0, GsonUtil.getGson().toJson((JsonElement)json));
               } catch (JsonParseException var3) {
                  Protocol1_12_2To1_13.LOGGER.log(Level.SEVERE, "Error transforming status response", var3);
               }
            });
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.AWARD_STATS, wrapper -> {
         int size = wrapper.read(Types.VAR_INT);
         List<StatisticData> remappedStats = new ArrayList<>();

         for (int i = 0; i < size; i++) {
            String name = wrapper.read(Types.STRING);
            String[] split = name.split("\\.");
            int categoryId = 0;
            int newId = -1;
            int value = wrapper.read(Types.VAR_INT);
            if (split.length == 2) {
               categoryId = 8;
               Integer newIdRaw = StatisticMappings1_13.CUSTOM_STATS.get(name);
               if (newIdRaw != null) {
                  newId = newIdRaw;
               } else {
                  LOGGER.warning("Could not find statistic mapping for " + name);
               }
            } else if (split.length > 2) {
               String category = split[1];

               categoryId = switch (category) {
                  case "mineBlock" -> 0;
                  case "craftItem" -> 1;
                  case "useItem" -> 2;
                  case "breakItem" -> 3;
                  case "pickup" -> 4;
                  case "drop" -> 5;
                  case "killEntity" -> 6;
                  case "entityKilledBy" -> 7;
                  default -> categoryId;
               };
            }

            if (newId != -1) {
               remappedStats.add(new StatisticData(categoryId, newId, value));
            }
         }

         wrapper.write(Types.VAR_INT, remappedStats.size());

         for (StatisticData stat : remappedStats) {
            wrapper.write(Types.VAR_INT, stat.categoryId());
            wrapper.write(Types.VAR_INT, stat.newId());
            wrapper.write(Types.VAR_INT, stat.value());
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.COMMAND_SUGGESTIONS, wrapper -> {
         ProtocolStorables1_13 storables = wrapper.user().storables(this);
         wrapper.write(Types.VAR_INT, storables.tabCompleteTracker().getTransactionId());
         String input = storables.tabCompleteTracker().getInput();
         int index;
         int length;
         if (!input.endsWith(" ") && !input.isEmpty()) {
            int lastSpace = input.lastIndexOf(32) + 1;
            index = lastSpace;
            length = input.length() - lastSpace;
         } else {
            index = input.length();
            length = 0;
         }

         wrapper.write(Types.VAR_INT, index);
         wrapper.write(Types.VAR_INT, length);
         int count = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < count; i++) {
            String suggestion = wrapper.read(Types.STRING);
            if (suggestion.startsWith("/") && index == 0) {
               suggestion = suggestion.substring(1);
            }

            wrapper.write(Types.STRING, suggestion);
            wrapper.write(Types.OPTIONAL_COMPONENT, null);
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.OPEN_SCREEN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.STRING);
            this.handler(wrapper -> Protocol1_12_2To1_13.this.componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT)));
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.COOLDOWN, wrapper -> {
         int item = wrapper.read(Types.VAR_INT);
         int ticks = wrapper.read(Types.VAR_INT);
         wrapper.cancel();
         if (item == 383) {
            for (int i = 0; i < 44; i++) {
               int newItem = MAPPINGS.getItemMappings().getNewId(item << 16 | i);
               if (newItem == -1) {
                  break;
               }

               PacketWrapper packet = wrapper.create(ClientboundPackets1_13.COOLDOWN);
               packet.write(Types.VAR_INT, newItem);
               packet.write(Types.VAR_INT, ticks);
               packet.send(Protocol1_12_2To1_13.class);
            }
         } else {
            for (int i = 0; i < 16; i++) {
               int newItem = MAPPINGS.getItemMappings().getNewId(IdAndData.toRawData(item, i));
               if (newItem == -1) {
                  break;
               }

               PacketWrapper packet = wrapper.create(ClientboundPackets1_13.COOLDOWN);
               packet.write(Types.VAR_INT, newItem);
               packet.write(Types.VAR_INT, ticks);
               packet.send(Protocol1_12_2To1_13.class);
            }
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.LEVEL_EVENT, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.BLOCK_POSITION1_8);
            this.map(Types.INT);
            this.handler(wrapper -> {
               int id = wrapper.get(Types.INT, 0);
               int data = wrapper.get(Types.INT, 1);
               if (id == 1010) {
                  wrapper.set(Types.INT, 1, Protocol1_12_2To1_13.this.getMappingData().getItemMappings().getNewId(IdAndData.toRawData(data)));
               } else if (id == 2001) {
                  int blockId = data & 4095;
                  int blockData = data >> 12;
                  wrapper.set(Types.INT, 1, WorldPacketRewriter1_13.toNewId(IdAndData.toRawData(blockId, blockData)));
               }
            });
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.PLACE_GHOST_RECIPE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.BYTE);
            this.handler(wrapper -> wrapper.write(Types.STRING, "viaversion:legacy/" + wrapper.read(Types.VAR_INT)));
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.MAP_ITEM_DATA, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.handler(wrapper -> {
               int iconCount = wrapper.passthrough(Types.VAR_INT);

               for (int i = 0; i < iconCount; i++) {
                  byte directionAndType = wrapper.read(Types.BYTE);
                  int type = (directionAndType & 240) >> 4;
                  wrapper.write(Types.VAR_INT, type);
                  wrapper.passthrough(Types.BYTE);
                  wrapper.passthrough(Types.BYTE);
                  byte direction = (byte)(directionAndType & 15);
                  wrapper.write(Types.BYTE, direction);
                  wrapper.write(Types.OPTIONAL_COMPONENT, null);
               }
            });
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.RECIPE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.handler(wrapper -> {
               wrapper.write(Types.BOOLEAN, false);
               wrapper.write(Types.BOOLEAN, false);
            });
            this.handler(wrapper -> {
               int action = wrapper.get(Types.VAR_INT, 0);

               for (int i = 0; i < (action == 0 ? 2 : 1); i++) {
                  int[] ids = wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
                  String[] stringIds = new String[ids.length];

                  for (int j = 0; j < ids.length; j++) {
                     stringIds[j] = "viaversion:legacy/" + ids[j];
                  }

                  wrapper.write(Types.STRING_ARRAY, stringIds);
               }

               if (action == 0) {
                  wrapper.create(ClientboundPackets1_13.UPDATE_RECIPES, w -> Protocol1_12_2To1_13.this.writeDeclareRecipes(w)).send(Protocol1_12_2To1_13.class);
               }
            });
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.SET_OBJECTIVE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.map(Types.BYTE);
            this.handler(wrapper -> {
               byte mode = wrapper.get(Types.BYTE, 0);
               if (mode == 0 || mode == 2) {
                  String value = wrapper.read(Types.STRING);
                  wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(value));
                  String type = wrapper.read(Types.STRING);
                  wrapper.write(Types.VAR_INT, type.equals("integer") ? 0 : 1);
               }
            });
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.SET_PLAYER_TEAM, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.STRING);
            this.map(Types.BYTE);
            this.handler(wrapper -> {
               byte action = wrapper.get(Types.BYTE, 0);
               if (action == 0 || action == 2) {
                  String displayName = wrapper.read(Types.STRING);
                  wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(displayName));
                  String prefix = wrapper.read(Types.STRING);
                  String suffix = wrapper.read(Types.STRING);
                  wrapper.passthrough(Types.BYTE);
                  wrapper.passthrough(Types.STRING);
                  wrapper.passthrough(Types.STRING);
                  int colour = wrapper.read(Types.BYTE).intValue();
                  if (colour == -1) {
                     colour = 21;
                  }

                  if (Via.getConfig().is1_13TeamColourFix()) {
                     char lastColorChar = Protocol1_12_2To1_13.this.getLastColorChar(prefix);
                     colour = ChatColorUtil.getColorOrdinal(lastColorChar);
                     suffix = "§" + Character.toString(lastColorChar) + suffix;
                  }

                  wrapper.write(Types.VAR_INT, colour);
                  wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(prefix));
                  wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(suffix));
               }

               if (action == 0 || action == 3 || action == 4) {
                  String[] names = wrapper.read(Types.STRING_ARRAY);

                  for (int i = 0; i < names.length; i++) {
                     names[i] = Protocol1_12_2To1_13.this.rewriteTeamMemberName(names[i]);
                  }

                  wrapper.write(Types.STRING_ARRAY, names);
               }
            });
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.SET_SCORE, wrapper -> {
         String displayName = wrapper.read(Types.STRING);
         displayName = this.rewriteTeamMemberName(displayName);
         wrapper.write(Types.STRING, displayName);
      });
      this.registerClientbound(ClientboundPackets1_12_1.PLAYER_INFO, wrapper -> {
         int action = wrapper.passthrough(Types.VAR_INT);
         int count = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < count; i++) {
            wrapper.passthrough(Types.UUID);
            if (action == 0) {
               String playerName = wrapper.read(Types.STRING);
               wrapper.write(Types.STRING, this.rewriteTeamMemberName(playerName));
               wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
               wrapper.passthrough(Types.VAR_INT);
               wrapper.passthrough(Types.VAR_INT);
               JsonElement displayName = wrapper.passthrough(Types.OPTIONAL_COMPONENT);
               if (displayName != null) {
                  this.componentRewriter.processText(wrapper.user(), displayName);
               }
            } else if (action == 1 || action == 2) {
               wrapper.passthrough(Types.VAR_INT);
            } else if (action == 3) {
               JsonElement displayName = wrapper.passthrough(Types.OPTIONAL_COMPONENT);
               if (displayName != null) {
                  this.componentRewriter.processText(wrapper.user(), displayName);
               }
            }
         }
      });
      this.registerClientbound(ClientboundPackets1_12_1.UPDATE_ADVANCEMENTS, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               this.componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
               this.componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
               Item icon = wrapper.read(Types.ITEM1_8);
               this.itemRewriter.handleItemToClient(wrapper.user(), icon);
               wrapper.write(Types.ITEM1_13, icon);
               wrapper.passthrough(Types.VAR_INT);
               int flags = wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  wrapper.passthrough(Types.STRING);
               }

               wrapper.passthrough(Types.FLOAT);
               wrapper.passthrough(Types.FLOAT);
            }

            wrapper.passthrough(Types.STRING_ARRAY);
            int arrayLength = wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < arrayLength; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }
         }
      });
      this.cancelServerbound(State.LOGIN, ServerboundLoginPackets.CUSTOM_QUERY_ANSWER.getId());
      this.cancelServerbound(ServerboundPackets1_13.BLOCK_ENTITY_TAG_QUERY);
      this.registerServerbound(ServerboundPackets1_13.COMMAND_SUGGESTION, new PacketHandlers() {
         @Override
         public void register() {
            this.handler(wrapper -> {
               if (Via.getConfig().isDisable1_13AutoComplete()) {
                  wrapper.cancel();
               }

               int tid = wrapper.read(Types.VAR_INT);
               ProtocolStorables1_13 storables = wrapper.user().storables(Protocol1_12_2To1_13.this);
               storables.tabCompleteTracker().setTransactionId(tid);
            });
            this.map(Types.STRING, new ValueTransformer<String, String>(Types.STRING) {
               public String transform(PacketWrapper wrapper, String inputValue) {
                  ProtocolStorables1_13 storables = wrapper.user().storables(Protocol1_12_2To1_13.this);
                  storables.tabCompleteTracker().setInput(inputValue);
                  return "/" + inputValue;
               }
            });
            this.handler(wrapper -> {
               wrapper.write(Types.BOOLEAN, false);
               BlockPosition playerLookTarget = Via.getManager().getProviders().get(PlayerLookTargetProvider.class).getPlayerLookTarget(wrapper.user());
               wrapper.write(Types.OPTIONAL_POSITION1_8, playerLookTarget);
               if (!wrapper.isCancelled() && Via.getConfig().get1_13TabCompleteDelay() > 0) {
                  ProtocolStorables1_13 storables = wrapper.user().storables(Protocol1_12_2To1_13.this);
                  TabCompleteTracker tracker = storables.tabCompleteTracker();
                  wrapper.cancel();
                  tracker.setTimeToSend(System.currentTimeMillis() + Via.getConfig().get1_13TabCompleteDelay() * 50L);
                  tracker.setLastTabComplete(wrapper.get(Types.STRING, 0));
               }
            });
         }
      });
      this.registerServerbound(ServerboundPackets1_13.EDIT_BOOK, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, wrapper -> {
         Item item = wrapper.read(Types.ITEM1_13);
         boolean isSigning = wrapper.read(Types.BOOLEAN);
         this.itemRewriter.handleItemToServer(wrapper.user(), item);
         wrapper.write(Types.STRING, isSigning ? "MC|BSign" : "MC|BEdit");
         wrapper.write(Types.ITEM1_8, item);
      });
      this.cancelServerbound(ServerboundPackets1_13.ENTITY_TAG_QUERY);
      this.registerServerbound(ServerboundPackets1_13.PICK_ITEM, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, wrapper -> wrapper.write(Types.STRING, "MC|PickItem"));
      this.registerServerbound(ServerboundPackets1_13.PLACE_RECIPE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.BYTE);
            this.handler(wrapper -> {
               String s = wrapper.read(Types.STRING);
               Integer id;
               if (s.length() >= 19 && (id = Ints.tryParse(s.substring(18))) != null) {
                  wrapper.write(Types.VAR_INT, id);
               } else {
                  wrapper.cancel();
               }
            });
         }
      });
      this.registerServerbound(ServerboundPackets1_13.RECIPE_BOOK_UPDATE, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.VAR_INT);
            this.handler(wrapper -> {
               int type = wrapper.get(Types.VAR_INT, 0);
               if (type == 0) {
                  String s = wrapper.read(Types.STRING);
                  Integer id;
                  if (s.length() < 19 || (id = Ints.tryParse(s.substring(18))) == null) {
                     wrapper.cancel();
                     return;
                  }

                  wrapper.write(Types.INT, id);
               }

               if (type == 1) {
                  wrapper.passthrough(Types.BOOLEAN);
                  wrapper.passthrough(Types.BOOLEAN);
                  wrapper.read(Types.BOOLEAN);
                  wrapper.read(Types.BOOLEAN);
               }
            });
         }
      });
      this.registerServerbound(
         ServerboundPackets1_13.RENAME_ITEM, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, wrapper -> wrapper.write(Types.STRING, "MC|ItemName")
      );
      this.registerServerbound(ServerboundPackets1_13.SELECT_TRADE, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
         @Override
         public void register() {
            this.create(Types.STRING, "MC|TrSel");
            this.map(Types.VAR_INT, Types.INT);
         }
      });
      this.registerServerbound(ServerboundPackets1_13.SET_BEACON, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
         @Override
         public void register() {
            this.create(Types.STRING, "MC|Beacon");
            this.map(Types.VAR_INT, Types.INT);
            this.map(Types.VAR_INT, Types.INT);
         }
      });
      this.registerServerbound(ServerboundPackets1_13.SET_COMMAND_BLOCK, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
         @Override
         public void register() {
            this.create(Types.STRING, "MC|AutoCmd");
            this.handler(Protocol1_12_2To1_13.POS_TO_3_INT);
            this.map(Types.STRING);
            this.handler(wrapper -> {
               int mode = wrapper.read(Types.VAR_INT);
               byte flags = wrapper.read(Types.BYTE);
               String stringMode = mode == 0 ? "SEQUENCE" : (mode == 1 ? "AUTO" : "REDSTONE");
               wrapper.write(Types.BOOLEAN, (flags & 1) != 0);
               wrapper.write(Types.STRING, stringMode);
               wrapper.write(Types.BOOLEAN, (flags & 2) != 0);
               wrapper.write(Types.BOOLEAN, (flags & 4) != 0);
            });
         }
      });
      this.registerServerbound(ServerboundPackets1_13.SET_COMMAND_MINECART, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
         @Override
         public void register() {
            this.handler(wrapper -> {
               wrapper.write(Types.STRING, "MC|AdvCmd");
               wrapper.write(Types.BYTE, (byte)1);
            });
            this.map(Types.VAR_INT, Types.INT);
         }
      });
      this.registerServerbound(ServerboundPackets1_13.SET_STRUCTURE_BLOCK, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
         @Override
         public void register() {
            this.create(Types.STRING, "MC|Struct");
            this.handler(Protocol1_12_2To1_13.POS_TO_3_INT);
            this.map(Types.VAR_INT, new ValueTransformer<Integer, Byte>(Types.BYTE) {
               public Byte transform(PacketWrapper wrapper, Integer action) {
                  return (byte)(action + 1);
               }
            });
            this.map(Types.VAR_INT, new ValueTransformer<Integer, String>(Types.STRING) {
               public String transform(PacketWrapper wrapper, Integer mode) {
                  return mode == 0 ? "SAVE" : (mode == 1 ? "LOAD" : (mode == 2 ? "CORNER" : "DATA"));
               }
            });
            this.map(Types.STRING);
            this.map(Types.BYTE, Types.INT);
            this.map(Types.BYTE, Types.INT);
            this.map(Types.BYTE, Types.INT);
            this.map(Types.BYTE, Types.INT);
            this.map(Types.BYTE, Types.INT);
            this.map(Types.BYTE, Types.INT);
            this.map(Types.VAR_INT, new ValueTransformer<Integer, String>(Types.STRING) {
               public String transform(PacketWrapper wrapper, Integer mirror) {
                  return mirror == 0 ? "NONE" : (mirror == 1 ? "LEFT_RIGHT" : "FRONT_BACK");
               }
            });
            this.map(Types.VAR_INT, new ValueTransformer<Integer, String>(Types.STRING) {
               public String transform(PacketWrapper wrapper, Integer rotation) {
                  return rotation == 0 ? "NONE" : (rotation == 1 ? "CLOCKWISE_90" : (rotation == 2 ? "CLOCKWISE_180" : "COUNTERCLOCKWISE_90"));
               }
            });
            this.map(Types.STRING);
            this.handler(wrapper -> {
               float integrity = wrapper.read(Types.FLOAT);
               long seed = wrapper.read(Types.VAR_LONG);
               byte flags = wrapper.read(Types.BYTE);
               wrapper.write(Types.BOOLEAN, (flags & 1) != 0);
               wrapper.write(Types.BOOLEAN, (flags & 2) != 0);
               wrapper.write(Types.BOOLEAN, (flags & 4) != 0);
               wrapper.write(Types.FLOAT, integrity);
               wrapper.write(Types.VAR_LONG, seed);
            });
         }
      });
   }

   private void writeDeclareRecipes(PacketWrapper recipesPacket) {
      recipesPacket.write(Types.VAR_INT, RecipeData.recipes.size());

      for (Entry<String, RecipeData.Recipe> entry : RecipeData.recipes.entrySet()) {
         RecipeData.Recipe recipe = entry.getValue();
         recipesPacket.write(Types.STRING, entry.getKey());
         recipesPacket.write(Types.STRING, recipe.type());
         String var5 = recipe.type();
         switch (var5) {
            case "crafting_shapeless":
               recipesPacket.write(Types.STRING, recipe.group());
               recipesPacket.write(Types.VAR_INT, recipe.ingredients().length);

               for (Item[] ingredient : recipe.ingredients()) {
                  Item[] clone = new Item[ingredient.length];

                  for (int i = 0; i < ingredient.length; i++) {
                     if (ingredient[i] != null) {
                        clone[i] = ingredient[i].copy();
                     }
                  }

                  recipesPacket.write(Types.ITEM1_13_ARRAY, clone);
               }

               recipesPacket.write(Types.ITEM1_13, recipe.result().copy());
               break;
            case "crafting_shaped":
               recipesPacket.write(Types.VAR_INT, recipe.width());
               recipesPacket.write(Types.VAR_INT, recipe.height());
               recipesPacket.write(Types.STRING, recipe.group());

               for (Item[] ingredient : recipe.ingredients()) {
                  Item[] clone = new Item[ingredient.length];

                  for (int ix = 0; ix < ingredient.length; ix++) {
                     if (ingredient[ix] != null) {
                        clone[ix] = ingredient[ix].copy();
                     }
                  }

                  recipesPacket.write(Types.ITEM1_13_ARRAY, clone);
               }

               recipesPacket.write(Types.ITEM1_13, recipe.result().copy());
               break;
            case "smelting":
               recipesPacket.write(Types.STRING, recipe.group());
               Item[] ingredient = new Item[recipe.ingredient().length];

               for (int ixx = 0; ixx < ingredient.length; ixx++) {
                  if (recipe.ingredient()[ixx] != null) {
                     ingredient[ixx] = recipe.ingredient()[ixx].copy();
                  }
               }

               recipesPacket.write(Types.ITEM1_13_ARRAY, ingredient);
               recipesPacket.write(Types.ITEM1_13, recipe.result().copy());
               recipesPacket.write(Types.FLOAT, recipe.experience());
               recipesPacket.write(Types.VAR_INT, recipe.cookingTime());
         }
      }
   }

   @Override
   protected void onMappingDataLoaded() {
      ConnectionData.init();
      RecipeData.init();
      BlockIdData.init();
      Types1_13.PARTICLE
         .rawFiller()
         .reader(3, ParticleType.Readers.BLOCK)
         .reader(20, ParticleType.Readers.DUST)
         .reader(11, ParticleType.Readers.DUST)
         .reader(27, ParticleType.Readers.ITEM1_13);
      if (Via.getConfig().isServersideBlockConnections()
         && Via.getManager().getProviders().get(BlockConnectionProvider.class) instanceof PacketBlockConnectionProvider) {
         BlockConnectionStorage.init();
      }

      super.onMappingDataLoaded();
   }

   @Override
   public void init(UserConnection userConnection) {
      this.addEntityTracker(userConnection, new EntityTrackerBase(userConnection, EntityTypes1_13.EntityType.PLAYER));
      ProtocolStorables1_13 storables = userConnection.storables(this);
      storables.setClientWorld(new ClientWorld());
      userConnection.put(storables.tabCompleteTracker());
      userConnection.put(storables.blockStorage());
      if (Via.getConfig().isServersideBlockConnections()
         && Via.getManager().getProviders().get(BlockConnectionProvider.class) instanceof PacketBlockConnectionProvider) {
         BlockConnectionStorage blockConnectionStorage = new BlockConnectionStorage();
         storables.setBlockConnectionStorage(blockConnectionStorage);
         userConnection.put(blockConnectionStorage);
      }
   }

   public ProtocolStorables1_13 createStorables() {
      return new ProtocolStorables1_13();
   }

   @Override
   public void register(ViaProviders providers) {
      providers.register(BlockEntityProvider.class, new BlockEntityProvider(this));
      providers.register(PaintingProvider.class, new PaintingProvider());
      providers.register(PlayerLookTargetProvider.class, new PlayerLookTargetProvider());
   }

   public char getLastColorChar(String input) {
      int length = input.length();

      for (int index = length - 1; index > -1; index--) {
         char section = input.charAt(index);
         if (section == 167 && index < length - 1) {
            char c = input.charAt(index + 1);
            if (ChatColorUtil.isColorCode(c) && !FORMATTING_CODES.contains(c)) {
               return c;
            }
         }
      }

      return 'r';
   }

   protected String rewriteTeamMemberName(String name) {
      if (ChatColorUtil.stripColor(name).isEmpty()) {
         StringBuilder newName = new StringBuilder();

         for (int i = 1; i < name.length(); i += 2) {
            char colorChar = name.charAt(i);
            Character rewrite = SCOREBOARD_TEAM_NAME_REWRITE.get(colorChar);
            if (rewrite == null) {
               rewrite = colorChar;
            }

            newName.append('§').append(rewrite);
         }

         name = newName.toString();
      }

      return name;
   }

   public MappingData1_13 getMappingData() {
      return MAPPINGS;
   }

   @Override
   public ProtocolLogger getLogger() {
      return LOGGER;
   }

   public EntityPacketRewriter1_13 getEntityRewriter() {
      return this.entityRewriter;
   }

   public ItemPacketRewriter1_13 getItemRewriter() {
      return this.itemRewriter;
   }

   public ComponentRewriter1_13 getComponentRewriter() {
      return this.componentRewriter;
   }

   static {
      SCOREBOARD_TEAM_NAME_REWRITE.put('0', 'g');
      SCOREBOARD_TEAM_NAME_REWRITE.put('1', 'h');
      SCOREBOARD_TEAM_NAME_REWRITE.put('2', 'i');
      SCOREBOARD_TEAM_NAME_REWRITE.put('3', 'j');
      SCOREBOARD_TEAM_NAME_REWRITE.put('4', 'p');
      SCOREBOARD_TEAM_NAME_REWRITE.put('5', 'q');
      SCOREBOARD_TEAM_NAME_REWRITE.put('6', 's');
      SCOREBOARD_TEAM_NAME_REWRITE.put('7', 't');
      SCOREBOARD_TEAM_NAME_REWRITE.put('8', 'u');
      SCOREBOARD_TEAM_NAME_REWRITE.put('9', 'v');
      SCOREBOARD_TEAM_NAME_REWRITE.put('a', 'w');
      SCOREBOARD_TEAM_NAME_REWRITE.put('b', 'x');
      SCOREBOARD_TEAM_NAME_REWRITE.put('c', 'y');
      SCOREBOARD_TEAM_NAME_REWRITE.put('d', 'z');
      SCOREBOARD_TEAM_NAME_REWRITE.put('e', '!');
      SCOREBOARD_TEAM_NAME_REWRITE.put('f', '?');
      SCOREBOARD_TEAM_NAME_REWRITE.put('k', '#');
      SCOREBOARD_TEAM_NAME_REWRITE.put('l', '(');
      SCOREBOARD_TEAM_NAME_REWRITE.put('m', ')');
      SCOREBOARD_TEAM_NAME_REWRITE.put('n', ':');
      SCOREBOARD_TEAM_NAME_REWRITE.put('o', ';');
      SCOREBOARD_TEAM_NAME_REWRITE.put('r', '/');
   }
}
