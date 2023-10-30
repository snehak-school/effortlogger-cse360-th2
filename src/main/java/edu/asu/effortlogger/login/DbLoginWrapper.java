package edu.asu.effortlogger.login;

import edu.asu.effortlogger.model.AuthStatus;
import edu.asu.effortlogger.model.User;
import edu.asu.effortlogger.model.UserAuthResult;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class DbLoginWrapper {
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    static Argon2PasswordEncoder argon2 = new Argon2PasswordEncoder(16, 128, 1, 20000, 3);
    public Connection conn;

    public DbLoginWrapper(Connection conn) {
        this.conn = conn;
    }


    /**
     * Tries to login and returns the result of the login attempt.
     * <p>
     * If the incorrect password is entered, a failed attempt counter is incremented.
     * Too many failed attempts lead to login being blocked.
     *
     * @author Eli Kitch
     */
    public UserAuthResult loginWith(String username, String password) {
        try {
            var sql = "select * from users where name = ?";
            var ps = conn.prepareStatement(sql);

            ps.setString(1, username);

            var r = ps.executeQuery();
            if (!r.next()) {
                return new UserAuthResult(AuthStatus.USER_NOT_FOUND, null, 0);
            }

            final int failedLogins = r.getInt(5);
            if (failedLogins >= MAX_LOGIN_ATTEMPTS) {
                return new UserAuthResult(AuthStatus.TOO_MANY_FAILED_LOGINS, null, failedLogins);
            }

            var hash = r.getString(4);
            if (!argon2.matches(password, hash)) {
                int nc = failedLogins + 1;
                setFailedLoginCt(username, nc);
                return new UserAuthResult(AuthStatus.INCORRECT_PASSWORD, null, nc);
            }

            var u = new User(r.getInt(1), r.getString(2), r.getString(4));
            setFailedLoginCt(username, 0);
            return new UserAuthResult(AuthStatus.SUCCESS, Optional.of(u), 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new UserAuthResult(AuthStatus.UNKNOWN_ERROR, null, 0);
    }


    private void setFailedLoginCt(String username, int ct) throws SQLException {
        var sql = "update users set consecutive_incorrect_pass = ? where name = ?";
        var ps = conn.prepareStatement(sql);

        ps.setInt(1, ct);
        ps.setString(2, username);
        ps.execute();
    }
}
