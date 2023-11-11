package edu.asu.effortlogger.login;

import edu.asu.effortlogger.login.model.UserCreateStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DbLoginWrapperTest {
    static DbLoginWrapper db;

    @BeforeAll
    static void setUp(@TempDir Path dbBase) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbBase.resolve("test.db"));

        var ps = c.prepareStatement("""
                CREATE TABLE users
                (
                    id                         INT PRIMARY KEY,
                    token                      TEXT not null,
                    name                       TEXT unique not null ,
                    pass_hash                  TEXT not null,
                    consecutive_incorrect_pass INT,
                    access_group               INT
                );
                """);

        ps.execute();
        db = new DbLoginWrapper(c);
    }


    /**
     * Tests the db wrapper to make sure that repeat usernames are correctly
     * disallowed and provide the correct error code.
     *
     * @author sneha
     */
    @Test
    void testRepeatUsername() {
        // initial name
        var repeat = UUID.randomUUID().toString();
        var cr = db.registerUser(repeat, "lorem ipsum", 0);
        assertEquals(cr, UserCreateStatus.SUCCESS);

        // test unrelated users allowed
        cr = db.registerUser(UUID.randomUUID().toString(), "lorem ipsum", 0);
        assertEquals(cr, UserCreateStatus.SUCCESS);

        // repeat name
        cr = db.registerUser(repeat, "lorem ipsum dolor", 0);
        assertEquals(cr, UserCreateStatus.USERNAME_TAKEN);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (!db.conn.isClosed()) {
            db.conn.close();
        }
    }
}