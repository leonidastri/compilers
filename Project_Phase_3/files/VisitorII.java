import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

// We need to pass List<String> argu in order to keep in a list parameters of a method call
// in order to help us when we have other calls as call parameters

public class VisitorII extends GJDepthFirst<String, List<String>> {
	/* Variables we need to keep through visits */
	private SymbolTable table;
	private UserDefinedClass current_class, object_class;
	private Method current_method;
	private String action, declared_method_name;

	public VisitorII( SymbolTable t ) {
		table = t;
		current_class = null;
		object_class = null;
		current_method = null;
		declared_method_name = null;
		action = null;
	}

	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	*/
	public String visit(Goal n, List<String> argu) throws Exception {
		String _ret = null;

		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);

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

		n.f15.accept(this, argu);

		// we exit class so we do not need current class and current method
		current_class = null;
		current_method = null;

		return _ret;
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
		String _ret = null, class_name;

		class_name = n.f1.f0.toString();

		// keep current class until we exit class
		current_class = table.getSpecificUserDefinedClass(class_name);

		n.f3.accept(this, argu);
		n.f4.accept(this, argu);

		// we do not need class anymore
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
		String _ret = null, class_name;

		class_name = n.f1.f0.toString();

		// keep current class until we exit class
		current_class = table.getSpecificUserDefinedClass(class_name);

		n.f5.accept(this, argu);
		n.f6.accept(this, argu);

		// we do not need class anymore
		current_class = null;

