package com.ekkongames.jdacbl.commands;

/**
 * Created by Dolphish on 2016-10-28.
 */
public class CommandInfo {

    private final String[] names;
    private final String helpText;
    private final String moreHelpText;
    private final String usage;
    private final boolean visible;
    private final String authRole;

    private CommandInfo(Builder builder) {
        this.names = builder.names;
        this.helpText = builder.helpText;
        this.moreHelpText = builder.moreHelpText;
        this.usage = builder.usage;
        this.visible = builder.visible;
        this.authRole = builder.authRole;
    }

    public String[] getNames() {
        return names;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getMoreHelpText() {
        return moreHelpText;
    }

    public String getUsage() {
        return usage;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean requiresAuthentication() {
        return !authRole.isEmpty();
    }

    public String getAuthenticationRole() {
        return authRole;
    }

    public static class Builder {

        private String[] names;
        private String helpText;
        private String moreHelpText;
        private String usage;
        private boolean visible;
        private String authRole;

        public Builder() {
            this.names = new String[0];
            this.helpText = "";
            this.moreHelpText = "";
            this.usage = "";
            this.visible = true;
            this.authRole = "";
        }

        public Builder names(String... names) {
            this.names = names;
            return this;
        }

        public Builder summary(String helpText) {
            this.helpText = helpText;
            return this;
        }

        public Builder description(String moreHelpText) {
            this.moreHelpText = moreHelpText;
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Builder auth(String role) {
            this.authRole = role;
            return this;
        }

        public CommandInfo build() {
            return new CommandInfo(this);
        }

    }
}
