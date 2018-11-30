
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PassengerAgent extends Agent {
	private static final long serialVersionUID = 1L;
	
	private String targetQtdCarSeat;
	private AID[] motoristAgents;
	
	// Put agent initializations here
		protected void setup() {
			// Printout a welcome message
			System.out.println("Hallo! Passenger-agent "+getAID().getName()+" is ready.");

			// Get the title of the book to buy as a start-up argument
			Object[] args = getArguments();
			if (args != null && args.length > 0) {
				targetQtdCarSeat = (String) args[0];
				System.out.println("Target quantity car seat is " + targetQtdCarSeat);

				// Add a TickerBehaviour that schedules a request to motorists agents every 10 seconds
				addBehaviour(new TickerBehaviour(this, 10000) {

					private static final long serialVersionUID = 1L;

					protected void onTick() {
						System.out.println("Trying to hitchhiking with "+targetQtdCarSeat +" car seat");
						// Update the list of seller agents
						DFAgentDescription template = new DFAgentDescription();
						ServiceDescription sd = new ServiceDescription();
						sd.setType("hitchhiking");
						template.addServices(sd);
						
						try {
							DFAgentDescription[] result = DFService.search(myAgent, template); 
							System.out.println("Found the following motorists agents:");
							motoristAgents = new AID[result.length];
							
							for (int i = 0; i < result.length; ++i) {
								motoristAgents[i] = result[i].getName();
								System.out.println(motoristAgents[i].getName());
							}
						}
						catch (FIPAException fe) {
							fe.printStackTrace();
						}

						// Perform the request
						myAgent.addBehaviour(new RequestPerformer());
					}
				} );
			}
			else {
				// Make the agent terminate
				System.out.println("No target car seat specified");
				doDelete();
			}
		}
		
		/**
		   Inner class RequestPerformer.
		   This is the behaviour used by Passenger agents to request seller 
		   agents the target qtd car seat.
		 */
		private class RequestPerformer extends Behaviour {

			private static final long serialVersionUID = 1L;
			private AID bestMotorist; // The agent who provides the best offer 
			private int bestCarSeat;  // The best offered car seat
			private int repliesCnt = 0; // The counter of replies from seller agents
			private MessageTemplate mt; // The template to receive replies
			private int step = 0;

			public void action() {
				switch (step) {
				case 0:
					// Send the cfp to all sellers
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < motoristAgents.length; ++i) {
						cfp.addReceiver(motoristAgents[i]);
					} 
					cfp.setContent(targetQtdCarSeat);
					cfp.setConversationId("hitchhiking");
					cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("hitchhiking"),
							MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 1;
					break;
				case 1:
					// Receive all proposals/refusals from seller agents
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							// This is an offer 
							int qtdCarSeat = Integer.parseInt(reply.getContent());
							System.out.println(qtdCarSeat);
							System.out.println(bestMotorist);
							if (bestMotorist == null || qtdCarSeat > bestCarSeat) {
								// This is the best offer at present
								bestCarSeat = qtdCarSeat;
								bestMotorist = reply.getSender();
							}
						}
						repliesCnt++;
						if (repliesCnt >= motoristAgents.length) {
							// We received all replies
							step = 2; 
						}
					}
					else {
						block();
					}
					break;
				case 2:
					// Send the purchase order to the seller that provided the best offer
					ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					order.addReceiver(bestMotorist);
					order.setContent(targetQtdCarSeat);
					order.setConversationId("hitchhiking");
					order.setReplyWith("order"+System.currentTimeMillis());
					myAgent.send(order);
					// Prepare the template to get the purchase order reply
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("hitchhiking"),
							MessageTemplate.MatchInReplyTo(order.getReplyWith()));
					step = 3;
					break;
				case 3:      
					// Receive the purchase order reply
					reply = myAgent.receive(mt);
					if (reply != null) {
						// Purchase order reply received
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// Purchase successful. We can terminate
							System.out.println(targetQtdCarSeat+" successfully purchased from agent "+reply.getSender().getName());
							System.out.println("Car Seat = "+bestCarSeat);
							myAgent.doDelete();
						}
						else {
							System.out.println("Attempt failed: there are no rides available.");
						}

						step = 4;
					}
					else {
						block();
					}
					break;
				}        
			}

		
		
		public boolean done() {
			
			if (step == 2 && bestMotorist == null) {
				System.out.println("Attempt failed: "+targetQtdCarSeat+" not available for sale");
			}
			
			boolean bookIsNotAvailable = (step == 2 && bestMotorist == null);
			boolean negotiationIsConcluded = (step == 4);
			
			boolean isDone = false;
			if (bookIsNotAvailable || negotiationIsConcluded) {
				isDone = true;
			}
			else {
				isDone = false;
			}
			
			return isDone;
			//return ((step == 2 && bestSeller == null) || step == 4);
		}
	}  // End of inner class RequestPerformer

}
