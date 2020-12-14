import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/*
    handles write and append operations to a file.
    If a file does not exist, the file is created.
 */
public class OutputFile {
    String filePath;

    public OutputFile(String path) {
        this.filePath = path;
    }

    // write string to a file
    public void write(String str) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath));
            writer.write(str);
            writer.close();
        }
        catch (IOException e) {
            System.out.println(e);
            e.printStackTrace (System.out);
        }
    }

    // appends string to a file
    public void append(String str){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath, true));
            writer.append(str);
            writer.close();
        }
        catch (IOException e) {
            System.out.println(e);
            e.printStackTrace (System.out);
        }
    }

    // prepend string to a file
    public void prepend(String str) {
        try {
            String content = Files.readString(Path.of(this.filePath));
            this.write(String.format("%s\n%s", str, content));
        }
        catch (IOException e) {
            System.out.println(e);
            e.printStackTrace (System.out);
        }
    }

}
