package com.sk89q.worldedit.extent.clipboard.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.BannerBlockCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.BedBlockCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.EntityNBTCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.FlowerPotCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.NBTCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.NoteBlockCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.Pre13HangingCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.SignCompatibilityHandler;
import com.sk89q.worldedit.extent.clipboard.io.legacycompat.SkullBlockCompatibilityHandler;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.storage.NBTConversions;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;

public class MCEditSchematicReader extends NBTSchematicReader {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final LinRootEntry root;
   private static final ImmutableList<NBTCompatibilityHandler> COMPATIBILITY_HANDLERS = ImmutableList.of(
      new SignCompatibilityHandler(),
      new FlowerPotCompatibilityHandler(),
      new NoteBlockCompatibilityHandler(),
      new SkullBlockCompatibilityHandler(),
      new BannerBlockCompatibilityHandler(),
      new BedBlockCompatibilityHandler()
   );
   private static final ImmutableList<EntityNBTCompatibilityHandler> ENTITY_COMPATIBILITY_HANDLERS = ImmutableList.of(new Pre13HangingCompatibilityHandler());

   public MCEditSchematicReader(NBTInputStream inputStream) {
      try {
         NamedTag tag = inputStream.readNamedTag();
         this.root = new LinRootEntry(tag.getName(), (LinCompoundTag)tag.getTag().toLinTag());
      } catch (IOException var3) {
         throw new UncheckedIOException(var3);
      }
   }

   MCEditSchematicReader(LinRootEntry root) {
      this.root = root;
   }

