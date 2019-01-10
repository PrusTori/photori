package com.viktoriia.photori.db;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class DatabaseBackup {

    public DatabaseBackup() {
    }

    public static void executeCommand(String operationType) {

        File backupFilePath = new File(
                System.getProperty("user.home")
                        + File.separator
                        + "Backups"
                        + File.separator
        );
        if (!backupFilePath.exists()) {
            File dir = backupFilePath;
            dir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String backupFileName = "photori_" + sdf.format(new Date()) + ".backup";

        List<String> commands = getPgCommands(backupFilePath, backupFileName, operationType);
        if (!commands.isEmpty()) {
            try {
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.environment().put("PGPASSWORD", "pass129049p");

                Process process = pb.start();

                try (BufferedReader buf = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line = buf.readLine();
                    while (line != null) {
                        System.err.println(line);
                        line = buf.readLine();
                    }
                }

                process.waitFor();
                process.destroy();

            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Error: Invalid params.");
        }
    }

    private static List<String> getPgCommands(
            File backupFilePath,
            String backupFileName,
            String type
    ) {

        ArrayList<String> commands = new ArrayList<>();
        switch (type) {
            case "backup":
                commands.add("pg_dump");
                commands.add("-h");
                commands.add("localhost");
                commands.add("-p");
                commands.add("5432");
                commands.add("-U");
                commands.add("postgres");
                commands.add("-F");
                commands.add("c");
                commands.add("-b");
                commands.add("-v");
                commands.add("-f");
                commands.add(backupFilePath.getAbsolutePath() + File.separator + backupFileName);
                commands.add("-d");
                commands.add("photori");
                break;
            case "restore":
                commands.add("pg_restore");
                commands.add("-h");
                commands.add("localhost");
                commands.add("-p");
                commands.add("5432");
                commands.add("-U");
                commands.add("postgres");
                commands.add("-d");
                commands.add("photori");
                commands.add("-v");
                commands.add(backupFilePath.getAbsolutePath() + File.separator + backupFileName);
                break;
            default:
                return Collections.EMPTY_LIST;
        }
        return commands;
    }

}