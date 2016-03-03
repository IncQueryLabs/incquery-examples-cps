package org.eclipse.viatra.examples.cps.generator

import org.eclipse.viatra.examples.cps.generator.dtos.GeneratorPlan
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhaseActionGeneration
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhaseActionStatisticsBasedGeneration
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhaseApplicationAllocation
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhaseHostCommunication
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhaseInstanceGeneration
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhasePrepare
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhaseSignalSet
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhaseTypeGeneration
import org.eclipse.viatra.examples.cps.generator.phases.CPSPhaseTypeStatisticsBasedGeneration

enum CPSPlans{
	DEFAULT, STATISTICS_BASED
}

class CPSPlanBuilder {
	
	def static buildDefaultPlan(){
		var GeneratorPlan plan = new GeneratorPlan();
		
		plan.addPhase(new CPSPhasePrepare());
		plan.addPhase(new CPSPhaseSignalSet());
		plan.addPhase(new CPSPhaseTypeGeneration());
		plan.addPhase(new CPSPhaseInstanceGeneration());
		plan.addPhase(new CPSPhaseHostCommunication());
		plan.addPhase(new CPSPhaseApplicationAllocation());
		plan.addPhase(new CPSPhaseActionGeneration());
		
		plan;
	}

	def static buildCharacteristicBasedPlan(){
		var GeneratorPlan plan = new GeneratorPlan();
		
		plan.addPhase(new CPSPhasePrepare());
		plan.addPhase(new CPSPhaseSignalSet());
		plan.addPhase(new CPSPhaseTypeStatisticsBasedGeneration());
		plan.addPhase(new CPSPhaseInstanceGeneration());
		plan.addPhase(new CPSPhaseHostCommunication());
		plan.addPhase(new CPSPhaseApplicationAllocation());
		plan.addPhase(new CPSPhaseActionStatisticsBasedGeneration());
		
		plan;
	}
}