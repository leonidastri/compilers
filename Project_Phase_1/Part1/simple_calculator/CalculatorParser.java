import java.io.InputStream;
import java.io.IOException;

public class CalculatorParser {

    private int lookaheadToken;
    private InputStream in;

    private int evalDigit(int digit){
        return digit - '0';
    }

    public CalculatorParser(InputStream in) throws IOException {
        this.in = in;
        lookaheadToken = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookaheadToken != symbol) {
            throw new ParseError();
        }
        lookaheadToken = in.read();
    }

    private int Exp() throws IOException, ParseError {

        int val1;

        if( lookaheadToken < '0' || lookaheadToken > '9' )
            if( lookaheadToken != '(') {
                throw new ParseError();
            }
        val1 = Term();
        return Exp2(val1);
    }

    private int Exp2(int val1) throws IOException, ParseError {

        int val2, val3;

        if( lookaheadToken == '+' ) {
            consume('+');
            val2 = Term();
            val2 = val1+val2;
            val3 = Exp2(val2);
            return val3;
        } else if( lookaheadToken == '-') {
            consume('-');
            val2 = Term();
            val2 = val1-val2;
            val3 = Exp2(val2);
            return val3;
        } else if( lookaheadToken == ')' || lookaheadToken == -1 || lookaheadToken == '\n') {
            return val1;
        } else {
            throw new ParseError();
        }
    }
    private int Term() throws IOException, ParseError {

        int val1;

        if( lookaheadToken < '0' || lookaheadToken > '9' )
            if( lookaheadToken != '(') {
                throw new ParseError();
            }
        val1 = Factor();
        return Term2(val1);
    }

    private int Term2(int val1) throws IOException, ParseError {

        int val2, val3;

        if( lookaheadToken == '+' || lookaheadToken == '-' || lookaheadToken == ')' || lookaheadToken == -1 || lookaheadToken == '\n') {
            return val1;
        } else if( lookaheadToken == '*') {
            consume('*');
            val2 = Factor();
            val2 = val1 * val2;
            val3 = Term2(val2);
            return val3;
        } else if( lookaheadToken == '/') {
            consume('/');
            val2 = Factor();
            val2 = val1 / val2;
            val3 = Term2(val2);
            return val3;
        } else {
            throw new ParseError();
        }
    }

    private int Factor() throws IOException, ParseError {

        int val;

        if( lookaheadToken >= '0' && lookaheadToken <= '9') {
            val = evalDigit(lookaheadToken);
            consume(lookaheadToken);
            return val;
        } else if( lookaheadToken == '(') {
            consume(lookaheadToken);
            val = Exp();
            consume(')');
            return val;
        } else {
            throw new ParseError();
        }
    }

    public int parse() throws IOException, ParseError {

        int val = Exp();

        if(lookaheadToken != '\n' && lookaheadToken != -1) {
            throw new ParseError();
        } else {
            return val;
        }
    }

    public static void main(String[] args) {
        try {
            CalculatorParser parser = new CalculatorParser(System.in);
            System.out.println( parser.parse());
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        catch(ParseError err){
            System.err.println(err.getMessage());
        }
    }

}

