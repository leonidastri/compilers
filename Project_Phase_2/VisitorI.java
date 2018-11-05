import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

/*
	First visitor creates the symbol table and checks for double declarations of classes,  double declarations methods of class,
	double declarations of parameters/variables in methods. Furthermore, checks that a class is declared before it is extended.
	Finally, checks if there are different declarations of a method in child class and all parent classes.
*/

public class VisitorI extends GJDepthFirst<String, String> {

	private SymbolTable table;
	private UserDefinedClass current_class;		// keep class in order to put methods in class
	private Method current_method;				// keep method in order to put vars in method

	public VisitorI( SymbolTable t ) {
		table = t;
		current_class = null;
		current_method = null;
	}

	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */
	public String visit(Goal n, String argu) throws Exception {
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
	public String visit(MainClass n, String argu) throws Exception {

		String _ret = null, class_name, method_name, method_type, var_name, var_type;
		UserDefinedClass new_class;
		Method new_method;

		class_name = n.f1.f0.toString();
		method_type = n.f4.toString()+n.f5.toString();
		method_name = n.f6.toString();

		table.setMainClassName(class_name);

		// new class insert to table
		new_class = new UserDefinedClass(class_name);
		table.putUserDefinedClass( class_name, new_class);
		// new method insert to class
		new_method = new Method( method_type, method_name);
		new_class.putMethodOfClass( method_name, new_method);

		// keep class and method until we finish with this class
		current_method = new_method;
		current_class = new_class;

		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		n.f9.accept(this, argu);
		n.f10.accept(this, argu);
		n.f11.accept(this, argu);
		var_name = n.f11.f0.toString();
		n.f12.accept(this, argu);
		n.f13.accept(this, argu);
		n.f14.accept(this, argu);
		n.f15.accept(this, argu);
		n.f16.accept(this, argu);
		n.f17.accept(this, argu);

		var_type = n.f8.toString() + n.f9.toString() + n.f10.toString();

		// check if parameter of method was declared again inside method 
		if( current_method.variableDeclaredInMethod( var_name ) )
			throw new Exception("Parameter " + var_type + " " + var_name + " has been declared again");

		current_method.putVariableOfMethod( var_name, var_type );

		// we exit the class, we dont need current_class and current_method
		current_method = null;
		current_class = null;

		//System.out.println( "Main class: " + class_name + method_name + method_type + var_name + var_type);

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
	public String visit(ClassDeclaration n, String argu) throws Exception {
		String _ret = null, class_name;
		UserDefinedClass new_class;

		class_name = n.f1.f0.toString();

		// check if class already exists in table
		if( table.declaredClass(class_name) ) {
			throw new Exception("Class " + class_name + " has been declared again");
		}

		// insert in table
		new_class = new UserDefinedClass(class_name);
		table.putUserDefinedClass( class_name, new_class);

		// keep class until we finsih with this class
		current_class = new_class;

		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);

		// we exit the class, we dont need current_class
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
	public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
		String _ret = null, class_name, parent_class_name;
		UserDefinedClass new_class, parent_class;

		class_name = n.f1.f0.toString();
		parent_class_name = n.f3.f0.toString();

		// checks if class already exists in table
		if( table.declaredClass(class_name) ) {
			throw new Exception("Class " + class_name + " has been declared again");
		}

		new_class = new UserDefinedClass(class_name);

		if( parent_class_name != null ) {
			// check if parent class exists in table( parent classes must be defined before "extends")
			if( !table.declaredClass(parent_class_name) ) {
				throw new Exception("Class " + parent_class_name + " to extend has not been declared");
			}

			// add parent class name to the list of parents of new class( helps to keep track of parents of a class)
			parent_class = table.getSpecificUserDefinedClass( parent_class_name );
			new_class.addParentClass( parent_class_name, parent_class.getParentClassesOfClass() );
		}

		// insert in table
		table.putUserDefinedClass( class_name, new_class);

		// keep class until we finsih with this class
		current_class = new_class;

		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);

		// we exit the class, we dont need current_class
		current_class = null;

		//System.out.println("Class: " + class_name + " extends " + parent_class_name);

		return _ret;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public String visit(VarDeclaration n, String argu) throws Exception {
		String _ret = null, var_type, var_name;

		var_type = n.f0.accept(this, argu);
		var_name = n.f1.f0.toString();

		// checks for class fields
		if( current_method == null ) {
			// checks if field already exists in class
			if( current_class.fieldDeclaredInClass(var_name) ) {
				throw new Exception("Field " + var_type + " " + var_name + " has been declared again");
			} else {
				current_class.putFieldOfClass( var_name, var_type );
			}
		// checks for method variables/parameters
		} else {
			// checks if variable/parameter already exists in method
			if( current_method.variableDeclaredInMethod( var_name) ) {
				throw new Exception("Variable " + var_type + " " + var_name + " has been declared again");
			} else {
				current_method.putVariableOfMethod( var_name, var_type );
			}
		}

		n.f1.accept(this, argu);
		n.f2.accept(this, argu);

		//System.out.println("VarDeclaration: " + var_type + " " + var_name);

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
	public String visit(MethodDeclaration n, String argu) throws Exception {

		String _ret = null, method_type, method_name;
		Method new_method;

		method_name = n.f2.f0.toString();

		n.f0.accept(this, argu);
		method_type = n.f1.accept(this, argu);

		// check if method declared again in current class
		if( current_class.methodDeclaredInClass(method_name) ) {
			throw new Exception("Method " + method_name + " has been declared again");
		}

		// insert new method in table
		new_method = new Method( method_type, method_name);
		current_class.putMethodOfClass( method_name, new_method);

		// keep method until we finsih with this method
		current_method = new_method;

		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		n.f7.accept(this, argu);
		n.f8.accept(this, argu);
		n.f9.accept(this, argu);
		n.f10.accept(this, argu);
		n.f11.accept(this, argu);
		n.f12.accept(this, argu);

		// check if parent classes of class have the same function
		if( table.checkMethodInParentClasses( current_class, current_method, method_name) ) {
			throw new Exception("Method " + method_name + " of class " + current_class.getNameOfClass() + " exists in parent class with different return type or different parameters");
		}

		// we exit this method so we do not need it anymore
		current_method = null;

		//System.out.println( "MethodDeclaration: " + method_type + " " + method_name);

		return _ret;
	}

    /**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Identifier n, String argu) throws Exception {
		String _ret = null;

		n.f0.accept(this, argu);
		_ret = n.f0.toString();

		return _ret;
	}

	/**
     * f0 -> Type()
     * f1 -> Identifier()
     */
	public String visit(FormalParameter n, String argu) throws Exception {
		String _ret = null, var_type, var_name;

		var_type = n.f0.accept(this, argu);
		var_name = n.f1.f0.toString();

		// check if parameter declared again in current method
		// Do not need to check if there is a field with same name as there is shadowing
		if( current_method.variableDeclaredInMethod( var_name) ) {
			throw new Exception("Parameter " + var_type + " " + var_name + " has been declared again");
		} else {
			// add to parameter list of method in order to help check methods
			current_method.putParameterOfMethod( var_type );
			current_method.putVariableOfMethod( var_name, var_type );
		}

		n.f1.accept(this, argu);

		//System.out.println("FormalParameter: " + var_type + " " + var_name);

		return _ret;
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
     */
	public String visit(ArrayType n, String argu) throws Exception {
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
	public String visit(BooleanType n, String argu) throws Exception {
		String _ret = null;

		n.f0.accept(this, argu);
		_ret = n.f0.toString();

		return _ret;
	}

	/**
	 * f0 -> "int"
	 */
	public String visit(IntegerType n, String argu) throws Exception {
		String _ret = null;

		n.f0.accept(this, argu);
		_ret = n.f0.toString();

		return _ret;
	}
	
}
