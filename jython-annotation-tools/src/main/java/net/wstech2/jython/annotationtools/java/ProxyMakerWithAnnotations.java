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

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.python.compiler.ClassFileUtils;
import org.python.compiler.Code;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

public class ProxyMakerWithAnnotations extends CustomMakerWithSystemClassLoader {

	public static final String ANNOTATIONS_DICT_KEY = "annotations";
	public static final String PROXY_CONFIG_DICT_KEY = "__proxy_config";
	
	protected PyObject dict;
	
	protected Map<String,MemberDescription> members = new Hashtable<String,MemberDescription>();
	protected String classAnnotationsMemberName;
	protected String javaClassName ;  
	
	
	
	public ProxyMakerWithAnnotations(Class<?> superclass, Class<?>[] interfaces, String pythonClass, String pythonModule,
			String myClass, PyObject dict) {
		super(superclass, interfaces, pythonClass, pythonModule, myClass, dict);
		this.dict = dict;
		this.javaClassName = myClass;
	}
	
	
	@Override
	public void build() throws Exception {
		extractProxyConfig();
		super.build();
	}
	
	protected void extractProxyConfig() throws Exception {
		if(dict==null)return;
		
		for(PyObject memberName: dict.asIterable()){
			PyDictionary proxyConfig = getMemberAttributeByMemberNameAndAttrName(memberName, ProxyMakerWithAnnotations.PROXY_CONFIG_DICT_KEY, PyDictionary.class);
			if(proxyConfig == null){
				continue;
			}
			
			MemberDescription memberDesc = new MemberDescription((PyString)memberName);
			this.members.put(memberName.toString(), memberDesc);
			//iterate through annotation definitions
			PyList annotations = getAttributeByNameAndType( proxyConfig, ProxyMakerWithAnnotations.ANNOTATIONS_DICT_KEY,PyList.class);
			for(Object ann: annotations){
				processAnnotation(memberDesc,(PyDictionary)ann);
			}
		}
		checkRequiredParameters();
		registerAtBytecodeMonitorIfApplicable();
	}
	
	protected void registerAtBytecodeMonitorIfApplicable() {
		try{
			MemberDescription member = members.get(this.classAnnotationsMemberName);
			boolean register =  (Boolean)member.getExtraInfo().get("exportClassBytecodeToFile");
			if(register){
				ByteCodeMonitor.getInstance().registerMonitoredClass(this.javaClassName);				
			}
		}catch(Throwable t){}
	}


	protected void checkRequiredParameters() {
		String[] requiredAttributes =  {"__module__"};
		
		for(String s: requiredAttributes){
			PyObject member = dict.__finditem__(s);
			if(member == null){
				 throw Py.TypeError(String.format(
		                    "Attribute [%s] not found in [%s].",
		                    s, this.classfile.name));
			}
		}
	}


	protected void processAnnotation(MemberDescription memberDesc, PyDictionary ann) {
		AnnotationDescr annotationDescr = getAnnotationDescr(ann);
		//analyse control annotations (access modifiers, member type, etc.)

		if(setAdditionalInfoBySpecialAnnotationClass(memberDesc, annotationDescr)==false){
			memberDesc.getAnnotations().add(annotationDescr);	
		}	
	}
	
	protected void setMemberAccessTypeIfAvailable(MemberDescription memberDesc, AnnotationDescr annotationDescr) {
		
	}

	protected boolean setAdditionalInfoBySpecialAnnotationClass(MemberDescription memberDesc, AnnotationDescr annotationDescr) {
		if(annotationDescr.annotation.equals(JavaClassAnnotations.class)){
			setAdditionalInfoByJavaClassAnnotationsAnnotationClass(memberDesc, annotationDescr);
			return true;
		}
		else if(annotationDescr.annotation.equals(JavaBeanAttribute.class)){
			setAdditionalInfoByJavaBeanAttributeAnnotationClass(memberDesc, annotationDescr);
			return true;
		}
		else if(annotationDescr.annotation.equals(JavaAttribute.class)){
			setAdditionalInfoByJavaAttributeAnnotationClass(memberDesc, annotationDescr);
			return true;
		}
		else if(annotationDescr.annotation.equals(JavaPublicMethod.class)){
			setAdditionalInfoByJavaPublicMethodAnnotationClass(memberDesc, annotationDescr);
			return true;
		}
		
		return false;
	}

