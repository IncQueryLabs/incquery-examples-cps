package org.eclipse.incquery.examples.cps.planexecutor

import org.apache.log4j.Logger
import org.eclipse.incquery.examples.cps.planexecutor.exceptions.ModelGeneratorException
import org.eclipse.incquery.examples.cps.planexecutor.api.IPlan
import org.eclipse.incquery.examples.cps.planexecutor.api.Initializer

class PlanExecutor<FragmentType, InputType extends Initializer<FragmentType>> {
	
	protected extension Logger logger = Logger.getLogger("cps.generator.Generator")
	
	def process(IPlan<FragmentType> plan, InputType input){
		val FragmentType fragment = input.getInitialFragment;
		
		continueProcessing(plan, fragment)
		
		return fragment;
	}
	
	def continueProcessing(IPlan<FragmentType> plan, FragmentType fragment) {
		plan.phases.forEach[phase, i| 
			info("<< PHASE " + phase.class.simpleName + " >>");
			phase.getOperations(fragment).forEach[operation, j|
				try{
					info("< OPERATION " + operation.class.simpleName + " >");
					operation.execute(fragment);
					info("<-------------------- END OPERATION ----------------------->");
				}catch(ModelGeneratorException e){
					info(e.message);
				}
			]
			info("<<===================== END PHASE ========================>>");
		]
	}
	
}