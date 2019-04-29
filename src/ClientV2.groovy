/**
 * Aaron Thomas
 * CS4850: Computer Networks
 * Prof. Culmer
 * Spring 2019
 *
 * ClientV2 contains a client utilizing the Socket API to connect to a chat-room using the ServerV2 class.
 * Valid commands follow:
 *
 * login [userID] [password]
 * newuser [userID] [password]
 * send all [message]
 * send [userID] [message]
 * who
 * logout
 *
 * To use V2 of this chatroom, first run the main function in the ServerV2 class which will initialize the server
 * and await a client connection. Then, run the main function in this class and you will be able to communicate
 * with the server.
 */
class ClientV2 extends BaseClient {

    private boolean promptPrinted = false

    static void main(String[] args) {
        ClientV2 client = new ClientV2()
        client.connect(IP, PORT, "Two")
    }

    void handleMessages() {
        // Makeshift do-while Groovy loop. See https://stackoverflow.com/a/46474198
        while ({
            if (!promptPrinted) {
                // Check if user sent message in last iteration to avoid duplicate prompts
                print "> "
                promptPrinted = true
            }

            if (inr.ready()) {
                // Server message available
                println inr.readLine()
                promptPrinted = false
            }

            String message = ""
            if (userInr.ready()) {
                // User sent a message, send to server and print response
                message = userInr.readLine()

                try {
                    String response = this.sendMessage(message)
                    if (response) println response // No response, reprint prompt
                    promptPrinted = false
                } catch (SocketException ignore) {
                    println "Server disconnected, exiting.."
                    this.disconnect(0)
                }
            }

            // Exit condition
            !message.equalsIgnoreCase("logout")
        }());

        println "Disconnecting from server, exiting client.."
    }

    String sendMessage(String msg) {
        out.println(msg)
        if (msg.startsWith("send")) return null // No server response for sent msgs
        inr.readLine()
    }

}
