package com.sk89q.worldedit.util.io;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import javax.annotation.Nullable;

public interface ResourceLoader {
   @Nullable
   default URL getResource(Class<?> clazz, String pathName) throws IOException {
      Preconditions.checkArgument(!pathName.startsWith("/"), "pathName must not start with /");
      String qualifiedName = clazz.getName().substring(0, clazz.getName().lastIndexOf(46)).replace(".", "/") + "/" + pathName;
      return this.getRootResource(qualifiedName);
   }

   @Nullable
   default URL getRootResource(String pathName) throws IOException {
      Preconditions.checkArgument(!pathName.startsWith("/"), "pathName must not start with /");
      return this.getClass().getClassLoader().getResource(pathName);
   }

   Path getLocalResource(String pathName);
}
