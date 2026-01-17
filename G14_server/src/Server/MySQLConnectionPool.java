package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MySQLConnectionPool manages reusable MySQL connections for the server.
 *
 * This class is a Singleton: there is only one pool instance for the whole server.
 *
 * Main idea:
 * - The pool keeps a queue of available connections (PooledConnection objects).
 * - When the server needs a connection, it tries to reuse one from the queue.
 * - If the queue is empty, it creates a new physical MySQL connection.
 * - When a connection is returned, it goes back to the queue (if there is space).
 *
 * The pool also runs a background "cleaner" that checks idle connections.
 * If a connection was not used for too long, it is closed automatically.
 */
public class MySQLConnectionPool {

	/**
     * The singleton instance of the pool (created only once).
     */
    private static MySQLConnectionPool instance;

    /**
     * Database connection settings used when creating new connections.
     */
    private final String dbUrl;
    private final String user;
    private final String pass;

    /**
     * Pool configuration:
     * maxPoolSize = how many connections can be stored in the pool queue.
     * maxIdleTimeMillis = how long a connection may stay unused before closing.
     * checkIntervalSeconds = how often the cleaner checks for idle connections.
     */
    private final int maxPoolSize;
    private final long maxIdleTimeMillis;
    private final long checkIntervalSeconds;

    /**
     * Queue that holds available (idle) pooled connections.
     */
    private final BlockingQueue<PooledConnection> pool;
    
    /**
     * Background service that runs the cleanup task periodically.
     */
    private final ScheduledExecutorService cleanerService;

    /**
     * Private constructor so that only init() can create the singleton instance.
     *
     * @param dbUrl JDBC URL for the MySQL database
     * @param user database username
     * @param pass database password
     * @param maxPoolSize maximum number of connections kept inside the pool queue
     * @param maxIdleTimeMillis max allowed idle time before closing a connection
     * @param checkIntervalSeconds how often to run the cleanup check
     */
    private MySQLConnectionPool(String dbUrl, String user, String pass,
                                int maxPoolSize, long maxIdleTimeMillis, long checkIntervalSeconds) {

        this.dbUrl = dbUrl;
        this.user = user;
        this.pass = pass;

        this.maxPoolSize = maxPoolSize;
        this.maxIdleTimeMillis = maxIdleTimeMillis;
        this.checkIntervalSeconds = checkIntervalSeconds;

        this.pool = new LinkedBlockingQueue<>(maxPoolSize);

     // Start a background timer that checks idle connections every X seconds
        this.cleanerService = Executors.newSingleThreadScheduledExecutor();
        this.cleanerService.scheduleAtFixedRate(
                this::checkIdleConnections,
                checkIntervalSeconds,
                checkIntervalSeconds,
                TimeUnit.SECONDS
        );

        System.out.println("[Pool] Initialized. Max Size: " + maxPoolSize);
    }

    /**
     * Creates the pool instance only once.
     *
     * If init() is called again after the pool already exists,
     * nothing happens (the existing pool remains).
     *
     * @param dbUrl JDBC URL for the MySQL database
     * @param user database username
     * @param pass database password
     * @param maxPoolSize maximum number of connections stored in the pool queue
     * @param maxIdleTimeMillis max allowed idle time before closing a connection
     * @param checkIntervalSeconds how often to run the cleanup check
     */
    public static synchronized void init(String dbUrl, String user, String pass,
                                         int maxPoolSize, long maxIdleTimeMillis, long checkIntervalSeconds) {
        if (instance == null) {
            instance = new MySQLConnectionPool(dbUrl, user, pass, maxPoolSize, maxIdleTimeMillis, checkIntervalSeconds);
        }
    }

    /**
     * Returns the existing pool instance.
     *
     * @return the initialized pool instance
     * @throws IllegalStateException if init() was not called before using the pool
     */
    public static synchronized MySQLConnectionPool getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Connection pool not initialized. Call init() first.");
        }
        return instance;
    }

    /**
     * Gets a connection for database work.
     *
     * Steps:
     * 1) Try to take a connection from the pool queue.
     * 2) If the pool is empty -> create a new physical connection.
     * 3) If reusing an existing connection -> reset its "last used" time.
     *
     * @return a PooledConnection (can be reused later)
     */
    public PooledConnection getConnection() {
        PooledConnection pConn = pool.poll();

        if (pConn == null) {
            System.out.println("[Pool] Queue empty. Creating NEW physical connection!!!");
            return createNewConnection();
        }

        pConn.touch();
        System.out.println("[Pool] Reusing existing connection.");
        return pConn;
    }

    /**
     * Returns a connection back to the pool after finishing DB work.
     *
     * If the pool already has the maximum amount of connections stored,
     * the returned connection is not kept and will be closed instead.
     *
     * @param pConn the pooled connection to return to the pool
     */
    public void releaseConnection(PooledConnection pConn) {
        if (pConn == null) return;

        pConn.touch();
        boolean added = pool.offer(pConn);

        if (added) {
            System.out.println("[Pool] Connection returned. Current Pool Size: " + pool.size());
        } else {
        	// Pool is full, so we do not keep more connections
            try { pConn.closePhysicalConnection(); } catch (Exception ignored) {}
        }
    }

    /**
     * Creates a brand new physical connection to MySQL and wraps it as PooledConnection.
     *
     * @return a new PooledConnection or null if connection failed
     */
    private PooledConnection createNewConnection() {
        try {
            Connection c = DriverManager.getConnection(dbUrl, user, pass);
            return new PooledConnection(c);
        } catch (SQLException e) {
            System.err.println("CONNECTION ERROR DETAILS");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cleanup task: checks all connections that are currently inside the pool queue.
     *
     * If a connection was idle longer than maxIdleTimeMillis, it is closed and removed.
     * Otherwise, it is returned back to the pool queue.
     *
     * This method runs automatically in the background every checkIntervalSeconds.
     */
    private void checkIdleConnections() {
        if (pool.isEmpty()) return;

        System.out.println("[Timer] Checking idle connections...");
        
     // Take all idle connections out of the queue temporarily
        List<PooledConnection> temp = new ArrayList<>();
        pool.drainTo(temp);

        long now = System.currentTimeMillis();
        int closedCount = 0;

        for (PooledConnection pConn : temp) {
            if (pConn == null) continue;

            long idleTime = now - pConn.getLastUsed();
            System.out.println("[Timer] Connection idle for: " + idleTime + " ms");
            
            if (now - pConn.getLastUsed() > maxIdleTimeMillis) {
                try {
                	System.out.println("[Timer] Closing idle connection"); 
                    pConn.closePhysicalConnection();
                    closedCount++;
                } catch (Exception ignored) {}
            } else {
            	// Still valid -> return it back into the pool
                pool.offer(pConn);
            }
        }

        if (closedCount > 0) {
            System.out.println("[Timer] Evicted " + closedCount + " idle connections. Pool Size: " + pool.size());
        }
    }

    /**
     * Shuts down the pool completely.
     *
     * This method stops the background cleaner and closes all connections
     * that are currently in the pool queue.
     *
     * After shutdown, the pool instance becomes null and must be re-initialized
     * if the server starts again.
     */
    public synchronized void shutdown() {
        try {
            cleanerService.shutdownNow();
        } catch (Exception ignored) {}

        pool.forEach(pc -> {
            try { pc.closePhysicalConnection(); } catch (Exception ignored) {}
        });
        pool.clear();

        instance = null;
        System.out.println("[Pool] Shutdown complete.");
    }
}
