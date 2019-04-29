abstract class BaseServer {

    protected ServerSocket serverSocket

    protected HashMap<String, String> userDB = new HashMap<>()

    protected static final USER_FILE_PATH = "./resources/users.txt"
    protected static final SERVER_HEADER = "\033[3mServer:\033[0m "
    protected static final PORT = 10819

    abstract void start(int port);
    abstract void stop(int code);

    BaseServer() {
        userDB = new HashMap<>()

        try {
            this.readUsers()
        } catch (Exception e) {
            println "Error: " + e.getMessage() + ". Exiting.."
            this.stop(-1)
        }
    }

    // Read users from USER_FILE_PATH into userDB
    protected void readUsers() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(USER_FILE_PATH))
        String userInfo = reader.readLine()

        while (userInfo != null) {
            if (userInfo.isEmpty()) {
                // Skip empty lines
                userInfo = reader.readLine()
                continue
            }
            def (String user, String pass) = userInfo.split(",")
            userDB.put(user, pass)
            userInfo = reader.readLine()
        }
    }

    // Save new user in DB and USER_FILE_PATH
    protected boolean saveNewUser(String username, String password) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE_PATH, true))
            writer.newLine() // To avoid writing two users on same line
            writer.append(username + "," + password)
            userDB.put(username, password)
            writer.close()
            return true
        } catch (Exception ignore) {
            return false
        }
    }

}
