package ru.ms.actors;

import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Subparser;
import ru.ms.profile.__PerfProfile;

/**
 * Created by sergey on 24.11.16.
 */
public class ProfileListActor implements IActor {
    public static void addOptions(Subparser profile) {
        profile.addArgument("--list").action(Arguments.storeTrue()).dest("listEvents");
        profile.addArgument("--top").action(Arguments.storeTrue()).dest("outputTop");
    }

    public static class ProfileListParams {
        @Arg(dest="logFile")
        public String  logFile;

        @Arg(dest="listEvents")
        public boolean listEvents;

        @Arg(dest="outputTop")
        public boolean top;
    }

    @Override
    public void _do(Object settings) throws Exception {
        ProfileListParams params = (ProfileListParams) settings;

//        __PerfProfile profile = new __PerfProfile();
//
//        if (params.listEvents) {
//            System.out.println("Available events:");
//            for (String evt : profile.getAvailableCounters()) {
//                System.out.println(evt);
//            }
//        }
//        if (params.top) {
//            //for (AssemblyBlock ab : cmpl.getAssembly().getBlocks()) {
//            //CfGraph cfg = new CfGraph();
//        }
    }
}
