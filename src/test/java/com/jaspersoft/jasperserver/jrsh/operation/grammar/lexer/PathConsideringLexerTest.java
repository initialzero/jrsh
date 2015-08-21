package com.jaspersoft.jasperserver.jrsh.operation.grammar.lexer;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link PathConsideringLexer} class.
 *
 * @author Alexander Krasnyanskiy
 */
public class PathConsideringLexerTest {

    public static final String OPERATION_OF_THREE_TOKENS = "import ~/Downloads/folder/New\\ Folder/file.zip with-include-audit-events ";
    public static final String OPERATION_OF_TWO_TOKENS =   "import ~/Downloads/folder/New\\ Folder/My\\ Another\\ Cool\\ Folder/file.zip";

    private Lexer lexer = new PathConsideringLexer();

    @Test
    public void shouldRecognizePathWithOneSpaceAsSingleToken() {
        // When
        int tokensAmount = lexer.convert(OPERATION_OF_THREE_TOKENS).size();
        // Then
        Assert.assertSame(tokensAmount, 3);
    }

    @Test
    public void shouldRecognizePathWithFourSpacesAsSingleToken() {
        // When
        int tokensAmount = lexer.convert(OPERATION_OF_TWO_TOKENS).size();
        // Then
        Assert.assertSame(tokensAmount, 2);
    }

}