import java.util.*;

// Method: class for methods
class Method {

    private String method_name;									// Method name
	private String method_type;									// Method type
    private List<String> method_parameters;						// Contains types of parameters to help check
    private LinkedHashMap<String,String> method_variables;		// Contains variables and parameters

	// Constructor
    public Method( String type, String name ) {
        setNameOfMethod(name);
		setTypeOfMethod(type);
        method_parameters = new ArrayList<String>();
        method_variables = new LinkedHashMap<String,String>();
    }

	// "Set", "Put" methods
    public void setTypeOfMethod( String type ) {
        method_type = type;
    }

    public void setNameOfMethod( String name ) {
        method_name = name;
    }

	public void putParameterOfMethod( String type ) {
        method_parameters.add( type );
    }

    public void putVariableOfMethod( String name, String type ) {
        method_variables.put( name, type );
    }

	// "Get" methods
    public String getTypeOfMethod() {
        return method_type;
    }

    public String getNameOfMethod() {
        return method_name;
    }

    public List<String> getParametersOfMethod() {
        return method_parameters;
    }

    public LinkedHashMap<String,String> getVariablesOfMethod() {
        return method_variables;
    }

	// Helpful methods

	// Checks if variable is in the method
	public boolean variableDeclaredInMethod( String v_name ) {
		return method_variables.containsKey(v_name);
	}

	// Take type of variable/parameter with name v_name, a method has
    public String getSpecificVarType( String v_name ) {
        return method_variables.get(v_name);
    }
}

// UserDefinedClass: class for classes
class UserDefinedClass {

	private int field_offset;
	private int method_offset;
    private String class_name;										// Class name
	private List<String> parent_classes;							// Contains parent class names of class 
    private LinkedHashMap<String,String> class_fields;				// Contains fields of class
	private LinkedHashMap<String,Method> class_methods;				// Contains methods of class
	private LinkedHashMap<String,Integer> offsets;					// Contains fields and methods with their offsets

	// Constructor
    public UserDefinedClass( String name ) {
        field_offset = 0;
		method_offset = 0;
		setNameOfClass(name);
        parent_classes = new ArrayList<String>();
        class_fields = new LinkedHashMap<String,String>();
        class_methods = new LinkedHashMap<String,Method>();
		offsets = new LinkedHashMap<String,Integer>();
    }

	// "Set", "Put" methods
    public void setNameOfClass( String name ) {
        class_name = name;
    }

    public void setFieldOffset( int offset ) {
        field_offset = offset;
    }

    public void setMethodOffset( int offset ) {
        method_offset = offset;
    }

	public void putMethodOfClass( String method_name, Method class_method ) {
        class_methods.put( method_name, class_method );
	}

    public void putFieldOfClass( String name, String type ) {
    	class_fields.put( name, type );
    }

	// This method adds in a list the parent classes of class
	public void addParentClass( String c_name, List<String> parent_parents) {
		for( String name : parent_parents )
			parent_classes.add(name);

		parent_classes.add(0, c_name);
    }

	// this method stores fields and methods of class with their offsets
    public void putNewOffset( String entry, Integer offset ) {
        offsets.put(entry,offset);
    }

	// "Get" methods
    public String getNameOfClass() {
        return class_name;
    }

    public LinkedHashMap<String,Method> getMethodsOfClass() {
        return class_methods;
    }

    public LinkedHashMap<String,String> getFieldsOfClass() {
        return class_fields;
    }

	public List<String> getParentClassesOfClass() {
		return parent_classes;
    }

	public int getFieldOffset() {
		return field_offset;
	}

	public int getMethodOffset() {
		return method_offset;
	}

    public LinkedHashMap<String,Integer> getOffsets() {
        return offsets;
    }

	// Helpful methods

	// Take type of field with name f_name, a class has
	public String getSpecificFieldType( String f_name ) {
        return class_fields.get(f_name);
    }

	// Take method with name m_mane, a class has
	public Method getSpecificMethodOfClass( String m_name ) {
        return class_methods.get(m_name);
    }

	// Checks if field is in class
	public boolean fieldDeclaredInClass( String f_name ) {
		return class_fields.containsKey(f_name);
	}

	// Checks if method is in class
	public boolean methodDeclaredInClass( String m_name ) {
		return class_methods.containsKey(m_name);
	}

