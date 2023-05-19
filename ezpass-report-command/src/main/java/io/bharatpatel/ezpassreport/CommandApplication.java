package io.bharatpatel.ezpassreport;

import io.bharatpatel.ezpassreport.models.ReportRequest;
import io.bharatpatel.ezpassreport.services.CommandLineService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

public class CommandApplication {

    private final CommandLineService commandLineService = CommandLineService.getInstance();
    private final DefaultParser commandLineParser = new DefaultParser();

    public static void main(String[] args) {

        var command = new CommandApplication();
        var commandLine = command.parseArgs(args);

        if (commandLine == null) {
            command.commandLineService.printHelp();
            return;
        }

        var loginCredentials = command.parseLoginCredentials(commandLine);
        var reportRequest = command.parseReportRequest(commandLine);

        if (loginCredentials == null || reportRequest == null) {
            command.commandLineService.printHelp();
            return;
        }

        var session = ReportManagerKt.getSession(loginCredentials);

        var transponders = ReportManagerKt.getTransponders(session);
        System.out.println(transponders);

        var vehicles = ReportManagerKt.getVehicles(session);
        System.out.println(vehicles);

        var transactions = ReportManagerKt.getTransactions(session, reportRequest.startDate(), reportRequest.endDate());
        System.out.println(transactions);
    }

    private CommandLine parseArgs(String[] args) {
        try {
            return this.commandLineParser.parse(commandLineService.getOptions(), args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private LoginCredentials parseLoginCredentials(CommandLine commandLine) {
        try {
            return this.commandLineService.loginCredentials(commandLine);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private ReportRequest parseReportRequest(CommandLine commandLine) {
        try {
            return this.commandLineService.reportRequest(commandLine);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
