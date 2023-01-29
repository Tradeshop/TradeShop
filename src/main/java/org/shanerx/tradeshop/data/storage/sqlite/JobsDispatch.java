package org.shanerx.tradeshop.data.storage.sqlite;

import org.shanerx.tradeshop.TradeShop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class JobsDispatch {

    private DatabaseManager db;
    private TradeShop plugin;

    private volatile Thread dispatcherThread;
    private volatile Queue<PreparedStatement> jobs;

    protected JobsDispatch(DatabaseManager dbm, TradeShop plugin) {
        this.db = dbm;
        this.plugin = plugin;

        assertMainThread();

        jobs = new LinkedList<>();
    }

    public boolean isIdling() {
        return dispatcherThread == null || !dispatcherThread.isAlive();
    }

    private void assertMainThread() {
        if (Thread.currentThread().getId() != TradeShop.threadId)
            throw new UnsupportedOperationException("JobsDispatch methods can only be invoked in the main thread");
    }

    private void assertDispatcherThread() {
        if (dispatcherThread == null || dispatcherThread.getId() != Thread.currentThread().getId())
            throw new UnsupportedOperationException("JobsDispatch methods can only be invoked in the main thread");
    }

    public void enqueueJob(PreparedStatement stmt) {
        jobs.add(stmt);
    }

    public void runDispatcher() {
        if (dispatcherThread == null)
            dispatcherThread = new Thread(new RunnableDispatcher());
        else if (dispatcherThread.isAlive())
            return;

        dispatcherThread.start();
    }

    private class RunnableDispatcher implements Runnable {

        private volatile boolean jobActive = false;

        void dispatchNextJob() throws SQLException {
            assertDispatcherThread();
            if (jobs.isEmpty())
                return;
            else if (jobActive)
                throw new UnsupportedOperationException("Active job exists!");

            jobActive = true;

            PreparedStatement stmt = jobs.poll();
            Connection conn = stmt.getConnection();
            if (stmt.isClosed() || conn.isClosed() || !conn.isValid(0)) {
                throw new IllegalStateException("PreparedStatement connection is closed");
            }
            stmt.execute();
            stmt.close();

            jobActive = false;
        }

        @Override
        public void run() {
            assertDispatcherThread();

            while (!jobs.isEmpty()) {
                try {
                    dispatchNextJob();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
