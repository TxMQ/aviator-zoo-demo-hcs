
/*
 * This file is public domain.
 *
 * SWIRLDS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF 
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SWIRLDS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.txmq.aviator.core.Aviator;
import com.txmq.aviator.core.hcs.AviatorHCSConsensus;
import com.txmq.exozoodemo.SocketDemoState;

/**
 * This HelloSwirld creates a single transaction, consisting of the string "Hello Swirld", and then goes
 * into a busy loop (checking once a second) to see when the state gets the transaction. When it does, it
 * prints it, too.
 */
public class ExoZooDemoMain  {
	public static void main(String[] args) {
		try {			
			AviatorHCSConsensus consensus = new AviatorHCSConsensus();
			consensus.initState(SocketDemoState.class);
			Aviator.init(consensus);
			
		} catch (ReflectiveOperationException | HederaNetworkException | HederaStatusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}