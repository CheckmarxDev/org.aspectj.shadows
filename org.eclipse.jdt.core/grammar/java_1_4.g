-- Modified for AspectJ-1.1 grammar

--main options
%options ACTION, AN=JavaAction.java, GP=java, 
%options FILE-PREFIX=java, ESCAPE=$, PREFIX=TokenName, OUTPUT-SIZE=125 ,
%options NOGOTO-DEFAULT, SINGLE-PRODUCTIONS, LALR=1 , TABLE=TIME , 

--error recovering options.....
%options ERROR_MAPS 

--grammar understanding options
%options first follow
%options TRACE=FULL ,
%options VERBOSE

--Usefull macros helping reading/writing semantic actions
$Define 
$putCase 
/.    case $rule_number : // System.out.println("$rule_text");  
		   ./

$break
/. 
			break ;
./

-- here it starts really ------------------------------------------
$Terminals

	Identifier

	abstract assert boolean break byte case catch char class 
	continue default do double else extends false final finally float
	for if implements import instanceof int
	interface long native new null package private
	protected public return short static strictfp super switch
	synchronized this throw throws transient true try void
	volatile while

	aspect pointcut around before after declare privileged

	IntegerLiteral
	LongLiteral
	FloatingPointLiteral
	DoubleLiteral
	CharacterLiteral
	StringLiteral

	PLUS_PLUS
	MINUS_MINUS
	EQUAL_EQUAL
	LESS_EQUAL
	GREATER_EQUAL
	NOT_EQUAL
	LEFT_SHIFT
	RIGHT_SHIFT
	UNSIGNED_RIGHT_SHIFT
	PLUS_EQUAL
	MINUS_EQUAL
	MULTIPLY_EQUAL
	DIVIDE_EQUAL
	AND_EQUAL
	OR_EQUAL
	XOR_EQUAL
	REMAINDER_EQUAL
	LEFT_SHIFT_EQUAL
	RIGHT_SHIFT_EQUAL
	UNSIGNED_RIGHT_SHIFT_EQUAL
	OR_OR
	AND_AND
	PLUS
	MINUS
	NOT
	REMAINDER
	XOR
	AND
	MULTIPLY
	OR
	TWIDDLE
	DIVIDE
	GREATER
	LESS
	LPAREN
	RPAREN
	LBRACE
	RBRACE
	LBRACKET
	RBRACKET
	SEMICOLON
	QUESTION
	COLON
	COMMA
	DOT
	EQUAL

--    BodyMarker

$Alias

	'++'   ::= PLUS_PLUS
	'--'   ::= MINUS_MINUS
	'=='   ::= EQUAL_EQUAL
	'<='   ::= LESS_EQUAL
	'>='   ::= GREATER_EQUAL
	'!='   ::= NOT_EQUAL
	'<<'   ::= LEFT_SHIFT
	'>>'   ::= RIGHT_SHIFT
	'>>>'  ::= UNSIGNED_RIGHT_SHIFT
	'+='   ::= PLUS_EQUAL
	'-='   ::= MINUS_EQUAL
	'*='   ::= MULTIPLY_EQUAL
	'/='   ::= DIVIDE_EQUAL
	'&='   ::= AND_EQUAL
	'|='   ::= OR_EQUAL
	'^='   ::= XOR_EQUAL
	'%='   ::= REMAINDER_EQUAL
	'<<='  ::= LEFT_SHIFT_EQUAL
	'>>='  ::= RIGHT_SHIFT_EQUAL
	'>>>=' ::= UNSIGNED_RIGHT_SHIFT_EQUAL
	'||'   ::= OR_OR
	'&&'   ::= AND_AND

	'+'    ::= PLUS
	'-'    ::= MINUS
	'!'    ::= NOT
	'%'    ::= REMAINDER
	'^'    ::= XOR
	'&'    ::= AND
	'*'    ::= MULTIPLY
	'|'    ::= OR
	'~'    ::= TWIDDLE
	'/'    ::= DIVIDE
	'>'    ::= GREATER
	'<'    ::= LESS
	'('    ::= LPAREN
	')'    ::= RPAREN
	'{'    ::= LBRACE
	'}'    ::= RBRACE
	'['    ::= LBRACKET
	']'    ::= RBRACKET
	';'    ::= SEMICOLON
	'?'    ::= QUESTION
	':'    ::= COLON
	','    ::= COMMA
	'.'    ::= DOT
	'='    ::= EQUAL
	
$Start
	Goal

$Rules

