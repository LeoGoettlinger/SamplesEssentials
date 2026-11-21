package xyz.lychee.lagfixer.objects;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import lombok.Generated;
import xyz.lychee.lagfixer.managers.SupportManager;

public class ResourceMonitor extends AbstractMonitor {
   private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
   private double cpuProcess;
   private double cpuSystem;
   private double tps;
   private double mspt;
   private long ramFree;
   private long ramUsed;
   private long ramMax;
   private long ramTotal;

   public ResourceMonitor() {
      super(true, "resource");
   }

   public double cpuProcess() {
      return this.osBean.getProcessCpuLoad() * 100.0;
   }

   public double cpuSystem() {
      return this.osBean.getCpuLoad() * 100.0;
   }

   public double tps() {
      return SupportManager.getInstance().getNms().getTps();
   }

   public double mspt() {
      return SupportManager.getInstance().getFork().getMspt();
   }

   public double format(double d) {
      return (int)(d * 100.0) / 100.0;
   }

   public long formatBytes(long bytes) {
      return bytes / 1024L / 1024L;
   }

   @Override
   public void run() {
      this.cpuProcess = this.format(this.cpuProcess());
      this.cpuSystem = this.format(this.cpuSystem());
      this.tps = Math.min(this.format(this.tps()), 20.0);
      this.mspt = this.format(this.mspt());
      Runtime r = Runtime.getRuntime();
      this.ramFree = this.formatBytes(r.freeMemory());
      this.ramUsed = this.formatBytes(r.totalMemory() - r.freeMemory());
      this.ramMax = this.formatBytes(r.maxMemory());
      this.ramTotal = this.formatBytes(r.totalMemory());
   }

   @Generated
   public OperatingSystemMXBean getOsBean() {
      return this.osBean;
   }

   @Generated
   public double getCpuProcess() {
      return this.cpuProcess;
   }

   @Generated
   public double getCpuSystem() {
      return this.cpuSystem;
   }

   @Generated
   public double getTps() {
      return this.tps;
   }

   @Generated
   public double getMspt() {
      return this.mspt;
   }

   @Generated
   public long getRamFree() {
      return this.ramFree;
   }

   @Generated
   public long getRamUsed() {
      return this.ramUsed;
   }

   @Generated
   public long getRamMax() {
      return this.ramMax;
   }

   @Generated
   public long getRamTotal() {
      return this.ramTotal;
   }
}
