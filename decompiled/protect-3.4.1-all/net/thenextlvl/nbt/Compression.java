package net.thenextlvl.nbt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Compression {
   Compression NONE = new CompressionImpl.None();
   Compression GZIP = new CompressionImpl.GZIP();
   Compression ZLIB = new CompressionImpl.ZLIB();

   InputStream decompress(InputStream var1) throws IOException;

   OutputStream compress(OutputStream var1) throws IOException;
}
