import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStreams;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class cal {
    public static void main(String[] args) throws Exception {
        String inputFile = null;

        if (args.length > 0)
            inputFile = args[0];

        InputStream is = System.in;
        if (inputFile != null) is = new FileInputStream(inputFile);
        calLexer lexer = new calLexer(CharStreams.fromStream(is));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        calParser parser = new calParser(tokens);
        parser.removeErrorListeners();

        String fileName = getFileName(inputFile);

        ErrorHandler errorHandler = new ErrorHandler(fileName);
        parser.addErrorListener(errorHandler);
        parser.program();

        if (errorHandler.errorCount <= 0)
            System.out.println(fileName + " parsed successfully");
    }

    private static String getFileName(String inputFile) {
        if (inputFile == null)
            return "input";

        Path path = Paths.get(inputFile);
        return path.getFileName().toString();
    }
}
