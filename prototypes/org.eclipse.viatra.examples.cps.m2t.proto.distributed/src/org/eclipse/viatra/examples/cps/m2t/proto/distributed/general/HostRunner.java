package org.eclipse.viatra.examples.cps.m2t.proto.distributed.general;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.viatra.examples.cps.m2t.proto.distributed.general.applications.Application;
import org.eclipse.viatra.examples.cps.m2t.proto.distributed.general.applications.statemachines.State;
import org.eclipse.viatra.examples.cps.m2t.proto.distributed.general.hosts.Host;

public class HostRunner implements Runnable  {

	private static Logger logger = Logger.getLogger("cps.proto.distributed.hostrunner");

	Host host;
	
	public HostRunner(Host host) {
		this.host = host;
	}
		
	public boolean hasHost() {
		return host != null;
	}

	public Host getHost() {
		return host;
	}

	@Override
	public void run() {
		logger.info("Start running with " + host.getClass().getSimpleName());
		
		while(!Thread.interrupted()){
			for(Application app : host.getApplications()){
				State<?> currentState = app.getCurrentState();
				List<?> possibleNextStates = currentState.possibleNextStates(app);
				
				if(!possibleNextStates.isEmpty()){
					app.stepToState((State)possibleNextStates.get(0));
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
