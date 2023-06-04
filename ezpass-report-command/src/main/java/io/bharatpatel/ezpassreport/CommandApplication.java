package io.bharatpatel.ezpassreport;

import io.bharatpatel.ezpassreport.models.ReportRequest;
import io.bharatpatel.ezpassreport.services.CommandLineService;
import io.bharatpatel.ezpassreport.services.TablePrinter;
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
        new TablePrinter<>(transponders)
                .title("Transponders")
                .column("Tag", Transponder::getTagNumber)
                .column("Style", Transponder::getStyle)
                .column("Color", Transponder::getColor)
                .column("Status", Transponder::getStatus)
                .print();

        var vehicles = ReportManagerKt.getVehicles(session);
        new TablePrinter<>(vehicles)
                .title("Vehicles")
                .column("Plate", Vehicle::getPlateNumber)
                .column("State", Vehicle::getState)
                .column("Make", Vehicle::getMake)
                .column("Model", Vehicle::getModel)
                .column("Year", Vehicle::getYear)
                .column("Color", Vehicle::getColor)
                .column("Temporary", v -> String.valueOf(v.getTemporary()))
                .column("Start", v -> v.getStartDate() == null ? "" : v.getStartDate().toString())
                .column("End", v -> v.getEndDate() == null ? "" : v.getEndDate().toString())
                .print();


        var transactions = ReportManagerKt.getTransactions(session, reportRequest.startDate(), reportRequest.endDate());
        new TablePrinter<>(transactions)
                .title("Transactions")
                .column("Id", Transaction::getTransactionId)
                .column("Date", t -> t.getPostDate().toString())
                .column("Type", Transaction::getTransactionType)
                .column("Transponder", Transaction::getTransponderNumber)
                .column("Plate", Transaction::getPlateNumber)
                .column("Entry Plaza", Transaction::getEntryPlaza)
                .column("Exit Plaza", Transaction::getExitPlaza)
                .column("Entry Time", t -> t.getEntryDateTime() == null ? "" : t.getEntryDateTime().toString())
                .column("Exit Time", t -> t.getExitDateTime() == null ? "" : t.getExitDateTime().toString())
                .column("Charge", t -> {
                    if (t.getCharge().getType() == ChargeType.DEBIT) {
                        return "-" + t.getCharge().getAmount();
                    } else {
                        return String.valueOf(t.getCharge().getAmount());
                    }
                })
                .print();
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
