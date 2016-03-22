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
from java.lang import Float
from java.lang import String
from java.lang import Deprecated
from java.util import Map
from java.util import List

from annotations.java import JavaAwareTests
from annotations.java import JavaAware
from annotations.java import JavaClassAnnotations
from net.wstech2.jython.tests.java import SampleAnnotation
from annotations.java import JavaPublicMethod
from annotations.java import JavaBeanAttribute

from annotations import createProxyMaker
from annotations import A_ as _
from annotations import A_


class Test(JavaAware):
    
    __proxymaker__ = createProxyMaker
    __module__ = 'net.wstech2.jython.tests.devtests'


    @_(JavaClassAnnotations)
    @_(SampleAnnotation, value = 'SampleAnnotation Value')
    def __classannotations__(self): pass
    
    @_(JavaBeanAttribute, Float)    
    @_(SampleAnnotation, 'a sample annotation on attribute i')
    def i( self ): pass
   
    def setI(self, i): self.i = i
    def getI(self): return self.i
    
    
    
    @_(JavaPublicMethod, returnType = Float, paramTypes = (String, String, Map) )
    def persistData(self, 
                    strA , 
                    strB = [A_(SampleAnnotation, 'Annotation strB1'), 
                            A_(SampleAnnotation, 'Annotation strB2')],
                    map  = A_(SampleAnnotation,  'Annotation map')):
        jj=65
        ff=99
        JavaAwareMain.printDetails(self._getPyInstance())
        
    
#############
##
## Running 
##

def runTest():
    print  Test.persistData.__func__.__defaults__
    print dir(Test.persistData.__func__.func_code)
    print Test.persistData.__func__.func_code.co_varnames    
    
    test = Test()
    for m in test.getClass().getDeclaredMethods():
        print m
        print "\t",m.getParameterAnnotations()
     
    
    print 'test.i [',test.i 
    test.setI(11.0)
    print 'test.i [',test.i
    
    print '##############@@'
    for f2 in test.getClass().getFields():
        print f2.get(test)
        print ">>\t",f2.getDeclaredAnnotations()
    test.getClass().newInstance()

    
    
if __name__ == '__builtin__' or __name__ == '__main__':
    runTest()