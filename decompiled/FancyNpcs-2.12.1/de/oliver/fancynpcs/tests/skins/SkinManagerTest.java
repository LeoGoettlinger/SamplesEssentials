package de.oliver.fancynpcs.tests.skins;

import de.oliver.fancynpcs.api.skins.SkinData;
import de.oliver.fancynpcs.api.skins.SkinLoadException;
import de.oliver.fancynpcs.skins.SkinGenerationRequest;
import de.oliver.fancynpcs.skins.SkinManagerImpl;
import de.oliver.fancynpcs.skins.cache.SkinCache;
import de.oliver.fancynpcs.skins.cache.SkinCacheData;
import de.oliver.fancynpcs.skins.cache.SkinCacheFile;
import de.oliver.fancynpcs.skins.cache.SkinCacheMemory;
import de.oliver.fancynpcs.skins.uuidcache.UUIDMemoryCache;
import de.oliver.plugintests.Expectable;
import de.oliver.plugintests.annotations.FPAfterEach;
import de.oliver.plugintests.annotations.FPBeforeEach;
import de.oliver.plugintests.annotations.FPTest;
import java.util.UUID;
import org.bukkit.entity.Player;

public class SkinManagerTest {
   private SkinManagerImpl manager;
   private SkinCache memCache;
   private SkinCache fileCache;
   private FakeSkinQueue mojangQueue;
   private FakeSkinQueue mineSkinQueue;

   @FPBeforeEach
   public void setUp(Player player) {
      this.memCache = new SkinCacheMemory();
      this.fileCache = new SkinCacheFile();
      this.mojangQueue = new FakeSkinQueue();
      this.mineSkinQueue = new FakeSkinQueue();
      this.manager = new SkinManagerImpl(new UUIDMemoryCache(), this.fileCache, this.memCache, this.mojangQueue, this.mineSkinQueue);
   }

   @FPAfterEach
   public void tearDown(Player player) {
      this.memCache.clear();
      this.fileCache.clear();
   }

   @FPTest(
      name = "SkinManagerImpl#cacheSkin"
   )
   public void testCacheSkin(Player player) {
      SkinData data = new SkinData("TestSkin", SkinData.SkinVariant.SLIM, "TestSignature", "TestTexture");
      this.manager.cacheSkin(data);
      SkinCacheData gotFromFile = this.fileCache.getSkin("TestSkin");
      Expectable.expect(gotFromFile).toBeDefined();
      Expectable.expect(gotFromFile.skinData()).toEqual(data);
      SkinCacheData gotFromMem = this.memCache.getSkin("TestSkin");
      Expectable.expect(gotFromMem).toBeDefined();
      Expectable.expect(gotFromMem.skinData()).toEqual(data);
   }

   @FPTest(
      name = "SkinManagerImpl#getByUUID"
   )
   public void testGetByUUID(Player player) {
      record TestCase(
         String name,
         UUID uuid,
         SkinData.SkinVariant variant,
         SkinData cachedFile,
         SkinData cachedMem,
         SkinData expData,
         boolean expQueued,
         SkinGenerationRequest mojangRequest
      ) {
      }

      TestCase[] testCases = new TestCase[]{
         new TestCase(
            "Skin is cached in file",
            UUID.fromString("12345678-1234-1234-1234-123456789012"),
            SkinData.SkinVariant.SLIM,
            new SkinData("12345678-1234-1234-1234-123456789012", SkinData.SkinVariant.SLIM, "TestSignatureFile", "TestTextureFile"),
            null,
            new SkinData("12345678-1234-1234-1234-123456789012", SkinData.SkinVariant.SLIM, "TestSignatureFile", "TestTextureFile"),
            false,
            null
         ),
         new TestCase(
            "Skin is cached in memory",
            UUID.fromString("12345678-1234-1234-1234-123456789012"),
            SkinData.SkinVariant.SLIM,
            null,
            new SkinData("12345678-1234-1234-1234-123456789012", SkinData.SkinVariant.SLIM, "TestSignatureMem", "TestTextureMem"),
            new SkinData("12345678-1234-1234-1234-123456789012", SkinData.SkinVariant.SLIM, "TestSignatureMem", "TestTextureMem"),
            false,
            null
         ),
         new TestCase(
            "Skin is not cached",
            UUID.fromString("12345678-1234-1234-1234-123456789012"),
            SkinData.SkinVariant.SLIM,
            null,
            null,
            new SkinData("12345678-1234-1234-1234-123456789012", SkinData.SkinVariant.SLIM, null, null),
            true,
            new SkinGenerationRequest("12345678-1234-1234-1234-123456789012", SkinData.SkinVariant.SLIM)
         )
      };

      for (TestCase testCase : testCases) {
         this.setUp(player);
         System.out.println("Running test case: " + testCase.name);
         if (testCase.cachedFile != null) {
            this.fileCache.addSkin(testCase.cachedFile);
         }

         if (testCase.cachedMem != null) {
            this.memCache.addSkin(testCase.cachedMem);
         }

         SkinData got = this.manager.getByUUID(testCase.uuid, testCase.variant);
         Expectable.expect(got).toEqual(testCase.expData);
         if (testCase.expQueued) {
            Expectable.expect(this.mojangQueue.getQueue().size()).toBe(1);
            SkinGenerationRequest queued = this.mojangQueue.getQueue().getFirst();
            Expectable.expect(queued).toEqual(testCase.mojangRequest);
         } else {
            Expectable.expect(this.mojangQueue.getQueue().isEmpty()).toBe(true);
         }

         this.tearDown(player);
      }
   }

