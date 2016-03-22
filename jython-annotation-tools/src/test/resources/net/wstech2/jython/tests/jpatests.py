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


'''

Jython Annotation Tools is a library that enables the use of native Java annotations 
in Jython scripts and, additionally, offers some basic support for integration of 
Jython objects as beans in a SpringFramework context.

'''
'''

Annotations are managed through a decorator defined by the Jython class 
annotations.A_ (imported as "_" or wrapped by "annToDec()").

It works by storing the selected annotation class and its optional attributes into
a dictionary persisted as an attribute of the annotation owner's container. 
This information is then consumed by the proxy maker during the instrumentation phase
to generate and load the class bytecode of the Java Proxy who, in turn, encapsulates the
Jython class instance.

The decorator "@_" receives at least 1 parameter, informing the class of the annotation. 
This is a real java class which was imported (as can be seen in the examples). 
The second parameter, when no name is associated, will be stored at the attribute "value". 
All remaining parameters must be set by name.  
 
For example, the following Jython decorator code:
 
     @_(Entity, name = 'Employee')
     @_(Qualifier, 'beanName')
 
 is equivalent to these Java annotation definitions:
 
     @Entity(name = "Employee")
     @Qualifier("beanName")
 
'''
'''

Alternatively it is possible to import the Java annotation with the 
auxiliary function annToDec() from the module annotations.
This function will wrap the annotation and present it as decorator.

For example: 

    from annotations import  annToDec
    
    RequestMapping=annToDec('org.springframework.web.bind.annotation.RequestMapping')
    JavaPublicMethod=annToDec('annotations.java.JavaPublicMethod')
    
    class .... :
    
        ....
    
        @JavaPublicMethod(returnType = String)
        @RequestMapping( ['/','/index'] )
        def index(self): return "Generated in Jython!"
        
        ....

'''
'''

A Jython class intending to make use of Java annotations must descend from a Java class 
or implement at least 1 Java interface.
The empty interface annotations.java.JavaAware can be used when 
no other interface or parent is required.
 
'''
'''

Python/Jython decorators can be applied only to classes or methods, but not fields. 
Moreover, given the way Jython loads its classes as proxies, it is not possible to use 
class decorators to pass annotation informations, because when the decorator is processed 
the java proxy class is already in place. 
This leaves us only with method decorators to represent 4 different java annotation 
types (class, field, method, method parameter).
In order to do so we make use of "dead or empty methods" containing only a "pass" 
and decorated with specific annotations to indicate the attribute "type" on the Java side. 

In another words: we define the fields as methods and add the annotations to them 
through the decorator "@_" or with the respective annToDec-wrapped annotation. 

After consuming this configuration the Proxy Maker will remove the "empty method" from
the Jython side and every further use of this attribute will access and see it as 
a field, since, at this point, it will already have been created (and annotated)
via instrumentation on the java proxy class as such.   

'''
'''

Since class decorators are not an option, we need an "empty method" to serve as 
the receptor for the annotations targeted at the Class.
This method can have any name as long as it is annotated with  @JavaClassAnnotations. 
In our examples it is always called "__classannotations__" and defined as follows:

    @JavaClassAnnotations
    def __classannotations__(self): pass
    
'''
'''

Finally, fields requiring annotations have also to be declared as empty methods. 
As mentioned above, these empty methods will be replaced by actual fields once the
Proxy Maker has concluded the instrumentation process. Any use of the fields must take 
into consideration their java values and types only. The "empty method" does not live 
beyond the class definition phase.

Fields are defined with the decorator "@JavaBeanAttribute( TYPE )" with "TYPE" 
indicating the Java Class of the field.

For example, the following Java declaration: 
    
    @Id    
    @GeneratedValue( strategy = GenerationType.IDENTITY )    
    private Integer uid;
    
is represented in Jython as: 
    
    @JavaBeanAttribute(Integer)
    @Id    
    @GeneratedValue( strategy = GenerationType.IDENTITY )    
    def uid(self) : pass

'''
'''

Method Annotations.
 
In order to have its annotations processed, or just to be exposed to the Java ClassLoader,
a method must have been annotated with 
@JavaPublicMethod( returnType = ... , paramTypes = [ .... ] ). 

returnType: indicates the class of the value returned by the method or java.lang.Void if none.

paramTypes: is a list containing the classes of the method's parameters. If there are 
no parameters, this attributed can be disregarded and the method annotated as just: 

    @JavaPublicMethod( returnType = ... ).


For example, the Java definitions:

    public void setData(String name, Integer age){
        this.name = name;
        this.age = age;
    }
    
    @RequestMapping( {'/','/index'} )
    public String index(){
        return "Generated in Jython!";
    }

are written in Jython as:

    @JavaPublicMethod( returnType = Void, paramTypes = [String, Integer] )
    def setData(self, name, age): 
        self.name = name
        self.age = age

    @JavaPublicMethod( returnType = String )
    @RequestMapping( ['/','/index'] )
    def index(self): return "Generated in Jython!"

'''
'''

Method Parameter Annotations

It is also possible to add annotations to method parameters. In this case, however, 
they are not passed as decorators ("with @") but as the default value of the parameter. 
The proxy maker will identify this configuration and process the "default value" as 
a Annotation.


Example:

    JavaPublicMethod=annToDec('annotations.java.JavaPublicMethod')
    RequestMapping=annToDec('org.springframework.web.bind.annotation.RequestMapping')
    PathVariable=annToDec('org.springframework.web.bind.annotation.PathVariable')

    @JavaPublicMethod(returnType = String, paramTypes = [Model, String, String])
    @RequestMapping( '/get/{id}/{section}' )
    def get(self, model, id = PathVariable('id'), section = PathVariable('section')):
        model.put("key", "value") 
        print "model[%s]/id=[%s]/section=[%s]" % (model,id,section)
        return "Asked for Id/Section [%s][%s]." % ( id,section)

'''
 
