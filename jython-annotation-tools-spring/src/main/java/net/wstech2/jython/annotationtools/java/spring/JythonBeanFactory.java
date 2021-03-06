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

package net.wstech2.jython.annotationtools.java.spring;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class JythonBeanFactory {

	private static JythonBeanFactory instance = null;
	private static Object block = new Object();
	PythonInterpreter interpreter = new PythonInterpreter();
	private static PySystemState state = new PySystemState();
	private static PyObject importer = state.getBuiltins().__getitem__(Py.newString("__import__"));

	public JythonBeanFactory() {
	}

	public static JythonBeanFactory getInstance() {
		if (instance == null) {
			synchronized (block) {
				if (instance == null) {
					instance = new JythonBeanFactory();
				}
			}
		}
		return instance;
	}

	public Object createInstance(String moduleName, String className) {
		PyObject module = importer.__call__(Py.newString(moduleName));
		PyObject klass = module.__getattr__(className);
		return klass.__call__().__tojava__(Object.class);
	}
}
