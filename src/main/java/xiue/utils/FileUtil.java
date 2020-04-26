package xiue.utils;

import java.io.*;

public class FileUtil {

    public static void write(File file, byte[] data) throws IOException {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream os = null;
        os = new FileOutputStream(file);
        os.write(data);
        os.flush();
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void write(File file, String con) throws IOException {
        write(file, con.getBytes());
    }

    public static String read(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuffer sb = new StringBuffer();
            while (br.ready()) {
                sb.append(br.readLine());
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
