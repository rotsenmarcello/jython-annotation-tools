package net.wstech2.jython.tests.java.spring;

public class JavaDataConsumer {
	
	private Producer producer;

	public Producer getProducer() {
		return producer;
	}

	public void setProducer(Producer producer) {
		this.producer = producer;
	}
	
	public void printResponse(){
		System.out.println(". [] JavaDataConsumer [] .");
		System.out.println(producer.getResponse());
	}

}
