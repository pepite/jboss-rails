package org.jboss.ruby.enterprise.endpoints;

import java.net.URL;
import java.util.Collections;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.invoker.Invoker;
import org.jboss.logging.Logger;
import org.jboss.ruby.enterprise.endpoints.cxf.RubyEndpointInvoker;
import org.jboss.ruby.enterprise.endpoints.cxf.RubyReflectionServiceFactoryBean;
import org.jboss.ruby.enterprise.endpoints.cxf.RubyServiceConfiguration;
import org.jboss.ruby.enterprise.endpoints.databinding.RubyDataBinding;
import org.jboss.ruby.runtime.RubyRuntimePool;

/** The bean within MC representing a deployed Ruby WebService.
 * 
 * @author Bob McWhirter 
 */
public class RubyEndpoint {
	
	private static final Logger log = Logger.getLogger( RubyEndpoint.class );

	private RubyRuntimePool runtimePool;
	private Bus bus;
	private Server server;

	//private String dir;
	private URL wsdlLocation;
	
	private String classLocation;
	private String endpointClassName;
	
	private String targetNamespace;
	private String portName;
	
	private String address;
	
	public RubyEndpoint() {
		
	}
	
	public void setBus(Bus bus) {
		this.bus = bus;
	}
	
	public Bus getBus() {
		return this.bus;
	}
	
	public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
		this.runtimePool = runtimePool;
	}
	
	public RubyRuntimePool getRubyRuntimePool() {
		return this.runtimePool;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public void setClassLocation(String classLocation) {
		this.classLocation = classLocation;
	}
	
	public String getClassLocation() {
		return this.classLocation;
	}
	
	public void setEndpointClassName(String endpointClassName) {
		this.endpointClassName = endpointClassName;
	}
	
	public String getEndpointClassName() {
		return this.endpointClassName;
	}
	
	public void setWsdlLocation(URL wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}
	
	public URL getWsdlLocation() {
		return this.wsdlLocation;
	}
	
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}
	
	public String getTargetNamespace() {
		return this.targetNamespace;
	}
	
	public void setPortName(String portName) {
		this.portName = portName;
	}
	
	public String getPortName() {
		return this.portName;
	}
	
	public void start() {
		log.info( "start()" );
		AbstractServiceConfiguration serviceConfig = new RubyServiceConfiguration( getPortName() );
		ReflectionServiceFactoryBean serviceFactory = new RubyReflectionServiceFactoryBean();
		serviceFactory.setServiceConfigurations( Collections.singletonList( serviceConfig ) );
		
		ServerFactoryBean serverFactory = new ServerFactoryBean();
		serverFactory.setStart( false );
        serverFactory.setBus( bus );
		serverFactory.setServiceFactory( serviceFactory );
		
		RubyDataBinding dataBinding = new RubyDataBinding( this.runtimePool );
		serviceFactory.setDataBinding( dataBinding );
		
		RubyEndpointHandler serviceBean = createServiceBean( dataBinding );
		serverFactory.setServiceName( new QName( getTargetNamespace(), getPortName() ) );
		serverFactory.setEndpointName( new QName( getTargetNamespace(), getPortName() ) );
		serverFactory.setServiceClass( RubyEndpointHandler.class );
		serverFactory.setInvoker( createInvoker( serviceBean ) );
		
		serverFactory.setAddress( getAddress() );
		serverFactory.setWsdlURL( getWsdlLocation().toExternalForm() );
		
		
		SoapBindingFactory bindingFactory = new SoapBindingFactory();
		serverFactory.setBindingFactory( bindingFactory );
		
		this.server = serverFactory.create();
		
		log.info( "RUBY CLASS DEFS: " + dataBinding.getRubyClassDefinitions() );
		
		this.server.start();
	}
	

	private RubyEndpointHandler createServiceBean(RubyDataBinding dataBinding) {
		return new RubyEndpointHandler( this.runtimePool, this.classLocation, this.endpointClassName, dataBinding );
	}
	
	private Invoker createInvoker(RubyEndpointHandler handler) {
		return new RubyEndpointInvoker( handler );
	}

	public void stop() {
		log.info( "stop()" );
		this.server.stop();
	}
}