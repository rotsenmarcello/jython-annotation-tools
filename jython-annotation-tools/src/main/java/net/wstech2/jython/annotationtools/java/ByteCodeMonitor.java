/* 
 * 
 * Jython Annotation Tools. 
 * 
 * A library enabling Java annotations and  
 * SpringFramework integration for Jython classes.
 * 
 * http://www.wstech2.net/jat/
 * 
 * Copyright 2016 rotsen.marcello@wstech2.net.
 * 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package net.wstech2.jython.annotationtools.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.python.core.BytecodeNotification;
import org.python.core.BytecodeNotification.Callback;

public class ByteCodeMonitor implements Callback {
	
	public final static String CLASS_BYTECODE_EXPORT_DIR_PROPERTY="org.python.compiler.bytecodemonitor.exportdir"; 

	private static ByteCodeMonitor instance = null;
	private static Object block = new Object();
	
	private  Set<String> monitoredClasses = Collections.synchronizedSet(new HashSet<String>());
	
	public static ByteCodeMonitor getInstance(){
		if (instance != null) {
			return instance;
		}
		synchronized (block) {
			if (instance != null) {
				return instance;
			}
			instance = new ByteCodeMonitor();
			BytecodeNotification.register(instance);
			return instance;
		}
	}

	public void registerMonitoredClass(String className) {
		monitoredClasses.add(className);
		
	}

	public void notify(String name, byte[] bytes, Class c) {
		if(monitoredClasses.contains(name)==false){
			return;
		}
		String cacheDirectory=System.getProperty("org.python.compiler.bytecodemonitor.exportdir");
		try {
			String filePath = cacheDirectory + "/" + name.replace('.', '/') + ".class";
			new File(filePath.substring(0,filePath.lastIndexOf('/')) ).mkdirs();
			File f = new File(filePath);
			
			f.createNewFile();
			FileOutputStream fo = new FileOutputStream(f,false);
			fo.write(bytes, 0, bytes.length);
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadClassBytes(String name, byte[] bytes, Class c) throws Exception {
		ClassLoader urlClassLoader = (URLClassLoader) this.getClass().getClassLoader();
		Class<ClassLoader> urlClass = ClassLoader.class;
		Method method = urlClass.getDeclaredMethod("defineClass",
				new Class[] { String.class, byte[].class, int.class, int.class });
		method.setAccessible(true);
		//Class<?> c = (Class<?>) method.invoke(urlClassLoader, new Object[] { name, bytes,0,bytes.length });
		
		method = urlClass.getDeclaredMethod("resolveClass",
				new Class[] { Class.class});
		method.setAccessible(true);
		method.invoke(urlClassLoader, new Object[] { c });
		
	}
}
