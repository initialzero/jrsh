package com.jaspersoft.jasperserver.shell;

import com.jaspersoft.jasperserver.shell.command.Command;
import com.jaspersoft.jasperserver.shell.context.Context;
import com.jaspersoft.jasperserver.shell.exception.InterfaceException;
import com.jaspersoft.jasperserver.shell.exception.parser.MandatoryParameterException;
import com.jaspersoft.jasperserver.shell.exception.server.ServerException;
import com.jaspersoft.jasperserver.shell.parser.CommandParser;
import com.jaspersoft.jasperserver.shell.validator.CommandParameterValidator;

import java.util.Queue;
import java.util.Scanner;
import java.util.logging.LogManager;

import static com.jaspersoft.jasperserver.shell.ExecutionMode.SHELL;
import static com.jaspersoft.jasperserver.shell.ExecutionMode.TOOL;
import static com.jaspersoft.jasperserver.shell.factory.CommandFactory.create;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.util.Arrays.asList;

/**
 * @author Alexander Krasnyanskiy
 */
public class App {

    public static void main(String[] args) {

        App app = new App();
        Context context = new Context();
        Queue<Command> queue = null;

        CommandParser parser = new CommandParser(new CommandParameterValidator());
        parser.setContext(context);
        LogManager.getLogManager().reset();

        if (args.length < 1) {
            out.println("Welcome to JRSH v1.0!\n");
            while (true) {
                String input = app.readLine();
                if ("".equals(input)) continue;
                try {
                    queue = parser.parse(input);
                    queue.stream().filter(c -> c != null).forEach(c -> c.setMode(SHELL));
                } catch (InterfaceException e) {
                    if (e instanceof MandatoryParameterException) {
                        Command cmd = create("help");
                        cmd.parameter("anonymous").setValues(asList(e.getMessage()));
                        cmd.execute();
                    } else out.printf("error: %s\n", e.getMessage());
                    continue;
                }
                try {
                    queue.stream().filter(c -> c != null).forEach(Command::execute);
                } catch (ServerException e) {
                    out.printf("error: %s\n", e.getMessage());
                } catch (InterfaceException e) {
                    out.println(e.getMessage());
                }
            }
        } else {
            try {
                queue = parser.parse(args);
                queue.stream().filter(c -> c != null).forEach(c -> c.setMode(TOOL));
            } catch (InterfaceException e) {
                out.printf(e.getMessage());
                exit(1);
            }
            try {
                queue.stream().filter(c -> c != null).forEach(Command::execute);
            } catch (ServerException | InterfaceException e) {
                out.printf(e.getMessage());
                exit(1);
            }
        }
    }

    private String readLine() {
        out.printf((char) 27 + "[33m>>> " + (char) 27 + "[37m");
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }
}