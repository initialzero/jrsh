package com.jaspersoft.jasperserver.jrsh;

import com.jaspersoft.jasperserver.jrsh.completion.completer.RepositoryNameCompleterTest;
import com.jaspersoft.jasperserver.jrsh.evaluation.strategy.impl.ShellEvaluationStrategyTest;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.lexer.PathConsideringLexerTest;
import com.jaspersoft.jasperserver.jrsh.operation.impl.ImportOperationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ShellEvaluationStrategyTest.class,
        PathConsideringLexerTest.class,
        ImportOperationTest.class,
        RepositoryNameCompleterTest.class
})
public class UnitTestSuite {
}
