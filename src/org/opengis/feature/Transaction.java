/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source$
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.opengis.feature;

import java.io.IOException;
import java.util.Set;

/**
 * The controller for Transaction with FeatureStore.
 *
 * <p>
 * Shapefiles, databases, etc. are safely modified with the assistance of this
 * interface. Transactions are also to provide authorization when working with
 * locked features.
 * </p>
 *
 * <p>
 * All operations are considered to be working against a Transaction.
 * Transaction.AUTO_COMMIT is used to represent an immediate mode where
 * requests are immidately commited.
 * </p>
 *
 * <p>
 * For more information please see DataStore and FeatureStore.
 * </p>
 *
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 * Transaction t = new DefaultTransaction("handle");
 * t.putProperty( "hint", new Integer(7) );
 * try {
 *     FeatureStore road = (FeatureStore) store.getFeatureSource("road");
 *     FeatureStore river = (FeatureStore) store.getFeatureSource("river");
 *
 *     road.setTransaction( t );
 *     river.setTransaction( t );
 *
 *     t.addAuthorization( lockID );  // provide authoriztion
 *     road.removeFeatures( filter ); // opperate against transaction
 *     river.removeFeature( filter ); // opperate against transaction
 *
 *     t.commit(); // commit opperations
 * }
 * catch (IOException io){
 *     t.rollback(); // cancel opperations
 * }
 * finally {
 *     t.close(); // free resources
 * }
 * </code></pre>
 * <p>
 * Example code walkthrough from the (perspective of Tranasction):
 * </p>
 * <ol>
 * <li>A new transaction is created (an instanceof DefaultTransaction with a handle)</li>
 * <li>A hint is provided using Transaction.putProperty( key, value )</li>
 * <li>Transaction is provided to two FeatureStores, this may result
 *     in several Transaction.State instances being registered</li>
 *     <ul>
 *     <li>TransactionStateDiff (stored by DataStore):
 *         Used for in memory locking is used by many DataStore's
 *         (like ShapefileDataStore).
 *         Lazy creation by AbstractDataStore.state(transaction).
 *         </li>
 *     <li>JDBCTransactionState (stored by ConnectionPool):
 *         Used to manage connection rollback/commit.
 *         Lazy creation as part of JDBCDataStore.getConnection(transaction).
 *         </li>
 *     <li>InProcessLockingManager.FeatureLock (stored by LockingManger):
 *         Used for per transaction FeatureLocks, used to free locked features
 *         on Transaction commit/rollback.
 *         </li>  
 *     </ul>
 *     These instances of Transaction state may make use of any hint provided
 *     to Transaction.putProperty( key, value ) when they are connected with
 *     Transaction.State.setTransaction( transaction ).
 * <li>t.addAuthorization(lockID) is called, each Transaction.State has its
 *     addAuthroization(String) callback invoked with the value of lockID</li>
 * <li>FeatureStore.removeFeatures methods are called on the two DataStores.
 *     <ul>
 *     <li>PostgisFeatureStore.removeFeatures(fitler) handles opperation 
 *         without delegation.
 *         </li>
 *     <li>Most removeFeature(filter) implementations use the implementation
 *         provided by AbstractFeatureStore which delegates to FeatureWriter. 
 *         </li>
 *     </ul>
 *     Any of these opperations may make use of the
 *     Transaction.putProperty( key, value ).
 * <li>The transaction is commited, all of the Transaction.State methods have
 *     there Transaction.State.commit() methods called gicing them a chance
 *     to applyDiff maps, or commit various connections.
 *     </li>
 * <li>The transaction is closed, all of the Transaction.State methods have
 *     there Transaction.State.setTransaction( null ) called, giving them a 
 *     chance to clean up diffMaps, or return connections to the pool.
 *     </li>
 * </ol>
 * @author Jody Garnett
 * @author Chris Holmes, TOPP
 * @version $Id$
 */
public interface Transaction {
    
    /**
     * Marker constant used to indicate immidiate response.
     * <p>
     * The constant is a pure Marker (similar in spirit to a Null Object pattern).
     * All methods do nothing, this constant can be detected by interested parties
     * as a request to perform actions immidiately.
     * </p>
     */
    public static final Transaction AUTO_COMMIT = new AutoCommit();
    
