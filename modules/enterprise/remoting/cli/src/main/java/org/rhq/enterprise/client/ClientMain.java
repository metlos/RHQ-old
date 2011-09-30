/*
 * RHQ Management Platform
 * Copyright (C) 2005-2009 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.enterprise.client;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.MultiCompletor;
import jline.SimpleCompletor;
import mazz.i18n.Msg;

import org.rhq.core.domain.auth.Subject;
import org.rhq.enterprise.client.commands.ClientCommand;
import org.rhq.enterprise.client.commands.ScriptCommand;

/**
 * @author Greg Hinkle
 * @author Simeon Pinder
 */
public class ClientMain {

    // I18N messaging
    private static final Msg MSG = ClientI18NFactory.getMsg();

    // Stored command map. Key to instance that handles that command.
    private static Map<String, ClientCommand> commands = new HashMap<String, ClientCommand>();

    /**
     * This is the thread that is running the input loop; it accepts prompt commands from the user.
     */
    private Thread inputLoopThread;

    private BufferedReader inputReader;

    // JLine console reader
    private ConsoleReader consoleReader;

    private boolean stdinInput = true;

    // for feedback to user.
    private PrintWriter outputWriter;

    // Local storage of credentials for this session/client
    private String transport = null;
    private String host = null;
    private int port = 7080;
    private String user;
    private String pass;
    private ArrayList<String> notes = new ArrayList<String>();

    // reference to the webservice reference factory
    private RemoteClient remoteClient;

    // The subject that will be used to carry out all requested actions
    private Subject subject;

    private InteractiveJavascriptCompletor serviceCompletor;

    private boolean interactiveMode = true;

    private Recorder recorder = new NoOpRecorder();

    private String language = "JavaScript";
    
    // Entrance to main.
    public static void main(String[] args) throws Exception {

        // instantiate
        ClientMain main = new ClientMain();

        initCommands();

        // process startup arguments
        main.processArguments(args);

        if (main.interactiveMode) {
            // begin client access loop
            main.inputLoop();
        }
    }

