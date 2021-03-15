import java.sql.*

class DBHelper(
    val host: String = "localhost",
    val port: Int = 3306,
    val dbName: String,
    val user: String = "root",
    val password: String = "root"
    ) {

    private var stmt: Statement? = null

    private fun connect(){
        stmt?.run{
            if (!isClosed) close()
        }
        var rep = 0
        do {
            try {
                stmt =
                    DriverManager.getConnection("jdbc:mysql://$host:$port/$dbName?serverTimezone=UTC", user, password)
                        .createStatement()
            } catch (e: SQLSyntaxErrorException) {
                val tstmt =
                    DriverManager.getConnection("jdbc:mysql://$host:$port/?serverTimezone=UTC", user, password)
                        .createStatement()
                tstmt.execute("CREATE SCHEMA `$dbName`")
                tstmt.closeOnCompletion()
                rep++
            }
        } while (stmt == null && rep < 2)
    }

    private fun disconnect(){
        stmt?.close()
    }

    fun createDatabase(){
        connect()
        createTables()
        disconnect()
    }

    private fun createTables() {
        stmt?.run{
            addBatch("START TRANSACTION;")
            addBatch("DROP TABLE IF EXISTS `student`")
            addBatch("CREATE TABLE `student` (\n" +
                    "  `ID` int NOT NULL PRIMARY KEY AUTO_INCREMENT,\n" +
                    "  `Lastname` varchar(40) NOT NULL,\n" +
                    "  `Firstname` varchar(40) NOT NULL,\n" +
                    "  `Middlename` varchar(40) DEFAULT NULL,\n" +
                    "  `Group_id` varchar(6) NOT NULL,\n" +
                    "  `Gender` set('М','Ж') NOT NULL,\n" +
                    "  `Birth` date NOT NULL\n" +
                    ");")
            addBatch("DROP TABLE IF EXISTS `acad_group`")
            addBatch("CREATE TABLE `acad_group` (\n" +
                    "  `id` varchar(6) NOT NULL PRIMARY KEY,\n" +
                    "  `kval` int NOT NULL,\n" +
                    "  `plan` int NOT NULL\n" +
                    ")")
            addBatch("ALTER TABLE `student`\n" +
                    "  ADD KEY `Name` (`Lastname`,`Firstname`,`Middlename`),\n" +
                    "  ADD KEY `Group_id` (`Group_id`);")
            addBatch("ALTER TABLE `student`\n" +
                    "  ADD CONSTRAINT `student_ibfk_1` FOREIGN KEY (`Group_id`) REFERENCES `acad_group` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;")
            addBatch("COMMIT;")
            executeBatch()
        }
    }
}