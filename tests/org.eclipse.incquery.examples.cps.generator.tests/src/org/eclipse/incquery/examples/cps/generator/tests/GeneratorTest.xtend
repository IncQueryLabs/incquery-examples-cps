package org.eclipse.incquery.examples.cps.generator.tests

import com.google.common.collect.HashMultimap
import org.eclipse.incquery.examples.cps.cyberPhysicalSystem.ApplicationInstance
import org.eclipse.incquery.examples.cps.cyberPhysicalSystem.HostInstance
import org.eclipse.incquery.examples.cps.generator.dtos.AppClass
import org.eclipse.incquery.examples.cps.generator.dtos.CPSFragment
import org.eclipse.incquery.examples.cps.generator.dtos.CPSGeneratorInput
import org.eclipse.incquery.examples.cps.generator.dtos.MinMaxData
import org.eclipse.incquery.examples.cps.generator.dtos.Percentage
import org.eclipse.incquery.examples.cps.generator.interfaces.IConstraints
import org.eclipse.incquery.examples.cps.generator.operations.ActionGenerationOperation
import org.eclipse.incquery.examples.cps.generator.operations.ApplicationInstanceAllocationOperation
import org.eclipse.incquery.examples.cps.generator.operations.ApplicationInstanceGenerationOperation
import org.eclipse.incquery.examples.cps.generator.operations.ApplicationTypeGenerationOperation
import org.eclipse.incquery.examples.cps.generator.tests.constraints.AllocationCPSConstraints
import org.eclipse.incquery.examples.cps.generator.tests.constraints.DemoCPSConstraints
import org.eclipse.incquery.examples.cps.generator.tests.constraints.SimpleCPSConstraints
import org.eclipse.incquery.examples.cps.generator.utils.CPSModelBuilderUtil
import org.eclipse.incquery.examples.cps.generator.utils.StatsUtil
import org.eclipse.incquery.runtime.api.IncQueryEngine
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.*

class GeneratorTest extends TestBase {
	
	val long SEED = 11111111;
	
	@Ignore
	@Test
	def void testSimple(){
		runGeneratorOn(new SimpleCPSConstraints(), 111111);
	}
	
	@Ignore
	@Test
	def void testDemo(){
		runGeneratorOn(new DemoCPSConstraints(), 111111);
	}


	
	 ///////////////
	// Unit tests
	
	private extension CPSModelBuilderUtil modelBuilder = new CPSModelBuilderUtil
	
	// Test Actions
	@Test
	def void testActionGenerationOperation(){
		val fragment = prepareCPSFragmentWithEmptyModel(null)

		// Init model		
		val transition = fragment?.modelRoot?.prepareApplicationTypeWithId("A1")?.prepareStateMachine("SM1")?.prepareState("S1")?.prepareTransition("T1")
		
		val op = new ActionGenerationOperation("action(p1)", transition);
		
		// Execute Action
		op.execute(fragment)
		
		// Assertions
		assertEquals("action(p1)", transition.action)
		assertEquals(4, StatsUtil.size(fragment.modelRoot));
	}
	
	@Test
	def void testApplicationInstanceAllocationOperation(){
		val fragment = prepareCPSFragmentWithEmptyModel(null)

		// Init model		
		val appType = fragment?.modelRoot?.prepareApplicationTypeWithId("A1")
		val app1 = appType?.prepareApplicationInstanceWithId("A11")
		
		val hostType1 = fragment?.modelRoot?.prepareHostTypeWithId("H1")
		val hInstance1 = hostType1?.prepareHostInstanceWithIP("H11", "ip")
		val initSize =  StatsUtil.size(fragment.modelRoot)
		
		val allocMap = HashMultimap.<HostInstance, ApplicationInstance>create
		allocMap.put(hInstance1, app1);

		val op = new ApplicationInstanceAllocationOperation(allocMap)
		
		// Execute Action
		op.execute(fragment)
		
		// Assertions
		// Size
		assertEquals(initSize, StatsUtil.size(fragment.modelRoot));
		
		// Allocated to h1
		assertEquals(hInstance1, app1.allocatedTo);
	}
	
	@Test
	def void testApplicationInstanceGenerationOperation(){
		val fragment = prepareCPSFragmentWithEmptyModel(null)

		val appCount = 2

		// Init model		
		val appType = fragment?.modelRoot?.prepareApplicationTypeWithId("A1")
		val appClass = new AppClass(
			"AC1",
			null,
			new MinMaxData(appCount, appCount),
			null,
			null,
			null,
			null,
			null,
			null
		)
	
		val initSize =  StatsUtil.size(fragment.modelRoot)

		val op = new ApplicationInstanceGenerationOperation(appClass, appType)
		
		// Execute Action
		op.execute(fragment)
		
		// Assertions
		// Size
		assertEquals(initSize+appCount, StatsUtil.size(fragment.modelRoot));
		
		// Allocated to h1
		assertEquals(2, appType.instances.size);
	}
	
	@Test
	def void testApplicationTypeGenerationOperation(){
		val fragment = prepareCPSFragmentWithEmptyModel(null)

		val appCount = 2
		val stateCount = 3
		val transitionCount = 4

		// Init model		
		val appClass = new AppClass(
			"AC1",
			new MinMaxData(appCount, appCount),
			null,
			new MinMaxData(stateCount, stateCount),
			new MinMaxData(transitionCount, transitionCount),
			null,
			null,
			new Percentage(100),
			new Percentage(100)
		)
	
		val initSize =  StatsUtil.size(fragment.modelRoot)

		val op = new ApplicationTypeGenerationOperation(appClass)
		
		// Execute Action
		op.execute(fragment)
		
		
		// Assertions
		// Size
		assertEquals(initSize + appCount*2 + appCount * stateCount + appCount * transitionCount, StatsUtil.size(fragment.modelRoot));
	}
	
	@Test
	def void testDeterministicAllocation(){
		val constraints = new AllocationCPSConstraints()
		val out = runGeneratorOn(constraints, SEED);
		
		val engine = IncQueryEngine.on(out.modelRoot)
		val stats = StatsUtil.generateStats(engine, out.modelRoot)
		
//		persistModel(out);
		
		assertEquals(6, stats.allocatedAppCount)
		
		
//		AllocatedAppInstancesMatcher.on(engine).getAllValuesOfA()
		
	}
	
	

	private def prepareEmptyCPSModel() {
		val CPSModelBuilderUtil mb = new CPSModelBuilderUtil;
		val cps2dep = mb.prepareEmptyModel("testModel"+System.nanoTime);
			
		if(cps2dep != null && cps2dep.cps != null){
			return cps2dep.cps;
		}else{
			info("!!! Error: Cannot create CPS model");
		}
	} 
	
	private def prepareCPSFragmentWithEmptyModel(IConstraints constraints) {
		val model = prepareEmptyCPSModel
		val input = new CPSGeneratorInput(SEED, constraints, model);
		return new CPSFragment(input);
	}
	
	
	
	
	
	
	
}