   @FPTest(
      name = "SkinManagerImpl#getByUsername valid"
   )
   public void testGetByUsernameValid(Player player) {
      String username = "OliverHD";
      SkinData.SkinVariant variant = SkinData.SkinVariant.SLIM;
      SkinData got = this.manager.getByUsername(username, variant);
      Expectable.expect(got).toBeDefined();
      Expectable.expect(got.getIdentifier()).toEqual(username);
      Expectable.expect(got.getVariant()).toEqual(variant);
      Expectable.expect(this.mojangQueue.getQueue().size()).toBe(1);
      SkinGenerationRequest queued = this.mojangQueue.getQueue().getFirst();
      Expectable.expect(queued).toBeDefined();
      Expectable.expect(queued.getID()).toHaveLength(UUID.randomUUID().toString().length());
      Expectable.expect(queued.getVariant()).toEqual(variant);
   }

   @FPTest(
      name = "SkinManagerImpl#getByUsername invalid"
   )
   public void testGetByUsernameInvalid(Player player) {
      String username = "._.";
      SkinData.SkinVariant variant = SkinData.SkinVariant.SLIM;
      Runnable runnable = () -> this.manager.getByUsername(username, variant);
      SkinLoadException ex = Expectable.expect(runnable).toThrow(SkinLoadException.class);
      Expectable.expect(ex).toBeDefined();
      Expectable.expect(ex.getReason()).toBe(SkinLoadException.Reason.INVALID_USERNAME);
   }

   @FPTest(
      name = "SkinManagerImpl#getByURL valid"
   )
   public void testGetByURLValid(Player player) {
      String url = "https://example.com/skin.png";
      SkinData.SkinVariant variant = SkinData.SkinVariant.SLIM;
      SkinData got = this.manager.getByURL(url, variant);
      Expectable.expect(got).toBeDefined();
      Expectable.expect(got.getIdentifier()).toEqual(url);
      Expectable.expect(got.getVariant()).toEqual(variant);
      Expectable.expect(this.mineSkinQueue.getQueue().size()).toBe(1);
      SkinGenerationRequest queued = this.mineSkinQueue.getQueue().getFirst();
      Expectable.expect(queued).toBeDefined();
      Expectable.expect(queued.getID()).toEqual(url);
      Expectable.expect(queued.getVariant()).toEqual(variant);
      Expectable.expect(queued.getMineskinRequest()).toBeDefined();
   }

   @FPTest(
      name = "SkinManagerImpl#getByURL invalid"
   )
   public void testGetByURLInvalid(Player player) {
      String url = "invalid-url!";
      SkinData.SkinVariant variant = SkinData.SkinVariant.SLIM;
      Runnable runnable = () -> this.manager.getByURL(url, variant);
      SkinLoadException ex = Expectable.expect(runnable).toThrow(SkinLoadException.class);
      Expectable.expect(ex).toBeDefined();
      Expectable.expect(ex.getReason()).toBe(SkinLoadException.Reason.INVALID_URL);
   }

   @FPTest(
      name = "SkinManagerImpl#getByFile invalid"
   )
   public void testGetByFileInvalid(Player player) {
      String filePath = "invalid-file-path-" + System.currentTimeMillis() + ".html";
      SkinData.SkinVariant variant = SkinData.SkinVariant.SLIM;
      Runnable runnable = () -> this.manager.getByFile(filePath, variant);
      SkinLoadException ex = Expectable.expect(runnable).toThrow(SkinLoadException.class);
      Expectable.expect(ex).toBeDefined();
      Expectable.expect(ex.getReason()).toBe(SkinLoadException.Reason.INVALID_FILE);
   }
}
