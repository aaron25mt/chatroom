/**
 * Aaron Thomas
 * CS4850: Computer Networks
 * Prof. Culmer
 * Spring 2019
 *
 * ClientV1 contains a client utilizing the Socket API to connect to a chat-room using the ServerV1 class.
 * Valid commands follow:
 *
 * login [userID] [password]
 * newuser [userID] [password]
 * send [message]
 * logout
 *
 * To use V1 of this chatroom, first run the main function in the ServerV1 class which will initialize the server
 * and await a client connection. Then, run the main function in this class and you will be able to communicate
 * with the server.
 */
class ClientV1 extends BaseClient {

    static void main(String[] args) {
        ClientV1 client = new ClientV1()
        client.connect(IP, PORT, "One")
    }

    void handleMessages() {
        // Makeshift do-while Groovy loop. See https://stackoverflow.com/a/46474198
        while ({
            print "> "
            String message = userInr.readLine()

            try {
                String response = this.sendMessage(message)
                println response
            } catch (SocketException ignore) {
                println "Server disconnected, exiting.."
                this.disconnect(0)
            }

            // Exit condition
            !message.equalsIgnoreCase("logout")
        }());
    }

    String sendMessage(String msg) {
        out.println(msg)
        inr.readLine() // response
    }

}
