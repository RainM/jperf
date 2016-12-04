package ru.ms.processors;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import ru.ms.Profile;

import java.io.*;
import java.math.BigInteger;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by sergey on 01.12.16.
 */
public class PerfProfileProcessor  {

    public static void processArgParser(ArgumentParser p) {
        //p.addArgument("--");
    }

    /*
            2293703.406052:        427 instructions:      7f577ad5ffaa
            2293703.406053:       7883 instructions:      7f577ad5ffc8
            2293703.406105:     265935 instructions:  ffffffff8110029b
            2293703.406129:          1 cache-misses:  ffffffff8100d430
     */

    public static class ProfileStatistic {
        public final int eventsCount;
        public final Set<String> availableCounters;

        public ProfileStatistic(int eventsCount, Set<String> availableCounters) {
            this.eventsCount = eventsCount;
            this.availableCounters = availableCounters;
        }
    }

    public static ProfileStatistic loadPerfProfile(Profile profile) {
        ProcessBuilder pb = new ProcessBuilder("perf", "script", "--fields", "time,period,ip,event", "-G", "-i", "perf.data");
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);

        int eventsCount = 0;
        Set<String> events = new TreeSet<>();

        try {
            Process p = pb.start();
            BufferedReader br = new BufferedReader( new InputStreamReader(p.getInputStream()));

            OutputStream os = p.getOutputStream();
            InputStream es = p.getErrorStream();

            String line = br.readLine();
            while (line != null) {
                String[] items = line.split(": ");

                if (items.length == 3) {
                    String[] counterAndName = items[1].trim().split(" ");

                    Long cnt = Long.valueOf(counterAndName[0]);
                    String counterName = counterAndName[1];
                    Long addr = (new BigInteger(items[2].trim(), 16)).longValue();

                    // skip kernel events
                    if (addr > 0) {
                        profile.getEventProfile(counterName).addCounter(addr, cnt);

                        events.add(counterName);
                        eventsCount += 1;
                    }
                }

                line = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ProfileStatistic(eventsCount, events);
    }
}
