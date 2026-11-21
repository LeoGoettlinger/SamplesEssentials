package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jdk14;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.JsonCreator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.AnnotationIntrospector;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg.MapperConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedClass;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.AnnotatedConstructor;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.ClassUtil;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.NativeImageUtil;

public class JDK14Util {
   public static String[] getRecordFieldNames(Class<?> recordType) {
      return JDK14Util.RecordAccessor.instance().getRecordFieldNames(recordType);
   }

   public static AnnotatedConstructor findRecordConstructor(DeserializationContext ctxt, BeanDescription beanDesc, List<String> names) {
      return findRecordConstructor(beanDesc.getClassInfo(), ctxt.getAnnotationIntrospector(), ctxt.getConfig(), names);
   }

   public static AnnotatedConstructor findRecordConstructor(AnnotatedClass recordClass, AnnotationIntrospector intr, MapperConfig<?> config, List<String> names) {
      return new JDK14Util.CreatorLocator(recordClass, intr, config).locate(names);
   }

   static class CreatorLocator {
      protected final AnnotatedClass _recordClass;
      protected final MapperConfig<?> _config;
      protected final AnnotationIntrospector _intr;
      protected final List<AnnotatedConstructor> _constructors;
      protected final AnnotatedConstructor _primaryConstructor;
      protected final JDK14Util.RawTypeName[] _recordFields;

      CreatorLocator(AnnotatedClass recordClass, AnnotationIntrospector intr, MapperConfig<?> config) {
         this._recordClass = recordClass;
         this._intr = intr;
         this._config = config;
         this._recordFields = JDK14Util.RecordAccessor.instance().getRecordFields(recordClass.getRawType());
         if (this._recordFields == null) {
            this._constructors = recordClass.getConstructors();
            this._primaryConstructor = null;
         } else {
            int argCount = this._recordFields.length;
            AnnotatedConstructor primary = null;
            if (argCount == 0) {
               primary = recordClass.getDefaultConstructor();
               this._constructors = Collections.singletonList(primary);
            } else {
               this._constructors = recordClass.getConstructors();

               label40:
               for (AnnotatedConstructor ctor : this._constructors) {
                  if (ctor.getParameterCount() == argCount) {
                     for (int i = 0; i < argCount; i++) {
                        if (!ctor.getRawParameterType(i).equals(this._recordFields[i].rawType)) {
                           continue label40;
                        }
                     }

                     primary = ctor;
                     break;
                  }
               }
            }

            if (primary == null) {
               throw new IllegalArgumentException(
                  "Failed to find the canonical Record constructor of type " + ClassUtil.getTypeDescription(this._recordClass.getType())
               );
            }

            this._primaryConstructor = primary;
         }
      }

      public AnnotatedConstructor locate(List<String> names) {
         for (AnnotatedConstructor ctor : this._constructors) {
            JsonCreator.Mode creatorMode = this._intr.findCreatorAnnotation(this._config, ctor);
            if (null != creatorMode && JsonCreator.Mode.DISABLED != creatorMode) {
               if (JsonCreator.Mode.DELEGATING == creatorMode) {
                  return null;
               }

               if (ctor != this._primaryConstructor) {
                  return null;
               }
            }
         }

         if (this._recordFields == null) {
            return null;
         } else {
            for (JDK14Util.RawTypeName field : this._recordFields) {
               names.add(field.name);
            }

            return this._primaryConstructor;
         }
      }
   }

   static class RawTypeName {
      public final Class<?> rawType;
      public final String name;

      public RawTypeName(Class<?> rt, String n) {
         this.rawType = rt;
         this.name = n;
      }
   }

   static class RecordAccessor {
      private final Method RECORD_GET_RECORD_COMPONENTS;
      private final Method RECORD_COMPONENT_GET_NAME;
      private final Method RECORD_COMPONENT_GET_TYPE;
      private static final JDK14Util.RecordAccessor INSTANCE;
      private static final RuntimeException PROBLEM;

      private RecordAccessor() throws RuntimeException {
         try {
            this.RECORD_GET_RECORD_COMPONENTS = Class.class.getMethod("getRecordComponents");
            Class<?> c = Class.forName("java.lang.reflect.RecordComponent");
            this.RECORD_COMPONENT_GET_NAME = c.getMethod("getName");
            this.RECORD_COMPONENT_GET_TYPE = c.getMethod("getType");
         } catch (Exception var2) {
            throw new RuntimeException(
               String.format("Failed to access Methods needed to support `java.lang.Record`: (%s) %s", var2.getClass().getName(), var2.getMessage()), var2
            );
         }
      }

      public static JDK14Util.RecordAccessor instance() {
         if (PROBLEM != null) {
            throw PROBLEM;
         } else {
            return INSTANCE;
         }
      }

      public String[] getRecordFieldNames(Class<?> recordType) throws IllegalArgumentException {
         Object[] components = this.recordComponents(recordType);
         if (components == null) {
            return null;
         } else {
            String[] names = new String[components.length];

            for (int i = 0; i < components.length; i++) {
               try {
                  names[i] = (String)this.RECORD_COMPONENT_GET_NAME.invoke(components[i]);
               } catch (Exception var6) {
                  throw new IllegalArgumentException(
                     String.format("Failed to access name of field #%d (of %d) of Record type %s", i, components.length, ClassUtil.nameOf(recordType)), var6
                  );
               }
            }

            return names;
         }
      }

      public JDK14Util.RawTypeName[] getRecordFields(Class<?> recordType) throws IllegalArgumentException {
         Object[] components = this.recordComponents(recordType);
         if (components == null) {
            return null;
         } else {
            JDK14Util.RawTypeName[] results = new JDK14Util.RawTypeName[components.length];

            for (int i = 0; i < components.length; i++) {
               String name;
               try {
                  name = (String)this.RECORD_COMPONENT_GET_NAME.invoke(components[i]);
               } catch (Exception var9) {
                  throw new IllegalArgumentException(
                     String.format("Failed to access name of field #%d (of %d) of Record type %s", i, components.length, ClassUtil.nameOf(recordType)), var9
                  );
               }

               Class<?> type;
               try {
                  type = (Class<?>)this.RECORD_COMPONENT_GET_TYPE.invoke(components[i]);
               } catch (Exception var8) {
                  throw new IllegalArgumentException(
                     String.format("Failed to access type of field #%d (of %d) of Record type %s", i, components.length, ClassUtil.nameOf(recordType)), var8
                  );
               }

               results[i] = new JDK14Util.RawTypeName(type, name);
            }

            return results;
         }
      }

      protected Object[] recordComponents(Class<?> recordType) throws IllegalArgumentException {
         try {
            return (Object[])this.RECORD_GET_RECORD_COMPONENTS.invoke(recordType);
         } catch (Exception var3) {
            if (NativeImageUtil.isUnsupportedFeatureError(var3)) {
               return null;
            } else {
               throw new IllegalArgumentException("Failed to access RecordComponents of type " + ClassUtil.nameOf(recordType));
            }
         }
      }

      static {
         RuntimeException prob = null;
         JDK14Util.RecordAccessor inst = null;

         try {
            inst = new JDK14Util.RecordAccessor();
         } catch (RuntimeException var3) {
            prob = var3;
         }

         INSTANCE = inst;
         PROBLEM = prob;
      }
   }
}
