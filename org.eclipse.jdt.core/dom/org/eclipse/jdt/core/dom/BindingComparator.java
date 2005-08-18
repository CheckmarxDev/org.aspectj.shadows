/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.HashSet;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

/**
 * Internal helper class for comparing bindings.
 * 
 * @since 3.1
 */
class BindingComparator {
	/**
	 * @param bindings
	 * @param otherBindings
	 * @return true if both parameters are equals, false otherwise
	 */
	static boolean isEqual(TypeVariableBinding[] bindings, TypeVariableBinding[] otherBindings) {
		if (bindings == null) {
			return otherBindings == null;
		} else if (otherBindings == null) {
			return false;
		} else {
			int length = bindings.length;
			int otherLength = otherBindings.length;
			if (length != otherLength) {
				return false;
			}
			for (int i = 0; i < length; i++) {
				TypeVariableBinding typeVariableBinding = bindings[i];
				TypeVariableBinding typeVariableBinding2 = otherBindings[i];
				if (!isEqual(typeVariableBinding, typeVariableBinding2)) {
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * @param declaringElement
	 * @param declaringElement2
	 * @return true if both parameters are equals, false otherwise
	 */
	static boolean isEqual(Binding declaringElement, Binding declaringElement2, HashSet visitedTypes) {
		if (declaringElement instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding) {
			if (!(declaringElement2 instanceof org.eclipse.jdt.internal.compiler.lookup.TypeBinding)){
				return false;
			}
			return isEqual((org.eclipse.jdt.internal.compiler.lookup.TypeBinding) declaringElement,
					(org.eclipse.jdt.internal.compiler.lookup.TypeBinding) declaringElement2,
					visitedTypes);
		} else if (declaringElement instanceof org.eclipse.jdt.internal.compiler.lookup.MethodBinding) {
			if (!(declaringElement2 instanceof org.eclipse.jdt.internal.compiler.lookup.MethodBinding)) {
				return false;
			}
			return isEqual((org.eclipse.jdt.internal.compiler.lookup.MethodBinding) declaringElement,
					(org.eclipse.jdt.internal.compiler.lookup.MethodBinding) declaringElement2,
					visitedTypes);
		} else if (declaringElement instanceof VariableBinding) {
			if (!(declaringElement2 instanceof VariableBinding)) {
				return false;
			}
			return isEqual((VariableBinding) declaringElement,
					(VariableBinding) declaringElement2);
		} else if (declaringElement instanceof org.eclipse.jdt.internal.compiler.lookup.PackageBinding) {
			if (!(declaringElement2 instanceof org.eclipse.jdt.internal.compiler.lookup.PackageBinding)) {
				return false;
			}
			org.eclipse.jdt.internal.compiler.lookup.PackageBinding packageBinding = (org.eclipse.jdt.internal.compiler.lookup.PackageBinding) declaringElement;
			org.eclipse.jdt.internal.compiler.lookup.PackageBinding packageBinding2 = (org.eclipse.jdt.internal.compiler.lookup.PackageBinding) declaringElement2;
			return CharOperation.equals(packageBinding.compoundName, packageBinding2.compoundName);
		} else if (declaringElement instanceof ImportBinding) {
			if (!(declaringElement2 instanceof ImportBinding)) {
				return false;
			}
			ImportBinding importBinding = (ImportBinding) declaringElement;
			ImportBinding importBinding2 = (ImportBinding) declaringElement2;
			return importBinding.isStatic() == importBinding2.isStatic()
				&& importBinding.onDemand == importBinding2.onDemand
				&& CharOperation.equals(importBinding.compoundName, importBinding2.compoundName);
		}
		return false;
	}
	
	static boolean isEqual(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding,
			org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding2) {
		return isEqual(methodBinding, methodBinding2, new HashSet());
	}
			
	static boolean isEqual(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding,
			org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding2,
			HashSet visitedTypes) {
		return (methodBinding == null && methodBinding2 == null)
			|| (CharOperation.equals(methodBinding.selector, methodBinding2.selector)
				&& isEqual(methodBinding.returnType, methodBinding2.returnType, visitedTypes) 
				&& isEqual(methodBinding.thrownExceptions, methodBinding2.thrownExceptions, visitedTypes)
				&& isEqual(methodBinding.declaringClass, methodBinding2.declaringClass, visitedTypes)
				&& isEqual(methodBinding.typeVariables, methodBinding2.typeVariables, visitedTypes)
				&& isEqual(methodBinding.parameters, methodBinding2.parameters, visitedTypes));
	}

	static boolean isEqual(VariableBinding variableBinding, VariableBinding variableBinding2) {
		return (variableBinding.modifiers & CompilerModifiers.AccJustFlag) == (variableBinding2.modifiers & CompilerModifiers.AccJustFlag)
				&& CharOperation.equals(variableBinding.name, variableBinding2.name)
				&& isEqual(variableBinding.type, variableBinding2.type)
				&& (variableBinding.id == variableBinding2.id);
	}

	static boolean isEqual(FieldBinding fieldBinding, FieldBinding fieldBinding2) {
		HashSet visitedTypes = new HashSet();
		return (fieldBinding.modifiers & CompilerModifiers.AccJustFlag) == (fieldBinding2.modifiers & CompilerModifiers.AccJustFlag)
				&& CharOperation.equals(fieldBinding.name, fieldBinding2.name)
				&& isEqual(fieldBinding.type, fieldBinding2.type, visitedTypes)
				&& isEqual(fieldBinding.declaringClass, fieldBinding2.declaringClass, visitedTypes);
	}

	/**
	 * @param bindings
	 * @param otherBindings
	 * @return true if both parameters are equals, false otherwise
	 */
	static boolean isEqual(org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] bindings, org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] otherBindings) {
		return isEqual(bindings, otherBindings, new HashSet());
	}
	/**
	 * @param bindings
	 * @param otherBindings
	 * @return true if both parameters are equals, false otherwise
	 */
	static boolean isEqual(org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] bindings, org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] otherBindings, HashSet visitedTypes) {
		if (bindings == null) {
			return otherBindings == null;
		} else if (otherBindings == null) {
			return false;
		} else {
			int length = bindings.length;
			int otherLength = otherBindings.length;
			if (length != otherLength) {
				return false;
			}
			for (int i = 0; i < length; i++) {
				if (!isEqual(bindings[i], otherBindings[i], visitedTypes)) {
					return false;
				}
			}
			return true;
		}
	}
	static boolean isEqual(org.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding2, HashSet visitedTypes) {
		if (typeBinding == typeBinding2)
			return true;
		if (typeBinding == null || typeBinding2 == null)
			return false;

		switch (typeBinding.kind()) {
			case Binding.BASE_TYPE :
				if (!typeBinding2.isBaseType()) {
					return false;
				}
				return typeBinding.id == typeBinding2.id;
				
			case Binding.ARRAY_TYPE :
				if (!typeBinding2.isArrayType()) {
					return false;
				}
				return typeBinding.dimensions() == typeBinding2.dimensions()
						&& isEqual(typeBinding.leafComponentType(), typeBinding2.leafComponentType(), visitedTypes);
				
			case Binding.PARAMETERIZED_TYPE :
				if (!typeBinding2.isParameterizedType()) {
					return false;
				}
				ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) typeBinding;
				ParameterizedTypeBinding parameterizedTypeBinding2 = (ParameterizedTypeBinding) typeBinding2;
				return CharOperation.equals(parameterizedTypeBinding.compoundName, parameterizedTypeBinding2.compoundName)
					&& (parameterizedTypeBinding.modifiers & (CompilerModifiers.AccJustFlag | IConstants.AccInterface | IConstants.AccEnum | IConstants.AccAnnotation))
							== (parameterizedTypeBinding2.modifiers & (CompilerModifiers.AccJustFlag | IConstants.AccInterface | IConstants.AccEnum | IConstants.AccAnnotation))
					&& isEqual(parameterizedTypeBinding.arguments, parameterizedTypeBinding2.arguments, visitedTypes);
							
			case Binding.WILDCARD_TYPE :
				if (!typeBinding2.isWildcard()) {
					return false;
				}
				WildcardBinding wildcardBinding = (WildcardBinding) typeBinding;
				WildcardBinding wildcardBinding2 = (WildcardBinding) typeBinding2;
				return isEqual(wildcardBinding.bound, wildcardBinding2.bound, visitedTypes)
					&& wildcardBinding.boundKind == wildcardBinding2.boundKind;
				
			case Binding.TYPE_PARAMETER :
				if (visitedTypes.contains(typeBinding)) return true;
				visitedTypes.add(typeBinding);
				
				if (!(typeBinding2.isTypeVariable())) {
					return false;
				}
				if (typeBinding.isCapture()) {
					if (!(typeBinding2.isCapture())) {
						return false;
					}
					CaptureBinding captureBinding = (CaptureBinding) typeBinding;
					CaptureBinding captureBinding2 = (CaptureBinding) typeBinding2;
					return captureBinding.position == captureBinding2.position
						&& isEqual(captureBinding.wildcard, captureBinding2.wildcard, visitedTypes)
						&& isEqual(captureBinding.sourceType, captureBinding2.sourceType, visitedTypes);
				}
				TypeVariableBinding typeVariableBinding = (TypeVariableBinding) typeBinding;
				TypeVariableBinding typeVariableBinding2 = (TypeVariableBinding) typeBinding2;
				return CharOperation.equals(typeVariableBinding.sourceName, typeVariableBinding2.sourceName)
					&& isEqual(typeVariableBinding.declaringElement, typeVariableBinding2.declaringElement, visitedTypes)
					&& isEqual(typeVariableBinding.superclass(), typeVariableBinding2.superclass(), visitedTypes)
					&& isEqual(typeVariableBinding.superInterfaces(), typeVariableBinding2.superInterfaces(), visitedTypes);
			
			case Binding.GENERIC_TYPE :
				if (!typeBinding2.isGenericType()) {
					return false;
				}
				ReferenceBinding referenceBinding = (ReferenceBinding) typeBinding;
				ReferenceBinding referenceBinding2 = (ReferenceBinding) typeBinding2;
				return CharOperation.equals(referenceBinding.compoundName, referenceBinding2.compoundName)
					&& (referenceBinding.modifiers & (CompilerModifiers.AccJustFlag | IConstants.AccInterface | IConstants.AccEnum | IConstants.AccAnnotation))
							== (referenceBinding2.modifiers & (CompilerModifiers.AccJustFlag | IConstants.AccInterface | IConstants.AccEnum | IConstants.AccAnnotation))
					&& isEqual(referenceBinding.typeVariables(), referenceBinding2.typeVariables(), visitedTypes);
		
			case Binding.RAW_TYPE :
			default :
				if (!(typeBinding2 instanceof ReferenceBinding)) {
					return false;
				}				
				referenceBinding = (ReferenceBinding) typeBinding;
				referenceBinding2 = (ReferenceBinding) typeBinding2;
				return CharOperation.equals(referenceBinding.compoundName, referenceBinding2.compoundName)
					&& CharOperation.equals(referenceBinding.constantPoolName(), referenceBinding2.constantPoolName())
					&& (!referenceBinding2.isGenericType())
					&& (referenceBinding.isRawType() == referenceBinding2.isRawType())
					&& (referenceBinding.modifiers & (CompilerModifiers.AccJustFlag | IConstants.AccInterface | IConstants.AccEnum | IConstants.AccAnnotation))
							== (referenceBinding2.modifiers & (CompilerModifiers.AccJustFlag | IConstants.AccInterface | IConstants.AccEnum | IConstants.AccAnnotation));
				
		}
	}
	/**
	 * @param typeBinding
	 * @param typeBinding2
	 * @return true if both parameters are equals, false otherwise
	 */
	static boolean isEqual(org.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding2) {
		return isEqual(typeBinding, typeBinding2, new HashSet());
	}
}
