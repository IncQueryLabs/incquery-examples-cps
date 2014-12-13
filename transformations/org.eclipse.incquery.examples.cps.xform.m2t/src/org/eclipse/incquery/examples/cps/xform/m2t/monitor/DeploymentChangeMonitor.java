package org.eclipse.incquery.examples.cps.xform.m2t.monitor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.incquery.examples.cps.deployment.Deployment;
import org.eclipse.incquery.examples.cps.deployment.DeploymentElement;
import org.eclipse.incquery.examples.cps.deployment.DeploymentHost;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.ApplicationBehaviorChangeQuerySpecification;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.ApplicationIdChangeQuerySpecification;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.BehaviorChangeQuerySpecification;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.DeploymentHostIpChangeQuerySpecification;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.DeploymentHostsChangeQuerySpecification;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.HostApplicationsChangeQuerySpecification;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.HostIpChangeQuerySpecification;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.TransitionChangeQuerySpecification;
import org.eclipse.incquery.examples.cps.xform.m2t.listener.util.TriggerChangeQuerySpecification;
import org.eclipse.incquery.runtime.api.IMatchProcessor;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IQuerySpecification;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.evm.api.ExecutionSchema;
import org.eclipse.incquery.runtime.evm.api.Job;
import org.eclipse.incquery.runtime.evm.api.RuleSpecification;
import org.eclipse.incquery.runtime.evm.specific.ExecutionSchemas;
import org.eclipse.incquery.runtime.evm.specific.Jobs;
import org.eclipse.incquery.runtime.evm.specific.Lifecycles;
import org.eclipse.incquery.runtime.evm.specific.Rules;
import org.eclipse.incquery.runtime.evm.specific.Schedulers;
import org.eclipse.incquery.runtime.evm.specific.event.IncQueryActivationStateEnum;
import org.eclipse.incquery.runtime.evm.specific.job.EnableJob;
import org.eclipse.incquery.runtime.evm.specific.scheduler.UpdateCompleteBasedScheduler.UpdateCompleteBasedSchedulerFactory;
import org.eclipse.incquery.runtime.exception.IncQueryException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("unchecked")
public class DeploymentChangeMonitor implements IDeploymentChangeMonitor {

	Set<DeploymentElement> appearBetweenCheckpoints;
	Set<DeploymentElement> updateBetweenCheckpoints;
	Set<DeploymentElement> disappearBetweenCheckpoints;
	Set<DeploymentElement> appearAccumulator;
	Set<DeploymentElement> updateAccumulator;
	Set<DeploymentElement> disappearAccumulator;
	boolean deploymentBetweenCheckpointsChanged;
	boolean deploymentChanged;

	@Override
	public synchronized DeploymentChangeDelta createCheckpoint() {
		appearBetweenCheckpoints = appearAccumulator;
		updateBetweenCheckpoints = updateAccumulator;
		disappearBetweenCheckpoints = disappearAccumulator;
		appearAccumulator = Sets.newHashSet();
		updateAccumulator = Sets.newHashSet();
		disappearAccumulator = Sets.newHashSet();
		deploymentBetweenCheckpointsChanged = deploymentChanged;
		return new DeploymentChangeDelta(appearBetweenCheckpoints,
				updateBetweenCheckpoints, disappearBetweenCheckpoints,
				deploymentBetweenCheckpointsChanged);
	}

	@Override
	public DeploymentChangeDelta getDeltaSinceLastCheckpoint() {
		return new DeploymentChangeDelta(appearAccumulator, updateAccumulator,
				disappearAccumulator, deploymentChanged);
	}

