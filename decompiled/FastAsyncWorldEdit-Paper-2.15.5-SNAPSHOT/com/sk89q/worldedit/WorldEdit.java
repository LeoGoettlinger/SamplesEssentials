package com.sk89q.worldedit;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.exception.BrushRadiusLimitException;
import com.fastasyncworldedit.core.exception.OutsideWorldBoundsException;
import com.fastasyncworldedit.core.exception.RadiusLimitException;
import com.fastasyncworldedit.core.extension.factory.TransformFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.event.platform.InputType;
import com.sk89q.worldedit.event.platform.Interaction;
import com.sk89q.worldedit.event.platform.PlayerInputEvent;
import com.sk89q.worldedit.extension.factory.BlockFactory;
import com.sk89q.worldedit.extension.factory.ItemFactory;
import com.sk89q.worldedit.extension.factory.MaskFactory;
import com.sk89q.worldedit.extension.factory.PatternFactory;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.SchematicsEventListener;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.invoke.ReturnException;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.scripting.CraftScriptContext;
import com.sk89q.worldedit.scripting.CraftScriptEngine;
import com.sk89q.worldedit.scripting.RhinoCraftScriptEngine;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.io.file.FileSelectionAbortedException;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.util.io.file.FilenameResolutionException;
import com.sk89q.worldedit.util.io.file.InvalidFilenameException;
import com.sk89q.worldedit.util.task.SimpleSupervisor;
import com.sk89q.worldedit.util.task.Supervisor;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import com.sk89q.worldedit.world.registry.BundledItemData;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.script.ScriptException;
import org.apache.logging.log4j.Logger;

public final class WorldEdit {
   public static final Logger logger = LogManagerCompat.getLogger();
   private static final WorldEdit instance = new WorldEdit();
   private static String version;
   private final EventBus eventBus = new EventBus();
   private final PlatformManager platformManager = new PlatformManager(this);
   @Deprecated
   private final EditSessionFactory editSessionFactory = new EditSessionFactory.EditSessionFactoryImpl();
   private final SessionManager sessions = new SessionManager(this);
   private final Supervisor supervisor = new SimpleSupervisor();
   private final LazyReference<TranslationManager> translationManager = LazyReference.from(
      () -> new TranslationManager(getInstance().getPlatformManager().queryCapability(Capability.CONFIGURATION).getResourceLoader())
   );
   private final BlockFactory blockFactory = new BlockFactory(this);
   private final ItemFactory itemFactory = new ItemFactory(this);
   private final MaskFactory maskFactory = new MaskFactory(this);
   private final PatternFactory patternFactory = new PatternFactory(this);
   private final TransformFactory transformFactory = new TransformFactory(this);
   private static final Map<String, Direction> NAME_TO_DIRECTION_MAP;

   private WorldEdit() {
      this.eventBus.register(new SchematicsEventListener());
   }

   public static WorldEdit getInstance() {
      return instance;
   }

   public PlatformManager getPlatformManager() {
      return this.platformManager;
   }

   public EventBus getEventBus() {
      return this.eventBus;
   }

   public Supervisor getSupervisor() {
      return this.supervisor;
   }

   public ListeningExecutorService getExecutorService() {
      return this.platformManager.getExecutorService();
   }

   public BlockFactory getBlockFactory() {
      return this.blockFactory;
   }

   public ItemFactory getItemFactory() {
      return this.itemFactory;
   }

   public MaskFactory getMaskFactory() {
      return this.maskFactory;
   }

   public PatternFactory getPatternFactory() {
      return this.patternFactory;
   }

   public TransformFactory getTransformFactory() {
      return this.transformFactory;
   }

   public SessionManager getSessionManager() {
      return this.sessions;
   }

   public TranslationManager getTranslationManager() {
      return this.translationManager.getValue();
   }

   public File getSafeSaveFile(Actor actor, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
      return this.getSafeFile(actor, dir, filename, defaultExt, extensions, true);
   }

   public File getSafeOpenFile(Actor actor, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
      return this.getSafeFile(actor, dir, filename, defaultExt, extensions, false);
   }

