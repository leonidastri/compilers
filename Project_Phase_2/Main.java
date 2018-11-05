import syntaxtree.*;
import visitor.*;
import java.io.*;

class Main {

	public static void main (String [] args) {

		if(args.length < 1){
		    System.err.println("Usage: java Driver <inputFile>");
		    System.exit(1);
		}

		FileInputStream fis = null;

		// Check every file given
		for( String file : args ) {
			try{

				System.out.println("File: " + file);

				SymbolTable table = new SymbolTable();

				fis = new FileInputStream(file);
				MiniJavaParser parser = new MiniJavaParser(fis);

				Goal root = parser.Goal();
				System.err.println("Program parsed successfully.");

				// type-check: if error occures, a message is printed to help recognize it
				try {
					VisitorI vI = new VisitorI(table);
					root.accept(vI, null);
					VisitorII vII = new VisitorII(table);
					root.accept(vII, null);
					System.err.println("Program type-checked successfully.");
					System.err.println("Program offset info:");
					// finds and stores offsets of fields and methods of every class 
					table.findOffsets();
					// for all classes, it prints offsets
 					table.printOffsets();
				} catch(Exception ex) {
					System.out.println(ex.getMessage());
				}
		
			} catch(ParseException ex) {
				System.out.println(ex.getMessage());

			} catch(FileNotFoundException ex){
				System.err.println(ex.getMessage());
			} finally {
				try {
					if(fis != null) fis.close();
				} catch(IOException ex){
					System.err.println(ex.getMessage());
				}
			}
		}
    }
}
