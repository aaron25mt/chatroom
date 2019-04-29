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
class ServerV2 extends BaseServer {

    private HashSet<ClientHandler> clients
    private HashMap<String, ClientHandler> loggedInClients

    private final int MAX_CLIENTS = 3

    static void main(String[] args) {
        ServerV2 server = new ServerV2()
        server.start(PORT)
    }

    ServerV2() {
        super()

        clients = new HashSet<>()
        loggedInClients = new HashMap<>()
    }

    @Override
    void start(int port) {
        serverSocket = new ServerSocket(port)
        println "My chat room server. Version Two."
        while (true) {
            if (clients.size() < MAX_CLIENTS) {
                ClientHandler client = new ClientHandler(serverSocket.accept())
                client.start()
                clients.add(client)
            }
        }
    }

    void stop(int code) {
        for (ClientHandler client: clients) {
            client.interrupt()
        }
        serverSocket.close()
        System.exit(0)
    }

    private class ClientHandler extends Thread {

        private Socket clientSocket
        private PrintWriter out
        private BufferedReader inr
        private String loggedInUser

        ClientHandler(Socket socket) {
            clientSocket = socket
            out = new PrintWriter(clientSocket.getOutputStream(), true)
            inr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        }

        void run() {
            this.handleMessages()
            this.disconnect()
        }

        void disconnect() {
            clients.remove(this)
            inr.close()
            out.close()
            clientSocket.close()
        }

        void send(String message) {
            out.println(message)
        }

        private void handleMessages() {
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
                            out.println("Denied. No record of user with provided userID.")
                            break
                        }
                        if (!userDB.get(username).equalsIgnoreCase(password)) {
                            out.println("Denied. Invalid username/password combination.")
                            break
                        }

                        for (ClientHandler client: loggedInClients.values()) {
                            client.send(username + " joins")
                        }
                        println username + " login."
                        out.println("Login confirmed.")
                        this.loggedInUser = username
                        loggedInClients.put(username, this)
                        break
                    case "newuser":
                        if (messageWords.length != 3) {
                            out.println("Command: newuser [userID] [password]")
                            break
                        }
                        if (loggedInUser) {
                            out.println("Denied. Already logged in.")
                            break
                        }

                        String userID = messageWords[1]
                        String password = messageWords[2]
                        if (userDB.containsKey(userID)) {
                            out.println("Denied. User already exists with provided user ID.")
                            break
                        }
                        if (userID.length() > 32) {
                            out.println("Denied. User ID must be 32 characters or less.")
                            break
                        }
                        if (password.length() < 4 || password.length() > 8) {
                            out.println("Denied. Password must be between 4 and 8 characters.")
                            break
                        }

                        boolean userCreated = saveNewUser(userID, password)
                        userCreated ?
                                out.println("User created, use 'login [username] [password]' to login.") :
                                out.println("Denied. Server unable to create user.")
                        break
                    case "send":
                        if (!loggedInUser) {
                            out.println("Denied. No user logged in.")
                            break
                        }
                        if (messageWords.length < 3) {
                            out.println("Command: send [all | userID] [message]")
                            break
                        }

                        String header = loggedInUser + ": "
                        String reconstructedMessage = messageWords[2..messageWords.length - 1].join(" ")
                        String dest = messageWords[1]
                        Set<ClientHandler> destUsers = clients

                        if (loggedInUser == dest) {
                            out.println("Denied. You cannot message yourself.")
                            break
                        }

                        if (dest != "all") {
                            // Direct message
                            ClientHandler destUser = loggedInClients.get(dest)
                            if (!destUser) {
                                out.println("Denied. No client found with that userID.")
                                break
                            }

                            destUser.send(header + reconstructedMessage)
                            println loggedInUser + " (to " + dest + "): " + reconstructedMessage
                            break
                        }

                        // Multicast messages
                        for (ClientHandler client: destUsers) {
                            if (client == this) continue // Don't send to self
                            client.send(header + reconstructedMessage)
                        }
                        println header + reconstructedMessage
                        break
                    case "logout":
                        if (!loggedInUser) {
                            out.println("Denied. No user logged in.")
                            break
                        }
                        if (messageWords.length != 1) {
                            out.println("Command: logout")
                            break
                        }

                        loggedInClients.remove(this.loggedInUser)
                        for (ClientHandler client: loggedInClients.values()) {
                            client.send(this.loggedInUser + " left.")
                        }
                        println this.loggedInUser + " logout."
                        this.loggedInUser = null
                        continueHandling = false
                        break
                    case "who":
                        if (!loggedInUser) {
                            out.println("Denied. No user logged in.")
                            break
                        }
                        if (messageWords.length != 1) {
                            out.println("Command: who")
                            break
                        }

                        Set<String> connectedClients = loggedInClients.keySet()
                        out.println(connectedClients.isEmpty() ? "No clients connected." : connectedClients.join(", "))
                        break
                    default:
                        out.println("Denied. I do not understand this request.")
                }
            }
        }

    }

}
