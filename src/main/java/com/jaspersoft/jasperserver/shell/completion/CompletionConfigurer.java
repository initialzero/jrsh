package com.jaspersoft.jasperserver.shell.completion;

import com.jaspersoft.jasperserver.shell.completion.completer.CommandCommonParameterCompleter;
import com.jaspersoft.jasperserver.shell.completion.completer.CustomParameterCompleter;
import com.jaspersoft.jasperserver.shell.completion.completer.ParameterCompleter;
import com.jaspersoft.jasperserver.shell.completion.completer.RepositoryPathCompleter;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.NullCompleter;
import jline.console.completer.StringsCompleter;

import static com.jaspersoft.jasperserver.shell.profile.ProfileUtil.list;
import static java.util.Arrays.asList;

/**
 * @author Alexander Krasnyanskiy
 */
public class CompletionConfigurer {

    private AggregateCompleter aggregator;

    public CompletionConfigurer() {

        Completer exit = new StringsCompleter("exit");
        Completer clear = new StringsCompleter("clear");
        Completer session = new StringsCompleter("session");
        Completer logout = new StringsCompleter("logout");

        Completer profile = new StringsCompleter("profile");
        Completer profileParameters = new CommandCommonParameterCompleter("save", "load", "list");

        Completer login = new StringsCompleter("login");
        Completer loginParameters = new CommandCommonParameterCompleter("--server", "--username", "--password");

        Completer show = new StringsCompleter("show");
        Completer showParameters = new CommandCommonParameterCompleter("repo", "server-info");


        /**
         * Replicate Completer
         */
        Completer replicate = new StringsCompleter("replicate");
        Completer firstProfileName = new ParameterCompleter(list());
        Completer direction = new ParameterCompleter("to");
        Completer secondProfileName = new CustomParameterCompleter(list());


        /**
         * Export Completer
         */
        Completer export = new StringsCompleter("export");
        Completer all = new StringsCompleter("all");
        Completer repo = new StringsCompleter("repo");
        Completer role = new StringsCompleter("role");
        Completer user = new StringsCompleter("user");
        Completer path = new RepositoryPathCompleter();


        /**
         * Import Completer
         */
        Completer import_ = new StringsCompleter("import");
        Completer file = new FileNameCompleter();
        Completer events = new CommandCommonParameterCompleter("with-audit-events", "with-access-events",
                "with-monitoring-events", "with-events", "with-update", "with-skip-user-update");


        /**
         * Help Completer
         */
        Completer help = new StringsCompleter("help");
        Completer helpParameters = new CommandCommonParameterCompleter(asList("login", "logout", "import",
                "export", "exit", "show", "session", "replicate", "profile"));


        Completer loginCompleter = new ArgumentCompleter(login, loginParameters);
        Completer helpCompleter = new ArgumentCompleter(help, helpParameters, new NullCompleter());
        Completer showCompleter = new ArgumentCompleter(show, showParameters, new NullCompleter());
        Completer importCompleter = new ArgumentCompleter(import_, file, events);
        Completer profileCompleter = new ArgumentCompleter(profile, profileParameters, new NullCompleter());
        Completer replicateCompleter = new ArgumentCompleter(replicate, firstProfileName, direction,
                secondProfileName, new NullCompleter());

        aggregator = new AggregateCompleter(exit,
                // fixme!
                // replace this workaround with the better logic
                new ArgumentCompleter(export, new NullCompleter()),
                new ArgumentCompleter(export, all, new NullCompleter()),
                new ArgumentCompleter(export, repo, new NullCompleter()),
                new ArgumentCompleter(export, repo, path, events),
                new ArgumentCompleter(export, role, new NullCompleter()),
                new ArgumentCompleter(export, user, new NullCompleter()),

                clear, logout, replicateCompleter, profileCompleter, session, loginCompleter,
                importCompleter, showCompleter, helpCompleter);
    }

    public AggregateCompleter getAggregator() {
        return aggregator;
    }
}