from java.lang import Class
from java.lang import Integer
from java.lang import Long
from java.lang import String
from java.lang import Void
from java.lang.reflect import Modifier 
from java.util import Date
from java.util import List
from java.io import Serializable


from annotations.java import JavaAware
from javax.persistence import EntityManager
from javax.persistence import EntityManagerFactory
from javax.persistence import Persistence

from annotations import createProxyMaker
from annotations import annToDec
from javax.persistence import GenerationType
from javax.persistence import TemporalType

Column=annToDec('javax.persistence.Column')
Entity=annToDec('javax.persistence.Entity')
Id=annToDec('javax.persistence.Id')
Table=annToDec('javax.persistence.Table')
Temporal=annToDec('javax.persistence.Temporal')
GeneratedValue=annToDec('javax.persistence.GeneratedValue')

JavaClassAnnotations=annToDec('annotations.java.JavaClassAnnotations')
JavaPublicMethod=annToDec('annotations.java.JavaPublicMethod')
JavaBeanAttribute=annToDec('annotations.java.JavaBeanAttribute')
JavaAttribute=annToDec('annotations.java.JavaAttribute')


class Employee(JavaAware, Serializable):
    
     
    __proxymaker__ = createProxyMaker
    '''REQUIRED -> Defines the method who creates the Java class responsible for generating
         the Java Proxy encapsulating this Jython class. '''
    
    
    __module__ = 'net.wstech2.jython.tests.jpatests'
    '''REQUIRED -> Defines the string to be appended to the prefix "org.python.proxies"
    to compose the Java "Full classname" of the Proxy Class encapsulating this Jython 
    class.
    In this case, for example, the proxy class will be defined as:
    org.python.proxies.net.wstech2.jython.tests.jpatests.Employee
    ''' 
    
   
    @JavaClassAnnotations(exportClassBytecodeToFile = True)
    @Entity(name = "Employee")
    @Table(name = "EMPLOYEES")
    def __classannotations__(self): pass
    '''REQUIRED -> JavaClassAnnotations indicates the jython attribute representing 
    the "CLASS". 
    All class-level annotations are set on this empty method.
    The optional parameter 'exportClassBytecodeToFile' is used to define whether or 
    not (default) the ProxyaAker is to export the produced bytecode to a file at the directory 
    indicated by the system property "org.python.compiler.bytecodemonitor.exportdir".
    This may be necessary for classes processed by frameworks whose annotation discovery
    process mechanism requires an actual file on the filesystem and in the java classpath. 
    This is the case with Hibernate when used as a JPA provider, for example.''' 
    
   
    @JavaBeanAttribute(Integer)
    @Id    
    @GeneratedValue(strategy = GenerationType.IDENTITY)    
    def uid(self) : pass
    '''JavaBeanAttribute indicates a "Bean field". It takes only one parameter containing 
    the java class type of the Bean. 
    No need to write and annotate getters and setters for @_(JavaBeanAttribute, ...)
    attributes, they will be automatically generated as Java methods 
    by the Proxy Maker.''' 
        
        
    @JavaBeanAttribute(String)
    def name(self):pass
    
    
    @JavaBeanAttribute(Date)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="EV_DATE" )
    def date(self): pass
    
    
    @JavaAttribute(value = Long(3) , type=Long.TYPE, access= Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL  )
    def serialVersionUID(self): pass
    
    
    
    #no need to create getters and setters for @_(JavaBeanAttribute, ...) fields. 
    #THEY WILL BE GENERATED AUTOMATICALLY!    
    #     @_(JavaPublicMethod, returnType = Void, paramTypes = [Date] )
    #     def setDate(self, date): self.date = date
    #      
    #     @_(JavaPublicMethod, returnType = Date )
    #     def getDate(self): return self.date
    #      
    #     @_(JavaPublicMethod, returnType = Void, paramTypes = [String] )
    #     def setName(self, name): self.name = name
    #      
    #     @_(JavaPublicMethod, returnType = String )
    #     def getName(self): return self.name
    # #     
    #     @_(JavaPublicMethod, returnType = Void, paramTypes = [Integer] )
    #     def setUid(self, uid): self.uid = uid
    #        
    #     @_(JavaPublicMethod, returnType = Integer )
    #     def getUid(self): return self.uid
    
    
    
