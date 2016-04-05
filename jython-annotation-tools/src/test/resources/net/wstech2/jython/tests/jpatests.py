 
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

