package com.jaspersoft.jasperserver.jrsh.evaluation.strategy.impl;

import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jrsh.common.SessionFactory;
import com.jaspersoft.jasperserver.jrsh.operation.impl.ExportOperation;
import com.jaspersoft.jasperserver.jrsh.operation.impl.LoginOperation;
import com.jaspersoft.jasperserver.jrsh.operation.parser.OperationParser;
import com.jaspersoft.jasperserver.jrsh.operation.result.OperationResult;
import com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexander Krasnyanskiy
 */
public class ShellEvaluationStrategyTest {
    @Mock
    private OperationParser operationParserMock;
    @Mock
    private LoginOperation loginOperationMock;
    @Mock
    private ExportOperation exportOperationMock;
    @Mock
    private OperationResult loginOperationResultMock;
    @Mock
    private OperationResult failedLoginOperationResultMock;
    @Mock
    private OperationResult exportOperationResultMock;
    @Mock
    private Session sessionMock;
    @Mock
    private ConsoleReader consoleReaderMock;
    @Spy
    private ShellEvaluationStrategy strategySpy = new ShellEvaluationStrategy();

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        strategySpy.setParser(operationParserMock);
        strategySpy.setConsole(consoleReaderMock);
        SessionFactory.updateSharedSession(sessionMock);
    }

    @Test
    public void shouldExecuteTwoOperationsInShellModeAndInterruptItDueToTheExitKeyBeenPressed() throws Exception {

        // Given
        List<String> script = Collections.singletonList("login superuser%superuser@localhost:8080/jrs-test");
        Mockito.doReturn("export all")
                .doThrow(new UserInterruptException("Let's pretend that we've pressed `Ctrl+C` key"))
                .when(consoleReaderMock)
                .readLine();

        Mockito.doReturn(loginOperationMock).when(operationParserMock).parseOperation("login superuser%superuser@localhost:8080/jrs-test");
        Mockito.doReturn(exportOperationMock).when(operationParserMock).parseOperation("export all");
        Mockito.doReturn(loginOperationResultMock).when(loginOperationMock).execute(sessionMock);
        Mockito.doReturn(exportOperationResultMock).when(exportOperationMock).execute(sessionMock);
        Mockito.doReturn("Message1").when(loginOperationResultMock).getResultMessage();
        Mockito.doReturn("Message2").when(exportOperationResultMock).getResultMessage();
        Mockito.doReturn(ResultCode.SUCCESS).when(loginOperationResultMock).getResultCode();
        Mockito.doReturn(ResultCode.SUCCESS).when(exportOperationResultMock).getResultCode();
        Mockito.doNothing().when(strategySpy).print("Message1");
        Mockito.doNothing().when(strategySpy).print("Message2");

        // When
        OperationResult result = strategySpy.eval(script);

        // Then
        Assert.assertEquals(result.getResultCode(), ResultCode.INTERRUPTED);

        Mockito.verify(loginOperationMock, Mockito.times(1)).execute(sessionMock);
        Mockito.verify(exportOperationMock, Mockito.times(1)).execute(sessionMock);
        Mockito.verify(loginOperationResultMock, Mockito.times(1)).getResultCode();
        Mockito.verify(loginOperationResultMock, Mockito.times(1)).getResultMessage();
        Mockito.verify(exportOperationResultMock, Mockito.times(1)).getResultCode();
        Mockito.verify(exportOperationResultMock, Mockito.times(1)).getResultMessage();
        Mockito.verify(strategySpy, Mockito.times(1)).print("Message1");
        Mockito.verify(strategySpy, Mockito.times(1)).print("Message2");
        Mockito.verify(strategySpy, Mockito.times(1)).logout();
        Mockito.verifyNoMoreInteractions(loginOperationMock);
        Mockito.verifyNoMoreInteractions(exportOperationMock);
    }

    @Test
    public void shouldExitShellModeIfLoginFailed() throws IOException {

        // Given
        List<String> script = Collections.singletonList("login wrong%credentials@localhost:8080/jrs-test");

        Mockito.doReturn(loginOperationMock).when(operationParserMock).parseOperation("login wrong%credentials@localhost:8080/jrs-test");
        Mockito.doReturn(failedLoginOperationResultMock).when(loginOperationMock).execute(sessionMock);
        Mockito.doReturn(ResultCode.FAILED).when(failedLoginOperationResultMock).getResultCode();
        Mockito.doReturn("Failed").when(failedLoginOperationResultMock).getResultMessage();
        Mockito.doNothing().when(strategySpy).print("Failed");

        // When
        OperationResult result = strategySpy.eval(script);

        // Then
        Assert.assertEquals(result.getResultCode(), ResultCode.FAILED);
        Assert.assertEquals(result.getResultMessage(), "Failed");
        Assert.assertEquals(result.getContext(), loginOperationMock);

        Mockito.verify(loginOperationMock, Mockito.times(1)).execute(sessionMock);
        Mockito.verify(operationParserMock, Mockito.times(1)).parseOperation("login wrong%credentials@localhost:8080/jrs-test");
        Mockito.verify(failedLoginOperationResultMock, Mockito.times(2)).getResultMessage();
        Mockito.verify(strategySpy, Mockito.times(1)).print("Failed");
    }

    @After
    public void after() {
        Mockito.reset(
                operationParserMock,
                loginOperationMock,
                sessionMock,
                loginOperationResultMock,
                exportOperationResultMock,
                consoleReaderMock,
                exportOperationMock,
                failedLoginOperationResultMock
        );
    }

}