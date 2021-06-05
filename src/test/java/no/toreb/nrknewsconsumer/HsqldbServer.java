package no.toreb.nrknewsconsumer;

import org.hsqldb.server.Server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class HsqldbServer {

    public static void main(final String[] args) throws IOException {
        startHsqlDbServer();
    }

    static void startHsqlDbServer() throws IOException {
        final Server server = new Server();
        server.setDatabasePath(0, "file:./target/application;sql.syntax_pgs=true");
        server.setDatabaseName(0, "application");
        server.setPort(9191);
        server.setLogWriter(new PrintWriter(new BufferedWriter(new FileWriter("./target/hsqldb-server.log"))));
        server.start();
    }
}