/. // This method is part of an automatic generation : do NOT edit-modify  
protected void consumeRule(int act) {
  switch ( act ) {
./



Goal ::= '++' CompilationUnit
Goal ::= '--' MethodBody
Goal ::= '==' ConstructorBody
-- Initializer
Goal ::= '>>' StaticInitializer
Goal ::= '>>' Initializer
-- error recovery
Goal ::= '>>>' Headers
Goal ::= '*' BlockStatements
Goal ::= '*' MethodPushModifiersHeader
Goal ::= '*' CatchHeader
-- JDOM
Goal ::= '&&' FieldDeclaration
Goal ::= '||' ImportDeclaration
Goal ::= '?' PackageDeclaration
Goal ::= '+' TypeDeclaration
Goal ::= '/' GenericMethodDeclaration
Goal ::= '&' ClassBodyDeclaration
-- code snippet
Goal ::= '%' Expression
-- completion parser
Goal ::= '!' ConstructorBlockStatementsopt
Goal ::= '~' BlockStatementsopt

Literal -> IntegerLiteral
Literal -> LongLiteral
Literal -> FloatingPointLiteral
Literal -> DoubleLiteral
Literal -> CharacterLiteral
Literal -> StringLiteral
Literal -> null
Literal -> BooleanLiteral
BooleanLiteral -> true
BooleanLiteral -> false


JavaIdentifier -> 'Identifier'
JavaIdentifier -> AjSimpleName

JavaIdentifierNoAround -> 'Identifier'
JavaIdentifierNoAround -> AjSimpleNameNoAround


-------------------------------------------------------------
-------------------------------------------------------------
--a Type results in both a push of its dimension(s) and its name(s).

Type ::= PrimitiveType
 /.$putCase consumePrimitiveType(); $break ./
Type -> ReferenceType

PrimitiveType -> NumericType
NumericType -> IntegralType
NumericType -> FloatingPointType

PrimitiveType -> 'boolean'
PrimitiveType -> 'void'
IntegralType -> 'byte'
IntegralType -> 'short'
IntegralType -> 'int'
IntegralType -> 'long'
IntegralType -> 'char'
FloatingPointType -> 'float'
FloatingPointType -> 'double'

ReferenceType ::= ClassOrInterfaceType
/.$putCase consumeReferenceType();  $break ./
ReferenceType -> ArrayType -- here a push of dimensions is done, that explains the two previous push 0

ClassOrInterfaceType -> Name

--
-- These rules have been rewritten to avoid some conflicts introduced
-- by adding the 1.1 features
--
-- ArrayType ::= PrimitiveType '[' ']'
-- ArrayType ::= Name '[' ']'
-- ArrayType ::= ArrayType '[' ']'
--

ArrayType ::= PrimitiveType Dims
ArrayType ::= Name Dims

ClassType -> ClassOrInterfaceType


--------------------------------------------------------------
--------------------------------------------------------------


NameOrAj -> AjName
NameOrAj -> Name


AjName -> AjSimpleName
AjName -> AjQualifiedName

AjSimpleName -> AjSimpleNameNoAround
AjSimpleNameNoAround -> 'aspect'
AjSimpleNameNoAround -> 'privileged'
AjSimpleNameNoAround -> 'pointcut'
AjSimpleName -> 'around'
AjSimpleNameNoAround -> 'before'
AjSimpleNameNoAround -> 'after'
AjSimpleNameNoAround -> 'declare'

AjQualifiedName ::= AjName '.' SimpleName
/.$putCase consumeQualifiedName(); $break ./


Name -> SimpleName
Name -> QualifiedName

SimpleName -> 'Identifier' --JavaIdentifier

QualifiedName ::= Name '.' JavaIdentifier --SimpleName 
/.$putCase consumeQualifiedName(); $break ./

CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt ImportDeclarationsopt TypeDeclarationsopt
/.$putCase consumeCompilationUnit(); $break ./

EnterCompilationUnit ::= $empty
/.$putCase consumeEnterCompilationUnit(); $break ./

Headers -> Header
Headers ::= Headers Header

Header -> ImportDeclaration
Header -> PackageDeclaration
Header -> ClassHeader
Header -> InterfaceHeader
Header -> StaticInitializer
Header -> MethodHeader
Header -> ConstructorHeader
Header -> FieldDeclaration
Header -> AllocationHeader

CatchHeader ::= 'catch' '(' FormalParameter ')' '{'
/.$putCase consumeCatchHeader(); $break ./

ImportDeclarations -> ImportDeclaration
ImportDeclarations ::= ImportDeclarations ImportDeclaration 
/.$putCase consumeImportDeclarations(); $break ./

TypeDeclarations -> TypeDeclaration
TypeDeclarations ::= TypeDeclarations TypeDeclaration
/.$putCase consumeTypeDeclarations(); $break ./

PackageDeclaration ::= PackageDeclarationName ';'
/.$putCase  consumePackageDeclaration(); $break ./

PackageDeclarationName ::= 'package' Name
/.$putCase  consumePackageDeclarationName(); $break ./

ImportDeclaration -> SingleTypeImportDeclaration
ImportDeclaration -> TypeImportOnDemandDeclaration

SingleTypeImportDeclaration ::= SingleTypeImportDeclarationName ';'
/.$putCase consumeSingleTypeImportDeclaration(); $break ./
			  
SingleTypeImportDeclarationName ::= 'import' Name
/.$putCase consumeSingleTypeImportDeclarationName(); $break ./
			  
TypeImportOnDemandDeclaration ::= TypeImportOnDemandDeclarationName ';'
/.$putCase consumeTypeImportOnDemandDeclaration(); $break ./

TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
/.$putCase consumeTypeImportOnDemandDeclarationName(); $break ./

TypeDeclaration -> ClassDeclaration
TypeDeclaration -> InterfaceDeclaration
-- this declaration in part of a list od declaration and we will
-- use and optimized list length calculation process 
-- thus we decrement the number while it will be incremend.....
TypeDeclaration ::= ';' 
/. $putCase consumeEmptyTypeDeclaration(); $break ./

--18.7 Only in the LALR(1) Grammar

Modifiers ::= Modifier
Modifiers ::= Modifiers Modifier

Modifier -> 'public' 
Modifier -> 'protected'
Modifier -> 'private'
Modifier -> 'static'
Modifier -> 'abstract'
Modifier -> 'final'
Modifier -> 'native'
Modifier -> 'synchronized'
Modifier -> 'transient'
Modifier -> 'volatile'
Modifier -> 'strictfp'


--New AspectJ Productions

-- two declarations are visible outside of aspects
Header -> PointcutDeclaration
Header -> BasicAdviceDeclaration
Header -> AroundDeclaration
Header -> DeclareDeclaration
Header -> InterTypeMethodDeclaration
Header -> InterTypeFieldDeclaration


TypeDeclaration -> AspectDeclaration
Header -> AspectDeclaration

ClassMemberDeclaration -> AspectDeclaration
InterfaceMemberDeclaration -> AspectDeclaration


ClassMemberDeclaration -> PointcutDeclaration
InterfaceMemberDeclaration -> PointcutDeclaration

-- everthing else is only visible inside an aspect
AspectDeclaration ::= AspectHeader AspectBody
/.$putCase consumeAspectDeclaration(); $break ./

AspectHeader ::= AspectHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt AspectHeaderRest
/.$putCase consumeAspectHeader(); $break ./

AspectHeaderName ::= Modifiersopt 'aspect' 'Identifier'
/.$putCase consumeAspectHeaderName(false); $break ./

AspectHeaderName ::= Modifiersopt 'privileged' Modifiersopt 'aspect' 'Identifier'
/.$putCase consumeAspectHeaderName(true); $break ./


AspectHeaderRest ::= $empty

--[dominates TypePattern] [persingleton() | percflow(PCD) | perthis(PCD) | pertarget(PCD)]
AspectHeaderRest ::= AspectHeaderRestStart PseudoTokens
/.$putCase consumeAspectHeaderRest(); $break ./

AspectHeaderRestStart ::= 'Identifier'
/.$putCase consumePseudoTokenIdentifier(); $break ./


AspectBody ::= '{' AspectBodyDeclarationsopt '}'

AspectBodyDeclarations ::= AspectBodyDeclaration
AspectBodyDeclarations ::= AspectBodyDeclarations AspectBodyDeclaration
/.$putCase consumeClassBodyDeclarations(); $break ./


AspectBodyDeclarationsopt ::= $empty
/.$putCase consumeEmptyClassBodyDeclarationsopt(); $break ./

-- ??? why is NestedType here
AspectBodyDeclarationsopt ::= NestedType AspectBodyDeclarations
/.$putCase consumeClassBodyDeclarationsopt(); $break ./


AspectBodyDeclaration ::= ClassBodyDeclaration
/.$putCase consumeClassBodyDeclarationInAspect(); $break ./


-- pointcuts and advice

PointcutDeclaration ::= PointcutHeader MethodHeaderParameters ';'
/.$putCase consumeEmptyPointcutDeclaration(); $break ./

PointcutDeclaration ::= PointcutHeader MethodHeaderParameters ':' PseudoTokens  ';'
/.$putCase consumePointcutDeclaration(); $break ./

PointcutHeader ::= Modifiersopt 'pointcut'  JavaIdentifier '('
/.$putCase consumePointcutHeader(); $break ./



AspectBodyDeclaration -> AroundDeclaration
AspectBodyDeclaration -> BasicAdviceDeclaration

AroundDeclaration ::= AroundHeader MethodBody
/.$putCase consumeAroundDeclaration(); $break ./

AroundHeader ::= AroundHeaderName MethodHeaderParameters MethodHeaderThrowsClauseopt ':' PseudoTokens
/.$putCase consumeAroundHeader(); $break ./

-- no modifiers are actually allowed on around, but the grammar is happier this way
AroundHeaderName ::= Modifiersopt Type  'around' '(' 
/.$putCase consumeAroundHeaderName(); $break ./

BasicAdviceDeclaration ::= BasicAdviceHeader MethodBody
/.$putCase consumeBasicAdviceDeclaration(); $break ./

BasicAdviceHeader ::= BasicAdviceHeaderName MethodHeaderParameters ExtraParamopt MethodHeaderThrowsClauseopt ':' PseudoTokens
/.$putCase consumeBasicAdviceHeader(); $break ./


BasicAdviceHeaderName ::= Modifiersopt 'before' '(' 
/.$putCase consumeBasicAdviceHeaderName(false); $break ./

BasicAdviceHeaderName ::= Modifiersopt 'after' '(' 
/.$putCase consumeBasicAdviceHeaderName(true); $break ./


ExtraParamopt ::= 'Identifier' '(' FormalParameter ')'
/.$putCase consumeExtraParameterWithFormal(); $break ./
ExtraParamopt ::= 'Identifier' '(' ')'
/.$putCase consumeExtraParameterNoFormal(); $break ./
-- deprecated, but we're probably stuck with it now
ExtraParamopt ::= 'Identifier'
/.$putCase consumeExtraParameterNoFormal(); $break ./
ExtraParamopt ::= $empty




-- intertype declarations

OnType ::= JavaIdentifier
OnType ::= OnType '.' JavaIdentifier
/.$putCase consumeQualifiedName(); $break ./

AspectBodyDeclaration -> InterTypeMethodDeclaration
AspectBodyDeclaration -> InterTypeConstructorDeclaration
AspectBodyDeclaration -> InterTypeFieldDeclaration

InterTypeMethodDeclaration -> AbstractInterTypeMethodDeclaration
InterTypeMethodDeclaration ::= InterTypeMethodHeader MethodBody 
/.$putCase // set to true to consume a method with a body
  consumeInterTypeMethodDeclaration(true);  $break ./

InterTypeMethodHeader ::= InterTypeMethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims MethodHeaderThrowsClauseopt
/.$putCase consumeInterTypeMethodHeader(); $break ./

InterTypeMethodHeaderName ::= Modifiersopt Type OnType '.' JavaIdentifier '('
/.$putCase consumeInterTypeMethodHeaderName(); $break ./


AbstractInterTypeMethodDeclaration ::= InterTypeMethodHeader ';'
/.$putCase // set to false to consume a method without body
  consumeInterTypeMethodDeclaration(false); $break ./


InterTypeConstructorDeclaration ::= InterTypeConstructorHeader ConstructorBody 
/.$putCase // set to true to consume a method with a body
  consumeInterTypeConstructorDeclaration();  $break ./

InterTypeConstructorHeader ::= InterTypeConstructorHeaderName MethodHeaderParameters MethodHeaderThrowsClauseopt
/.$putCase consumeInterTypeConstructorHeader(); $break ./

-- using Name instead of OnType to make jikespg happier
InterTypeConstructorHeaderName ::= Modifiersopt Name '.' 'new' '('
/.$putCase consumeInterTypeConstructorHeaderName(); $break ./


InterTypeFieldDeclaration ::= Modifiersopt Type OnType '.' JavaIdentifier InterTypeFieldBody ';'
/.$putCase consumeInterTypeFieldDeclaration(); $break ./

InterTypeFieldBody ::=  $empty
InterTypeFieldBody ::= '=' ForceNoDiet VariableInitializer RestoreDiet




-- declares (more fun than a pcd)
AspectBodyDeclaration -> DeclareDeclaration

DeclareDeclaration ::= DeclareHeader PseudoTokens ';'
/.$putCase consumeDeclareDeclaration(); $break ./

DeclareHeader ::= 'declare' 'Identifier' ':' 
/.$putCase consumeDeclareHeader(); $break ./



-- the joy of pcds
PseudoTokens ::= PseudoToken
PseudoTokens ::= PseudoTokens PseudoToken
/.$putCase consumePseudoTokens(); $break ./

PseudoToken ::= JavaIdentifier
/.$putCase consumePseudoTokenIdentifier(); $break ./

PseudoToken ::= '('
/.$putCase consumePseudoToken("("); $break ./

PseudoToken ::= ')'
/.$putCase consumePseudoToken(")"); $break ./

PseudoToken ::= '.'
/.$putCase consumePseudoToken("."); $break ./

PseudoToken ::= '*'
/.$putCase consumePseudoToken("*"); $break ./

PseudoToken ::= '+'
/.$putCase consumePseudoToken("+"); $break ./

PseudoToken ::= '&&'
/.$putCase consumePseudoToken("&&"); $break ./

PseudoToken ::= '||'
/.$putCase consumePseudoToken("||"); $break ./

PseudoToken ::= '!'
/.$putCase consumePseudoToken("!"); $break ./

PseudoToken ::= ':'
/.$putCase consumePseudoToken(":"); $break ./

PseudoToken ::= ','
/.$putCase consumePseudoToken(","); $break ./

PseudoToken ::= '['
/.$putCase consumePseudoToken("["); $break ./

PseudoToken ::= ']'
/.$putCase consumePseudoToken("]"); $break ./



PseudoToken ::= PrimitiveType
/.$putCase consumePseudoTokenPrimitiveType(); $break ./

PseudoToken ::= Modifier
/.$putCase consumePseudoTokenModifier(); $break ./

PseudoToken ::= Literal
/.$putCase consumePseudoTokenLiteral(); $break ./


PseudoToken ::= 'this'
/.$putCase consumePseudoToken("this", 1, true); $break ./

PseudoToken ::= 'super'
/.$putCase consumePseudoToken("super", 1, true); $break ./


-- special handling for if
PseudoToken ::= 'if' '(' Expression ')'
/.$putCase consumePseudoTokenIf(); $break ./

PseudoToken ::= 'assert'
/.$putCase consumePseudoToken("assert", 1, true); $break ./

PseudoToken ::= 'import'
/.$putCase consumePseudoToken("import", 1, true); $break ./

PseudoToken ::= 'package'
/.$putCase consumePseudoToken("package", 1, true); $break ./

PseudoToken ::= 'throw'
/.$putCase consumePseudoToken("throw", 1, true); $break ./

PseudoToken ::= 'new'
/.$putCase consumePseudoToken("new", 1, true); $break ./

PseudoToken ::= 'do'
/.$putCase consumePseudoToken("do", 1, true); $break ./

PseudoToken ::= 'for'
/.$putCase consumePseudoToken("for", 1, true); $break ./

PseudoToken ::= 'switch'
/.$putCase consumePseudoToken("switch", 1, true); $break ./

PseudoToken ::= 'try'
/.$putCase consumePseudoToken("try", 1, true); $break ./

PseudoToken ::= 'while'
/.$putCase consumePseudoToken("while", 1, true); $break ./

PseudoToken ::= 'break'
/.$putCase consumePseudoToken("break", 1, true); $break ./

PseudoToken ::= 'continue'
/.$putCase consumePseudoToken("continue", 1, true); $break ./

PseudoToken ::= 'return'
/.$putCase consumePseudoToken("return", 1, true); $break ./

PseudoToken ::= 'case'
/.$putCase consumePseudoToken("case", 1, true); $break ./

PseudoToken ::= 'catch'
/.$putCase consumePseudoToken("catch", 0, true); $break ./

PseudoToken ::= 'instanceof'
/.$putCase consumePseudoToken("instanceof", 0, true); $break ./

PseudoToken ::= 'else'
/.$putCase consumePseudoToken("else", 0, true); $break ./

PseudoToken ::= 'extends'
/.$putCase consumePseudoToken("extends", 0, true); $break ./

PseudoToken ::= 'finally'
/.$putCase consumePseudoToken("finally", 0, true); $break ./

PseudoToken ::= 'implements'
/.$putCase consumePseudoToken("implements", 0, true); $break ./

PseudoToken ::= 'throws'
/.$putCase consumePseudoToken("throws", 0, true); $break ./

-- add all other keywords as identifiers



--18.8 Productions from 8: Class Declarations
--ClassModifier ::=
--      'abstract'
--    | 'final'
--    | 'public'
--18.8.1 Productions from 8.1: Class Declarations

ClassDeclaration ::= ClassHeader ClassBody
/.$putCase consumeClassDeclaration(); $break ./

ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt
/.$putCase consumeClassHeader(); $break ./

ClassHeaderName ::= Modifiersopt 'class' JavaIdentifier
/.$putCase consumeClassHeaderName(); $break ./

ClassHeaderExtends ::= 'extends' ClassType
/.$putCase consumeClassHeaderExtends(); $break ./

ClassHeaderImplements ::= 'implements' InterfaceTypeList
/.$putCase consumeClassHeaderImplements(); $break ./

InterfaceTypeList -> InterfaceType
InterfaceTypeList ::= InterfaceTypeList ',' InterfaceType
/.$putCase consumeInterfaceTypeList(); $break ./

InterfaceType ::= ClassOrInterfaceType
/.$putCase consumeInterfaceType(); $break ./

ClassBody ::= '{' ClassBodyDeclarationsopt '}'

ClassBodyDeclarations ::= ClassBodyDeclaration
ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration
/.$putCase consumeClassBodyDeclarations(); $break ./

ClassBodyDeclaration -> ClassMemberDeclaration
ClassBodyDeclaration -> StaticInitializer
ClassBodyDeclaration -> ConstructorDeclaration
--1.1 feature
ClassBodyDeclaration ::= Diet NestedMethod Block
/.$putCase consumeClassBodyDeclaration(); $break ./
Diet ::= $empty
/.$putCase consumeDiet(); $break./

Initializer ::= Diet NestedMethod Block
/.$putCase consumeClassBodyDeclaration(); $break ./

ClassMemberDeclaration -> FieldDeclaration
ClassMemberDeclaration -> MethodDeclaration
--1.1 feature
ClassMemberDeclaration -> ClassDeclaration
--1.1 feature
ClassMemberDeclaration -> InterfaceDeclaration

-- Empty declarations are not valid Java ClassMemberDeclarations.
-- However, since the current (2/14/97) Java compiler accepts them 
-- (in fact, some of the official tests contain this erroneous
-- syntax)

GenericMethodDeclaration -> MethodDeclaration
GenericMethodDeclaration -> ConstructorDeclaration

ClassMemberDeclaration ::= ';'
/.$putCase consumeEmptyClassMemberDeclaration(); $break./

--18.8.2 Productions from 8.3: Field Declarations
--VariableModifier ::=
--      'public'
--    | 'protected'
--    | 'private'
--    | 'static'
--    | 'final'
--    | 'transient'
--    | 'volatile'

FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
/.$putCase consumeFieldDeclaration(); $break ./

VariableDeclarators -> VariableDeclarator 
VariableDeclarators ::= VariableDeclarators ',' VariableDeclarator
/.$putCase consumeVariableDeclarators(); $break ./

VariableDeclarator ::= VariableDeclaratorId EnterVariable ExitVariableWithoutInitialization

VariableDeclarator ::= VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization

EnterVariable ::= $empty
/.$putCase consumeEnterVariable(); $break ./

ExitVariableWithInitialization ::= $empty
/.$putCase consumeExitVariableWithInitialization(); $break ./

ExitVariableWithoutInitialization ::= $empty
/.$putCase consumeExitVariableWithoutInitialization(); $break ./

ForceNoDiet ::= $empty
/.$putCase consumeForceNoDiet(); $break ./
RestoreDiet ::= $empty
/.$putCase consumeRestoreDiet(); $break ./

VariableDeclaratorId ::= JavaIdentifier Dimsopt

VariableInitializer -> Expression
VariableInitializer -> ArrayInitializer

--18.8.3 Productions from 8.4: Method Declarations
--MethodModifier ::=
--      'public'
--    | 'protected'
--    | 'private'
--    | 'static'
--    | 'abstract'
--    | 'final'
--    | 'native'
--    | 'synchronized'
--

MethodDeclaration -> AbstractMethodDeclaration
MethodDeclaration ::= MethodHeader MethodBody 
/.$putCase // set to true to consume a method with a body
  consumeMethodDeclaration(true);  $break ./

AbstractMethodDeclaration ::= MethodHeader ';'
/.$putCase // set to false to consume a method without body
  consumeMethodDeclaration(false); $break ./

MethodHeader ::= MethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims MethodHeaderThrowsClauseopt
/.$putCase consumeMethodHeader(); $break ./

MethodPushModifiersHeader ::= MethodPushModifiersHeaderName MethodHeaderParameters MethodHeaderExtendedDims MethodHeaderThrowsClauseopt
/.$putCase consumeMethodHeader(); $break ./

MethodPushModifiersHeaderName ::= Modifiers Type PushModifiers JavaIdentifierNoAround '(' 
/.$putCase consumeMethodPushModifiersHeaderName(); $break ./

MethodPushModifiersHeaderName ::= Type PushModifiers JavaIdentifierNoAround '(' 
/.$putCase consumeMethodPushModifiersHeaderName(); $break ./

MethodHeaderName ::= Modifiersopt Type JavaIdentifierNoAround '('
/.$putCase consumeMethodHeaderName(); $break ./

MethodHeaderParameters ::= FormalParameterListopt ')'
/.$putCase consumeMethodHeaderParameters(); $break ./

MethodHeaderExtendedDims ::= Dimsopt
/.$putCase consumeMethodHeaderExtendedDims(); $break ./

MethodHeaderThrowsClause ::= 'throws' ClassTypeList
/.$putCase consumeMethodHeaderThrowsClause(); $break ./

ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters MethodHeaderThrowsClauseopt
/.$putCase consumeConstructorHeader(); $break ./

ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('
/.$putCase consumeConstructorHeaderName(); $break ./

ConstructorHeaderName ::=  Modifiersopt 'aspect' '('  -- makes aspect harder
/.$putCase consumeConstructorHeaderName(); $break ./

FormalParameterList -> FormalParameter
FormalParameterList ::= FormalParameterList ',' FormalParameter
/.$putCase consumeFormalParameterList(); $break ./

--1.1 feature
FormalParameter ::= Modifiersopt Type VariableDeclaratorId
/.$putCase // the boolean is used to know if the modifiers should be reset
 	consumeFormalParameter(); $break ./

ClassTypeList -> ClassTypeElt
ClassTypeList ::= ClassTypeList ',' ClassTypeElt
/.$putCase consumeClassTypeList(); $break ./

ClassTypeElt ::= ClassType
/.$putCase consumeClassTypeElt(); $break ./


MethodBody ::= NestedMethod '{' BlockStatementsopt '}' 
/.$putCase consumeMethodBody(); $break ./

NestedMethod ::= $empty
/.$putCase consumeNestedMethod(); $break ./

--18.8.4 Productions from 8.5: Static Initializers

StaticInitializer ::=  StaticOnly Block
/.$putCase consumeStaticInitializer(); $break./

StaticOnly ::= 'static'
/.$putCase consumeStaticOnly(); $break ./

--18.8.5 Productions from 8.6: Constructor Declarations
--ConstructorModifier ::=
--      'public'
--    | 'protected'
--    | 'private'
--
--
ConstructorDeclaration ::= ConstructorHeader ConstructorBody
/.$putCase consumeConstructorDeclaration() ; $break ./ 

-- These rules are added to be able to parse constructors with no body
ConstructorDeclaration ::= ConstructorHeader ';'
/.$putCase consumeInvalidConstructorDeclaration() ; $break ./ 

-- the rules ExplicitConstructorInvocationopt has been expanded
-- in the rule below in order to make the grammar lalr(1).
-- ConstructorBody ::= '{' ExplicitConstructorInvocationopt BlockStatementsopt '}'
-- Other inlining has occured into the next rule too....

ConstructorBody ::= NestedMethod  '{' ConstructorBlockStatementsopt '}'
/.$putCase consumeConstructorBody(); $break ./

ConstructorBlockStatementsopt -> BlockStatementsopt

ConstructorBlockStatementsopt -> ExplicitConstructorInvocation

ConstructorBlockStatementsopt ::= ExplicitConstructorInvocation BlockStatements
/.$putCase  consumeConstructorBlockStatements(); $break ./

ExplicitConstructorInvocation ::= 'this' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.This); $break ./

ExplicitConstructorInvocation ::= 'super' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.Super); $break ./

--1.1 feature
ExplicitConstructorInvocation ::= Primary '.' 'super' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.Super); $break ./

