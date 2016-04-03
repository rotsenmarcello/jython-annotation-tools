package net.wstech2.jython.tests.java;

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
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.web.client.RestTemplate;

public class SampleTests {
	
	@Rule
	public OutputCapture capture = new OutputCapture();
	
	RestTemplate template = new TestRestTemplate();
	
	protected ScriptEngine engine = null;

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
		engine.eval("import annotations");
		engine.eval("from springwebtests import runTest");
		engine.eval("runTest()");
		String body = template.getForEntity("http://localhost:8080/get/1/section", String.class).getBody();
		assertThat(body, containsString("Asked for Id/Section [1][section]."));
	}
	
}



