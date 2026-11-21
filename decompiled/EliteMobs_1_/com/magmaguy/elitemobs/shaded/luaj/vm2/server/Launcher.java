package com.magmaguy.elitemobs.shaded.luaj.vm2.server;

import java.io.InputStream;
import java.io.Reader;

public interface Launcher {
   Object[] launch(String var1, Object[] var2);

   Object[] launch(InputStream var1, Object[] var2);

   Object[] launch(Reader var1, Object[] var2);
}