--1.1 feature
ExplicitConstructorInvocation ::= Name '.' 'super' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.Super); $break ./

--1.1 feature
ExplicitConstructorInvocation ::= Primary '.' 'this' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.This); $break ./

--1.1 feature
ExplicitConstructorInvocation ::= Name '.' 'this' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.This); $break ./

--18.9 Productions from 9: Interface Declarations

--18.9.1 Productions from 9.1: Interface Declarations
--InterfaceModifier ::=
--      'public'
--    | 'abstract'
--
InterfaceDeclaration ::= InterfaceHeader InterfaceBody
/.$putCase consumeInterfaceDeclaration(); $break ./

InterfaceHeader ::= InterfaceHeaderName InterfaceHeaderExtendsopt
/.$putCase consumeInterfaceHeader(); $break ./

InterfaceHeaderName ::= Modifiersopt 'interface' JavaIdentifier
/.$putCase consumeInterfaceHeaderName(); $break ./

-- This rule will be used to accept inner local interface and then report a relevant error message
InvalidInterfaceDeclaration -> InterfaceHeader InterfaceBody

InterfaceHeaderExtends ::= 'extends' InterfaceTypeList
/.$putCase consumeInterfaceHeaderExtends(); $break ./

InterfaceBody ::= '{' InterfaceMemberDeclarationsopt '}' 

