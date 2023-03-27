package xyz.shxiaj.pso;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/26 13:40
 */
class ScriptOperation {
    public static final String BASH = "bash";
    public static final String SCRIPTFILE = "psoem.sh";
    public static final String LOGDIR = "./runLog/%d.log";
    public static final String ENEDAT = "./part/p%d/ene.dat";

    public static Process runEm(double[] variable, int id) throws IOException {
        List<String> scriptArgs = new ArrayList<>();
        scriptArgs.add(BASH);
        scriptArgs.add(SCRIPTFILE);
        for (double var : variable) {
            scriptArgs.add(String.valueOf(var));
        }
        scriptArgs.add(String.valueOf(id));
        File logFile = new File(String.format(LOGDIR, id));
        ProcessBuilder processBuilder = new ProcessBuilder(scriptArgs);
        processBuilder.redirectOutput(logFile);
        // processBuilder.redirectInput(logFile);
        processBuilder.redirectError(logFile);
        return processBuilder.start();
    }

    public static double readDat(int id) throws IOException {
        String filePath = String.format(ENEDAT, id);
        String datString = Files.readString(Paths.get(filePath));
        return Double.parseDouble(datString);
    }
}
