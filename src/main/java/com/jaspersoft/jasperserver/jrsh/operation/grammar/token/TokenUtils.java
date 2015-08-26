package com.jaspersoft.jasperserver.jrsh.operation.grammar.token;

import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.CannotCreateTokenException;

/**
 * Static convenience method for instantiating tokens.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0.7
 */
public abstract class TokenUtils {

    /**
     * Convenience method to instantiate also initialize a class using
     * its arg constructor.
     *
     * @param tokenType  a token type
     * @param tokenName  a token name
     * @param tokenValue a token value
     * @param mandatory  define if token mandatory
     * @param tail       define id token a tail
     * @return token instance
     * @throws CannotCreateTokenException
     */
    public static Token instantiateToken(Class<? extends Token> tokenType,
                                         String tokenName, String tokenValue,
                                         boolean mandatory, boolean tail) {
        AbstractToken token = null;
        try {
            token = (AbstractToken) tokenType.newInstance();
            token.setName(tokenName)
                    .setValue(tokenValue)
                    .setMandatory(mandatory)
                    .setTailOfRule(tail);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CannotCreateTokenException(tokenType);
        }
        return token;
    }
}
