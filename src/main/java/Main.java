import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class Main{

	public static void main( String[] args ) throws IOException {
		String file ="/home/sem/MT/Mt3/src/test/test.txt";
		String content = new Scanner(new File(file)).useDelimiter("\\Z").next();
		var lexer = new SampleLexer( CharStreams.fromString(content) );
		TokenStream tokens = new CommonTokenStream(lexer);
		SampleParser parser = new SampleParser(tokens);
		SampleVisitor<String> v = new MySampleVisitor();
		System.out.println(v.visitText(parser.text()));
	}
}