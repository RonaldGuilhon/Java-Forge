package com.javaforge.server.model;

public class ServerConfig {

    public enum ServerType {
        TOMCAT("Tomcat", "catalina.bat", "catalina.sh"),
        WILDFLY("WildFly", "standalone.bat", "standalone.sh"),
        PAYARA("Payara", "asadmin.bat", "asadmin"),
        GLASSFISH("GlassFish", "asadmin.bat", "asadmin"),
        JETTY("Jetty", "jetty.bat", "jetty.sh");

        private final String label;
        private final String winScript;
        private final String unixScript;

        ServerType(String label, String winScript, String unixScript) {
            this.label = label;
            this.winScript = winScript;
            this.unixScript = unixScript;
        }

        public String getLabel() { return label; }
        public String getWinScript() { return winScript; }
        public String getUnixScript() { return unixScript; }
        public String toString() { return label; }
    }

    private ServerType type = ServerType.TOMCAT;
    private String homeDir = "";
    private String deployDir = "";
    private int httpPort = 8080;
    private int debugPort = 5005;
    private String javaHome = System.getProperty("java.home");
    private int minMemory = 256;
    private int maxMemory = 1024;
    private String jvmArgs = "";

    public ServerType getType() { return type; }
    public void setType(ServerType type) { this.type = type; }
    public String getHomeDir() { return homeDir; }
    public void setHomeDir(String homeDir) { this.homeDir = homeDir; }
    public String getDeployDir() { return deployDir; }
    public void setDeployDir(String deployDir) { this.deployDir = deployDir; }
    public int getHttpPort() { return httpPort; }
    public void setHttpPort(int httpPort) { this.httpPort = httpPort; }
    public int getDebugPort() { return debugPort; }
    public void setDebugPort(int debugPort) { this.debugPort = debugPort; }
    public String getJavaHome() { return javaHome; }
    public void setJavaHome(String javaHome) { this.javaHome = javaHome; }
    public int getMinMemory() { return minMemory; }
    public void setMinMemory(int minMemory) { this.minMemory = minMemory; }
    public int getMaxMemory() { return maxMemory; }
    public void setMaxMemory(int maxMemory) { this.maxMemory = maxMemory; }
    public String getJvmArgs() { return jvmArgs; }
    public void setJvmArgs(String jvmArgs) { this.jvmArgs = jvmArgs; }
}