   @Override
   public Clipboard read() throws IOException {
      if (!this.root.name().equals("Schematic")) {
         throw new IOException("Tag 'Schematic' does not exist or is not first");
      } else {
         LinCompoundTag schematicTag = this.root.value();
         if (!schematicTag.value().containsKey("Blocks")) {
            throw new IOException("Schematic file is missing a 'Blocks' tag");
         } else {
            String materials = schematicTag.getTag("Materials", LinTagType.stringTag()).value();
            if (!materials.equals("Alpha")) {
               throw new IOException("Schematic file is not an Alpha schematic");
            } else {
               short width = schematicTag.getTag("Width", LinTagType.shortTag()).valueAsShort();
               short height = schematicTag.getTag("Height", LinTagType.shortTag()).valueAsShort();
               short length = schematicTag.getTag("Length", LinTagType.shortTag()).valueAsShort();

               BlockVector3 origin;
               Region region;
               try {
                  int originX = schematicTag.getTag("WEOriginX", LinTagType.intTag()).valueAsInt();
                  int originY = schematicTag.getTag("WEOriginY", LinTagType.intTag()).valueAsInt();
                  int originZ = schematicTag.getTag("WEOriginZ", LinTagType.intTag()).valueAsInt();
                  BlockVector3 min = BlockVector3.at(originX, originY, originZ);
                  int offsetX = schematicTag.getTag("WEOffsetX", LinTagType.intTag()).valueAsInt();
                  int offsetY = schematicTag.getTag("WEOffsetY", LinTagType.intTag()).valueAsInt();
                  int offsetZ = schematicTag.getTag("WEOffsetZ", LinTagType.intTag()).valueAsInt();
                  BlockVector3 offset = BlockVector3.at(offsetX, offsetY, offsetZ);
                  origin = min.subtract(offset);
                  region = new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE));
               } catch (NoSuchElementException var28) {
                  origin = BlockVector3.ZERO;
                  region = new CuboidRegion(origin, origin.add(width, height, length).subtract(BlockVector3.ONE));
               }

               byte[] blockId = schematicTag.getTag("Blocks", LinTagType.byteArrayTag()).value();
               byte[] blockData = schematicTag.getTag("Data", LinTagType.byteArrayTag()).value();
               byte[] addId = new byte[0];
               short[] blocks = new short[blockId.length];
               LinByteArrayTag addBlocks = schematicTag.findTag("AddBlocks", LinTagType.byteArrayTag());
               if (addBlocks != null) {
                  addId = addBlocks.value();
               }

               for (int index = 0; index < blockId.length; index++) {
                  if (index >> 1 >= addId.length) {
                     blocks[index] = (short)(blockId[index] & 255);
                  } else if ((index & 1) == 0) {
                     blocks[index] = (short)(((addId[index >> 1] & 15) << 8) + (blockId[index] & 255));
                  } else {
                     blocks[index] = (short)(((addId[index >> 1] & 240) << 4) + (blockId[index] & 255));
                  }
               }

               LinListTag<LinCompoundTag> tileEntityTag = schematicTag.findListTag("TileEntities", LinTagType.compoundTag());
               List<LinCompoundTag> tileEntities = tileEntityTag == null ? List.of() : tileEntityTag.value();
               BlockMap<BaseBlock> tileEntityBlocks = BlockMap.createForBaseBlock();

               for (LinCompoundTag tag : tileEntities) {
                  LinCompoundTag.Builder newTag = tag.toBuilder();
                  String id = tag.getTag("id", LinTagType.stringTag()).value();
                  newTag.putString("id", this.convertBlockEntityId(id));
                  int x = tag.getTag("x", LinTagType.intTag()).valueAsInt();
                  int y = tag.getTag("y", LinTagType.intTag()).valueAsInt();
                  int z = tag.getTag("z", LinTagType.intTag()).valueAsInt();
                  int indexx = y * width * length + z * width + x;
                  if (indexx >= blocks.length) {
                     LOGGER.warn("Skipping corrupt tile entity at position {} {} {} in schematic.", x, y, z);
                  } else {
                     BlockState block = this.getBlockState(blocks[indexx], blockData[indexx]);
                     if (block != null) {
                        BaseBlock updatedBlock = block.toBaseBlock(LazyReference.from(newTag::build));
                        UnmodifiableIterator data = COMPATIBILITY_HANDLERS.iterator();

                        while (true) {
                           if (data.hasNext()) {
                              NBTCompatibilityHandler handler = (NBTCompatibilityHandler)data.next();
                              updatedBlock = handler.updateNbt(updatedBlock);
                              if (updatedBlock.getNbtReference() != null) {
                                 continue;
                              }
                           }

                           tileEntityBlocks.put(BlockVector3.at(x, y, z), updatedBlock);
                           break;
                        }
                     }
                  }
               }

               BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
               clipboard.setOrigin(origin);
               Set<Integer> unknownBlocks = new HashSet<>();

               for (int x = 0; x < width; x++) {
                  for (int y = 0; y < height; y++) {
                     for (int z = 0; z < length; z++) {
                        int indexx = y * width * length + z * width + x;
                        BlockVector3 pt = BlockVector3.at(x, y, z);
                        BaseBlock state = tileEntityBlocks.get(pt);
                        if (state == null) {
                           BlockState blockState = this.getBlockState(blocks[indexx], blockData[indexx]);
                           if (blockState == null) {
                              short block = blocks[indexx];
                              byte data = blockData[indexx];
                              int combined = block << 8 | data;
                              if (unknownBlocks.add(combined)) {
                                 LOGGER.warn("Unknown block when loading schematic: {} {}. This is most likely abad schematic.", block, data);
                              }
                              continue;
                           }

                           state = blockState.toBaseBlock();
                        }

                        clipboard.setBlock(region.getMinimumPoint().add(pt), state);
                     }
                  }
               }

               LinListTag<LinCompoundTag> entityList = schematicTag.findListTag("Entities", LinTagType.compoundTag());
               if (entityList != null) {
                  for (LinCompoundTag tagx : entityList.value()) {
                     String id = this.convertEntityId(tagx.getTag("id", LinTagType.stringTag()).value());
                     Location location = NBTConversions.toLocation(
                        clipboard, tagx.getListTag("Pos", LinTagType.doubleTag()), tagx.getListTag("Rotation", LinTagType.floatTag())
                     );
                     if (!id.isEmpty()) {
                        EntityType entityType = EntityTypes.get(id.toLowerCase(Locale.ROOT));
                        if (entityType != null) {
                           UnmodifiableIterator var53 = ENTITY_COMPATIBILITY_HANDLERS.iterator();

                           while (var53.hasNext()) {
                              EntityNBTCompatibilityHandler compatibilityHandler = (EntityNBTCompatibilityHandler)var53.next();
                              tagx = compatibilityHandler.updateNbt(entityType, tagx);
                           }

                           BaseEntity state = new BaseEntity(entityType, LazyReference.computed(tagx));
                           clipboard.createEntity(location, state);
                        } else {
                           LOGGER.warn("Unknown entity when pasting schematic: " + id.toLowerCase(Locale.ROOT));
                        }
                     }
                  }
               }

               return clipboard;
            }
         }
      }
   }

   private String convertEntityId(String id) {
      switch (id) {
         case "AreaEffectCloud":
            return "area_effect_cloud";
         case "ArmorStand":
            return "armor_stand";
         case "CaveSpider":
            return "cave_spider";
         case "MinecartChest":
            return "chest_minecart";
         case "DragonFireball":
            return "dragon_fireball";
         case "ThrownEgg":
            return "egg";
         case "EnderDragon":
            return "ender_dragon";
         case "ThrownEnderpearl":
            return "ender_pearl";
         case "FallingSand":
            return "falling_block";
         case "FireworksRocketEntity":
            return "fireworks_rocket";
         case "MinecartFurnace":
            return "furnace_minecart";
         case "MinecartHopper":
            return "hopper_minecart";
         case "EntityHorse":
            return "horse";
         case "ItemFrame":
            return "item_frame";
         case "LeashKnot":
            return "leash_knot";
         case "LightningBolt":
            return "lightning_bolt";
         case "LavaSlime":
            return "magma_cube";
         case "MinecartRideable":
            return "minecart";
         case "MushroomCow":
            return "mooshroom";
         case "Ozelot":
            return "ocelot";
         case "PolarBear":
            return "polar_bear";
         case "ThrownPotion":
            return "potion";
         case "ShulkerBullet":
            return "shulker_bullet";
         case "SmallFireball":
            return "small_fireball";
         case "MinecartSpawner":
            return "spawner_minecart";
         case "SpectralArrow":
            return "spectral_arrow";
         case "PrimedTnt":
            return "tnt";
         case "MinecartTNT":
            return "tnt_minecart";
         case "VillagerGolem":
            return "villager_golem";
         case "WitherBoss":
            return "wither";
         case "WitherSkull":
            return "wither_skull";
         case "PigZombie":
            return "zombie_pigman";
         case "XPOrb":
         case "xp_orb":
            return "experience_orb";
         case "ThrownExpBottle":
         case "xp_bottle":
            return "experience_bottle";
         case "EyeOfEnderSignal":
         case "eye_of_ender_signal":
            return "eye_of_ender";
         case "EnderCrystal":
         case "ender_crystal":
            return "end_crystal";
         case "fireworks_rocket":
            return "firework_rocket";
         case "MinecartCommandBlock":
         case "commandblock_minecart":
            return "command_block_minecart";
         case "snowman":
            return "snow_golem";
         case "villager_golem":
            return "iron_golem";
         case "evocation_fangs":
            return "evoker_fangs";
         case "evocation_illager":
            return "evoker";
         case "vindication_illager":
            return "vindicator";
         case "illusion_illager":
            return "illusioner";
         default:
            return id;
      }
   }

   private String convertBlockEntityId(String id) {
      switch (id) {
         case "Cauldron":
            return "brewing_stand";
         case "Control":
            return "command_block";
         case "DLDetector":
            return "daylight_detector";
         case "Trap":
            return "dispenser";
         case "EnchantTable":
            return "enchanting_table";
         case "EndGateway":
            return "end_gateway";
         case "AirPortal":
            return "end_portal";
         case "EnderChest":
            return "ender_chest";
         case "FlowerPot":
            return "flower_pot";
         case "RecordPlayer":
            return "jukebox";
         case "MobSpawner":
            return "mob_spawner";
         case "Music":
         case "noteblock":
            return "note_block";
         case "Structure":
            return "structure_block";
         case "Chest":
            return "chest";
         case "Sign":
            return "sign";
         case "Banner":
            return "banner";
         case "Beacon":
            return "beacon";
         case "Comparator":
            return "comparator";
         case "Dropper":
            return "dropper";
         case "Furnace":
            return "furnace";
         case "Hopper":
            return "hopper";
         case "Skull":
            return "skull";
         default:
            return id;
      }
   }

   private BlockState getBlockState(int id, int data) {
      BlockState foundBlock = LegacyMapper.getInstance().getBlockFromLegacy(id, data);
      return foundBlock == null && data != 0 ? LegacyMapper.getInstance().getBlockFromLegacy(id, 0) : foundBlock;
   }

   @Override
   public void close() throws IOException {
   }
}
