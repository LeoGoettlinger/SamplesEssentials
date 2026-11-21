package xyz.lychee.lagfixer.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import lombok.Generated;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class HardwareMenu extends AbstractMenu {
   private final ItemBuilder i1 = this.skull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJiMTcxMmI5MDdjZTZiMTQwMmVhYWMyOGVjMjRhNGQ5NTU2OGY0YWI4N2U1OTc5ODBjMTViMjJiYmJkN2E1In19fQ==",
      "&b\ud83c\udf10 &f&lNetwork:"
   );
   private final ItemBuilder i2 = this.skull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDU2Yzk0NjE5MDMxMjMxNjhjZTY2N2VhZDdlYTU2YTUxNjEzMDk3MDQ5YmE2NDc4MzJiMzcyMmFmZmJlYjYzNiJ9fX0=",
      "&9\ud83d\udcbe &f&lProcessor:"
   );
   private final ItemBuilder i3 = this.skull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjYwYjAwNGYzNjBlMjg4NTVjY2YxMjM1YzJiZGVhMGEyOTk3YjBiYzAzMjU4ZTJkYzI0YWI4YTI1NzBhZWE2In19fQ==",
      "&a\ud83c\udf9e &f&lMemory:"
   );
   private final ItemBuilder i4 = this.skull(
      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg5MWFmZDM5ZTJlNjczOGJjNmE4Yzg4YzI0OWZkYmNmNGE0NWM0YTI0MjQ3ZjFkMTBiYWUwYzY0ZDk5OTFlMSJ9fX0=",
      "&e\ud83d\udcc2 &f&lDisk Storage:"
   );
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final SystemInfo si;
   private final HardwareAbstractionLayer hal;
   private HardwareMenu.HardwareData hardwareData;
   private long lastNetworkUpdate = 0L;
   private long prevBytesSent = 0L;
   private long prevBytesRecv = 0L;

   public HardwareMenu(LagFixer plugin, int size, String title) {
      super(plugin, size, title, 3, true);
      this.si = new SystemInfo();
      this.hal = this.si.getHardware();
      this.loadOrCreateHardwareData(plugin);
      this.surroundInventory();
      this.fillButtons();
      this.getInv()
         .setItem(
            11,
            ItemBuilder.createSkull(
                  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWY0NDZhOGY5Mjg0YzYyY2Y4ZDQ5MWZiZGIzMzhmZDM5ZWJiZWJlMzVlOTU5YzJmYzRmNzg2YzY3NTIyZWZiIn19fQ=="
               )
               .setName("&4⚠ &c&lPERFORMANCE WARNING!")
               .setLore(
                  " &8{*} &7Heavy impact on servers with &c<4 &7CPU threads",
                  " &8{*} &7Don't leave this GUI open indefinitely",
                  " ",
                  "&a✔ Asynchronous updates &8(&7non-blocking&8)",
                  "&a✔ Auto-closes after inactivity"
               )
               .build()
         );
      this.getInv().setItem(size - 1, ConfigMenu.getBack());
      this.fillInventory();
   }

   private void loadOrCreateHardwareData(LagFixer plugin) {
      File dataFile = new File(plugin.getDataFolder(), "hardware_data.json");
      if (dataFile.exists()) {
         try {
            try (Reader reader = new FileReader(dataFile)) {
               this.hardwareData = (HardwareMenu.HardwareData)this.gson.fromJson(reader, HardwareMenu.HardwareData.class);
            }

            return;
         } catch (IOException var8) {
         }
      }

      this.collectAndSaveHardwareData(dataFile);
   }

   private void collectAndSaveHardwareData(File dataFile) {
      SystemInfo tempSi = new SystemInfo();
      HardwareAbstractionLayer tempHal = tempSi.getHardware();
      CentralProcessor cpu = tempHal.getProcessor();
      GlobalMemory memory = tempHal.getMemory();
      List<HWDiskStore> disks = tempHal.getDiskStores();
      this.hardwareData = new HardwareMenu.HardwareData();
      this.hardwareData.setCpuName(cpu.getProcessorIdentifier().getName());
      this.hardwareData.setCpuMicroarchitecture(cpu.getProcessorIdentifier().getMicroarchitecture());
      this.hardwareData.setCpuVendorFreq(cpu.getProcessorIdentifier().getVendorFreq());
      this.hardwareData.setLogicalCores(cpu.getLogicalProcessorCount());
      this.hardwareData.setPhysicalCores(cpu.getPhysicalProcessorCount());
      this.hardwareData.setTotalMemory(memory.getTotal());
      this.hardwareData.setPageSize(memory.getPageSize());
      this.hardwareData
         .setMemoryType(memory.getPhysicalMemory().isEmpty() ? "Unknown" : ((PhysicalMemory)memory.getPhysicalMemory().getFirst()).getMemoryType());
      this.hardwareData.setDiskCount(disks.size());
      this.hardwareData.setPartitionCount(disks.stream().mapToInt(d -> d.getPartitions().size()).sum());
      this.hardwareData.setNetworkInterfaceCount(tempHal.getNetworkIFs().size());

      try (Writer writer = new FileWriter(dataFile)) {
         this.gson.toJson(this.hardwareData, writer);
      } catch (IOException var12) {
      }
   }

   private void fillButtons() {
      this.getInv().setItem(12, this.i1.build());
      this.getInv().setItem(13, this.i2.build());
      this.getInv().setItem(14, this.i3.build());
      this.getInv().setItem(15, this.i4.build());
   }

   private ItemBuilder skull(String textureHash, String name) {
      return ItemBuilder.createSkull(textureHash).setName(name).setLore(" &8{*} &7Loading lore...");
   }

   @Override
   public void update() {
      TimingUtil timing = TimingUtil.startNew();

      try {
         List<NetworkIF> networks = this.hal.getNetworkIFs();
         long bytesSent = 0L;
         long bytesRecv = 0L;

         for (NetworkIF net : networks) {
            net.updateAttributes();
            bytesSent += net.getBytesSent();
            bytesRecv += net.getBytesRecv();
         }

         this.i1
            .setLore(
               " &8{*} &7Interfaces: &f" + this.hardwareData.getNetworkInterfaceCount(),
               " &8{*} &7Sent: &f" + FormatUtil.formatBytes(bytesSent),
               " &8{*} &7Received: &f" + FormatUtil.formatBytes(bytesRecv),
               " &8{*} &7Throughput: &f" + this.calculateNetworkSpeed(bytesSent, bytesRecv),
               "&7Network data updated in &e" + timing.stop()
            );
      } catch (Throwable var21) {
         this.i1.setLore(" &8{*} &7An error occurred while", "&7retrieving network information :(");
      }

      timing.start();

      try {
         this.i2
            .setLore(
               " &8{*} &7Model: &f" + this.hardwareData.getCpuName(),
               " &8{*} &7Logical cores: &f" + this.hardwareData.getLogicalCores(),
               " &8{*} &7Physical cores: &f" + this.hardwareData.getPhysicalCores(),
               " &8{*} &7Microarchitecture: &f" + this.hardwareData.getCpuMicroarchitecture(),
               " &8{*} &7Frequency: &f" + FormatUtil.formatHertz(this.hardwareData.getCpuVendorFreq()),
               "&7Cpu data updated in &e" + timing.stop()
            );
      } catch (Throwable var19) {
         this.i2.setLore(" &8{*} &7An error occurred while", "&7retrieving processer information :(");
      }

      timing.start();

      try {
         GlobalMemory memory = this.hal.getMemory();
         long usedMem = this.hardwareData.getTotalMemory() - memory.getAvailable();
         double memPercent = usedMem * 100.0 / this.hardwareData.getTotalMemory();
         VirtualMemory swap = memory.getVirtualMemory();
         this.i3
            .setLore(
               " &8{*} &7Total RAM: &f" + FormatUtil.formatBytesDecimal(this.hardwareData.getTotalMemory()),
               " &8{*} &7Used RAM: &f" + FormatUtil.formatBytesDecimal(usedMem) + String.format(" (&f%.1f%%&7)", memPercent),
               " &8{*} &7Available RAM: &f" + FormatUtil.formatBytesDecimal(memory.getAvailable()),
               " &8{*} &7Page Size: &f" + FormatUtil.formatBytesDecimal(this.hardwareData.getPageSize()),
               " &8{*} &7Memory Type: &f" + this.hardwareData.getMemoryType(),
               " &8{*} &7Swap Total: &f" + FormatUtil.formatBytesDecimal(swap.getSwapTotal()),
               " &8{*} &7Swap Used: &f" + FormatUtil.formatBytesDecimal(swap.getSwapUsed()),
               "&7Memory data updated in &e" + timing.stop()
            );
      } catch (Throwable var18) {
         this.i3.setLore(" &8{*} &7An error occurred while", "&7retrieving memory information :(");
      }

      timing.start();

      try {
         long totalReadBytes = 0L;
         long totalWriteBytes = 0L;
         long readOps = 0L;
         long writeOps = 0L;
         long queueLength = 0L;

         for (HWDiskStore disk : this.hal.getDiskStores()) {
            disk.updateAttributes();
            totalReadBytes += disk.getReadBytes();
            totalWriteBytes += disk.getWriteBytes();
            readOps += disk.getReads();
            writeOps += disk.getWrites();
            queueLength += disk.getCurrentQueueLength();
         }

         long totalDiskSpace = 0L;
         long usedDiskSpace = 0L;

         for (OSFileStore fs : this.si.getOperatingSystem().getFileSystem().getFileStores()) {
            totalDiskSpace += fs.getTotalSpace();
            usedDiskSpace += fs.getTotalSpace() - fs.getUsableSpace();
         }

         double diskUsagePercent = totalDiskSpace > 0L ? usedDiskSpace * 100.0 / totalDiskSpace : 0.0;
         this.i4
            .setLore(
               " &8{*} &7Physical Disks: &f" + this.hardwareData.getDiskCount(),
               " &8{*} &7Partitions: &f" + this.hardwareData.getPartitionCount(),
               " &8{*} &7Total Capacity: &f" + FormatUtil.formatBytes(totalDiskSpace),
               " &8{*} &7Used Space: &f" + FormatUtil.formatBytes(usedDiskSpace) + String.format(" (&f%.1f%%&7)", diskUsagePercent),
               " &8{*} &7Read Data: &f" + FormatUtil.formatBytes(totalReadBytes) + " (&f" + readOps + " ops&7)",
               " &8{*} &7Written Data: &f" + FormatUtil.formatBytes(totalWriteBytes) + " (&f" + writeOps + " ops&7)",
               " &8{*} &7Disk Queues: &f" + queueLength,
               "&7Storage data updated in &e" + timing.stop()
            );
      } catch (Throwable var20) {
         this.i4.setLore(" &8{*} &7An error occurred while", "&7retrieving storage information :(");
      }

      this.fillButtons();
   }

   private String calculateNetworkSpeed(long currentSent, long currentRecv) {
      long timeDiff = System.currentTimeMillis() - this.lastNetworkUpdate;
      if (this.lastNetworkUpdate != 0L && timeDiff != 0L) {
         double sentSpeed = (currentSent - this.prevBytesSent) / (timeDiff / 1000.0);
         double recvSpeed = (currentRecv - this.prevBytesRecv) / (timeDiff / 1000.0);
         this.lastNetworkUpdate = System.currentTimeMillis();
         this.prevBytesSent = currentSent;
         this.prevBytesRecv = currentRecv;
         return String.format("&a▲&f%s/s &c▼&f%s/s", FormatUtil.formatBytesDecimal((long)sentSpeed), FormatUtil.formatBytesDecimal((long)recvSpeed));
      } else {
         this.lastNetworkUpdate = System.currentTimeMillis();
         this.prevBytesSent = currentSent;
         this.prevBytesRecv = currentRecv;
         return "Calculating...";
      }
   }

   @Override
   public void handleClick(InventoryClickEvent e, ItemStack item) {
   }

   @Override
   public AbstractMenu previousMenu() {
      return MenuCommand.getInstance().getMainMenu();
   }

   private static class HardwareData {
      String cpuName;
      String cpuMicroarchitecture;
      long cpuVendorFreq;
      int logicalCores;
      int physicalCores;
      long totalMemory;
      long pageSize;
      String memoryType;
      int diskCount;
      int partitionCount;
      int networkInterfaceCount;

      @Generated
      public String getCpuName() {
         return this.cpuName;
      }

      @Generated
      public String getCpuMicroarchitecture() {
         return this.cpuMicroarchitecture;
      }

      @Generated
      public long getCpuVendorFreq() {
         return this.cpuVendorFreq;
      }

      @Generated
      public int getLogicalCores() {
         return this.logicalCores;
      }

      @Generated
      public int getPhysicalCores() {
         return this.physicalCores;
      }

      @Generated
      public long getTotalMemory() {
         return this.totalMemory;
      }

      @Generated
      public long getPageSize() {
         return this.pageSize;
      }

      @Generated
      public String getMemoryType() {
         return this.memoryType;
      }

      @Generated
      public int getDiskCount() {
         return this.diskCount;
      }

      @Generated
      public int getPartitionCount() {
         return this.partitionCount;
      }

      @Generated
      public int getNetworkInterfaceCount() {
         return this.networkInterfaceCount;
      }

      @Generated
      public void setCpuName(String cpuName) {
         this.cpuName = cpuName;
      }

      @Generated
      public void setCpuMicroarchitecture(String cpuMicroarchitecture) {
         this.cpuMicroarchitecture = cpuMicroarchitecture;
      }

      @Generated
      public void setCpuVendorFreq(long cpuVendorFreq) {
         this.cpuVendorFreq = cpuVendorFreq;
      }

      @Generated
      public void setLogicalCores(int logicalCores) {
         this.logicalCores = logicalCores;
      }

      @Generated
      public void setPhysicalCores(int physicalCores) {
         this.physicalCores = physicalCores;
      }

      @Generated
      public void setTotalMemory(long totalMemory) {
         this.totalMemory = totalMemory;
      }

      @Generated
      public void setPageSize(long pageSize) {
         this.pageSize = pageSize;
      }

      @Generated
      public void setMemoryType(String memoryType) {
         this.memoryType = memoryType;
      }

      @Generated
      public void setDiskCount(int diskCount) {
         this.diskCount = diskCount;
      }

      @Generated
      public void setPartitionCount(int partitionCount) {
         this.partitionCount = partitionCount;
      }

      @Generated
      public void setNetworkInterfaceCount(int networkInterfaceCount) {
         this.networkInterfaceCount = networkInterfaceCount;
      }
   }
}
