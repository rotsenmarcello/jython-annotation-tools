'''
 * 
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
 *
 '''

import types
from java.lang import Class

from net.wstech2.jython.annotationtools.java import ProxyMakerWithAnnotations

class A_:
 
    annotationClass = None
    arguments = None
    value = None
    asDict = None
 
    def __init__(self,annotationClass,value = None, *args, **kwargs):
        self.annotationClass = annotationClass
        self.arguments = kwargs
        if value is not None:
            self.arguments["value"] = value
        self.asDict = { 'annotationClass' : self.annotationClass, 'arguments' : self.arguments }   
         
     
    def __call__(self, func, *args, **kwargs):
        if hasattr(func,'__proxy_config') is False:
            setattr(func,"__proxy_config", { 'annotations' : list() })            
        
        __proxy_config = getattr(func,"__proxy_config")    
        __proxy_config['annotations'].append({ 'annotationClass' : self.annotationClass, 'arguments' : self.arguments })
        return func
        
def annToDec(annotationFullClassName):
    
    sep = annotationFullClassName.rfind('.');
    annotationClassName=annotationFullClassName[sep+1:];
    moduleName=annotationFullClassName[:sep];
    annotationClass=Class.forName(annotationFullClassName)
    
    def wrappedA_( *args, **kwargs):
        first = None
        if len(args) == 1:
            first = args[0] 
        if  type(first) == types.FunctionType: #meaning an annotation without parameters
            instance=A_(annotationClass)
            instance.__call__(first)
            return first
        else:
            
            return A_(annotationClass, *args, **kwargs)
        
    return wrappedA_    

def createProxyMaker(superclass, interfaces, className, pythonModuleName, fullProxyName, dict):
    fn = fullProxyName
    fullProxyName=fn[0:fn.rindex('$')].replace('$','.')
    return ProxyMakerWithAnnotations(superclass, interfaces, className, pythonModuleName, fullProxyName, dict)

