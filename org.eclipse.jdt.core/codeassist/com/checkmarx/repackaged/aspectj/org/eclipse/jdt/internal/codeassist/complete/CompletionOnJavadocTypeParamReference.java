/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.codeassist.complete;

import com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.ast.JavadocSingleTypeReference;
import com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnJavadocTypeParamReference extends JavadocSingleTypeReference implements CompletionOnJavadoc {
	public int completionFlags = JAVADOC;
	public char[][] missingParams;

	public CompletionOnJavadocTypeParamReference(char[] name, long pos, int start, int end) {
		super(name, pos, start, end);
	}

	public CompletionOnJavadocTypeParamReference(JavadocSingleTypeReference typeRef) {
		super(typeRef.token, (((long)typeRef.sourceStart)<<32)+typeRef.sourceEnd, typeRef.tagSourceStart, typeRef.tagSourceStart);
	}

	/**
	 * @param flags The completionFlags to set.
	 */
	public void addCompletionFlags(int flags) {
		this.completionFlags |= flags;
	}

	/**
	 * Get completion node flags.
	 *
	 * @return int Flags of the javadoc completion node.
	 */
	public int getCompletionFlags() {
		return this.completionFlags;
	}

	/* (non-Javadoc)
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.ast.AllocationExpression#printExpression(int, java.lang.StringBuffer)
	 */
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("<CompletionOnJavadocTypeParamReference:"); //$NON-NLS-1$
		if (this.token != null) super.printExpression(indent, output);
		return output.append('>');
	}

	/* (non-Javadoc)
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleNameReference#reportError(com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public TypeBinding reportError(BlockScope scope) {
		return null;
	}
}
