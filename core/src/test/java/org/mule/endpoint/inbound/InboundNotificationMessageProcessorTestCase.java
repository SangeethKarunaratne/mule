/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.endpoint.inbound;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.context.notification.EndpointMessageNotification;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

public class InboundNotificationMessageProcessorTestCase extends AbstractInboundMessageProcessorTestCase
{

    public void testProcess() throws Exception
    {
        TestEndpointMessageNotificationListener<EndpointMessageNotification> listener = new TestEndpointMessageNotificationListener<EndpointMessageNotification>();
        muleContext.registerListener(listener);

        InboundEndpoint endpoint = createTestInboundEndpoint(null, null);
        MessageProcessor mp = new InboundNotificationMessageProcessor(endpoint);
        MuleEvent event = createTestInboundEvent(endpoint);
        mp.process(event);

        assertTrue(listener.latch.await(RECEIVE_TIMEOUT, TimeUnit.MILLISECONDS));
        assertEquals(EndpointMessageNotification.MESSAGE_RECEIVED, listener.messageNotification.getAction());
        assertEquals(endpoint.getEndpointURI().getUri().toString(),
            listener.messageNotification.getEndpoint());
        assertTrue(listener.messageNotification.getSource() instanceof MuleMessage);
        assertEquals(event.getMessage().getPayload(),
            ((MuleMessage) listener.messageNotification.getSource()).getPayload());
    }

}
