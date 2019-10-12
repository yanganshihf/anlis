import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Test {
    public static void main(String[] args){
        try {
            String s = args[0];
            String py = args[1];
            String shp = args[2];
            String[] cmd = new String[] { s, py, shp };
            Process p = Runtime.getRuntime().exec(cmd);
            //取得命令结果的输出流    
            InputStream fis = p.getInputStream();
            //用一个读输出流类去读    
            InputStreamReader isr = new InputStreamReader(fis);
            //用缓冲器读行    
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            //直到读完为止    
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            } 
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
