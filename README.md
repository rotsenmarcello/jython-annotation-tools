# Overview
Jython Annotation Tools is a JAVA-Library that enables the use of native Java annotations in Jython scripts and, additionally, offers some basic support for integration of Jython objects as beans in a SpringFramework context.

Annotations are managed through a decorator defined by the Jython class <b>annotations.A_</b> (imported as <b>"_"</b> or wrapped by <b>"annToDec()"</b>).

It works by storing  the selected annotation class and its optional attributes into a dictionary persisted as an attribute of the annotation owner's container. 
This information is then consumed by the proxy maker during the instrumentation phase to generate and load the class bytecode of the Java Proxy who, in turn, encapsulates the Jython class instance.

The decorator <b>"@_"</b> receives at least 1 parameter, informing the class of the annotation. This is a real java class which was imported (as can be seen in the examples). 
The second parameter, when no name is associated, will be stored in the attribute "value". All remaining parameters must be set by name.  
 
For example, the following Jython decorator code:
  ```python
     @_(Entity, name = 'Employee')
     @_(Qualifier, 'beanName')
 ```
 
 is equivalent to these Java annotation definitions:
  ```java
     @Entity(name = "Employee")
     @Qualifier("beanName")
 ```

Alternatively it is possible to import the Java annotation with the auxiliary function <b>annToDec()</b> from the module <b>annotations</b>.
This function will wrap the annotation and present it as decorator.

For example: 
```python
    from annotations import  annToDec
    
    RequestMapping=annToDec('org.springframework.web.bind.annotation.RequestMapping')
    JavaPublicMethod=annToDec('annotations.java.JavaPublicMethod')
    
    class ... :
    
        ...
    
        @JavaPublicMethod(returnType = String)
        @RequestMapping( ['/','/index'] )
        def index(self): return "Generated in Jython!"
        
        ...

```

A Jython class intending to make use of Java annotations must descend from a Java class or implement at least 1 Java interface.
The empty interface <b>annotations.java.JavaAware</b> can be used when no other interface or parent is required.

# Usage

1. Download the file jython-annotation-tools-0.9.0.jar from http://search.maven.org/#artifactdetails|net.wstech2|jython-annotation-tools|0.9.0|jar and put it in the java classpath of your application.

2. Download the python module annotations.py from https://github.com/rotsenmarcello/jython-annotation-tools/blob/master/jython-annotation-tools/src/main/resources/annotations.py and put it in the python syspath of your application.

3. In the script, import the auxiliary functions of the annotations module as follows:

  ```python
  from annotations import createProxyMaker
  from annotations import annToDec
  ```

4. For every annotation, "import" it with the auxiliary function annToDec

  ```python
  JavaClassAnnotations=annToDec('annotations.java.JavaClassAnnotations')
  ```

5. In order to use the annotation tools on a jython class, this class has to descend from a java class or interface. If no specific java class/interface is necessary, the interface <b>annotations.java.JavaAware</b> can the used.
  The class attributes <b>__proxymaker__</b> and <b>__module__</b> must be defined to identify, respectively, the function used to create the java class inside the VM and its package name.
  Also, an empty method decorated with <b>@JavaClassAnnotations</b> must be defined. This method is used to receive all annotations targeted at the class.

  ```python
  from annotations.java import JavaAware
  from annotations import createProxyMaker
  from annotations import annToDec
  ...
  
  JavaClassAnnotations=annToDec('annotations.java.JavaClassAnnotations')
  
  ...
  
  class AClass(JavaAware):
  
    ...
    
    __proxymaker__ = createProxyMaker
    
    __module__ = 'java.package.of.the.generated.class.in.the.vm'
    
    @JavaClassAnnotations
    def __classannotations__(self): pass
    
    ...
  ```

6. Consult the following sections and examples to obtain more information on the different types of annotations and how to use them in jython scripts.



# Compatibility between Java annotations and Jython decorators

Python/Jython decorators can be applied only to classes or methods, but not fields. Moreover, given the way Jython loads its classes as proxies, it is not possible to use class decorators to pass annotation informations, because when the decorator is processed the java proxy class is already in place. 
This leaves us only with method decorators to represent 4 different java annotation types (class, field, method, method parameter).

#Class Annotations

Since class decorators are not an option, we need an "empty method" to serve as 
the receptor for the annotations targeted at the Class.
This method can have any name as long as it is annotated with  <b>@JavaClassAnnotations. </b>
In our examples it is always called <b>"__classannotations__"</b> and defined as follows:

```python
    @JavaClassAnnotations
    def __classannotations__(self): pass
```

All class-level annotations are set on this empty method. The optional parameter <b>'exportClassBytecodeToFile'</b> is used to define whether or not (default) the ProxyMaker is to export the produced bytecode to a file at the directory indicated by the system property <b>"org.python.compiler.bytecodemonitor.exportdir"</b>.

This may be necessary for classes processed by frameworks whose annotation discovery process mechanisms require an actual file on the filesystem and in the java classpath. This is the case with Hibernate when used as a JPA provider, for example.
 
#Field Annotations
 
Fields requiring annotations have also to be declared as empty methods containing only a "pass" and decorated with specific annotations to indicate the "attribute type" on the Java side. 

In another words: we define the fields as methods and add the annotations to them through the decorator <b>"@_"</b> or with the respective annToDec-wrapped annotation. 

