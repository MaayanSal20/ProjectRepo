package Server;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * PooledConnection is a small helper class used by the connection pool.
 *
 * It wraps a real JDBC Connection and keeps track of the last time
 * this connection was used.
 *
 * The pool uses this information to decide when a connection has been
 * idle for too long and should be closed.
 */
public class PooledConnection {

	/**
     * The actual JDBC connection to the database.
     */
    private final Connection connection;
    
    /**
     * The last time (in milliseconds) this connection was used.
     */
    private long lastUsed;

    /**
     * Creates a new PooledConnection wrapper around a JDBC connection.
     *
     * When the object is created, the connection is marked as "just used".
     *
     * @param connection the physical JDBC connection to wrap
     */
    public PooledConnection(Connection connection) {
        this.connection = connection;
        touch();
    }

    /**
     * Returns the wrapped JDBC connection.
     *
     * This connection is used by repository classes to run SQL queries.
     *
     * @return the physical JDBC Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Returns the last time this connection was used.
     *
     * The value is a timestamp in milliseconds since epoch.
     *
     * @return the last usage time of the connection
     */
    public long getLastUsed() {
        return lastUsed;
    }

    /**
     * Updates the "last used" timestamp to the current time.
     *
     * This method should be called whenever the connection is
     * taken from or returned to the pool.
     */
    public void touch() {
        lastUsed = System.currentTimeMillis();
    }

    /**
     * Closes the physical JDBC connection.
     *
     * This method is used by the connection pool when a connection
     * is no longer needed (for example, when it has been idle
     * for too long or when the pool is shutting down).
     *
     * @throws SQLException if closing the connection fails
     */
    public void closePhysicalConnection() throws SQLException {
    	if (connection != null && !connection.isClosed())
    		connection.close();
    }
}