    /**
     * Retrieve a Transaction property held by this transaction.
     * <p>
     * This may be used to provide hints to DataStore implementations, it
     * operates as a blackboard for client, FeatureSource communication.
     * <p>
     * If this proves successful addAuthorization/getAuthorization will be
     * replaced with this mechanism.
     */
    public Object getProperty( Object key );

    /**
     * List of Authorizations IDs held by this transaction.
     * <p>
     * This list is reset by the next call to commit() or rollback().
     * <p>
     * Authorization IDs are used to provide FeatureLock support.
     *
     * @return List of Authorization IDs
     */
    public Set getAuthorizations();

    /**
     * Allows FeatureSource to squirel away information( and callbacks ) for
     * later.
     * <p>
     * The most common example is a JDBC DataStore saving the required
     * connection for later opperations.
     * <pre><code>
     * ConnectionState implements State {
     *     public Connection conn;
     *     public addAuthorization() {}
     *     public commit(){ conn.commit(); }
     *     public rollback(){ conn.rollback(); }
     * }
     * </code></pre>
     * <p>
     * putState will call State.setTransaction( transaction ) to allow State a
     * chance to configure itself.
     *
     * @param key Key used to externalize State
     * @param state Externalized State
     */
    public void putState(Object key, State state);

    /**
     * Allows FeatureSources to clean up information ( and callbacks ) they
     * earlier provided.
     * <p>
     * Care should be taken when using shared State to not remove State
     * required by another FeatureSources.
     * <p>
     * removeState will call State.setTransaction( null ) to allow State a
     * chance cleanup after itself.
     *
     * @param key Key that was used to externalize State
     */
    public void removeState(Object key);

    /**
     * Allows DataStores to squirel away information( and callbacks ) for
     * later.
     * <p>
     * The most common example is a JDBC DataStore saving the required
     * connection for later opperations.
     *
     * @return Current State externalized by key, or <code>null</code> if not
     *         found
     */
    public State getState(Object key);

    /**
     * Makes all transactions made since the previous commit/rollback
     * permanent.
     * <p>
     * FeatureSources will need to issue any changes notifications using a
     * FeatureEvent.FEATURES_CHANGED to all FeatureSources with the same
     * typeName and a different Transaction. FeatureSources with the same
     * Transaction will of been notified of changes as the FeaureWriter made
     * them.
     * <p>
     * Workflows:
     * <ul>
     * <li>LockRequest + Transaction.AUTO_COMMIT returns a LockResponse indicating the success of the opperation and
     * and authoriazation tokens aquired.
     * <li>LockRequest.TRANSACTION_LOCK + Transaction returns LockResponse.TRANSACTION_LOCKRESPONSE indicating
     * a short term lock is held that will expire at the next commit or rollback. Use this workflow to reserve
     * content before starting edits.
     * <li>LockRequest + Transaction returns LockResponse.PENDING, check the result of Commit to discover the success of of any lock
     * methods made during the transaction.
     * </ul>
     * For a discussion of these workflows please read the package javadocs.
     * </p>
     * @return LockResponse for Transaction.AUTO_COMMIT, LockResponse.TRANSACTION_LOCK for a short term lock, or LockResponse.PENDING when used in a Transaction.
     */
    public LockResponse commit() throws IOException;

    /**
     * Undoes all transactions made since the last commit or rollback.
     * <p>
     * FeatureSources will need to issue any changes notifications using a
     * FeatureEvent.FEATURES_CHANGED. This will need to be issued to all
     * FeatureSources with the same typeName and Transaction.
     *
     * @throws IOException if there are problems with the datasource.
     * @throws UnsupportedOperationException if the rollback method is not
     *         supported by this datasource.
     */
    public void rollback() throws IOException;

    /**
     * Provides an Authorization ID for this Transaction.
     * <p>
     * All proceeding modifyFeatures,removeFeature, unLockFeatures, refreshLock
     * and ReleaseLock operations will make use of the provided authorization.
     * <p>
     * Authorization is only maintained until the this Transaction is commited
     * or rolledback.
     * <p>
     * That is operations will only succeed if affected features either:
     * <ul>
     * <li>
     * not locked
     * </li>
     * <li>
     * locked with the provided authID
     * </li>
     * </ul>
     * <p>
     * Authorization ID is provided as a String, rather than a FeatureLock, to
     * account for across process lock use.
     *
     * @param authID
     */
    public void addAuthorization(String authID) throws IOException;

