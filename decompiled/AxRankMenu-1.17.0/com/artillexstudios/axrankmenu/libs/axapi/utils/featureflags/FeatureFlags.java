package com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags;

import com.artillexstudios.axrankmenu.libs.axapi.utils.Nameable;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type.BooleanFlag;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type.EnumFlag;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type.IntegerFlag;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type.ListFlag;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type.LongFlag;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type.PatternFlag;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.type.StringFlag;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LoggerNameFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

public final class FeatureFlags {
   public static final BooleanFlag PACKET_ENTITY_TRACKER_ENABLED = new BooleanFlag(false);
   public static final BooleanFlag DEBUG = new BooleanFlag(false);
   public static final BooleanFlag DEBUG_INCOMING_PACKETS = new BooleanFlag(false);
   public static final BooleanFlag DEBUG_OUTGOING_PACKETS = new BooleanFlag(false);
   public static final BooleanFlag USE_LEGACY_HEX_FORMATTER = new BooleanFlag(false);
   public static final IntegerFlag PACKET_ENTITY_TRACKER_THREADS = new IntegerFlag(3);
   public static final LongFlag HOLOGRAM_UPDATE_TICKS = new LongFlag(0L);
   public static final ListFlag<Pattern> PLACEHOLDER_PATTERNS = new ListFlag<>(
      PatternFlag.TRANSFORMER, Arrays.asList(Pattern.compile("%.+%"), Pattern.compile("<.+>"))
   );
   public static final BooleanFlag PLACEHOLDER_API_HOOK = new BooleanFlag(false);
   public static final StringFlag PLACEHOLDER_API_IDENTIFIER = new StringFlag("");
   public static final IntegerFlag COMPONENT_CACHE_SIZE = new IntegerFlag(1000);
   public static final BooleanFlag GUI_WAIT_FOR_ALL = new BooleanFlag(false);
   public static final IntegerFlag INVENTORY_CLICK_COOLDOWN = new IntegerFlag(25);
   public static final BooleanFlag STRICT_ITEM_OVERRIDE_HANDLING = new BooleanFlag(false);
   public static final BooleanFlag USE_INVENTORY_UPDATER = new BooleanFlag(false);
   public static final BooleanFlag LOG_EXCEPTION_UTILS_EXCEPTIONS = new BooleanFlag(false);
   public static final BooleanFlag PARSE_PLACEHOLDER_API_IN_ITEM_BUILDER = new BooleanFlag(false);
   public static final IntegerFlag COLOR_CACHE_SIZE = new IntegerFlag(1000);
   public static final BooleanFlag LISTEN_TO_RIDE_PACKET = new BooleanFlag(false);
   public static final IntegerFlag ASYNC_UTILS_POOL_SIZE = new IntegerFlag(1);
   public static final BooleanFlag ENABLE_PACKET_LISTENERS = new BooleanFlag(false);
   public static final IntegerFlag SERVER_PLAYER_CACHE_SIZE = new IntegerFlag(1500);
   public static final IntegerFlag HOLOGRAM_PARSED_LINE_CACHE = new IntegerFlag(2000);
   public static final BooleanFlag ENABLE_GUI_LISTENERS = new BooleanFlag(false);
   public static final EnumFlag<LoggerNameFormat> LOGGER_NAME_FORMAT = new EnumFlag<>(LoggerNameFormat.FULL, LoggerNameFormat.class);
   public static final IntegerFlag LIBRARY_DOWNLOADER_DEPTH = new IntegerFlag(5);

   public static void refresh() {
      String name = Nameable.getInstance().getName();
      PACKET_ENTITY_TRACKER_ENABLED.refresh(name + "enableEntityTracker");
      DEBUG.refresh(name + "debug");
      DEBUG_INCOMING_PACKETS.refresh(name + "debugIncomingPackets");
      DEBUG_OUTGOING_PACKETS.refresh(name + "debugOutgoingPackets");
      USE_LEGACY_HEX_FORMATTER.refresh(name + "useLegacyHexFormat");
      PACKET_ENTITY_TRACKER_THREADS.refresh(name + "entityTrackerThreads");
      HOLOGRAM_UPDATE_TICKS.refresh(name + "hologramUpdateTicks");
      PLACEHOLDER_PATTERNS.refresh(name + "placeholderPatterns");
      PLACEHOLDER_API_HOOK.refresh(name + "placeholderApiHook");
      PLACEHOLDER_API_IDENTIFIER.refresh(name + "placeholderApiIdentifier");
      COMPONENT_CACHE_SIZE.refresh(name + "componentCacheSize");
      GUI_WAIT_FOR_ALL.refresh(name + "guiWaitForAll");
      INVENTORY_CLICK_COOLDOWN.refresh(name + "inventoryClickCooldown");
      STRICT_ITEM_OVERRIDE_HANDLING.refresh(name + "strictInventoryOverrideHandling");
      USE_INVENTORY_UPDATER.refresh(name + "useInventoryUpdater");
      LOG_EXCEPTION_UTILS_EXCEPTIONS.refresh(name + "logExceptionUtilsExceptions");
      PARSE_PLACEHOLDER_API_IN_ITEM_BUILDER.refresh(name + "parsePlaceholderAPiInItemBuilder");
      COLOR_CACHE_SIZE.refresh(name + "colorCacheSize");
      LISTEN_TO_RIDE_PACKET.refresh(name + "listenToRidePacket");
      ASYNC_UTILS_POOL_SIZE.refresh(name + "asyncUtilsPoolSize");
      ENABLE_PACKET_LISTENERS.refresh(name + "enablePacketListeners");
      SERVER_PLAYER_CACHE_SIZE.refresh(name + "serverPlayerCacheSize");
      HOLOGRAM_PARSED_LINE_CACHE.refresh(name + "hologramParsedLineCache");
      ENABLE_GUI_LISTENERS.refresh(name + "enableGuiListeners");
      LOGGER_NAME_FORMAT.refresh(name + "loggerNameFormat");
      LIBRARY_DOWNLOADER_DEPTH.refresh(name + "libraryDownloaderDepth");
   }
}
