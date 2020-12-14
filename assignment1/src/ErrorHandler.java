import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;

public class ErrorHandler implements ANTLRErrorListener {
    private String file;
    public Integer errorCount;

    public ErrorHandler(String file) {
        this.file = file;
        this.errorCount = 0;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        System.out.println(this.file + " has not parsed");
        this.errorCount++;
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

    }
}
