/* *******************************************************************
 * Copyright (c) 2002,2003 Palo Alto Research Center, Incorporated (PARC).
 *               2013, contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation
 *     Adrian Colyer refactored for use in org.eclipse.jdt.core package 
 * ******************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

// AspectJ Extension - this whole class is an AspectJ extension to the parser
public class Parser extends TheOriginalJDTParserClass {
	
	private static final String ASPECTJ_DECLARATION_FACTORY = "org.aspectj.ajdt.internal.compiler.parser.DeclarationFactory";
	private static IDeclarationFactory declarationFactory;
	
	static {
		try{
			initTables(Parser.class);
			declarationFactory = (IDeclarationFactory) Class.forName(ASPECTJ_DECLARATION_FACTORY).newInstance();
		} catch(java.io.IOException ex){
			throw new ExceptionInInitializerError(ex.getMessage());
		} catch (InstantiationException ex) {
			throw new ExceptionInInitializerError(ex.getMessage());
		} catch (IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex.getMessage());
		} catch (ClassNotFoundException ex) {
			System.err.println("Warning: AspectJ declaration factory class not found on classpath");
			//throw new ExceptionInInitializerError(ex.getMessage());
		}
	}

	public interface IDeclarationFactory {
			MessageSend createProceed(MessageSend m);
			TypeDeclaration createAspect(CompilationResult result);
			void setPrivileged(TypeDeclaration aspectDecl, boolean isPrivileged);
			void setPerClauseFrom(TypeDeclaration aspectDecl, ASTNode pseudoTokens, Parser parser);
			void setDominatesPatternFrom(TypeDeclaration aspectDecl, ASTNode pseudoTokens, Parser parser);
			ASTNode createPseudoTokensFrom(ASTNode[] tokens, CompilationResult result);
			MethodDeclaration createMethodDeclaration(CompilationResult result);
			ConstructorDeclaration createConstructorDeclaration(CompilationResult result);
			MethodDeclaration createPointcutDeclaration(CompilationResult result);
			MethodDeclaration createAroundAdviceDeclaration(CompilationResult result);
			MethodDeclaration createAfterAdviceDeclaration(CompilationResult result);
			MethodDeclaration createBeforeAdviceDeclaration(CompilationResult result);
			ASTNode createPointcutDesignator(Parser parser, ASTNode pseudoTokens);
			void setPointcutDesignatorOnAdvice(MethodDeclaration adviceDecl, ASTNode des);
			void setPointcutDesignatorOnPointcut(MethodDeclaration adviceDecl, ASTNode des);
			void setExtraArgument(MethodDeclaration adviceDeclaration, Argument arg);
			boolean isAfterAdvice(MethodDeclaration adviceDecl);
			void setAfterThrowingAdviceKind(MethodDeclaration adviceDecl);
			void setAfterReturningAdviceKind(MethodDeclaration adviceDecl);
			MethodDeclaration createDeclareDeclaration(CompilationResult result, ASTNode pseudoTokens, Parser parser);
			MethodDeclaration createDeclareAnnotationDeclaration(CompilationResult result, ASTNode pseudoTokens, Annotation annotation, Parser parser,char kind);
			MethodDeclaration createInterTypeFieldDeclaration(CompilationResult result, TypeReference onType);
			MethodDeclaration createInterTypeMethodDeclaration(CompilationResult result);
			MethodDeclaration createInterTypeConstructorDeclaration(CompilationResult result);
			void setSelector(MethodDeclaration interTypeDecl, char[] selector);
			void setDeclaredModifiers(MethodDeclaration interTypeDecl, int modifiers);
			void setInitialization(MethodDeclaration itdFieldDecl, Expression initialization);
			void setOnType(MethodDeclaration interTypeDecl, TypeReference onType);
			ASTNode createPseudoToken(Parser parser, String value, boolean isIdentifier);
			ASTNode createIfPseudoToken(Parser parser, Expression expr);
			void setLiteralKind(ASTNode pseudoToken, String string);
			boolean shouldTryToRecover(ASTNode node);
			TypeDeclaration createIntertypeMemberClassDeclaration(CompilationResult compilationResult);
			void setOnType(TypeDeclaration interTypeDecl, TypeReference onType);
	}
	
//	public final static void initAjTables(Class parserClass)
//		throws java.io.IOException {
//
//		final String prefix = FILEPREFIX;
//		int i = 0;
//		lhsStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		char[] chars = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		check_tableStatic = new short[chars.length];
//		for (int c = chars.length; c-- > 0;) {
//			check_tableStatic[c] = (short) (chars[c] - 32768);
//		}
//		asbStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		asrStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		symbol_indexStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		actionStatic = lhsStatic;
//	}

	//positions , dimensions , .... (int stacks)
	protected int aspectIntPtr;
	protected int[] aspectIntStack;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.TheOriginalJDTParserClass#initialize()
	 */
	public void initialize() {
		super.initialize();
		aspectIntPtr = -1;
		aspectIntStack = new int[4];
	}

	public void initialize(boolean initializeNLS) {
		super.initialize(initializeNLS);
		aspectIntPtr = -1;
		aspectIntStack = new int[4];
	}
	
	public void initializeScanner(){
		this.scanner = new Scanner(
			false /*comment*/, 
			false /*whitespace*/, 
			this.options.getSeverity(CompilerOptions.NonExternalizedString) != ProblemSeverities.Ignore /*nls*/, 
			this.options.sourceLevel /*sourceLevel*/, 
			this.options.taskTags/*taskTags*/,
			this.options.taskPriorities/*taskPriorities*/,
			this.options.isTaskCaseSensitive/*taskCaseSensitive*/);
	}

//*************New display debugging method
	private static final boolean AJ_DEBUG = false;

	void println(Object o) {
		if (AJ_DEBUG) System.out.println(o);
	}

	private void printStack(Object[] s, int p) {
		List list = Arrays.asList(s);
		System.out.println("  " + list.subList(0, p+1));
	}
	
	private void printStack(int[] s, int p) {
		StringBuffer buf = new StringBuffer("[");
		for (int i=0; i<p+1; i++) {
			if (i > 0) buf.append(", ");
			buf.append(Integer.toString(s[i]));
		}
		buf.append("]");
		System.out.println("  " + buf);
	}
			
	private void printStack(long[] s, int p) {
		StringBuffer buf = new StringBuffer("[");
		for (int i=0; i<p+1; i++) {
			if (i > 0) buf.append(", ");
			buf.append(Long.toString(s[i]));
		}
		buf.append("]");
		System.out.println("  " + buf);
	}
			
	private void printStack(char[][] s, int p) {
		StringBuffer buf = new StringBuffer("[");
		for (int i=0; i<p+1; i++) {
			if (i > 0) buf.append(", ");
			buf.append(new String(s[i]));
		}
		buf.append("]");
		System.out.println("  " + buf);
	}
			
	public void display() {
		if (!AJ_DEBUG) return;
		System.out.print("astStack: ");
		printStack(astStack, astPtr);
		System.out.print("astLengthStack: ");
		printStack(astLengthStack, astLengthPtr);
		
		System.out.print("expressionStack: ");
		printStack(expressionStack, expressionPtr);
		System.out.print("expressionLengthStack: ");
		printStack(expressionLengthStack, expressionLengthPtr);

		System.out.print("identifierStack: ");
		printStack(identifierStack, identifierPtr);
		System.out.print("identifierLengthStack: ");
		printStack(identifierLengthStack, identifierLengthPtr);
		System.out.print("identifierPositionStack: ");
		printStack(identifierPositionStack, identifierPtr);

		
		System.out.print("intStack:");
		printStack(intStack, intPtr);
		System.out.println();
	}	



//************** Overriding behavior for standard Java rules
	protected MethodDeclaration createMethodDeclaration(CompilationResult result) {
		return declarationFactory.createMethodDeclaration(result);
	}
	
	protected ConstructorDeclaration createConstructorDeclaration(CompilationResult result) {
		return declarationFactory.createConstructorDeclaration(result);
	}
	
	protected void consumeMethodInvocationName() {
		super.consumeMethodInvocationName();

		MessageSend m = (MessageSend)expressionStack[expressionPtr];
		if (CharOperation.equals(m.selector, "proceed".toCharArray())) {
			expressionStack[expressionPtr] = declarationFactory.createProceed(m);
		}
	}
	
	protected void consumeToken(int type) {
		currentTokenStart = scanner.startPosition;
		super.consumeToken(type);
		switch (type) {
			case TokenNameaspect :  // pseudo keyword
				//aspectIntPtr = -1; //XXX  If we ever see a bug with aspects nested in aspects,
                //                   // this line is the culprit!
				pushOnAspectIntStack(this.scanner.currentPosition - 1);
				pushOnAspectIntStack(this.scanner.startPosition);
				// deliberate fall through...
			case TokenNameprivileged :  // pseudo keyword
			case TokenNamepointcut :  // pseudo keyword
			case TokenNamebefore :  // pseudo keyword
			case TokenNameafter :  // pseudo keyword
			case TokenNamearound :  // pseudo keyword
			case TokenNamedeclare :  // pseudo keyword
				pushIdentifier();
				scanner.commentPtr = -1;
				break;
		}
	}


//************New AspectJ rules	
	protected void consumeAspectDeclaration() {
	    // AspectDeclaration ::= AspectHeader AspectBody
	    consumeClassDeclaration();
	    //??? post parsing step here
	}
	
	protected void consumeAspectHeader() {
	    // AspectHeader ::= AspectHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt AspectHeaderRest
		consumeClassHeader();
	}

	protected void consumeAspectHeaderName(boolean isPrivileged) {
		// (isPrivileged == false) -> AspectHeaderName ::= Modifiersopt 'aspect' 'Identifier'
		// (isPrivileged == true) -> AspectHeaderName ::= Modifiersopt 'privileged' Modifiersopt 'aspect' 'Identifier'
		TypeDeclaration aspectDecl = declarationFactory.createAspect(this.compilationUnit.compilationResult);
		if (this.nestedMethod[this.nestedType] == 0) {
			if (this.nestedType != 0) {
				aspectDecl.bits |= ASTNode.IsMemberType;
			}
		} else {
			// Record that the block has a declaration for local types
			aspectDecl.bits |= ASTNode.IsLocalType;
			markEnclosingMemberWithLocalType();
			blockReal();
		}			

		println("aspect header name: ");
		this.display();

		//highlight the name of the type
		long pos = identifierPositionStack[identifierPtr];
		aspectDecl.sourceEnd = (int) pos;
		aspectDecl.sourceStart = (int) (pos >>> 32);
		aspectDecl.name = identifierStack[identifierPtr--];
		identifierLengthPtr--;

		//compute the declaration source too
		// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
		// we want to keep the beginning position but get rid of the end position
		// it is only used for the ClassLiteralAccess positions.
		aspectDecl.declarationSourceStart = this.aspectIntStack[this.aspectIntPtr--]; 
		this.aspectIntPtr--; // remove the end position of the class token

		// pop the aspect pseudo-token
		eatIdentifier();


		// handle modifiers, only without privileged for now
		if (isPrivileged) {
			pos = eatIdentifier(); // eat the privileged
//			int end = (int) pos;
//		    int start = (int) (pos >>> 32);
		    declarationFactory.setPrivileged(aspectDecl,true);
			//problemReporter().signalError(start, end, "privileged is unimplemented in 1.1alpha1");
		}
		aspectDecl.modifiersSourceStart = intStack[intPtr--];
		aspectDecl.modifiers = intStack[intPtr--];
		if (isPrivileged) {
			aspectDecl.modifiersSourceStart = intStack[intPtr--];
			aspectDecl.modifiers |= intStack[intPtr--];
		}
		if (aspectDecl.modifiersSourceStart >= 0) {
			aspectDecl.declarationSourceStart = aspectDecl.modifiersSourceStart;
		}

		println("modifiers: " + aspectDecl.modifiers);

		// consume annotations
		int length;
		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			System.arraycopy(
				this.expressionStack, 
				(this.expressionPtr -= length) + 1, 
				aspectDecl.annotations = new Annotation[length], 
				0, 
				length); 
		}

		aspectDecl.bodyStart = aspectDecl.sourceEnd + 1;
		pushOnAstStack(aspectDecl);

		listLength = 0; // will be updated when reading super-interfaces
		// recovery
		if (currentElement != null) {
			lastCheckPoint = aspectDecl.bodyStart;
			currentElement = currentElement.add(aspectDecl, 0);
			lastIgnoredToken = -1;
		}

        // Grab the javadoc
        aspectDecl.javadoc = this.javadoc;
        this.javadoc = null;

		this.display();
	}

	protected void consumeAspectHeaderNameWithTypeParameters(boolean isPriviliged) {
		TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];

		// consume type parameters
		int length = this.genericsLengthStack[this.genericsLengthPtr--];
		this.genericsPtr -= length;
		System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeDecl.typeParameters = new TypeParameter[length], 0, length);

		typeDecl.bodyStart = typeDecl.typeParameters[length-1].declarationSourceEnd + 1;
		
		this.listTypeParameterLength = 0;
		
		if (this.currentElement != null) { // is recovering
			this.lastCheckPoint = typeDecl.bodyStart;
		}
	}
	
	private long eatIdentifier() {
		long pos = identifierPositionStack[identifierPtr];
		identifierPtr--;
		identifierLengthPtr--;
		return pos;
	}

	protected void consumeAspectHeaderRest() {
		//--[dominates TypePattern] [persingleton() | percflow(PCD) | perthis(PCD) | pertarget(PCD)]
		//AspectHeaderRest ::= AspectHeaderRestStart PseudoTokens
		concatNodeLists();
		this.display();
		ASTNode pseudoTokens = popPseudoTokens("{");
		println("pseudo: " + pseudoTokens);

		TypeDeclaration aspectDecl = (TypeDeclaration) astStack[astPtr];
		
		declarationFactory.setDominatesPatternFrom(aspectDecl,pseudoTokens,this);
		declarationFactory.setPerClauseFrom(aspectDecl,pseudoTokens,this);
		// XXX handle dominates
	}
	
	
	protected void consumePointcutDeclaration() {
		consumePointcutDesignatorOnDeclaration();
	}
	
	// AspectJ extension - accessor method for the currentTokenStart
	public int getCurrentTokenStart() {
		return currentTokenStart;
	}
	// End AspectJ extension
	
	protected void consumeEmptyPointcutDeclaration() {
		// AspectJ extension - set up some positions, required by AST support
		MethodDeclaration pcutDecl = (MethodDeclaration)astStack[astPtr];
		pcutDecl.bodyEnd = endStatementPosition;
		// End Aspectj Extension
		//??? set pcd to non-null
	}
	
	protected void consumePointcutHeader() {
		//PointcutDeclaration ::= Modifiersopt 'pointcut'  JavaIdentifier '('
		
		MethodDeclaration ret = declarationFactory.createPointcutDeclaration(compilationUnit.compilationResult);
		
		//the name
		long pos = identifierPositionStack[identifierPtr];
//		int sourceEnd = (int) pos;
		ret.sourceStart = (int) (pos >>> 32);
		ret.selector = identifierStack[identifierPtr--];
		identifierLengthPtr--;
        
        // Grab the javadoc
		ret.javadoc = this.javadoc;
        this.javadoc = null;
        
		// pop the 'pointcut' keyword
		eatIdentifier();

		// modifiers
		ret.declarationSourceStart = intStack[intPtr--];
		ret.modifiers = intStack[intPtr--];
		// consume annotations
		int length;
		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			System.arraycopy(
				this.expressionStack, 
				(this.expressionPtr -= length) + 1, 
				ret.annotations = new Annotation[length], 
				0, 
				length); 
		}
		
		pushOnAstStack(ret);
	}
	


	protected void consumeAroundDeclaration() {
		// AroundDeclaration ::= AroundHeader MethodBody
		consumeMethodDeclaration(true);
	}

	protected void consumeAroundHeader() {
		consumePointcutDesignatorOnAdvice();
		resetModifiers(); // forget any modifiers encountered in the pointcut 263666
		consumeMethodHeader();
	}

	protected void consumeAroundHeaderName() {
		// AroundHeaderName ::= Modifiersopt Type  'around' '(' 
		
		MethodDeclaration adviceDecl = declarationFactory.createAroundAdviceDeclaration(compilationUnit.compilationResult);
		
		// skip the name of the advice
		long pos = eatIdentifier();
		adviceDecl.sourceStart = (int) (pos >>> 32);
        
		// but put in a placeholder name
        adviceDecl.selector = new char[] {'a','j','c','$','a','d','v','i','c','e'};

		TypeReference returnType = getTypeReference(intStack[intPtr--]);
		
		//modifiers
		adviceDecl.declarationSourceStart = intStack[intPtr--];
		adviceDecl.modifiers = intStack[intPtr--];

		adviceDecl.returnType = returnType;
		
		// consume annotations
		int length;
		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			System.arraycopy(
				this.expressionStack, 
				(this.expressionPtr -= length) + 1, 
				adviceDecl.annotations = new Annotation[length], 
				0, 
				length); 
		}
        
        // Grab the javadoc
        adviceDecl.javadoc = this.javadoc;
        this.javadoc = null;
		
		//XXX get some locations right
		
		pushOnAstStack(adviceDecl);
	}
	
	protected void consumePointcutDesignatorOnAdvice() {
		ASTNode des = popPointcutDesignator("{");
		MethodDeclaration adviceDecl = (MethodDeclaration)astStack[astPtr];
		declarationFactory.setPointcutDesignatorOnAdvice(adviceDecl,des);
		adviceDecl.sourceEnd = 	des.sourceEnd;
		adviceDecl.bodyStart = des.sourceEnd+1;
	}
	
	protected void consumePointcutDesignatorOnDeclaration() {
		ASTNode des = popPointcutDesignator(";");
		MethodDeclaration pcutDecl = (MethodDeclaration)astStack[astPtr];
		declarationFactory.setPointcutDesignatorOnPointcut(pcutDecl,des);
		pcutDecl.sourceEnd = 	des.sourceEnd;
		pcutDecl.bodyStart = des.sourceEnd+1;
		pcutDecl.bodyEnd = endPosition;
		pcutDecl.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition);
	}
	
	
	protected void consumeBasicAdviceDeclaration() {
		// BasicAdviceDeclaration ::= BasicAdviceHeader MethodBody
		consumeMethodDeclaration(true);
	}

	protected void consumeBasicAdviceHeader() {
		// BasicAdviceHeader ::= BasicAdviceHeaderName MethodHeaderParameters ExtraParamopt MethodHeaderThrowsClauseopt ':' PseudoTokens
		consumePointcutDesignatorOnAdvice();
		resetModifiers(); // forget any modifiers encountered in the pointcut 263666
		consumeMethodHeader();
	}
	
	
	protected void consumeBasicAdviceHeaderName(boolean isAfter) {
		// BasicAdviceHeaderName ::= 'before'|'after '(' 
		
		MethodDeclaration adviceDecl =
			(isAfter ? declarationFactory.createAfterAdviceDeclaration(compilationUnit.compilationResult) :
					  declarationFactory.createBeforeAdviceDeclaration(compilationUnit.compilationResult));
		
        // skip the name of the advice
		long pos = eatIdentifier();
		// but give a placeholder selector name
		adviceDecl.selector = new char[] {'a','j','c','$','a','d','v','i','c','e'};
        adviceDecl.sourceStart = (int) (pos >>> 32);
		
		//modifiers
		adviceDecl.declarationSourceStart = intStack[intPtr--];
		adviceDecl.modifiers = intStack[intPtr--];

		// consume annotations
		int length;
		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			System.arraycopy(
				this.expressionStack, 
				(this.expressionPtr -= length) + 1, 
				adviceDecl.annotations = new Annotation[length], 
				0, 
				length); 
		}

        // Grab the javadoc
        adviceDecl.javadoc = this.javadoc;
        this.javadoc = null;
		
		//??? get more locations right
		
		pushOnAstStack(adviceDecl);
	}
	
	protected void consumeExtraParameterWithFormal() {
		Argument arg = (Argument)astStack[astPtr--];
		astLengthPtr--;
		
		declarationFactory.setExtraArgument((MethodDeclaration)astStack[astPtr],arg);
		
		consumeExtraParameterNoFormal();
	}

	
	protected void consumeExtraParameterNoFormal() {
		
		
	    long pos = identifierPositionStack[identifierPtr];
	    int end = (int) pos;
		int start = (int) (pos >>> 32);
	    char[] name = identifierStack[identifierPtr--];
	    identifierLengthPtr--;
	    
	    //System.out.println("extra parameter: " + new String(name));
	    
	    MethodDeclaration adviceDecl = (MethodDeclaration)astStack[astPtr];
	    if (declarationFactory.isAfterAdvice(adviceDecl)) {
	    	//XXX error, extra param makes no sense here
	    }
	    
	    if (CharOperation.equals(name, "throwing".toCharArray())) {
	    	declarationFactory.setAfterThrowingAdviceKind(adviceDecl);
	    } else if (CharOperation.equals(name, "returning".toCharArray())) {
			declarationFactory.setAfterReturningAdviceKind(adviceDecl);
	    } else {
			problemReporter().parseError(
				start, 
				end, 
				currentToken,
				name, 
				String.valueOf(name), 
				new String[] {"throwing", "returning", ":"}); 
	    }
	}

	protected void consumeClassBodyDeclarationInAspect() { }
	

	protected void consumeDeclareDeclaration() {
		concatNodeLists();
		ASTNode tokens = popPseudoTokens(";");
		MethodDeclaration declareDecl = declarationFactory.createDeclareDeclaration(this.compilationUnit.compilationResult,tokens,this);
//		println("parsed declare: " + declare);
		display();
		pushOnAstStack(declareDecl);
	}


	protected void consumeDeclareAnnotation(char kind) {
		concatNodeLists();
		ASTNode tokens = popPseudoTokens(";");

		int length;
		Annotation[] annotations = new Annotation[1]; // there should only ever be one for us...
    	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
    		System.arraycopy(
    			this.expressionStack, 
    			(this.expressionPtr -= length) + 1, 
    			annotations = new Annotation[length], 
    			0, 
    			length); 
    	}

    	MethodDeclaration declareDecl = declarationFactory.createDeclareAnnotationDeclaration(this.compilationUnit.compilationResult,tokens,annotations[0],this,kind);
    	pushOnAstStack(declareDecl);
	}

	protected void consumeDeclareAnnotationHeader() {
		consumePseudoTokenIdentifier();  // name
		consumePseudoTokenIdentifier();  // declare
		swapAstStack();
		consumePseudoTokens();

		consumePseudoToken("@",0,false);
		swapAstStack();
		consumePseudoTokens();
		
		consumePseudoToken(":", 0, false);
		consumePseudoTokens();

		display();
	}

	protected void consumeDeclareHeader() {
		consumePseudoTokenIdentifier();  // name
		consumePseudoTokenIdentifier();  // declare
		swapAstStack();
		consumePseudoTokens();
		
		consumePseudoToken(":", 0, false);
		consumePseudoTokens();

//		println(">>>>>>>>>>>>>>>>>>>>>>>declare header");
		display();
	}

	protected void consumeInterTypeFieldHeader(boolean hasTypeParameters) {
//		println("about to consume field");
		this.display();

		long pos = identifierPositionStack[identifierPtr];
		int end = (int) pos;
		int start = (int) (pos >>> 32);
		char[] identifierName = identifierStack[identifierPtr--];
//		int extendedDimension = this.intStack[this.intPtr--];  // XXXX see consumeEnterVariable for what to do with this
		identifierLengthPtr--;

		if (hasTypeParameters) {
			pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
		} else {
			consumeClassOrInterfaceName();
		}
		TypeReference onType = getTypeReference(0);


		TypeReference returnType = getTypeReference(intStack[intPtr--]);
		this.display();

		int decSourceStart = intStack[intPtr--];
		int fieldModifiers = intStack[intPtr--];

		MethodDeclaration dec = declarationFactory.createInterTypeFieldDeclaration(
				this.compilationUnit.compilationResult,
				onType);
		
		dec.returnType = returnType;
		dec.sourceStart = start;
		dec.sourceEnd = end;
		declarationFactory.setSelector(dec,identifierName);
		dec.declarationSourceStart = decSourceStart;
		declarationFactory.setDeclaredModifiers(dec,fieldModifiers);
//		declarationFactory.setInitialization(dec,initialization);
		
		dec.bodyEnd = endPosition;
//		dec.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition);

		// Grab the javadoc
        dec.javadoc = this.javadoc;
        this.javadoc = null;

    	// consume annotations
    	int length;
    	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
    		System.arraycopy(
    			this.expressionStack, 
    			(this.expressionPtr -= length) + 1, 
    			dec.annotations = new Annotation[length], 
    			0, 
    			length); 
    	}

		pushOnAstStack(dec);
		println("consumed field: " + dec);
		this.display();
}
	protected void consumeExitITDVariableWithoutInitializer() {
		MethodDeclaration itdDecl = (MethodDeclaration) this.astStack[this.astPtr];
		declarationFactory.setInitialization(itdDecl,null);
	}
	protected void consumeExitITDVariableWithInitializer() {
		this.expressionLengthPtr--;
		MethodDeclaration itdDecl = (MethodDeclaration) this.astStack[this.astPtr];
		Expression initialization = this.expressionStack[this.expressionPtr--];
		declarationFactory.setInitialization(itdDecl,initialization);
		// we need to update the declarationSourceEnd of the local variable declaration to the
		// source end position of the initialization expression
		itdDecl.declarationSourceEnd = initialization.sourceEnd;
	}

	protected void consumeInterTypeFieldDeclaration() {
		MethodDeclaration dec = (MethodDeclaration) this.astStack[this.astPtr];

		dec.bodyEnd = endPosition;
		dec.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition);
	}

	protected void consumeInterTypeMethodDeclaration(boolean isNotAbstract) {
		consumeMethodDeclaration(isNotAbstract);
	}

	protected void consumeInterTypeMethodHeader() {
		consumeMethodHeader();		
	}

	protected void consumeInterTypeConstructorDeclaration() {
		consumeMethodDeclaration(true);
	}

	protected void consumeInterTypeConstructorHeader() {
		consumeMethodHeader();		
	}

	protected void consumeInterTypeMethodHeaderName(boolean hasMethodTypeParameters, boolean hasGenericTypeParameters) {
		//InterTypeMethodHeaderName ::= Modifiersopt Type OnType '.' JavaIdentifier '('
		this.display();
		MethodDeclaration md = declarationFactory.createInterTypeMethodDeclaration(
				this.compilationUnit.compilationResult);

		//identifier
		char[] name = identifierStack[identifierPtr];
		long selectorSource = identifierPositionStack[identifierPtr--];
		identifierLengthPtr--;


		//onType
		if (hasGenericTypeParameters) {
			pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
			//consumeClassOrInterfaceName();
		} else {
			consumeClassOrInterfaceName();			
		}
		TypeReference onType = getTypeReference(0);

		//type
		md.returnType = getTypeReference(intStack[intPtr--]);

		// consume method type parameters
		if (hasMethodTypeParameters) {
			int tp_length = this.genericsLengthStack[this.genericsLengthPtr--];
			this.genericsPtr -= tp_length;
			System.arraycopy(this.genericsStack, this.genericsPtr + 1, md.typeParameters = new TypeParameter[tp_length], 0, tp_length);
		}

		declarationFactory.setOnType(md,onType);

		//modifiers
		md.declarationSourceStart = intStack[intPtr--];
		declarationFactory.setDeclaredModifiers(md,intStack[intPtr--]);

		//highlight starts at selector start
		md.sourceStart = (int) (selectorSource >>> 32);
		pushOnAstStack(md);
		md.sourceEnd = lParenPos;
		md.bodyStart = lParenPos + 1;
		declarationFactory.setSelector(md,name);
		listLength = 0;
		// initialize listLength before reading parameters/throws
 
 		// Grab the javadoc
        md.javadoc = this.javadoc;
        this.javadoc = null;

    	// consume annotations
    	int length;
    	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
    		System.arraycopy(
    			this.expressionStack, 
    			(this.expressionPtr -= length) + 1, 
    			md.annotations = new Annotation[length], 
    			0, 
    			length); 
    	}

		// recovery
		if (currentElement != null) {
			if (currentElement instanceof RecoveredType
				//|| md.modifiers != 0
				|| (scanner.getLineNumber(md.returnType.sourceStart)
					== scanner.getLineNumber(md.sourceStart))) {
				lastCheckPoint = md.bodyStart;
				currentElement = currentElement.add(md, 0);
				lastIgnoredToken = -1;
			} else {
				lastCheckPoint = md.sourceStart;
				restartRecovery = true;
			}
		}
	}

	protected void consumeInterTypeConstructorHeaderName(boolean hasConstructorTypeParameters, boolean hasTargetTypeParameters) {
		//InterTypeConstructorHeaderName ::= Modifiersopt Name '.' 'new' '('
		this.display();
		MethodDeclaration md = declarationFactory.createInterTypeConstructorDeclaration(
				this.compilationUnit.compilationResult);

		//identifier
//		md.selector = identifierStack[identifierPtr];
//		long selectorSource = identifierPositionStack[identifierPtr--];
////		identifierLengthPtr--;

		//onType
		if (!hasTargetTypeParameters) {
			consumeClassOrInterfaceName();			
		}
		TypeReference onType = getTypeReference(0);
		declarationFactory.setOnType(md,onType);

		println("got onType: " + onType);
		this.display();

		intPtr--; // pop new info
		//type
		md.returnType = TypeReference.baseTypeReference(T_void, 0, null); //getTypeReference(intStack[intPtr--]);
		
		if (hasConstructorTypeParameters) {
			// consume type parameters
			int tp_length = this.genericsLengthStack[this.genericsLengthPtr--];
			this.genericsPtr -= tp_length;
			System.arraycopy(this.genericsStack, this.genericsPtr + 1, md.typeParameters = new TypeParameter[tp_length], 0, tp_length);
		}
		
		//modifiers
		md.declarationSourceStart = intStack[intPtr--];
		declarationFactory.setDeclaredModifiers(md,intStack[intPtr--]);
		//md.modifiers = intStack[intPtr--];

		// consume annotations
		int length;
		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			System.arraycopy(
				this.expressionStack, 
				(this.expressionPtr -= length) + 1, 
				md.annotations = new Annotation[length], 
				0, 
				length); 
		}

		//highlight starts at selector start
		//md.sourceStart = (int) (selectorSource >>> 32);
		md.sourceStart = onType.sourceStart;
		pushOnAstStack(md);
		md.sourceEnd = lParenPos;
		md.bodyStart = lParenPos + 1;
		listLength = 0;
		// initialize listLength before reading parameters/throws

		declarationFactory.setSelector(md,
			(new String(CharOperation.concatWith(onType.getTypeName(), '_')) + "_new").toCharArray());
		

		// recovery
		if (currentElement != null) {
			if (currentElement instanceof RecoveredType
				//|| md.modifiers != 0
				//|| (scanner.getLineNumber(md.returnType.sourceStart)
				//	== scanner.getLineNumber(md.sourceStart))
				) {
				//lastCheckPoint = md.bodyStart;
				currentElement = currentElement.add(md, 0);
				lastIgnoredToken = -1;
			} else {
				lastCheckPoint = md.sourceStart;
				restartRecovery = true;
			}
		}
	}



