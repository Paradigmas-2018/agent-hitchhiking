import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MotoristGui extends JFrame{

	private static final long serialVersionUID = 1L;

	private MotoristAgent myMotorist;
	
	private JTextField qtdCarSeatField, cityField;
	
	public MotoristGui(MotoristAgent motorist) {
		super(motorist.getLocalName());
		
		myMotorist = motorist;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(new JLabel("City:"));
		cityField = new JTextField(15);
		p.add(cityField);
		p.add(new JLabel("Quantity car seat:"));
		qtdCarSeatField = new JTextField(15);
		p.add(qtdCarSeatField);
		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String qtdCarSeat = qtdCarSeatField.getText().trim();
					String city = cityField.getText().trim();
					
					myMotorist.updateCatalogue(city, Integer.parseInt(qtdCarSeat));
					qtdCarSeatField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(MotoristGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myMotorist.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}	
}
