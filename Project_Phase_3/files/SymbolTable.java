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

	private int field_offset;										// Field offset counter for class
	private int method_offset;										// Method offset counter for class
	private int has_vtable;											// 1 if class has vtable, 0 if class do not have vtable
    private String class_name;										// Class name
	private List<String> parent_classes;							// Contains parent class names of class 
    private LinkedHashMap<String,String> class_fields;				// Contains fields of class
	private LinkedHashMap<String,Method> class_methods;				// Contains methods of class
	private LinkedHashMap<String,Integer> field_offsets;			// Contains fields with their offsets
	private LinkedHashMap<String,Integer> method_offsets;			// Contains methods with their offsets

	// Constructor
    public UserDefinedClass( String name ) {
        field_offset = 0;
		method_offset = 0;
		setNameOfClass(name);
        parent_classes = new ArrayList<String>();
        class_fields = new LinkedHashMap<String,String>();
        class_methods = new LinkedHashMap<String,Method>();
		field_offsets = new LinkedHashMap<String,Integer>();
		method_offsets = new LinkedHashMap<String,Integer>();
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

    public void setIfVtableExists( int v ) {
        has_vtable = v;
    }

	// This method adds in a list the parent classes of class
	public void addParentClass( String c_name, List<String> parent_parents) {
		for( String name : parent_parents )
			parent_classes.add(name);

		parent_classes.add(0, c_name);
    }

    public void putNewFieldOffset( String entry, Integer offset ) {
        field_offsets.put(entry,offset);
    }

    public void putNewMethodOffset( String entry, Integer offset ) {
        method_offsets.put(entry,offset);
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

	public int getSpecificFieldOffset( String f ) {
		return field_offsets.get(f);
	}

	public int getSpecificMethodOffset( String m ) {
		return method_offsets.get(m);
	}

    public LinkedHashMap<String,Integer> getFieldOffsets() {
        return field_offsets;
    }

    public LinkedHashMap<String,Integer> getMethodOffsets() {
        return method_offsets;
    }

    public int getIfVtableExists() {
       return has_vtable;
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

	public boolean checkIfMethodOffsetExist(String m) {
		return method_offsets.containsKey(m);
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
				this.putNewFieldOffset( name, field_offset );
				field_offset += 8;
			} else {
				entry = class_name + "." + name;
				this.putNewFieldOffset( name, field_offset );

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
				this.putNewMethodOffset( name, method_offset );
				method_offset += 8;
			}
		}

		this.setFieldOffset(field_offset);
		this.setMethodOffset(method_offset);

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

			offsets = a_class.getFieldOffsets();

			for( String name : offsets.keySet() ) {
				offset = offsets.get(name);
				System.out.println(class_name + "." + name + " : " + offset);
			}

			offsets = a_class.getMethodOffsets();

			for( String name : offsets.keySet() ) {
				offset = offsets.get(name);
				System.out.println(class_name + "." + name + " : " + offset);
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

			System.out.println( "field ofs: " + a_class.getFieldOffset() + "\nMethod ofs " + a_class.getMethodOffset());
		}
	}

	
	//----------------------------------------------- New code for exe3 -----------------------------------------------------------------

	// Method for creating string of method for vtable
	public String vtableMethodPrint( String class_name, Method method, String method_name, int flag ) {

		String vtable_string = "", type = null;
		List<String> par_types;

		if( !method_name.equals("main") ) {
			
			if( flag == 1 )
				vtable_string += ", ";

				type = method.getTypeOfMethod();

				if( type.equals("int") )
					vtable_string += "i8* bitcast (i32 (i8*";
				else if( type.equals("boolean") )
					vtable_string += "i8* bitcast (i1 (i8*";
				else if( type.equals("int[]") )
					vtable_string += "i8* bitcast (i32* (i8*";
				else
					vtable_string += "i8* bitcast (i8* (i8*";

			par_types = method.getParametersOfMethod();

			for( String par_type : par_types ) {
				if( par_type.equals("int") )
					vtable_string += ",i32";
				else if( par_type.equals("boolean") )
					vtable_string += ",i1";
				else if( par_type.equals("int[]") )
					vtable_string += ",i32*";
				else
					vtable_string += ",i8*";	
			}

			vtable_string += (")* @" + class_name + "." + method_name + " to i8*)");

		}

		return vtable_string;
	}

	// Method that creates string for vtable for every class
	public String vtablePrint( String class_name, int flag ) {

		int i = 0, flag2 = 0, size = 0;
		String vtable_string = null, parent_name;
		List<String> parent_classes, all_methods;
		Method method;
		UserDefinedClass a_class, parent_class;
		LinkedHashMap<String,Method> class_methods;
		LinkedHashMap<String,Integer> parent_method_offsets, method_offsets;

		a_class = user_defined_classes.get(class_name);
		parent_classes = a_class.getParentClassesOfClass();
		class_methods = a_class.getMethodsOfClass();
		method_offsets = a_class.getMethodOffsets();
		size = method_offsets.size();

		// Helping array to keep all names of methods of a class( +inherited methods )
		// one time each method name
		all_methods = new ArrayList<String>();

		for( i = parent_classes.size()-1; i  >= 0; i-- ) {
			parent_name = parent_classes.get(i);
			parent_class = user_defined_classes.get(parent_name);
			parent_method_offsets = parent_class.getMethodOffsets();

			for( String meth_name : parent_method_offsets.keySet() ) {
				all_methods.add(meth_name);
			}
	
			size += parent_method_offsets.size();
		}

		// If size is 0 no vtable for class
		if( size == 0 ) {
			a_class.setIfVtableExists(0);
		} else {
			a_class.setIfVtableExists(1);
		}

		for( String meth_name : method_offsets.keySet() ) {
			all_methods.add(meth_name);
		}

		vtable_string = "@." + class_name + "_vtable = global [" + size + " x i8*] [";

		flag2 = 0;

		// For all the methods of class ( +inherited methods ), check first if method name is in current class and
		// if not found check to parent_class and if not found check in parent-parent class, ...
		for( String meth_name : all_methods ) {
			//System.out.println(meth_name);
			class_methods = a_class.getMethodsOfClass();
			if( class_methods.containsKey(meth_name) ) {
				method = class_methods.get(meth_name);
				vtable_string += vtableMethodPrint( class_name, method, meth_name, flag2 );
			} else {

				for( String p_name : parent_classes ) {

					parent_class = user_defined_classes.get(p_name);
					class_methods = parent_class.getMethodsOfClass();

					if( class_methods.containsKey(meth_name) ) {
						method = class_methods.get(meth_name);
						vtable_string += vtableMethodPrint( p_name, method, meth_name, flag2 );
						break;
					}
				}

			}

			flag2 = 1;
		}

		return vtable_string + "]";
	}

	// load method variable string
	public String loadMethodVariable( Method method, String id_name, String new_var1 ) {

		String type, load_string;

		type = method.getSpecificVarType( id_name );

		if( type.equals("boolean") ) {
			type = "i1";
		} else if( type.equals("int") ) {
			type = "i32";
		} else if( type.equals("int[]") ) {
			type = "i32*";
		} else {
			type = "i8*";
		}

		load_string = "\t" + new_var1 + " = load " + type + ", " + type + "* %" + id_name;

		return load_string;

	}

	public String getFieldVariable( UserDefinedClass a_class, String id_name, String new_var1, String new_var2 ) {
		int offset = 0;
		String type = null, get_string;
		List<String> parent_classes;
		UserDefinedClass parent_class;

		// Checks if identifier is declared in current class
		if( a_class.fieldDeclaredInClass( id_name ) ) {

			type = a_class.getSpecificFieldType(id_name);
			offset = a_class.getSpecificFieldOffset(id_name);

		} else {

			parent_classes = a_class.getParentClassesOfClass();

			// Checks if identifier is declared in a parent class of current class
			for( String c_name: parent_classes) {

				parent_class = this.getSpecificUserDefinedClass(c_name);

				if( parent_class.fieldDeclaredInClass( id_name ) ) {
					type = parent_class.getSpecificFieldType(id_name);
					offset = parent_class.getSpecificFieldOffset(id_name);
					break;
				}
			}
		}

		if( type.equals("boolean") ) {
			type = "i1";
		} else if( type.equals("int") ) {
			type = "i32";
		} else if( type.equals("int[]") ) {
			type = "i32*";
		} else {
			type = "i8*";
		}

		if( a_class.getIfVtableExists() == 1 )
			offset += 8;

		get_string = "\t" + new_var1 + " = getelementptr i8, i8* %this, i32 " + offset + "\n";
		get_string += ( "\t" + new_var2 + " = bitcast i8* " + new_var1 + " to " + type + "*\n");

		return get_string;
	}

	public String loadFieldVariable( UserDefinedClass a_class, String id_name, String new_var1, String new_var2, String new_var3 ) {
		int offset = 0;
		String type = null, load_string;
		List<String> parent_classes;
		UserDefinedClass parent_class;

		// Checks if identifier is declared in current class
		if( a_class.fieldDeclaredInClass( id_name ) ) {

			type = a_class.getSpecificFieldType(id_name);
			offset = a_class.getSpecificFieldOffset(id_name);

		} else {

			parent_classes = a_class.getParentClassesOfClass();

			// Checks if identifier is declared in a parent class of current class
			for( String c_name: parent_classes) {

				parent_class = this.getSpecificUserDefinedClass(c_name);

				if( parent_class.fieldDeclaredInClass( id_name ) ) {
					type = parent_class.getSpecificFieldType(id_name);
					offset = parent_class.getSpecificFieldOffset(id_name);
					break;
				}
			}
		}

		if( type.equals("boolean") ) {
			type = "i1";
		} else if( type.equals("int") ) {
			type = "i32";
		} else if( type.equals("int[]") ) {
			type = "i32*";
		} else {
			type = "i8*";
		}

		if( a_class.getIfVtableExists() == 1 )
			offset += 8;

		load_string = "\t" + new_var1 + " = getelementptr i8, i8* %this, i32 " + offset + "\n";
		load_string += ( "\t" + new_var2 + " = bitcast i8* " + new_var1 + " to " + type + "*\n");
		load_string += ("\t" + new_var3 + " = load " + type + ", " + type + "* " + new_var2);

		return load_string;
	}

	// find size of vtable
	public int findSizeOfVtable( String class_name ) {
		int size = 0;
		List<String> parent_classes, all_methods;
		UserDefinedClass a_class, parent_class;
		LinkedHashMap<String,Integer> parent_method_offsets, method_offsets;

		a_class = user_defined_classes.get(class_name);
		parent_classes = a_class.getParentClassesOfClass();
		method_offsets = a_class.getMethodOffsets();

		size = method_offsets.size();

		for( String parent_name : parent_classes) {

			parent_class = user_defined_classes.get(parent_name);
			parent_method_offsets = parent_class.getMethodOffsets();
	
			size += parent_method_offsets.size();
		}

		return size;
	}
}
