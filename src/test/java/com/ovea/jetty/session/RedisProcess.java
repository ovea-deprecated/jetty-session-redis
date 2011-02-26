package com.ovea.jetty.session;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class RedisProcess {

    ProcessReader reader;

    public void start() throws Exception {
        Process process = new ProcessBuilder("/bin/bash", "-c", "redis/redis-start.sh")
                .redirectErrorStream(true)
                .directory(new File("."))
                .start();
        reader = new ProcessReader(process);
        process.waitFor();
        reader.stop();

        process = new ProcessBuilder("/bin/bash", "-c", "redis/redis-monitor.sh")
                .redirectErrorStream(true)
                .directory(new File("."))
                .start();
        reader = new ProcessReader(process);
    }

    public void stop() throws Exception {
        if (reader != null)
            reader.stop();
        new ProcessBuilder("/bin/bash", "-c", "redis/redis-stop.sh")
                .redirectErrorStream(true)
                .directory(new File("."))
                .start();
    }

    static class ProcessReader {
        final Thread reader;

        ProcessReader(final Process process) {
            reader = new Thread() {
                @Override
                public void run() {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null)
                            System.out.println("[RedisProcess] " + line);

                    } catch (IOException ignored) {
                    } finally {
                        try {
                            reader.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            };
            reader.start();
        }

        void stop() {
            reader.interrupt();
        }
    }
}