InterfaceMemberDeclarations -> InterfaceMemberDeclaration
InterfaceMemberDeclarations ::= InterfaceMemberDeclarations InterfaceMemberDeclaration
/.$putCase consumeInterfaceMemberDeclarations(); $break ./

--same as for class members
InterfaceMemberDeclaration ::= ';'
/.$putCase consumeEmptyInterfaceMemberDeclaration(); $break ./

-- This rule is added to be able to parse non abstract method inside interface and then report a relevent error message
InvalidMethodDeclaration -> MethodHeader MethodBody

InterfaceMemberDeclaration -> ConstantDeclaration
InterfaceMemberDeclaration ::= InvalidMethodDeclaration
/.$putCase ignoreMethodBody(); $break ./

-- These rules are added to be able to parse constructors inside interface and then report a relevent error message
InvalidConstructorDeclaration ::= ConstructorHeader ConstructorBody
/.$putCase ignoreInvalidConstructorDeclaration(true);  $break ./

InvalidConstructorDeclaration ::= ConstructorHeader ';'
/.$putCase ignoreInvalidConstructorDeclaration(false);  $break ./

InterfaceMemberDeclaration -> AbstractMethodDeclaration
InterfaceMemberDeclaration -> InvalidConstructorDeclaration
 
--1.1 feature
InterfaceMemberDeclaration -> ClassDeclaration
--1.1 feature
InterfaceMemberDeclaration -> InterfaceDeclaration

