package com.jaspersoft.jasperserver.jrsh.core;

import com.jaspersoft.jasperserver.jrsh.core.completion.impl.RepositoryCompleterTest;
import com.jaspersoft.jasperserver.jrsh.core.evaluation.strategy.impl.ShellEvaluationStrategyTest;
import com.jaspersoft.jasperserver.jrsh.core.operation.grammar.lexer.PathIdentifyingLexerTest;
import com.jaspersoft.jasperserver.jrsh.core.operation.impl.ImportOperationTest;
import com.jaspersoft.jasperserver.jrsh.core.operation.impl.LoginOperationTest;
import com.jaspersoft.jasperserver.jrsh.core.operation.parser.ConditionsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ConditionsTest.class,
        LoginOperationTest.class,
        ShellEvaluationStrategyTest.class,
        ImportOperationTest.class,
        PathIdentifyingLexerTest.class,
        RepositoryCompleterTest.class
})
public class UnitTestSuite {
}
