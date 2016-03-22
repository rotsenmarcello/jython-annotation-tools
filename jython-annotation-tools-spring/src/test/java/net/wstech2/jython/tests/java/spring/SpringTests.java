package net.wstech2.jython.tests.java.spring;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.springframework.boot.test.OutputCapture;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SpringTests  {
	
	@Rule
	public OutputCapture capture = new OutputCapture();
	
	protected ScriptEngine engine = null;

	@Before
	public void initScriptEngineAndSetSysPath() throws ScriptException {
		engine = new ScriptEngineManager().getEngineByName("python");
		engine.eval("import sys");
		for (String syspath : System.getProperty("org.python.compiler.syspath.append").split(File.pathSeparator)) {
			System.out.println(String.format("Adding [%s] to sys.path", syspath));
			engine.eval(String.format("sys.path.append('%s')", syspath.replace('\\', '/')));
		}
	}
	
	@Test
	public void testSpringSimple() throws ScriptException {
		engine.eval("import annotations");
		engine.eval("from springtests import runTest");
		engine.eval("runTest()");
		assertThat(capture.toString(), containsString("[DataProducer]Test executed successfully!"));
	}
	
	@Test
	public void testSpringFactory() throws ScriptException {
		ApplicationContext context =
			    new ClassPathXmlApplicationContext(new String[] {"spring-context-config.xml"});
		JavaDataConsumer dc = context.getBean("JavaDataConsumer", JavaDataConsumer.class);
		dc.printResponse();
		assertThat(capture.toString(), containsString("[DataProducer]Test executed successfully!"));
	}
}