After consuming this configuration the Proxy Maker will remove the "empty method" from the Jython side and every further use of this attribute will access and see it as a field, since, at this point, it will already have been created (and annotated) via instrumentation on the java proxy class as such.  The "empty method" does not live beyond the class definition phase.

Fields are defined with one of the following annotations:

<b>@JavaBeanAttribute( Class<?> type )</b>: indicates a "Bean field". It takes only one parameter containing the java class type of the Bean. 
No need to write and annotate getters and setters for @_(JavaBeanAttribute, ...) attributes, they will be automatically generated as Java methods by the Proxy Maker.

<b>@JavaAttribute( String value, Class<?> type, int access )</b>: used for static attributes. <b>"VALUE"</b> specifies the initial value of the field,  <b>"TYPE"</b>  the Java Class and <b>"ACCESS"</b> the access mode based on <b>"java.lang.reflect.Modifier.*"</b>.


For example, the following Java declaration: 
```java
    @Id    
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Integer uid;
    
    public static final long serialVersionUID = 3L;
```
is represented in Jython as: 

```python

    @JavaBeanAttribute(Integer)
    @Id    
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    def uid(self) : pass
    
    @JavaAttribute(value = Long(3) , type=Long.TYPE, access= Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL  )
    def serialVersionUID(self): pass
```

# Method Annotations

In order to have its annotations processed, or just to be exposed to the Java ClassLoader, a method must have been annotated with <b>@JavaPublicMethod( returnType = ... , paramTypes = [ .... ] )</b>. 

<b>returnType</b>: indicates the class of the value returned by the method or java.lang.Void if none.

<b>paramTypes</b>: is a list containing the classes of the method's parameters. If there are 
no parameters, this attributed can be disregarded and the method annotated as just: 
```java
    @JavaPublicMethod( returnType = ... ).
```

For example, the Java definitions:
```java
    public void setData(String name, Integer age){
        this.name = name;
        this.age = age;
    }
    
    @RequestMapping( {"/","/index"} )
    public String index(){
        return "Generated in Jython!";
    }
```

are written in Jython as:
```python
    @JavaPublicMethod( returnType = Void, paramTypes = [String, Integer] )
    def setData(self, name, age): 
        self.name = name
        self.age = age

    @JavaPublicMethod( returnType = String )
    @RequestMapping( ['/','/index'] )
    def index(self): return "Generated in Jython!"
```

# Method Parameter Annotations

It is also possible to add annotations to method parameters. In this case, however, they are not passed as decorators (with <b>"@"</b>) but as the default value of the parameter. 
The proxy maker will identify this configuration and process the "default value" as a Annotation.

Example:
```python
    JavaPublicMethod=annToDec('annotations.java.JavaPublicMethod')
    RequestMapping=annToDec('org.springframework.web.bind.annotation.RequestMapping')
    PathVariable=annToDec('org.springframework.web.bind.annotation.PathVariable')

    @JavaPublicMethod(returnType = String, paramTypes = [Model, String, String])
    @RequestMapping( '/get/{id}/{section}' )
    def get(self, model, id = PathVariable('id'), section = PathVariable('section')):
        model.put("key", "value") 
        print "model[%s]/id=[%s]/section=[%s]" % (model,id,section)
        return "Asked for Id/Section [%s][%s]." % ( id,section)
```

# Running the examples

In order to run the examples it is required to define 2 system properties:

<b>org.python.compiler.bytecodemonitor.exportdir</b>: full path of the directory where are to be saved the .class files containing the generated bytecode of the classes marked with <b>"@JavaClassAnnotations(exportClassBytecodeToFile = True)"</b>. This directory must be appended to the system classpath and erased before every new start of the application, i.e., the java process.

<b>org.python.compiler.syspath.append</b>: a list of the directories containg the python modules (dirs and the .py files), separated by the system's native path  separator (';' on Windows, ':' on Linux/Unix).

If executing  from an Eclipse "Run Configuration", the "VM Arguments" of the "Arguments" tab should be filled with the following text:

```shell
-Dorg.python.compiler.bytecodemonitor.exportdir="${project_loc:/jython-annotation-tools}/target/classes/"
-Dorg.python.compiler.syspath.append="${project_loc:/jython-annotation-tools}/src/main/resources/;${project_loc:/jython-annotation-tools}/src/test/resources/;${project_loc:/jython-annotation-tools-spring}/src/main/resources/;${project_loc:/jython-annotation-tools-spring}/src/test/resources/;${project_loc:/jython-annotation-tools-samples}/src/main/resources/;${project_loc:/jython-annotation-tools-samples}/src/test/resources/"
```

# Example with JPA/Hibernate Annotations

```python

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
```

# Example with Spring/SpringBoot Annotations

```python
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
    '''REQUIRED -> Defines the method who creates the Java class responsible for generating
         the Java Proxy encapsulating this Jython class. '''
    
    
    __module__ = 'net.wstech2.jython.tests.springwebtests'
    '''REQUIRED -> Defines the string to be appended to the prefix "org.python.proxies"
    to compose the Java "Full classname" of the Proxy Class encapsulating this Jython 
    class.
    In this case, for example, the proxy class will be defined as:
    org.python.proxies.net.wstech2.jython.tests.springwebtests.WebHandler'''


    @JavaClassAnnotations
    @RestController
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
    __module__ = 'net.wstech2.jython.tests.springwebtests'
    
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
```