	protected void setAdditionalInfoByJavaClassAnnotationsAnnotationClass(MemberDescription memberDesc,
			AnnotationDescr annotationDescr) {
		memberDesc.setType(MemberDescription.TYPE.CLASS_ANNOTATIONS_METHOD);
		this.classAnnotationsMemberName = memberDesc.getName();
		memberDesc.getExtraInfo().putAll(annotationDescr.getFields());
	}


	protected void setAdditionalInfoByJavaBeanAttributeAnnotationClass(MemberDescription memberDesc,
			AnnotationDescr annotationDescr) {
		memberDesc.setType(MemberDescription.TYPE.FIELD_METHOD); 
		memberDesc.setAccessType(Modifier.PRIVATE);
		Class<?> type = (Class<?>)annotationDescr.getFields().get("value");
		annotationDescr.getFields().put("type", type);
		annotationDescr.getFields().remove("value");
		memberDesc.getExtraInfo().putAll(annotationDescr.getFields());
		memberDesc.getExtraInfo().put("addBeanGetterAndSetter", true);
	}


	protected void setAdditionalInfoByJavaAttributeAnnotationClass(MemberDescription memberDesc,
			AnnotationDescr annotationDescr) {
		memberDesc.setType(MemberDescription.TYPE.FIELD_METHOD);
		Integer access= (Integer)annotationDescr.getFields().get("access");
		if(access == null){
			access = Modifier.STATIC | Modifier.PUBLIC;
			annotationDescr.getFields().put("access", access);
		}
		memberDesc.setAccessType(access);
		memberDesc.getExtraInfo().putAll(annotationDescr.getFields());
		memberDesc.getExtraInfo().put("addBeanGetterAndSetter", false);
	}


	protected void setAdditionalInfoByJavaPublicMethodAnnotationClass(MemberDescription memberDesc,
			AnnotationDescr annotationDescr) {
		memberDesc.setType(MemberDescription.TYPE.METHOD);
		//TODO: ABSTRACT IS REQUIRED BY THE SUPER.addMethod
		memberDesc.setAccessType(Modifier.PUBLIC | Modifier.ABSTRACT);
		memberDesc.getExtraInfo().putAll(annotationDescr.getFields());
		if(memberDesc.getExtraInfo().get("paramTypes")==null){
			memberDesc.getExtraInfo().put("paramTypes", new Class[0]);
		}else if(memberDesc.getExtraInfo().get("paramTypes").getClass().isArray()==false){
			if(memberDesc.getExtraInfo().get("paramTypes") instanceof List){
				memberDesc.getExtraInfo().put("paramTypes", 
						((List) memberDesc.getExtraInfo().get("paramTypes")).toArray(new Class<?>[0]));
			}else{
				Class<?>[] c = new Class[1];
				c[0] = (Class<?>) memberDesc.getExtraInfo().get("paramTypes");
				memberDesc.getExtraInfo().put("paramTypes", c);	
			}
		}
	}


	protected AnnotationDescr getAnnotationDescr(PyDictionary ann) {
		Class klazz = getAttributeByNameAndType(ann, "annotationClass", Class.class);
		AnnotationDescr annotationDescr = new AnnotationDescr(klazz, new ConcurrentHashMap<String,Object>());
		PyDictionary arguments = getAttributeByNameAndType(ann, "arguments", PyDictionary.class);
		for(Object  key : arguments.keySet() ){
			annotationDescr.getFields().put(key.toString(), fromAnnotationParameterToJavaClass(klazz, key, arguments));	
		}
		return annotationDescr;
	}

