package org.eclipse.incquery.examples.cps.generator.phases

import com.google.common.collect.Lists
import org.eclipse.incquery.examples.cps.generator.dtos.CPSFragment
import org.eclipse.incquery.examples.cps.generator.operations.ApplicationInstanceGenerationOperation
import org.eclipse.incquery.examples.cps.generator.operations.HostInstanceGenerationOperation
import org.eclipse.incquery.examples.cps.planexecutor.interfaces.IPhase

class CPSPhaseInstanceGeneration implements IPhase<CPSFragment>{
	
	override getOperations(CPSFragment fragment) {
		val operations = Lists.newArrayList();
		
		// ApplicationInstances
		for(appClass : fragment.applicationTypes.keySet){
			val appTypes = fragment.applicationTypes.get(appClass);
			if(appTypes != null){
				for(appType : appTypes){
					operations.add(new ApplicationInstanceGenerationOperation(appClass, appType));
				}			
			}
		}

		
		// Generate Host Instances
		// HostClasses
		for(hostClass : fragment.hostTypes.keySet){
			val types = fragment.hostTypes.get(hostClass);
			// HostTypes
			if(types != null){
				for(type : types){
					operations.add(new HostInstanceGenerationOperation(hostClass, type));
				}
			}
			
		}
		
		return operations;
	}
	
}