    /**
     * Provides a Transaction property for this Transasction.
     * 
     * <p>
     * All proceeding FeatureSource (for FeatureReader/Writer) opperations may
     * make use of the provided property.
     * </p>
     */
    public void putProperty( Object key, Object value ) throws IOException;
 
    /**
     * Provides an opportunity for a Transaction to free any State it maintains.
     * <p>
     * This method should call State.setTransaction( null ) on all State it
     * maintains.
     * <p>
     * It is hoped that FeatureStore implementations that have externalized
     * their State with the transaction take the opportunity to revert to
     * Transction.AUTO_COMMIT.
     *
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * DataStore implementations can use this interface to externalize the
     * state they require to implement Transaction Support.
     * <p>
     * The commit and rollback methods will be called as required. The
     * intension is that several DataStores can share common transaction state
     * (example: Postgis DataStores sharing a connection to the same
     * database).
     *
     * @author jgarnett, Refractions Reasearch Inc.
     */

    public static interface State {
        /**
         * Provides configuration information for Transaction.State
         * <p>
         * setTransaction is called with non null <code>transaction</code> when
         * Transaction.State is <code>putState</code> into a Transaction. This
         * tranasction will be used to determine correct event notification.
         * <p>
         * setTransaction is called with <code>null</code> when removeState is
         * called (usually during Transaction.close() ).
         *
         * @param transaction
         */
        void setTransaction(Transaction transaction);

        /**
         * Call back used for Transaction.setAuthorization()
         */
        void addAuthorization(String AuthID) throws IOException;

        /**
         * Call back used for Transaction.commit()
         */
        void commit() throws IOException;

        /**
         * Call back used for Transaction.rollback()
         */
        void rollback() throws IOException;
    }
}

/**
 * NullObject indicating AUTO_COMMIT mode.
 * <p>
 * It follows the pattern of "Null Object" or more accuratly "Special Case".
 * </p>
 * @author jgarnett
 */
class AutoCommit implements Transaction {
    /** AutoCommit cannot retain properties */
    public Object getProperty(Object key) {
        return null;
    }
    /** AutoCommit cannot retain authorizations */ 
    public Set getAuthorizations() {
        // TODO Auto-generated method stub
        return null;
    }
    /** AutoCommit cannot retain state - it is infact stateless */
    public void putState(Object key, State state) {
        // TODO Auto-generated method stub        
    }
    /** AutoCommit cannot retain state - it is infact stateless */
    public void removeState(Object key) {
        // TODO Auto-generated method stub
        
    }
    /** AutoCommit cannot retain state - it is infact stateless */
    public State getState(Object key) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /** AutoCommit commits all the time - so this is a NOP */
    public LockResponse commit() throws IOException {
        // No lock requests have been buffered (they were auto commited after all)
        return LockResponse.PENDING;
    }
    /**
     * AutoCommit does not support rollback - it is already too late.
     * 
     * @throws IOException Indicating to client code that rollback cannot be supported
     */
    public void rollback() throws IOException {
        // Conversly we could treat this as a a NOP
        // Justification - the commit has already occured a rollback
        // would acomplish nothing. And nothing is just what we can do.
        throw new IOException("AUTO_COMMIT does not support rollback");
    }
    /** AutoCommit cannot retain authorizations */ 
    public void addAuthorization(String authID) throws IOException {
        throw new IOException("Authorization IDs are not valid for AutoCommit Transaction");
    }

    /**
     * AutoCommit does not support properties.
     * 
     * @throws IOException Indicating to client code that properties are not supported
     */
    public void putProperty(Object key, Object value) throws IOException {
        throw new UnsupportedOperationException("AUTO_COMMIT does not support properties");        
    }

    /** AutoCommit does not maintain State - so this is a NOP */
    public void close() throws IOException {
        // We have no state to clean up after
    }
    
}