	protected Object fromAnnotationParameterToJavaClass(Class klazz, Object key, PyDictionary arguments) {
		Object pyObject =  arguments.get(key);
		Class paramClassType = getAnnonationParameterType(klazz, key.toString());
		boolean paramExpectedIsArray = paramClassType.isArray();
		boolean paramIsArray = pyObject.getClass().isArray();
		Object result = null;
		if( ( pyObject instanceof PyObject ) == false  ){
			if(paramExpectedIsArray == false){
				return pyObject;	
			}
			result = pyObject;
			if(!paramIsArray){
				Object[] tmp = new Object[1];
				tmp[0] = result;
				result = tmp;
			}
		}
		else{
			result =  Py.tojava((PyObject) pyObject, paramClassType);	
		}
		
		//TODO: fix  org.python.compiler.ClassFile.visitAnnotation (AnnotationVisitor, String, Object)
		//needed to change arrays  into a List in order to bypass a limitation regarding array processing
		//in org.python.compiler.ClassFile.visitAnnotation(AnnotationVisitor, String, Object)
		
		if( result.getClass().isArray() ){
			List<Object> l = new ArrayList<Object>();
			for(Object o : (Object [])result){
				l.add(o);
			}
			result = l;
		}
		return result;
	}

	protected Class getAnnonationParameterType(Class klazz, String key) {
		for(Method m : klazz.getMethods()){
			if(m.getName().equalsIgnoreCase(key)){
				return m.getReturnType();
			}
		}
		return null;
	}

	protected<T> T getMemberAttributeByMemberNameAndAttrName(PyObject memberName, String attributeName,Class<T> type) {
		PyObject member = dict.__finditem__(memberName);
		if(member==null) return null;
		Object ret = member.__findattr__(new PyString(attributeName));
		return type.cast(ret);
	}
	
	
	protected<T> T getAttributeByNameAndType(PyDictionary dict, String attributeName, Class<T> type) {
		if(dict==null) return null;
		Object ret = dict.get(attributeName);
		return type.cast(ret);
	}

	
	protected void visitClassAnnotations() throws Exception {
		super.visitClassAnnotations();
		MemberDescription annMember = this.members.get(this.classAnnotationsMemberName);
		for(AnnotationDescr annotationDescr : annMember.getAnnotations()){
			this.classfile.addClassAnnotation(annotationDescr);	
		}
		
    }

	protected void visitMethods() throws Exception {
		super.visitMethods();
		for(String memberName : this.members.keySet()){
			
			MemberDescription memberDesc = this.members.get(memberName);
			switch(memberDesc.getType()){
				case METHOD:{
					addMethodAndAnnotations(memberDesc);
					break;
				}
				case FIELD_METHOD:{
					addFieldAndAnnotations(memberDesc);
					break;
				}
			}
		}
	}

	protected void addFieldAndAnnotations(MemberDescription fieldTypeMemberDesc) throws IOException {
		//remove the existing function allowing set/get calls to interact with the java field
		dict.__delitem__(fieldTypeMemberDesc.getPyName());
		ClassFileUtils.addField(this.classfile, 
				fieldTypeMemberDesc.getName(),//remove the initial "_" defined at the wrapper method  
				((Class<?>)fieldTypeMemberDesc.getExtraInfo().get("type")),//class type is stored at the "type" attribute of the map
				fieldTypeMemberDesc.getAccessType(),//PUBLIC is required by the engine in order to access the attribute content 
				fieldTypeMemberDesc.getAnnotations(),
				null,
				fieldTypeMemberDesc.getExtraInfo().get("value"));
		if(fieldTypeMemberDesc.getExtraInfo().get("addBeanGetterAndSetter").equals(Boolean.TRUE)){
			addBeanGetterAndSetter( fieldTypeMemberDesc);	
		}
		
	}

	protected void addBeanGetterAndSetter(MemberDescription fieldTypeMemberDesc) throws IOException {
		if(hasGetterAndSetterDefined(fieldTypeMemberDesc)){
			return;
		}
		addBeanSetter(fieldTypeMemberDesc);
		addBeanGetter(fieldTypeMemberDesc);
	}

	protected void addBeanGetter(MemberDescription fieldTypeMemberDesc) throws IOException {
		Class<?> beanType = ((Class<?>)fieldTypeMemberDesc.getExtraInfo().get("type"));
		String getterMethodName = "get" + getBeanNameWithFirstCapitalLetter(fieldTypeMemberDesc.getName());
        // getBean method
		Code code = classfile.addMethod(getterMethodName, makeSig(beanType), Modifier.PUBLIC);
        code.aload(0);
        code.getfield(classfile.name, fieldTypeMemberDesc.getName(), mapType(beanType));
        code.areturn();
	}

