package xyz.shxiaj.pso.early;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/24 20:33
 */
class CmdTest {

    public static Process callScript(List<String> cmdArgs) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        // List<String> cmdArgs = new ArrayList<>();
        // cmdArgs.add("ls");
        // cmdArgs.add("-a");
        // processBuilder.command(cmdArgs);
        File log = new File("./log.log");
        processBuilder.redirectOutput(log);
        // processBuilder.redirectInput(log);
        processBuilder.redirectError(log);
        Process process = processBuilder.start();
        // String out = IoUtil.read(process.getInputStream(), Charset.defaultCharset());
        // System.out.println(out);
        return process;
    }

    public static void run() {
        String[] s1 = {"bash", "psoem.sh", "10", "30", "40", "0.5", "0.2", "1"};
        String[] s2 = {"bash", "psoem.sh", "20", "30", "40", "0.3", "0.2", "2"};
        String[] s3 = {"bash", "psoem.sh", "30", "30", "40", "0.4", "0.2", "3"};
        String[] s4 = {"ipconfig", "/all"};
        try {
            Process p1 = callScript(List.of(s1));
            // Process p2 = callScript(List.of(s2));
            // Process p3 = callScript(List.of(s3));
            int i = p1.waitFor();
            System.out.println(i);
            // p2.waitFor();
            // p3.waitFor();
            // callScript(List.of(s4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        run();
    }
}
