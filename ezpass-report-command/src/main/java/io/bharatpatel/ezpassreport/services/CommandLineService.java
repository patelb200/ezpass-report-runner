package io.bharatpatel.ezpassreport.services;

import io.bharatpatel.ezpassreport.LoginCredentials;
import io.bharatpatel.ezpassreport.models.ReportRequest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CommandLineService {

    private static final CommandLineService INSTANCE = new CommandLineService();
    private final Options options;

    private CommandLineService() {
        this.options = initOptions();
    }

    public static CommandLineService getInstance() {
        return INSTANCE;
    }

    public LoginCredentials loginCredentials(CommandLine commandLine) {
        String username;
        if (commandLine.hasOption("u"))
            username = commandLine.getOptionValue("u");
        else
            throw new IllegalStateException("Username is missing");

        String password;
        if (commandLine.hasOption("p"))
            password = commandLine.getOptionValue("p");
        else
            throw new IllegalStateException("Password is missing");

        return new LoginCredentials(username, password);
    }

    public ReportRequest reportRequest(CommandLine commandLine) {
        LocalDate startDate;
        if (commandLine.hasOption("startDate")) {
            try {
                startDate = LocalDate.parse(commandLine.getOptionValue("startDate"), DateTimeFormatter.ofPattern("uuuuMMdd"));

            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Unable to parse start date");
            }
        } else {
            throw new IllegalArgumentException("Missing report start date");
        }

        LocalDate endDate;
        if (commandLine.hasOption("startDate")) {
            try {
                endDate = LocalDate.parse(commandLine.getOptionValue("endDate"), DateTimeFormatter.ofPattern("uuuuMMdd"));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Unable to parse end date");
            }
        } else {
            throw new IllegalArgumentException("Missing report end date");
        }

        return new ReportRequest(startDate, endDate);
    }

    private Options initOptions() {

        var options = new Options();

        options.addOption("u", true, "Account username");
        options.addOption("p", true, "Account password");
        options.addOption("startDate", true, "Report start date in YYYYMMDD format");
        options.addOption("endDate", true, "Report end date in YYYYMMDD format");

        return options;
    }

    public Options getOptions() {
        return this.options;
    }

    public void printHelp() {
        var formatter = new HelpFormatter();
        formatter.printHelp("ezpass-report", this.options);
    }
}
