package com.jaspersoft.jasperserver.shell.command.impl;

import com.jaspersoft.jasperserver.shell.command.Command;
import com.jaspersoft.jasperserver.shell.Context;
import com.jaspersoft.jasperserver.shell.ContextAware;
import com.jaspersoft.jasperserver.shell.exception.InterfaceException;
import com.jaspersoft.jasperserver.shell.CommandFactory;
import com.jaspersoft.jasperserver.shell.Parameter;

import java.util.Map.Entry;

import static java.lang.System.out;

/**
 * @author Alexander Krasnyanskiy
 */
public class HelpCommand extends Command implements ContextAware {

    private Context context;

    public HelpCommand() {
        name = "help";
        description = "Show all information about JRSH.";
        parameters.add(new Parameter().setName("anonymous").setOptional(true));
    }

    @Override
    public void run() {
        Parameter p = parameter("anonymous");
        if (p != null && !p.getValues().isEmpty()) {
            try {
                String val = p.getValues().get(0);
                Command cmd = CommandFactory.createCommand(val);
                out.printf("\t%s%s\n", "Description: ", cmd.getDescription());
                String usage = cmd.getUsageDescription();
                if (usage != null) {
                    out.println(cmd.getUsageDescription());
                }
            } catch (InterfaceException e) {
                out.printf("error: %s\n", e.getMessage());
            }
        } else {
            out.print("Usage: <command> [options] [args]\n"
                    + "Type 'help <command>' for help on a specific command.\n");
            out.println("\n\u001B[30;47mAvailable commands: \u001B[0m");
            for (Entry<String, String> e : context.getCmdDescription().entrySet()) {
                if (e.getKey() != null) {
                    if (e.getKey().equals("replicate")) {
                        out.printf("\t%s\t%s\n", e.getKey(), e.getValue());
                    } else {
                        out.printf("\t%s\t\t%s\n", e.getKey(), e.getValue());
                    }
                }
            }
        }
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}