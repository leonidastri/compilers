# compilers
Compilers course, Fall semester 2017


## About

## Project Phase 1

In this phase the task is to implement an LL(1) Calculator Parser(Part 1) and a translator to Java(Part 2).

### Part 1

Implemented a recursive descent parser in Java that reads expressions and computes the values or prints "parse error" if there is a syntax error.The calculator accepts expressions with addition, subtraction, multiplication, and division operators, as well as parentheses.

### Part 2

Implemented a parser and translator for a language supporting string operations. The language supports the concatenation operator over strings, function definitions and calls, conditionals (if-else i.e, every "if" must be followed by an "else"), and the following logical expressions:

    string equality (string1 = string2): Whether string1 is equal to string2.
    is-substring-of (string1 in string2): Whether string1 is a substring of/is contained in string2.

All values in the language are strings.

The parser, based on a context-free grammar, translates the input language into Java. Used JavaCUP for the generation of the parser combined with a generated-one lexer,JFlex.

The output language is a subset of Java so it can be compiled using the "javac" command and executed using the "java" command if we want to test our output.
We can assume that the program input will always be semantically correct.

## Project Phase 2 - MiniJava Static Checking (Semantic Analysis)

Wrote a program to do semantic analysis for MiniJava, a subset of Java, using the MiniJava grammar in JavaCC form and the JTB tool to convert it into a grammar that produces class hierarchies in order to write one or more visitors who will take control over the MiniJava input file and will tell whether it is semantically correct, or will print an error message.

## Project Phase 3 -  Generating intermediate code (MiniJava -> LLVM)

In this part of the project implemented visitors that convert MiniJava code into the intermediate representation used by the LLVM compiler project. The MiniJava language is the same as in the previous part. The LLVM language is documented in the [LLVM Language Reference Manual](https://llvm.org/docs/LangRef.html#instruction-reference), although used only a subset of the instructions.
