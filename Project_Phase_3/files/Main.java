import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.util.*;

class Main {

	public static void main (String [] args) {

		int pos;
		String name;

		if(args.length < 1){
		    System.err.println("Usage: java Driver <inputFile>");
		    System.exit(1);
		}

		FileInputStream fis = null;
		BufferedWriter writer = null;

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

					// Previous code for exe2
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
					//table.printSymbolTable();
					// New code for exe3
					pos = file.lastIndexOf(".");
					if (pos > 0) {
					    name = file.substring(0, pos);
					    writer = new BufferedWriter( new FileWriter(name+".ll") );

						
						LLVMVisitor vIII = new LLVMVisitor(writer, table);
						root.accept(vIII, null);
					}

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
					if(writer != null ) writer.close();
				} catch(IOException ex){
					System.err.println(ex.getMessage());
				}
			}
		}
    }
}
