package se.mah.kd405a_group1.medea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.ImageIcon;

public class ScreenGUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScreenGUI frame = new ScreenGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ScreenGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		System.out.println("JFrame Width: "+width+" Height: "+height);
		this.setBounds(0, 0, (int)width, (int)height); 
		this.setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.PINK);
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblMylabel = new JLabel("InfoScreen - GRUPP1");
		lblMylabel.setForeground(Color.BLACK);
		lblMylabel.setBackground(SystemColor.inactiveCaptionBorder);
		lblMylabel.setFont(new Font("Century Gothic", lblMylabel.getFont().getStyle(), 29));
		lblMylabel.setBounds(50, 321, 450, 154);
		contentPane.add(lblMylabel);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(ScreenGUI.class.getResource("/se/mah/kd405a_group1/medea/res/medea_logo.jpg")));
		lblNewLabel.setBounds(50, 50, 210, 259);
		contentPane.add(lblNewLabel);
	}
}
