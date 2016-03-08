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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.python.compiler.ProxyCodeHelpers.AnnotationDescr;
import org.python.core.PyString;

public class MemberDescription {

	public enum TYPE {
		CLASS_ANNOTATIONS_METHOD,
		FIELD_METHOD,		
		METHOD
	};
	
	private TYPE type = MemberDescription.TYPE.METHOD;
	private Map<String,Object> extraInfo = new ConcurrentHashMap<String,Object>();
	private String name;
	private PyString pyName;
	private int accessType = Modifier.PUBLIC;
	private List<AnnotationDescr> annotations = new ArrayList<AnnotationDescr>();
		
	public Map<String,Object> getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(Map<String,Object> extraInfo) {
		this.extraInfo = extraInfo;
	}

	public MemberDescription(){
		
	}
	
	public MemberDescription( PyString pyName){
		this.name = pyName.toString();
		this.pyName = pyName;
	}
	
	public MemberDescription( String name,PyString pyName){
		this.name = name;
		this.pyName = pyName;
	}
	
	public TYPE getType() {
		return type;
	}
	public void setType(TYPE type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PyString getPyName() {
		return pyName;
	}
	public void setPyName(PyString pyName) {
		this.pyName = pyName;
	}
	public int getAccessType() {
		return accessType;
	}
	public void setAccessType(int accessType) {
		this.accessType = accessType;
	}
	public List<AnnotationDescr> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(List<AnnotationDescr> annotations) {
		this.annotations = annotations;
	}

	
	
}
