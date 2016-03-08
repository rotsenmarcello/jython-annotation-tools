// 
// Based on  BytecodeLoader.java by ("Copyright (c) Corporation for National Research Initiatives")
// 
// Modified in 2016 by rotsen.marcello@wstech2.net
//
//
package net.wstech2.jython.annotationtools.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.python.core.BytecodeNotification;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyRunnable;
import org.python.core.imp;
import org.python.objectweb.asm.ClassReader;

/**
 * Utility class for loading compiled python modules and java classes defined in python modules.
 */
public class CustomBytecodeLoader {

    /**
     * Turn the java byte code in data into a java class.
     *
     * @param name
     *            the name of the class
     * @param data
     *            the java byte code.
     * @param referents
     *            superclasses and interfaces that the new class will reference.
     * @throws Exception 
     */
    public  Class<?> makeClass(String name, byte[] data, Class<?>... referents) throws Exception {
        Loader loader = new Loader(this.getClass().getClassLoader());
        for (Class<?> referent : referents) {
            try {
                ClassLoader cur = referent.getClassLoader();
                if (cur != null) {
                    loader.addParent(cur);
                }
            } catch (SecurityException e) {
            }
        }
        Class<?> c = loader.loadClassFromBytes(name, data);
        BytecodeNotification.notify(name, data, c);
        return c;
    }

    /**
     * Turn the java byte code in data into a java class.
     *
     * @param name
     *            the name of the class
     * @param referents
     *            superclasses and interfaces that the new class will reference.
     * @param data
     *            the java byte code.
     * @throws Exception 
     */
    public  Class<?> makeClass(String name, List<Class<?>> referents, byte[] data) throws Exception {
        if (referents != null) {
            return makeClass(name, data, referents.toArray(new Class[referents.size()]));
        }
        return makeClass(name, data);
    }

    /**
     * Turn the java byte code for a compiled python module into a java class.
     *
     * @param name
     *            the name of the class
     * @param data
     *            the java byte code.
     */
    public  PyCode makeCode(String name, byte[] data, String filename) {
        try {
            Class<?> c = makeClass(name, data);
            Object o = c.getConstructor(new Class[] {String.class})
                    .newInstance(new Object[] {filename});
            return ((PyRunnable)o).getMain();
        } catch (Exception e) {
            throw Py.JavaError(e);
        }
    }

    public static class Loader  {

        private List<ClassLoader> parents = Collections.synchronizedList(new ArrayList<ClassLoader>());
        
        private ClassLoader targetClassLoader;

        public Loader(ClassLoader targetClassLoader) {
            //super(new URL[0]);
            this.targetClassLoader = targetClassLoader;
            parents.add(imp.getSyspathJavaLoader());
            addParent(this.targetClassLoader);
        }

        public void addParent(ClassLoader referent) {
            if (!parents.contains(referent)) {
                parents.add(0, referent);
            }
        }

        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//            Class<?> c = targetClassLoader.findLoadedClass(name);
//            if (c != null) {
//                return c;
//            }
            for (ClassLoader loader : parents) {
                try {
                    return loader.loadClass(name);
                } catch (ClassNotFoundException cnfe) {}
            }
            // couldn't find the .class file on sys.path
            throw new ClassNotFoundException(name);
        }

        public Class<?> loadClassFromBytes(String name, byte[] data) throws Exception {
            if (name.endsWith("$py")) {
                try {
                    // Get the real class name: we might request a 'bar'
                    // Jython module that was compiled as 'foo.bar', or
                    // even 'baz.__init__' which is compiled as just 'baz'
                    ClassReader cr = new ClassReader(data);
                    name = cr.getClassName().replace('/', '.');
                } catch (RuntimeException re) {
                    // Probably an invalid .class, fallback to the
                    // specified name
                }
            }
            try{
            	return this.loadClass(name, true);
            } catch (ClassNotFoundException cnfe) {}
            return loadClassBytes(name,data);
            
        }
        
        
        public Class<?>  loadClassBytes(String name, byte[] bytes) throws Exception {
    		Class<ClassLoader> urlClass = ClassLoader.class;
    		Method method = urlClass.getDeclaredMethod("defineClass",
    				new Class[] { String.class, byte[].class, int.class, int.class });
    		method.setAccessible(true);
    		Class<?> c = (Class<?>) method.invoke(targetClassLoader, new Object[] { name, bytes,0,bytes.length });
    		
    		method = urlClass.getDeclaredMethod("resolveClass",
    				new Class[] { Class.class});
    		method.setAccessible(true);
    		method.invoke(targetClassLoader, new Object[] { c });
    		return c;
    	}
    }
}