//*********************************************************


	protected void consumePseudoToken(String value) {
		consumePseudoToken(value, 0, false);
	}

	protected void consumePseudoToken(
		String value,
		int popFromIntStack,
		boolean isIdentifier) {
		intPtr -= popFromIntStack;

		int start = currentTokenStart;
		int end = start + value.length() - 1;
		ASTNode tok = declarationFactory.createPseudoToken(this, value, isIdentifier);
		tok.sourceStart = start;
		tok.sourceEnd = end;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokenIdentifier() {
		long pos = identifierPositionStack[identifierPtr];
		int end = (int) pos;
		int start = (int) (pos >>> 32);
		char[] name = identifierStack[identifierPtr--];
		identifierLengthPtr--;

		ASTNode tok = declarationFactory.createPseudoToken(this, new String(name), true);
		tok.sourceStart = start;
		tok.sourceEnd = end;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokenIf() {
		//this.display();
		Expression expr = (Expression) expressionStack[expressionPtr--];
		expressionLengthPtr--;
		println("expr: " + expr);

		int start = intStack[intPtr--];
		ASTNode tok = declarationFactory.createIfPseudoToken(this, expr);
		tok.sourceStart = start;
		tok.sourceEnd = this.rParenPos;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokenLiteral() {
		Literal literal = (Literal) expressionStack[expressionPtr--];
		expressionLengthPtr--;
		//System.out.println("literal: " + new String(literal.source()));

		ASTNode tok = declarationFactory.createPseudoToken(this, new String(literal.source()), false);
		declarationFactory.setLiteralKind(tok,"string");
		tok.sourceStart = literal.sourceStart;
		tok.sourceEnd = literal.sourceEnd;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokenModifier() {
		//int modifier = modifiers;
		consumePseudoToken(Modifier.toString(modifiers), 0, true);
		modifiers = ClassFileConstants.AccDefault;
	}

	protected void consumePseudoTokenPrimitiveType() {
		TypeReference type = getTypeReference(0);

		ASTNode tok = declarationFactory.createPseudoToken(this, type.toString(), true);
		tok.sourceStart = type.sourceStart;
		tok.sourceEnd = type.sourceEnd;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokens() {
		optimizedConcatNodeLists();
	}
	// This method is part of an automatic generation : do NOT edit-modify
	protected void consumeRule(int act) {
	  switch ( act ) {
	    case 38 : if (DEBUG) { System.out.println("Type ::= PrimitiveType"); }  //$NON-NLS-1$
			    consumePrimitiveType();  
				break;
	 
	    case 52 : if (DEBUG) { System.out.println("ReferenceType ::= ClassOrInterfaceType"); }  //$NON-NLS-1$
			    consumeReferenceType();  
				break;
	 
	    case 56 : if (DEBUG) { System.out.println("ClassOrInterface ::= Name"); }  //$NON-NLS-1$
			    consumeClassOrInterfaceName();  
				break;
	 
	    case 57 : if (DEBUG) { System.out.println("ClassOrInterface ::= GenericType DOT Name"); }  //$NON-NLS-1$
			    consumeClassOrInterface();  
				break;
	 
	    case 58 : if (DEBUG) { System.out.println("GenericType ::= ClassOrInterface TypeArguments"); }  //$NON-NLS-1$
			    consumeGenericType();  
				break;
	 
	    case 59 : if (DEBUG) { System.out.println("GenericType ::= ClassOrInterface LESS GREATER"); }  //$NON-NLS-1$
			    consumeGenericTypeWithDiamond();  
				break;
	 
	    case 60 : if (DEBUG) { System.out.println("ArrayTypeWithTypeArgumentsName ::= GenericType DOT Name"); }  //$NON-NLS-1$
			    consumeArrayTypeWithTypeArgumentsName();  
				break;
	 
	    case 61 : if (DEBUG) { System.out.println("ArrayType ::= PrimitiveType Dims"); }  //$NON-NLS-1$
			    consumePrimitiveArrayType();  
				break;
	 
	    case 62 : if (DEBUG) { System.out.println("ArrayType ::= Name Dims"); }  //$NON-NLS-1$
			    consumeNameArrayType();  
				break;
	 
	    case 63 : if (DEBUG) { System.out.println("ArrayType ::= ArrayTypeWithTypeArgumentsName Dims"); }  //$NON-NLS-1$
			    consumeGenericTypeNameArrayType();  
				break;
	 
	    case 64 : if (DEBUG) { System.out.println("ArrayType ::= GenericType Dims"); }  //$NON-NLS-1$
			    consumeGenericTypeArrayType();  
				break;
	 
	    case 70 : if (DEBUG) { System.out.println("AjName ::= AjSimpleName"); }  //$NON-NLS-1$
			    consumeZeroTypeAnnotations();  
				break;
	 
	    case 71 : if (DEBUG) { System.out.println("AjName ::= AjQualifiedName"); }  //$NON-NLS-1$
			    consumeZeroTypeAnnotations();  
				break;
	 
	    case 80 : if (DEBUG) { System.out.println("AjQualifiedName ::= AjName DOT SimpleNameOrAj"); }  //$NON-NLS-1$
			    consumeQualifiedName();  
				break;
	 
	    case 83 : if (DEBUG) { System.out.println("Name ::= SimpleName"); }  //$NON-NLS-1$
			    consumeZeroTypeAnnotations();  
				break;
	 
	    case 88 : if (DEBUG) { System.out.println("UnannotatableName ::= UnannotatableName DOT SimpleName"); }  //$NON-NLS-1$
			    consumeUnannotatableQualifiedName();  
				break;
	 
	    case 89 : if (DEBUG) { System.out.println("QualifiedName ::= Name DOT JavaIdentifier"); }  //$NON-NLS-1$
			    consumeQualifiedName(false);  
				break;
	 
	    case 90 : if (DEBUG) { System.out.println("QualifiedName ::= Name DOT TypeAnnotations JavaIdentifier"); }  //$NON-NLS-1$
			    consumeQualifiedName(true);  
				break;
	 
	    case 91 : if (DEBUG) { System.out.println("TypeAnnotationsopt ::="); }  //$NON-NLS-1$
			    consumeZeroTypeAnnotations();  
				break;
	 
	     case 95 : if (DEBUG) { System.out.println("TypeAnnotations0 ::= TypeAnnotations0 TypeAnnotation"); }  //$NON-NLS-1$
			    consumeOneMoreTypeAnnotation();  
				break;
	 
	     case 96 : if (DEBUG) { System.out.println("TypeAnnotation ::= NormalTypeAnnotation"); }  //$NON-NLS-1$
			    consumeTypeAnnotation();  
				break;
	 
	     case 97 : if (DEBUG) { System.out.println("TypeAnnotation ::= MarkerTypeAnnotation"); }  //$NON-NLS-1$
			    consumeTypeAnnotation();  
				break;
	 
	     case 98 : if (DEBUG) { System.out.println("TypeAnnotation ::= SingleMemberTypeAnnotation"); }  //$NON-NLS-1$
			    consumeTypeAnnotation();  
				break;
	 
	    case 99 : if (DEBUG) { System.out.println("TypeAnnotationName ::= AT308 UnannotatableName"); }  //$NON-NLS-1$
			    consumeAnnotationName() ;  
				break;
	 
	    case 100 : if (DEBUG) { System.out.println("NormalTypeAnnotation ::= TypeAnnotationName LPAREN..."); }  //$NON-NLS-1$
			    consumeNormalAnnotation(true) ;  
				break;
	 
	    case 101 : if (DEBUG) { System.out.println("MarkerTypeAnnotation ::= TypeAnnotationName"); }  //$NON-NLS-1$
			    consumeMarkerAnnotation(true) ;  
				break;
	 
	    case 102 : if (DEBUG) { System.out.println("SingleMemberTypeAnnotation ::= TypeAnnotationName LPAREN"); }  //$NON-NLS-1$
			    consumeSingleMemberAnnotation(true) ;  
				break;
	 
	    case 103 : if (DEBUG) { System.out.println("RejectTypeAnnotations ::="); }  //$NON-NLS-1$
			    consumeNonTypeUseName();  
				break;
	 
	    case 104 : if (DEBUG) { System.out.println("PushZeroTypeAnnotations ::="); }  //$NON-NLS-1$
			    consumeZeroTypeAnnotations();  
				break;
	 
	    case 105 : if (DEBUG) { System.out.println("VariableDeclaratorIdOrThis ::= this"); }  //$NON-NLS-1$
			    consumeExplicitThisParameter(false);  
				break;
	 
	    case 106 : if (DEBUG) { System.out.println("VariableDeclaratorIdOrThis ::= UnannotatableName DOT..."); }  //$NON-NLS-1$
			    consumeExplicitThisParameter(true);  
				break;
	 
	    case 107 : if (DEBUG) { System.out.println("VariableDeclaratorIdOrThis ::= VariableDeclaratorId"); }  //$NON-NLS-1$
			    consumeVariableDeclaratorIdParameter();  
				break;
	 
	    case 108 : if (DEBUG) { System.out.println("CompilationUnit ::= EnterCompilationUnit..."); }  //$NON-NLS-1$
			    consumeCompilationUnit();  
				break;
	 
	    case 109 : if (DEBUG) { System.out.println("InternalCompilationUnit ::= PackageDeclaration"); }  //$NON-NLS-1$
			    consumeInternalCompilationUnit();  
				break;
	 
	    case 110 : if (DEBUG) { System.out.println("InternalCompilationUnit ::= PackageDeclaration..."); }  //$NON-NLS-1$
			    consumeInternalCompilationUnit();  
				break;
	 
	    case 111 : if (DEBUG) { System.out.println("InternalCompilationUnit ::= PackageDeclaration..."); }  //$NON-NLS-1$
			    consumeInternalCompilationUnitWithTypes();  
				break;
	 
	    case 112 : if (DEBUG) { System.out.println("InternalCompilationUnit ::= PackageDeclaration..."); }  //$NON-NLS-1$
			    consumeInternalCompilationUnitWithTypes();  
				break;
	 
	    case 113 : if (DEBUG) { System.out.println("InternalCompilationUnit ::= ImportDeclarations..."); }  //$NON-NLS-1$
			    consumeInternalCompilationUnit();  
				break;
	 
	    case 114 : if (DEBUG) { System.out.println("InternalCompilationUnit ::= TypeDeclarations"); }  //$NON-NLS-1$
			    consumeInternalCompilationUnitWithTypes();  
				break;
	 
	    case 115 : if (DEBUG) { System.out.println("InternalCompilationUnit ::= ImportDeclarations..."); }  //$NON-NLS-1$
			    consumeInternalCompilationUnitWithTypes();  
				break;
	 
	    case 116 : if (DEBUG) { System.out.println("InternalCompilationUnit ::="); }  //$NON-NLS-1$
			    consumeEmptyInternalCompilationUnit();  
				break;
	 
	    case 117 : if (DEBUG) { System.out.println("ReduceImports ::="); }  //$NON-NLS-1$
			    consumeReduceImports();  
				break;
	 
	    case 118 : if (DEBUG) { System.out.println("EnterCompilationUnit ::="); }  //$NON-NLS-1$
			    consumeEnterCompilationUnit();  
				break;
	 
	    case 134 : if (DEBUG) { System.out.println("CatchHeader ::= catch LPAREN CatchFormalParameter RPAREN"); }  //$NON-NLS-1$
			    consumeCatchHeader();  
				break;
	 
	    case 136 : if (DEBUG) { System.out.println("ImportDeclarations ::= ImportDeclarations..."); }  //$NON-NLS-1$
			    consumeImportDeclarations();  
				break;
	 
	    case 138 : if (DEBUG) { System.out.println("TypeDeclarations ::= TypeDeclarations TypeDeclaration"); }  //$NON-NLS-1$
			    consumeTypeDeclarations();  
				break;
	 
	    case 139 : if (DEBUG) { System.out.println("PackageDeclaration ::= PackageDeclarationName SEMICOLON"); }  //$NON-NLS-1$
			    consumePackageDeclaration();  
				break;
	 
	    case 140 : if (DEBUG) { System.out.println("PackageDeclarationName ::= Modifiers package..."); }  //$NON-NLS-1$
			    consumePackageDeclarationNameWithModifiers();  
				break;
	 
	    case 141 : if (DEBUG) { System.out.println("PackageDeclarationName ::= PackageComment package Name"); }  //$NON-NLS-1$
			    consumePackageDeclarationName();  
				break;
	 
	    case 142 : if (DEBUG) { System.out.println("PackageComment ::="); }  //$NON-NLS-1$
			    consumePackageComment();  
				break;
	 
	    case 147 : if (DEBUG) { System.out.println("SingleTypeImportDeclaration ::=..."); }  //$NON-NLS-1$
			    consumeImportDeclaration();  
				break;
	 
	    case 148 : if (DEBUG) { System.out.println("SingleTypeImportDeclarationName ::= import Name..."); }  //$NON-NLS-1$
			    consumeSingleTypeImportDeclarationName();  
				break;
	 
	    case 149 : if (DEBUG) { System.out.println("TypeImportOnDemandDeclaration ::=..."); }  //$NON-NLS-1$
			    consumeImportDeclaration();  
				break;
	 
	    case 150 : if (DEBUG) { System.out.println("TypeImportOnDemandDeclarationName ::= import Name DOT..."); }  //$NON-NLS-1$
			    consumeTypeImportOnDemandDeclarationName();  
				break;
	 
	     case 153 : if (DEBUG) { System.out.println("TypeDeclaration ::= SEMICOLON"); }  //$NON-NLS-1$
			    consumeEmptyTypeDeclaration();  
				break;
	 
	    case 157 : if (DEBUG) { System.out.println("Modifiers ::= Modifiers Modifier"); }  //$NON-NLS-1$
			    consumeModifiers2();  
				break;
	 
	    case 170 : if (DEBUG) { System.out.println("Modifier ::= Annotation"); }  //$NON-NLS-1$
			    consumeAnnotationAsModifier();  
				break;
	 
	    case 183 : if (DEBUG) { System.out.println("AspectDeclaration ::= AspectHeader AspectBody"); }  //$NON-NLS-1$
			    consumeAspectDeclaration();  
				break;
	 
	    case 184 : if (DEBUG) { System.out.println("AspectHeader ::= AspectHeaderName ClassHeaderExtendsopt"); }  //$NON-NLS-1$
			    consumeAspectHeader();  
				break;
	 
	    case 187 : if (DEBUG) { System.out.println("AspectHeaderName ::= AspectHeaderName1 TypeParameters"); }  //$NON-NLS-1$
			    consumeAspectHeaderNameWithTypeParameters(false);  
				break;
	 
	    case 188 : if (DEBUG) { System.out.println("AspectHeaderName ::= AspectHeaderName2 TypeParameters"); }  //$NON-NLS-1$
			    consumeAspectHeaderNameWithTypeParameters(true);  
				break;
	 
	    case 189 : if (DEBUG) { System.out.println("AspectHeaderName1 ::= Modifiersopt aspect Identifier"); }  //$NON-NLS-1$
			    consumeAspectHeaderName(false);  
				break;
	 
	    case 190 : if (DEBUG) { System.out.println("AspectHeaderName2 ::= Modifiersopt privileged..."); }  //$NON-NLS-1$
			    consumeAspectHeaderName(true);  
				break;
	 
	    case 192 : if (DEBUG) { System.out.println("AspectHeaderRest ::= AspectHeaderRestStart PseudoTokens"); }  //$NON-NLS-1$
			    consumeAspectHeaderRest();  
				break;
	 
	    case 193 : if (DEBUG) { System.out.println("AspectHeaderRestStart ::= Identifier"); }  //$NON-NLS-1$
			    consumePseudoTokenIdentifier();  
				break;
	 
	    case 196 : if (DEBUG) { System.out.println("AspectBodyDeclarations ::= AspectBodyDeclarations..."); }  //$NON-NLS-1$
			    consumeClassBodyDeclarations();  
				break;
	 
	    case 197 : if (DEBUG) { System.out.println("AspectBodyDeclarationsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyClassBodyDeclarationsopt();  
				break;
	 
	    case 198 : if (DEBUG) { System.out.println("AspectBodyDeclarationsopt ::= NestedType..."); }  //$NON-NLS-1$
			    consumeClassBodyDeclarationsopt();  
				break;
	 
	    case 199 : if (DEBUG) { System.out.println("AspectBodyDeclaration ::=..."); }  //$NON-NLS-1$
			    consumeClassBodyDeclarationInAspect();  
				break;
	 
	    case 203 : if (DEBUG) { System.out.println("ClassBodyDeclarationNoAroundMethod ::= Diet NestedMethod"); }  //$NON-NLS-1$
			    consumeClassBodyDeclaration();  
				break;
	 
	    case 213 : if (DEBUG) { System.out.println("ClassMemberDeclarationNoAroundMethod ::= SEMICOLON"); }  //$NON-NLS-1$
			    consumeEmptyTypeDeclaration();  
				break;

	    case 215 : if (DEBUG) { System.out.println("MethodDeclarationNoAround ::= MethodHeaderNoAround..."); }  //$NON-NLS-1$
			    // set to true to consume a method with a body
	  consumeMethodDeclaration(true);   
				break;
	 
	    case 216 : if (DEBUG) { System.out.println("AbstractMethodDeclarationNoAround ::=..."); }  //$NON-NLS-1$
			    // set to false to consume a method without body
	  consumeMethodDeclaration(false);  
				break;
	 
	    case 217 : if (DEBUG) { System.out.println("MethodHeaderNoAround ::= MethodHeaderNameNoAround..."); }  //$NON-NLS-1$
			    consumeMethodHeader();  
				break;
	 
	    case 218 : if (DEBUG) { System.out.println("MethodHeaderNameNoAround ::= Modifiersopt TypeParameters"); }  //$NON-NLS-1$
			    consumeMethodHeaderNameWithTypeParameters(false);  
				break;
	 
	    case 219 : if (DEBUG) { System.out.println("MethodHeaderNameNoAround ::= Modifiersopt Type..."); }  //$NON-NLS-1$
			    consumeMethodHeaderName(false);  
				break;
	 
	    case 220 : if (DEBUG) { System.out.println("PointcutDeclaration ::= PointcutHeader..."); }  //$NON-NLS-1$
			    consumeEmptyPointcutDeclaration();  
				break;
	 
	    case 221 : if (DEBUG) { System.out.println("PointcutDeclaration ::= PointcutHeader..."); }  //$NON-NLS-1$
			    consumePointcutDeclaration();  
				break;
	 
	    case 222 : if (DEBUG) { System.out.println("PointcutHeader ::= Modifiersopt pointcut JavaIdentifier"); }  //$NON-NLS-1$
			    consumePointcutHeader();  
				break;
	 
	    case 225 : if (DEBUG) { System.out.println("AroundDeclaration ::= AroundHeader MethodBody"); }  //$NON-NLS-1$
			    consumeAroundDeclaration();  
				break;
	 
	    case 226 : if (DEBUG) { System.out.println("AroundHeader ::= AroundHeaderName FormalParameterListopt"); }  //$NON-NLS-1$
			    consumeAroundHeader();  
				break;
	 
	    case 227 : if (DEBUG) { System.out.println("AroundHeaderName ::= Modifiersopt Type around LPAREN"); }  //$NON-NLS-1$
			    consumeAroundHeaderName();  
				break;
	 
	    case 228 : if (DEBUG) { System.out.println("AroundHeaderName ::= Modifiersopt around LPAREN"); }  //$NON-NLS-1$
			    consumeAroundHeaderNameMissingReturnType();  
				break;
	 
	    case 229 : if (DEBUG) { System.out.println("BasicAdviceDeclaration ::= BasicAdviceHeader MethodBody"); }  //$NON-NLS-1$
			    consumeBasicAdviceDeclaration();  
				break;
	 
	    case 232 : if (DEBUG) { System.out.println("BeforeAdviceHeader ::= BeforeAdviceHeaderName..."); }  //$NON-NLS-1$
			    consumeBasicAdviceHeader();  
				break;
	 
	    case 233 : if (DEBUG) { System.out.println("AfterAdviceHeader ::= AfterAdviceHeaderName..."); }  //$NON-NLS-1$
			    consumeBasicAdviceHeader();  
				break;
	 
	    case 234 : if (DEBUG) { System.out.println("BeforeAdviceHeaderName ::= Modifiersopt before LPAREN"); }  //$NON-NLS-1$
			    consumeBasicAdviceHeaderName(false);  
				break;
	 
	    case 235 : if (DEBUG) { System.out.println("AfterAdviceHeaderName ::= Modifiersopt after LPAREN"); }  //$NON-NLS-1$
			    consumeBasicAdviceHeaderName(true);  
				break;
	 
	    case 236 : if (DEBUG) { System.out.println("ExtraParamopt ::= Identifier LPAREN FormalParameter..."); }  //$NON-NLS-1$
			    consumeExtraParameterWithFormal();  
				break;
	 
	    case 237 : if (DEBUG) { System.out.println("ExtraParamopt ::= Identifier LPAREN RPAREN"); }  //$NON-NLS-1$
			    consumeExtraParameterNoFormal();  
				break;
	 
	    case 238 : if (DEBUG) { System.out.println("ExtraParamopt ::= Identifier"); }  //$NON-NLS-1$
			    consumeExtraParameterNoFormal();  
				break;
	 
	    case 240 : if (DEBUG) { System.out.println("OnType ::= JavaIdentifier"); }  //$NON-NLS-1$
			    consumeZeroTypeAnnotations();  
				break;
	 
	    case 241 : if (DEBUG) { System.out.println("OnType ::= OnType DOT JavaIdentifier"); }  //$NON-NLS-1$
			    consumeZeroTypeAnnotations(); consumeQualifiedName();  
				break;
	 
	    case 246 : if (DEBUG) { System.out.println("InterTypeMethodDeclaration ::= InterTypeMethodHeader..."); }  //$NON-NLS-1$
			    // set to true to consume a method with a body
	  consumeInterTypeMethodDeclaration(true);   
				break;
	 
	    case 247 : if (DEBUG) { System.out.println("InterTypeMethodHeader ::= InterTypeMethodHeaderName..."); }  //$NON-NLS-1$
			    consumeInterTypeMethodHeader();  
				break;
	 
	    case 248 : if (DEBUG) { System.out.println("InterTypeMethodHeaderName ::= Modifiersopt Type OnType"); }  //$NON-NLS-1$
			    consumeInterTypeMethodHeaderName(false,false);  
				break;
	 
	    case 249 : if (DEBUG) { System.out.println("InterTypeMethodHeaderName ::= Modifiersopt Type OnType"); }  //$NON-NLS-1$
			    consumeInterTypeMethodHeaderNameIllegallyUsingTypePattern("*");  
				break;
	 
	    case 250 : if (DEBUG) { System.out.println("InterTypeMethodHeaderName ::= Modifiersopt Type OnType"); }  //$NON-NLS-1$
			    consumeInterTypeMethodHeaderNameIllegallyUsingTypePattern("+");  
				break;
	 
	    case 251 : if (DEBUG) { System.out.println("InterTypeMethodHeaderName ::= Modifiersopt Type OnType"); }  //$NON-NLS-1$
			    consumeInterTypeMethodHeaderName(false,true);  
				break;
	 
	    case 252 : if (DEBUG) { System.out.println("InterTypeMethodHeaderName ::= Modifiersopt..."); }  //$NON-NLS-1$
			    consumeInterTypeMethodHeaderName(true,false);  
				break;
	 
	    case 253 : if (DEBUG) { System.out.println("InterTypeMethodHeaderName ::= Modifiersopt..."); }  //$NON-NLS-1$
			    consumeInterTypeMethodHeaderName(true,true);  
				break;
	 
	    case 254 : if (DEBUG) { System.out.println("AbstractInterTypeMethodDeclaration ::=..."); }  //$NON-NLS-1$
			    // set to false to consume a method without body
	  consumeInterTypeMethodDeclaration(false);  
				break;
	 
	    case 255 : if (DEBUG) { System.out.println("TypeParametersAsReference ::= TypeParameters"); }  //$NON-NLS-1$
			    convertTypeParametersToSingleTypeReferences();  
				break;
	 
	    case 256 : if (DEBUG) { System.out.println("InterTypeConstructorDeclaration ::=..."); }  //$NON-NLS-1$
			    // set to true to consume a method with a body
	  consumeInterTypeConstructorDeclaration();   
				break;
	 
	    case 257 : if (DEBUG) { System.out.println("InterTypeConstructorHeader ::=..."); }  //$NON-NLS-1$
			    consumeInterTypeConstructorHeader();  
				break;
	 
	    case 258 : if (DEBUG) { System.out.println("InterTypeConstructorHeaderName ::= Modifiersopt Name DOT"); }  //$NON-NLS-1$
			    consumeInterTypeConstructorHeaderName(false,false);  
				break;
	 
	    case 259 : if (DEBUG) { System.out.println("InterTypeConstructorHeaderName ::= Modifiersopt Name DOT"); }  //$NON-NLS-1$
			    consumeInterTypeConstructorHeaderNameIllegallyUsingTypePattern("*");  
				break;
	 
	    case 260 : if (DEBUG) { System.out.println("InterTypeConstructorHeaderName ::= Modifiersopt Name..."); }  //$NON-NLS-1$
			    consumeInterTypeConstructorHeaderNameIllegallyUsingTypePattern("+");  
				break;
	 
	    case 261 : if (DEBUG) { System.out.println("InterTypeConstructorHeaderName ::= Modifiersopt..."); }  //$NON-NLS-1$
			    consumeInterTypeConstructorHeaderName(true,false);  
				break;
	 
	    case 262 : if (DEBUG) { System.out.println("InterTypeConstructorHeaderName ::= Modifiersopt..."); }  //$NON-NLS-1$
			    consumeInterTypeConstructorHeaderName(false,true);  
				break;
	 
	    case 263 : if (DEBUG) { System.out.println("InterTypeConstructorHeaderName ::= Modifiersopt..."); }  //$NON-NLS-1$
			    consumeInterTypeConstructorHeaderName(true,true);  
				break;
	 
	    case 264 : if (DEBUG) { System.out.println("InterTypeFieldDeclaration ::= InterTypeFieldHeader..."); }  //$NON-NLS-1$
			    consumeInterTypeFieldDeclaration();  
				break;
	 
	    case 265 : if (DEBUG) { System.out.println("InterTypeFieldHeader ::= Modifiersopt Type OnType DOT..."); }  //$NON-NLS-1$
			    consumeInterTypeFieldHeader(false);  
				break;
	 
	    case 266 : if (DEBUG) { System.out.println("InterTypeFieldHeader ::= Modifiersopt Type OnType DOT..."); }  //$NON-NLS-1$
			    consumeInterTypeFieldHeaderIllegallyAttemptingToUseATypePattern("*");  
				break;
	 
	    case 267 : if (DEBUG) { System.out.println("InterTypeFieldHeader ::= Modifiersopt Type OnType PLUS"); }  //$NON-NLS-1$
			    consumeInterTypeFieldHeaderIllegallyAttemptingToUseATypePattern("+");  
				break;
	 
	    case 268 : if (DEBUG) { System.out.println("InterTypeFieldHeader ::= Modifiersopt Type OnType..."); }  //$NON-NLS-1$
			    consumeInterTypeFieldHeader(true);  
				break;
	 
	    case 269 : if (DEBUG) { System.out.println("InterTypeFieldBody ::="); }  //$NON-NLS-1$
			    consumeExitITDVariableWithoutInitializer();  
				break;
	 
	    case 270 : if (DEBUG) { System.out.println("InterTypeFieldBody ::= EQUAL ForceNoDiet..."); }  //$NON-NLS-1$
			    consumeExitITDVariableWithInitializer();  
				break;
	 
	    case 272 : if (DEBUG) { System.out.println("DeclareDeclaration ::= DeclareHeader PseudoTokens..."); }  //$NON-NLS-1$
			    consumeDeclareDeclaration();  
				break;
	 
	    case 273 : if (DEBUG) { System.out.println("DeclareHeader ::= declare Identifier COLON"); }  //$NON-NLS-1$
			    consumeDeclareHeader();  
				break;
	 
	    case 274 : if (DEBUG) { System.out.println("DeclareDeclaration ::= DeclareAnnotationHeader..."); }  //$NON-NLS-1$
			    consumeDeclareAnnotation(' ');  
				break;
	 
	    case 275 : if (DEBUG) { System.out.println("DeclareDeclaration ::= DeclareAnnotationHeader..."); }  //$NON-NLS-1$
			    consumeDeclareAnnotation('+');  
				break;
	 
	    case 276 : if (DEBUG) { System.out.println("DeclareDeclaration ::= DeclareAnnotationHeader..."); }  //$NON-NLS-1$
			    consumeDeclareAnnotation('-');  
				break;
	 
	    case 279 : if (DEBUG) { System.out.println("DeclareAnnotationHeader ::= declare AT Identifier COLON"); }  //$NON-NLS-1$
			    consumeDeclareAnnotationHeader();  
				break;
	 
	    case 282 : if (DEBUG) { System.out.println("PseudoTokens ::= PseudoTokens ColonPseudoToken"); }  //$NON-NLS-1$
			    consumePseudoTokens();  
				break;
	 
	    case 283 : if (DEBUG) { System.out.println("PseudoTokens ::= PseudoTokens PseudoToken"); }  //$NON-NLS-1$
			    consumePseudoTokens();  
				break;
	 
	    case 285 : if (DEBUG) { System.out.println("PseudoTokensNoColon ::= PseudoTokensNoColon PseudoToken"); }  //$NON-NLS-1$
			    consumePseudoTokens();  
				break;
	 
	    case 286 : if (DEBUG) { System.out.println("ColonPseudoToken ::= COLON"); }  //$NON-NLS-1$
			    consumePseudoToken(":");  
				break;
	 
	    case 287 : if (DEBUG) { System.out.println("PseudoToken ::= JavaIdentifier"); }  //$NON-NLS-1$
			    consumePseudoTokenIdentifier();  
				break;
	 
	    case 288 : if (DEBUG) { System.out.println("PseudoToken ::= LPAREN"); }  //$NON-NLS-1$
			    consumePseudoToken("(");  
				break;
	 
	    case 289 : if (DEBUG) { System.out.println("PseudoToken ::= RPAREN"); }  //$NON-NLS-1$
			    consumePseudoToken(")");  
				break;
	 
	    case 290 : if (DEBUG) { System.out.println("PseudoToken ::= DOT"); }  //$NON-NLS-1$
			    consumePseudoToken(".");  
				break;
	 
	    case 291 : if (DEBUG) { System.out.println("PseudoToken ::= MULTIPLY"); }  //$NON-NLS-1$
			    consumePseudoToken("*");  
				break;
	 
	    case 292 : if (DEBUG) { System.out.println("PseudoToken ::= PLUS"); }  //$NON-NLS-1$
			    consumePseudoToken("+");  
				break;
	 
	    case 293 : if (DEBUG) { System.out.println("PseudoToken ::= EQUAL"); }  //$NON-NLS-1$
			    consumePseudoToken("=");  
				break;
	 
	    case 294 : if (DEBUG) { System.out.println("PseudoToken ::= AND_AND"); }  //$NON-NLS-1$
			    consumePseudoToken("&&");  
				break;
	 
	    case 295 : if (DEBUG) { System.out.println("PseudoToken ::= OR_OR"); }  //$NON-NLS-1$
			    consumePseudoToken("||");  
				break;
	 
	    case 296 : if (DEBUG) { System.out.println("PseudoToken ::= NOT"); }  //$NON-NLS-1$
			    consumePseudoToken("!");  
				break;
	 
	    case 297 : if (DEBUG) { System.out.println("PseudoToken ::= COMMA"); }  //$NON-NLS-1$
			    consumePseudoToken(",");  
				break;
	 
	    case 298 : if (DEBUG) { System.out.println("PseudoToken ::= LBRACKET"); }  //$NON-NLS-1$
			    consumePseudoToken("[");  
				break;
	 
	    case 299 : if (DEBUG) { System.out.println("PseudoToken ::= RBRACKET"); }  //$NON-NLS-1$
			    consumePseudoToken("]");  
				break;
	 
	    case 300 : if (DEBUG) { System.out.println("PseudoToken ::= AT"); }  //$NON-NLS-1$
			    consumePseudoToken("@");  
				break;
	 
	    case 301 : if (DEBUG) { System.out.println("PseudoToken ::= ELLIPSIS"); }  //$NON-NLS-1$
			    consumePseudoToken("...");  
				break;
	 
	    case 302 : if (DEBUG) { System.out.println("PseudoToken ::= QUESTION"); }  //$NON-NLS-1$
			    consumePseudoToken("?");  
				break;
	 
	    case 303 : if (DEBUG) { System.out.println("PseudoToken ::= LESS"); }  //$NON-NLS-1$
			    consumePseudoToken("<");  
				break;
	 
	    case 304 : if (DEBUG) { System.out.println("PseudoToken ::= GREATER"); }  //$NON-NLS-1$
			    consumePseudoToken(">");  
				break;
	 
	    case 305 : if (DEBUG) { System.out.println("PseudoToken ::= RIGHT_SHIFT"); }  //$NON-NLS-1$
			    consumePseudoToken(">>");  
				break;
	 
	    case 306 : if (DEBUG) { System.out.println("PseudoToken ::= UNSIGNED_RIGHT_SHIFT"); }  //$NON-NLS-1$
			    consumePseudoToken(">>>");  
				break;
	 
	    case 307 : if (DEBUG) { System.out.println("PseudoToken ::= AND"); }  //$NON-NLS-1$
			    consumePseudoToken("&");  
				break;
	 
	    case 308 : if (DEBUG) { System.out.println("PseudoToken ::= NOT_EQUAL"); }  //$NON-NLS-1$
			    consumePseudoToken("!=");  
				break;
	 
	    case 309 : if (DEBUG) { System.out.println("PseudoToken ::= PrimitiveType"); }  //$NON-NLS-1$
			    consumePseudoTokenPrimitiveType();  
				break;
	 
	    case 310 : if (DEBUG) { System.out.println("PseudoToken ::= SimpleModifier"); }  //$NON-NLS-1$
			    consumePseudoTokenModifier();  
				break;
	 
	    case 311 : if (DEBUG) { System.out.println("PseudoToken ::= Literal"); }  //$NON-NLS-1$
			    consumePseudoTokenLiteral();  
				break;
	 
	    case 312 : if (DEBUG) { System.out.println("PseudoToken ::= this"); }  //$NON-NLS-1$
			    consumePseudoToken("this", 1, true);  
				break;
	 
	    case 313 : if (DEBUG) { System.out.println("PseudoToken ::= class"); }  //$NON-NLS-1$
			    consumePseudoToken("class", 1, true);  
				break;
	 
	    case 314 : if (DEBUG) { System.out.println("PseudoToken ::= super"); }  //$NON-NLS-1$
			    consumePseudoToken("super", 1, true);  
				break;
	 
	    case 315 : if (DEBUG) { System.out.println("PseudoToken ::= if LPAREN Expression RPAREN"); }  //$NON-NLS-1$
			    consumePseudoTokenIf();  
				break;
	 
	    case 316 : if (DEBUG) { System.out.println("PseudoToken ::= assert"); }  //$NON-NLS-1$
			    consumePseudoToken("assert", 1, true);  
				break;
	 
	    case 317 : if (DEBUG) { System.out.println("PseudoToken ::= import"); }  //$NON-NLS-1$
			    consumePseudoToken("import", 1, true);  
				break;
	 
	    case 318 : if (DEBUG) { System.out.println("PseudoToken ::= package"); }  //$NON-NLS-1$
			    consumePseudoToken("package", 1, true);  
				break;
	 
	    case 319 : if (DEBUG) { System.out.println("PseudoToken ::= throw"); }  //$NON-NLS-1$
			    consumePseudoToken("throw", 1, true);  
				break;
	 
	    case 320 : if (DEBUG) { System.out.println("PseudoToken ::= new"); }  //$NON-NLS-1$
			    consumePseudoToken("new", 1, true);  
				break;
	 
	    case 321 : if (DEBUG) { System.out.println("PseudoToken ::= do"); }  //$NON-NLS-1$
			    consumePseudoToken("do", 1, true);  
				break;
	 
	    case 322 : if (DEBUG) { System.out.println("PseudoToken ::= for"); }  //$NON-NLS-1$
			    consumePseudoToken("for", 1, true);  
				break;
	 
	    case 323 : if (DEBUG) { System.out.println("PseudoToken ::= switch"); }  //$NON-NLS-1$
			    consumePseudoToken("switch", 1, true);  
				break;
	 
	    case 324 : if (DEBUG) { System.out.println("PseudoToken ::= try"); }  //$NON-NLS-1$
			    consumePseudoToken("try", 1, true);  
				break;
	 
	    case 325 : if (DEBUG) { System.out.println("PseudoToken ::= while"); }  //$NON-NLS-1$
			    consumePseudoToken("while", 1, true);  
				break;
	 
	    case 326 : if (DEBUG) { System.out.println("PseudoToken ::= break"); }  //$NON-NLS-1$
			    consumePseudoToken("break", 1, true);  
				break;
	 
	    case 327 : if (DEBUG) { System.out.println("PseudoToken ::= continue"); }  //$NON-NLS-1$
			    consumePseudoToken("continue", 1, true);  
				break;
	 
	    case 328 : if (DEBUG) { System.out.println("PseudoToken ::= return"); }  //$NON-NLS-1$
			    consumePseudoToken("return", 1, true);  
				break;
	 
	    case 329 : if (DEBUG) { System.out.println("PseudoToken ::= case"); }  //$NON-NLS-1$
			    consumePseudoToken("case", 1, true);  
				break;
	 
	    case 330 : if (DEBUG) { System.out.println("PseudoToken ::= catch"); }  //$NON-NLS-1$
			    consumePseudoToken("catch", 0, true);  
				break;
	 
	    case 331 : if (DEBUG) { System.out.println("PseudoToken ::= instanceof"); }  //$NON-NLS-1$
			    consumePseudoToken("instanceof", 0, true);  
				break;
	 
	    case 332 : if (DEBUG) { System.out.println("PseudoToken ::= else"); }  //$NON-NLS-1$
			    consumePseudoToken("else", 0, true);  
				break;
	 
	    case 333 : if (DEBUG) { System.out.println("PseudoToken ::= extends"); }  //$NON-NLS-1$
			    consumePseudoToken("extends", 0, true);  
				break;
	 
	    case 334 : if (DEBUG) { System.out.println("PseudoToken ::= finally"); }  //$NON-NLS-1$
			    consumePseudoToken("finally", 0, true);  
				break;
	 
	    case 335 : if (DEBUG) { System.out.println("PseudoToken ::= implements"); }  //$NON-NLS-1$
			    consumePseudoToken("implements", 0, true);  
				break;
	 
	    case 336 : if (DEBUG) { System.out.println("PseudoToken ::= throws"); }  //$NON-NLS-1$
			    consumePseudoToken("throws", 0, true);  
				break;
	 
	    case 337 : if (DEBUG) { System.out.println("ClassDeclaration ::= ClassHeader ClassBody"); }  //$NON-NLS-1$
			    consumeClassDeclaration();  
				break;
	 
	    case 338 : if (DEBUG) { System.out.println("IntertypeClassDeclaration ::= IntertypeClassHeader..."); }  //$NON-NLS-1$
			    consumeIntertypeClassDeclaration();  
				break;
	 
	    case 339 : if (DEBUG) { System.out.println("IntertypeClassHeader ::= IntertypeClassHeaderName..."); }  //$NON-NLS-1$
			    consumeIntertypeClassHeader();  
				break;
	 
	    case 340 : if (DEBUG) { System.out.println("IntertypeClassHeaderName ::= IntertypeClassHeaderName1"); }  //$NON-NLS-1$
			    consumeIntertypeTypeHeaderNameWithTypeParameters();  
				break;
	 
	    case 342 : if (DEBUG) { System.out.println("IntertypeClassHeaderName1 ::= Modifiersopt class OnType"); }  //$NON-NLS-1$
			    consumeIntertypeClassHeaderName(false);  
				break;
	 
	    case 343 : if (DEBUG) { System.out.println("InterTypeClassHeaderName1 ::= Modifiersopt class OnType"); }  //$NON-NLS-1$
			    consumeIntertypeClassHeaderName(true);  
				break;
	 
	    case 344 : if (DEBUG) { System.out.println("ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt..."); }  //$NON-NLS-1$
			    consumeClassHeader();  
				break;
	 
	    case 345 : if (DEBUG) { System.out.println("ClassHeaderName ::= ClassHeaderName1 TypeParameters"); }  //$NON-NLS-1$
			    consumeTypeHeaderNameWithTypeParameters();  
				break;
	 
	    case 347 : if (DEBUG) { System.out.println("ClassHeaderName1 ::= Modifiersopt class JavaIdentifier"); }  //$NON-NLS-1$
			    consumeClassHeaderName1();  
				break;
	 
	    case 348 : if (DEBUG) { System.out.println("ClassHeaderExtends ::= extends ClassType"); }  //$NON-NLS-1$
			    consumeClassHeaderExtends();  
				break;
	 
	    case 349 : if (DEBUG) { System.out.println("ClassHeaderImplements ::= implements InterfaceTypeList"); }  //$NON-NLS-1$
			    consumeClassHeaderImplements();  
				break;
	 
	    case 351 : if (DEBUG) { System.out.println("InterfaceTypeList ::= InterfaceTypeList COMMA..."); }  //$NON-NLS-1$
			    consumeInterfaceTypeList();  
				break;
	 
	    case 352 : if (DEBUG) { System.out.println("InterfaceType ::= ClassOrInterfaceType"); }  //$NON-NLS-1$
			    consumeInterfaceType();  
				break;
	 
	    case 355 : if (DEBUG) { System.out.println("ClassBodyDeclarations ::= ClassBodyDeclarations..."); }  //$NON-NLS-1$
			    consumeClassBodyDeclarations();  
				break;
	 
	    case 359 : if (DEBUG) { System.out.println("ClassBodyDeclaration ::= Diet NestedMethod..."); }  //$NON-NLS-1$
			    consumeClassBodyDeclaration();  
				break;
	 
	    case 360 : if (DEBUG) { System.out.println("Diet ::="); }  //$NON-NLS-1$
			    consumeDiet();  
				break;

	    case 361 : if (DEBUG) { System.out.println("Initializer ::= Diet NestedMethod CreateInitializer..."); }  //$NON-NLS-1$
			    consumeClassBodyDeclaration();  
				break;
	 
	    case 362 : if (DEBUG) { System.out.println("CreateInitializer ::="); }  //$NON-NLS-1$
			    consumeCreateInitializer();  
				break;

	    case 369 : if (DEBUG) { System.out.println("ClassMemberDeclaration ::= SEMICOLON"); }  //$NON-NLS-1$
			    consumeEmptyTypeDeclaration();  
				break;

	    case 372 : if (DEBUG) { System.out.println("FieldDeclaration ::= Modifiersopt Type..."); }  //$NON-NLS-1$
			    consumeFieldDeclaration();  
				break;
	 
	    case 374 : if (DEBUG) { System.out.println("VariableDeclarators ::= VariableDeclarators COMMA..."); }  //$NON-NLS-1$
			    consumeVariableDeclarators();  
				break;
	 
	    case 377 : if (DEBUG) { System.out.println("EnterVariable ::="); }  //$NON-NLS-1$
			    consumeEnterVariable();  
				break;
	 
	    case 378 : if (DEBUG) { System.out.println("ExitVariableWithInitialization ::="); }  //$NON-NLS-1$
			    consumeExitVariableWithInitialization();  
				break;
	 
	    case 379 : if (DEBUG) { System.out.println("ExitVariableWithoutInitialization ::="); }  //$NON-NLS-1$
			    consumeExitVariableWithoutInitialization();  
				break;
	 
	    case 380 : if (DEBUG) { System.out.println("ForceNoDiet ::="); }  //$NON-NLS-1$
			    consumeForceNoDiet();  
				break;
	 
	    case 381 : if (DEBUG) { System.out.println("RestoreDiet ::="); }  //$NON-NLS-1$
			    consumeRestoreDiet();  
				break;
	 
	    case 386 : if (DEBUG) { System.out.println("MethodDeclaration ::= MethodHeader MethodBody"); }  //$NON-NLS-1$
			    // set to true to consume a method with a body
	 consumeMethodDeclaration(true);  
				break;
	 
	    case 387 : if (DEBUG) { System.out.println("AbstractMethodDeclaration ::= MethodHeader SEMICOLON"); }  //$NON-NLS-1$
			    // set to false to consume a method without body
	 consumeMethodDeclaration(false);  
				break;
	 
	    case 388 : if (DEBUG) { System.out.println("MethodHeader ::= MethodHeaderName FormalParameterListopt"); }  //$NON-NLS-1$
			    consumeMethodHeader();  
				break;
	 
	    case 389 : if (DEBUG) { System.out.println("DefaultMethodHeader ::= DefaultMethodHeaderName..."); }  //$NON-NLS-1$
			    consumeMethodHeader();  
				break;
	 
	    case 390 : if (DEBUG) { System.out.println("MethodHeaderName ::= Modifiersopt TypeParameters Type..."); }  //$NON-NLS-1$
			    consumeMethodHeaderNameWithTypeParameters(false);  
				break;
	 
	    case 391 : if (DEBUG) { System.out.println("MethodHeaderName ::= Modifiersopt Type JavaIdentifier..."); }  //$NON-NLS-1$
			    consumeMethodHeaderName(false);  
				break;
	 
	    case 392 : if (DEBUG) { System.out.println("DefaultMethodHeaderName ::= ModifiersWithDefault..."); }  //$NON-NLS-1$
			    consumeMethodHeaderNameWithTypeParameters(false);  
				break;
	 
	    case 393 : if (DEBUG) { System.out.println("DefaultMethodHeaderName ::= ModifiersWithDefault Type..."); }  //$NON-NLS-1$
			    consumeMethodHeaderName(false);  
				break;
	 
	    case 394 : if (DEBUG) { System.out.println("ModifiersWithDefault ::= Modifiersopt default..."); }  //$NON-NLS-1$
			    consumePushCombineModifiers();  
				break;
	 
	    case 395 : if (DEBUG) { System.out.println("MethodHeaderRightParen ::= RPAREN"); }  //$NON-NLS-1$
			    consumeMethodHeaderRightParen();  
				break;
	 
	    case 396 : if (DEBUG) { System.out.println("MethodHeaderExtendedDims ::= Dimsopt"); }  //$NON-NLS-1$
			    consumeMethodHeaderExtendedDims();  
				break;
	 
	    case 397 : if (DEBUG) { System.out.println("MethodHeaderThrowsClause ::= throws ClassTypeList"); }  //$NON-NLS-1$
			    consumeMethodHeaderThrowsClause();  
				break;
	 
	    case 398 : if (DEBUG) { System.out.println("ConstructorHeader ::= ConstructorHeaderName..."); }  //$NON-NLS-1$
			    consumeConstructorHeader();  
				break;
	 
	    case 399 : if (DEBUG) { System.out.println("ConstructorHeaderName ::= Modifiersopt TypeParameters..."); }  //$NON-NLS-1$
			    consumeConstructorHeaderNameWithTypeParameters();  
				break;
	 
	    case 400 : if (DEBUG) { System.out.println("ConstructorHeaderName ::= Modifiersopt Identifier LPAREN"); }  //$NON-NLS-1$
			    consumeConstructorHeaderName();  
				break;
	 
	    case 401 : if (DEBUG) { System.out.println("ConstructorHeaderName ::= Modifiersopt aspect LPAREN"); }  //$NON-NLS-1$
			    consumeConstructorHeaderName();  
				break;
	 
	    case 403 : if (DEBUG) { System.out.println("FormalParameterList ::= FormalParameterList COMMA..."); }  //$NON-NLS-1$
			    consumeFormalParameterList();  
				break;
	 
	    case 404 : if (DEBUG) { System.out.println("FormalParameter ::= Modifiersopt Type..."); }  //$NON-NLS-1$
			    consumeFormalParameter(false);  
				break;
	 
	    case 405 : if (DEBUG) { System.out.println("FormalParameter ::= Modifiersopt Type..."); }  //$NON-NLS-1$
			    consumeFormalParameter(true);  
				break;
	 
	    case 406 : if (DEBUG) { System.out.println("FormalParameter ::= Modifiersopt Type AT308DOTDOTDOT..."); }  //$NON-NLS-1$
			    consumeFormalParameter(true);  
				break;
	 
	    case 407 : if (DEBUG) { System.out.println("CatchFormalParameter ::= Modifiersopt CatchType..."); }  //$NON-NLS-1$
			    consumeCatchFormalParameter();  
				break;
	 
	    case 408 : if (DEBUG) { System.out.println("CatchType ::= UnionType"); }  //$NON-NLS-1$
			    consumeCatchType();  
				break;
	 
	    case 409 : if (DEBUG) { System.out.println("UnionType ::= Type"); }  //$NON-NLS-1$
			    consumeUnionTypeAsClassType();  
				break;
	 
	    case 410 : if (DEBUG) { System.out.println("UnionType ::= UnionType OR Type"); }  //$NON-NLS-1$
			    consumeUnionType();  
				break;
	 
	    case 412 : if (DEBUG) { System.out.println("ClassTypeList ::= ClassTypeList COMMA ClassTypeElt"); }  //$NON-NLS-1$
			    consumeClassTypeList();  
				break;
	 
	    case 413 : if (DEBUG) { System.out.println("ClassTypeElt ::= ClassType"); }  //$NON-NLS-1$
			    consumeClassTypeElt();  
				break;
	 
	    case 414 : if (DEBUG) { System.out.println("MethodBody ::= NestedMethod LBRACE BlockStatementsopt..."); }  //$NON-NLS-1$
			    consumeMethodBody();  
				break;
	 
	    case 415 : if (DEBUG) { System.out.println("NestedMethod ::="); }  //$NON-NLS-1$
			    consumeNestedMethod();  
				break;
	 
	    case 416 : if (DEBUG) { System.out.println("StaticInitializer ::= StaticOnly Block"); }  //$NON-NLS-1$
			    consumeStaticInitializer();  
				break;

	    case 417 : if (DEBUG) { System.out.println("StaticOnly ::= static"); }  //$NON-NLS-1$
			    consumeStaticOnly();  
				break;
	 
	    case 418 : if (DEBUG) { System.out.println("ConstructorDeclaration ::= ConstructorHeader MethodBody"); }  //$NON-NLS-1$
			    consumeConstructorDeclaration() ;  
				break;
	 
	    case 419 : if (DEBUG) { System.out.println("ConstructorDeclaration ::= ConstructorHeader SEMICOLON"); }  //$NON-NLS-1$
			    consumeInvalidConstructorDeclaration() ;  
				break;
	 
	    case 420 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= this LPAREN..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocation(0, THIS_CALL);  
				break;
	 
	    case 421 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= OnlyTypeArguments this"); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocationWithTypeArguments(0,THIS_CALL);  
				break;
	 
	    case 422 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= super LPAREN..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocation(0,SUPER_CALL);  
				break;
	 
	    case 423 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= OnlyTypeArguments..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocationWithTypeArguments(0,SUPER_CALL);  
				break;
	 
	    case 424 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Primary DOT super..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocation(1, SUPER_CALL);  
				break;
	 
	    case 425 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Primary DOT..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocationWithTypeArguments(1, SUPER_CALL);  
				break;
	 
	    case 426 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Name DOT super LPAREN"); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocation(2, SUPER_CALL);  
				break;
	 
	    case 427 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Name DOT..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocationWithTypeArguments(2, SUPER_CALL);  
				break;
	 
	    case 428 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Primary DOT this..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocation(1, THIS_CALL);  
				break;
	 
	    case 429 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Primary DOT..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocationWithTypeArguments(1, THIS_CALL);  
				break;
	 
	    case 430 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Name DOT this LPAREN"); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocation(2, THIS_CALL);  
				break;
	 
	    case 431 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Name DOT..."); }  //$NON-NLS-1$
			    consumeExplicitConstructorInvocationWithTypeArguments(2, THIS_CALL);  
				break;
	 
	    case 432 : if (DEBUG) { System.out.println("InterfaceDeclaration ::= InterfaceHeader InterfaceBody"); }  //$NON-NLS-1$
			    consumeInterfaceDeclaration();  
				break;
	 
	    case 433 : if (DEBUG) { System.out.println("InterfaceHeader ::= InterfaceHeaderName..."); }  //$NON-NLS-1$
			    consumeInterfaceHeader();  
				break;
	 
	    case 434 : if (DEBUG) { System.out.println("InterfaceHeaderName ::= InterfaceHeaderName1..."); }  //$NON-NLS-1$
			    consumeTypeHeaderNameWithTypeParameters();  
				break;
	 
	    case 436 : if (DEBUG) { System.out.println("InterfaceHeaderName1 ::= Modifiersopt interface..."); }  //$NON-NLS-1$
			    consumeInterfaceHeaderName1();  
				break;
	 
	    case 437 : if (DEBUG) { System.out.println("InterfaceHeaderExtends ::= extends InterfaceTypeList"); }  //$NON-NLS-1$
			    consumeInterfaceHeaderExtends();  
				break;
	 
	    case 440 : if (DEBUG) { System.out.println("InterfaceMemberDeclarations ::=..."); }  //$NON-NLS-1$
			    consumeInterfaceMemberDeclarations();  
				break;
	 
	    case 441 : if (DEBUG) { System.out.println("InterfaceMemberDeclaration ::= SEMICOLON"); }  //$NON-NLS-1$
			    consumeEmptyTypeDeclaration();  
				break;
	 
	    case 443 : if (DEBUG) { System.out.println("InterfaceMemberDeclaration ::= DefaultMethodHeader..."); }  //$NON-NLS-1$
			    consumeInterfaceMethodDeclaration(false);  
				break;
	 
	    case 444 : if (DEBUG) { System.out.println("InterfaceMemberDeclaration ::= MethodHeader MethodBody"); }  //$NON-NLS-1$
			    consumeInterfaceMethodDeclaration(false);  
				break;
	 
	    case 445 : if (DEBUG) { System.out.println("InterfaceMemberDeclaration ::= DefaultMethodHeader..."); }  //$NON-NLS-1$
			    consumeInterfaceMethodDeclaration(true);  
				break;
	 
	    case 446 : if (DEBUG) { System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader..."); }  //$NON-NLS-1$
			    consumeInvalidConstructorDeclaration(true);  
				break;
	 
	    case 447 : if (DEBUG) { System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader..."); }  //$NON-NLS-1$
			    consumeInvalidConstructorDeclaration(false);  
				break;
	 
	    case 458 : if (DEBUG) { System.out.println("PushLeftBrace ::="); }  //$NON-NLS-1$
			    consumePushLeftBrace();  
				break;
	 
	    case 459 : if (DEBUG) { System.out.println("ArrayInitializer ::= LBRACE PushLeftBrace ,opt RBRACE"); }  //$NON-NLS-1$
			    consumeEmptyArrayInitializer();  
				break;
	 
	    case 460 : if (DEBUG) { System.out.println("ArrayInitializer ::= LBRACE PushLeftBrace..."); }  //$NON-NLS-1$
			    consumeArrayInitializer();  
				break;
	 
	    case 461 : if (DEBUG) { System.out.println("ArrayInitializer ::= LBRACE PushLeftBrace..."); }  //$NON-NLS-1$
			    consumeArrayInitializer();  
				break;
	 
	    case 463 : if (DEBUG) { System.out.println("VariableInitializers ::= VariableInitializers COMMA..."); }  //$NON-NLS-1$
			    consumeVariableInitializers();  
				break;
	 
	    case 464 : if (DEBUG) { System.out.println("Block ::= OpenBlock LBRACE BlockStatementsopt RBRACE"); }  //$NON-NLS-1$
			    consumeBlock();  
				break;
	 
	    case 465 : if (DEBUG) { System.out.println("OpenBlock ::="); }  //$NON-NLS-1$
			    consumeOpenBlock() ;  
				break;
	 
	    case 467 : if (DEBUG) { System.out.println("BlockStatements ::= BlockStatements BlockStatement"); }  //$NON-NLS-1$
			    consumeBlockStatements() ;  
				break;
	 
	    case 471 : if (DEBUG) { System.out.println("BlockStatement ::= InterfaceDeclaration"); }  //$NON-NLS-1$
			    consumeInvalidInterfaceDeclaration();  
				break;
	 
	    case 472 : if (DEBUG) { System.out.println("BlockStatement ::= AnnotationTypeDeclaration"); }  //$NON-NLS-1$
			    consumeInvalidAnnotationTypeDeclaration();  
				break;
	 
	    case 473 : if (DEBUG) { System.out.println("BlockStatement ::= EnumDeclaration"); }  //$NON-NLS-1$
			    consumeInvalidEnumDeclaration();  
				break;
	 
	    case 474 : if (DEBUG) { System.out.println("LocalVariableDeclarationStatement ::=..."); }  //$NON-NLS-1$
			    consumeLocalVariableDeclarationStatement();  
				break;
	 
	    case 475 : if (DEBUG) { System.out.println("LocalVariableDeclaration ::= Type PushModifiers..."); }  //$NON-NLS-1$
			    consumeLocalVariableDeclaration();  
				break;
	 
	    case 476 : if (DEBUG) { System.out.println("LocalVariableDeclaration ::= Modifiers Type..."); }  //$NON-NLS-1$
			    consumeLocalVariableDeclaration();  
				break;
	 
	    case 477 : if (DEBUG) { System.out.println("PushModifiers ::="); }  //$NON-NLS-1$
			    consumePushModifiers();  
				break;
	 
	    case 478 : if (DEBUG) { System.out.println("PushModifiersForHeader ::="); }  //$NON-NLS-1$
			    consumePushModifiersForHeader();  
				break;
	 
	    case 479 : if (DEBUG) { System.out.println("PushRealModifiers ::="); }  //$NON-NLS-1$
			    consumePushRealModifiers();  
				break;
	 
	    case 506 : if (DEBUG) { System.out.println("EmptyStatement ::= SEMICOLON"); }  //$NON-NLS-1$
			    consumeEmptyStatement();  
				break;
	 
	    case 507 : if (DEBUG) { System.out.println("LabeledStatement ::= Label COLON Statement"); }  //$NON-NLS-1$
			    consumeStatementLabel() ;  
				break;
	 
	    case 508 : if (DEBUG) { System.out.println("LabeledStatementNoShortIf ::= Label COLON..."); }  //$NON-NLS-1$
			    consumeStatementLabel() ;  
				break;
	 
	    case 509 : if (DEBUG) { System.out.println("Label ::= JavaIdentifier"); }  //$NON-NLS-1$
			    consumeLabel() ;  
				break;
	 
	     case 510 : if (DEBUG) { System.out.println("ExpressionStatement ::= StatementExpression SEMICOLON"); }  //$NON-NLS-1$
			    consumeExpressionStatement();  
				break;
	 
	    case 519 : if (DEBUG) { System.out.println("IfThenStatement ::= if LPAREN Expression RPAREN..."); }  //$NON-NLS-1$
			    consumeStatementIfNoElse();  
				break;
	 
	    case 520 : if (DEBUG) { System.out.println("IfThenElseStatement ::= if LPAREN Expression RPAREN..."); }  //$NON-NLS-1$
			    consumeStatementIfWithElse();  
				break;
	 
	    case 521 : if (DEBUG) { System.out.println("IfThenElseStatementNoShortIf ::= if LPAREN Expression..."); }  //$NON-NLS-1$
			    consumeStatementIfWithElse();  
				break;
	 
	    case 522 : if (DEBUG) { System.out.println("SwitchStatement ::= switch LPAREN Expression RPAREN..."); }  //$NON-NLS-1$
			    consumeStatementSwitch() ;  
				break;
	 
	    case 523 : if (DEBUG) { System.out.println("SwitchBlock ::= LBRACE RBRACE"); }  //$NON-NLS-1$
			    consumeEmptySwitchBlock() ;  
				break;
	 
	    case 526 : if (DEBUG) { System.out.println("SwitchBlock ::= LBRACE SwitchBlockStatements..."); }  //$NON-NLS-1$
			    consumeSwitchBlock() ;  
				break;
	 
	    case 528 : if (DEBUG) { System.out.println("SwitchBlockStatements ::= SwitchBlockStatements..."); }  //$NON-NLS-1$
			    consumeSwitchBlockStatements() ;  
				break;
	 
	    case 529 : if (DEBUG) { System.out.println("SwitchBlockStatement ::= SwitchLabels BlockStatements"); }  //$NON-NLS-1$
			    consumeSwitchBlockStatement() ;  
				break;
	 
	    case 531 : if (DEBUG) { System.out.println("SwitchLabels ::= SwitchLabels SwitchLabel"); }  //$NON-NLS-1$
			    consumeSwitchLabels() ;  
				break;
	 
	     case 532 : if (DEBUG) { System.out.println("SwitchLabel ::= case ConstantExpression COLON"); }  //$NON-NLS-1$
			    consumeCaseLabel();  
				break;
	 
	     case 533 : if (DEBUG) { System.out.println("SwitchLabel ::= default COLON"); }  //$NON-NLS-1$
			    consumeDefaultLabel();  
				break;
	 
	    case 534 : if (DEBUG) { System.out.println("WhileStatement ::= while LPAREN Expression RPAREN..."); }  //$NON-NLS-1$
			    consumeStatementWhile() ;  
				break;
	 
	    case 535 : if (DEBUG) { System.out.println("WhileStatementNoShortIf ::= while LPAREN Expression..."); }  //$NON-NLS-1$
			    consumeStatementWhile() ;  
				break;
	 
	    case 536 : if (DEBUG) { System.out.println("DoStatement ::= do Statement while LPAREN Expression..."); }  //$NON-NLS-1$
			    consumeStatementDo() ;  
				break;
	 
	    case 537 : if (DEBUG) { System.out.println("ForStatement ::= for LPAREN ForInitopt SEMICOLON..."); }  //$NON-NLS-1$
			    consumeStatementFor() ;  
				break;
	 
	    case 538 : if (DEBUG) { System.out.println("ForStatementNoShortIf ::= for LPAREN ForInitopt..."); }  //$NON-NLS-1$
			    consumeStatementFor() ;  
				break;
	 
	    case 539 : if (DEBUG) { System.out.println("ForInit ::= StatementExpressionList"); }  //$NON-NLS-1$
			    consumeForInit() ;  
				break;
	 
	    case 543 : if (DEBUG) { System.out.println("StatementExpressionList ::= StatementExpressionList..."); }  //$NON-NLS-1$
			    consumeStatementExpressionList() ;  
				break;
	 
	    case 544 : if (DEBUG) { System.out.println("AssertStatement ::= assert Expression SEMICOLON"); }  //$NON-NLS-1$
			    consumeSimpleAssertStatement() ;  
				break;
	 
	    case 545 : if (DEBUG) { System.out.println("AssertStatement ::= assert Expression COLON Expression"); }  //$NON-NLS-1$
			    consumeAssertStatement() ;  
				break;
	 
	    case 546 : if (DEBUG) { System.out.println("BreakStatement ::= break SEMICOLON"); }  //$NON-NLS-1$
			    consumeStatementBreak() ;  
				break;
	 
	    case 547 : if (DEBUG) { System.out.println("BreakStatement ::= break Identifier SEMICOLON"); }  //$NON-NLS-1$
			    consumeStatementBreakWithLabel() ;  
				break;
	 
	    case 548 : if (DEBUG) { System.out.println("ContinueStatement ::= continue SEMICOLON"); }  //$NON-NLS-1$
			    consumeStatementContinue() ;  
				break;
	 
	    case 549 : if (DEBUG) { System.out.println("ContinueStatement ::= continue Identifier SEMICOLON"); }  //$NON-NLS-1$
			    consumeStatementContinueWithLabel() ;  
				break;
	 
	    case 550 : if (DEBUG) { System.out.println("ReturnStatement ::= return Expressionopt SEMICOLON"); }  //$NON-NLS-1$
			    consumeStatementReturn() ;  
				break;
	 
	    case 551 : if (DEBUG) { System.out.println("ThrowStatement ::= throw Expression SEMICOLON"); }  //$NON-NLS-1$
			    consumeStatementThrow();  
				break;
	 
	    case 552 : if (DEBUG) { System.out.println("SynchronizedStatement ::= OnlySynchronized LPAREN..."); }  //$NON-NLS-1$
			    consumeStatementSynchronized();  
				break;
	 
	    case 553 : if (DEBUG) { System.out.println("OnlySynchronized ::= synchronized"); }  //$NON-NLS-1$
			    consumeOnlySynchronized();  
				break;
	 
	    case 554 : if (DEBUG) { System.out.println("TryStatement ::= try TryBlock Catches"); }  //$NON-NLS-1$
			    consumeStatementTry(false, false);  
				break;
	 
	    case 555 : if (DEBUG) { System.out.println("TryStatement ::= try TryBlock Catchesopt Finally"); }  //$NON-NLS-1$
			    consumeStatementTry(true, false);  
				break;
	 
	    case 556 : if (DEBUG) { System.out.println("TryStatementWithResources ::= try ResourceSpecification"); }  //$NON-NLS-1$
			    consumeStatementTry(false, true);  
				break;
	 
	    case 557 : if (DEBUG) { System.out.println("TryStatementWithResources ::= try ResourceSpecification"); }  //$NON-NLS-1$
			    consumeStatementTry(true, true);  
				break;
	 
	    case 558 : if (DEBUG) { System.out.println("ResourceSpecification ::= LPAREN Resources ;opt RPAREN"); }  //$NON-NLS-1$
			    consumeResourceSpecification();  
				break;
	 
	    case 559 : if (DEBUG) { System.out.println(";opt ::="); }  //$NON-NLS-1$
			    consumeResourceOptionalTrailingSemiColon(false);  
				break;
	 
	    case 560 : if (DEBUG) { System.out.println(";opt ::= SEMICOLON"); }  //$NON-NLS-1$
			    consumeResourceOptionalTrailingSemiColon(true);  
				break;
	 
	    case 561 : if (DEBUG) { System.out.println("Resources ::= Resource"); }  //$NON-NLS-1$
			    consumeSingleResource();  
				break;
	 
	    case 562 : if (DEBUG) { System.out.println("Resources ::= Resources TrailingSemiColon Resource"); }  //$NON-NLS-1$
			    consumeMultipleResources();  
				break;
	 
	    case 563 : if (DEBUG) { System.out.println("TrailingSemiColon ::= SEMICOLON"); }  //$NON-NLS-1$
			    consumeResourceOptionalTrailingSemiColon(true);  
				break;
	 
	    case 564 : if (DEBUG) { System.out.println("Resource ::= Type PushModifiers VariableDeclaratorId..."); }  //$NON-NLS-1$
			    consumeResourceAsLocalVariableDeclaration();  
				break;
	 
	    case 565 : if (DEBUG) { System.out.println("Resource ::= Modifiers Type PushRealModifiers..."); }  //$NON-NLS-1$
			    consumeResourceAsLocalVariableDeclaration();  
				break;
	 
	    case 567 : if (DEBUG) { System.out.println("ExitTryBlock ::="); }  //$NON-NLS-1$
			    consumeExitTryBlock();  
				break;
	 
	    case 569 : if (DEBUG) { System.out.println("Catches ::= Catches CatchClause"); }  //$NON-NLS-1$
			    consumeCatches();  
				break;
	 
	    case 570 : if (DEBUG) { System.out.println("CatchClause ::= catch LPAREN CatchFormalParameter RPAREN"); }  //$NON-NLS-1$
			    consumeStatementCatch() ;  
				break;
	 
	    case 572 : if (DEBUG) { System.out.println("PushLPAREN ::= LPAREN"); }  //$NON-NLS-1$
			    consumeLeftParen();  
				break;
	 
	    case 573 : if (DEBUG) { System.out.println("PushRPAREN ::= RPAREN"); }  //$NON-NLS-1$
			    consumeRightParen();  
				break;
	 
	    case 578 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= this"); }  //$NON-NLS-1$
			    consumePrimaryNoNewArrayThis();  
				break;
	 
	    case 579 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= PushLPAREN Expression_NotName..."); }  //$NON-NLS-1$
			    consumePrimaryNoNewArray();  
				break;
	 
	    case 580 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= PushLPAREN Name PushRPAREN"); }  //$NON-NLS-1$
			    consumePrimaryNoNewArrayWithName();  
				break;
	 
	    case 583 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= Name DOT this"); }  //$NON-NLS-1$
			    consumePrimaryNoNewArrayNameThis();  
				break;
	 
	    case 584 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= Name DOT super"); }  //$NON-NLS-1$
			    consumePrimaryNoNewArrayNameSuper();  
				break;
	 
	    case 585 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= Name DOT class"); }  //$NON-NLS-1$
			    consumePrimaryNoNewArrayName();  
				break;
	 
	    case 586 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= Name Dims DOT class"); }  //$NON-NLS-1$
			    consumePrimaryNoNewArrayArrayType();  
				break;
	 
	    case 587 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= PrimitiveType Dims DOT class"); }  //$NON-NLS-1$
			    consumePrimaryNoNewArrayPrimitiveArrayType();  
				break;
	 
	    case 588 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= PrimitiveType DOT class"); }  //$NON-NLS-1$
			    consumePrimaryNoNewArrayPrimitiveType();  
				break;
	 
	    case 594 : if (DEBUG) { System.out.println("ReferenceExpressionTypeArgumentsAndTrunk0 ::=..."); }  //$NON-NLS-1$
			    consumeReferenceExpressionTypeArgumentsAndTrunk(false);  
				break;
	 
	    case 595 : if (DEBUG) { System.out.println("ReferenceExpressionTypeArgumentsAndTrunk0 ::=..."); }  //$NON-NLS-1$
			    consumeReferenceExpressionTypeArgumentsAndTrunk(true);  
				break;
	 
	    case 596 : if (DEBUG) { System.out.println("ReferenceExpression ::= PrimitiveType Dims COLON_COLON"); }  //$NON-NLS-1$
			    consumeReferenceExpressionTypeForm(true);  
				break;
	 
	    case 597 : if (DEBUG) { System.out.println("ReferenceExpression ::= Name Dimsopt COLON_COLON..."); }  //$NON-NLS-1$
			    consumeReferenceExpressionTypeForm(false);  
				break;
	 
	    case 598 : if (DEBUG) { System.out.println("ReferenceExpression ::= Name BeginTypeArguments..."); }  //$NON-NLS-1$
			    consumeReferenceExpressionGenericTypeForm();  
				break;
	 
	    case 599 : if (DEBUG) { System.out.println("ReferenceExpression ::= Primary COLON_COLON..."); }  //$NON-NLS-1$
			    consumeReferenceExpressionPrimaryForm();  
				break;
	 
	    case 600 : if (DEBUG) { System.out.println("ReferenceExpression ::= super COLON_COLON..."); }  //$NON-NLS-1$
			    consumeReferenceExpressionSuperForm();  
				break;
	 
	    case 601 : if (DEBUG) { System.out.println("NonWildTypeArgumentsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyTypeArguments();  
				break;
	 
	    case 603 : if (DEBUG) { System.out.println("IdentifierOrNew ::= Identifier"); }  //$NON-NLS-1$
			    consumeIdentifierOrNew(false);  
				break;
	 
	    case 604 : if (DEBUG) { System.out.println("IdentifierOrNew ::= new"); }  //$NON-NLS-1$
			    consumeIdentifierOrNew(true);  
				break;
	 
	    case 605 : if (DEBUG) { System.out.println("LambdaExpression ::= LambdaParameters ARROW LambdaBody"); }  //$NON-NLS-1$
			    consumeLambdaExpression();  
				break;
	 
	    case 606 : if (DEBUG) { System.out.println("LambdaParameters ::= Identifier"); }  //$NON-NLS-1$
			    consumeTypeElidedLambdaParameter(false);  
				break;
	 
	    case 612 : if (DEBUG) { System.out.println("TypeElidedFormalParameterList ::=..."); }  //$NON-NLS-1$
			    consumeFormalParameterList();  
				break;
	 
	    case 613 : if (DEBUG) { System.out.println("TypeElidedFormalParameter ::= Modifiersopt Identifier"); }  //$NON-NLS-1$
			    consumeTypeElidedLambdaParameter(true);  
				break;
	 
	    case 615 : if (DEBUG) { System.out.println("LambdaBody ::= NestedType NestedMethod LBRACE..."); }  //$NON-NLS-1$
			    consumeBlock();  
				break;
	 
	    case 616 : if (DEBUG) { System.out.println("ElidedLeftBraceAndReturn ::="); }  //$NON-NLS-1$
			    consumeElidedLeftBraceAndReturn();  
				break;
	 
	    case 617 : if (DEBUG) { System.out.println("AllocationHeader ::= new ClassType LPAREN..."); }  //$NON-NLS-1$
			    consumeAllocationHeader();  
				break;
	 
	    case 618 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::= new..."); }  //$NON-NLS-1$
			    consumeClassInstanceCreationExpressionWithTypeArguments();  
				break;
	 
	    case 619 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::= new ClassType..."); }  //$NON-NLS-1$
			    consumeClassInstanceCreationExpression();  
				break;
	 
	    case 620 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::= Primary DOT new..."); }  //$NON-NLS-1$
			    consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() ;  
				break;
	 
	    case 621 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::= Primary DOT new..."); }  //$NON-NLS-1$
			    consumeClassInstanceCreationExpressionQualified() ;  
				break;
	 
	    case 622 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::=..."); }  //$NON-NLS-1$
			    consumeClassInstanceCreationExpressionQualified() ;  
				break;
	 
	    case 623 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::=..."); }  //$NON-NLS-1$
			    consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() ;  
				break;
	 
	    case 624 : if (DEBUG) { System.out.println("EnterInstanceCreationArgumentList ::="); }  //$NON-NLS-1$
			    consumeEnterInstanceCreationArgumentList();  
				break;
	 
	    case 625 : if (DEBUG) { System.out.println("ClassInstanceCreationExpressionName ::= Name DOT"); }  //$NON-NLS-1$
			    consumeClassInstanceCreationExpressionName() ;  
				break;
	 
	    case 626 : if (DEBUG) { System.out.println("UnqualifiedClassBodyopt ::="); }  //$NON-NLS-1$
			    consumeClassBodyopt();  
				break;
	 
	    case 628 : if (DEBUG) { System.out.println("UnqualifiedEnterAnonymousClassBody ::="); }  //$NON-NLS-1$
			    consumeEnterAnonymousClassBody(false);  
				break;
	 
	    case 629 : if (DEBUG) { System.out.println("QualifiedClassBodyopt ::="); }  //$NON-NLS-1$
			    consumeClassBodyopt();  
				break;
	 
	    case 631 : if (DEBUG) { System.out.println("QualifiedEnterAnonymousClassBody ::="); }  //$NON-NLS-1$
			    consumeEnterAnonymousClassBody(true);  
				break;
	 
	    case 633 : if (DEBUG) { System.out.println("ArgumentList ::= ArgumentList COMMA Expression"); }  //$NON-NLS-1$
			    consumeArgumentList();  
				break;
	 
	    case 634 : if (DEBUG) { System.out.println("ArrayCreationHeader ::= new PrimitiveType..."); }  //$NON-NLS-1$
			    consumeArrayCreationHeader();  
				break;
	 
	    case 635 : if (DEBUG) { System.out.println("ArrayCreationHeader ::= new ClassOrInterfaceType..."); }  //$NON-NLS-1$
			    consumeArrayCreationHeader();  
				break;
	 
	    case 636 : if (DEBUG) { System.out.println("ArrayCreationWithoutArrayInitializer ::= new..."); }  //$NON-NLS-1$
			    consumeArrayCreationExpressionWithoutInitializer();  
				break;
	 
	    case 637 : if (DEBUG) { System.out.println("ArrayCreationWithArrayInitializer ::= new PrimitiveType"); }  //$NON-NLS-1$
			    consumeArrayCreationExpressionWithInitializer();  
				break;
	 
	    case 638 : if (DEBUG) { System.out.println("ArrayCreationWithoutArrayInitializer ::= new..."); }  //$NON-NLS-1$
			    consumeArrayCreationExpressionWithoutInitializer();  
				break;
	 
	    case 639 : if (DEBUG) { System.out.println("ArrayCreationWithArrayInitializer ::= new..."); }  //$NON-NLS-1$
			    consumeArrayCreationExpressionWithInitializer();  
				break;
	 
	    case 641 : if (DEBUG) { System.out.println("DimWithOrWithOutExprs ::= DimWithOrWithOutExprs..."); }  //$NON-NLS-1$
			    consumeDimWithOrWithOutExprs();  
				break;
	 
	     case 643 : if (DEBUG) { System.out.println("DimWithOrWithOutExpr ::= TypeAnnotationsopt LBRACKET..."); }  //$NON-NLS-1$
			    consumeDimWithOrWithOutExpr();  
				break;
	 
	     case 644 : if (DEBUG) { System.out.println("Dims ::= DimsLoop"); }  //$NON-NLS-1$
			    consumeDims();  
				break;
	 
	     case 647 : if (DEBUG) { System.out.println("OneDimLoop ::= LBRACKET RBRACKET"); }  //$NON-NLS-1$
			    consumeOneDimLoop(false);  
				break;
	 
	     case 648 : if (DEBUG) { System.out.println("OneDimLoop ::= TypeAnnotations LBRACKET RBRACKET"); }  //$NON-NLS-1$
			    consumeOneDimLoop(true);  
				break;
	 
	    case 649 : if (DEBUG) { System.out.println("FieldAccess ::= Primary DOT JavaIdentifier"); }  //$NON-NLS-1$
			    consumeFieldAccess(false);  
				break;
	 
	    case 650 : if (DEBUG) { System.out.println("FieldAccess ::= super DOT JavaIdentifier"); }  //$NON-NLS-1$
			    consumeFieldAccess(true);  
				break;
	 
	    case 651 : if (DEBUG) { System.out.println("MethodInvocation ::= NameOrAj LPAREN ArgumentListopt..."); }  //$NON-NLS-1$
			    consumeMethodInvocationName();  
				break;
	 
	    case 652 : if (DEBUG) { System.out.println("MethodInvocation ::= Name DOT OnlyTypeArguments..."); }  //$NON-NLS-1$
			    consumeMethodInvocationNameWithTypeArguments();  
				break;
	 
	    case 653 : if (DEBUG) { System.out.println("MethodInvocation ::= Primary DOT OnlyTypeArguments..."); }  //$NON-NLS-1$
			    consumeMethodInvocationPrimaryWithTypeArguments();  
				break;
	 
	    case 654 : if (DEBUG) { System.out.println("MethodInvocation ::= Primary DOT JavaIdentifier LPAREN"); }  //$NON-NLS-1$
			    consumeMethodInvocationPrimary();  
				break;
	 
	    case 655 : if (DEBUG) { System.out.println("MethodInvocation ::= super DOT OnlyTypeArguments..."); }  //$NON-NLS-1$
			    consumeMethodInvocationSuperWithTypeArguments();  
				break;
	 
	    case 656 : if (DEBUG) { System.out.println("MethodInvocation ::= super DOT JavaIdentifier LPAREN..."); }  //$NON-NLS-1$
			    consumeMethodInvocationSuper();  
				break;
	 
	    case 657 : if (DEBUG) { System.out.println("ArrayAccess ::= Name LBRACKET Expression RBRACKET"); }  //$NON-NLS-1$
			    consumeArrayAccess(true);  
				break;
	 
	    case 658 : if (DEBUG) { System.out.println("ArrayAccess ::= AjName LBRACKET Expression RBRACKET"); }  //$NON-NLS-1$
			    consumeArrayAccess(true);  
				break;
	 
	    case 659 : if (DEBUG) { System.out.println("ArrayAccess ::= PrimaryNoNewArray LBRACKET Expression..."); }  //$NON-NLS-1$
			    consumeArrayAccess(false);  
				break;
	 
	    case 660 : if (DEBUG) { System.out.println("ArrayAccess ::= ArrayCreationWithArrayInitializer..."); }  //$NON-NLS-1$
			    consumeArrayAccess(false);  
				break;
	 
	    case 662 : if (DEBUG) { System.out.println("PostfixExpression ::= NameOrAj"); }  //$NON-NLS-1$
			    consumePostfixExpression();  
				break;
	 
	    case 665 : if (DEBUG) { System.out.println("PostIncrementExpression ::= PostfixExpression PLUS_PLUS"); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.PLUS,true);  
				break;
	 
	    case 666 : if (DEBUG) { System.out.println("PostDecrementExpression ::= PostfixExpression..."); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.MINUS,true);  
				break;
	 
	    case 667 : if (DEBUG) { System.out.println("PushPosition ::="); }  //$NON-NLS-1$
			    consumePushPosition();  
				break;
	 
	    case 670 : if (DEBUG) { System.out.println("UnaryExpression ::= PLUS PushPosition UnaryExpression"); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.PLUS);  
				break;
	 
	    case 671 : if (DEBUG) { System.out.println("UnaryExpression ::= MINUS PushPosition UnaryExpression"); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.MINUS);  
				break;
	 
	    case 673 : if (DEBUG) { System.out.println("PreIncrementExpression ::= PLUS_PLUS PushPosition..."); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.PLUS,false);  
				break;
	 
	    case 674 : if (DEBUG) { System.out.println("PreDecrementExpression ::= MINUS_MINUS PushPosition..."); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.MINUS,false);  
				break;
	 
	    case 676 : if (DEBUG) { System.out.println("UnaryExpressionNotPlusMinus ::= TWIDDLE PushPosition..."); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.TWIDDLE);  
				break;
	 
	    case 677 : if (DEBUG) { System.out.println("UnaryExpressionNotPlusMinus ::= NOT PushPosition..."); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.NOT);  
				break;
	 
	    case 679 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN PrimitiveType Dimsopt..."); }  //$NON-NLS-1$
			    consumeCastExpressionWithPrimitiveType();  
				break;
	 
	    case 680 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name..."); }  //$NON-NLS-1$
			    consumeCastExpressionWithGenericsArray();  
				break;
	 
	    case 681 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name..."); }  //$NON-NLS-1$
			    consumeCastExpressionWithQualifiedGenericsArray();  
				break;
	 
	    case 682 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name PushRPAREN..."); }  //$NON-NLS-1$
			    consumeCastExpressionLL1();  
				break;
	 
	    case 683 : if (DEBUG) { System.out.println("CastExpression ::= BeginIntersectionCast PushLPAREN..."); }  //$NON-NLS-1$
			    consumeCastExpressionLL1WithBounds();  
				break;
	 
	    case 684 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name Dims..."); }  //$NON-NLS-1$
			    consumeCastExpressionWithNameArray();  
				break;
	 
	    case 685 : if (DEBUG) { System.out.println("AdditionalBoundsListOpt ::="); }  //$NON-NLS-1$
			    consumeZeroAdditionalBounds();  
				break;
	 
	    case 689 : if (DEBUG) { System.out.println("OnlyTypeArgumentsForCastExpression ::= OnlyTypeArguments"); }  //$NON-NLS-1$
			    consumeOnlyTypeArgumentsForCastExpression();  
				break;
	 
	    case 690 : if (DEBUG) { System.out.println("InsideCastExpression ::="); }  //$NON-NLS-1$
			    consumeInsideCastExpression();  
				break;
	 
	    case 691 : if (DEBUG) { System.out.println("InsideCastExpressionLL1 ::="); }  //$NON-NLS-1$
			    consumeInsideCastExpressionLL1();  
				break;
	 
	    case 692 : if (DEBUG) { System.out.println("InsideCastExpressionLL1WithBounds ::="); }  //$NON-NLS-1$
			    consumeInsideCastExpressionLL1WithBounds ();  
				break;
	 
	    case 693 : if (DEBUG) { System.out.println("InsideCastExpressionWithQualifiedGenerics ::="); }  //$NON-NLS-1$
			    consumeInsideCastExpressionWithQualifiedGenerics();  
				break;
	 
	    case 695 : if (DEBUG) { System.out.println("MultiplicativeExpression ::= MultiplicativeExpression..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.MULTIPLY);  
				break;
	 
	    case 696 : if (DEBUG) { System.out.println("MultiplicativeExpression ::= MultiplicativeExpression..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.DIVIDE);  
				break;
	 
	    case 697 : if (DEBUG) { System.out.println("MultiplicativeExpression ::= MultiplicativeExpression..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.REMAINDER);  
				break;
	 
	    case 699 : if (DEBUG) { System.out.println("AdditiveExpression ::= AdditiveExpression PLUS..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.PLUS);  
				break;
	 
	    case 700 : if (DEBUG) { System.out.println("AdditiveExpression ::= AdditiveExpression MINUS..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.MINUS);  
				break;
	 
	    case 702 : if (DEBUG) { System.out.println("ShiftExpression ::= ShiftExpression LEFT_SHIFT..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.LEFT_SHIFT);  
				break;
	 
	    case 703 : if (DEBUG) { System.out.println("ShiftExpression ::= ShiftExpression RIGHT_SHIFT..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.RIGHT_SHIFT);  
				break;
	 
	    case 704 : if (DEBUG) { System.out.println("ShiftExpression ::= ShiftExpression UNSIGNED_RIGHT_SHIFT"); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.UNSIGNED_RIGHT_SHIFT);  
				break;
	 
	    case 706 : if (DEBUG) { System.out.println("RelationalExpression ::= RelationalExpression LESS..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.LESS);  
				break;
	 
	    case 707 : if (DEBUG) { System.out.println("RelationalExpression ::= RelationalExpression GREATER..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.GREATER);  
				break;
	 
	    case 708 : if (DEBUG) { System.out.println("RelationalExpression ::= RelationalExpression LESS_EQUAL"); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.LESS_EQUAL);  
				break;
	 
	    case 709 : if (DEBUG) { System.out.println("RelationalExpression ::= RelationalExpression..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.GREATER_EQUAL);  
				break;
	 
	    case 711 : if (DEBUG) { System.out.println("InstanceofExpression ::= InstanceofExpression instanceof"); }  //$NON-NLS-1$
			    consumeInstanceOfExpression();  
				break;
	 
	    case 713 : if (DEBUG) { System.out.println("EqualityExpression ::= EqualityExpression EQUAL_EQUAL..."); }  //$NON-NLS-1$
			    consumeEqualityExpression(OperatorIds.EQUAL_EQUAL);  
				break;
	 
	    case 714 : if (DEBUG) { System.out.println("EqualityExpression ::= EqualityExpression NOT_EQUAL..."); }  //$NON-NLS-1$
			    consumeEqualityExpression(OperatorIds.NOT_EQUAL);  
				break;
	 
	    case 716 : if (DEBUG) { System.out.println("AndExpression ::= AndExpression AND EqualityExpression"); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.AND);  
				break;
	 
	    case 718 : if (DEBUG) { System.out.println("ExclusiveOrExpression ::= ExclusiveOrExpression XOR..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.XOR);  
				break;
	 
	    case 720 : if (DEBUG) { System.out.println("InclusiveOrExpression ::= InclusiveOrExpression OR..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.OR);  
				break;
	 
	    case 722 : if (DEBUG) { System.out.println("ConditionalAndExpression ::= ConditionalAndExpression..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.AND_AND);  
				break;
	 
	    case 724 : if (DEBUG) { System.out.println("ConditionalOrExpression ::= ConditionalOrExpression..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.OR_OR);  
				break;
	 
	    case 726 : if (DEBUG) { System.out.println("ConditionalExpression ::= ConditionalOrExpression..."); }  //$NON-NLS-1$
			    consumeConditionalExpression(OperatorIds.QUESTIONCOLON) ;  
				break;
	 
	    case 729 : if (DEBUG) { System.out.println("Assignment ::= PostfixExpression AssignmentOperator..."); }  //$NON-NLS-1$
			    consumeAssignment();  
				break;
	 
	    case 731 : if (DEBUG) { System.out.println("Assignment ::= InvalidArrayInitializerAssignement"); }  //$NON-NLS-1$
			    ignoreExpressionAssignment(); 
				break;
	 
	    case 732 : if (DEBUG) { System.out.println("AssignmentOperator ::= EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(EQUAL);  
				break;
	 
	    case 733 : if (DEBUG) { System.out.println("AssignmentOperator ::= MULTIPLY_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(MULTIPLY);  
				break;
	 
	    case 734 : if (DEBUG) { System.out.println("AssignmentOperator ::= DIVIDE_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(DIVIDE);  
				break;
	 
	    case 735 : if (DEBUG) { System.out.println("AssignmentOperator ::= REMAINDER_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(REMAINDER);  
				break;
	 
	    case 736 : if (DEBUG) { System.out.println("AssignmentOperator ::= PLUS_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(PLUS);  
				break;
	 
	    case 737 : if (DEBUG) { System.out.println("AssignmentOperator ::= MINUS_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(MINUS);  
				break;
	 
	    case 738 : if (DEBUG) { System.out.println("AssignmentOperator ::= LEFT_SHIFT_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(LEFT_SHIFT);  
				break;
	 
	    case 739 : if (DEBUG) { System.out.println("AssignmentOperator ::= RIGHT_SHIFT_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(RIGHT_SHIFT);  
				break;
	 
	    case 740 : if (DEBUG) { System.out.println("AssignmentOperator ::= UNSIGNED_RIGHT_SHIFT_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(UNSIGNED_RIGHT_SHIFT);  
				break;
	 
	    case 741 : if (DEBUG) { System.out.println("AssignmentOperator ::= AND_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(AND);  
				break;
	 
	    case 742 : if (DEBUG) { System.out.println("AssignmentOperator ::= XOR_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(XOR);  
				break;
	 
	    case 743 : if (DEBUG) { System.out.println("AssignmentOperator ::= OR_EQUAL"); }  //$NON-NLS-1$
			    consumeAssignmentOperator(OR);  
				break;
	 
	    case 744 : if (DEBUG) { System.out.println("Expression ::= AssignmentExpression"); }  //$NON-NLS-1$
			    consumeExpression();  
				break;
	 
	    case 747 : if (DEBUG) { System.out.println("Expressionopt ::="); }  //$NON-NLS-1$
			    consumeEmptyExpression();  
				break;
	 
	    case 752 : if (DEBUG) { System.out.println("ClassBodyDeclarationsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyClassBodyDeclarationsopt();  
				break;
	 
	    case 753 : if (DEBUG) { System.out.println("ClassBodyDeclarationsopt ::= NestedType..."); }  //$NON-NLS-1$
			    consumeClassBodyDeclarationsopt();  
				break;
	 
	     case 754 : if (DEBUG) { System.out.println("Modifiersopt ::="); }  //$NON-NLS-1$
			    consumeDefaultModifiers();  
				break;
	 
	    case 755 : if (DEBUG) { System.out.println("Modifiersopt ::= Modifiers"); }  //$NON-NLS-1$
			    consumeModifiers();  
				break;
	 
	    case 756 : if (DEBUG) { System.out.println("BlockStatementsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyBlockStatementsopt();  
				break;
	 
	     case 758 : if (DEBUG) { System.out.println("Dimsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyDimsopt();  
				break;
	 
	     case 760 : if (DEBUG) { System.out.println("ArgumentListopt ::="); }  //$NON-NLS-1$
			    consumeEmptyArgumentListopt();  
				break;
	 
	    case 764 : if (DEBUG) { System.out.println("FormalParameterListopt ::="); }  //$NON-NLS-1$
			    consumeFormalParameterListopt();  
				break;
	 
	     case 768 : if (DEBUG) { System.out.println("InterfaceMemberDeclarationsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyInterfaceMemberDeclarationsopt();  
				break;
	 
	     case 769 : if (DEBUG) { System.out.println("InterfaceMemberDeclarationsopt ::= NestedType..."); }  //$NON-NLS-1$
			    consumeInterfaceMemberDeclarationsopt();  
				break;
	 
	    case 770 : if (DEBUG) { System.out.println("NestedType ::="); }  //$NON-NLS-1$
			    consumeNestedType();  
				break;

	     case 771 : if (DEBUG) { System.out.println("ForInitopt ::="); }  //$NON-NLS-1$
			    consumeEmptyForInitopt();  
				break;
	 
	     case 773 : if (DEBUG) { System.out.println("ForUpdateopt ::="); }  //$NON-NLS-1$
			    consumeEmptyForUpdateopt();  
				break;
	 
	     case 777 : if (DEBUG) { System.out.println("Catchesopt ::="); }  //$NON-NLS-1$
			    consumeEmptyCatchesopt();  
				break;
	 
	     case 779 : if (DEBUG) { System.out.println("EnumDeclaration ::= EnumHeader EnumBody"); }  //$NON-NLS-1$
			    consumeEnumDeclaration();  
				break;
	 
	     case 780 : if (DEBUG) { System.out.println("EnumHeader ::= EnumHeaderName ClassHeaderImplementsopt"); }  //$NON-NLS-1$
			    consumeEnumHeader();  
				break;
	 
	     case 781 : if (DEBUG) { System.out.println("EnumHeaderName ::= Modifiersopt enum JavaIdentifier"); }  //$NON-NLS-1$
			    consumeEnumHeaderName();  
				break;
	 
	     case 782 : if (DEBUG) { System.out.println("EnumHeaderName ::= Modifiersopt enum JavaIdentifier..."); }  //$NON-NLS-1$
			    consumeEnumHeaderNameWithTypeParameters();  
				break;
	 
	     case 783 : if (DEBUG) { System.out.println("EnumBody ::= LBRACE EnumBodyDeclarationsopt RBRACE"); }  //$NON-NLS-1$
			    consumeEnumBodyNoConstants();  
				break;
	 
	     case 784 : if (DEBUG) { System.out.println("EnumBody ::= LBRACE COMMA EnumBodyDeclarationsopt..."); }  //$NON-NLS-1$
			    consumeEnumBodyNoConstants();  
				break;
	 
	     case 785 : if (DEBUG) { System.out.println("EnumBody ::= LBRACE EnumConstants COMMA..."); }  //$NON-NLS-1$
			    consumeEnumBodyWithConstants();  
				break;
	 
	     case 786 : if (DEBUG) { System.out.println("EnumBody ::= LBRACE EnumConstants..."); }  //$NON-NLS-1$
			    consumeEnumBodyWithConstants();  
				break;
	 
	    case 788 : if (DEBUG) { System.out.println("EnumConstants ::= EnumConstants COMMA EnumConstant"); }  //$NON-NLS-1$
			    consumeEnumConstants();  
				break;
	 
	    case 789 : if (DEBUG) { System.out.println("EnumConstantHeaderName ::= Modifiersopt Identifier"); }  //$NON-NLS-1$
			    consumeEnumConstantHeaderName();  
				break;
	 
	    case 790 : if (DEBUG) { System.out.println("EnumConstantHeader ::= EnumConstantHeaderName..."); }  //$NON-NLS-1$
			    consumeEnumConstantHeader();  
				break;
	 
	    case 791 : if (DEBUG) { System.out.println("EnumConstant ::= EnumConstantHeader ForceNoDiet..."); }  //$NON-NLS-1$
			    consumeEnumConstantWithClassBody();  
				break;
	 
	    case 792 : if (DEBUG) { System.out.println("EnumConstant ::= EnumConstantHeader"); }  //$NON-NLS-1$
			    consumeEnumConstantNoClassBody();  
				break;
	 
	    case 793 : if (DEBUG) { System.out.println("Arguments ::= LPAREN ArgumentListopt RPAREN"); }  //$NON-NLS-1$
			    consumeArguments();  
				break;
	 
	    case 794 : if (DEBUG) { System.out.println("Argumentsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyArguments();  
				break;
	 
	    case 796 : if (DEBUG) { System.out.println("EnumDeclarations ::= SEMICOLON ClassBodyDeclarationsopt"); }  //$NON-NLS-1$
			    consumeEnumDeclarations();  
				break;
	 
	    case 797 : if (DEBUG) { System.out.println("EnumBodyDeclarationsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyEnumDeclarations();  
				break;
	 
	    case 799 : if (DEBUG) { System.out.println("EnhancedForStatement ::= EnhancedForStatementHeader..."); }  //$NON-NLS-1$
			    consumeEnhancedForStatement();  
				break;
	 
	    case 800 : if (DEBUG) { System.out.println("EnhancedForStatementNoShortIf ::=..."); }  //$NON-NLS-1$
			    consumeEnhancedForStatement();  
				break;
	 
	    case 801 : if (DEBUG) { System.out.println("EnhancedForStatementHeaderInit ::= for LPAREN Type..."); }  //$NON-NLS-1$
			    consumeEnhancedForStatementHeaderInit(false);  
				break;
	 
	    case 802 : if (DEBUG) { System.out.println("EnhancedForStatementHeaderInit ::= for LPAREN Modifiers"); }  //$NON-NLS-1$
			    consumeEnhancedForStatementHeaderInit(true);  
				break;
	 
	    case 803 : if (DEBUG) { System.out.println("EnhancedForStatementHeader ::=..."); }  //$NON-NLS-1$
			    consumeEnhancedForStatementHeader();  
				break;
	 
	    case 804 : if (DEBUG) { System.out.println("SingleStaticImportDeclaration ::=..."); }  //$NON-NLS-1$
			    consumeImportDeclaration();  
				break;
	 
	    case 805 : if (DEBUG) { System.out.println("SingleStaticImportDeclarationName ::= import static Name"); }  //$NON-NLS-1$
			    consumeSingleStaticImportDeclarationName();  
				break;
	 
	    case 806 : if (DEBUG) { System.out.println("StaticImportOnDemandDeclaration ::=..."); }  //$NON-NLS-1$
			    consumeImportDeclaration();  
				break;
	 
	    case 807 : if (DEBUG) { System.out.println("StaticImportOnDemandDeclarationName ::= import static..."); }  //$NON-NLS-1$
			    consumeStaticImportOnDemandDeclarationName();  
				break;
	 
	    case 808 : if (DEBUG) { System.out.println("TypeArguments ::= LESS TypeArgumentList1"); }  //$NON-NLS-1$
			    consumeTypeArguments();  
				break;
	 
	    case 809 : if (DEBUG) { System.out.println("OnlyTypeArguments ::= LESS TypeArgumentList1"); }  //$NON-NLS-1$
			    consumeOnlyTypeArguments();  
				break;
	 
	    case 811 : if (DEBUG) { System.out.println("TypeArgumentList1 ::= TypeArgumentList COMMA..."); }  //$NON-NLS-1$
			    consumeTypeArgumentList1();  
				break;
	 
	    case 813 : if (DEBUG) { System.out.println("TypeArgumentList ::= TypeArgumentList COMMA TypeArgument"); }  //$NON-NLS-1$
			    consumeTypeArgumentList();  
				break;
	 
	    case 814 : if (DEBUG) { System.out.println("TypeArgument ::= ReferenceType"); }  //$NON-NLS-1$
			    consumeTypeArgument();  
				break;
	 
	    case 818 : if (DEBUG) { System.out.println("ReferenceType1 ::= ReferenceType GREATER"); }  //$NON-NLS-1$
			    consumeReferenceType1();  
				break;
	 
	    case 819 : if (DEBUG) { System.out.println("ReferenceType1 ::= ClassOrInterface LESS..."); }  //$NON-NLS-1$
			    consumeTypeArgumentReferenceType1();  
				break;
	 
	    case 821 : if (DEBUG) { System.out.println("TypeArgumentList2 ::= TypeArgumentList COMMA..."); }  //$NON-NLS-1$
			    consumeTypeArgumentList2();  
				break;
	 
	    case 824 : if (DEBUG) { System.out.println("ReferenceType2 ::= ReferenceType RIGHT_SHIFT"); }  //$NON-NLS-1$
			    consumeReferenceType2();  
				break;
	 
	    case 825 : if (DEBUG) { System.out.println("ReferenceType2 ::= ClassOrInterface LESS..."); }  //$NON-NLS-1$
			    consumeTypeArgumentReferenceType2();  
				break;
	 
	    case 827 : if (DEBUG) { System.out.println("TypeArgumentList3 ::= TypeArgumentList COMMA..."); }  //$NON-NLS-1$
			    consumeTypeArgumentList3();  
				break;
	 
	    case 830 : if (DEBUG) { System.out.println("ReferenceType3 ::= ReferenceType UNSIGNED_RIGHT_SHIFT"); }  //$NON-NLS-1$
			    consumeReferenceType3();  
				break;
	 
	    case 831 : if (DEBUG) { System.out.println("Wildcard ::= TypeAnnotationsopt QUESTION"); }  //$NON-NLS-1$
			    consumeWildcard();  
				break;
	 
	    case 832 : if (DEBUG) { System.out.println("Wildcard ::= TypeAnnotationsopt QUESTION WildcardBounds"); }  //$NON-NLS-1$
			    consumeWildcardWithBounds();  
				break;
	 
	    case 833 : if (DEBUG) { System.out.println("WildcardBounds ::= extends ReferenceType"); }  //$NON-NLS-1$
			    consumeWildcardBoundsExtends();  
				break;
	 
	    case 834 : if (DEBUG) { System.out.println("WildcardBounds ::= super ReferenceType"); }  //$NON-NLS-1$
			    consumeWildcardBoundsSuper();  
				break;
	 
	    case 835 : if (DEBUG) { System.out.println("Wildcard1 ::= TypeAnnotationsopt QUESTION GREATER"); }  //$NON-NLS-1$
			    consumeWildcard1();  
				break;
	 
	    case 836 : if (DEBUG) { System.out.println("Wildcard1 ::= TypeAnnotationsopt QUESTION..."); }  //$NON-NLS-1$
			    consumeWildcard1WithBounds();  
				break;
	 
	    case 837 : if (DEBUG) { System.out.println("WildcardBounds1 ::= extends ReferenceType1"); }  //$NON-NLS-1$
			    consumeWildcardBounds1Extends();  
				break;
	 
	    case 838 : if (DEBUG) { System.out.println("WildcardBounds1 ::= super ReferenceType1"); }  //$NON-NLS-1$
			    consumeWildcardBounds1Super();  
				break;
	 
	    case 839 : if (DEBUG) { System.out.println("Wildcard2 ::= TypeAnnotationsopt QUESTION RIGHT_SHIFT"); }  //$NON-NLS-1$
			    consumeWildcard2();  
				break;
	 
	    case 840 : if (DEBUG) { System.out.println("Wildcard2 ::= TypeAnnotationsopt QUESTION..."); }  //$NON-NLS-1$
			    consumeWildcard2WithBounds();  
				break;
	 
	    case 841 : if (DEBUG) { System.out.println("WildcardBounds2 ::= extends ReferenceType2"); }  //$NON-NLS-1$
			    consumeWildcardBounds2Extends();  
				break;
	 
	    case 842 : if (DEBUG) { System.out.println("WildcardBounds2 ::= super ReferenceType2"); }  //$NON-NLS-1$
			    consumeWildcardBounds2Super();  
				break;
	 
	    case 843 : if (DEBUG) { System.out.println("Wildcard3 ::= TypeAnnotationsopt QUESTION..."); }  //$NON-NLS-1$
			    consumeWildcard3();  
				break;
	 
	    case 844 : if (DEBUG) { System.out.println("Wildcard3 ::= TypeAnnotationsopt QUESTION..."); }  //$NON-NLS-1$
			    consumeWildcard3WithBounds();  
				break;
	 
	    case 845 : if (DEBUG) { System.out.println("WildcardBounds3 ::= extends ReferenceType3"); }  //$NON-NLS-1$
			    consumeWildcardBounds3Extends();  
				break;
	 
	    case 846 : if (DEBUG) { System.out.println("WildcardBounds3 ::= super ReferenceType3"); }  //$NON-NLS-1$
			    consumeWildcardBounds3Super();  
				break;
	 
	    case 847 : if (DEBUG) { System.out.println("TypeParameterHeader ::= TypeAnnotationsopt..."); }  //$NON-NLS-1$
			    consumeTypeParameterHeader();  
				break;
	 
	    case 848 : if (DEBUG) { System.out.println("TypeParameters ::= LESS TypeParameterList1"); }  //$NON-NLS-1$
			    consumeTypeParameters();  
				break;
	 
	    case 850 : if (DEBUG) { System.out.println("TypeParameterList ::= TypeParameterList COMMA..."); }  //$NON-NLS-1$
			    consumeTypeParameterList();  
				break;
	 
	    case 852 : if (DEBUG) { System.out.println("TypeParameter ::= TypeParameterHeader extends..."); }  //$NON-NLS-1$
			    consumeTypeParameterWithExtends();  
				break;
	 
	    case 853 : if (DEBUG) { System.out.println("TypeParameter ::= TypeParameterHeader extends..."); }  //$NON-NLS-1$
			    consumeTypeParameterWithExtendsAndBounds();  
				break;
	 
	    case 855 : if (DEBUG) { System.out.println("AdditionalBoundList ::= AdditionalBoundList..."); }  //$NON-NLS-1$
			    consumeAdditionalBoundList();  
				break;
	 
	    case 856 : if (DEBUG) { System.out.println("AdditionalBound ::= AND ReferenceType"); }  //$NON-NLS-1$
			    consumeAdditionalBound();  
				break;
	 
	    case 858 : if (DEBUG) { System.out.println("TypeParameterList1 ::= TypeParameterList COMMA..."); }  //$NON-NLS-1$
			    consumeTypeParameterList1();  
				break;
	 
	    case 859 : if (DEBUG) { System.out.println("TypeParameter1 ::= TypeParameterHeader GREATER"); }  //$NON-NLS-1$
			    consumeTypeParameter1();  
				break;
	 
	    case 860 : if (DEBUG) { System.out.println("TypeParameter1 ::= TypeParameterHeader extends..."); }  //$NON-NLS-1$
			    consumeTypeParameter1WithExtends();  
				break;
	 
	    case 861 : if (DEBUG) { System.out.println("TypeParameter1 ::= TypeParameterHeader extends..."); }  //$NON-NLS-1$
			    consumeTypeParameter1WithExtendsAndBounds();  
				break;
	 
	    case 863 : if (DEBUG) { System.out.println("AdditionalBoundList1 ::= AdditionalBoundList..."); }  //$NON-NLS-1$
			    consumeAdditionalBoundList1();  
				break;
	 
	    case 864 : if (DEBUG) { System.out.println("AdditionalBound1 ::= AND ReferenceType1"); }  //$NON-NLS-1$
			    consumeAdditionalBound1();  
				break;
	 
	    case 870 : if (DEBUG) { System.out.println("UnaryExpression_NotName ::= PLUS PushPosition..."); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.PLUS);  
				break;
	 
	    case 871 : if (DEBUG) { System.out.println("UnaryExpression_NotName ::= MINUS PushPosition..."); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.MINUS);  
				break;
	 
	    case 874 : if (DEBUG) { System.out.println("UnaryExpressionNotPlusMinus_NotName ::= TWIDDLE..."); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.TWIDDLE);  
				break;
	 
	    case 875 : if (DEBUG) { System.out.println("UnaryExpressionNotPlusMinus_NotName ::= NOT PushPosition"); }  //$NON-NLS-1$
			    consumeUnaryExpression(OperatorIds.NOT);  
				break;
	 
	    case 878 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.MULTIPLY);  
				break;
	 
	    case 879 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::= NameOrAj MULTIPLY"); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.MULTIPLY);  
				break;
	 
	    case 880 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.DIVIDE);  
				break;
	 
	    case 881 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::= NameOrAj DIVIDE..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.DIVIDE);  
				break;
	 
	    case 882 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.REMAINDER);  
				break;
	 
	    case 883 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::= NameOrAj REMAINDER"); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.REMAINDER);  
				break;
	 
	    case 885 : if (DEBUG) { System.out.println("AdditiveExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.PLUS);  
				break;
	 
	    case 886 : if (DEBUG) { System.out.println("AdditiveExpression_NotName ::= NameOrAj PLUS..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.PLUS);  
				break;
	 
	    case 887 : if (DEBUG) { System.out.println("AdditiveExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.MINUS);  
				break;
	 
	    case 888 : if (DEBUG) { System.out.println("AdditiveExpression_NotName ::= NameOrAj MINUS..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.MINUS);  
				break;
	 
	    case 890 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= ShiftExpression_NotName..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.LEFT_SHIFT);  
				break;
	 
	    case 891 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= NameOrAj LEFT_SHIFT..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.LEFT_SHIFT);  
				break;
	 
	    case 892 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= ShiftExpression_NotName..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.RIGHT_SHIFT);  
				break;
	 
	    case 893 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= NameOrAj RIGHT_SHIFT..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.RIGHT_SHIFT);  
				break;
	 
	    case 894 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= ShiftExpression_NotName..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.UNSIGNED_RIGHT_SHIFT);  
				break;
	 
	    case 895 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= NameOrAj..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.UNSIGNED_RIGHT_SHIFT);  
				break;
	 
	    case 897 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= ShiftExpression_NotName"); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.LESS);  
				break;
	 
	    case 898 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= Name LESS..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.LESS);  
				break;
	 
	    case 899 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= ShiftExpression_NotName"); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.GREATER);  
				break;
	 
	    case 900 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= NameOrAj GREATER..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.GREATER);  
				break;
	 
	    case 901 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.LESS_EQUAL);  
				break;
	 
	    case 902 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= NameOrAj LESS_EQUAL..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.LESS_EQUAL);  
				break;
	 
	    case 903 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.GREATER_EQUAL);  
				break;
	 
	    case 904 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= NameOrAj GREATER_EQUAL"); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.GREATER_EQUAL);  
				break;
	 
	    case 906 : if (DEBUG) { System.out.println("InstanceofExpression_NotName ::= NameOrAj instanceof..."); }  //$NON-NLS-1$
			    consumeInstanceOfExpressionWithName();  
				break;
	 
	    case 907 : if (DEBUG) { System.out.println("InstanceofExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeInstanceOfExpression();  
				break;
	 
	    case 909 : if (DEBUG) { System.out.println("EqualityExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeEqualityExpression(OperatorIds.EQUAL_EQUAL);  
				break;
	 
	    case 910 : if (DEBUG) { System.out.println("EqualityExpression_NotName ::= NameOrAj EQUAL_EQUAL..."); }  //$NON-NLS-1$
			    consumeEqualityExpressionWithName(OperatorIds.EQUAL_EQUAL);  
				break;
	 
	    case 911 : if (DEBUG) { System.out.println("EqualityExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeEqualityExpression(OperatorIds.NOT_EQUAL);  
				break;
	 
	    case 912 : if (DEBUG) { System.out.println("EqualityExpression_NotName ::= NameOrAj NOT_EQUAL..."); }  //$NON-NLS-1$
			    consumeEqualityExpressionWithName(OperatorIds.NOT_EQUAL);  
				break;
	 
	    case 914 : if (DEBUG) { System.out.println("AndExpression_NotName ::= AndExpression_NotName AND..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.AND);  
				break;
	 
	    case 915 : if (DEBUG) { System.out.println("AndExpression_NotName ::= NameOrAj AND..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.AND);  
				break;
	 
	    case 917 : if (DEBUG) { System.out.println("ExclusiveOrExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.XOR);  
				break;
	 
	    case 918 : if (DEBUG) { System.out.println("ExclusiveOrExpression_NotName ::= NameOrAj XOR..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.XOR);  
				break;
	 
	    case 920 : if (DEBUG) { System.out.println("InclusiveOrExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.OR);  
				break;
	 
	    case 921 : if (DEBUG) { System.out.println("InclusiveOrExpression_NotName ::= NameOrAj OR..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.OR);  
				break;
	 
	    case 923 : if (DEBUG) { System.out.println("ConditionalAndExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.AND_AND);  
				break;
	 
	    case 924 : if (DEBUG) { System.out.println("ConditionalAndExpression_NotName ::= NameOrAj AND_AND..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.AND_AND);  
				break;
	 
	    case 926 : if (DEBUG) { System.out.println("ConditionalOrExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeBinaryExpression(OperatorIds.OR_OR);  
				break;
	 
	    case 927 : if (DEBUG) { System.out.println("ConditionalOrExpression_NotName ::= NameOrAj OR_OR..."); }  //$NON-NLS-1$
			    consumeBinaryExpressionWithName(OperatorIds.OR_OR);  
				break;
	 
	    case 929 : if (DEBUG) { System.out.println("ConditionalExpression_NotName ::=..."); }  //$NON-NLS-1$
			    consumeConditionalExpression(OperatorIds.QUESTIONCOLON) ;  
				break;
	 
	    case 930 : if (DEBUG) { System.out.println("ConditionalExpression_NotName ::= NameOrAj QUESTION..."); }  //$NON-NLS-1$
			    consumeConditionalExpressionWithName(OperatorIds.QUESTIONCOLON) ;  
				break;
	 
	    case 934 : if (DEBUG) { System.out.println("AnnotationTypeDeclarationHeaderName ::= Modifiers AT..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeDeclarationHeaderName() ;  
				break;
	 
	    case 935 : if (DEBUG) { System.out.println("AnnotationTypeDeclarationHeaderName ::= Modifiers AT..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters() ;  
				break;
	 
	    case 936 : if (DEBUG) { System.out.println("AnnotationTypeDeclarationHeaderName ::= AT..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters() ;  
				break;
	 
	    case 937 : if (DEBUG) { System.out.println("AnnotationTypeDeclarationHeaderName ::= AT..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeDeclarationHeaderName() ;  
				break;
	 
	    case 938 : if (DEBUG) { System.out.println("AnnotationTypeDeclarationHeader ::=..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeDeclarationHeader() ;  
				break;
	 
	    case 939 : if (DEBUG) { System.out.println("AnnotationTypeDeclaration ::=..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeDeclaration() ;  
				break;
	 
	    case 941 : if (DEBUG) { System.out.println("AnnotationTypeMemberDeclarationsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyAnnotationTypeMemberDeclarationsopt() ;  
				break;
	 
	    case 942 : if (DEBUG) { System.out.println("AnnotationTypeMemberDeclarationsopt ::= NestedType..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeMemberDeclarationsopt() ;  
				break;
	 
	    case 944 : if (DEBUG) { System.out.println("AnnotationTypeMemberDeclarations ::=..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeMemberDeclarations() ;  
				break;
	 
	    case 945 : if (DEBUG) { System.out.println("AnnotationMethodHeaderName ::= Modifiersopt..."); }  //$NON-NLS-1$
			    consumeMethodHeaderNameWithTypeParameters(true);  
				break;
	 
	    case 946 : if (DEBUG) { System.out.println("AnnotationMethodHeaderName ::= Modifiersopt Type..."); }  //$NON-NLS-1$
			    consumeMethodHeaderName(true);  
				break;
	 
	    case 947 : if (DEBUG) { System.out.println("AnnotationMethodHeaderDefaultValueopt ::="); }  //$NON-NLS-1$
			    consumeEmptyMethodHeaderDefaultValue() ;  
				break;
	 
	    case 948 : if (DEBUG) { System.out.println("AnnotationMethodHeaderDefaultValueopt ::= DefaultValue"); }  //$NON-NLS-1$
			    consumeMethodHeaderDefaultValue();  
				break;
	 
	    case 949 : if (DEBUG) { System.out.println("AnnotationMethodHeader ::= AnnotationMethodHeaderName..."); }  //$NON-NLS-1$
			    consumeMethodHeader();  
				break;
	 
	    case 950 : if (DEBUG) { System.out.println("AnnotationTypeMemberDeclaration ::=..."); }  //$NON-NLS-1$
			    consumeAnnotationTypeMemberDeclaration() ;  
				break;
	 
	    case 958 : if (DEBUG) { System.out.println("AnnotationName ::= AT UnannotatableNameOrAj"); }  //$NON-NLS-1$
			    consumeAnnotationName() ;  
				break;
	 
	    case 959 : if (DEBUG) { System.out.println("NormalAnnotation ::= AnnotationName LPAREN..."); }  //$NON-NLS-1$
			    consumeNormalAnnotation(false) ;  
				break;
	 
	    case 960 : if (DEBUG) { System.out.println("MemberValuePairsopt ::="); }  //$NON-NLS-1$
			    consumeEmptyMemberValuePairsopt() ;  
				break;
	 
	    case 963 : if (DEBUG) { System.out.println("MemberValuePairs ::= MemberValuePairs COMMA..."); }  //$NON-NLS-1$
			    consumeMemberValuePairs() ;  
				break;
	 
	    case 964 : if (DEBUG) { System.out.println("MemberValuePair ::= SimpleNameOrAj EQUAL..."); }  //$NON-NLS-1$
			    consumeMemberValuePair() ;  
				break;
	 
	    case 965 : if (DEBUG) { System.out.println("EnterMemberValue ::="); }  //$NON-NLS-1$
			    consumeEnterMemberValue() ;  
				break;
	 
	    case 966 : if (DEBUG) { System.out.println("ExitMemberValue ::="); }  //$NON-NLS-1$
			    consumeExitMemberValue() ;  
				break;
	 
	    case 968 : if (DEBUG) { System.out.println("MemberValue ::= NameOrAj"); }  //$NON-NLS-1$
			    consumeMemberValueAsName() ;  
				break;
	 
	    case 971 : if (DEBUG) { System.out.println("MemberValueArrayInitializer ::=..."); }  //$NON-NLS-1$
			    consumeMemberValueArrayInitializer() ;  
				break;
	 
	    case 972 : if (DEBUG) { System.out.println("MemberValueArrayInitializer ::=..."); }  //$NON-NLS-1$
			    consumeMemberValueArrayInitializer() ;  
				break;
	 
	    case 973 : if (DEBUG) { System.out.println("MemberValueArrayInitializer ::=..."); }  //$NON-NLS-1$
			    consumeEmptyMemberValueArrayInitializer() ;  
				break;
	 
	    case 974 : if (DEBUG) { System.out.println("MemberValueArrayInitializer ::=..."); }  //$NON-NLS-1$
			    consumeEmptyMemberValueArrayInitializer() ;  
				break;
	 
	    case 975 : if (DEBUG) { System.out.println("EnterMemberValueArrayInitializer ::="); }  //$NON-NLS-1$
			    consumeEnterMemberValueArrayInitializer() ;  
				break;
	 
	    case 977 : if (DEBUG) { System.out.println("MemberValues ::= MemberValues COMMA MemberValue"); }  //$NON-NLS-1$
			    consumeMemberValues() ;  
				break;
	 
	    case 978 : if (DEBUG) { System.out.println("MarkerAnnotation ::= AnnotationName"); }  //$NON-NLS-1$
			    consumeMarkerAnnotation(false) ;  
				break;
	 
	    case 979 : if (DEBUG) { System.out.println("SingleMemberAnnotationMemberValue ::= MemberValue"); }  //$NON-NLS-1$
			    consumeSingleMemberAnnotationMemberValue() ;  
				break;
	 
	    case 980 : if (DEBUG) { System.out.println("SingleMemberAnnotation ::= AnnotationName LPAREN..."); }  //$NON-NLS-1$
			    consumeSingleMemberAnnotation(false) ;  
				break;
	 
	    case 981 : if (DEBUG) { System.out.println("RecoveryMethodHeaderName ::= Modifiersopt TypeParameters"); }  //$NON-NLS-1$
			    consumeRecoveryMethodHeaderNameWithTypeParameters();  
				break;
	 
	    case 982 : if (DEBUG) { System.out.println("RecoveryMethodHeaderName ::= Modifiersopt Type..."); }  //$NON-NLS-1$
			    consumeRecoveryMethodHeaderName();  
				break;
	 
	    case 983 : if (DEBUG) { System.out.println("RecoveryMethodHeaderName ::= ModifiersWithDefault..."); }  //$NON-NLS-1$
			    consumeRecoveryMethodHeaderNameWithTypeParameters();  
				break;
	 
	    case 984 : if (DEBUG) { System.out.println("RecoveryMethodHeaderName ::= ModifiersWithDefault Type"); }  //$NON-NLS-1$
			    consumeRecoveryMethodHeaderName();  
				break;
	 
	    case 985 : if (DEBUG) { System.out.println("RecoveryMethodHeader ::= RecoveryMethodHeaderName..."); }  //$NON-NLS-1$
			    consumeMethodHeader();  
				break;
	 
	    case 986 : if (DEBUG) { System.out.println("RecoveryMethodHeader ::= RecoveryMethodHeaderName..."); }  //$NON-NLS-1$
			    consumeMethodHeader();  
				break;
	 
		}
	}


// AspectJ: new method
// TODO - review if this is right, should we make the choice in the java.g file?
protected void consumeQualifiedName() {
	consumeQualifiedName(false);
}



	
	// Helpers
	

	private void consumeIntertypeClassHeader() {
		TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];	
		if (this.currentToken == TokenNameLBRACE) { 
			typeDecl.bodyStart = this.scanner.currentPosition;
		}
		if (this.currentElement != null) {
			this.restartRecovery = true; // used to avoid branching back into the regular automaton		
		}
		// flush the comments related to the class header
		this.scanner.commentPtr = -1;
	}

	private void consumeIntertypeClassDeclaration() {
		int length;
		if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
			//there are length declarations
			//dispatch according to the type of the declarations
			dispatchDeclarationInto(length);
		}

		TypeDeclaration typeDecl = (TypeDeclaration) this.astStack[this.astPtr];


		//convert constructor that do not have the type's name into methods
		boolean hasConstructor = typeDecl.checkConstructors((Parser)this);
		
		//add the default constructor when needed (interface don't have it)
		if (!hasConstructor) {
			switch(TypeDeclaration.kind(typeDecl.modifiers)) {
				case TypeDeclaration.CLASS_DECL :
				case TypeDeclaration.ENUM_DECL :
					boolean insideFieldInitializer = false;
					if (this.diet) {
						for (int i = this.nestedType; i > 0; i--){
							if (this.variablesCounter[i] > 0) {
								insideFieldInitializer = true;
								break;
							}
						}
					}
					typeDecl.createDefaultConstructor(!this.diet || insideFieldInitializer, true);
			}
		}
		//always add <clinit> (will be remove at code gen time if empty)
		if (this.scanner.containsAssertKeyword) {
			typeDecl.bits |= ASTNode.ContainsAssertion;
		}
		typeDecl.addClinit();
		typeDecl.bodyEnd = this.endStatementPosition;
		if (length == 0 && !containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
			typeDecl.bits |= ASTNode.UndocumentedEmptyBlock;
		}

		typeDecl.declarationSourceEnd = flushCommentsDefinedPriorTo(this.endStatementPosition); 
		
	}

	private void consumeIntertypeTypeHeaderNameWithTypeParameters() {
		TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];

		// consume type parameters
		int length = this.genericsLengthStack[this.genericsLengthPtr--];
		this.genericsPtr -= length;
		System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeDecl.typeParameters = new TypeParameter[length], 0, length);

		typeDecl.bodyStart = typeDecl.typeParameters[length-1].declarationSourceEnd + 1;
		
		this.listTypeParameterLength = 0;
		
		if (this.currentElement != null) { // is recovering
			RecoveredType recoveredType = (RecoveredType) this.currentElement;
			recoveredType.pendingTypeParameters = null;
			
			this.lastCheckPoint = typeDecl.bodyStart;
		}		
	}

	private void consumeIntertypeClassHeaderName(boolean b) {	
		TypeDeclaration typeDecl = declarationFactory.createIntertypeMemberClassDeclaration(this.compilationUnit.compilationResult);
		if (this.nestedMethod[this.nestedType] == 0) {
			if (this.nestedType != 0) {
				typeDecl.bits |= ASTNode.IsMemberType;
			}
		} else {
			// Record that the block has a declaration for local types
			typeDecl.bits |= ASTNode.IsLocalType;
			markEnclosingMemberWithLocalType();
			blockReal();
		}
	
		this.display();
		//highlight the name of the type
		long pos = this.identifierPositionStack[this.identifierPtr];
		typeDecl.sourceEnd = (int) pos;
		typeDecl.sourceStart = (int) (pos >>> 32);
		typeDecl.name = this.identifierStack[this.identifierPtr--];
		this.identifierLengthPtr--;
		
		//onType
		if (b) {
			pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
			//consumeClassOrInterfaceName();
		} else {
			consumeClassOrInterfaceName();			
		}
		TypeReference onType = getTypeReference(0);

		declarationFactory.setOnType(typeDecl,onType);
	
		//compute the declaration source too
		// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
		// we want to keep the beginning position but get rid of the end position
		// it is only used for the ClassLiteralAccess positions.
		typeDecl.declarationSourceStart = this.intStack[this.intPtr--]; 
		this.intPtr--; // remove the end position of the class token
	
		typeDecl.modifiersSourceStart = this.intStack[this.intPtr--];
		typeDecl.modifiers = this.intStack[this.intPtr--];
		if (typeDecl.modifiersSourceStart >= 0) {
			typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
		}
	
		// Store secondary info
		if ((typeDecl.bits & ASTNode.IsMemberType) == 0 && (typeDecl.bits & ASTNode.IsLocalType) == 0) {
			if (this.compilationUnit != null && !CharOperation.equals(typeDecl.name, this.compilationUnit.getMainTypeName())) {
				typeDecl.bits |= ASTNode.IsSecondaryType;
			}
		}
	
		// consume annotations
		int length;
		if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
			System.arraycopy(
				this.expressionStack, 
				(this.expressionPtr -= length) + 1, 
				typeDecl.annotations = new Annotation[length], 
				0, 
				length); 
		}
		typeDecl.bodyStart = typeDecl.sourceEnd + 1;
		pushOnAstStack(typeDecl);
	
		this.listLength = 0; // will be updated when reading super-interfaces
		// recovery
		if (this.currentElement != null){ 
			this.lastCheckPoint = typeDecl.bodyStart;
			this.currentElement = this.currentElement.add(typeDecl, 0);
			this.lastIgnoredToken = -1;
		}
		// javadoc
		typeDecl.javadoc = this.javadoc;
		this.javadoc = null;
		this.display();

	}

	protected ASTNode popPointcutDesignator(String terminator) {
		ASTNode tokens = popPseudoTokens(terminator);
		return declarationFactory.createPointcutDesignator(this, tokens);
	}

	protected ASTNode popPseudoTokens(String terminator) {
		consumePseudoToken(terminator);
		consumePseudoTokens();
		//System.out.println("next token is: " + new String(scanner.getCurrentTokenSource()));

		int length = astLengthStack[astLengthPtr--];
		astPtr -= length;

		//arguments
		ASTNode[] tokens = new ASTNode[length];
		System.arraycopy(astStack, astPtr + 1, tokens, 0, length);
		//md.bodyStart = rParenPos+1;
		listLength = 0; // reset listLength after having read all parameters

		return declarationFactory.createPseudoTokensFrom(tokens,this.compilationUnit.compilationResult()); 
			//	new PseudoTokens(tokens, makeSourceContext(this.compilationUnit.compilationResult()));
	}

//	private ISourceContext makeSourceContext(CompilationResult compilationResult) {
//		return new EclipseSourceContext(compilationResult);
//	}


	private void swapAstStack() {
		ASTNode top = astStack[astPtr];
		ASTNode next = astStack[astPtr-1];
		astStack[astPtr] = next;
		astStack[astPtr-1] = top;
	}

	
	
	/**
	 * Recovery rule for when someone tries to use * or + in an ITD
	 */
	private void consumeInterTypeFieldHeaderIllegallyAttemptingToUseATypePattern(String badToken) {
		consumeInterTypeFieldHeader(false); // make the best of what we did get
		MethodDeclaration errorNode = (MethodDeclaration) astStack[astPtr];
		problemReporter().parseErrorDeleteToken(errorNode.sourceStart -2,  // '+.'
				                                errorNode.sourceStart,  
				                                TokenNameIdentifier,
				                                badToken.toCharArray(),
				                                badToken);	 	
	}
	
	/**
	 * Recovery rule for when someone tries to use * or + in an ITD
	 */
	private void consumeInterTypeConstructorHeaderNameIllegallyUsingTypePattern(String badToken) {
		consumeInterTypeConstructorHeaderName(false,false); // make the best of what we did get
		MethodDeclaration errorNode = (MethodDeclaration) astStack[astPtr];
		problemReporter().parseErrorDeleteToken(errorNode.sourceStart -2,  // '+.'
				                                errorNode.sourceStart,  
				                                TokenNameIdentifier,
				                                badToken.toCharArray(),
				                                badToken);	 		
	}
	
	/**
	 * Recovery rule for when someone tries to use * or + in an ITD
	 */
	private void consumeInterTypeMethodHeaderNameIllegallyUsingTypePattern(String badToken) {
		consumeInterTypeMethodHeaderName(false,false); // make the best of what we did get
		MethodDeclaration errorNode = (MethodDeclaration) astStack[astPtr];
		problemReporter().parseErrorDeleteToken(errorNode.sourceStart -2,  // '+.'
				                                errorNode.sourceStart,  
				                                TokenNameIdentifier,
				                                badToken.toCharArray(),
				                                badToken);	 		
	}
	
	/**
	 * Recovery rule for when around advice is specified without a return type
	 */
	private void consumeAroundHeaderNameMissingReturnType() {
		problemReporter().parseErrorInsertToComplete(scanner.startPosition, scanner.currentPosition, "return type","around advice declaration");
		this.restartRecovery = true;
	}
	
	/**
	 * Recovery rule for a screwed up declaration 
	 */
	private void consumeBadHeader() {
		// we read... modifiersopt QualifiedName LPAREN FormalParameterListopt RPAREN		
		problemReporter().parseErrorReplaceTokens(scanner.startPosition, scanner.currentPosition, "valid member declaration");
		this.restartRecovery = true;
	}

protected void consumeSimpleAssertStatement() {
	super.consumeSimpleAssertStatement();
}

/**
 * this method is called by the parser when processing inter-type declarations. We have
 * just finished parsing the type parameters following the OnType of the ITD. Unfortunatey
 * we parsed them as TypeParameter(s) whereas we're going to create a type reference for the
 * on type (and type references can't have type parameters, only type declarations can). Therefore
 * we replace the TypeParameter(s) with SingleTypeReference(s) so that everything will go 
 * smoothly down the line.
 */
private void convertTypeParametersToSingleTypeReferences() {
	for(int typeParameterIndex = 0; typeParameterIndex < genericsLengthStack[genericsLengthPtr]; typeParameterIndex++) {
		TypeParameter tp = (TypeParameter) genericsStack[genericsPtr - typeParameterIndex];
		SingleTypeReference str = new SingleTypeReference(tp.name,tp.declarationSourceStart);
		genericsStack[genericsPtr - typeParameterIndex] = str;
	}
}

// AspectJ: added so the super ctor in TheOriginalJDTParser is visible
public Parser() {}

public Parser(
	ProblemReporter problemReporter,
	boolean optimizeStringLiterals) {
	super(problemReporter, optimizeStringLiterals);
}

// don't try to recover if we're parsing AspectJ constructs
protected boolean shouldTryToRecover() {
	int index = 0;
	ASTNode node;
	while (index < astStack.length && (node = astStack[index++]) != null) {
		if (!declarationFactory.shouldTryToRecover(node)) {
			return false;
		}
	}
	return true;
}

protected void pushOnAspectIntStack(int pos) {

	int stackLength = this.aspectIntStack.length;
	if (++this.aspectIntPtr >= stackLength) {
		System.arraycopy(
			this.aspectIntStack, 0,
			this.aspectIntStack = new int[stackLength + StackIncrement], 0,
			stackLength);
	}
	this.aspectIntStack[this.aspectIntPtr] = pos;
}
}