   private File getSafeFile(@Nullable Actor actor, File dir, String filename, String defaultExt, String[] extensions, boolean isSave) throws FilenameException {
      if (extensions != null && extensions.length == 1 && extensions[0] == null) {
         extensions = null;
      }

      File f;
      if (filename.equals("#") && actor != null) {
         if (isSave) {
            f = actor.openFileSaveDialog(extensions);
         } else {
            f = actor.openFileOpenDialog(extensions);
         }

         if (f == null) {
            throw new FileSelectionAbortedException(Caption.of("worldedit.error.no-file-selected"));
         }
      } else {
         List<String> exts = (List<String>)(extensions == null ? ImmutableList.of(defaultExt) : Lists.asList(defaultExt, extensions));
         f = this.getSafeFileWithExtensions(dir, filename, exts, isSave);
      }

      try {
         Path filePath = Paths.get(f.toURI()).normalize();
         Path dirPath = Paths.get(dir.toURI()).normalize();
         boolean inDir = filePath.startsWith(dirPath);
         Path existingParent = filePath;

         do {
            existingParent = existingParent.getParent();
         } while (existingParent != null && !existingParent.toFile().exists());

         boolean isSym = existingParent != null && !existingParent.toRealPath().equals(existingParent);
         if (inDir && (this.getConfiguration().allowSymlinks || !isSym)) {
            return filePath.toFile();
         } else {
            throw new FilenameResolutionException(filename, Caption.of("worldedit.error.file-resolution.outside-root"));
         }
      } catch (InvalidPathException | IOException var13) {
         throw new FilenameResolutionException(filename, Caption.of("worldedit.error.file-resolution.resolve-failed"));
      }
   }

   private File getSafeFileWithExtensions(File dir, String filename, List<String> exts, boolean isSave) throws InvalidFilenameException {
      if (isSave) {
         if (exts.size() != 1) {
            exts = exts.subList(0, 1);
         }
      } else {
         int dot = filename.lastIndexOf(46);
         if (dot < 0 || dot == filename.length() - 1) {
            String currentExt = filename.substring(dot + 1);
            if (exts.contains(currentExt) && this.checkFilename(filename)) {
               File f = new File(dir, filename);
               if (f.exists()) {
                  return f;
               }
            }
         }
      }

      File result = null;
      Iterator<String> iter = exts.iterator();

      while (iter.hasNext() && (result == null || !isSave && !result.exists())) {
         result = this.getSafeFileWithExtension(dir, filename, iter.next());
      }

      if (result == null) {
         throw new InvalidFilenameException(filename, Caption.of("worldedit.error.invalid-filename.invalid-characters"));
      } else {
         return result;
      }
   }

   private File getSafeFileWithExtension(File dir, String filename, String extension) {
      if (extension != null) {
         int dot = filename.lastIndexOf(46);
         if (dot < 0 || dot == filename.length() - 1 || !filename.substring(dot + 1).equalsIgnoreCase(extension)) {
            filename = filename + "." + extension;
         }
      }

      return !this.checkFilename(filename) ? null : new File(dir, filename);
   }

   private boolean checkFilename(String filename) {
      return filename.matches("^[A-Za-z0-9_\\-./'$@~!%()\\[\\]+{},]+\\.[A-Za-z0-9]+$");
   }

   public void loadMappings() {
      BundledBlockData.getInstance();
      BundledItemData.getInstance();
      LegacyMapper.getInstance();
   }

   @Deprecated
   public void checkMaxRadius(double radius) throws MaxRadiusException {
      if (this.getConfiguration().maxRadius > 0 && radius > this.getConfiguration().maxRadius) {
         throw new MaxRadiusException();
      }
   }

