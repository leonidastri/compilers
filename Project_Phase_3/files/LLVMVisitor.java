import visitor.GJDepthFirst;
import syntaxtree.*;
import java.util.*;
import java.io.*;

public class LLVMVisitor extends GJDepthFirst<String,List<String>> {

	private BufferedWriter writer;
	private SymbolTable table;
	private int current_var;
	private int current_label;
	private Method current_method;
	private UserDefinedClass current_class;

	// get next register as showed in presentation of exercise
	private String newVar() {
		this.current_var++;
		return "%_" + (current_var-1);
	}

	// get new label as showed in presentation of exercise
	private String newLabel(String id) {
		this.current_label++;
		return id + (current_label-1);
	}

	// print string for llvm
	private void emit( String s) {
		try {
			writer.write(s);
			writer.newLine();
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	// Constructor
	public LLVMVisitor( BufferedWriter w, SymbolTable t ) {
		this.writer = w;
		this.table = t;
		this.current_var = 0;
		this.current_label = 0;
		this.current_method = null;
		this.current_class = null;
	}

	/**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
	public String visit(Goal n, List<String> argu) throws Exception {

		int flag = 0;
		String _ret = null, vtable_string = null;
		UserDefinedClass a_class;
		LinkedHashMap<String,UserDefinedClass> user_defined_classes;

		user_defined_classes = table.getUserDefinedClasses();

		// for every class print vtable info
		for( String class_name : user_defined_classes.keySet() ) {

			vtable_string = table.vtablePrint(class_name, flag);
			emit(vtable_string);
			flag = 1;
		}

		// print some useful methods
		emit("\ndeclare i8* @calloc(i32, i32)");
		emit("declare i32 @printf(i8*, ...)");
		emit("declare void @exit(i32)");
		emit("@_cint = constant [4 x i8] c\"%d\\0a\\00\"");
		emit("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n");
		emit("define void @print_int(i32 %i) {");
    	emit("\t%_str = bitcast [4 x i8]* @_cint to i8*");
		emit("\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)");
    	emit("\tret void");
		emit("}\n");

		emit("define void @throw_oob() {");
    	emit("\t%_str = bitcast [15 x i8]* @_cOOB to i8*");
    	emit("\tcall i32 (i8*, ...) @printf(i8* %_str)");
    	emit("\tcall void @exit(i32 1)");
    	emit("\tret void");
		emit("}\n");

		n.f0.accept(this, argu);
		n.f1.accept(this, argu);

		return _ret;
	}

	/**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
	public String visit(MainClass n, List<String> argu) throws Exception {
		String _ret = null, class_name, method_name;

		class_name = n.f1.f0.toString();
		method_name = n.f6.toString();

		// keep current class and current method until we do not need them
		current_class = table.getSpecificUserDefinedClass(class_name);
		current_method = current_class.getSpecificMethodOfClass(method_name);

		emit("define i32 @main() {");

		n.f1.accept(this, argu);
		n.f8.accept(this, argu);

		n.f11.accept(this, argu);

		n.f14.accept(this, argu);
		n.f15.accept(this, argu);

		emit("\tret i32 0\n}\n");

		// we exit class so we do not need current class and current method
		current_class = null;
		current_method = null;

		return _ret;
	}

	/**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
	public String visit(TypeDeclaration n, List<String> argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
	public String visit(ClassDeclaration n, List<String> argu) throws Exception {
		String _ret = null, class_name = null;

		// keep current class until exiting current class
		class_name = n.f1.accept(this, argu);
		current_class = table.getSpecificUserDefinedClass(class_name);

		//n.f3.accept(this, argu);
		n.f4.accept(this, argu);

		current_class = null;

		return _ret;
	}

	/**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
	public String visit(ClassExtendsDeclaration n, List<String> argu) throws Exception {
		String _ret = null, class_name = null;

		// keep current class until exiting current class
		class_name = n.f1.accept(this, argu);
		current_class = table.getSpecificUserDefinedClass(class_name);

		n.f3.accept(this, argu);

		//n.f5.accept(this, argu);

		n.f6.accept(this, argu);

		current_class = null;

		return _ret;
	}

	/**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
	public String visit(VarDeclaration n, List<String> argu) throws Exception {
		String _ret = null, type, identifier;

		type = n.f0.accept(this, argu);
		identifier = n.f1.accept(this, argu);

		// Alloca every variable of method
		emit( "\t%" + identifier + " = alloca " + type );

    	return _ret;
	}

	/**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
	public String visit(MethodDeclaration n, List<String> argu) throws Exception {

		String _ret = null, method_name, method_type, parameter_type, method_define_string, parameter_string, expression;
		List<String> parameters;

		method_type = n.f1.accept(this, argu);
		method_name = n.f2.accept(this, argu);

		// define method of class
		method_define_string = "define " + method_type + " @" + current_class.getNameOfClass() + "." + method_name + "(";
		current_method = current_class.getSpecificMethodOfClass(method_name);

		// keep parameters of method in arraylist
		parameters = new ArrayList<String>();
		n.f4.accept(this, parameters);

		parameter_string = "i8* %this";

		// print parameters
		for( String par : parameters ) {
			parameter_type = current_method.getSpecificVarType( par );

			if( parameter_type.equals("boolean") ) {
				parameter_type = "i1";
			} else if( parameter_type.equals("int") ) {
				parameter_type = "i32";
			} else if( parameter_type.equals("int[]") ) {
				parameter_type = "i32*";
			} else {
				parameter_type = "i8*";
			}
			
			parameter_string += (", " + parameter_type + " %." + par);
		}

		emit(  method_define_string + parameter_string + ") {\n" );

		// aloca parameters and store the values
		for( String par : parameters ) {

			parameter_type = current_method.getSpecificVarType( par );

			if( parameter_type.equals("boolean") ) {
				parameter_type = "i1";
			} else if( parameter_type.equals("int") ) {
				parameter_type = "i32";
			} else if( parameter_type.equals("int[]") ) {
				parameter_type = "i32*";
			} else {
				parameter_type = "i8*";
			}

			emit( "\t%" + par + " = alloca " + parameter_type );
			emit( "\tstore " + parameter_type + "%." + par + ", " + parameter_type + "* %" + par);
		}

		emit("\n");

		n.f7.accept(this, argu);
		n.f8.accept(this, argu);

		expression = n.f10.accept(this, argu);

		// print return expression of method
		emit("\n\tret " + method_type + " " + expression + "\n}\n" );

		current_method = null;

		return _ret;
	}

	/**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
	public String visit(FormalParameterList n, List<String> argu) throws Exception {
		String _ret = null;

		n.f0.accept(this, argu);
		n.f1.accept(this, argu);

		return _ret;
	}

	/**
     * f0 -> Type()
     * f1 -> Identifier()
     */
	public String visit(FormalParameter n, List<String> parameters) throws Exception {
		String _ret = null, identifier;

		n.f0.accept(this, null);
		identifier = n.f1.accept(this, null);

		parameters.add(identifier);

		return _ret;
	}

	/**
     * f0 -> ( FormalParameterTerm() )*
	 */
	public String visit(FormalParameterTail n, List<String> argu) throws Exception {

		return n.f0.accept(this, argu);

	}

	/**
     * f0 -> ","
     * f1 -> FormalParameter()
	 */
	public String visit(FormalParameterTerm n, List<String> argu) throws Exception {

		String _ret = null;

		n.f1.accept(this, argu);

		return _ret;
	}

	/**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
	public String visit(Type n, List<String> argu) throws Exception {

		// if we have identifier return i8* else the specific type
		if( n.f0.which == 3 )
			return "i8*";

		return n.f0.accept(this, argu);
	}

	/**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
	 */
	public String visit(ArrayType n, List<String> argu) throws Exception {
		String type = "i32*";

		return type;
	}

	/**
     * f0 -> "boolean"
     */
	public String visit(BooleanType n, List<String> argu) throws Exception {
		String type = "i1";

		return type;
	}

	/**
     * f0 -> "int"
	 */
	public String visit(IntegerType n, List<String> argu) throws Exception {
		String type = "i32";

		return type;
	}

	/**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
	 */
	public String visit(Statement n, List<String> argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
	public String visit(Block n, List<String> argu) throws Exception {
		String _ret = null;

		n.f1.accept(this, argu);

		return _ret;
	}

	/**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
	public String visit(AssignmentStatement n, List<String> argu) throws Exception {
		String _ret = null, id, id_type, exp, new_var, new_var1, new_var2, get_string;

		id = n.f0.accept(this, argu);

		//System.out.println(id);

		// if variable is in current method
		if( current_method.variableDeclaredInMethod( id ) ) {
			new_var  = "%" + id;
		// if variable is field of current class or field of parent class
		} else {
			new_var1 = newVar();
			new_var2 = newVar();
			get_string = table.getFieldVariable( current_class, id, new_var1, new_var2 );
			emit( get_string );
			new_var = new_var2;
		}

		id_type = current_class.getSpecificIdType( table, current_method, id);

		if( id_type.equals("boolean") ) {
			id_type = "i1";
		} else if( id_type.equals("int") ) {
			id_type = "i32";
		} else if( id_type.equals("int[]") ) {
			id_type = "i32*";
		} else {
			id_type = "i8*";
		}

		exp = n.f2.accept(this, argu);

		// store expression
		emit( "\tstore " + id_type + " " + exp + " , " + id_type + "* " + new_var );

		return _ret;
	}

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public String visit(ArrayAssignmentStatement n, List<String> argu) throws Exception {
		String _ret = null, id, exp1, exp2, load_string;
		String new_var, new_var1, new_var2, new_var3, new_var4, new_var5, new_var6, new_var7, label1, label2, label3, lid = "oob";

		id = n.f0.accept(this, argu);

		// load variable of method
		if( current_method.variableDeclaredInMethod( id ) ) {
			new_var5 = newVar();
			load_string = table.loadMethodVariable( current_method, id, new_var5 );
			emit( load_string );
			new_var = new_var5;
		// load field variable of current class or parent class
		} else {
			new_var5 = newVar();
			new_var6 = newVar();
			new_var7 = newVar();
			load_string = table.loadFieldVariable( current_class, id, new_var5, new_var6, new_var7 );
			emit( load_string );
			new_var = new_var7;
		}

		new_var1 = newVar();
		new_var2 = newVar();
		new_var3 = newVar();
		new_var4 = newVar();
		label1 = newLabel(lid);
		label2 = newLabel(lid);
		label3 = newLabel(lid);

		exp1 = n.f2.accept(this, argu);
		exp2 = n.f5.accept(this, argu);

		emit( "\t" + new_var1 + " = load i32, i32* " + new_var );
		emit( "\t" + new_var2 + " = icmp ult i32 " + exp1 + ", " + new_var1 );
		emit( "\tbr i1 " + new_var2 + ", label %" + label1 + ", label %" + label2 );
		emit( "\n" +label1 + ":\n" );
		emit( "\t" + new_var3 + " = add i32 " + exp1 + ", 1");
		emit( "\t" + new_var4 + " = getelementptr i32, i32* " + new_var + ", i32 " + new_var3 );
		emit( "\tstore i32 " + exp2 + ", i32* " + new_var4 );
		emit( "\tbr label %" + label3 );
		emit( "\n" + label2 + ":\n" );
		emit( "\tcall void @throw_oob()" );
		emit( "\tbr label %" + label3 );
		emit( "\n" + label3 + ":\n" );

		return _ret;
	}

	/**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
	public String visit(IfStatement n, List<String> argu) throws Exception {
		String _ret = null, expression, label1, label2, label3, id = "if";

		label1 = newLabel( id );
		label2 = newLabel( id );
		label3 = newLabel( id );

		expression = n.f2.accept(this, argu);

		emit("\tbr i1 " + expression + ", label %" + label1 + ", label %" + label2);

		emit(label1 + ":\n");
	    n.f4.accept(this, argu);
		emit( "\tbr label %" + label3 );

		emit(label2 + ":\n");
    	n.f6.accept(this, argu);
		emit( "\tbr label %" + label3 );

		emit(label3 + ":\n");

		return _ret;
	}

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
	public String visit(WhileStatement n, List<String> argu) throws Exception {
		String _ret = null, expression, label1, label2, label3, id = "loop";

		label1 = newLabel( id );
		label2 = newLabel( id );
		label3 = newLabel( id );
	
		emit( "\tbr label %" + label1 );
		emit( label1 + ":\n");

		expression = n.f2.accept(this, argu);

		emit("\tbr i1 " + expression + ", label %" + label2 + ", label %" + label3);
		emit( label2 + ":\n" );

		n.f4.accept(this, argu);

		emit( "\tbr label %" + label1 );
		emit( label3 + ":\n" );

		return _ret;
	}

	/**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
	public String visit(PrintStatement n, List<String> argu) throws Exception {

		String _ret = null, expression;

		expression = n.f2.accept(this, argu);
		emit( "\tcall void (i32) @print_int(i32 " + expression + " )" );

		return _ret;
	}

	/**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
	public String visit(Expression n, List<String> argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
	public String visit(AndExpression n, List<String> argu) throws Exception {
		String clause1, clause2, new_var, label1, label2, label3, label4, id = "andclause";

		// use of phi function and not "and" as we were asked to do
		new_var = newVar();
		label1 = newLabel(id);
		label2 = newLabel(id);
		label3 = newLabel(id);
		label4 = newLabel(id);

		emit( "\tbr label %" + label1 + "\n");
		emit( label1 + ":\n");
		emit( "\tbr i1 1, label %" + label2 + ", label %" + label4 );
		emit( label2 + ":\n" );

		clause1 = n.f0.accept(this, argu);
		clause2 = n.f2.accept(this, argu);

		emit( "\tbr label %" + label3 );
		emit( label3 + ":\n" );

		emit( "\tbr label %" + label4 );
		emit( label4 + ":\n" );
		emit( "\t" + new_var + " = phi i1 [" + clause1 + ", %" + label1 + "], [" + clause2 + ", %" + label3 + "]" );

		return new_var;
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
	public String visit(CompareExpression n, List<String> argu) throws Exception {
		String new_var, primary_expression1, primary_expression2;

		new_var = newVar();
		primary_expression1 = n.f0.accept(this, argu);
		primary_expression2 = n.f2.accept(this, argu);

		emit( "\t" + new_var + " = icmp slt i32 " + primary_expression1 + ", " + primary_expression2 );

		return new_var;
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
	 */
	public String visit(PlusExpression n, List<String> argu) throws Exception {

		String new_var, primary_expression1, primary_expression2;

		new_var = newVar();
		primary_expression1 = n.f0.accept(this, argu);
		primary_expression2 = n.f2.accept(this, argu);

		emit( "\t" + new_var + " = add i32 " + primary_expression1 + ", " + primary_expression2 );

		return new_var;
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
	public String visit(MinusExpression n, List<String> argu) throws Exception {

		String new_var, primary_expression1, primary_expression2;

		new_var = newVar();
		primary_expression1 = n.f0.accept(this, argu);
		primary_expression2 = n.f2.accept(this, argu);

		emit( "\t" + new_var + " = sub i32 " + primary_expression1 + ", " + primary_expression2 );

		return new_var;
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
	public String visit(TimesExpression n, List<String> argu) throws Exception {

		String new_var, primary_expression1, primary_expression2;

		new_var = newVar();
		primary_expression1 = n.f0.accept(this, argu);
		primary_expression2 = n.f2.accept(this, argu);

		emit( "\t" + new_var + " = mul i32 " + primary_expression1 + ", " + primary_expression2 );

		return new_var;
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
	public String visit(ArrayLookup n, List<String> argu) throws Exception {
		String _ret = null, p_exp1, p_exp2, new_var1, new_var2, new_var3, new_var4, new_var5, label1, label2, label3, id = "oob";

		new_var1 = newVar();
		new_var2 = newVar();
		new_var3 = newVar();
		new_var4 = newVar();
		new_var5 = newVar();
		label1 = newLabel(id);
		label2 = newLabel(id);
		label3 = newLabel(id);

		p_exp1 = n.f0.accept(this, argu);
		p_exp2 = n.f2.accept(this, argu);

		emit( "\t" + new_var1 + " = load i32, i32* " + p_exp1 );
		emit( "\t" + new_var2 + " = icmp ult i32 " + p_exp2 + ", " + new_var1 );
		emit( "\tbr i1 " + new_var2 + ", label %" + label1 + ", label %" + label2 );
		emit( "\n" + label1 + ":\n" );
		emit( "\t" + new_var3 + " = add i32 " + p_exp2 + ", 1");
		emit( new_var4 + " = getelementptr i32, i32* " + p_exp1 + ", i32 " + new_var3 );
		emit( "\t" + new_var5 + " = load i32 , i32* " + new_var4 );
		emit( "\tbr label %" + label3 );
		emit( "\n" + label2 + ":\n" );
		emit( "\tcall void @throw_oob()" );
		emit( "\tbr label %" + label3 );
		emit( "\n" + label3 + ":\n" );

		return new_var5;
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
	public String visit(ArrayLength n, List<String> argu) throws Exception {
		String _ret = null, new_var1, new_var2, addr;

		addr = n.f0.accept(this, argu);

		new_var1 = newVar();
		emit("\t" + new_var1 + " = getelementptr i32, i32* " + addr + ", i32 0");
	
		new_var2 = newVar();
		emit("\t" + new_var2 + " = load i32, i32* " + new_var1);

		return new_var2;
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
	public String visit(MessageSend n, List<String> par) throws Exception {
		int method_offset = 0, i;
		String _ret = null, p_exp, id, s1 = "", s2 = "", type, par_type, return_type = "";
		String new_var1, new_var2, new_var3, new_var4, new_var5, new_var6;
		List<String> parameters, parent_classes, parameter_types = null;
		Method method;
		UserDefinedClass a_class, parent_class;
		new_var1 = newVar();
		new_var2 = newVar();
		new_var3 = newVar();
		new_var4 = newVar();
		new_var5 = newVar();
		new_var6 = newVar();


		parameters = new ArrayList<String>();
		p_exp = n.f0.accept(this, parameters);

		//System.out.println("AAA" + parameters.get(0));

		// from first place of array take the type of primary expression
		if( table.declaredClass( parameters.get(0) ) ) {
			type = parameters.get(0);
		} else {
			type = current_class.getSpecificIdType( table, current_method, parameters.get(0) );
		}

		// get class of primary expression to find method
		a_class = table.getSpecificUserDefinedClass(type);

		id = n.f2.accept(this, null);

		// if method is in this specific class then take necessary info
		if( a_class.methodDeclaredInClass(id) && a_class.checkIfMethodOffsetExist(id) ) {
			method_offset = a_class.getSpecificMethodOffset(id);
			method = a_class.getSpecificMethodOfClass(id);
			return_type = method.getTypeOfMethod();
			parameter_types = method.getParametersOfMethod();
		} else {
			// check if method exists in parent class
			parent_classes = a_class.getParentClassesOfClass();

			for( String parent_name : parent_classes ) {
				parent_class = table.getSpecificUserDefinedClass(parent_name);
				// if exists in current parent then take the neccessary info
				if( parent_class.checkIfMethodOffsetExist(id) ) {
					method_offset = parent_class.getSpecificMethodOffset(id);
					method = parent_class.getSpecificMethodOfClass(id);
					return_type = method.getTypeOfMethod();
					parameter_types = method.getParametersOfMethod();
					break;
				}
			}
		}

		n.f4.accept(this, parameters);

		for( i = 0; i < parameter_types.size(); i++ ) {
			//System.out.println(parameter_types.get(i) + " " +parameters.get(i+1));
			par_type = parameter_types.get(i);

			if( par_type.equals("boolean") ) {
				par_type = "i1";
			} else if( par_type.equals("int") ) {
				par_type = "i32";
			} else if( par_type.equals("int[]") ) {
				par_type = "i32*";
			} else {
				par_type = "i8*";
			}

			s1 += ( ", " + par_type );
			s2 += ( ", " + par_type + " " + parameters.get(i+1) );
		}

 		if ( par != null )
			par.add(return_type);

		if( return_type.equals("boolean") ) {
			return_type = "i1";
		} else if( return_type.equals("int") ) {
			return_type = "i32";
		} else if( return_type.equals("int[]") ) {
			return_type = "i32*";
		} else {
			return_type = "i8*";
		}

		// mmethod call
		emit( "\t" + new_var1 + " = bitcast i8* " + p_exp + " to i8***" );
		emit( "\t" + new_var2 + " = load i8**, i8*** " + new_var1 );
		emit( "\t" + new_var3 + " = getelementptr i8*, i8** " + new_var2 + ", i32 " + method_offset/8 );
		emit( "\t" + new_var4 + " = load i8*, i8** " + new_var3 );
		emit( "\t" + new_var5 + " = bitcast i8* " + new_var4 + " to " + return_type + " (i8* " + s1 + ")*" );
		emit( "\t" + new_var6 + " = call " + return_type + " " + new_var5 + " (i8* " + p_exp + s2 + ")" );



		return new_var6;
	}

	/**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
	public String visit(ExpressionList n, List<String> parameters) throws Exception {
		String _ret = null, exp;

		exp = n.f0.accept(this, null);
		parameters.add(exp);
		n.f1.accept(this, parameters);

		return _ret;
	}

	/**
     * f0 -> ( ExpressionTerm() )*
     */
	public String visit(ExpressionTail n, List<String> argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
     * f0 -> ","
     * f1 -> Expression()
	 */
	public String visit(ExpressionTerm n, List<String> parameters) throws Exception {
		String _ret = null, exp;

		exp = n.f1.accept(this, null);
		parameters.add(exp);

		return _ret;
	}

	/**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
	public String visit(Clause n, List<String> argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
	public String visit(PrimaryExpression n, List<String> parameters) throws Exception {

		String p_exp, p_exp_var, new_var1, new_var2, new_var3, load_string;

		p_exp = n.f0.accept(this,parameters);

		// if we have identifier load it and keep name if necessary
		if( n.f0.which == 3 ) {
			if( parameters != null ) {
				parameters.add(p_exp);
			}
			if( current_method.variableDeclaredInMethod( p_exp ) ) {
				new_var1 = newVar();
				load_string = table.loadMethodVariable( current_method, p_exp, new_var1 );
				emit( load_string );
				return new_var1;
			} else {
				new_var1 = newVar();
				new_var2 = newVar();
				new_var3 = newVar();
				load_string = table.loadFieldVariable( current_class, p_exp, new_var1, new_var2, new_var3 );
				emit( load_string );
				return new_var3;
			}
		}

		return p_exp;
	}

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, List<String> argu) throws Exception {

		String new_var = newVar();

		emit( "\t" + new_var + " = add i32 0, " + n.f0.toString() );

		return new_var;
    }

	/**
     * f0 -> "true"
     */
	public String visit(TrueLiteral n, List<String> argu) throws Exception {
		return "1";
	}

	/**
     * f0 -> "false"
     */
	public String visit(FalseLiteral n, List<String> argu) throws Exception {
		return "0";
	}

	/**
     * f0 -> <IDENTIFIER>
     */
	public String visit(Identifier n, List<String> argu) throws Exception {

		return n.f0.toString();
	}

	/**
     * f0 -> "this"
     */
	public String visit(ThisExpression n, List<String> parameters) throws Exception {

		// keep in first place of array the class name if its necessary
		if( parameters != null ) {
			parameters.add(current_class.getNameOfClass());
		}

		return "%this";
	}

	/**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
	public String visit(ArrayAllocationExpression n, List<String> argu) throws Exception {
		String _ret = null, array_size, label1, label2, id = "arr_alloc", new_var1, new_var2, new_var3, new_var4;

		label1 = newLabel(id);
		label2 = newLabel(id);
		new_var1 = newVar();
		new_var2 = newVar();
		new_var3 = newVar();
		new_var4 = newVar();

		array_size = n.f3.accept(this, argu);

		// creates array and checks for out of bounds too the expression
		emit( "\t" + new_var1 + " = icmp slt i32 " + array_size + ", 0");
		emit( "\t br i1 " + new_var1 + ", label %" + label1 + ", label %" + label2);
		emit( label1 + ":\n" );
		emit( "\tcall void @throw_oob()" );
		emit( "\tbr label %" + label2);
		emit( label2 + ":\n" );
		emit( "\t" + new_var2 + " = add i32 " + array_size + ", 1");
		emit( "\t" + new_var3 + " = call i8* @calloc(i32 4, i32 " + new_var2 +")" );
		emit( "\t" + new_var4 + " = bitcast i8* " + new_var3 + " to i32*");
		emit( "\tstore i32 " + array_size + ", i32* " + new_var4);

		return new_var4;
	}

	/**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
	public String visit(AllocationExpression n, List<String> parameters) throws Exception {
		int size, field_offset, object_size;
		String _ret = null, new_var1, new_var2, new_var3, class_name;
		UserDefinedClass a_class;

		new_var1 = newVar();
		new_var2 = newVar();
		new_var3 = newVar();

		class_name = n.f1.accept(this, null);

		// keep in first position of array the name of class if its necessary
		if( parameters != null ) {
			parameters.add(class_name);
		}

		// size of vtable 
		size = table.findSizeOfVtable( class_name );

		a_class = table.getSpecificUserDefinedClass(class_name);

		// get field_offset counter to calloc object
		field_offset = a_class.getFieldOffset();

		object_size = field_offset;

		// if vtable exists add +8 for vtable
		if( a_class.getIfVtableExists() == 1 )
			object_size += 8;

		emit( "\t" + new_var1 + " = call i8* @calloc(i32 1, i32 " + object_size + ")" );
		emit( "\t" + new_var2 + " = bitcast i8* " + new_var1 + " to i8***" );
		emit( "\t" + new_var3 + " = getelementptr [" + size + " x i8*], [" + size + " x i8*]* @." + class_name + "_vtable, i32 0, i32 0" );
		emit( "\tstore i8** " + new_var3 + ", i8*** " + new_var2 );

		return new_var1;
	}

	/**
     * f0 -> "!"
     * f1 -> Clause()
     */
	public String visit(NotExpression n, List<String> argu) throws Exception {
		String new_var, clause;

		new_var = newVar();

		clause = n.f1.accept(this, argu);

		emit( "\t" + new_var +" = xor i1 1, " + clause );

		return new_var;
	}

	/**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
	public String visit(BracketExpression n, List<String> argu) throws Exception {
		return n.f1.accept(this, argu);
	}

}
