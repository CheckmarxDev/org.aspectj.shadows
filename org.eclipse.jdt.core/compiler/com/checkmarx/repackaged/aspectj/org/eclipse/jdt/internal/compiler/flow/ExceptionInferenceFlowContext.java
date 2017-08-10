/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.flow;

import com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 * try statements, exception handlers, etc...
 */

public class ExceptionInferenceFlowContext extends FieldInitsFakingFlowContext {
	public ExceptionInferenceFlowContext(
			FlowContext parent,
			ASTNode associatedNode,
			ReferenceBinding[] handledExceptions,
			FlowContext initializationParent,
			BlockScope scope,
			UnconditionalFlowInfo flowInfo) {
	super(parent, associatedNode, handledExceptions, initializationParent, scope, flowInfo);
}
}