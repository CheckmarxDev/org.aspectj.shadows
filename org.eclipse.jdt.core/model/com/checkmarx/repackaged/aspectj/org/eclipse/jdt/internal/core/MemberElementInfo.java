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
package com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.core;

/**
 *Element info for IMember elements.
 */
/* package */ abstract class MemberElementInfo extends SourceRefElementInfo {
	/**
	 * The modifiers associated with this member.
	 *
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants
	 */
	protected int flags;

	/**
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.ISourceType#getNameSourceEnd()
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.ISourceMethod#getNameSourceEnd()
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.ISourceField#getNameSourceEnd()
	 */
	public int getNameSourceEnd() {
		return -1;
	}
	/**
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.ISourceType#getNameSourceStart()
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.ISourceMethod#getNameSourceStart()
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.ISourceField#getNameSourceStart()
	 */
	public int getNameSourceStart() {
		return -1;
	}
	
	/**
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.IGenericType#getModifiers()
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.IGenericMethod#getModifiers()
	 * @see com.checkmarx.repackaged.aspectj.org.eclipse.jdt.internal.compiler.env.IGenericField#getModifiers()
	 */
	public int getModifiers() {
		return this.flags;
	}
	protected void setFlags(int flags) {
		this.flags = flags;
	}
}