		return _ret;
	}

	/**
	 * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
	*/
	public String visit(VarDeclaration n, List<String> argu) throws Exception {
		String return_type = null, var_name, var_type;
		LinkedHashMap<String,String> method_variables, class_fields, parent_class_fields;

		var_type = n.f0.accept(this, argu);

		// now that we have all classes and types we can check if every variable declaration
		// is correct or not correct
		if( !var_type.equals("int") && !var_type.equals("boolean") && !var_type.equals("int[]")
			&& !table.getUserDefinedClasses().containsKey(var_type) )
			throw new Exception("Var type " + var_type + " does not exist");

		return return_type;
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
		String _ret = null, method_name, method_type, method_return_type;

		method_type = n.f1.accept(this, argu);

		// now that we have all classes and types we can check if every method return type
		// is correct or not correct	
		if( !method_type.equals("int") && !method_type.equals("boolean") && !method_type.equals("int[]")
			&& !table.getUserDefinedClasses().containsKey(method_type) )
			throw new Exception("Method return type " + method_type + " does not exist");

		method_name = n.f2.f0.toString();
		current_method = current_class.getSpecificMethodOfClass( method_name );

    	n.f7.accept(this, argu);
    	n.f8.accept(this, argu);

		// type of expression in return
    	method_return_type = n.f10.accept(this, argu);

		// if expression is class type
		if( !method_return_type.equals("int") && !method_return_type.equals("boolean") && !method_return_type.equals("int[]") ) {

			// if it is the same as return type of method
			if( method_return_type.equals(method_type) ) {
				current_method = null;
				return _ret;
			}

			// Check if return type of method is parent class of return expression
			if( !table.checkForParents( method_type, method_return_type ) )
				throw new Exception( "Method type: " + method_type + " and actual return type: " + method_return_type + " does not match");

		// if expression is known type
		} else {
			if( !method_return_type.equals(method_type) )
				throw new Exception( "Method type: " + method_type + " and actual return type: " + method_return_type + " does not match");
		}

		// we do not need method anymore
		current_method = null;

		return _ret;
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
		String _ret = null, temp;

		// keep current action
		temp = action;

		// change action to statement
		action = "statement";

		_ret = n.f0.accept(this, argu);

		// restore action
		action = temp;

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
		String _ret = null, temp;

		// keep current action
		temp = action;

		// change action to null because we have expression
		action = null;

		_ret = n.f0.accept(this, argu);

		// restore action
		action = temp;

		return _ret;
	}

	/**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
	 * f3 -> ";"
	 */
	public String visit(AssignmentStatement n, List<String> argu) throws Exception {
		String _ret = null, identifier_type, expression_type;

		identifier_type = n.f0.accept(this, argu);
		expression_type = n.f2.accept(this, argu);


		// if types are not the same
		if( !identifier_type.equals(expression_type) ) {
			// if they are class types
			if( !identifier_type.equals("int") && !identifier_type.equals("boolean") && !identifier_type.equals("int[]") ) {
				if( identifier_type.equals(expression_type) )
					return _ret;
				// type of expression must have as parent class the type of identifier
				if( !table.checkForParents(identifier_type,expression_type) )
					throw new Exception("Assignment error, different types " + identifier_type + " and " + expression_type);
			// if they are known types
			} else {
				throw new Exception("Assignment error, different types " + identifier_type + " and " + expression_type);
			}
		}

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
    	String _ret = null, type;

    	type = n.f0.accept(this, argu);

		// type of identifier must be int array
		// type of expression1(index) must be int
		// type of expression2 must be int
		if( !type.equals("int[]") )
			throw new Exception("Array assignnment error, identifier has type " + type + " not an array");

		n.f1.accept(this, argu);
		type = n.f2.accept(this, argu);
		if( !type.equals("int") )
			throw new Exception("Array assignnment error, array index has type " + type + " not an int");

		n.f3.accept(this, argu);
		n.f4.accept(this, argu);

		type = n.f5.accept(this, argu);
		if( !type.equals("int") )
			throw new Exception("Array assignnment error, expression has type " + type + " not an int");

		n.f6.accept(this, argu);

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
		String _ret = null, type;

		type = n.f2.accept(this, argu);

		// if expression must be boolean
		if( !type.equals("boolean") )
			throw new Exception("If expression error, " + type + " not boolean");

		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);

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
		String _ret = null, type;

		n.f0.accept(this, argu);

		type = n.f2.accept(this, argu);

		// while expression must be boolean
		if( !type.equals("boolean") )
			throw new Exception("While expression error, " + type + " not boolean");

		n.f4.accept(this, argu);

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
		String _ret = null, type;

		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		type = n.f2.accept(this, argu);

		// print expression must be int
		if( !type.equals("int") )
			throw new Exception("Printstatement error, " + type + " not int");

		n.f3.accept(this, argu);
		n.f4.accept(this, argu);

		return _ret;
	}

	/**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
	public String visit(AndExpression n, List<String> argu) throws Exception {
		String type;

		type = n.f0.accept(this, argu);

		// clause type myst be boolean
		if( !type.equals("boolean") )
			throw new Exception("Left expression of \"&&\" is " + type + " not boolean");

		type = n.f2.accept(this, argu);

		if( !type.equals("boolean") )
			throw new Exception("Right expression of \"&&\" is " + type + " not boolean");

		return "boolean";
   }

	/**
	 * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
	public String visit(CompareExpression n, List<String> argu) throws Exception {
		String type;

		type = n.f0.accept(this, argu);

		// primary expression must be int
		if( !type.equals("int") )
			throw new Exception("Left expression of \"<\" is " + type + " not int");

		type = n.f2.accept(this, argu);

		if( !type.equals("int") )
			throw new Exception("Right expression of \"<\" is " + type + " not int");

		return "boolean";
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
	public String visit(PlusExpression n, List<String> argu) throws Exception {
		String type;

		type = n.f0.accept(this, argu);

		if( !type.equals("int") )
			throw new Exception("Left expression of \"+\" is " + type + " not int");

		type = n.f2.accept(this, argu);

		if( !type.equals("int") )
			throw new Exception("Right expression of \"+\" is " + type + " not int");

		return "int";
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
	public String visit(MinusExpression n, List<String> argu) throws Exception {
		String type;

		type = n.f0.accept(this, argu);

		if( !type.equals("int") )
			throw new Exception("Left expression of \"-\" is " + type + " not int");

		type = n.f2.accept(this, argu);

		if( !type.equals("int") )
			throw new Exception("Right expression of \"-\" is " + type + " not int");

		return "int";
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
	public String visit(TimesExpression n, List<String> argu) throws Exception {
		String type;

		type = n.f0.accept(this, argu);

		if( !type.equals("int") )
			throw new Exception("Left expression of \"*\" is " + type + " not int");


		type = n.f2.accept(this, argu);

		if( !type.equals("int") )
			throw new Exception("Right expression of \"*\" is " + type + " not int");

		return "int";
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
	 */
	public String visit(ArrayLookup n, List<String> argu) throws Exception {
		String type;

		type = n.f0.accept(this, argu);

		// type must be int array
		if( !type.equals("int[]") )
			throw new Exception("ArrayLookUp error, type is " + type + " not array");

		type = n.f2.accept(this, argu);

		// type of intex must be int
		if( !type.equals("int") )
			throw new Exception("ArrayLookUp error, array index has type " + type + " not int");


		return "int";
	}

	/**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
	 */
	public String visit(ArrayLength n, List<String> argu) throws Exception {
		String type;

		type = n.f0.accept(this, argu);

		// type must be int array
		if( !type.equals("int[]") )
			throw new Exception("ArrayLength error, " + type + " not intArray");

		n.f1.accept(this, argu);
		n.f2.accept(this, argu);

		return "int";
	}

	/**
	 * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
	public String visit(MessageSend n, List<String> argu) throws Exception {
		String _ret = null, c_name, m_name;
		UserDefinedClass a_class;
		Method method;
		List<String> par = new ArrayList<String>();

		c_name = n.f0.accept(this, argu);
    	m_name = n.f2.accept(this, argu);

		if( table.declaredClass( c_name ) ) {
			a_class = table.getSpecificUserDefinedClass( c_name );
		} else {
			throw new Exception("Message send error, class " + c_name + " does not exist");
		}

		method = table.findMethodOfObject(a_class, m_name);

		if( method != null ) {
			par = method.getParametersOfMethod();
			declared_method_name = m_name;
			object_class = a_class;
		} else {
			throw new Exception("Message send error, class " + m_name + " does not exist");
		}

		n.f4.accept(this, par);

		return method.getTypeOfMethod();
   }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
   public String visit(ExpressionList n, List<String> par) throws Exception {
		int i;
		String _ret = null, type, cm_name, temp_declared_method = declared_method_name;		// temp_declared_method: keep method name to check
		UserDefinedClass temp_class = object_class;											// temp_class: keep class of object called method

		List<String> call_method_parameters = new ArrayList<String>();						// list of parameters of call method

		type = n.f0.accept(this, null);

		// add type of parameter
		call_method_parameters.add(type);

		_ret = n.f1.accept(this, call_method_parameters);

		// DEBUG MESSAGE IGNORE
		/*for(String nk : call_method_parameters )
			System.out.println("A" + nk);
		for(String nd : par )
			System.out.println("B" + nd);*/

		// now we have the list of types of parameters and we need to compare them with declared method's list of parameters
		if( call_method_parameters.size() == par.size() ) {

			for( i = 0; i < par.size(); i++ ) {
				cm_name = call_method_parameters.get(i);

				// if type is not known type
				if( !cm_name.equals("int") && !cm_name.equals("boolean") && !cm_name.equals("int[]") ) {

					if( cm_name.equals(par.get(i)) ) {
						return _ret;
					}

					// parameter of call method must have as parent class the type of parameter of declared method
					if( !table.checkForParents( par.get(i), cm_name ) )
						throw new Exception( "Message send error, different type of parameter " + i + " in " + temp_declared_method);

				// if type is known
				} else {
					// they must be equal
					if( !cm_name.equals(par.get(i)) ) {
						throw new Exception( "Message send error, different type of parameter " + i + " in " + temp_declared_method);
					}
				}
			}

		// not the same number of parameters
		} else {
			throw new Exception("Message send error, function " + temp_declared_method + " is differently declared [different number of parameters]");
		}

		return _ret;
	}

	/**
     * f0 -> ( ExpressionTerm() )*
     */
	public String visit(ExpressionTail n, List<String> call_method_parameters) throws Exception {
		return n.f0.accept(this, call_method_parameters);
	}

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public String visit(ExpressionTerm n, List<String> call_method_parameters) throws Exception {
		String _ret = null, type;

		type = n.f1.accept(this,null);

		// add type of parameter
		call_method_parameters.add(type);

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
	public String visit(PrimaryExpression n, List<String> argu) throws Exception {
		String _ret = null, temp;

		// keep previous action
		temp = action;

		// we have primary_expression so action must change
		action = "primary_expression";

		_ret = n.f0.accept(this, argu);

		// restore action
		action = temp;

		return _ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n, List<String> argu) throws Exception {
		return "int";
	}

	/**
	 * f0 -> "true"
	 */
	public String visit(TrueLiteral n, List<String> argu) throws Exception {
		return "boolean";
	}

	/**
	 * f0 -> "false"
	 */
	public String visit(FalseLiteral n, List<String> argu) throws Exception {
		return "boolean";
	}

	/**
	 * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
	public String visit(ArrayAllocationExpression n, List<String> argu) throws Exception {
		String type;

		type = n.f3.accept(this, argu);

		// expression must have type of int
		if( !type.equals("int") )
			throw new Exception("ArrayAllocationExpression error, array index has type " + type + " not int");

		return "int[]";
	}

	/**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
	public String visit(AllocationExpression n, List<String> argu) throws Exception {
		String c_name;

		c_name = n.f1.accept(this, argu);

		// class must be declared
		if( !table.declaredClass(c_name) )
			throw new Exception("AllocationExpression error, identifier " + c_name + " does not exist");

		return c_name;
	}

	/**
	 * f0 -> "this"
	 */
	public String visit(ThisExpression n, List<String> argu) throws Exception {

		// return name of current class 
		return current_class.getNameOfClass();
	}

	/**
	 * f0 -> "!"
	 * f1 -> Clause()
	 */
	public String visit(NotExpression n, List<String> argu) throws Exception {
		String type;

		type = n.f1.accept(this, argu);

		// type must be boolean
		if( !type.equals("boolean") )
			throw new Exception("NotExpression error, type is " + type + " not boolean");

		return "boolean";
	}

	/**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
	public String visit(BracketExpression n, List<String> argu) throws Exception {
		
		return n.f1.accept(this, argu);
	}

    /**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Identifier n, List<String> argu) throws Exception {
		String _ret = null, type;

		n.f0.accept(this, argu);
		_ret = n.f0.toString();

		// if identifier is a class return identifier else check 
		// to return identifier or type
		if( !table.declaredClass(_ret) ) {

			// if action is null( expression), we need identifier
			if( action == null ) {
				return _ret;
			// if action is statement or primary_expression, we need type
			} else if( action.equals("primary_expression") || action.equals("statement") ) {
				type = current_class.getSpecificIdType(table, current_method, _ret);
				if( type != null )
					return type;
				else
					throw new Exception("Identifier " + _ret + " does not exist");
			}
		}

		return _ret;
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
     */
	public String visit(ArrayType n, List<String> argu) throws Exception {
		String _ret = null;

        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
		_ret = n.f0.toString() + n.f1.toString() + n.f2.toString();

        return _ret;
    }

	/**
	 * f0 -> "boolean"
     */
	public String visit(BooleanType n, List<String> argu) throws Exception {
		String _ret = null;

		n.f0.accept(this, argu);
		_ret = n.f0.toString();

		return _ret;
	}

	/**
	 * f0 -> "int"
	 */
	public String visit(IntegerType n, List<String> argu) throws Exception {
		String _ret = null;

		n.f0.accept(this, argu);
		_ret = n.f0.toString();

		return _ret;
	}
}
