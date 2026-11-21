package com.viaversion.viaversion.protocol;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.ProtocolPathKey;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.VersionedPacketTransformer;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.ServerProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.Reference2ObjectOpenHashMap;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import com.viaversion.viaversion.protocol.packet.VersionedPacketTransformerImpl;
import com.viaversion.viaversion.protocol.shared_registration.def.DefaultRegistrations;
import com.viaversion.viaversion.protocols.base.InitialBaseProtocol;
import com.viaversion.viaversion.protocols.base.v1_16.ClientboundBaseProtocol1_16;
import com.viaversion.viaversion.protocols.base.v1_7.ClientboundBaseProtocol1_7;
import com.viaversion.viaversion.protocols.base.v1_7.ServerboundBaseProtocol1_7;
import com.viaversion.viaversion.protocols.v1_10to1_11.Protocol1_10To1_11;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.Protocol1_11_1To1_12;
import com.viaversion.viaversion.protocols.v1_11to1_11_1.Protocol1_11To1_11_1;
import com.viaversion.viaversion.protocols.v1_12_1to1_12_2.Protocol1_12_1To1_12_2;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.Protocol1_12To1_12_1;
import com.viaversion.viaversion.protocols.v1_13_1to1_13_2.Protocol1_13_1To1_13_2;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.protocols.v1_14_1to1_14_2.Protocol1_14_1To1_14_2;
import com.viaversion.viaversion.protocols.v1_14_2to1_14_3.Protocol1_14_2To1_14_3;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.Protocol1_14_3To1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.Protocol1_14_4To1_15;
import com.viaversion.viaversion.protocols.v1_14to1_14_1.Protocol1_14To1_14_1;
import com.viaversion.viaversion.protocols.v1_15_1to1_15_2.Protocol1_15_1To1_15_2;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15to1_15_1.Protocol1_15To1_15_1;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.Protocol1_16_1To1_16_2;
import com.viaversion.viaversion.protocols.v1_16_2to1_16_3.Protocol1_16_2To1_16_3;
import com.viaversion.viaversion.protocols.v1_16_3to1_16_4.Protocol1_16_3To1_16_4;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16to1_16_1.Protocol1_16To1_16_1;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.Protocol1_17_1To1_18;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.Protocol1_17To1_17_1;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.Protocol1_18_2To1_19;
import com.viaversion.viaversion.protocols.v1_18to1_18_2.Protocol1_18To1_18_2;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.Protocol1_19_1To1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.Protocol1_19_3To1_19_4;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.Protocol1_19_4To1_20;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.Protocol1_19To1_19_1;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.Protocol1_21_11To26_1;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.Protocol1_21_5To1_21_6;
import com.viaversion.viaversion.protocols.v1_21_6to1_21_7.Protocol1_21_6To1_21_7;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.Protocol1_21_7To1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.Protocol1_9_1To1_9_3;
import com.viaversion.viaversion.protocols.v1_9_3to1_10.Protocol1_9_3To1_10;
import com.viaversion.viaversion.protocols.v1_9to1_9_1.Protocol1_9To1_9_1;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.util.MathUtil;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ProtocolManagerImpl implements ProtocolManager {
   public static final AtomicInteger PROTOCOL_INDEX = new AtomicInteger();
   private static final InitialBaseProtocol BASE_PROTOCOL = new InitialBaseProtocol();
   private final Object2ObjectMap<ProtocolVersion, Object2ObjectMap<ProtocolVersion, Protocol>> registryMap = new Object2ObjectOpenHashMap<>(32);
   private final Map<Class<? extends Protocol>, Protocol<?, ?, ?, ?>> protocols = new Reference2ObjectOpenHashMap<>(64);
   private final Map<ProtocolPathKey, List<ProtocolPathEntry>> pathCache = new ConcurrentHashMap<>();
   private final Set<ProtocolVersion> supportedVersions = new HashSet<>();
   private final List<Pair<Range<ProtocolVersion>, Protocol>> serverboundBaseProtocols = Lists.newCopyOnWriteArrayList();
   private final List<Pair<Range<ProtocolVersion>, Protocol>> clientboundBaseProtocols = Lists.newCopyOnWriteArrayList();
   private final ReadWriteLock mappingLoaderLock = new ReentrantReadWriteLock();
   private Map<Class<? extends Protocol>, CompletableFuture<Void>> mappingLoaderFutures = new Reference2ObjectOpenHashMap<>();
   private ExecutorService mappingLoaderExecutor;
   private boolean mappingsLoaded;
   private ServerProtocolVersion serverProtocolVersion = new ServerProtocolVersionSingleton(ProtocolVersion.unknown);
   private int maxPathDeltaIncrease;
   private int maxProtocolPathSize = 50;

   public ProtocolManagerImpl() {
      int parallelism = MathUtil.clamp(Runtime.getRuntime().availableProcessors(), 2, 12);
      AtomicInteger threadIndex = new AtomicInteger(0);
      this.mappingLoaderExecutor = new ForkJoinPool(parallelism, pool -> {
         ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
         worker.setName("Via-Mappingloader-" + threadIndex.incrementAndGet());
         return worker;
      }, ForkJoinWorkerThread.getDefaultUncaughtExceptionHandler(), true);
      DefaultRegistrations.apply();
   }

   public void registerProtocols() {
      BASE_PROTOCOL.initialize();
      BASE_PROTOCOL.register(Via.getManager().getProviders());
      this.registerBaseProtocol(Direction.CLIENTBOUND, new ClientboundBaseProtocol1_7(), Range.closedOpen(ProtocolVersion.v1_7_2, ProtocolVersion.v1_16));
      this.registerBaseProtocol(Direction.CLIENTBOUND, new ClientboundBaseProtocol1_16(), Range.atLeast(ProtocolVersion.v1_16));
      this.registerBaseProtocol(Direction.SERVERBOUND, new ServerboundBaseProtocol1_7(), Range.atLeast(ProtocolVersion.v1_7_2));
      this.registerProtocol(new Protocol1_8To1_9(), ProtocolVersion.v1_9, ProtocolVersion.v1_8);
      this.registerProtocol(new Protocol1_9To1_9_1(), Arrays.asList(ProtocolVersion.v1_9_1, ProtocolVersion.v1_9_2), ProtocolVersion.v1_9);
      this.registerProtocol(new Protocol1_9_1To1_9_3(), ProtocolVersion.v1_9_3, ProtocolVersion.v1_9_2);
      this.registerProtocol(new Protocol1_9_3To1_10(), ProtocolVersion.v1_10, ProtocolVersion.v1_9_3);
      this.registerProtocol(new Protocol1_10To1_11(), ProtocolVersion.v1_11, ProtocolVersion.v1_10);
      this.registerProtocol(new Protocol1_11To1_11_1(), ProtocolVersion.v1_11_1, ProtocolVersion.v1_11);
      this.registerProtocol(new Protocol1_11_1To1_12(), ProtocolVersion.v1_12, ProtocolVersion.v1_11_1);
      this.registerProtocol(new Protocol1_12To1_12_1(), ProtocolVersion.v1_12_1, ProtocolVersion.v1_12);
      this.registerProtocol(new Protocol1_12_1To1_12_2(), ProtocolVersion.v1_12_2, ProtocolVersion.v1_12_1);
      this.registerProtocol(new Protocol1_12_2To1_13(), ProtocolVersion.v1_13, ProtocolVersion.v1_12_2);
      this.registerProtocol(new Protocol1_13To1_13_1(), ProtocolVersion.v1_13_1, ProtocolVersion.v1_13);
      this.registerProtocol(new Protocol1_13_1To1_13_2(), ProtocolVersion.v1_13_2, ProtocolVersion.v1_13_1);
      this.registerProtocol(new Protocol1_13_2To1_14(), ProtocolVersion.v1_14, ProtocolVersion.v1_13_2);
      this.registerProtocol(new Protocol1_14To1_14_1(), ProtocolVersion.v1_14_1, ProtocolVersion.v1_14);
      this.registerProtocol(new Protocol1_14_1To1_14_2(), ProtocolVersion.v1_14_2, ProtocolVersion.v1_14_1);
      this.registerProtocol(new Protocol1_14_2To1_14_3(), ProtocolVersion.v1_14_3, ProtocolVersion.v1_14_2);
      this.registerProtocol(new Protocol1_14_3To1_14_4(), ProtocolVersion.v1_14_4, ProtocolVersion.v1_14_3);
      this.registerProtocol(new Protocol1_14_4To1_15(), ProtocolVersion.v1_15, ProtocolVersion.v1_14_4);
      this.registerProtocol(new Protocol1_15To1_15_1(), ProtocolVersion.v1_15_1, ProtocolVersion.v1_15);
      this.registerProtocol(new Protocol1_15_1To1_15_2(), ProtocolVersion.v1_15_2, ProtocolVersion.v1_15_1);
      this.registerProtocol(new Protocol1_15_2To1_16(), ProtocolVersion.v1_16, ProtocolVersion.v1_15_2);
      this.registerProtocol(new Protocol1_16To1_16_1(), ProtocolVersion.v1_16_1, ProtocolVersion.v1_16);
      this.registerProtocol(new Protocol1_16_1To1_16_2(), ProtocolVersion.v1_16_2, ProtocolVersion.v1_16_1);
      this.registerProtocol(new Protocol1_16_2To1_16_3(), ProtocolVersion.v1_16_3, ProtocolVersion.v1_16_2);
      this.registerProtocol(new Protocol1_16_3To1_16_4(), ProtocolVersion.v1_16_4, ProtocolVersion.v1_16_3);
      this.registerProtocol(new Protocol1_16_4To1_17(), ProtocolVersion.v1_17, ProtocolVersion.v1_16_4);
      this.registerProtocol(new Protocol1_17To1_17_1(), ProtocolVersion.v1_17_1, ProtocolVersion.v1_17);
      this.registerProtocol(new Protocol1_17_1To1_18(), ProtocolVersion.v1_18, ProtocolVersion.v1_17_1);
      this.registerProtocol(new Protocol1_18To1_18_2(), ProtocolVersion.v1_18_2, ProtocolVersion.v1_18);
      this.registerProtocol(new Protocol1_18_2To1_19(), ProtocolVersion.v1_19, ProtocolVersion.v1_18_2);
      this.registerProtocol(new Protocol1_19To1_19_1(), ProtocolVersion.v1_19_1, ProtocolVersion.v1_19);
      this.registerProtocol(new Protocol1_19_1To1_19_3(), ProtocolVersion.v1_19_3, ProtocolVersion.v1_19_1);
      this.registerProtocol(new Protocol1_19_3To1_19_4(), ProtocolVersion.v1_19_4, ProtocolVersion.v1_19_3);
      this.registerProtocol(new Protocol1_19_4To1_20(), ProtocolVersion.v1_20, ProtocolVersion.v1_19_4);
      this.registerProtocol(new Protocol1_20To1_20_2(), ProtocolVersion.v1_20_2, ProtocolVersion.v1_20);
      this.registerProtocol(new Protocol1_20_2To1_20_3(), ProtocolVersion.v1_20_3, ProtocolVersion.v1_20_2);
      this.registerProtocol(new Protocol1_20_3To1_20_5(), ProtocolVersion.v1_20_5, ProtocolVersion.v1_20_3);
      this.registerProtocol(new Protocol1_20_5To1_21(), ProtocolVersion.v1_21, ProtocolVersion.v1_20_5);
      this.registerProtocol(new Protocol1_21To1_21_2(), ProtocolVersion.v1_21_2, ProtocolVersion.v1_21);
      this.registerProtocol(new Protocol1_21_2To1_21_4(), ProtocolVersion.v1_21_4, ProtocolVersion.v1_21_2);
      this.registerProtocol(new Protocol1_21_4To1_21_5(), ProtocolVersion.v1_21_5, ProtocolVersion.v1_21_4);
      this.registerProtocol(new Protocol1_21_5To1_21_6(), ProtocolVersion.v1_21_6, ProtocolVersion.v1_21_5);
      this.registerProtocol(new Protocol1_21_6To1_21_7(), ProtocolVersion.v1_21_7, ProtocolVersion.v1_21_6);
      this.registerProtocol(new Protocol1_21_7To1_21_9(), ProtocolVersion.v1_21_9, ProtocolVersion.v1_21_7);
      this.registerProtocol(new Protocol1_21_9To1_21_11(), ProtocolVersion.v1_21_11, ProtocolVersion.v1_21_9);
      this.registerProtocol(new Protocol1_21_11To26_1(), ProtocolVersion.v26_1, ProtocolVersion.v1_21_11);
      this.registerProtocol(new Protocol26_1To26_2(), ProtocolVersion.v26_2, ProtocolVersion.v26_1);
      this.registerProtocol(new Protocol26_2To26_3(), ProtocolVersion.v26_3, ProtocolVersion.v26_2);
   }

   @Override
   public void registerProtocol(Protocol protocol, ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
      this.registerProtocol(protocol, Collections.singletonList(clientVersion), serverVersion);
   }

   @Override
   public void registerProtocol(Protocol protocol, List<ProtocolVersion> supportedClientVersion, ProtocolVersion serverVersion) {
      if (protocol instanceof AbstractProtocol<?, ?, ?, ?> abstractProtocol) {
         abstractProtocol.setServerVersion(serverVersion);
         abstractProtocol.setClientVersion(supportedClientVersion.stream().max(ProtocolVersion::compareTo).orElseThrow());
      }

      if (!this.pathCache.isEmpty()) {
         this.pathCache.clear();
      }

      this.protocols.put((Class<? extends Protocol>)protocol.getClass(), protocol);

      for (ProtocolVersion clientVersion : supportedClientVersion) {
         Preconditions.checkArgument(!clientVersion.equals(serverVersion));
         Object2ObjectMap<ProtocolVersion, Protocol> protocolMap = this.registryMap.computeIfAbsent(clientVersion, s -> new Object2ObjectOpenHashMap<>(2));
         protocolMap.put(serverVersion, protocol);
      }

      protocol.register(Via.getManager().getProviders());
      if (Via.getManager().isInitialized()) {
         this.refreshVersions();
      }

      if (!protocol.hasMappingDataToLoad()) {
         protocol.initialize();
      } else {
         if (this.mappingLoaderExecutor != null) {
            if (protocol.dependsOn() != null) {
               this.addMappingLoaderFuture((Class<? extends Protocol>)protocol.getClass(), protocol.dependsOn(), protocol::loadMappingData);
            } else {
               this.addMappingLoaderFuture((Class<? extends Protocol>)protocol.getClass(), protocol::loadMappingData);
            }
         } else {
            protocol.loadMappingData();
         }
      }
   }

   @Override
   public void registerBaseProtocol(Direction direction, Protocol baseProtocol, Range<ProtocolVersion> supportedProtocols) {
      Preconditions.checkArgument(baseProtocol.isBaseProtocol(), "Protocol is not a base protocol");
      ProtocolVersion lower = supportedProtocols.hasLowerBound() ? (ProtocolVersion)supportedProtocols.lowerEndpoint() : null;
      ProtocolVersion upper = supportedProtocols.hasUpperBound() ? (ProtocolVersion)supportedProtocols.upperEndpoint() : null;
      Preconditions.checkArgument(lower == null || lower.getVersionType() != VersionType.SPECIAL, "Base protocol versions cannot contain a special version");
      Preconditions.checkArgument(upper == null || upper.getVersionType() != VersionType.SPECIAL, "Base protocol versions cannot contain a special version");
      baseProtocol.initialize();
      if (direction == Direction.SERVERBOUND) {
         this.serverboundBaseProtocols.add(new Pair<>(supportedProtocols, baseProtocol));
      } else {
         this.clientboundBaseProtocols.add(new Pair<>(supportedProtocols, baseProtocol));
      }

      baseProtocol.register(Via.getManager().getProviders());
      if (Via.getManager().isInitialized()) {
         this.refreshVersions();
      }
   }

   public void refreshVersions() {
      this.supportedVersions.clear();
      this.supportedVersions.add(this.serverProtocolVersion.lowestSupportedProtocolVersion());

      for (ProtocolVersion version : ProtocolVersion.getProtocols()) {
         List<ProtocolPathEntry> protocolPath = this.getProtocolPath(version, this.serverProtocolVersion.lowestSupportedProtocolVersion());
         if (protocolPath != null) {
            this.supportedVersions.add(version);

            for (ProtocolPathEntry pathEntry : protocolPath) {
               this.supportedVersions.add(pathEntry.outputProtocolVersion());
            }
         }
      }
   }

   @Nullable
   @Override
   public List<ProtocolPathEntry> getProtocolPath(ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
      if (clientVersion == serverVersion) {
         return null;
      } else {
         ProtocolPathKey protocolKey = new ProtocolPathKeyImpl(clientVersion, serverVersion);
         List<ProtocolPathEntry> protocolList = this.pathCache.get(protocolKey);
         if (protocolList != null) {
            return protocolList.isEmpty() ? null : protocolList;
         } else {
            List<ProtocolPathEntry> path = this.calculateProtocolPath(clientVersion, serverVersion);
            if (path == null) {
               this.pathCache.put(protocolKey, List.of());
               return null;
            } else {
               this.pathCache.put(protocolKey, path);
               return path;
            }
         }
      }
   }

   @Override
   public <C extends ClientboundPacketType, S extends ServerboundPacketType> VersionedPacketTransformer<C, S> createPacketTransformer(
      ProtocolVersion inputVersion, @Nullable Class<C> clientboundPacketsClass, @Nullable Class<S> serverboundPacketsClass
   ) {
      Preconditions.checkArgument(clientboundPacketsClass != ClientboundPacketType.class && serverboundPacketsClass != ServerboundPacketType.class);
      return new VersionedPacketTransformerImpl<>(inputVersion, clientboundPacketsClass, serverboundPacketsClass);
   }

   @Nullable
   private List<ProtocolPathEntry> calculateProtocolPath(ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
      Set<ProtocolVersion> visited = new HashSet<>();
      visited.add(clientVersion);
      Deque<ProtocolManagerImpl.PathNode> queue = new ArrayDeque<>();
      queue.add(new ProtocolManagerImpl.PathNode(null, clientVersion, null, 0));

      ProtocolManagerImpl.PathNode node;
      while ((node = queue.poll()) != null) {
         if (node.depth() <= this.maxProtocolPathSize) {
            Object2ObjectMap<ProtocolVersion, Protocol> toServerProtocolMap = this.registryMap.get(node.version());
            if (toServerProtocolMap != null) {
               Protocol protocol = toServerProtocolMap.get(serverVersion);
               if (protocol != null) {
                  return this.reconstructPath(new ProtocolManagerImpl.PathNode(node, serverVersion, protocol, node.depth() + 1));
               }

               for (Entry<ProtocolVersion, Protocol> entry : toServerProtocolMap.entrySet()) {
                  ProtocolVersion translatedToVersion = entry.getKey();
                  if (!visited.contains(translatedToVersion)) {
                     if (this.maxPathDeltaIncrease != -1 && translatedToVersion.getVersionType() == node.version().getVersionType()) {
                        int delta = Math.abs(serverVersion.getVersion() - translatedToVersion.getVersion())
                           - Math.abs(serverVersion.getVersion() - node.version().getVersion());
                        if (delta > this.maxPathDeltaIncrease) {
                           continue;
                        }
                     }

                     visited.add(translatedToVersion);
                     queue.add(new ProtocolManagerImpl.PathNode(node, translatedToVersion, entry.getValue(), node.depth() + 1));
                  }
               }
            }
         }
      }

      return null;
   }

   private List<ProtocolPathEntry> reconstructPath(ProtocolManagerImpl.PathNode node) {
      ProtocolPathEntry[] path = new ProtocolPathEntry[node.depth()];

      for (ProtocolManagerImpl.PathNode current = node; current.protocol() != null; current = current.parent()) {
         path[current.depth() - 1] = new ProtocolPathEntryImpl(current.version(), current.protocol());
      }

      return List.of(path);
   }

   @Nullable
   @Override
   public <T extends Protocol> T getProtocol(Class<T> protocolClass) {
      return (T)this.protocols.get(protocolClass);
   }

   @Nullable
   @Override
   public Protocol getProtocol(ProtocolVersion clientVersion, ProtocolVersion serverVersion) {
      Object2ObjectMap<ProtocolVersion, Protocol> map = this.registryMap.get(clientVersion);
      return map != null ? map.get(serverVersion) : null;
   }

   @Override
   public List<Protocol> getBaseProtocols(@Nullable ProtocolVersion clientVersion, @Nullable ProtocolVersion serverVersion) {
      List<Protocol> list = new ArrayList<>();
      if (clientVersion != null) {
         for (Pair<Range<ProtocolVersion>, Protocol> rangeProtocol : this.serverboundBaseProtocols) {
            if (rangeProtocol.key().contains(clientVersion)) {
               list.add(rangeProtocol.value());
            }
         }
      }

      if (serverVersion != null) {
         for (Pair<Range<ProtocolVersion>, Protocol> rangeProtocolx : this.clientboundBaseProtocols) {
            if (rangeProtocolx.key().contains(serverVersion)) {
               list.add(rangeProtocolx.value());
            }
         }
      }

      return list;
   }

   @Override
   public Collection<Protocol<?, ?, ?, ?>> getProtocols() {
      return Collections.unmodifiableCollection(this.protocols.values());
   }

   @Override
   public ServerProtocolVersion getServerProtocolVersion() {
      return this.serverProtocolVersion;
   }

   public void setServerProtocol(ServerProtocolVersion serverProtocolVersion) {
      this.serverProtocolVersion = serverProtocolVersion;
   }

   @Override
   public boolean isWorkingPipe() {
      for (Object2ObjectMap<ProtocolVersion, Protocol> map : this.registryMap.values()) {
         for (ProtocolVersion protocolVersion : this.serverProtocolVersion.supportedProtocolVersions()) {
            if (map.containsKey(protocolVersion)) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public SortedSet<ProtocolVersion> getSupportedVersions() {
      return Collections.unmodifiableSortedSet(new TreeSet<>(this.supportedVersions));
   }

   @Override
   public void setMaxPathDeltaIncrease(int maxPathDeltaIncrease) {
      this.maxPathDeltaIncrease = Math.max(-1, maxPathDeltaIncrease);
   }

   @Override
   public int getMaxPathDeltaIncrease() {
      return this.maxPathDeltaIncrease;
   }

   @Override
   public int getMaxProtocolPathSize() {
      return this.maxProtocolPathSize;
   }

   @Override
   public void setMaxProtocolPathSize(int maxProtocolPathSize) {
      this.maxProtocolPathSize = maxProtocolPathSize;
   }

   @Override
   public Protocol getBaseProtocol() {
      return BASE_PROTOCOL;
   }

   @Override
   public void completeMappingDataLoading(Class<? extends Protocol> protocolClass) {
      if (!this.mappingsLoaded) {
         CompletableFuture<Void> future = this.getMappingLoaderFuture(protocolClass);
         if (future != null) {
            future.join();
         }
      }
   }

   @Override
   public boolean checkForMappingCompletion(boolean propagateErrors) {
      this.mappingLoaderLock.readLock().lock();

      try {
         if (this.mappingsLoaded) {
            return false;
         } else {
            for (CompletableFuture<Void> future : this.mappingLoaderFutures.values()) {
               if (!future.isDone()) {
                  return false;
               }

               if (propagateErrors) {
                  future.join();
               }
            }

            this.shutdownLoaderExecutor();
            return true;
         }
      } finally {
         this.mappingLoaderLock.readLock().unlock();
      }
   }

   @Override
   public void addMappingLoaderFuture(Class<? extends Protocol> protocolClass, Runnable runnable) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(runnable, this.mappingLoaderExecutor)
         .whenComplete(($, t) -> this.logIfErrored(protocolClass, t));
      this.mappingLoaderLock.writeLock().lock();

      try {
         this.mappingLoaderFutures.put(protocolClass, future);
      } finally {
         this.mappingLoaderLock.writeLock().unlock();
      }
   }

   @Override
   public void addMappingLoaderFuture(Class<? extends Protocol> protocolClass, Class<? extends Protocol> dependsOn, Runnable runnable) {
      CompletableFuture<Void> future = this.getMappingLoaderFuture(dependsOn)
         .thenRunAsync(runnable, this.mappingLoaderExecutor)
         .whenComplete(($, t) -> this.logIfErrored(protocolClass, t));
      this.mappingLoaderLock.writeLock().lock();

      try {
         this.mappingLoaderFutures.put(protocolClass, future);
      } finally {
         this.mappingLoaderLock.writeLock().unlock();
      }
   }

   @Nullable
   @Override
   public CompletableFuture<Void> getMappingLoaderFuture(Class<? extends Protocol> protocolClass) {
      this.mappingLoaderLock.readLock().lock();

      CompletableFuture var2;
      try {
         var2 = this.mappingsLoaded ? null : this.mappingLoaderFutures.get(protocolClass);
      } finally {
         this.mappingLoaderLock.readLock().unlock();
      }

      return var2;
   }

   @Override
   public PacketWrapper createPacketWrapper(@Nullable PacketType packetType, @Nullable ByteBuf buf, UserConnection connection) {
      return new PacketWrapperImpl(packetType, buf, connection);
   }

   @Deprecated
   @Override
   public PacketWrapper createPacketWrapper(int packetId, @Nullable ByteBuf buf, UserConnection connection) {
      return new PacketWrapperImpl(packetId, buf, connection);
   }

   @Override
   public boolean hasLoadedMappings() {
      return this.mappingsLoaded;
   }

   @Override
   public int registeredProtocolCount() {
      return PROTOCOL_INDEX.get();
   }

   public void shutdownLoaderExecutor() {
      Preconditions.checkArgument(!this.mappingsLoaded);
      Via.getPlatform().getLogger().info("Finished mapping loading, shutting down loader executor.");
      this.mappingsLoaded = true;
      this.mappingLoaderExecutor.shutdown();
      this.mappingLoaderExecutor = null;
      this.mappingLoaderFutures.clear();
      this.mappingLoaderFutures = null;
      MappingDataLoader.INSTANCE.clearCache();
   }

   private void logIfErrored(Class<? extends Protocol> protocolClass, @Nullable Throwable throwable) {
      if (throwable != null) {
         Via.getPlatform().getLogger().log(Level.SEVERE, "Error during loading of " + protocolClass.getSimpleName(), throwable);
      }
   }

   private record PathNode(@Nullable ProtocolManagerImpl.PathNode parent, ProtocolVersion version, @Nullable Protocol protocol, int depth) {
   }
}
