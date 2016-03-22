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
from java.lang import Class
from java.lang import Integer
from java.lang import String
from java.lang import Deprecated
from java.lang import Void
from java.util import Date
from java.util import List

from annotations.java import JavaAware
from org.springframework.boot import SpringApplication
from org.springframework.ui import Model

from annotations import  annToDec
from annotations import createProxyMaker


###Define Decorators based on Java annotations
JavaClassAnnotations=annToDec('annotations.java.JavaClassAnnotations')
JavaPublicMethod=annToDec('annotations.java.JavaPublicMethod')
JavaBeanAttribute=annToDec('annotations.java.JavaBeanAttribute')
Autowired=annToDec('org.springframework.beans.factory.annotation.Autowired')
Qualifier=annToDec('org.springframework.beans.factory.annotation.Qualifier')
Bean=annToDec('org.springframework.context.annotation.Bean')
Configuration=annToDec('org.springframework.context.annotation.Configuration')
RestController=annToDec('org.springframework.web.bind.annotation.RestController')
SpringBootApplication=annToDec('org.springframework.boot.autoconfigure.SpringBootApplication')
RequestMapping=annToDec('org.springframework.web.bind.annotation.RequestMapping')
PathVariable=annToDec('org.springframework.web.bind.annotation.PathVariable')
###End decorator definitions

class WebHandler(JavaAware):
    
    __proxymaker__ = createProxyMaker
    __module__ = 'springwebtests'

    @JavaClassAnnotations
    @RestController
    def __classannotations__(self): pass
    
    @JavaPublicMethod(returnType = String)
    @RequestMapping( ['/','/index'] )
    def index(self): return "Generated in Jython!"
    
    @JavaPublicMethod(returnType = String, paramTypes = [Model, String, String])
    @RequestMapping( '/get/{id}/{section}' )
    def get(self, model, id = PathVariable('id'), section = PathVariable('section')):
        model.put("akey", "avalue") 
        print "model[%s]/id=[%s]/section=[%s]" % (model,id,section)
        return "Asked for Id/Section [%s][%s]." % ( id,section)
    
class Test(JavaAware):
    
    __proxymaker__ = createProxyMaker
    __module__ = 'springwebtests'
    
    @JavaClassAnnotations
    @SpringBootApplication
    @Configuration
    def __classannotations__(self): pass
    
    @Bean
    @JavaPublicMethod(returnType = WebHandler)
    def getWebHandler(self): return WebHandler()
    


#############
##
## Running 
##

def runTest():
    app = SpringApplication([Test])
    app.run()

if __name__ == '__builtin__' or __name__ == '__main__':
    runTest()


