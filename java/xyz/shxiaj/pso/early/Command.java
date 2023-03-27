package xyz.shxiaj.pso.early;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author shxiaj.github.io
 * @Date 2022/10/24 19:22
 */
class Command {
    public static void main(String[] args) {
        Command cmd = new Command();
        // String s = cmd.callScript("./1.sh");
        // System.out.println(s);
        // cmd.callScript1();
    }

    // path为shell脚本绝对路径
    public String callScript(String path) {
        String result = null;
        BufferedReader br = null;
        try {
            Process ps = Runtime.getRuntime().exec(path);
            ps.waitFor();

            br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void callScript1() {
        ProcessBuilder pb = new ProcessBuilder();
        List<String> cmd = new ArrayList<>();
        cmd.add("bash");
        cmd.add("./1.sh");
        pb.command(cmd);
        String result = null;
        BufferedReader br = null;
        try {
            Process ps = pb.start();
            ps.waitFor();
            br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(result);
    }
}