	protected void addBeanSetter(MemberDescription fieldTypeMemberDesc) throws IOException {
		Class<?> beanType = ((Class<?>)fieldTypeMemberDesc.getExtraInfo().get("type"));
		String setterMethodName = "set" + getBeanNameWithFirstCapitalLetter(fieldTypeMemberDesc.getName());
		// setBean method 
        Code code = classfile.addMethod(setterMethodName, makeSig(void.class, beanType), Modifier.PUBLIC);
        code.aload(0);
        code.aload(1);
        code.putfield(classfile.name, fieldTypeMemberDesc.getName(), mapType(beanType));
        code.return_();		
	}

	protected boolean hasGetterAndSetterDefined(MemberDescription fieldTypeMemberDesc) {
		String beanName = getBeanNameWithFirstCapitalLetter(fieldTypeMemberDesc.getName());
		if(members.containsKey("set"+beanName) || members.containsKey("get"+beanName)){
			return true;
		}
		return false;
	}

	protected String getBeanNameWithFirstCapitalLetter(String name) {
		return name.replaceFirst("^.", name.substring(0, 1).toUpperCase());
	}

	protected void addMethodAndAnnotations(MemberDescription memberDesc) throws Exception {
		String memberName = memberDesc.getName();
		Class<?> returnTypeClass = (Class<?>)memberDesc.getExtraInfo().get("returnType");
		if(returnTypeClass==Void.class){
			returnTypeClass = void.class;
		}
		super.addMethod( memberName,
				memberName,
	            returnTypeClass,
	            (Class[])memberDesc.getExtraInfo().get("paramTypes"),
	            new Class[0],
	            memberDesc.getAccessType(),//TODO: ABSTRACT IS REQUIRED BY THE SUPER.addMethod
	            null,
	            memberDesc.getAnnotations().toArray(new AnnotationDescr[0]),
	            extractMethodParametersAnnotations(memberDesc));
	}

	protected AnnotationDescr[][] extractMethodParametersAnnotations(MemberDescription memberDesc) {
		int argCount = dict.__finditem__(memberDesc.getPyName())
				.__getattr__("__code__")
				.__getattr__("co_argcount")
				.asInt();
		PyObject __defaults__ = dict.__finditem__(memberDesc.getPyName()).__getattr__("__defaults__");
		int defaultsCount = 0;
		argCount--;//the 'self' from  "def f(self, ... ):" does not count
		if(argCount==0){ 
			return new AnnotationDescr[0][];
		}
		
		if(__defaults__!=null && __defaults__ instanceof List ){
			defaultsCount = __defaults__.__len__();
		}
		
		List<AnnotationDescr[]> descs = new ArrayList<AnnotationDescr[]>();
		//add empty descriptors for parameters without annotations, 
		//i.e. without python default values (those must come first)
		for(int c=0; c<(argCount - defaultsCount);c++){
			descs.add(new AnnotationDescr[0]);
		}
		
		//process parameter annotations
		for(int c=argCount - defaultsCount; c<argCount;c++){
			Object methodParamAnnotations = ((List)__defaults__).get(c - (argCount - defaultsCount));
			if(methodParamAnnotations==null){
				continue;
			}
			descs.add(processMethodParameterAnnotations(methodParamAnnotations));
		}
		return descs.toArray(new AnnotationDescr[0][]);
	}

	protected AnnotationDescr[] processMethodParameterAnnotations(Object methodParamAnnotations) {
		List<AnnotationDescr> descs = new ArrayList<AnnotationDescr>();
		if(methodParamAnnotations instanceof PyList){
			for(PyObject ann: ((PyList)methodParamAnnotations).asIterable()){
				PyDictionary asDict = (PyDictionary)ann.__findattr__("asDict");
				if(asDict!=null){
					descs.add(getAnnotationDescr(asDict));
				}
			}
		}else if( methodParamAnnotations instanceof PyObject ){
			PyDictionary asDict  = null;
			try{
				asDict = (PyDictionary)(((PyObject)methodParamAnnotations).__findattr__("asDict"));
			}catch(Throwable t){}
			if(asDict!=null){
				descs.add(getAnnotationDescr(asDict));	
			}
		}
		return descs.toArray(new AnnotationDescr[0]);
	}
	
	@Override
	protected void visitConstructors() throws Exception {
		super.visitConstructors();
		
    }
	
}