	@Override
	public synchronized void startMonitoring(Deployment deployment,
			IncQueryEngine engine) throws IncQueryException {

		this.appearBetweenCheckpoints = Sets.newHashSet();
		this.updateBetweenCheckpoints = Sets.newHashSet();
		this.disappearBetweenCheckpoints = Sets.newHashSet();
		this.appearAccumulator = Sets.newHashSet();
		this.updateAccumulator = Sets.newHashSet();
		this.disappearAccumulator = Sets.newHashSet();
		deploymentBetweenCheckpointsChanged = false;
		deploymentChanged = false;

		UpdateCompleteBasedSchedulerFactory schedulerFactory = Schedulers
				.getIQEngineSchedulerFactory(engine);
		ExecutionSchema executionSchema = ExecutionSchemas
				.createIncQueryExecutionSchema(engine, schedulerFactory);

		Set<Job<?>> allJobs = Sets.newHashSet();

		Set<Job<IPatternMatch>> deploymentJobs = createDeploymentJobs();
		allJobs.addAll(deploymentJobs);

		IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>> deploymentHostChangeQuerySpec = (IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) DeploymentHostsChangeQuerySpecification
				.instance();
		IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>> deploymentHostIpChangeQuerySpec = (IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) DeploymentHostIpChangeQuerySpecification
				.instance();

		registerJobsForPattern(executionSchema, deploymentJobs,
				deploymentHostChangeQuerySpec);
		registerJobsForPattern(executionSchema, deploymentJobs,
				deploymentHostIpChangeQuerySpec);

		Map<IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>, Set<Job<IPatternMatch>>> querySpecificationsToJobs = getDeploymentElementChangeQuerySpecifications();

		for (IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>> querySpec : querySpecificationsToJobs
				.keySet()) {
			registerJobsForPattern(executionSchema,
					querySpecificationsToJobs.get(querySpec), querySpec);
		}
		Collection<Set<Job<IPatternMatch>>> registeredJobs = querySpecificationsToJobs
				.values();
		for (Set<Job<IPatternMatch>> deploymentElementJobs : registeredJobs) {
			allJobs.addAll(deploymentElementJobs);
		}

		executionSchema.startUnscheduledExecution();

		// Enable the jobs to listen to changes
		for (Job<?> job : allJobs) {
			EnableJob<?> enableJob = (EnableJob<?>) job;
			enableJob.setEnabled(true);
		}

	}

	private Set<Job<IPatternMatch>> createDeploymentJobs() {

		Set<Job<IPatternMatch>> jobs = Sets.newHashSet();

		Job<IPatternMatch> appear = Jobs.newStatelessJob(
				IncQueryActivationStateEnum.APPEARED,
				new IMatchProcessor<IPatternMatch>() {

					@Override
					public void process(IPatternMatch match) {
						deploymentChanged = true;
					}

				});
		Job<IPatternMatch> disappear = Jobs.newStatelessJob(
				IncQueryActivationStateEnum.DISAPPEARED,
				new IMatchProcessor<IPatternMatch>() {

					@Override
					public void process(IPatternMatch match) {
						deploymentChanged = true;
					}

				});
		Job<IPatternMatch> update = Jobs.newStatelessJob(
				IncQueryActivationStateEnum.UPDATED,
				new IMatchProcessor<IPatternMatch>() {

					@Override
					public void process(IPatternMatch match) {
						deploymentChanged = true;
					}

				});

		jobs.add(Jobs.newEnableJob(appear));
		jobs.add(Jobs.newEnableJob(disappear));
		jobs.add(Jobs.newEnableJob(update));

		return jobs;
	}

	private void registerJobsForPattern(
			ExecutionSchema executionSchema,
			Set<Job<IPatternMatch>> deploymentElementJobs,
			IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>> changeQuerySpecification) {
		RuleSpecification<IPatternMatch> applicationRules = Rules
				.newMatcherRuleSpecification(changeQuerySpecification,
						Lifecycles.getDefault(true, true),
						deploymentElementJobs);
		executionSchema.addRule(applicationRules);
	}

	private Map<IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>, Set<Job<IPatternMatch>>> getDeploymentElementChangeQuerySpecifications()
			throws IncQueryException {
		Map<IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>, Set<Job<IPatternMatch>>> querySpecifications = Maps
				.newHashMap();
		querySpecifications
				.put((IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) HostApplicationsChangeQuerySpecification
						.instance(), hostChangeJobs());
		querySpecifications
				.put((IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) HostIpChangeQuerySpecification
						.instance(), hostChangeJobs());
		querySpecifications
				.put((IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) ApplicationIdChangeQuerySpecification
						.instance(), applicationChangeJobs());
		querySpecifications
				.put((IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) ApplicationBehaviorChangeQuerySpecification
						.instance(), applicationChangeJobs());
		querySpecifications
				.put((IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) BehaviorChangeQuerySpecification
						.instance(), defaultChangeJobs());
		querySpecifications
				.put((IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) TransitionChangeQuerySpecification
						.instance(), defaultChangeJobs());
		querySpecifications
				.put((IQuerySpecification<? extends IncQueryMatcher<IPatternMatch>>) TriggerChangeQuerySpecification
						.instance(), defaultChangeJobs());
		return querySpecifications;
	}