ConstantDeclaration -> FieldDeclaration

ArrayInitializer ::= '{' ,opt '}'
/.$putCase consumeEmptyArrayInitializer(); $break ./
ArrayInitializer ::= '{' VariableInitializers '}'
/.$putCase consumeArrayInitializer(); $break ./
ArrayInitializer ::= '{' VariableInitializers , '}'
/.$putCase consumeArrayInitializer(); $break ./

VariableInitializers ::= VariableInitializer
VariableInitializers ::= VariableInitializers ',' VariableInitializer
/.$putCase consumeVariableInitializers(); $break ./

Block ::= OpenBlock '{' BlockStatementsopt '}'
/.$putCase consumeBlock(); $break ./
OpenBlock ::= $empty
/.$putCase consumeOpenBlock() ; $break ./

BlockStatements -> BlockStatement
BlockStatements ::= BlockStatements BlockStatement
/.$putCase consumeBlockStatements() ; $break ./

BlockStatement -> LocalVariableDeclarationStatement
BlockStatement -> Statement
--1.1 feature
BlockStatement -> ClassDeclaration
BlockStatement ::= InvalidInterfaceDeclaration
/.$putCase ignoreInterfaceDeclaration(); $break ./

LocalVariableDeclarationStatement ::= LocalVariableDeclaration ';'
/.$putCase consumeLocalVariableDeclarationStatement(); $break ./

LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators
/.$putCase consumeLocalVariableDeclaration(); $break ./

-- 1.1 feature
-- The modifiers part of this rule makes the grammar more permissive. 
-- The only modifier here is final. We put Modifiers to allow multiple modifiers
-- This will require to check the validity of the modifier

LocalVariableDeclaration ::= Modifiers Type PushModifiers VariableDeclarators
/.$putCase consumeLocalVariableDeclaration(); $break ./

PushModifiers ::= $empty
/.$putCase consumePushModifiers(); $break ./

Statement -> StatementWithoutTrailingSubstatement
Statement -> LabeledStatement
Statement -> IfThenStatement
Statement -> IfThenElseStatement
Statement -> WhileStatement
Statement -> ForStatement

StatementNoShortIf -> StatementWithoutTrailingSubstatement
StatementNoShortIf -> LabeledStatementNoShortIf
StatementNoShortIf -> IfThenElseStatementNoShortIf
StatementNoShortIf -> WhileStatementNoShortIf
StatementNoShortIf -> ForStatementNoShortIf

StatementWithoutTrailingSubstatement -> AssertStatement
StatementWithoutTrailingSubstatement -> Block
StatementWithoutTrailingSubstatement -> EmptyStatement
StatementWithoutTrailingSubstatement -> ExpressionStatement
StatementWithoutTrailingSubstatement -> SwitchStatement
StatementWithoutTrailingSubstatement -> DoStatement
StatementWithoutTrailingSubstatement -> BreakStatement
StatementWithoutTrailingSubstatement -> ContinueStatement
StatementWithoutTrailingSubstatement -> ReturnStatement
StatementWithoutTrailingSubstatement -> SynchronizedStatement
StatementWithoutTrailingSubstatement -> ThrowStatement
StatementWithoutTrailingSubstatement -> TryStatement

EmptyStatement ::= ';'
/.$putCase consumeEmptyStatement(); $break ./

LabeledStatement ::= JavaIdentifier ':' Statement
/.$putCase consumeStatementLabel() ; $break ./

LabeledStatementNoShortIf ::= JavaIdentifier ':' StatementNoShortIf
/.$putCase consumeStatementLabel() ; $break ./

ExpressionStatement ::= StatementExpression ';'
/. $putCase consumeExpressionStatement(); $break ./

StatementExpression ::= Assignment
StatementExpression ::= PreIncrementExpression
StatementExpression ::= PreDecrementExpression
StatementExpression ::= PostIncrementExpression
StatementExpression ::= PostDecrementExpression
StatementExpression ::= MethodInvocation
StatementExpression ::= ClassInstanceCreationExpression

IfThenStatement ::=  'if' '(' Expression ')' Statement
/.$putCase consumeStatementIfNoElse(); $break ./

IfThenElseStatement ::=  'if' '(' Expression ')' StatementNoShortIf 'else' Statement
/.$putCase consumeStatementIfWithElse(); $break ./

IfThenElseStatementNoShortIf ::=  'if' '(' Expression ')' StatementNoShortIf 'else' StatementNoShortIf
/.$putCase consumeStatementIfWithElse(); $break ./

SwitchStatement ::= 'switch' OpenBlock '(' Expression ')' SwitchBlock
/.$putCase consumeStatementSwitch() ; $break ./

SwitchBlock ::= '{' '}'
/.$putCase consumeEmptySwitchBlock() ; $break ./

