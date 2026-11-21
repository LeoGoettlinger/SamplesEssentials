package com.fastasyncworldedit.core.util;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.filter.block.SingleFilterBlock;
import com.fastasyncworldedit.core.util.image.ImageUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.Logger;

public class TextureUtil implements TextureHolder {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final int[] FACTORS = new int[766];
   private final File folder;
   private final TextureUtil.BiomeColor[] biomes = new TextureUtil.BiomeColor[]{
      new TextureUtil.BiomeColor(0, "ocean", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(1, "plains", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(2, "desert", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(3, "mountains", 0.2F, 0.3F, 9616729, 7842607),
      new TextureUtil.BiomeColor(4, "forest", 0.7F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(5, "taiga", 0.25F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(6, "swamp", 0.8F, 0.9F, 9616729, 7842607),
      new TextureUtil.BiomeColor(7, "river", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(8, "nether", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(9, "the_end", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(10, "frozen_ocean", 0.0F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(11, "frozen_river", 0.0F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(12, "snowy_tundra", 0.0F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(13, "snowy_mountains", 0.0F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(14, "mushroom_fields", 0.9F, 1.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(15, "mushroom_field_shore", 0.9F, 1.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(16, "beach", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(17, "desert_hills", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(18, "wooded_hills", 0.7F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(19, "taiga_hills", 0.25F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(20, "mountain_edge", 0.2F, 0.3F, 9616729, 7842607),
      new TextureUtil.BiomeColor(21, "jungle", 0.95F, 0.9F, 9616729, 7842607),
      new TextureUtil.BiomeColor(22, "jungle_hills", 0.95F, 0.9F, 9616729, 7842607),
      new TextureUtil.BiomeColor(23, "jungle_edge", 0.95F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(24, "deep_ocean", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(25, "stone_shore", 0.2F, 0.3F, 9616729, 7842607),
      new TextureUtil.BiomeColor(26, "snowy_beach", 0.05F, 0.3F, 9616729, 7842607),
      new TextureUtil.BiomeColor(27, "birch_forest", 0.6F, 0.6F, 9616729, 7842607),
      new TextureUtil.BiomeColor(28, "birch_forest_hills", 0.6F, 0.6F, 9616729, 7842607),
      new TextureUtil.BiomeColor(29, "dark_forest", 0.7F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(30, "snowy_taiga", -0.5F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(31, "snowy_taiga_hills", -0.5F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(32, "giant_tree_taiga", 0.3F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(33, "giant_tree_taiga_hills", 0.3F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(34, "wooded_mountains", 0.2F, 0.3F, 9616729, 7842607),
      new TextureUtil.BiomeColor(35, "savanna", 1.2F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(36, "savanna_plateau", 1.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(37, "badlands", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(38, "wooded_badlands_plateau", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(39, "badlands_plateau", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(40, "small_end_islands", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(41, "end_midlands", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(42, "end_highlands", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(43, "end_barrens", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(44, "warm_ocean", 0.8F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(45, "lukewarm_ocean", 0.8F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(46, "cold_ocean", 0.8F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(47, "deep_warm_ocean", 0.8F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(48, "deep_lukewarm_ocean", 0.8F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(49, "deep_cold_ocean", 0.8F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(50, "deep_frozen_ocean", 0.8F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(51, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(52, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(53, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(54, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(55, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(56, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(57, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(58, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(59, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(60, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(61, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(62, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(63, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(64, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(65, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(66, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(67, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(68, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(69, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(70, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(71, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(72, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(73, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(74, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(75, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(76, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(77, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(78, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(79, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(80, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(81, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(82, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(83, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(84, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(85, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(86, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(87, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(88, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(89, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(90, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(91, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(92, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(93, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(94, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(95, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(96, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(97, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(98, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(99, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(100, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(101, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(102, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(103, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(104, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(105, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(106, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(107, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(108, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(109, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(110, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(111, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(112, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(113, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(114, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(115, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(116, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(117, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(118, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(119, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(120, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(121, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(122, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(123, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(124, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(125, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(126, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(127, "the_void", 0.5F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(128, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(129, "sunflower_plains", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(130, "desert_lakes", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(131, "gravelly_mountains", 0.2F, 0.3F, 9616729, 7842607),
      new TextureUtil.BiomeColor(132, "flower_forest", 0.7F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(133, "taiga_mountains", 0.25F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(134, "swamp_hills", 0.8F, 0.9F, 9616729, 7842607),
      new TextureUtil.BiomeColor(135, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(136, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(137, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(138, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(139, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(140, "ice_spikes", 0.0F, 0.5F, 9616729, 7842607),
      new TextureUtil.BiomeColor(141, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(142, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(143, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(144, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(145, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(146, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(147, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(148, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(149, "modified_jungle", 0.95F, 0.9F, 9616729, 7842607),
      new TextureUtil.BiomeColor(150, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(151, "modified_jungle_edge", 0.95F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(152, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(153, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(154, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(155, "tall_birch_forest", 0.6F, 0.6F, 9616729, 7842607),
      new TextureUtil.BiomeColor(156, "tall_birch_hills", 0.6F, 0.6F, 9616729, 7842607),
      new TextureUtil.BiomeColor(157, "dark_forest_hills", 0.7F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(158, "snowy_taiga_mountains", -0.5F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(159, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(160, "giant_spruce_taiga", 0.25F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(161, "giant_spruce_taiga_hills", 0.25F, 0.8F, 9616729, 7842607),
      new TextureUtil.BiomeColor(162, "gravelly_mountains+", 0.2F, 0.3F, 9616729, 7842607),
      new TextureUtil.BiomeColor(163, "shattered_savanna", 1.1F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(164, "shattered_savanna_plateau", 1.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(165, "eroded_badlands", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(166, "modified_wooded_badlands_plateau", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(167, "modified_badlands_plateau", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(168, "bamboo_jungle", 0.95F, 0.9F, 9616729, 7842607),
      new TextureUtil.BiomeColor(168, "bamboo_jungle_hills", 0.95F, 0.9F, 9616729, 7842607),
      new TextureUtil.BiomeColor(170, "soul_sand_valley", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(171, "crimson_forest", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(172, "warped_forest", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(173, "basalt_deltas", 2.0F, 0.0F, 9616729, 7842607),
      new TextureUtil.BiomeColor(174, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(175, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(176, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(177, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(178, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(179, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(180, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(181, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(182, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(183, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(184, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(185, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(186, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(187, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(188, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(189, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(190, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(191, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(192, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(193, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(194, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(195, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(196, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(197, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(198, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(199, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(200, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(201, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(202, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(203, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(204, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(205, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(206, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(207, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(208, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(209, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(210, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(211, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(212, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(213, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(214, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(215, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(216, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(217, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(218, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(219, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(220, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(221, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(222, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(223, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(224, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(225, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(226, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(227, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(228, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(229, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(230, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(231, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(232, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(233, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(234, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(235, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(236, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(237, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(238, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(239, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(240, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(241, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(242, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(243, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(244, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(245, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(246, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(247, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(248, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(249, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(250, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(251, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(252, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(253, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(254, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607),
      new TextureUtil.BiomeColor(255, "Unknown Biome", 0.8F, 0.4F, 9616729, 7842607)
   };
   private static final String VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest.json";
   protected int[] blockColors = new int[BlockTypes.size()];
   protected long[] blockDistance = new long[BlockTypes.size()];
   protected long[] distances;
   protected int[] validColors;
   protected int[] validBlockIds;
   protected int[] validLayerColors;
   protected int[][] validLayerBlocks;
   protected int[] validMixBiomeColors;
   protected long[] validMixBiomeIds;
   protected TextureUtil.BiomeColor[] validBiomes;

   public TextureUtil() throws FileNotFoundException {
      this(MainUtil.getFile(Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.TEXTURES));
   }

   public TextureUtil(File folder) throws FileNotFoundException {
      this.folder = folder;
      if (!folder.exists()) {
         try {
            LOGGER.info("Downloading asset jar from Mojang, please wait...");
            new File(Fawe.<IFawe>platform().getDirectory() + "/" + Settings.settings().PATHS.TEXTURES + "/").mkdirs();

            try {
               TextureUtil.VersionMetadata metadata = getLatestVersion();
               LOGGER.info("Latest release version is {}", metadata.version());
               TextureUtil.HashedResource resource = getLatestClientJarUrl(metadata);
               Path out = Path.of(Fawe.<IFawe>platform().getDirectory().getPath(), Settings.settings().PATHS.TEXTURES, metadata.version() + ".jar");

               try (
                  InputStream stream = new URL(resource.resource()).openStream();
                  OutputStream writer = Files.newOutputStream(out);
               ) {
                  stream.transferTo(writer);
               }

               try {
                  String sha1 = calculateSha1(out);
                  if (!sha1.equals(resource.hash())) {
                     Files.deleteIfExists(out);
                     LOGGER.error(
                        "Hash comparison of final file failed (Expected: '{}', Calculated: '{}')\nTo prevent possibly malicious intentions, the downloaded file has been removed",
                        resource.hash(),
                        sha1
                     );
                     return;
                  }
               } catch (NoSuchAlgorithmException var12) {
                  LOGGER.warn(
                     "Couldn't verify integrity of downloaded client file\nPlease verify that the downloaded files '{}' hash is equal to '{}'",
                     out,
                     resource.hash()
                  );
                  return;
               }

               LOGGER.info("Asset jar has been downloaded and validated successfully.");
            } catch (IOException var14) {
               LOGGER.error(
                  "Could not download version jar. Please do so manually by creating a `FastAsyncWorldEdit/textures` folder with a `.minecraft/versions` jar in it.\nIf the file exists, please make sure the server has read access to the directory."
               );
            }
         } catch (SecurityException var15) {
            LOGGER.error(
               "Could not download asset jar. It's likely your file permission are setup improperly and do not allow fetching data from the Mojang servers.\nPlease create the following folder manually: `FastAsyncWorldEdit/textures` with a `.minecraft/versions` jar in it."
            );
         }
      }
   }

   public static TextureUtil fromClipboard(Clipboard clipboard) throws FileNotFoundException {
      boolean[] ids = new boolean[BlockTypes.size()];

      for (BlockVector3 pt : clipboard.getRegion()) {
         ids[clipboard.getBlock(pt).getInternalBlockTypeId()] = true;
      }

      HashSet<BlockType> blocks = new HashSet<>();

      for (int typeId = 0; typeId < ids.length; typeId++) {
         if (ids[typeId]) {
            blocks.add(BlockTypesCache.values[typeId]);
         }
      }

      return fromBlocks(blocks);
   }

   public static TextureUtil fromBlocks(Set<BlockType> blocks) throws FileNotFoundException {
      return new FilteredTextureUtil(Fawe.instance().getTextureUtil(), blocks);
   }

   public static TextureUtil fromMask(Mask mask) throws FileNotFoundException {
      HashSet<BlockType> blocks = new HashSet<>();
      SingleFilterBlock extent = new SingleFilterBlock();
      new MaskTraverser(mask).reset(extent);
      TextureUtil tu = Fawe.instance().getTextureUtil();

      for (int typeId : tu.getValidBlockIds()) {
         BlockType block = BlockTypesCache.values[typeId];
         extent.init(0, 0, 0, block.getDefaultState().toBaseBlock());
         if (mask.test(extent)) {
            blocks.add(block);
         }
      }

      return fromBlocks(blocks);
   }

   protected static int hueDistance(int red1, int green1, int blue1, int red2, int green2, int blue2) {
      int total1 = red1 + green1 + blue1;
      int total2 = red2 + green2 + blue2;
      if (total1 != 0 && total2 != 0) {
         int factor1 = FACTORS[total1];
         int factor2 = FACTORS[total2];
         long r = 512L * ((long)red1 * factor1 - (long)red2 * factor2) >> 10;
         long g = (long)green1 * factor1 - (long)green2 * factor2;
         long b = 767L * ((long)blue1 * factor1 - (long)blue2 * factor2) >> 10;
         return (int)(r * r + g * g + b * b >> 25);
      } else {
         return 0;
      }
   }

   protected static long colorDistance(int c1, int c2) {
      int red1 = c1 >> 16 & 0xFF;
      int green1 = c1 >> 8 & 0xFF;
      int blue1 = c1 & 0xFF;
      return colorDistance(red1, green1, blue1, c2);
   }

   private static long colorDistance(int red1, int green1, int blue1, int c2) {
      int red2 = c2 >> 16 & 0xFF;
      int green2 = c2 >> 8 & 0xFF;
      int blue2 = c2 & 0xFF;
      int rmean = red1 + red2 >> 1;
      int r = red1 - red2;
      int g = green1 - green2;
      int b = blue1 - blue2;
      int hd = hueDistance(red1, green1, blue1, red2, green2, blue2);
      return ((long)(512 + rmean) * r * r >> 8) + 4L * g * g + ((long)(767 - rmean) * b * b >> 8) + (long)hd * hd;
   }

   public static int multiplyColor(int c1, int c2) {
      int alpha1 = c1 >> 24 & 0xFF;
      int alpha2 = c2 >> 24 & 0xFF;
      int red1 = c1 >> 16 & 0xFF;
      int green1 = c1 >> 8 & 0xFF;
      int blue1 = c1 & 0xFF;
      int red2 = c2 >> 16 & 0xFF;
      int green2 = c2 >> 8 & 0xFF;
      int blue2 = c2 & 0xFF;
      int red = red1 * red2 / 255;
      int green = green1 * green2 / 255;
      int blue = blue1 * blue2 / 255;
      int alpha = alpha1 * alpha2 / 255;
      return (alpha << 24) + (red << 16) + (green << 8) + blue;
   }

   public static int averageColor(int c1, int c2) {
      int alpha1 = c1 >> 24 & 0xFF;
      int alpha2 = c2 >> 24 & 0xFF;
      int red1 = c1 >> 16 & 0xFF;
      int green1 = c1 >> 8 & 0xFF;
      int blue1 = c1 & 0xFF;
      int red2 = c2 >> 16 & 0xFF;
      int green2 = c2 >> 8 & 0xFF;
      int blue2 = c2 & 0xFF;
      int red = red1 + red2 >> 1;
      int green = green1 + green2 >> 1;
      int blue = blue1 + blue2 >> 1;
      int alpha = alpha1 + alpha2 >> 1;
      return (alpha << 24) + (red << 16) + (green << 8) + blue;
   }

   public static int averageColor(int... colors) {
      int alpha = 0;
      int red = 0;
      int green = 0;
      int blue = 0;

      for (int c : colors) {
         alpha += c >> 24 & 0xFF;
         red += c >> 16 & 0xFF;
         green += c >> 8 & 0xFF;
         blue += c & 0xFF;
      }

      int num = colors.length;
      alpha /= num;
      red /= num;
      green /= num;
      blue /= num;
      return (alpha << 24) + (red << 16) + (green << 8) + blue;
   }

   public static int combineTransparency(int top, int bottom) {
      int alpha1 = top >> 24 & 0xFF;
      int alpha2 = 255 - alpha1;
      int red1 = top >> 16 & 0xFF;
      int green1 = top >> 8 & 0xFF;
      int blue1 = top & 0xFF;
      int red2 = bottom >> 16 & 0xFF;
      int green2 = bottom >> 8 & 0xFF;
      int blue2 = bottom & 0xFF;
      int red = (red1 * alpha1 + red2 * alpha2) / 255;
      int green = (green1 * alpha1 + green2 * alpha2) / 255;
      int blue = (blue1 * alpha1 + blue2 * alpha2) / 255;
      return (red << 16) + (green << 8) + blue + -16777216;
   }

   private static long getDistance(BufferedImage image, int c1) {
      long totalDistSqr = 0L;
      int width = image.getWidth();
      int height = image.getHeight();
      int area = width * height;
      int red1 = c1 >> 16 & 0xFF;
      int green1 = c1 >> 8 & 0xFF;
      int blue1 = c1 & 0xFF;

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            int c2 = image.getRGB(x, y);
            long distance = colorDistance(red1, green1, blue1, c2);
            totalDistSqr += distance * distance;
         }
      }

      return totalDistSqr / area;
   }

   private static TextureUtil.VersionMetadata getLatestVersion() throws IOException {
      try (
         BufferedInputStream in = new BufferedInputStream(new URL("https://piston-meta.mojang.com/mc/game/version_manifest.json").openStream());
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      ) {
         JsonElement element = JsonParser.parseReader(reader);
         Iterator var3 = element.getAsJsonObject().getAsJsonArray("versions").iterator();

         JsonObject version;
         do {
            if (!var3.hasNext()) {
               throw new IOException("Failed to get latest version metadata");
            }

            JsonElement versions = (JsonElement)var3.next();
            version = versions.getAsJsonObject();
         } while (!version.get("type").getAsString().equals("release"));

         String clientJsonUrl = version.get("url").getAsString();
         String id = version.get("id").getAsString();
         return new TextureUtil.VersionMetadata(id, clientJsonUrl);
      }
   }

   private static TextureUtil.HashedResource getLatestClientJarUrl(TextureUtil.VersionMetadata metadata) throws IOException {
      TextureUtil.HashedResource var5;
      try (
         BufferedInputStream in = new BufferedInputStream(new URL(metadata.url()).openStream());
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      ) {
         JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
         JsonObject client = object.getAsJsonObject("downloads").getAsJsonObject("client");
         var5 = new TextureUtil.HashedResource(client.get("url").getAsString(), client.get("sha1").getAsString());
      }

      return var5;
   }

   private static String calculateSha1(Path path) throws NoSuchAlgorithmException, IOException {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");

      String var4;
      try (
         BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(path));
         DigestOutputStream digestOutputStream = new DigestOutputStream(OutputStream.nullOutputStream(), digest);
      ) {
         stream.transferTo(digestOutputStream);
         var4 = HexFormat.of().formatHex(digest.digest());
      }

      return var4;
   }

   @Override
   public TextureUtil getTextureUtil() {
      return this;
   }

   public TextureUtil fork() {
      return this;
   }

   public BlockType getNearestBlock(int color) {
      long min = Long.MAX_VALUE;
      int closest = 0;
      int red1 = color >> 16 & 0xFF;
      int green1 = color >> 8 & 0xFF;
      int blue1 = color & 0xFF;
      int alpha = color >> 24 & 0xFF;

      for (int i = 0; i < this.validColors.length; i++) {
         int other = this.validColors[i];
         if ((other >> 24 & 0xFF) == alpha) {
            long distance = colorDistance(red1, green1, blue1, other);
            if (distance < min) {
               min = distance;
               closest = this.validBlockIds[i];
            }
         }
      }

      return min == Long.MAX_VALUE ? null : BlockTypesCache.values[closest];
   }

   public BlockType getNextNearestBlock(int color) {
      long min = Long.MAX_VALUE;
      int closest = 0;
      int red1 = color >> 16 & 0xFF;
      int green1 = color >> 8 & 0xFF;
      int blue1 = color & 0xFF;
      int alpha = color >> 24 & 0xFF;

      for (int i = 0; i < this.validColors.length; i++) {
         int other = this.validColors[i];
         if (other != color && (other >> 24 & 0xFF) == alpha) {
            long distance = colorDistance(red1, green1, blue1, other);
            if (distance < min) {
               min = distance;
               closest = this.validBlockIds[i];
            }
         }
      }

      return min == Long.MAX_VALUE ? null : BlockTypesCache.values[closest];
   }

   public BlockType[] getNearestLayer(int color) {
      int[] closest = null;
      long min = Long.MAX_VALUE;
      int red1 = color >> 16 & 0xFF;
      int green1 = color >> 8 & 0xFF;
      int blue1 = color & 0xFF;
      int alpha = color >> 24 & 0xFF;

      for (int i = 0; i < this.validLayerColors.length; i++) {
         int other = this.validLayerColors[i];
         if ((other >> 24 & 0xFF) == alpha) {
            long distance = colorDistance(red1, green1, blue1, other);
            if (distance < min) {
               min = distance;
               closest = this.validLayerBlocks[i];
            }
         }
      }

      return new BlockType[]{BlockTypesCache.values[closest[0]], BlockTypesCache.values[closest[1]]};
   }

   public BlockType getLighterBlock(BlockType block) {
      return this.getNearestBlock(block, false);
   }

   public BlockType getDarkerBlock(BlockType block) {
      return this.getNearestBlock(block, true);
   }

   public BlockType getLighterBlock(int color) {
      return this.getNearestBlock(color, false);
   }

   public BlockType getDarkerBlock(int color) {
      return this.getNearestBlock(color, true);
   }

   public int getColor(BlockType block) {
      return block == BlockTypes.GRASS_BLOCK ? this.validBiomes[0].grassCombined : this.blockColors[block.getInternalId()];
   }

   public int getColor(BiomeType biome) {
      return this.validBiomes[biome.getInternalId()].grassCombined;
   }

   public TextureUtil.BiomeColor getBiome(int biome) {
      return this.biomes[biome];
   }

   protected boolean getIsBlockCloserThanBiome(char[] blockAndBiomeIdOutput, int color, int biomePriority) {
      BlockType block = this.getNearestBlock(color);
      TextureUtil.BiomeColor biome = this.getNearestBiome(color);
      int blockColor = this.getColor(block);
      blockAndBiomeIdOutput[0] = block.getDefaultState().getOrdinalChar();
      blockAndBiomeIdOutput[1] = (char)biome.id;
      return colorDistance(biome.grassCombined, color) - biomePriority > colorDistance(blockColor, color);
   }

   protected int getBiomeMix(int[] biomeIdsOutput, int color) {
      long closest = Long.MAX_VALUE;
      int closestAverage = Integer.MAX_VALUE;
      long min = Long.MAX_VALUE;
      int red1 = color >> 16 & 0xFF;
      int green1 = color >> 8 & 0xFF;
      int blue1 = color & 0xFF;
      int alpha = color >> 24 & 0xFF;

      for (int i = 0; i < this.validMixBiomeColors.length; i++) {
         int other = this.validMixBiomeColors[i];
         if ((other >> 24 & 0xFF) == alpha) {
            long distance = colorDistance(red1, green1, blue1, other);
            if (distance < min) {
               min = distance;
               closest = this.validMixBiomeIds[i];
               closestAverage = other;
            }
         }
      }

      biomeIdsOutput[0] = (int)(closest & 255L);
      biomeIdsOutput[1] = (int)(closest >> 8 & 255L);
      biomeIdsOutput[2] = (int)(closest >> 16 & 255L);
      return closestAverage;
   }

   public TextureUtil.BiomeColor getNearestBiome(int color) {
      int grass = this.blockColors[BlockTypes.GRASS_BLOCK.getInternalId()];
      if (grass == 0) {
         return null;
      } else {
         TextureUtil.BiomeColor closest = null;
         long min = Long.MAX_VALUE;
         int red = color >> 16 & 0xFF;
         int green = color >> 8 & 0xFF;
         int blue = color & 0xFF;

         for (TextureUtil.BiomeColor biome : this.validBiomes) {
            long distance = colorDistance(red, green, blue, biome.grassCombined);
            if (distance < min) {
               min = distance;
               closest = biome;
            }
         }

         return closest;
      }
   }

   protected File getFolder() {
      return this.folder;
   }

   private BufferedImage readImage(ZipFile zipFile, String name) throws IOException {
      ZipEntry entry = this.getEntry(zipFile, name);
      if (entry != null) {
         BufferedImage var5;
         try (InputStream is = zipFile.getInputStream(entry)) {
            var5 = ImageIO.read(is);
         }

         return var5;
      } else {
         return null;
      }
   }

   private ZipEntry getEntry(ZipFile file, String path) {
      ZipEntry entry = file.getEntry(path);
      if (entry == null) {
         path = path.replace("/", File.separator);
         entry = file.getEntry(path);
      }

      return entry;
   }

   public void loadModTextures() throws IOException {
      Int2ObjectOpenHashMap<Integer> colorMap = new Int2ObjectOpenHashMap();
      Int2ObjectOpenHashMap<Long> distanceMap = new Int2ObjectOpenHashMap();
      Gson gson = new Gson();
      if (this.folder.exists()) {
         File[] files = this.folder.listFiles((dir, name) -> name.endsWith(".jar"));
         if (files == null || files.length == 0) {
            LOGGER.error(
               "No version jar found in {}. Delete the named folder and restart your server to download the missing assets.\nIf no asset jar is created, please do so manually by creating a `FastAsyncWorldEdit/textures` folder with a `.minecraft/versions` jar or mods in it.",
               this.folder.getPath()
            );
         }

         if (files != null && files.length > 0) {
            for (File file : files) {
               ZipFile zipFile = new ZipFile(file);
               String[] modelsDir = new String[]{"assets/%1$s/models/block/%2$s.json", "assets/%1$s/models/item/%2$s.json"};
               String texturesDir = "assets/%1$s/textures/%2$s.png";
               Type typeToken = (new TypeToken<Map<String, Object>>() {}).getType();

               for (BlockType blockType : BlockTypesCache.values) {
                  if (blockType.getMaterial().isFullCube() && !blockType.id().toLowerCase().contains("shulker")) {
                     String uniqueBiomesColors = blockType.id().toLowerCase(Locale.ROOT);
                     String name;
                     String nameSpace;
                     ZipEntry entry;
                     int combined;
                     switch (uniqueBiomesColors) {
                        case "slime_block":
                        case "honey_block":
                        case "mob_spawner":
                        case "spawner":
                           continue;
                        default:
                           combined = blockType.getInternalId();
                           String id = blockType.id();
                           String[] split = id.split(":", 2);
                           name = split.length == 1 ? id : split[1];
                           nameSpace = split.length == 1 ? "" : split[0];
                           entry = null;
                     }

                     for (String dir : modelsDir) {
                        String modelFileName = String.format(dir, nameSpace, name);
                        if ((entry = this.getEntry(zipFile, modelFileName)) != null) {
                           break;
                        }
                     }

                     if (entry == null) {
                        String[] dirs = Arrays.stream(modelsDir).map(s -> String.format(s, nameSpace, name)).toArray(String[]::new);
                        LOGGER.error("Cannot find {} in {}", dirs, file);
                     } else {
                        String textureFileName;
                        label212:
                        try (InputStream is = zipFile.getInputStream(entry)) {
                           JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                           Map<String, Object> root = (Map<String, Object>)gson.fromJson(reader, typeToken);
                           Map<String, Object> textures = (Map<String, Object>)root.get("textures");
                           if (textures != null) {
                              Set<String> models = new HashSet<>();

                              for (Entry<String, Object> stringObjectEntry : textures.entrySet()) {
                                 Object value = stringObjectEntry.getValue();
                                 if (value instanceof String) {
                                    models.add((String)value);
                                 } else if (value instanceof Map) {
                                    value = ((Map)value).get("model");
                                    if (value != null) {
                                       models.add((String)value);
                                    }
                                 }
                              }

                              if (models.size() == 1) {
                                 String[] texSplit = models.iterator().next().split(":");
                                 String texture = texSplit[texSplit.length - 1];
                                 textureFileName = String.format(texturesDir, nameSpace, texture);
                                 break label212;
                              }
                           }
                           continue;
                        }

                        BufferedImage image = this.readImage(zipFile, textureFileName);
                        if (image == null) {
                           LOGGER.error("Cannot find {}", textureFileName);
                        } else {
                           int color = ImageUtil.getColor(image);
                           long distance = getDistance(image, color);
                           distanceMap.put(combined, distance);
                           colorMap.put(combined, color);
                        }
                     }
                  }
               }

               Integer grass = null;
               String grassFileName = String.format(texturesDir, "minecraft", "block/grass_block_top");
               BufferedImage image = this.readImage(zipFile, grassFileName);
               if (image != null) {
                  grass = ImageUtil.getColor(image);
               }

               if (grass != null) {
                  colorMap.put(BlockTypes.GRASS_BLOCK.getInternalId(), grass);
                  ZipEntry grassEntry = this.getEntry(zipFile, "assets/minecraft/textures/colormap/grass.png");
                  if (grassEntry != null) {
                     try (InputStream isx = zipFile.getInputStream(grassEntry)) {
                        BufferedImage imagex = ImageIO.read(isx);

                        for (TextureUtil.BiomeColor biome : this.biomes) {
                           float adjTemp = Math.clamp(biome.temperature, 0.0F, 1.0F);
                           float adjRainfall = Math.clamp(biome.rainfall, 0.0F, 1.0F) * adjTemp;
                           int x = (int)(255.0F - adjTemp * 255.0F);
                           int z = (int)(255.0F - adjRainfall * 255.0F);
                           biome.grass = imagex.getRGB(x, z);
                        }
                     }

                     this.biomes[6].grass = 0;
                     this.biomes[134].grass = 0;
                     this.biomes[29].grass = multiplyColor(this.biomes[29].grass, -14142454);
                     this.biomes[157].grass = multiplyColor(this.biomes[157].grass, -14142454);
                     this.biomes[37].grass = -7306931;
                     this.biomes[38].grass = -7306931;
                     this.biomes[39].grass = -7306931;
                     this.biomes[165].grass = -7306931;
                     this.biomes[166].grass = -7306931;
                     this.biomes[167].grass = -7306931;
                     List<TextureUtil.BiomeColor> valid = new ArrayList<>();

                     for (TextureUtil.BiomeColor biome : this.biomes) {
                        if (biome.grass != 0 && !biome.name.equalsIgnoreCase("Unknown Biome")) {
                           valid.add(biome);
                        }

                        biome.grassCombined = multiplyColor(grass, biome.grass);
                     }

                     this.validBiomes = valid.toArray(new TextureUtil.BiomeColor[0]);
                     ArrayList<TextureUtil.BiomeColor> uniqueColors = new ArrayList<>();
                     Set<Integer> uniqueBiomesColors = new IntArraySet();

                     for (TextureUtil.BiomeColor color : this.validBiomes) {
                        if (uniqueBiomesColors.add(color.grass)) {
                           uniqueColors.add(color);
                        }
                     }

                     uniqueBiomesColors.clear();
                     LongArrayList layerIds = new LongArrayList();
                     LongArrayList layerColors = new LongArrayList();

                     for (int i = 0; i < uniqueColors.size(); i++) {
                        for (int j = i; j < uniqueColors.size(); j++) {
                           for (int k = j; k < uniqueColors.size(); k++) {
                              TextureUtil.BiomeColor c1 = uniqueColors.get(i);
                              TextureUtil.BiomeColor c2 = uniqueColors.get(j);
                              TextureUtil.BiomeColor c3 = uniqueColors.get(k);
                              int average = averageColor(c1.grass, c2.grass, c3.grass);
                              if (uniqueBiomesColors.add(average)) {
                                 layerColors.add(average);
                                 layerIds.add(c1.id + ((long)c2.id << 8) + ((long)c3.id << 16));
                              }
                           }
                        }
                     }

                     this.validMixBiomeColors = new int[layerColors.size()];

                     for (int i = 0; i < layerColors.size(); i++) {
                        this.validMixBiomeColors[i] = (int)layerColors.getLong(i);
                     }

                     this.validMixBiomeIds = layerIds.toLongArray();
                  }
               }

               zipFile.close();
            }
         }
      }

      this.validBlockIds = new int[colorMap.size()];
      this.validColors = new int[colorMap.size()];
      int index = 0;

      for (ObjectIterator var37 = colorMap.int2ObjectEntrySet().iterator(); var37.hasNext(); index++) {
         it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<Integer> entry = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<Integer>)var37.next();
         int combinedId = entry.getIntKey();
         int colorx = (Integer)entry.getValue();
         this.blockColors[combinedId] = colorx;
         this.validBlockIds[index] = combinedId;
         this.validColors[index] = colorx;
      }

      ArrayList<Long> distances = new ArrayList<>(distanceMap.values());
      Collections.sort(distances);
      this.distances = new long[distances.size()];

      for (int i = 0; i < this.distances.length; i++) {
         this.distances[i] = distances.get(i);
      }

      ObjectIterator var41 = distanceMap.int2ObjectEntrySet().iterator();

      while (var41.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<Long> entry = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<Long>)var41.next();
         this.blockDistance[entry.getIntKey()] = (Long)entry.getValue();
      }

      this.calculateLayerArrays();
   }

   protected void calculateLayerArrays() {
      Int2ObjectOpenHashMap<int[]> colorLayerMap = new Int2ObjectOpenHashMap();

      for (int i = 0; i < this.validBlockIds.length; i++) {
         int color = this.validColors[i];
         int combined = this.validBlockIds[i];
         if (this.hasAlpha(color)) {
            for (int j = 0; j < this.validBlockIds.length; j++) {
               int colorOther = this.validColors[j];
               if (!this.hasAlpha(colorOther)) {
                  int combinedOther = this.validBlockIds[j];
                  int combinedColor = combineTransparency(color, colorOther);
                  colorLayerMap.put(combinedColor, new int[]{combined, combinedOther});
               }
            }
         }
      }

      this.validLayerColors = new int[colorLayerMap.size()];
      this.validLayerBlocks = new int[colorLayerMap.size()][];
      int index = 0;
      ObjectIterator var10 = colorLayerMap.int2ObjectEntrySet().iterator();

      while (var10.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<int[]> entry = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<int[]>)var10.next();
         this.validLayerColors[index] = entry.getIntKey();
         this.validLayerBlocks[index++] = (int[])entry.getValue();
      }
   }

   protected BlockType getNearestBlock(BlockType block, boolean darker) {
      int color = this.getColor(block);
      if (color == 0) {
         return block;
      } else {
         BlockType darkerBlock = this.getNearestBlock(color, darker);
         return darkerBlock != null ? darkerBlock : block;
      }
   }

   protected BlockType getNearestBlock(int color, boolean darker) {
      long min = Long.MAX_VALUE;
      int closest = 0;
      int red1 = color >> 16 & 0xFF;
      int green1 = color >> 8 & 0xFF;
      int blue1 = color & 0xFF;
      int alpha = color >> 24 & 0xFF;
      int intensity1 = 2 * red1 + 4 * green1 + 3 * blue1;

      for (int i = 0; i < this.validColors.length; i++) {
         int other = this.validColors[i];
         if (other != color && (other >> 24 & 0xFF) == alpha) {
            int red2 = other >> 16 & 0xFF;
            int green2 = other >> 8 & 0xFF;
            int blue2 = other & 0xFF;
            int intensity2 = 2 * red2 + 4 * green2 + 3 * blue2;
            if (darker ? intensity2 < intensity1 : intensity1 < intensity2) {
               long distance = colorDistance(red1, green1, blue1, other);
               if (distance < min) {
                  min = distance;
                  closest = this.validBlockIds[i];
               }
            }
         }
      }

      return min == Long.MAX_VALUE ? null : BlockTypesCache.values[closest];
   }

   private String getFileName(String path) {
      String[] split = path.split("[/|\\\\]");
      String name = split[split.length - 1];
      int dot = name.indexOf(46);
      if (dot != -1) {
         name = name.substring(0, dot);
      }

      return name;
   }

   private String alphabetize(String asset) {
      String[] split = asset.split("_");
      Arrays.sort((Object[])split);
      return StringMan.join((Object[])split, "_");
   }

   protected boolean hasAlpha(int color) {
      int alpha = color >> 24 & 0xFF;
      return alpha != 255;
   }

   public int[] getValidBlockIds() {
      return (int[])this.validBlockIds.clone();
   }

   static {
      for (int i = 1; i < FACTORS.length; i++) {
         FACTORS[i] = 65535 / i;
      }
   }

   public static class BiomeColor {
      public int id;
      public String name;
      public float temperature;
      public float rainfall;
      public int grass;
      public int grassCombined;
      public int foliage;

      public BiomeColor(int id, String name, float temperature, float rainfall, int grass, int foliage) {
         this.id = id;
         this.name = name;
         this.temperature = temperature;
         this.rainfall = rainfall;
         this.grass = grass;
         this.grassCombined = grass;
         this.foliage = foliage;
      }
   }

   private record HashedResource(String resource, String hash) {
   }

   private record VersionMetadata(String version, String url) {
   }
}