class Test():
    
    entityManagerFactory = None
    
    def run(self):    
        self.setup()
        self.insert()
        self.display()
        self.shutdown()


    def shutdown(self):
        self.entityManagerFactory.close()

    def display(self):
        entityManager = self.entityManagerFactory.createEntityManager()
        print "entityManager => ",entityManager
        for e in entityManager.createQuery(" from Employee" , Employee).getResultList():
            # the getters and setters used in the next line have been 
            # created automatically for every attribute 
            # annotated with @_(JavaBeanAttribute, ...)
            print "Employee[uid][name][admission]=[%d][%s][%s]",e.getUid(), e.getName(),e.getDate()

    def insert(self):
        entityManager = self.entityManagerFactory.createEntityManager()
        print "entityManager => ",entityManager
        entityManager.getTransaction().begin()
        
        e = Employee()
        e.setName('employee of the month')
        entityManager.persist(e)
            
        for i in range(50):
            e = Employee()
            # the getters and setters used in the next line have been 
            # created automatically for every attribute 
            # annotated with @_(JavaBeanAttribute, ...) 
            e.setName('employee number ' + str(i))
            e.setDate(Date())
            entityManager.persist(e)
        
        entityManager.getTransaction().commit()
        entityManager.close()
        
    def setup(self):
        self.entityManagerFactory = Persistence.createEntityManagerFactory("net.wstech2.jython.tests.jpa")
        print "entityManagerFactory => ",self.entityManagerFactory 

#############
##
## Running 
##


def runTest():
    test = Test()
    test.run()

if __name__ == '__builtin__' or __name__ == '__main__':
    runTest()

