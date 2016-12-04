package ru.ms;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import ru.ms.cfg.CfGraph;
import ru.ms.cfg.CfgParser;
import ru.ms.printer.ICfgPrinter;
import ru.ms.processors.IProcessor;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sergey on 03.12.16.
 */
public class Context {

    public static Context instance = null;

    public static Context initInstance(File file) {
        instance = new Context(file);
        return instance;
    }

    public static Context getInstance() {
        return instance;
    }

    private final File logFileName;
    private CfgParser cfgParser;
    private CfGraph currentGraph;
    private Profile currentProfile = new Profile();
    private Profile.EventProfile currentEventProfile;

    public CfGraph getCurrentGraph() {
        return currentGraph;
    }

    public Profile getProfile() {
        return currentProfile;
    }

    public Profile.EventProfile getCurrentEventProfile() {
        return currentEventProfile;
    }

    public Context(File logFile) {
        logFileName = logFile;
    }

    public void load() {
        cfgParser = CfgParser.parseLog(logFileName);
    }

    public void reload() {
        load();
    }

    public void print(ICfgPrinter p) {

    }

    public void map(IProcessor processor) {
        processor.transform(this);
    }

    public void addProfile(Profile.EventProfile profile, String eventName) {
        currentProfile.addEventProfile(profile, eventName);
    }

    public Set<String> availableEvents() {
        return currentProfile.availableEvents();
    }

    public void setEvent(String eventName) {
        currentEventProfile = currentProfile.getEventProfile(eventName);
    }

    public void setCompilation(int methodIdx, int compilationIdx) {
        cfgParser.selectCompilation(methodIdx, compilationIdx);
        currentGraph = cfgParser.getSelectedGraph();
    }

    public static class CompilationInfo {
        public final String methodName;
        public final int methodId;
        public final List<Compilation> compilations;

        public CompilationInfo(
                String methodName,
                int methodId,
                List<Compilation> compilations) {
            this.methodName = methodName;
            this.methodId = methodId;
            this.compilations = compilations;
        }
    }

    public List<CompilationInfo> getCompilations(String filterRegEx) {
        return cfgParser
                .listCompilation()
                .stream()
                .filter(
                        (cmpl) -> cmpl.methodName.matches(filterRegEx)
                )
                .collect(Collectors.toList());
    }
}