   @Deprecated
   public void checkMaxBrushRadius(double radius) throws MaxBrushRadiusException {
      if (this.getConfiguration().maxBrushRadius > 0 && radius > this.getConfiguration().maxBrushRadius) {
         throw new MaxBrushRadiusException();
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public void checkMaxBrushRadius(Expression radius) throws MaxBrushRadiusException {
      double val = radius.evaluate();
      Preconditions.checkArgument(val >= 0.0, "Radius must be a positive number.");
      if (this.getConfiguration().maxBrushRadius > 0 && val > this.getConfiguration().maxBrushRadius) {
         throw new MaxBrushRadiusException();
      }
   }

   public void checkMaxRadius(double radius, Actor actor) {
      int max = actor.getLimit().MAX_RADIUS;
      if (max > 0 && radius > max) {
         throw new RadiusLimitException(max);
      }
   }

   public void checkMaxBrushRadius(double radius, Actor actor) {
      int max = actor.getLimit().MAX_BRUSH_RADIUS;
      if (max > 0 && radius > max) {
         throw new RadiusLimitException(max);
      }
   }

   public void checkMaxBrushRadius(Expression expression, Actor actor) {
      double radius = expression.evaluate();
      Preconditions.checkArgument(radius >= 0.0, "Radius must be a positive number.");
      int max = actor.getLimit().MAX_BRUSH_RADIUS;
      if (max > 0 && radius > max) {
         throw new BrushRadiusLimitException(max);
      }
   }

   public void checkExtentHeightBounds(BlockVector3 position, Extent extent) {
      if (position.y() < extent.getMinY() || position.y() > extent.getMaxY()) {
         throw new OutsideWorldBoundsException(position.y());
      }
   }

   @Deprecated
   public File getWorkingDirectoryFile(String path) {
      return this.getWorkingDirectoryPath(path).toFile();
   }

   public Path getWorkingDirectoryPath(String path) {
      Path p = Paths.get(path);
      return p.isAbsolute() ? p : this.getConfiguration().getWorkingDirectoryPath().resolve(path);
   }

   public Path getSchematicsFolderPath() {
      return this.getWorkingDirectoryPath(this.getConfiguration().saveDir);
   }

   public BlockVector3 getDirection(@Nullable Player player, String dirStr) throws UnknownDirectionException {
      dirStr = dirStr.toLowerCase(Locale.ROOT);
      Direction dir = this.getPlayerDirection(player, dirStr);
      if (!dir.isUpright() && !dir.isCardinal()) {
         throw new UnknownDirectionException(dir.name());
      } else {
         return dir.toBlockVector();
      }
   }

   public BlockVector3 getDiagonalDirection(@Nullable Player player, String dirStr) throws UnknownDirectionException {
      dirStr = dirStr.toLowerCase(Locale.ROOT);
      Direction dir = this.getPlayerDirection(player, dirStr);
      if (!dir.isCardinal() && !dir.isOrdinal() && !dir.isUpright()) {
         throw new UnknownDirectionException(dir.name());
      } else {
         return dir.toBlockVector();
      }
   }

   private Direction getPlayerDirection(@Nullable Player player, String dirStr) throws UnknownDirectionException {
      Direction byName = NAME_TO_DIRECTION_MAP.get(dirStr);
      if (byName != null) {
         return byName;
      } else {
         switch (dirStr) {
            case "m":
            case "me":
            case "f":
            case "forward":
               return this.getDirectionRelative(player, 0);
            case "b":
            case "back":
               Direction dir = this.getDirectionRelative(player, 180);
               if (dir.isUpright()) {
                  dir = dir == Direction.UP ? Direction.DOWN : Direction.UP;
               }

               return dir;
            case "l":
            case "left":
               return this.getDirectionRelative(player, -90);
            case "r":
            case "right":
               return this.getDirectionRelative(player, 90);
            default:
               throw new UnknownDirectionException(dirStr);
         }
      }
   }

   private Direction getDirectionRelative(Player player, int yawOffset) throws UnknownDirectionException {
      if (player != null) {
         return player.getCardinalDirection(yawOffset);
      } else {
         throw new UnknownDirectionException("Only a player can use relative directions");
      }
   }

   public void flushBlockBag(Actor actor, EditSession editSession) {
      BlockBag blockBag = editSession.getBlockBag();
      if (blockBag != null) {
         blockBag.flushChanges();
      }

      Map<BlockType, Integer> missingBlocks = editSession.popMissingBlocks();
      if (!missingBlocks.isEmpty()) {
         TextComponent.Builder str = TextComponent.builder();
         str.append("Missing these blocks: ");
         int size = missingBlocks.size();
         int i = 0;

         for (Entry<BlockType, Integer> blockTypeIntegerEntry : missingBlocks.entrySet()) {
            str.append(blockTypeIntegerEntry.getKey().getRichName());
            str.append(" [Amt: ").append(String.valueOf(blockTypeIntegerEntry.getValue())).append("]");
            if (++i != size) {
               str.append(", ");
            }
         }

         actor.printError(str.build());
      }
   }

   public boolean handleArmSwing(Player player) {
      PlayerInputEvent event = new PlayerInputEvent(player, InputType.PRIMARY);
      this.getEventBus().post(event);
      return event.isCancelled();
   }

   public boolean handleRightClick(Player player) {
      PlayerInputEvent event = new PlayerInputEvent(player, InputType.SECONDARY);
      this.getEventBus().post(event);
      return event.isCancelled();
   }

   @Deprecated
   public boolean handleBlockRightClick(Player player, Location clicked) {
      return this.handleBlockRightClick(player, clicked, null);
   }

   public boolean handleBlockRightClick(Player player, Location clicked, @Nullable Direction face) {
      BlockInteractEvent event = new BlockInteractEvent(player, clicked, face, Interaction.OPEN);
      this.getEventBus().post(event);
      return event.isCancelled();
   }

   @Deprecated
   public boolean handleBlockLeftClick(Player player, Location clicked) {
      return this.handleBlockLeftClick(player, clicked, null);
   }

   public boolean handleBlockLeftClick(Player player, Location clicked, @Nullable Direction face) {
      BlockInteractEvent event = new BlockInteractEvent(player, clicked, face, Interaction.HIT);
      this.getEventBus().post(event);
      return event.isCancelled();
   }

   public void runScript(Player player, File f, String[] args) throws WorldEditException {
      String filename = f.getPath();
      int index = filename.lastIndexOf(46);
      String ext = filename.substring(index + 1);
      if (!ext.equalsIgnoreCase("js")) {
         player.print(Caption.of("worldedit.script.unsupported"));
      } else {
         String script;
         try {
            InputStream file;
            if (!f.exists()) {
               file = WorldEdit.class.getResourceAsStream("craftscripts/" + filename);
               if (file == null) {
                  player.print(Caption.of("worldedit.script.file-not-found", TextComponent.of(filename)));
                  return;
               }
            } else {
               file = new FileInputStream(f);
            }

            DataInputStream in = new DataInputStream(file);
            byte[] data = new byte[in.available()];
            in.readFully(data);
            in.close();
            script = new String(data, 0, data.length, StandardCharsets.UTF_8);
         } catch (IOException var24) {
            player.print(Caption.of("worldedit.script.read-error", TextComponent.of(var24.getMessage())));
            return;
         }

         LocalSession session = this.getSessionManager().get(player);
         CraftScriptContext scriptContext = new CraftScriptContext(
            this, this.getPlatformManager().queryCapability(Capability.USER_COMMANDS), this.getConfiguration(), session, player, args
         );

         CraftScriptEngine engine;
         try {
            engine = new RhinoCraftScriptEngine();
         } catch (NoClassDefFoundError var23) {
            player.print(Caption.of("worldedit.script.no-script-engine"));
            return;
         }

         engine.setTimeLimit(this.getConfiguration().scriptTimeout);
         Map<String, Object> vars = new HashMap<>();
         vars.put("argv", args);
         vars.put("context", scriptContext);
         vars.put("player", player);

         try {
            engine.evaluate(script, filename, vars);
         } catch (ScriptException var25) {
            if (!(Throwables.getRootCause(var25) instanceof ReturnException)) {
               player.print(Caption.of("worldedit.script.failed", TextComponent.of(var25.getMessage())));
               logger.warn("Failed to execute script", var25);
            }
         } catch (WorldEditException | NumberFormatException var26) {
            throw var26;
         } catch (Throwable var27) {
            player.print(Caption.of("worldedit.script.failed-console", TextComponent.of(var27.getClass().getCanonicalName())));
            logger.warn("Failed to execute script", var27);
         } finally {
            for (EditSession editSession : scriptContext.getEditSessions()) {
               editSession.close();
               session.remember(editSession);
            }
         }
      }
   }

   public LocalConfiguration getConfiguration() {
      return this.getPlatformManager().getConfiguration();
   }

   @Deprecated
   public EditSessionFactory getEditSessionFactory() {
      return this.editSessionFactory;
   }

   public EditSessionBuilder newEditSessionBuilder() {
      return new EditSessionBuilder(this.eventBus);
   }

   public EditSession newEditSession(@Nullable World world) {
      return this.newEditSessionBuilder().world(world).build();
   }

   public <A extends Actor & Locatable> EditSession newEditSession(A locatableActor) {
      return this.newEditSessionBuilder().locatableActor(locatableActor).build();
   }

   public static String getVersion() {
      if (version != null) {
         return version;
      } else {
         WorldEditManifest manifest = WorldEditManifest.load();
         return version = manifest.getWorldEditVersion();
      }
   }

   static {
      getVersion();
      SetMultimap<Direction, String> directionNames = HashMultimap.create();

      for (Direction direction : Direction.valuesOf(Direction.Flag.CARDINAL | Direction.Flag.UPRIGHT)) {
         String name = direction.name().toLowerCase(Locale.ROOT);

         for (int i = 1; i <= name.length(); i++) {
            directionNames.put(direction, name.substring(0, i));
         }
      }

      Builder<String, Direction> nameToDirectionMap = ImmutableMap.builder();

      for (Direction direction : directionNames.keySet()) {
         directionNames.get(direction).forEach(name -> nameToDirectionMap.put(name, direction));
      }

      UnmodifiableIterator var12 = ImmutableList.of(Direction.NORTH, Direction.SOUTH).iterator();

      while (var12.hasNext()) {
         Direction direction = (Direction)var12.next();
         UnmodifiableIterator var15 = ImmutableList.of(Direction.WEST, Direction.EAST).iterator();

         while (var15.hasNext()) {
            Direction diagonal = (Direction)var15.next();

            for (String dirName : directionNames.get(direction)) {
               for (String diagName : directionNames.get(diagonal)) {
                  nameToDirectionMap.put(dirName + diagName, Direction.valueOf(direction.name() + diagonal.name()));
               }
            }
         }
      }

      NAME_TO_DIRECTION_MAP = nameToDirectionMap.build();
   }
}
