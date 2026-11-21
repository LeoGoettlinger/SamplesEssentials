package oshi.jna.platform.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public interface NtDll extends com.sun.jna.platform.win32.NtDll {
   NtDll INSTANCE = (NtDll)Native.load("NtDll", NtDll.class, W32APIOptions.DEFAULT_OPTIONS);
   int PROCESS_BASIC_INFORMATION = 0;

   int NtQueryInformationProcess(HANDLE var1, int var2, Pointer var3, int var4, IntByReference var5);

   @FieldOrder({"DosPath", "Handle"})
   public static class CURDIR extends Structure {
      public NtDll.UNICODE_STRING DosPath;
      public Pointer Handle;
   }

   @FieldOrder({"pad", "pad2", "ProcessParameters"})
   public static class PEB extends Structure {
      public byte[] pad = new byte[4];
      public Pointer[] pad2 = new Pointer[3];
      public Pointer ProcessParameters;
   }

   @FieldOrder({"Reserved1", "PebBaseAddress", "Reserved2"})
   public static class PROCESS_BASIC_INFORMATION extends Structure {
      public Pointer Reserved1;
      public Pointer PebBaseAddress;
      public Pointer[] Reserved2 = new Pointer[4];
   }

   @FieldOrder({"Flags", "Length", "TimeStamp", "DosPath"})
   public static class RTL_DRIVE_LETTER_CURDIR extends Structure {
      public short Flags;
      public short Length;
      public int TimeStamp;
      public NtDll.STRING DosPath;
   }

   @FieldOrder({"MaximumLength", "Length", "Flags", "DebugFlags", "ConsoleHandle", "ConsoleFlags", "StandardInput", "StandardOutput", "StandardError", "CurrentDirectory", "DllPath", "ImagePathName", "CommandLine", "Environment", "StartingX", "StartingY", "CountX", "CountY", "CountCharsX", "CountCharsY", "FillAttribute", "WindowFlags", "ShowWindowFlags", "WindowTitle", "DesktopInfo", "ShellInfo", "RuntimeData", "CurrentDirectories", "EnvironmentSize", "EnvironmentVersion", "PackageDependencyData", "ProcessGroupId", "LoaderThreads", "RedirectionDllName", "HeapPartitionName", "DefaultThreadpoolCpuSetMasks", "DefaultThreadpoolCpuSetMaskCount"})
   public static class RTL_USER_PROCESS_PARAMETERS extends Structure {
      public int MaximumLength;
      public int Length;
      public int Flags;
      public int DebugFlags;
      public HANDLE ConsoleHandle;
      public int ConsoleFlags;
      public HANDLE StandardInput;
      public HANDLE StandardOutput;
      public HANDLE StandardError;
      public NtDll.CURDIR CurrentDirectory;
      public NtDll.UNICODE_STRING DllPath;
      public NtDll.UNICODE_STRING ImagePathName;
      public NtDll.UNICODE_STRING CommandLine;
      public Pointer Environment;
      public int StartingX;
      public int StartingY;
      public int CountX;
      public int CountY;
      public int CountCharsX;
      public int CountCharsY;
      public int FillAttribute;
      public int WindowFlags;
      public int ShowWindowFlags;
      public NtDll.UNICODE_STRING WindowTitle;
      public NtDll.UNICODE_STRING DesktopInfo;
      public NtDll.UNICODE_STRING ShellInfo;
      public NtDll.UNICODE_STRING RuntimeData;
      public NtDll.RTL_DRIVE_LETTER_CURDIR[] CurrentDirectories = new NtDll.RTL_DRIVE_LETTER_CURDIR[32];
      public ULONG_PTR EnvironmentSize;
      public ULONG_PTR EnvironmentVersion;
      public Pointer PackageDependencyData;
      public int ProcessGroupId;
      public int LoaderThreads;
      public NtDll.UNICODE_STRING RedirectionDllName;
      public NtDll.UNICODE_STRING HeapPartitionName;
      public ULONG_PTR DefaultThreadpoolCpuSetMasks;
      public int DefaultThreadpoolCpuSetMaskCount;
   }

   @FieldOrder({"Length", "MaximumLength", "Buffer"})
   public static class STRING extends Structure {
      public short Length;
      public short MaximumLength;
      public Pointer Buffer;
   }

   @FieldOrder({"Length", "MaximumLength", "Buffer"})
   public static class UNICODE_STRING extends Structure {
      public short Length;
      public short MaximumLength;
      public Pointer Buffer;
   }
}
