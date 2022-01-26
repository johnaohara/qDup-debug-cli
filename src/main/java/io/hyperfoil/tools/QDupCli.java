///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.aesh:aesh:2.6

package io.hyperfoil.tools;

import org.aesh.AeshConsoleRunner;
import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Arguments;
import org.aesh.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class QDupCli {

    static final String baseDebugUri = "http://localhost:21337";
    static final String baseInfoUri = "http://localhost:31337";
    static final HttpClient httpClient;

    static {
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public static void main(String[] args) {
        AeshConsoleRunner.builder()
                .command(BreakpointCommand.class)
                .command(BreakpointsCommand.class)
                .command(CommandsCommand.class)
                .command(EvalCommand.class)
                .command(ResumeCommand.class)
                .command(StartCommand.class)
                .command(StateCommand.class)
                .prompt("[qDup]$ ")
                .addExitCommand()
                .start();
    }

    @CommandDefinition(name = "breakpoints", description = "")
    public static class BreakpointsCommand implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) {
            return executeStringGet(baseDebugUri.concat("/breakpoints"), commandInvocation);
        }
    }

    @CommandDefinition(name = "commands", description = "")
    public static class CommandsCommand implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) {
            return executeStringGet(baseDebugUri.concat("/commands"), commandInvocation);
        }
    }

    @CommandDefinition(name = "breakpoint", description = "")
    public static class BreakpointCommand implements Command {

        @Arguments
        private List<Resource> arguments;

        @Override
        public CommandResult execute(CommandInvocation commandInvocation) {
            arguments.stream().forEach(resouce -> {
                String uri = "http://localhost:21337/breakpoint/" + resouce.getName();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response =
                        null;
                try {
                    response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    commandInvocation.println(response.body());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });
            return CommandResult.SUCCESS;
        }
    }

    @CommandDefinition(name = "resume", description = "resume debug session running")
    public static class ResumeCommand implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) {
            HttpResponse<String> response = executeStringRequest(baseDebugUri.concat("/resume"));
            if (response != null && response.statusCode() == 200) {
                commandInvocation.println("Debug session resumed");
                return CommandResult.SUCCESS;
            } else {
                return CommandResult.FAILURE;
            }
        }
    }

    @CommandDefinition(name = "start", description = "start debug session running")
    public static class StartCommand implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) {
            HttpResponse<String> response = executeStringRequest(baseDebugUri.concat("/start"));
            if (response != null && response.statusCode() == 200) {
                commandInvocation.println("Debug session started");
                return CommandResult.SUCCESS;
            } else {
                return CommandResult.FAILURE;
            }
        }
    }

    @CommandDefinition(name = "state", description = "get global state")
    public static class StateCommand implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) {
            return executeStringGet(baseInfoUri.concat("/state"), commandInvocation);
        }
    }

    @CommandDefinition(name = "eval", description = "evaluate command state")
    public static class EvalCommand implements Command {
        @Arguments
        private List<Resource> arguments;
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) {
            if (arguments.size() != 2) {
                commandInvocation.println("Please supply only 2 arguments");
                return CommandResult.FAILURE;
            }

            String stateUri = baseDebugUri.concat("/state");
            for(Resource arg: arguments){
                stateUri = stateUri.concat("/").concat(URLEncoder.encode(arg.getName()));
            }
            return executeStringGet(stateUri, commandInvocation);
        }
    }

    private static CommandResult executeStringGet(String uri, CommandInvocation commandInvocation) {
        HttpResponse<String> response = executeStringRequest(uri);
        if (response != null && response.statusCode() == 200) {
            commandInvocation.println(response.body());
            return CommandResult.SUCCESS;
        } else {
            return CommandResult.FAILURE;
        }

    }

    private static HttpResponse<String> executeStringRequest(String uri) {
        return executeRequest(uri, HttpResponse.BodyHandlers.ofString());
    }

    private static <T> HttpResponse<T> executeRequest(String uri, HttpResponse.BodyHandler<T> responseBodyHandler) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
        HttpResponse<T> response = null;
        try {
            response = httpClient.send(request, responseBodyHandler);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;

    }

}