    private static void initCommands() {
        for (Class<ClientCommand> commandClass : ClientCommand.COMMANDS) {
            ClientCommand command = null;
            try {
                command = commandClass.newInstance();
                commands.put(command.getPromptCommandString(), command);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    //
    public ClientMain() throws Exception {

        // this.inputReader = new BufferedReader(new
        // InputStreamReader(System.in));

        // initialize the printwriter to system.out for console conversations
        this.outputWriter = new PrintWriter(System.out, true);

        // Initialize JLine console elements.
        consoleReader = new jline.ConsoleReader();

        // Setup the command line completers for listed actions for the user before login
        // completes initial commands available
        Completor commandCompletor = new SimpleCompletor(commands.keySet().toArray(new String[commands.size()]));
        // completes help arguments (basically, help <command>)
        Completor helpCompletor = new ArgumentCompletor(new Completor[] { new SimpleCompletor("help"),
            new SimpleCompletor(commands.keySet().toArray(new String[commands.size()])) });

        this.serviceCompletor = new InteractiveJavascriptCompletor(consoleReader);
        consoleReader.addCompletor(new MultiCompletor(new Completor[] { serviceCompletor, helpCompletor,
            commandCompletor }));

        // enable pagination
        consoleReader.setUsePagination(true);
    }

    // ?? what is this again? Might be able to remove this.
    public void start() {
        outputWriter = new PrintWriter(System.out);
        // inputReader = new BufferedReader(new InputStreamReader(System.in));

    }

    public String getUserInput(String prompt) {

        String input_string = "";
        boolean use_default_prompt = (prompt == null);

        while ((input_string != null) && (input_string.trim().length() == 0)) {
            if (prompt == null) {
                if (!loggedIn()) {
                    prompt = "unconnected$ ";
                } else {
                    // prompt = host + ":" + port + "> ";
                    // Modify the prompt to display host:port(logged-in-user)
                    String loggedInUser = "";
                    if ((getSubject() != null) && (getSubject().getName() != null)) {
                        loggedInUser = getSubject().getName();
                    }
                    if (loggedInUser.trim().length() > 0) {
                        prompt = loggedInUser + "@" + host + ":" + port + "$ ";
                    } else {
                        prompt = host + ":" + port + "$ ";
                    }
                }
            }
            // outputWriter.print(prompt);

            try {
                outputWriter.flush();
                input_string = consoleReader.readLine(prompt);
                // inputReader.readLine();
            } catch (Exception e) {
                input_string = null;
            }
        }

        if (input_string != null) {
            // if we are processing a script, show the input that was just read
            if (!stdinInput) {
                outputWriter.println(input_string);
            }
        } else if (!stdinInput) {
            // if we are processing a script, we hit the EOF, so close the inputstream
            try {
                inputReader.close();
            } catch (IOException e1) {
            }
        }

        return input_string;
    }

    public ConsoleReader getConsoleReader() {
        return consoleReader;
    }

    /**
     * Indicates whether the 'Subject', used for all authenticated actions, is currently logged in.
     *
     * @return flag indicating status of realtime check.
     */
    public boolean loggedIn() {
        return subject != null && remoteClient != null & remoteClient.isLoggedIn();
    }

    /**
     * This enters in an infinite loop. Because this never returns, the current thread never dies and hence the agent
     * stays up and running. The user can enter agent commands at the prompt - the commands are sent to the agent as if
     * the user is a remote client.
     */
    private void inputLoop() {
        // we need to start a new thread and run our loop in it; otherwise, our
        // shutdown hook doesn't work
        Runnable loop_runnable = new Runnable() {
            public void run() {
                while (true) {
                    String cmd;
                    cmd = getUserInput(null);

                    try {
                        recorder.record(cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        // parse the command into separate arguments and execute
                        String[] cmd_args = parseCommandLine(cmd);
                        boolean can_continue = executePromptCommand(cmd_args);

                        // break the input loop if the prompt command told us to exit
                        // if we are not in daemon mode, this really will end up killing the agent
                        if (!can_continue) {
                            break;
                        }
                    } catch (Throwable t) {
                        // outputWriter.println(ThrowableUtil.getAllMessages(t));
                        t.printStackTrace(outputWriter);
                        // LOG.debug(t,
                        // AgentI18NResourceKeys.COMMAND_FAILURE_STACK_TRACE);
                    }
                }

                return;
            }
        };

        // start the thread
        inputLoopThread = new Thread(loop_runnable);
        inputLoopThread.setName("RHQ Client Prompt Input Thread");
        inputLoopThread.setDaemon(false);
        inputLoopThread.start();

        return;
    }

    public boolean executePromptCommand(String[] args) throws Exception {
        String cmd = args[0];
        if (commands.containsKey(cmd)) {
            ClientCommand command = commands.get(cmd);

            if (shouldDisplayHelp(args)) {
                outputWriter.println("syntax: " + command.getSyntax());
                outputWriter.println("description: " + command.getHelp() + "\n");
                return true;
            }

            if (shouldDisplayDetailedHelp(args)) {
                outputWriter.println("syntax: " + command.getSyntax());
                outputWriter.println("description: " + command.getDetailedHelp() + "\n");
                return true;
            }

            try {
                boolean response = command.execute(this, args);
                processNotes(outputWriter);
                outputWriter.println("");
                return response;
            } catch (ArrayIndexOutOfBoundsException e) {
                outputWriter.println("An incorrect number of arguments was specified.");
                outputWriter.println("Expected syntax: " + command.getSyntax());
            }
        } else {
            boolean result = commands.get("exec").execute(this, args);
            if (loggedIn()) {
                this.serviceCompletor.setContext(((ScriptCommand) commands.get("exec")).getContext());
            }

            return result;
        }
        return true;
    }

    private boolean shouldDisplayHelp(String[] args) {
        if (args.length < 2) {
            return false;
        }

        return args[1].equals("-h");
    }

    private boolean shouldDisplayDetailedHelp(String[] args) {
        if (args.length < 2) {
            return false;
        }

        return args[1].equals("--help");
    }

    /**
     * Meant to display small note/helpful ui messages to the user as feedback from the previous command.
     *
     * @param outputWriter2
     *            reference to printWriter.
     */
    private void processNotes(PrintWriter outputWriter2) {
        if ((outputWriter2 != null) && (notes.size() > 0)) {
            for (String line : notes) {
                outputWriter2.println("-> " + line);
            }
            notes.clear();
        }
    }

    /**
     * Given a command line, this will parse each argument and return the argument array.
     *
     * @param cmdLine
     *            the command line
     * @return the array of command line arguments
     */
    public String[] parseCommandLine(String cmdLine) {
        if (cmdLine == null) {
            return new String[] { "" };
        }

        ByteArrayInputStream in = new ByteArrayInputStream(cmdLine.getBytes());
        StreamTokenizer strtok = new StreamTokenizer(new InputStreamReader(in));
        List<String> args = new ArrayList<String>();
        boolean keep_going = true;

        // we don't want to parse numbers and we want ' to be a normal word
        // character
        strtok.ordinaryChars('0', '9');
        strtok.ordinaryChar('.');
        strtok.ordinaryChar('-');
        strtok.ordinaryChar('\'');
        strtok.wordChars(33, 127);

        // parse the command line
        while (keep_going) {
            int nextToken;

            try {
                nextToken = strtok.nextToken();
            } catch (IOException e) {
                nextToken = StreamTokenizer.TT_EOF;
            }

            if (nextToken == java.io.StreamTokenizer.TT_WORD) {
                args.add(strtok.sval);
            } else if (nextToken == '\"') {
                args.add(strtok.sval);
            } else if ((nextToken == java.io.StreamTokenizer.TT_EOF) || (nextToken == java.io.StreamTokenizer.TT_EOL)) {
                keep_going = false;
            }
        }

        return args.toArray(new String[args.size()]);
    }

    private void displayUsage() {
        outputWriter.println("rhq-cli.sh [-h] [-u user] [-p pass] [-P] [-s host] [-t port] [-f file]|[-c command]");
    }

    void processArguments(String[] args) throws IllegalArgumentException, IOException {
        String sopts = "-:hu:p:Ps:t:r:c:f:v";
        LongOpt[] lopts = { new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("user", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
            new LongOpt("password", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
            new LongOpt("prompt", LongOpt.OPTIONAL_ARGUMENT, null, 'P'),
            new LongOpt("host", LongOpt.REQUIRED_ARGUMENT, null, 's'),
            new LongOpt("port", LongOpt.REQUIRED_ARGUMENT, null, 't'),
            new LongOpt("transport", LongOpt.REQUIRED_ARGUMENT, null, 'r'),
            new LongOpt("command", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
            new LongOpt("file", LongOpt.NO_ARGUMENT, null, 'f'),
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            new LongOpt("args-style", LongOpt.REQUIRED_ARGUMENT, null, -2),
            new LongOpt("language", LongOpt.OPTIONAL_ARGUMENT, null, 'l') };
            
        Getopt getopt = new Getopt("Cli", args, sopts, lopts, false);
        int code;

        List<String> execCmdLine = new ArrayList<String>();
        execCmdLine.add("exec");

        while ((code = getopt.getopt()) != -1) {
            switch (code) {
                case ':':
                case '?': {
                    // for now both of these should exit
                    displayUsage();
                    throw new IllegalArgumentException(MSG.getMsg(ClientI18NResourceKeys.BAD_ARGS));
                }

                case 1: {
                    // this catches non-option arguments which can be passed when running a script in non-interactive mode
                    // with -f or running a single command in non-interactive mode with -c.
                    execCmdLine.add(getopt.getOptarg());
                    break;
                }

                case 'h': {
                    displayUsage();
                    break;
                }

                case 'u': {
                    this.user = getopt.getOptarg();
                    break;
                }
                case 'p': {
                    this.pass = getopt.getOptarg();
                    break;
                }
                case 'P': {
                    this.pass = this.consoleReader.readLine("password: ", (char) 0);
                    break;
                }
                case 'c': {
                    interactiveMode = false;
                    execCmdLine.add(getopt.getOptarg());
                    break;
                }
                case 'f': {
                    interactiveMode = false;
                    execCmdLine.add("-f");
                    execCmdLine.add(getopt.getOptarg());
                    break;
                }
                case -2: {
                    execCmdLine.add("--args-style=" + getopt.getOptarg());
                    break;
                }
                case 's': {
                    setHost(getopt.getOptarg());
                    break;
                }
            case 'r': {
                setTransport(getopt.getOptarg());
                break;
            }
                case 't': {
                    String portArg = getopt.getOptarg();
                    try {
                        setPort(Integer.parseInt(portArg));
                    } catch (Exception e) {
                        outputWriter.println("Invalid port [" + portArg + "]");
                    }
                    break;
                }
                case 'v': {
                    String versionString = Version.getProductNameAndVersionBuildInfo();
                    outputWriter.println(versionString);
                    break;
                }
                case 'l': {
                    language = getopt.getOptarg();
                    break;
                }
            }
        }

        if (interactiveMode) {
            outputWriter.println(Version.getProductNameAndVersion());
        }

        if (user != null && pass != null) {
            ClientCommand loginCmd = commands.get("login");
            if (host != null) {
                loginCmd.execute(this, new String[] { "login", user, pass, host, String.valueOf(port), transport });
            } else {
                loginCmd.execute(this, new String[] { "login", user, pass });
            }
            if (!loggedIn()) {
                return;
            }
        }

        if (!interactiveMode) {
            commands.get("exec").execute(this, execCmdLine.toArray(new String[] {}));
        }
    }

    public RemoteClient getRemoteClient() {
        return remoteClient;
    }

    public void setRemoteClient(RemoteClient remoteClient) {
        this.remoteClient = remoteClient;

        if (remoteClient != null) {
            ScriptCommand sc = (ScriptCommand) commands.get("exec");
            sc.initBindings(this);
            this.serviceCompletor.setContext(sc.getContext());
            this.serviceCompletor.setServices(remoteClient.getManagers());
        }
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public PrintWriter getPrintWriter() {
        return outputWriter;
    }

    public void setPrintWriter(PrintWriter writer) {
        this.outputWriter = writer;
    }

    public int getConsoleWidth() {
        return this.consoleReader.getTermwidth();
    }

    public Map<String, ClientCommand> getCommands() {
        return commands;
    }

    public String getLanguage() {
        return language;
    }
    
    /**
     * This method allows ClientCommands to insert a small note to be displayed after the command has been executed. A
     * note can be an indicaiton of a problem that was handled or a note about some option that should be changed.
     *
     * These notes are meant to be terse, and pasted/purged at the end of every command execution.
     *
     * @param note
     *            String. Ex."There were errors retrieving some data from the server objects. See System Admin."
     */
    public void addMenuNote(String note) {
        if ((note != null) && (note.trim().length() > 0)) {
            notes.add(note);
        }
    }

    public boolean isInteractiveMode() {
        return interactiveMode;
    }

    public Recorder getRecorder() {
        return recorder;
    }

    public void setRecorder(Recorder recorder) {
        this.recorder = recorder;
    }
}
