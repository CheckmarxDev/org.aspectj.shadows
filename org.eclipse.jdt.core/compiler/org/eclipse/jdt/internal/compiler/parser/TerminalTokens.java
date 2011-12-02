/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/**
 * IMPORTANT NOTE: These constants are dedicated to the internal Scanner implementation.
 * It is mirrored in org.eclipse.jdt.core.compiler public package where it is API.
 * The mirror implementation is using the backward compatible ITerminalSymbols constant
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens
 * which constant values reflect the latest parser generation state.
 */
/**
 * Maps each terminal symbol in the java-grammar into a unique integer.
 * This integer is used to represent the terminal when computing a parsing action.
 *
 * Disclaimer : These constant values are generated automatically using a Java
 * grammar, therefore their actual values are subject to change if new keywords
 * were added to the language (for instance, 'assert' is a keyword in 1.4).
 */
public interface TerminalTokens {

	// special tokens not part of grammar - not autogenerated
	int TokenNameWHITESPACE = 1000,
		TokenNameCOMMENT_LINE = 1001,
		TokenNameCOMMENT_BLOCK = 1002,
		TokenNameCOMMENT_JAVADOC = 1003;

	 public final static int
     TokenNameIdentifier = 21,
     TokenNameabstract = 54,
     TokenNameassert = 81,
     TokenNameboolean = 38,
     TokenNamebreak = 82,
     TokenNamebyte = 39,
     TokenNamecase = 95,
     TokenNamecatch = 93,
     TokenNamechar = 40,
     TokenNameclass = 79,
     TokenNamecontinue = 83,
     TokenNameconst = 115,
     TokenNamedefault = 114,
     TokenNamedo = 84,
     TokenNamedouble = 41,
     TokenNameelse = 108,
     TokenNameenum = 111,
     TokenNameextends = 92,
     TokenNamefalse = 63,
     TokenNamefinal = 55,
     TokenNamefinally = 96,
     TokenNamefloat = 42,
     TokenNamefor = 85,
     TokenNamegoto = 116,
     TokenNameif = 86,
     TokenNameimplements = 112,
     TokenNameimport = 94,
     TokenNameinstanceof = 14,
     TokenNameint = 43,
     TokenNameinterface = 109,
     TokenNamelong = 44,
     TokenNamenative = 56,
     TokenNamenew = 49,
     TokenNamenull = 64,
     TokenNamepackage = 91,
     TokenNameprivate = 57,
     TokenNameprotected = 58,
     TokenNamepublic = 59,
     TokenNamereturn = 87,
     TokenNameshort = 45,
     TokenNamestatic = 52,
     TokenNamestrictfp = 60,
     TokenNamesuper = 50,
     TokenNameswitch = 88,
     TokenNamesynchronized = 53,
     TokenNamethis = 51,
     TokenNamethrow = 89,
     TokenNamethrows = 110,
     TokenNametransient = 61,
     TokenNametrue = 65,
     TokenNametry = 90,
     TokenNamevoid = 46,
     TokenNamevolatile = 62,
     TokenNamewhile = 80,
     TokenNameaspect = 30,
     TokenNamepointcut = 32,
     TokenNamearound = 33,
     TokenNamebefore = 34,
     TokenNameafter = 35,
     TokenNamedeclare = 36,
     TokenNameprivileged = 31,
     TokenNameIntegerLiteral = 66,
     TokenNameLongLiteral = 67,
     TokenNameFloatingPointLiteral = 68,
     TokenNameDoubleLiteral = 69,
     TokenNameCharacterLiteral = 70,
     TokenNameStringLiteral = 71,
     TokenNamePLUS_PLUS = 9,
     TokenNameMINUS_MINUS = 10,
     TokenNameEQUAL_EQUAL = 20,
     TokenNameLESS_EQUAL = 15,
     TokenNameGREATER_EQUAL = 16,
     TokenNameNOT_EQUAL = 17,
     TokenNameLEFT_SHIFT = 18,
     TokenNameRIGHT_SHIFT = 11,
     TokenNameUNSIGNED_RIGHT_SHIFT = 13,
     TokenNamePLUS_EQUAL = 97,
     TokenNameMINUS_EQUAL = 98,
     TokenNameMULTIPLY_EQUAL = 99,
     TokenNameDIVIDE_EQUAL = 100,
     TokenNameAND_EQUAL = 101,
     TokenNameOR_EQUAL = 102,
     TokenNameXOR_EQUAL = 103,
     TokenNameREMAINDER_EQUAL = 104,
     TokenNameLEFT_SHIFT_EQUAL = 105,
     TokenNameRIGHT_SHIFT_EQUAL = 106,
     TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 107,
     TokenNameOR_OR = 26,
     TokenNameAND_AND = 25,
     TokenNamePLUS = 2,
     TokenNameMINUS = 3,
     TokenNameNOT = 73,
     TokenNameREMAINDER = 6,
     TokenNameXOR = 23,
     TokenNameAND = 19,
     TokenNameMULTIPLY = 5,
     TokenNameOR = 27,
     TokenNameTWIDDLE = 76,
     TokenNameDIVIDE = 7,
     TokenNameGREATER = 12,
     TokenNameLESS = 4,
     TokenNameLPAREN = 22,
     TokenNameRPAREN = 28,
     TokenNameLBRACE = 74,
     TokenNameRBRACE = 47,
     TokenNameLBRACKET = 8,
     TokenNameRBRACKET = 77,
     TokenNameSEMICOLON = 29,
     TokenNameQUESTION = 24,
     TokenNameCOLON = 72,
     TokenNameCOMMA = 37,
     TokenNameDOT = 1,
     TokenNameEQUAL = 78,
     TokenNameAT = 48,
     TokenNameELLIPSIS = 113,
     TokenNameEOF = 75,
     TokenNameERROR = 117;
}
