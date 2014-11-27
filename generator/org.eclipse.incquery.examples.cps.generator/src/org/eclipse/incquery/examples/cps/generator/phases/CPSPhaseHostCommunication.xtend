package org.eclipse.incquery.examples.cps.generator.phases

import com.google.common.collect.HashMultimap
import com.google.common.collect.Lists
import java.util.List
import org.eclipse.incquery.examples.cps.cyberPhysicalSystem.HostInstance
import org.eclipse.incquery.examples.cps.generator.dtos.CPSFragment
import org.eclipse.incquery.examples.cps.generator.dtos.HostClass
import org.eclipse.incquery.examples.cps.generator.operations.HostInstanceCommunicatesWithOperation
import org.eclipse.incquery.examples.cps.generator.utils.RandomUtils
import org.eclipse.incquery.examples.cps.planexecutor.api.IPhase
import org.eclipse.incquery.examples.cps.generator.utils.CPSModelBuilderUtil

class CPSPhaseHostCommunication implements IPhase<CPSFragment>{
	
	private extension RandomUtils randUtil = new RandomUtils;
	
	override getOperations(CPSFragment fragment) {
		val operations = Lists.newArrayList();
	
		//val hostInstances = HostInstancesMatcher.on(fragment.engine).allValuesOfhostInstance.toList;

		// Calculate hostclass to host instance map
		val hostClassToInstanceMap = CPSModelBuilderUtil.calculateHostInstancesToHostClassMap(fragment);


		// Generate communications
		for(hostClass : fragment.hostTypes.keySet){ // HostClasses store the configuration
			val possibleTargetInstances = calculatePossibleTargetInstances(hostClass, hostClassToInstanceMap)
			
			for(hostType : fragment.hostTypes.get(hostClass)){ // Every HostInstance
				for(hostInstance : hostType.instances){
					// Initialize list of forbidden targets
					var List<HostInstance> forbiddenTargetInstances = Lists.newArrayList;
					// Add itself to the forbidden targets
					forbiddenTargetInstances.add(hostInstance); 
					// Calculate the number of new communication links
					val numberOfCommLinks = hostClass.numberOfCommunicationLines.randInt(fragment.random); 
					// Create communication links
					for(i : 0 ..< numberOfCommLinks){
						// Randomize target node
						val targetHostInstance = possibleTargetInstances.randElementExcept(forbiddenTargetInstances, fragment.random);
						if(targetHostInstance != null){
							forbiddenTargetInstances.add(targetHostInstance);
							operations.add(new HostInstanceCommunicatesWithOperation(hostInstance, targetHostInstance));
						}
					}
				}
			}
		}
		
		return operations;
	}
	
	def calculatePossibleTargetInstances(HostClass sourceClass, HashMultimap<HostClass, HostInstance> hostClassToInstances) {
		var minRatio = sourceClass.communicationRatios.values.fold(Integer.MAX_VALUE)[min, act| 
			if(act < min && act != 0){
				act
			}else{
				min
			}
		];
		
		val possibleInstances = Lists.<HostInstance>newArrayList;
		for(targetHClass : sourceClass.communicationRatios.keySet){
			val targetRatio = sourceClass.communicationRatios.get(targetHClass);
			val normRatio = Math.round(targetRatio as float / minRatio as float); //?
			for(i : 0 ..< normRatio){
				possibleInstances.addAll(hostClassToInstances.get(targetHClass));
			}
		}
		return possibleInstances;
	}
	
}