	private Set<Job<IPatternMatch>> hostChangeJobs() {
		IMatchProcessor<IPatternMatch> appearProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				registerAppear(match);
			}
		};
		IMatchProcessor<IPatternMatch> disappearProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				DeploymentHost host = (DeploymentHost) match.get(0);
				if (host.eContainer() != null) {
					registerUpdate(match);
				} else {
					registerDisappear(match);
				}
			}
		};
		IMatchProcessor<IPatternMatch> updateProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				registerUpdate(match);
			}
		};

		return createDeploymentElementJobs(appearProcessor, disappearProcessor,
				updateProcessor);
	}

	private Set<Job<IPatternMatch>> applicationChangeJobs() {
		IMatchProcessor<IPatternMatch> appearProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				registerAppear(match);
			}
		};
		IMatchProcessor<IPatternMatch> disappearProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				registerDisappear(match);
			}
		};
		IMatchProcessor<IPatternMatch> updateProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				registerUpdate(match);
			}
		};

		return createDeploymentElementJobs(appearProcessor, disappearProcessor,
				updateProcessor);
	}
	
	private Set<Job<IPatternMatch>> defaultChangeJobs() {
		IMatchProcessor<IPatternMatch> appearProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				registerAppear(match);
			}
		};
		IMatchProcessor<IPatternMatch> disappearProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				registerDisappear(match);
			}
		};
		IMatchProcessor<IPatternMatch> updateProcessor = new IMatchProcessor<IPatternMatch>() {
			@Override
			public void process(IPatternMatch match) {
				registerUpdate(match);
			}
		};

		return createDeploymentElementJobs(appearProcessor, disappearProcessor,
				updateProcessor);
	}

	private void registerUpdate(IPatternMatch match) {
		DeploymentElement deploymentElement = (DeploymentElement) match.get(0);
		if (appearAccumulator.contains(deploymentElement)) {
			appearAccumulator.remove(deploymentElement);
		}
		updateAccumulator.add(deploymentElement);
	}

	private void registerAppear(IPatternMatch match) {
		DeploymentElement deploymentElement = (DeploymentElement) match.get(0);
		if (disappearAccumulator.contains(deploymentElement)) {
			disappearAccumulator.remove(deploymentElement);
		}
		appearAccumulator.add(deploymentElement);
	}

	private void registerDisappear(IPatternMatch match) {
		DeploymentElement deploymentElement = (DeploymentElement) match.get(0);
		if (appearAccumulator.contains(deploymentElement)) {
			appearAccumulator.remove(deploymentElement);
		} else if (updateAccumulator.contains(deploymentElement)) {
			updateAccumulator.remove(deploymentElement);
		}
		disappearAccumulator.add(deploymentElement);
	}

	private Set<Job<IPatternMatch>> createDeploymentElementJobs(
			IMatchProcessor<IPatternMatch> appearProcessor,
			IMatchProcessor<IPatternMatch> disappearProcessor,
			IMatchProcessor<IPatternMatch> updateProcessor) {
		Set<Job<IPatternMatch>> jobs = Sets.newHashSet();

		Job<IPatternMatch> appear = Jobs.newStatelessJob(
				IncQueryActivationStateEnum.APPEARED, appearProcessor);
		Job<IPatternMatch> disappear = Jobs.newStatelessJob(
				IncQueryActivationStateEnum.DISAPPEARED, disappearProcessor);
		Job<IPatternMatch> update = Jobs.newStatelessJob(
				IncQueryActivationStateEnum.UPDATED, updateProcessor);

		jobs.add(Jobs.newEnableJob(appear));
		jobs.add(Jobs.newEnableJob(disappear));
		jobs.add(Jobs.newEnableJob(update));

		return jobs;
	}

}