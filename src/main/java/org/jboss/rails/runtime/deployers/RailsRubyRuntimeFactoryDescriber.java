package org.jboss.rails.runtime.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.rails.core.metadata.RailsApplicationMetaData;
import org.jboss.ruby.runtime.metadata.RubyRuntimeMetaData;

public class RailsRubyRuntimeFactoryDescriber extends AbstractDeployer {
	
	public RailsRubyRuntimeFactoryDescriber() {
		setStage( DeploymentStages.PRE_DESCRIBE );
		addInput( RailsApplicationMetaData.class );
		addOutput( RubyRuntimeMetaData.class );
	}

	public void deploy(DeploymentUnit unit) throws DeploymentException {
		log.debug( "attempt deploy against: " + unit );
		RailsApplicationMetaData railsMetaData = unit.getAttachment( RailsApplicationMetaData.class );
		if ( railsMetaData == null ) {
			log.debug( "no RailsApplicationMetaData attached" );
			return;
		}
		log.debug( "actually deploy against: " + unit );
		String initScript = createInitScript( railsMetaData.getRailsRootPath(), railsMetaData.getRailsEnv() );
		
		RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.class );
		if ( runtimeMetaData == null ) {
			runtimeMetaData = new RubyRuntimeMetaData();
			unit.addAttachment( RubyRuntimeMetaData.class, runtimeMetaData );
		}
		
		runtimeMetaData.setInitScript( initScript );
		
	}
	
	public static String createInitScript(String railsRoot, String railsEnv) {
		String initScript = 
			"RAILS_ROOT='" + railsRoot + "'\n" + 
			"RAILS_ENV='" + railsEnv + "'\n" + 
			"require \"#{RAILS_ROOT}/config/boot.rb\"\n" +
			"require \"#{RAILS_ROOT}/config/environment.rb\"\n";
		return initScript;
		
		
	}
	

}
