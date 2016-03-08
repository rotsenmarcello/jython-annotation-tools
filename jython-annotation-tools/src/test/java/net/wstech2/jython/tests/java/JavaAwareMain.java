package net.wstech2.jython.tests.java;

import java.io.File;
import java.nio.charset.Charset;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.springframework.boot.test.OutputCapture;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.web.client.RestTemplate;

public class JavaAwareMain {
	
	@Rule
	public OutputCapture capture = new OutputCapture();
	
	RestTemplate template = new TestRestTemplate();
	
	ScriptEngine engine = null;

	@Before
	public void initScriptEngineAndSetSysPath() throws ScriptException {
		engine = new ScriptEngineManager().getEngineByName("python");
		engine.eval("import sys");

		for (String syspath : System.getProperty("org.python.compiler.syspath.append").split(File.pathSeparator)) {
			engine.eval(String.format("sys.path.append('%s')", syspath.replace('\\', '/')));
		}
	}

	@Test
	public void testSpringBootWithWeb() throws ScriptException {
		engine.eval("import net.wstech2.jython.annotationtools");
		engine.eval("from net.wstech2.jython.tests.springwebtests import runTest");
		engine.eval("runTest()");
		String body = template.getForEntity("http://localhost:8080/get/1/section", String.class).getBody();
		assertThat(body, containsString("Asked for Id/Section [1][section]."));
	}
	
	@Test
	public void testSpringSimple() throws ScriptException {
		engine.eval("import net.wstech2.jython.annotationtools");
		engine.eval("from net.wstech2.jython.tests.springtests import runTest");
		engine.eval("runTest()");
		assertThat(capture.toString(), containsString("[DataProducer]Test executed successfully!"));
	}
	
	@Test
	public void testJPAHibernate() throws ScriptException {
		engine.eval("import net.wstech2.jython.annotationtools");
		engine.eval("from net.wstech2.jython.tests.jpatests import runTest");
		engine.eval("runTest()");
	}
	

	public static void main(String args[]) {
		try {
			new JavaAwareMain().run(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(String args[]) throws Exception {

		ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");

		// Using the eval() method on the engine causes a direct
		// interpretataion and execution of the code string passed into it
		engine.eval("import sys");
		engine.eval(String.format("sys.path.append('%s\\src\\main\\resources')", args[0]));
		engine.eval(String.format("sys.path.append('%s\\src\\test\\resources')", args[0]));
		engine.eval(
				FileUtils
						.readFileToString(
								new
								// File(String.format("%s\\%s\\devtests.py",args[0],"src\\test\\resources\\net\\wstech2\\jython\\tests"))
								 File(String.format("%s\\%s\\jpatests.py",args[0],"src\\test\\resources\\net\\wstech2\\jython\\tests"))
								// File(String.format("%s\\%s\\springtests.py",args[0],"src\\test\\resources\\net\\wstech2\\jython\\tests"))
								//File(String.format("%s\\%s\\springwebtests.py", args[0],"src\\test\\resources\\net\\wstech2\\jython\\tests"))
						,Charset.forName("UTF-8")));
	}

	
	
}



