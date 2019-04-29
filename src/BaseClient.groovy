abstract class BaseClient {

    protected Socket clientSocket
    protected PrintWriter out
    protected BufferedReader inr // Reads server input
    protected BufferedReader userInr // Reads user input

    protected int connectionAttempts = 0

    protected static final IP = "127.0.0.1"
    protected static final PORT = 10819

    abstract void handleMessages();
    abstract String sendMessage(String msg);

    void connect(String ip, int port, String version) {
        try {
            clientSocket = new Socket(ip, port)
        } catch (ConnectException e) {
            if (this.connectionAttempts == 5) {
                println "Couldn't connect to server after 5 attempts, error: " + e.getMessage() + ". Exiting.."
                System.exit(-1)
            }

            int waitTime = (int)Math.min(60, Math.pow(2, this.connectionAttempts++)) // Exponential wait time, capped at 1 min
            println "Couldn't connect to server, will wait " + waitTime + " seconds and try again.."
            Thread.sleep(waitTime * 1000)
            connect(ip, port, version)
        }
        out = new PrintWriter(clientSocket.getOutputStream(), true)
        inr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        userInr = new BufferedReader(new InputStreamReader(System.in))

        println "My chat room client. Version " + version + "."

        this.handleMessages()
        this.disconnect(0)
    }

    void disconnect(int code) {
        userInr.close()
        inr.close()
        out.close()
        clientSocket.close()
        System.exit(code)
    }

}
