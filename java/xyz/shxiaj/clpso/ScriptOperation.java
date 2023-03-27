package xyz.shxiaj.clpso;

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
    private static final String BASH = "bash";
    private static final String SCRIPTFILE = "psoem.sh";
    private static final String logDir = "./log";
    private static final String logFilename = "./log/%d.log";
    private static final String eneDatFile = "./part/p%d/ene.dat";
    private static final String dipoleDatFile = "./part/p%d/dipoles.dat";

    public static Process runEm(double[] variable, int id) throws IOException {
        List<String> scriptArgs = new ArrayList<>();
        scriptArgs.add(BASH);
        scriptArgs.add(SCRIPTFILE);
        for (double var : variable) {
            scriptArgs.add(String.valueOf(var));
        }
        scriptArgs.add(String.valueOf(id));
        ProcessBuilder processBuilder = new ProcessBuilder(scriptArgs);

        File file = new File(logDir);
        if (!file.exists()) file.mkdir();
        File logFile = new File(String.format(logFilename, id));

        processBuilder.redirectOutput(logFile);
        processBuilder.redirectError(logFile);
        // processBuilder.redirectInput(logFile);
        return processBuilder.start();
    }

    public static double getEneDat(int id) throws IOException {
        String filePath = String.format(eneDatFile, id);
        String datString = Files.readString(Paths.get(filePath));
        return Double.parseDouble(datString);
    }

    public static double getDipoleDat(int id) throws IOException {
        String filePath = String.format(dipoleDatFile, id);
        String datString = Files.readString(Paths.get(filePath));
        return Double.parseDouble(datString);
    }
}
