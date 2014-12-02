package org.eclipse.incquery.examples.cps.m2t.proto.distributed.generated.hosts.statemachines;

import java.util.List;

import org.eclipse.incquery.examples.cps.m2t.proto.distributed.general.applications.Application;
import org.eclipse.incquery.examples.cps.m2t.proto.distributed.general.applications.statemachines.State;

import com.google.common.collect.Lists;

public enum BehaviorISSBState implements State<BehaviorISSBState>{
     ///////////
	// States
	ISSWait {
        @Override
        public List<State<BehaviorISSBState>> possibleNextStates(Application app) {
        	List<State<BehaviorISSBState>> possibleStates = Lists.newArrayList();
        	
        	// Add Neutral Transitions
        	
        	// Add Send Transitions
        	        	
        	// Add Wait Transitions
        	if(app.hasMessageFor(ISS_RECEIVING)){
        		possibleStates.add(ISSReceived);
        	}
        	
        	return possibleStates;
        }
    },
    ISSReceived {
        @Override
        public List<State<BehaviorISSBState>> possibleNextStates(Application app) {
        	List<State<BehaviorISSBState>> possibleStates = Lists.newArrayList();
        	
        	// Add Neutral Transitions
        	possibleStates.add(ISSWait);
        	
        	// Add Send Transitions
        	        	
        	// Add Wait Transitions
        	
        	return possibleStates;
        }
    };
	
     ////////////
    // Triggers
    // TODO should be Enum...?
	public static final String ISS_RECEIVING = "ISSReceiving";
	
	 /////////////////
	// General part
	@Override
	abstract public List<State<BehaviorISSBState>> possibleNextStates(Application app);
	
	@Override
	public BehaviorISSBState stepTo(BehaviorISSBState nextState, Application app){
		if(possibleNextStates(app).contains(nextState)){
			return nextState;
		}
		return this;
	}

}