SwitchBlock ::= '{' SwitchBlockStatements '}'
SwitchBlock ::= '{' SwitchLabels '}'
SwitchBlock ::= '{' SwitchBlockStatements SwitchLabels '}'
/.$putCase consumeSwitchBlock() ; $break ./

SwitchBlockStatements -> SwitchBlockStatement
SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement
/.$putCase consumeSwitchBlockStatements() ; $break ./

SwitchBlockStatement ::= SwitchLabels BlockStatements
/.$putCase consumeSwitchBlockStatement() ; $break ./

SwitchLabels -> SwitchLabel
SwitchLabels ::= SwitchLabels SwitchLabel
/.$putCase consumeSwitchLabels() ; $break ./

SwitchLabel ::= 'case' ConstantExpression ':'
/. $putCase consumeCaseLabel(); $break ./

SwitchLabel ::= 'default' ':'
/. $putCase consumeDefaultLabel(); $break ./

WhileStatement ::= 'while' '(' Expression ')' Statement
/.$putCase consumeStatementWhile() ; $break ./

WhileStatementNoShortIf ::= 'while' '(' Expression ')' StatementNoShortIf
/.$putCase consumeStatementWhile() ; $break ./

DoStatement ::= 'do' Statement 'while' '(' Expression ')' ';'
/.$putCase consumeStatementDo() ; $break ./

ForStatement ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' Statement
/.$putCase consumeStatementFor() ; $break ./
ForStatementNoShortIf ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' StatementNoShortIf
/.$putCase consumeStatementFor() ; $break ./

--the minus one allows to avoid a stack-to-stack transfer
ForInit ::= StatementExpressionList
/.$putCase consumeForInit() ; $break ./
ForInit -> LocalVariableDeclaration

ForUpdate -> StatementExpressionList

StatementExpressionList -> StatementExpression
StatementExpressionList ::= StatementExpressionList ',' StatementExpression
/.$putCase consumeStatementExpressionList() ; $break ./

-- 1.4 feature
AssertStatement ::= 'assert' Expression ';'
/.$putCase consumeSimpleAssertStatement() ; $break ./

AssertStatement ::= 'assert' Expression ':' Expression ';'
/.$putCase consumeAssertStatement() ; $break ./
          
BreakStatement ::= 'break' ';'
/.$putCase consumeStatementBreak() ; $break ./

BreakStatement ::= 'break' Identifier ';'
/.$putCase consumeStatementBreakWithLabel() ; $break ./

ContinueStatement ::= 'continue' ';'
/.$putCase consumeStatementContinue() ; $break ./

ContinueStatement ::= 'continue' Identifier ';'
/.$putCase consumeStatementContinueWithLabel() ; $break ./

ReturnStatement ::= 'return' Expressionopt ';'
/.$putCase consumeStatementReturn() ; $break ./

ThrowStatement ::= 'throw' Expression ';'
/.$putCase consumeStatementThrow();
$break ./

SynchronizedStatement ::= OnlySynchronized '(' Expression ')'    Block
/.$putCase consumeStatementSynchronized(); $break ./
OnlySynchronized ::= 'synchronized'
/.$putCase consumeOnlySynchronized(); $break ./


TryStatement ::= 'try'    Block Catches
/.$putCase consumeStatementTry(false); $break ./
TryStatement ::= 'try'    Block Catchesopt Finally
/.$putCase consumeStatementTry(true); $break ./

Catches -> CatchClause
Catches ::= Catches CatchClause
/.$putCase consumeCatches(); $break ./

CatchClause ::= 'catch' '(' FormalParameter ')'    Block
/.$putCase consumeStatementCatch() ; $break ./

Finally ::= 'finally'    Block

--18.12 Productions from 14: Expressions

--for source positionning purpose
PushLPAREN ::= '('
/.$putCase consumeLeftParen(); $break ./
PushRPAREN ::= ')'
/.$putCase consumeRightParen(); $break ./

Primary -> PrimaryNoNewArray
Primary -> ArrayCreationWithArrayInitializer
Primary -> ArrayCreationWithoutArrayInitializer

PrimaryNoNewArray -> Literal
PrimaryNoNewArray ::= 'this'
/.$putCase consumePrimaryNoNewArrayThis(); $break ./

PrimaryNoNewArray ::=  PushLPAREN Expression PushRPAREN 
/.$putCase consumePrimaryNoNewArray(); $break ./

PrimaryNoNewArray -> ClassInstanceCreationExpression
PrimaryNoNewArray -> FieldAccess
--1.1 feature
PrimaryNoNewArray ::= Name '.' 'this'
/.$putCase consumePrimaryNoNewArrayNameThis(); $break ./
PrimaryNoNewArray ::= Name '.' 'super'
/.$putCase consumePrimaryNoNewArrayNameSuper(); $break ./

--1.1 feature
--PrimaryNoNewArray ::= Type '.' 'class'   
--inline Type in the previous rule in order to make the grammar LL1 instead 
-- of LL2. The result is the 3 next rules.
PrimaryNoNewArray ::= Name '.' 'class'
/.$putCase consumePrimaryNoNewArrayName(); $break ./

PrimaryNoNewArray ::= ArrayType '.' 'class'
/.$putCase consumePrimaryNoNewArrayArrayType(); $break ./

PrimaryNoNewArray ::= PrimitiveType '.' 'class'
/.$putCase consumePrimaryNoNewArrayPrimitiveType(); $break ./

PrimaryNoNewArray -> MethodInvocation
PrimaryNoNewArray -> ArrayAccess

--1.1 feature
--
-- In Java 1.0 a ClassBody could not appear at all in a
-- ClassInstanceCreationExpression.
--

AllocationHeader ::= 'new' ClassType '(' ArgumentListopt ')'
/.$putCase consumeAllocationHeader(); $break ./

ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt
/.$putCase consumeClassInstanceCreationExpression(); $break ./
--1.1 feature

ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
/.$putCase consumeClassInstanceCreationExpressionQualified() ; $break ./

--1.1 feature
ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
/.$putCase consumeClassInstanceCreationExpressionQualified() ; $break ./

ClassInstanceCreationExpressionName ::= Name '.'
/.$putCase consumeClassInstanceCreationExpressionName() ; $break ./

ClassBodyopt ::= $empty --test made using null as contents
/.$putCase consumeClassBodyopt(); $break ./
ClassBodyopt ::= EnterAnonymousClassBody ClassBody

EnterAnonymousClassBody ::= $empty
/.$putCase consumeEnterAnonymousClassBody(); $break ./

ArgumentList ::= Expression
ArgumentList ::= ArgumentList ',' Expression
/.$putCase consumeArgumentList(); $break ./

ArrayCreationWithoutArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs
/.$putCase consumeArrayCreationExpressionWithoutInitializer(); $break ./

ArrayCreationWithArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializer
/.$putCase consumeArrayCreationExpressionWithInitializer(); $break ./

ArrayCreationWithoutArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs
/.$putCase consumeArrayCreationExpressionWithoutInitializer(); $break ./

ArrayCreationWithArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializer
/.$putCase consumeArrayCreationExpressionWithInitializer(); $break ./

DimWithOrWithOutExprs ::= DimWithOrWithOutExpr
DimWithOrWithOutExprs ::= DimWithOrWithOutExprs DimWithOrWithOutExpr
/.$putCase consumeDimWithOrWithOutExprs(); $break ./

DimWithOrWithOutExpr ::= '[' Expression ']'
DimWithOrWithOutExpr ::= '[' ']'
/. $putCase consumeDimWithOrWithOutExpr(); $break ./
-- -----------------------------------------------