	// Returns type of identifier if identifier is in current method,
	// or it is field of current class or if it is field of a parent class of current class
	// If identifier is not found return null
	public String getSpecificIdType( SymbolTable t, Method method, String id_name ) {
		UserDefinedClass a_class;

		// Checks if identifier is declared in current method
		if( method.variableDeclaredInMethod(id_name) ) {
			return method.getSpecificVarType(id_name);
		}

		// Checks if identifier is declared in current class
		if( this.fieldDeclaredInClass( id_name ) ) {
			return this.getSpecificFieldType(id_name);
		}

		// Checks if identifier is declared in a parent class of current class
		for( String c_name: parent_classes) {

			a_class = t.getSpecificUserDefinedClass(c_name);

			if( a_class.fieldDeclaredInClass( id_name ) ) {
				return a_class.getSpecificFieldType(id_name);
			}
		}

		// If identifier is not found
		return null;
	}

	// This method finds offsets of fields and methods of class
	public void fixOffsets( SymbolTable table ) {

		String entry, type, c_name;
		Enumeration names;
		Method method;
		UserDefinedClass a_class;

		// From a the first parent we find, we must get field_offset
		// and method offset. if no parent we use the field_offset and
		// method offset of current class 
		for( int i = 0; i < parent_classes.size(); i ++ ) {
			c_name = parent_classes.get(i);

			a_class = table.getSpecificUserDefinedClass(c_name);
			field_offset = a_class.getFieldOffset();
			method_offset = a_class.getMethodOffset();

			break;

		}

		// Find offsets of fields
		for( String name : class_fields.keySet() ) {
			type = class_fields.get(name);

			if( !type.equals("int") && !type.equals("boolean") && !type.equals("int[]") ) {
				entry = class_name + "." + name;
				this.putNewOffset( entry, field_offset );
				field_offset += 8;
			} else {
				entry = class_name + "." + name;
				this.putNewOffset( entry, field_offset );

				if( type.equals("int") ) {
					field_offset += 4;
				} else if ( type.equals("boolean") ) {
					field_offset += 1;	
				} else {
					field_offset += 8;
				}
			}
		}

		// Find offsets of methods
		for( String name : class_methods.keySet() ) {
			method = class_methods.get(name);

			// Check if method exists in a parent class in order not
			// to find new offset of method
			if( !this.findMethodInParentClasses(table,name) ) {
				entry = class_name + "." + name;
				this.putNewOffset( entry, method_offset );
				method_offset += 8;
			}
		}

		// Update field_offset and method_offset in every parent class
		for( int i = 0; i < parent_classes.size(); i ++ ) {

			c_name = parent_classes.get(i);
			a_class = table.getSpecificUserDefinedClass(c_name);
			a_class.setFieldOffset(field_offset);
			a_class.setMethodOffset(method_offset);
		}

	}

	// Checks if method is declared in a parent class too
	// Be careful: It returns true if method is found in parent class
	// and false if it is not found
	public boolean findMethodInParentClasses( SymbolTable table, String m_name ) {
		String name;
		UserDefinedClass a_class;
		Method a_method;

		for( int i = 0; i < parent_classes.size(); i++ ) {

			name = parent_classes.get(i);
			a_class = table.getSpecificUserDefinedClass(name);

			if( a_class.methodDeclaredInClass( m_name ) ) {
				return true;
			}
		}

		return false;
	}
}

// SymbolTable class for symboltable
class SymbolTable {

	private String main_class_name;												// main class of file
	private LinkedHashMap<String,UserDefinedClass> user_defined_classes;		// Contains classes of file
	private List<String> classes;												// Contains names of classes of file

	// Constructor
    public SymbolTable() {
		main_class_name = null;
        user_defined_classes = new LinkedHashMap<String,UserDefinedClass>();
		classes = new ArrayList<String>();
    }

	// "Set" method
	public void setMainClassName( String class_name ) {
        main_class_name = class_name;
    }

	public void putUserDefinedClass( String class_name, UserDefinedClass user_defined_class ) {
        user_defined_classes.put( class_name, user_defined_class);
		classes.add(class_name);
    }

	// "Get" method
	public String getMainClassName() {
		return main_class_name;
	}

	public LinkedHashMap<String,UserDefinedClass> getUserDefinedClasses() {
		return user_defined_classes;
	}

    public List<String> getClasses() {
        return classes;
    }

	// Helpful methods

