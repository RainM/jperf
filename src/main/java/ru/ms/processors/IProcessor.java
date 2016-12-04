package ru.ms.processors;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import ru.ms.Context;
import ru.ms.cfg.CfGraph;

import java.util.function.Consumer;

/**
 * Created by sergey on 27.11.16.
 */
public interface IProcessor {

    public enum Type {
        LOOP(LoopProcessor.class, LoopProcessor::processArgParser),
        PERF_PROFILE(PerfProfileProcessor.class, PerfProfileProcessor::processArgParser);

        private final Class analyzer;
        private final Consumer<ArgumentParser> argparer;

        private Type(Class c, Consumer<ArgumentParser> argparer) {
            analyzer = c;
            this.argparer = argparer;
        }

        public IProcessor create(Object opts) throws Exception {
            IProcessor result = (IProcessor) analyzer.getConstructor().newInstance();
            return result;
        }
    }

    //CfGraph analyzeAndProcess(CfGraph g, Object options);

    void transform(Context ctx);
}