Dims ::= DimsLoop
/. $putCase consumeDims(); $break ./
DimsLoop -> OneDimLoop
DimsLoop ::= DimsLoop OneDimLoop
OneDimLoop ::= '[' ']'
/. $putCase consumeOneDimLoop(); $break ./

FieldAccess ::= Primary '.' JavaIdentifier
/.$putCase consumeFieldAccess(false); $break ./

FieldAccess ::= 'super' '.' JavaIdentifier
/.$putCase consumeFieldAccess(true); $break ./

MethodInvocation ::= NameOrAj '(' ArgumentListopt ')'
/.$putCase consumeMethodInvocationName(); $break ./

MethodInvocation ::= Primary '.' JavaIdentifier '(' ArgumentListopt ')'
/.$putCase consumeMethodInvocationPrimary(); $break ./

MethodInvocation ::= 'super' '.' JavaIdentifier '(' ArgumentListopt ')'
/.$putCase consumeMethodInvocationSuper(); $break ./

ArrayAccess ::= Name '[' Expression ']'
/.$putCase consumeArrayAccess(true); $break ./
ArrayAccess ::= PrimaryNoNewArray '[' Expression ']'
/.$putCase consumeArrayAccess(false); $break ./
ArrayAccess ::= ArrayCreationWithArrayInitializer '[' Expression ']'
/.$putCase consumeArrayAccess(false); $break ./

PostfixExpression -> Primary
PostfixExpression ::= NameOrAj
/.$putCase consumePostfixExpression(); $break ./
PostfixExpression -> PostIncrementExpression
PostfixExpression -> PostDecrementExpression

PostIncrementExpression ::= PostfixExpression '++'
/.$putCase consumeUnaryExpression(OperatorExpression.PLUS,true); $break ./

PostDecrementExpression ::= PostfixExpression '--'
/.$putCase consumeUnaryExpression(OperatorExpression.MINUS,true); $break ./

--for source managment purpose
PushPosition ::= $empty
 /.$putCase consumePushPosition(); $break ./

UnaryExpression -> PreIncrementExpression
UnaryExpression -> PreDecrementExpression
UnaryExpression ::= '+' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.PLUS); $break ./
UnaryExpression ::= '-' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.MINUS); $break ./
UnaryExpression -> UnaryExpressionNotPlusMinus

PreIncrementExpression ::= '++' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.PLUS,false); $break ./

PreDecrementExpression ::= '--' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.MINUS,false); $break ./

UnaryExpressionNotPlusMinus -> PostfixExpression
UnaryExpressionNotPlusMinus ::= '~' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.TWIDDLE); $break ./
UnaryExpressionNotPlusMinus ::= '!' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.NOT); $break ./
UnaryExpressionNotPlusMinus -> CastExpression

CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN InsideCastExpression UnaryExpression
/.$putCase consumeCastExpression(); $break ./
 CastExpression ::= PushLPAREN Name Dims PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
/.$putCase consumeCastExpression(); $break ./
-- Expression is here only in order to make the grammar LL1
CastExpression ::= PushLPAREN Expression PushRPAREN InsideCastExpressionLL1 UnaryExpressionNotPlusMinus
/.$putCase consumeCastExpressionLL1(); $break ./

InsideCastExpression ::= $empty
/.$putCase consumeInsideCastExpression(); $break ./
InsideCastExpressionLL1 ::= $empty
/.$putCase consumeInsideCastExpressionLL1(); $break ./

MultiplicativeExpression -> UnaryExpression
MultiplicativeExpression ::= MultiplicativeExpression '*' UnaryExpression
/.$putCase consumeBinaryExpression(OperatorExpression.MULTIPLY); $break ./
MultiplicativeExpression ::= MultiplicativeExpression '/' UnaryExpression
/.$putCase consumeBinaryExpression(OperatorExpression.DIVIDE); $break ./
MultiplicativeExpression ::= MultiplicativeExpression '%' UnaryExpression
/.$putCase consumeBinaryExpression(OperatorExpression.REMAINDER); $break ./

AdditiveExpression -> MultiplicativeExpression
AdditiveExpression ::= AdditiveExpression '+' MultiplicativeExpression
/.$putCase consumeBinaryExpression(OperatorExpression.PLUS); $break ./
AdditiveExpression ::= AdditiveExpression '-' MultiplicativeExpression
/.$putCase consumeBinaryExpression(OperatorExpression.MINUS); $break ./

ShiftExpression -> AdditiveExpression
ShiftExpression ::= ShiftExpression '<<'  AdditiveExpression
/.$putCase consumeBinaryExpression(OperatorExpression.LEFT_SHIFT); $break ./
ShiftExpression ::= ShiftExpression '>>'  AdditiveExpression
/.$putCase consumeBinaryExpression(OperatorExpression.RIGHT_SHIFT); $break ./
ShiftExpression ::= ShiftExpression '>>>' AdditiveExpression
/.$putCase consumeBinaryExpression(OperatorExpression.UNSIGNED_RIGHT_SHIFT); $break ./

RelationalExpression -> ShiftExpression
RelationalExpression ::= RelationalExpression '<'  ShiftExpression
/.$putCase consumeBinaryExpression(OperatorExpression.LESS); $break ./
RelationalExpression ::= RelationalExpression '>'  ShiftExpression
/.$putCase consumeBinaryExpression(OperatorExpression.GREATER); $break ./
RelationalExpression ::= RelationalExpression '<=' ShiftExpression
/.$putCase consumeBinaryExpression(OperatorExpression.LESS_EQUAL); $break ./
RelationalExpression ::= RelationalExpression '>=' ShiftExpression
/.$putCase consumeBinaryExpression(OperatorExpression.GREATER_EQUAL); $break ./
RelationalExpression ::= RelationalExpression 'instanceof' ReferenceType
/.$putCase consumeInstanceOfExpression(OperatorExpression.INSTANCEOF); $break ./

EqualityExpression -> RelationalExpression
EqualityExpression ::= EqualityExpression '==' RelationalExpression
/.$putCase consumeEqualityExpression(OperatorExpression.EQUAL_EQUAL); $break ./
EqualityExpression ::= EqualityExpression '!=' RelationalExpression
/.$putCase consumeEqualityExpression(OperatorExpression.NOT_EQUAL); $break ./

AndExpression -> EqualityExpression
AndExpression ::= AndExpression '&' EqualityExpression
/.$putCase consumeBinaryExpression(OperatorExpression.AND); $break ./

ExclusiveOrExpression -> AndExpression
ExclusiveOrExpression ::= ExclusiveOrExpression '^' AndExpression
/.$putCase consumeBinaryExpression(OperatorExpression.XOR); $break ./

InclusiveOrExpression -> ExclusiveOrExpression
InclusiveOrExpression ::= InclusiveOrExpression '|' ExclusiveOrExpression
/.$putCase consumeBinaryExpression(OperatorExpression.OR); $break ./

ConditionalAndExpression -> InclusiveOrExpression
ConditionalAndExpression ::= ConditionalAndExpression '&&' InclusiveOrExpression
/.$putCase consumeBinaryExpression(OperatorExpression.AND_AND); $break ./

ConditionalOrExpression -> ConditionalAndExpression
ConditionalOrExpression ::= ConditionalOrExpression '||' ConditionalAndExpression
/.$putCase consumeBinaryExpression(OperatorExpression.OR_OR); $break ./

ConditionalExpression -> ConditionalOrExpression
ConditionalExpression ::= ConditionalOrExpression '?' Expression ':' ConditionalExpression
/.$putCase consumeConditionalExpression(OperatorExpression.QUESTIONCOLON) ; $break ./

AssignmentExpression -> ConditionalExpression
AssignmentExpression -> Assignment

