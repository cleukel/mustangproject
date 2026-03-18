package org.mustangproject.Exceptions;

/***
 * will be thrown if an invoice cant be reproduced numerically
 * ArithmetricException for backwards compatibility, was a spelling error
 */
public class ArithmeticException extends ArithmetricException {
	public ArithmeticException() {
		super();
	}

	public ArithmeticException(String details) {
		super(details);
	}
}