package io.mosip.signup.config;

import io.micrometer.tracing.Tracer;
import org.apache.catalina.valves.AccessLogValve;

import java.io.CharArrayWriter;

public class MicrometerTracingValve extends AccessLogValve {

    private final Tracer tracer;

    public MicrometerTracingValve(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void log(CharArrayWriter message) {
        String traceId = tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "no-trace";
        String spanId = tracer.currentSpan() != null ? tracer.currentSpan().context().spanId() : "no-span";
        message.append(" traceId=").append(traceId).append(" spanId=").append(spanId);
        super.log(message);
    }

}