Assignment ::= PostfixExpression AssignmentOperator AssignmentExpression
/.$putCase consumeAssignment(); $break ./

-- this rule is added to parse an array initializer in a assigment and then report a syntax error knowing the exact senario
InvalidArrayInitializerAssignement ::= PostfixExpression AssignmentOperator ArrayInitializer
Assignment ::= InvalidArrayInitializerAssignement
/.$putcase ignoreExpressionAssignment();$break ./

AssignmentOperator ::= '='
/.$putCase consumeAssignmentOperator(EQUAL); $break ./
AssignmentOperator ::= '*='
/.$putCase consumeAssignmentOperator(MULTIPLY); $break ./
AssignmentOperator ::= '/='
/.$putCase consumeAssignmentOperator(DIVIDE); $break ./
AssignmentOperator ::= '%='
/.$putCase consumeAssignmentOperator(REMAINDER); $break ./
AssignmentOperator ::= '+='
/.$putCase consumeAssignmentOperator(PLUS); $break ./
AssignmentOperator ::= '-='
/.$putCase consumeAssignmentOperator(MINUS); $break ./
AssignmentOperator ::= '<<='
/.$putCase consumeAssignmentOperator(LEFT_SHIFT); $break ./
AssignmentOperator ::= '>>='
/.$putCase consumeAssignmentOperator(RIGHT_SHIFT); $break ./
AssignmentOperator ::= '>>>='
/.$putCase consumeAssignmentOperator(UNSIGNED_RIGHT_SHIFT); $break ./
AssignmentOperator ::= '&='
/.$putCase consumeAssignmentOperator(AND); $break ./
AssignmentOperator ::= '^='
/.$putCase consumeAssignmentOperator(XOR); $break ./
AssignmentOperator ::= '|='
/.$putCase consumeAssignmentOperator(OR); $break ./

Expression -> AssignmentExpression

ConstantExpression -> Expression

-- The following rules are for optional nonterminals.
--

PackageDeclarationopt -> $empty 
PackageDeclarationopt -> PackageDeclaration

ClassHeaderExtendsopt ::= $empty
ClassHeaderExtendsopt -> ClassHeaderExtends

Expressionopt ::= $empty
/.$putCase consumeEmptyExpression(); $break ./
Expressionopt -> Expression


---------------------------------------------------------------------------------------
--
-- The rules below are for optional terminal symbols.  An optional comma,
-- is only used in the context of an array initializer - It is a
-- "syntactic sugar" that otherwise serves no other purpose. By contrast,
-- an optional identifier is used in the definition of a break and 
-- continue statement. When the identifier does not appear, a NULL
-- is produced. When the identifier is present, the user should use the
-- corresponding TOKEN(i) method. See break statement as an example.
--
---------------------------------------------------------------------------------------

,opt -> $empty
,opt -> ,

ImportDeclarationsopt ::= $empty
/.$putCase consumeEmptyImportDeclarationsopt(); $break ./
ImportDeclarationsopt ::= ImportDeclarations
/.$putCase consumeImportDeclarationsopt(); $break ./


TypeDeclarationsopt ::= $empty
/.$putCase consumeEmptyTypeDeclarationsopt(); $break ./
TypeDeclarationsopt ::= TypeDeclarations
/.$putCase consumeTypeDeclarationsopt(); $break ./

ClassBodyDeclarationsopt ::= $empty
/.$putCase consumeEmptyClassBodyDeclarationsopt(); $break ./
ClassBodyDeclarationsopt ::= NestedType ClassBodyDeclarations
/.$putCase consumeClassBodyDeclarationsopt(); $break ./

Modifiersopt ::= $empty 
/. $putCase consumeDefaultModifiers(); $break ./
Modifiersopt ::= Modifiers 
/.$putCase consumeModifiers(); $break ./ 

BlockStatementsopt ::= $empty
/.$putCase consumeEmptyBlockStatementsopt(); $break ./
BlockStatementsopt -> BlockStatements

Dimsopt ::= $empty
/. $putCase consumeEmptyDimsopt(); $break ./
Dimsopt -> Dims

ArgumentListopt ::= $empty
/. $putCase consumeEmptyArgumentListopt(); $break ./
ArgumentListopt -> ArgumentList

MethodHeaderThrowsClauseopt ::= $empty
MethodHeaderThrowsClauseopt -> MethodHeaderThrowsClause
   
FormalParameterListopt ::= $empty
/.$putcase consumeFormalParameterListopt(); $break ./
FormalParameterListopt -> FormalParameterList

ClassHeaderImplementsopt ::= $empty
ClassHeaderImplementsopt -> ClassHeaderImplements

InterfaceMemberDeclarationsopt ::= $empty
/. $putCase consumeEmptyInterfaceMemberDeclarationsopt(); $break ./
InterfaceMemberDeclarationsopt ::= NestedType InterfaceMemberDeclarations
/. $putCase consumeInterfaceMemberDeclarationsopt(); $break ./

NestedType ::= $empty 
/.$putCase consumeNestedType(); $break./

ForInitopt ::= $empty
/. $putCase consumeEmptyForInitopt(); $break ./
ForInitopt -> ForInit

ForUpdateopt ::= $empty
/. $putCase consumeEmptyForUpdateopt(); $break ./
 ForUpdateopt -> ForUpdate

InterfaceHeaderExtendsopt ::= $empty
InterfaceHeaderExtendsopt -> InterfaceHeaderExtends

Catchesopt ::= $empty
/. $putCase consumeEmptyCatchesopt(); $break ./
Catchesopt -> Catches

/.	}
} ./

---------------------------------------------------------------------------------------

$names

-- BodyMarker ::= '"class Identifier { ... MethodHeader "'

-- void ::= 'void'

PLUS_PLUS ::=    '++'   
MINUS_MINUS ::=    '--'   
EQUAL_EQUAL ::=    '=='   
LESS_EQUAL ::=    '<='   
GREATER_EQUAL ::=    '>='   
NOT_EQUAL ::=    '!='   
LEFT_SHIFT ::=    '<<'   
RIGHT_SHIFT ::=    '>>'   
UNSIGNED_RIGHT_SHIFT ::=    '>>>'  
PLUS_EQUAL ::=    '+='   
MINUS_EQUAL ::=    '-='   
MULTIPLY_EQUAL ::=    '*='   
DIVIDE_EQUAL ::=    '/='   
AND_EQUAL ::=    '&='   
OR_EQUAL ::=    '|='   
XOR_EQUAL ::=    '^='   
REMAINDER_EQUAL ::=    '%='   
LEFT_SHIFT_EQUAL ::=    '<<='  
RIGHT_SHIFT_EQUAL ::=    '>>='  
UNSIGNED_RIGHT_SHIFT_EQUAL ::=    '>>>=' 
OR_OR ::=    '||'   
AND_AND ::=    '&&'   

PLUS ::=    '+'    
MINUS ::=    '-'    
NOT ::=    '!'    
REMAINDER ::=    '%'    
XOR ::=    '^'    
AND ::=    '&'    
MULTIPLY ::=    '*'    
OR ::=    '|'    
TWIDDLE ::=    '~'    
DIVIDE ::=    '/'    
GREATER ::=    '>'    
LESS ::=    '<'    
LPAREN ::=    '('    
RPAREN ::=    ')'    
LBRACE ::=    '{'    
RBRACE ::=    '}'    
LBRACKET ::=    '['    
RBRACKET ::=    ']'    
SEMICOLON ::=    ';'    
QUESTION ::=    '?'    
COLON ::=    ':'    
COMMA ::=    ','    
DOT ::=    '.'    
EQUAL ::=    '='    

$end
-- need a carriage return after the $end
