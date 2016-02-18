package org.eclipse.incquery.examples.cps.integration;

import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.incquery.examples.cps.deployment.Deployment;
import org.eclipse.incquery.examples.cps.traceability.CPSToDeployment;
import org.eclipse.incquery.examples.cps.xform.m2t.monitor.DeploymentChangeDelta;
import org.eclipse.incquery.examples.cps.xform.m2t.monitor.DeploymentChangeMonitor;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.exception.IncQueryException;
import org.eclipse.viatra.integration.mwe2.IPublishTo;
import org.eclipse.viatra.integration.mwe2.mwe2impl.TransformationStep;

public class ChangeMonitorTransformationStep extends TransformationStep {
    protected AdvancedViatraQueryEngine engine;
    protected DeploymentChangeMonitor monitor;
    protected DeploymentChangeDelta delta;
    
    @Override
    public void doInitialize(IWorkflowContext ctx) {
        // create transformation
        System.out.println("Initialized change monitor");
        engine = (AdvancedViatraQueryEngine) ctx.get("engine");
        Deployment deployment = ((CPSToDeployment) ctx.get("model")).getDeployment();
        monitor = new DeploymentChangeMonitor(deployment, engine);
        try {
            monitor.startMonitoring();
        } catch (IncQueryException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doExecute() {
        delta = monitor.createCheckpoint();
        System.out.println("Checkpoint created");
    }
    
    @Override
    public void publishMessages() {
        for (IPublishTo iPublishTo : publishTo) {
            iPublishTo.publishMessage(delta);
        }
    }
    
    @Override
    public void dispose() {
        System.out.println("Disposed change monitor");
    }
}
