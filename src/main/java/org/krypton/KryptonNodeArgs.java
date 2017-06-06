package org.krypton;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.nustaq.kontraktor.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by ruedi on 05.06.17.
 */
public class KryptonNodeArgs {

    @Parameter(names={"-h","-host"}, description = "host")
    String host = "localhost";

    @Parameter(names={"-p","-port"}, description = "port")
    int port = 80;

    @Parameter(names = {"-help","-?", "--help"}, help = true, description = "display help")
    boolean help;

    @Parameter(names = {"-b"}, description = "list of bootstrap node url's")
    List<String> bootstrap;

    @Parameter(names = {"-nolog"}, help = true, description = "log to sysout without log4j", arity = 1)
    public boolean sysoutlog = true;

    public boolean isHelp() {
        return help;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public boolean isSysoutlog() {
        return sysoutlog;
    }

    public static KryptonNodeArgs parseCommandLine(String[] args, KryptonNodeArgs options) {

        JCommander com = new JCommander();
        com.addObject(options);
        try {
            com.parse(args);
        } catch (Exception ex) {
            System.out.println("command line error: '"+ex.getMessage()+"'");
            options.help = true;
        }
        if ( options.help ) {
            com.usage();
            System.exit(-1);
        }
        return options;
    }

    public List<String> getBootstrap() {
        return bootstrap;
    }
}
