package org.eclipse.incquery.examples.cps.integration.messages

import java.security.InvalidParameterException
import java.util.List
import org.eclipse.incquery.examples.cps.integration.SerializerTransformationStep
import org.eclipse.incquery.examples.cps.xform.m2t.api.M2TOutputRecord
import org.eclipse.viatra.integration.mwe2.IMessage
import org.eclipse.viatra.integration.mwe2.IMessageProcessor
import org.eclipse.viatra.integration.mwe2.ITransformationStep

class M2TOutputMessageProcessor implements IMessageProcessor<List<M2TOutputRecord>, M2TOutputMessage> {
	ITransformationStep parent

	override ITransformationStep getParent() {
		return parent
	}

	override void setParent(ITransformationStep parent) {
		this.parent = parent
	}

	override void processMessage(IMessage<? extends Object> message) throws InvalidParameterException {
		if (message instanceof M2TOutputMessage) {
			var M2TOutputMessage event = (message as M2TOutputMessage)
			if (parent instanceof SerializerTransformationStep) {
				var SerializerTransformationStep serializerparent = (parent as SerializerTransformationStep)
				serializerparent.m2tOutput = event.getParameter()
			}
		}
	}
}