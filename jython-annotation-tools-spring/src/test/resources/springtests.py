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
 
import datetime

from java.lang import Class
from java.lang import Integer
from java.lang import String
from java.lang import Deprecated
from java.lang import Void
from java.util import Date
from java.util import List

from annotations.java import JavaAware

from annotations import createProxyMaker
from annotations import A_ as _


from annotations.java import JavaClassAnnotations
from annotations.java import JavaPublicMethod
from annotations.java import JavaBeanAttribute

from net.wstech2.jython.tests.java.spring import Producer

from org.springframework.context.annotation import AnnotationConfigApplicationContext
from org.springframework.beans.factory.annotation import Autowired
from org.springframework.beans.factory.annotation import Qualifier
from org.springframework.context  import  ConfigurableApplicationContext
from org.springframework.context.annotation  import Bean
from org.springframework.context.annotation import Configuration
from org.springframework.stereotype import Service
from datetime import datetime

class DataProducer(Producer):
    
    __proxymaker__ = createProxyMaker
    __module__ = 'springtests'

    @_(JavaClassAnnotations)
    @_(Service)
    def __classannotations__(self): pass
    
    def getResponse(self): return "[DataProducer]Test executed successfully!"


class DataConsumer(JavaAware):
    
    __proxymaker__ = createProxyMaker
    __module__ = 'springtests'

    @_(JavaClassAnnotations)
    @_(Service)
    def __classannotations__(self): pass
 
    
    @_(JavaBeanAttribute, DataProducer)
    @_(Autowired)
    def dataProducer(self): pass
    
    
    @_(JavaPublicMethod, returnType = Void)
    def printResponse(self):
        print '. [] JythonDataConsumer [] .' 
        print self.dataProducer.getResponse()
        
    
class Test(JavaAware):
    
    __proxymaker__ = createProxyMaker
    __module__ = 'springtests'
    
    @_(JavaClassAnnotations)
    @_(Configuration)
    def __classannotations__(self): pass
    
    @_(Bean)
    @_(JavaPublicMethod, returnType = DataProducer)
    def getDataProducer(self): return DataProducer()
    
    @_(Bean)
    @_(JavaPublicMethod, returnType = DataConsumer)
    def getDataConsumer(self): return DataConsumer()
    


#############
##
## Running 
##

def runTest():
    app = AnnotationConfigApplicationContext()
    app.register(Test)
    app.refresh()
    dc = app.getBean(DataConsumer)
    dc.printResponse()

if __name__ == '__builtin__' or __name__ == '__main__':
    runTest()
