# JDACBL

## Preamble
[Discord](https://discordapp.com) is an online chat service, and it is common practice to create "bots": preprogrammed users who can perform automated tasks within the chat. There are many different APIs used to create bots, in many different languages. However, many of these are not easy to use. JDACBL aims to remedy this.

## What is JDACBL?
JDACBL is a command-based API for creating Discord bots in Java. It relies on the popular Java bindings for the Discord API, [JDA](https://github.com/DV8FromTheWorld/JDA). It allows you to quickly and easily create bots that respond to user input.

## How do I use the library?
The API is designed with a simple but scalable API, in an attempt to make creating bots of any size relatively simple. For example, here is how you would create a simple "Hello, world!" command:

```java
public class HelloWorld extends Command {
    public HelloWorld() {
        super(new CommandInfo.Builder()
                .names("helloworld")
                .summary("say \"Hello, world!\"")
                .build());
    }

    @Override
    public void exec(CommandInput input) {
        BotUtils.sendPlainMessage("Hello, world!");
    }
}
```

This class must then be registered in your "entry point" class:
```java
public class MyDiscordBot implements EntryPoint {

    // the login token for this bot
    private static final String AUTH_TOKEN
            = "put your token here";

    @Override
    public BotInfo run() {
        CommandGroup commands = new CommandGroup.Builder()
                .add(new HelloWorld())
                .build();
        return new BotInfo.Builder()
                .setCommandGroup(commands)
                .build(AUTH_TOKEN);
    }

}
```

See [this repository](https://gitlab.com/jfdoming/slavabot) for a more detailed example of how to use the library.

## Documentation
Documentation is under construction.