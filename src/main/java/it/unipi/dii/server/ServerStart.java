package it.unipi.dii.server;

import it.unipi.dii.server.databaseDriver.DBExecutionMode;
import org.apache.commons.cli.*;

import java.io.IOException;

public class ServerStart {

    public static void main(String[] args) {

        int portNumber;
        int backlogLength;
        DBExecutionMode dbExecutionMode;
        Options options = new Options();

        Option portNumberOpt = new Option("p", "portnumber", true, "port number,default 8080 ");
        portNumberOpt.setRequired(false);
        options.addOption(portNumberOpt);

        Option dbModeOpt = new Option("d", "dbmode", true, """
                Database connection mode(default LOCAL):
                \tLOCAL(--local)\t->the application connects to a local deployment of the db.
                \tCLUSTER(--cluster)->the application connects to a remote cluster of dbs""");

        dbModeOpt.setRequired(false);
        options.addOption(dbModeOpt);

        Option backlogOpt = new Option("b", "backlog", true, "server connection backlog");

        backlogOpt.setRequired(false);
        options.addOption(backlogOpt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("p")) {
                portNumber = Integer.parseInt(cmd.getOptionValue("c"));
                if (portNumber > 65535 || portNumber < 2000) {
                    throw new RuntimeException("Port number must be between 2000 and 65535");
                }
            } else {
                portNumber = 8080;
            }

            if (cmd.hasOption("d")) {
                dbExecutionMode = DBExecutionMode.valueOf(cmd.getOptionValue("d").toUpperCase());
            } else {
                dbExecutionMode = DBExecutionMode.LOCAL;
            }

            if (cmd.hasOption("b")) {
                backlogLength = Integer.parseInt(cmd.getOptionValue("b"));
            } else {
                backlogLength = 4;
            }

            Server server = new Server(portNumber, backlogLength, dbExecutionMode);
            server.waitForConnection();

        } catch (NumberFormatException nfe) {
            System.out.println("Port number and/or backlog are not valid integers");
        } catch (IOException ioe) {
            System.out.println("A new server socket cannot be allocated. See stack trace for error details");
            ioe.printStackTrace();
        } catch (ParseException pe) {
            System.out.println("Unexpected parsing exception: " + pe.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
