/* 
 * 
 * Jython Annotation Tools. 
 * 
 * A library enabling Java annotations and  
 * SpringFramework integration for Jython classes.
 * 
 * This file is based on org.python.compiler.CustomMaker.java by Jython.org
 * 
 * Modified 2016 rotsen.marcello@wstech2.net 
 *
 */

package net.wstech2.jython.annotationtools.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.python.compiler.ClassFileUtils;
import org.python.compiler.Code;
import org.python.compiler.CustomMaker;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

public class CustomMakerWithSystemClassLoader extends CustomMaker {

	private static CustomBytecodeLoader customBytecodeLoader = new CustomBytecodeLoader();
	
	
	
	public CustomMakerWithSystemClassLoader(Class<?> superclass, Class<?>[] interfaces, String pythonClass, String pythonModule,
			String myClass, PyObject dict) {
		super(superclass, interfaces, pythonClass, pythonModule, myClass, dict);
		
	}
	
    @Override
    public void addProxy() throws Exception {
    	addPyProxyInterfaceMethods();

        // _initProxy method
        Code code = classfile.addMethod("__initProxy__", makeSig("V", $objArr), Modifier.PUBLIC);

        code.visitVarInsn(ALOAD, 0);
        code.visitLdcInsn(pythonModule);
        code.visitLdcInsn(pythonClass);
        code.visitVarInsn(ALOAD, 1);
        code.visitMethodInsn(INVOKESTATIC, "org/python/core/Py", "initProxy",
            makeSig("V", $pyProxy, $str, $str, $objArr), false);
        code.visitInsn(RETURN);

    }
	   
    //copied from ProxyMaker.addProxy()
	public void addPyProxyInterfaceMethods() throws Exception {
	        // implement PyProxy interface
	        classfile.addField("__proxy", $pyObj, Modifier.PROTECTED | Modifier.TRANSIENT );
	        // setProxy methods
	        Code code = classfile.addMethod("_setPyInstance", makeSig("V", $pyObj), Modifier.PUBLIC);
	        code.aload(0);
	        code.aload(1);
	        code.putfield(classfile.name, "__proxy", $pyObj);
	        code.return_();

	        // getProxy method
	        code = classfile.addMethod("_getPyInstance", makeSig($pyObj), Modifier.PUBLIC);
	        code.aload(0);
	        code.getfield(classfile.name, "__proxy", $pyObj);
	        code.areturn();

	        String pySys =  "Lorg/python/core/PySystemState;";
	        // implement PyProxy interface
	        classfile.addField("__systemState", pySys, Modifier.PROTECTED | Modifier.TRANSIENT);

	        // setProxy method
	        code = classfile.addMethod("_setPySystemState",
	                                   makeSig("V", pySys),
	                                   Modifier.PUBLIC);

	        code.aload(0);
	        code.aload(1);
	        code.putfield(classfile.name, "__systemState", pySys);
	        code.return_();

	        // getProxy method
	        code = classfile.addMethod("_getPySystemState", makeSig(pySys), Modifier.PUBLIC);
	        code.aload(0);
	        code.getfield(classfile.name, "__systemState", pySys);
	        code.areturn();
	    }
	
	// By default makeClass will have the same behavior as MakeProxies calling JavaMaker,
    // other than the debug behavior of saving the classfile (as controlled by
    // Options.ProxyDebugDirectory; users of CustomMaker simply need to save it themselves).
    //
    // Override this method to get custom classes built from any desired source.
	@Override
    public Class<?> makeClass() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            build(bytes); // Side effect of writing to bytes
            saveBytes(bytes);
            List<Class<?>> secondary = new LinkedList(Arrays.asList(interfaces));
            List<Class<?>> referents = null;
            if (secondary != null) {
                if (superclass != null) {
                    secondary.add(0, superclass);
                }
                referents = secondary;
            } else if (superclass != null) {
                referents = new ArrayList<Class<?>>(1);
                referents.add(superclass);
            }
            return customBytecodeLoader.makeClass(myClass, referents, bytes.toByteArray());
        } catch (Exception exc) {
            throw Py.JavaError(exc);
        }
    }
	
	
}
