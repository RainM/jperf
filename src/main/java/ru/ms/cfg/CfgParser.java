package ru.ms.cfg;

import org.adoptopenjdk.jitwatch.core.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.model.*;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import ru.ms.Context;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by sergey on 25.11.16.
 */
public class CfgParser {

    private final JITDataModel model;

    private CfgParser(JITDataModel model) {
        this.model = model;
    }

    private static void listAllClasses(MetaPackage mp, Predicate<MetaClass> func) {
        for (MetaPackage child_mp : mp.getChildPackages()) {
            listAllClasses(child_mp, func);
        }
        //mp.getPackageClasses().forEach(func::accept);
        for (MetaClass mc : mp.getPackageClasses()) {
            if (!func.test(mc))
                return;
        }
    }

    private static boolean listAllMethods(PackageManager pm, Predicate<IMetaMember> func) {
        for (MetaPackage mp : pm.getRootPackages()) {
            if (!listAllMethods(mp, func)) {
                return false;
            }
        }
        return true;
    }

    private static boolean listAllMethods(MetaPackage mp, Predicate<IMetaMember> func) {
        for (MetaPackage child_mp : mp.getChildPackages()) {
            if (!listAllMethods(child_mp, func)) {
                return false;
            }
        }
        for (MetaClass mc : mp.getPackageClasses()) {
            for (IMetaMember mm : mc.getMetaMembers()) {
                if (!func.test(mm)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class IntHolder {
        public int idx = 0;
        public int inc() {
            return ++idx;
        }
    }

    public List<Context.CompilationInfo> listCompilation() {
        final ArrayList<Context.CompilationInfo> result = new ArrayList<>();

        IntHolder idx = new IntHolder();
        listAllMethods(
                model.getPackageManager(),
                (IMetaMember x) -> {
                    result.add(
                            new Context.CompilationInfo(
                                    x.getFullyQualifiedMemberName(),
                                    idx.inc(),
                                    x.getCompilations())
                    );
                    return true;
                }
        );

        return result;
    }

    private CfGraph selectedGraph = null;

    public CfGraph getSelectedGraph() {
        return selectedGraph;
    }

    public boolean selectCompilation(int methodIdx, int compilationIdx) {
        IntHolder holder = new IntHolder();
        return listAllMethods(
                model.getPackageManager(),
                (IMetaMember mm) -> {
                    int idx = holder.inc();
                    if (idx == methodIdx) {
                        selectCompilation(mm, compilationIdx);
                        return false;
                    }
                    return true;
                }
        );
    }

    private void selectCompilation(IMetaMember mm, int compilationIdx) {
        List<Compilation> mc = mm.getCompilations();
        Compilation cmpl = mc.get(compilationIdx);

        AssemblyBlock blockToAnalyze = null;
        for (AssemblyBlock ab : cmpl.getAssembly().getBlocks()) {
            System.out.println(ab.getTitle());
            if (ab.getTitle().equals("[Entry Point]")) {
                blockToAnalyze = ab;
            }
        }

        if (blockToAnalyze != null) {
            selectedGraph = new CfGraph(blockToAnalyze);
        }
    }

    public static CfgParser parseLog(File log) {


        HotSpotLogParser parser = new HotSpotLogParser(new IJITListener() {
            @Override
            public void handleJITEvent(JITEvent event) {
            }

            @Override
            public void handleReadStart() {
            }

            @Override
            public void handleReadComplete() {
            }
            @Override
            public void handleLogEntry(String entry) {
            }

            @Override
            public void handleErrorEntry(String entry) {
            }
        });
        parser.processLogFile(log, (title, body) -> { System.out.println(title + " -> " + body); });

        CfgParser result = new CfgParser(parser.getModel());

        return result;
    }
}
