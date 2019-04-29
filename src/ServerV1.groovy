/**
 * Aaron Thomas
 * CS4850: Computer Networks
 * Prof. Culmer
 * Spring 2019
 *
 * ServerV1 contains a server utilizing the Socket API and a file containing a comma-delimited list of strings
 * in the "username,password" format to create a simple one-to-one chat-room with a client using ClientV1. Server
 * commands follow:
 *
 * login [userID] [password]
 * newuser [userID] [password]
 * send [message]
 * logout
 *
 * To use V1 of this chatroom, first run the main function in this class which will initialize the server and await
 * a client connection. Then, run the main function in the ClientV1 class and you will be able to communicate
 * with the server from there.
 */
class ServerV1 extends BaseServer {

    private Socket clientSocket
    private PrintWriter out // Writes to client
    private BufferedReader inr // Reads from client

    private boolean clientAuthenticated = false
    private String clientUserId = null

    static void main(String[] args) {
        ServerV1 server = new ServerV1()
        server.start(PORT)
    }

    void awaitClients() {
        clientSocket = serverSocket.accept()
        out = new PrintWriter(clientSocket.getOutputStream(), true)
        inr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))

        this.handleMessages()
        this.awaitClients() // Handle client until they disconnect, then await another client
    }

    @Override
    void start(int port) {
        serverSocket = new ServerSocket(port)
        println "My chat room server. Version One."
        this.awaitClients()
    }

    void stop(int code) {
        inr.close()
        out.close()
        clientSocket.close()
        serverSocket.close()
        System.exit(code)
    }

    protected void handleMessages() {
        boolean continueHandling = true

        while (continueHandling) {
            String message = inr.readLine()
            if (!message) break

            String[] messageWords = message.split(" ")
            String command = messageWords[0]
            switch (command) {
                case "login":
                    if (messageWords.length != 3) {
                        out.println("Command: login [userID] [password]")
                        break
                    }

                    String username = messageWords[1]
                    String password = messageWords[2]

                    if (!userDB.containsKey(username)) {
                        out.println(SERVER_HEADER + "Denied. No record of user with provided userID.")
                        break
                    }
                    if (!userDB.get(username).equalsIgnoreCase(password)) {
                        out.println(SERVER_HEADER + "Denied. Invalid username/password combination.")
                        break
                    }

                    out.println(SERVER_HEADER + username + " joins")
                    println username + " login."
                    clientAuthenticated = true
                    clientUserId = username
                    break
                case "newuser":
                    if (messageWords.length != 3) {
                        out.println("Command: newuser [userID] [password]")
                        break
                    }
                    if (clientAuthenticated) {
                        out.println(SERVER_HEADER + "Denied. Already logged in.")
                        break
                    }

                    String userID = messageWords[1]
                    String password = messageWords[2]

                    if (userDB.containsKey(userID)) {
                        out.println(SERVER_HEADER + "Denied. User already exists with provided user ID.")
                        break
                    }
                    if (userID.length() > 32) {
                        out.println(SERVER_HEADER + "Denied. User ID must be 32 characters or less.")
                        break
                    }
                    if (password.length() < 4 || password.length() > 8) {
                        out.println(SERVER_HEADER + "Denied. Password must be between 4 and 8 characters.")
                        break
                    }

                    boolean userCreated = this.saveNewUser(userID, password)
                    userCreated ?
                            out.println(SERVER_HEADER + "User created, use 'login [username] [password]' to login.") :
                            out.println(SERVER_HEADER + "Denied. Server unable to create user.")
                    break
                case "send":
                    if (!clientAuthenticated) {
                        out.println(SERVER_HEADER + "Denied. Please login first.")
                        break
                    }
                    if (messageWords.length < 2) {
                        out.println("Command: send [message]")
                        break
                    }

                    String clientMessage = messageWords[1..messageWords.length - 1].join(" ")
                    out.println(clientUserId + ": " + clientMessage)
                    println clientUserId + ": " + clientMessage
                    break
                case "logout":
                    if (!clientAuthenticated) {
                        out.println(SERVER_HEADER + "Denied. No user logged in.")
                        break
                    }
                    if (messageWords.length != 1) {
                        out.println("Command: logout")
                        break
                    }

                    out.println(SERVER_HEADER + clientUserId + " left.")
                    println(clientUserId + " logout.")
                    clientAuthenticated = false
                    clientUserId = null
                    continueHandling = false
                    break
                default:
                    out.println(SERVER_HEADER + "Denied. I do not understand this request.")
            }
        }
    }

}