	// Take class with name class_name, table has
	public UserDefinedClass getSpecificUserDefinedClass( String class_name) {
		return user_defined_classes.get(class_name);
	}

	// Checks if class is in table
	public boolean declaredClass( String class_name ) {
		return user_defined_classes.containsKey(class_name);
	}

	// Checks if method is declared the same way in parent classes of class
	// by checking the return type and the types of parameters
	// Be careful! Returns true if declaration is wrong and false if it is correct  
	public boolean checkMethodInParentClasses( UserDefinedClass current_class, Method current_method, String m_name ) {
		String type1, type2;
		List<String> parent_classes, par1, par2;
		UserDefinedClass a_class;
		Method a_method;

		parent_classes = current_class.getParentClassesOfClass();

		for( String c_name: parent_classes) {

			a_class = this.getSpecificUserDefinedClass(c_name);

			if( a_class.methodDeclaredInClass( m_name ) ) {

				a_method = a_class.getSpecificMethodOfClass( m_name );

				par1 = current_method.getParametersOfMethod();
				par2 = a_method.getParametersOfMethod();
				type1 = current_method.getTypeOfMethod();
				type2 = a_method.getTypeOfMethod();

				// If declaration is wrong return true
				if( !par1.equals(par2) || !type1.equals(type2) )
					return true;
			}
		}

		// If declaration is correct return false
		return false;
	}

	// Returns method called by the object if method is declared in class of object, or
	// if method is declared in parent class of class of the object,
	// or null if method is not found
	public Method findMethodOfObject( UserDefinedClass a_class, String m_name ) {
		LinkedHashMap<String,Method> methods = a_class.getMethodsOfClass();
		UserDefinedClass another_class;
		List<String> parent_classes;
		Method m;

		// Checks class of object to find method
		if( a_class.methodDeclaredInClass( m_name ) ) {

			return a_class.getSpecificMethodOfClass( m_name );
		}

		parent_classes = a_class.getParentClassesOfClass();

		// Checks parent classes of class of object to find method
		for( String c_name: parent_classes) {

			another_class = this.getSpecificUserDefinedClass(c_name);

			if( another_class.methodDeclaredInClass( m_name ) ) {
				return another_class.getSpecificMethodOfClass( m_name );
			}
		}

		// method is not found
		return null;
	}

	// Checks if id1 is parent class of id2
	public boolean checkForParents( String id1, String id2 ) {
		UserDefinedClass a_class;
		List<String> parents;

		if( id2.equals("int") || id2.equals("boolean") || id2.equals("int[]") )
			return false;

		a_class = this.getSpecificUserDefinedClass(id2);
		parents = a_class.getParentClassesOfClass();

		return parents.contains(id1);
	}

	// Finds and stores for in every class the offsets of fields and methods
	public void findOffsets() {

		String class_name;
		Enumeration class_names;
		UserDefinedClass a_class;


		for( String class_key : user_defined_classes.keySet() ) {

			if( class_key.equals(main_class_name) )
				continue;

			a_class = user_defined_classes.get(class_key);

			a_class.fixOffsets(this);
		}

	}

	// Prints the offsets saved in classes of symboltable
	void printOffsets() {
		int offset;
		Enumeration class_names, names;
		UserDefinedClass a_class;
		LinkedHashMap<String,Integer> offsets;

		for( String class_name : classes ) {
			a_class = user_defined_classes.get(class_name);

			offsets = a_class.getOffsets();

			for( String name : offsets.keySet() ) {
				offset = offsets.get(name);
				System.out.println(name + " : " + offset);
			}
			
		}
	}

	// Print symboltable in order to find errors when adding classes,methods,variables
	public void printSymbolTable() {
		UserDefinedClass a_class;
		Method m;
		LinkedHashMap<String,String> f;
		LinkedHashMap<String,Method> ms;

		System.out.println( "Printing SymbolTable" );

		for( String class_name : user_defined_classes.keySet() ) {

			a_class = user_defined_classes.get(class_name);

    		System.out.println( "Class: " + class_name );

			f = a_class.getFieldsOfClass();
			ms = a_class.getMethodsOfClass();

			for( String field_name : f.keySet() ) {
				System.out.println("Field: " + f.get(field_name) + " " + field_name );
			}

			for( String method_name : ms.keySet() ) {
				m = ms.get(method_name);
				System.out.println("Method: " + m.getTypeOfMethod() + " " + method_name );
			}
		}
	}

}
