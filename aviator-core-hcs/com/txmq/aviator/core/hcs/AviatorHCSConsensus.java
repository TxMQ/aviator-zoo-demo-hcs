package com.txmq.aviator.core.hcs;

import java.io.IOException;
import java.io.Serializable;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.mirror.MirrorClient;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicQuery;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicResponse;
import com.txmq.aviator.config.AviatorConfig;
import com.txmq.aviator.config.model.HCSConfig;
import com.txmq.aviator.core.Aviator;
import com.txmq.aviator.core.AviatorStateBase;
import com.txmq.aviator.core.IAviator;
import com.txmq.aviator.messaging.AviatorMessage;

public class AviatorHCSConsensus extends Aviator implements IAviator {

	private AviatorStateBase state;

	private MirrorClient mirrorClient;
	
	private Client hederaClient;
	
	private ConsensusTopicId topicID;
	
	private AccountId operatorID;
	
	private Ed25519PrivateKey operatorKey;
	
	/**
	 * Constructor.
	 * 
	 * Configures outgoing communications to Hedera, and incoming communications
	 * via streaming API on a mirror node.  Details on where to connect, what
	 * topic(s) to listen to, and the paying account(s) are all configured in 
	 * aviator-config.json
	 */
	public AviatorHCSConsensus() throws HederaNetworkException, HederaStatusException {
		HCSConfig hcsConfig = (HCSConfig) AviatorConfig.get("hcs");
		this.operatorID = AccountId.fromString(hcsConfig.operatorID);
		this.operatorKey = Ed25519PrivateKey.fromString(hcsConfig.operatorKey);
		
		//Initialize Hedera network and mirror node clients
		this.mirrorClient = new MirrorClient(hcsConfig.mirrorNodeAddress);		
		
		if (hcsConfig.useMainnet) {
			this.hederaClient = Client.forMainnet();
		} else {
			this.hederaClient = Client.forTestnet();
		}
		
		this.hederaClient.setOperator(this.operatorID, this.operatorKey);
		
		//TODO:  Support for multiple topics?
		if (hcsConfig.createTopic == true) {
			TransactionId transactionId;
			transactionId = new ConsensusTopicCreateTransaction().execute(this.hederaClient);
			this.topicID = transactionId.getReceipt(this.hederaClient).getConsensusTopicId();
			System.out.println("Create topic ID:  " + this.topicID.toString());
			System.out.println("Record this topic ID in aviator-config.json to re-connect to the same topic");
		} else {
			//Determine topic ID
			String[] topicTokens = hcsConfig.hcsTopicID.split(".");
			this.topicID = new ConsensusTopicId(
					Long.parseLong(topicTokens[0]), 
					Long.parseLong(topicTokens[1]), 
					Long.parseLong(topicTokens[2]));
			
			//TODO: Check if the topic exists - try to create it if it does not?
		}
		
		//Set up out topic responder - route incoming messages to this.handleTransaction()
		new MirrorConsensusTopicQuery()
			.setTopicId(this.topicID)
			.subscribe(
					mirrorClient, 
					response -> { this.handleTransaction(response); }, 
					Throwable::printStackTrace
			);
	}
	
	/**
	 * This method will be called in response to an incoming HCS message
	 */
	protected void handleTransaction(MirrorConsensusTopicResponse topicResponse) {
		//Unpack the incoming transaction and route it through the pipeline
		//TODO: might also have to be GUnzipped, see note in createTransactionImpl()
		try {
			AviatorMessage<?> message = AviatorMessage.deserialize(topicResponse.message);
			getPipelineRouter().routeExecuteConsensus(message, state);
		} catch (IOException | ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This method packages up a transaction originating from the application and 
	 * sends it through HCS for consensus ordering.  After the network agrees on
	 * the ordering, the transaction will be returned to us (and all other listening 
	 * nodes) through the mirror node, ultimately arriving at the handleTransaction()
	 * method above.
	 * 
	 * @param transaction
	 * @throws IOException
	 */
	@Override
	public void createTransactionImpl(AviatorMessage<? extends Serializable> transaction) throws IOException {
		getPipelineRouter().routeMessageReceived(transaction, this.state);
		if (transaction.isInterrupted() == false) {
			//Submit to HCS.
			try {
				new ConsensusMessageSubmitTransaction()
					.setTopicId(this.topicID)
					.setMessage(transaction.serialize())
					.execute(this.hederaClient)
					.getReceipt(this.hederaClient);
				
				//Notify the framework that we've submitted to the network
				getPipelineRouter().notifySubmitted(transaction);
				
				//TODO:  Submit pre-consensus?
			} catch (HederaNetworkException | HederaStatusException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void initState(AviatorStateBase state) {
		this.state = state;		
	}

	@Override
	public void initState(Class<? extends AviatorStateBase> stateClass) {
		try {
			this.state = stateClass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("An error was encountered while instantiating the state class");
		}		
	}

	@Override
	public AviatorStateBase getStateImpl() {
		return this.state;
	}

	@Override
	public int getBasePortImpl() {
		// TODO Auto-generated method stub
		return 50204;
	}

	@Override
	public String getNodeNameImpl() {
		return "node";
	}

}
