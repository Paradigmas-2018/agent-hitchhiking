import jade.core.behaviours.*;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MotoristAgent extends Agent {
	private static final long serialVersionUID = 1L;
	
	private Travel travel;
	// The GUI by means of which the user can add hitchhiking
	private MotoristGui myGui;
	
	protected void setup() {
		
		myGui = new MotoristGui(this);
		myGui.showGui();
		
		// Register the hitch-offer service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("hitchhiking");
		sd.setName("JADE-hitchhiking");
		dfd.addServices(sd);	
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		// Add the behaviour serving queries from passengers agents 
		addBehaviour(new OfferRequestsServer());
			
		// Add the behaviour serving purchase orders from buyer agents
		// Servidor de Pedidos de Caronas
		addBehaviour(new PurchaseOrdersServer());
	}

	
	/**
	   Inner class OfferRequestsServer.
	   This is the behaviour used by Motorists agents to serve incoming requests 
	   for offer from passengers agents.
	   If the requested car seat is in the local catalogue the seller agent replies 
	   with a PROPOSE message specifying the price. Otherwise a REFUSE message is
	   sent back.
	 */
	//FIPA PROTOCOLS: http://www.fipa.org/specs/fipa00030/
	private class OfferRequestsServer extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String destination = msg.getContent();
				ACLMessage reply = msg.createReply();

				boolean canHitchhiking = travel.getDestinationCity().equals(destination);
				if (canHitchhiking == true && travel.getQuantitySeat() > 0) {
					// The requested destination is available for hitchhicking. Reply with qtdSeat
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(travel.getQuantitySeat().intValue()));
				}
				else {
					// The hitchhicking is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}

	}// End of inner class OfferRequestsServer
	
	/**
	   Inner class PurchaseOrdersServer.
	   This is the behaviour used by Motorists agents to serve incoming 
	   offer acceptances (i.e. purchase orders) from passengers agents.
	   The motorist agent removes the hitch hicking from its catalogue 
	   and replies with an INFORM message to notify the buyer that the
	   hitch hicking has been sucesfully completed.
	 */
	// Servidor de Pedidos de Compras
	private class PurchaseOrdersServer extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String destination = msg.getContent();
				ACLMessage reply = msg.createReply();

				travel.setQuantitySeat(travel.getQuantitySeat()-1);
				if (travel != null) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println(destination+" match to "+msg.getSender().getName());
				}
				else {
					// The requested book has been sold to another buyer in the meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	
	/**
    This is invoked by the GUI when the user adds a new car seat for available carpool
	 */
	public void updateCatalogue(final String actualCity, final String destinationCity, final int qtdCarSeat) {
		addBehaviour(new OneShotBehaviour() {

			private static final long serialVersionUID = 1L;

			public void action() {
				
				travel = new Travel(actualCity, destinationCity, qtdCarSeat);
				System.out.println(travel+" inserted into catalogue. Car Seat = "+qtdCarSeat);
			}
		} );
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Close the GUI
		myGui.dispose();
		// Printout a dismissal message
		System.out.println("Motorist-agent "+getAID().getName()+" terminating.");
	}
}
