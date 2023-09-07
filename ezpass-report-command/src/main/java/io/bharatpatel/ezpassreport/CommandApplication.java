package io.bharatpatel.ezpassreport;

import io.bharatpatel.ezpassreport.models.ReportRequest;
import io.bharatpatel.ezpassreport.services.CommandLineService;
import io.bharatpatel.ezpassreport.services.TablePrinter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CommandApplication {

    private final CommandLineService commandLineService = CommandLineService.getInstance();
    private final DefaultParser commandLineParser = new DefaultParser();

    public static void main(String[] args) throws InterruptedException {

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

        var executorService = Executors.newFixedThreadPool(3);

        try {
            CompletableFuture.supplyAsync(() -> ReportManagerKt.getSession(loginCredentials), executorService)
                    .thenComposeAsync(session -> CompletableFuture.allOf(
                            CompletableFuture.supplyAsync(() -> ReportManagerKt.getTransponders(session), executorService)
                                    .thenAccept(printTransponders),
                            CompletableFuture.supplyAsync(() -> ReportManagerKt.getVehicles(session), executorService)
                                    .thenAccept(printVehicles),
                            CompletableFuture.supplyAsync(() -> ReportManagerKt.getTransactions(session, reportRequest.startDate(), reportRequest.endDate()), executorService)
                                    .thenAccept(printTransactions)
                    )).exceptionally(t -> {
                        System.out.println(t.getMessage());
                        return null;
                    }).join();
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        }

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

    private static final Consumer<List<Transponder>> printTransponders = transponders -> new TablePrinter<>(transponders)
            .title("Transponders")
            .column("Tag", Transponder::getTagNumber)
            .column("Style", Transponder::getStyle)
            .column("Color", Transponder::getColor)
            .column("Status", Transponder::getStatus)
            .print();

    private static final Consumer<List<Vehicle>> printVehicles = vehicles -> new TablePrinter<>(vehicles)
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

    private static final Consumer<List<Transaction>> printTransactions = transactions -> new TablePrinter<>(transactions)
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
