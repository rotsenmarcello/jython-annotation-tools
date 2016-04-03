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

package org.python.compiler;

import java.io.IOException;
import java.util.List;

import org.python.compiler.ProxyCodeHelpers.AnnotationDescr;
import org.python.objectweb.asm.AnnotationVisitor;
import org.python.objectweb.asm.FieldVisitor;



public class ClassFileUtils  {


	public static FieldVisitor addField(ClassFile cFile, String name, Class<?> type, int access, List<AnnotationDescr> annotationDescrs,  String signature,
            Object value ) throws IOException {
		
		FieldVisitor fv = cFile.cw.visitField(access, name, ProxyCodeHelpers.mapType(type), null, value);
		cFile.fieldVisitors.add(fv);
		if(annotationDescrs!=null){
			for (AnnotationDescr ad: annotationDescrs) {
	            AnnotationVisitor av = fv.visitAnnotation(ad.getName(), true);
	            if (ad.hasFields()) {
	            	ClassFile.visitAnnotations(av, ad.getFields());
	            }
	            av.visitEnd();
	        }	
		}
		return fv;
	}

}
