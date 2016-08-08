package co.phoenixlab.discord.api.util;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import lombok.Getter;

import java.util.function.UnaryOperator;

@Getter
public class OkFailMeter {

    private final Meter okMeter;
    private final Meter failMeter;

    public OkFailMeter(UnaryOperator<String> nameFactory, MetricRegistry metrics) {
        okMeter = metrics.meter(nameFactory.apply("ok"));
        failMeter = metrics.meter(nameFactory.apply("fail"));
    }

    public void ok() {
        okMeter.mark();
    }

    public void fail() {
        failMeter.mark();
    }
}
