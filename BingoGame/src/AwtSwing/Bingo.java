package AwtSwing;

//같은버튼을 두번 눌러도 로직이 돌아감.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import javax.sound.sampled.*;

public class Bingo {
	
	static JPanel panelNorth; //Top view 정보등
	static JPanel panelCenter; //Game view 실제게임화면
	static JLabel labelMessage;
	static JButton[] buttons = new JButton[16]; //4byte*4byte
	static String[]  images = {
			"fruit1.png","fruit2.png",         			
			"fruit3.png","fruit4.png",        			
			"fruit5.png","fruit6.png",        			
			"fruit7.png","fruit8.png",   
			
			"fruit1.png","fruit2.png",         			
			"fruit3.png","fruit4.png",        			
			"fruit5.png","fruit6.png",        			
			"fruit7.png","fruit8.png",      
			};
	
	static class MyFrame extends JFrame implements ActionListener{
		public MyFrame(String title) {
			super(title);
			this.setLayout(new BorderLayout());
			this.setSize(400,500);
			this.setVisible(true);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			initUI(this);  //화면 스크린 초기화 세팅
			mixCard();
			
			this.pack();  // 여백 없애기 _ Pack Empty Space
		}
		
		//오디오
		static void playSound(String filename) {
			File file = new File("./wav/"+filename);
			if(file.exists()) {
				try {
					AudioInputStream stream = AudioSystem.getAudioInputStream(file);
					Clip clip = AudioSystem.getClip();
					clip.open(stream);
					clip.start();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}else {
				System.out.println("File Not Found!");
			}
		}
		
		//게임로직을 위해서 선언: 오픈한 카드가 같으면 열려있고 아니면 닫힘
		static int openCount = 0;  //opened card count : 0, 1, 2
		static int buttonindexSave1 = 0;  //first opened card : 0~15
		static int buttonindexSave2 = 0;  //2nd opened card : 0~15
		static Timer timer; //javax.swing.Timer임
		static int tryCount = 0; //시도횟수
		static int successCount = 0; //성공횟수 : bingo count : 0~8	
		
		@Override
		public void actionPerformed(ActionEvent e) {
//			System.out.println("Button clicked!");
			
			//2개 열렸다면 더이상 카드가 열리지 않도록
			if(openCount == 2) {
				return;
			}
			
			JButton btn = (JButton)e.getSource(); //button객체 가져옴
			int index = getButtonIndex(btn);
//			System.out.println("Button index: " + index);
			
			//실제로 눌렸을때 이미지 표현
			btn.setIcon(changeImage(images[index]));
			
			openCount++;
			if(openCount == 1) {  //첫번째 카드라면
				 buttonindexSave1 = index;
			}else if (openCount == 2){
				buttonindexSave2 = index;
				tryCount++;
				labelMessage.setText("Find Same Fruit! " + "Try "+tryCount);
				
				//판정로직
				boolean isBingo = checkCard(buttonindexSave1,buttonindexSave2);
				if(isBingo == true) {
					playSound("success.wav");
					openCount = 0;
					successCount++;
					if(successCount == 8) {
						labelMessage.setText("Game Over "+"Try "+tryCount);
					}
				}else {
					backToQuestion();
				}
			}
		}
		
		public void backToQuestion() {
			//1초 후에 카드가 뒤집히게 : 사람이 인식
			timer = new Timer(500, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Timer");
					
					playSound("fail.wav");
					openCount = 0;
					buttons[buttonindexSave1].setIcon(changeImage("question.png"));
					buttons[buttonindexSave2].setIcon(changeImage("question.png"));
					timer.stop();
				}
			});
			
			timer.start();
		}
		
		public boolean checkCard(int index1, int index2) {
			if(index1 == index2) {
				return false;
			}
			if(images[index1].equals(images[index2])) {
				return true;
			}else {
				return false;
			}
		}
		
		public int getButtonIndex(JButton btn) {//버튼 눌렀을때 인덱스가져오기
			int index = 0;
			for(int i=0; i<16; i++) {
				if(buttons[i] == btn) {  //버튼이 같은지
					 index=i;
				}
			}
			return index;
		}
	}
	
	static void initUI(MyFrame MyFrame) {
		panelNorth = new JPanel();
		panelNorth.setPreferredSize(new Dimension(400,100));
		panelNorth.setBackground(Color.BLUE);
		labelMessage = new JLabel("Find Same Fruit!" + "Try 0");
		labelMessage.setPreferredSize(new Dimension(400,100));
		labelMessage.setForeground(Color.WHITE);
		labelMessage.setFont(new Font("Monaco",Font.BOLD,20));
			//panelNorth에 들어가는 메세지가 중앙정렬
			labelMessage.setHorizontalAlignment(JLabel.CENTER); 
		panelNorth.add(labelMessage);
		MyFrame.add("North",panelNorth);
		
		panelCenter = new JPanel();
		panelCenter.setLayout(new GridLayout(4,4));
		panelCenter.setPreferredSize(new Dimension(400,400));
		for(int i=0; i<16; i++) {
			buttons[i] = new JButton();
			buttons[i].setPreferredSize(new Dimension(100,100));
				//아이콘(이미지넣음) 메소드로 이미지 넣기
				buttons[i].setIcon(changeImage("question.png"));
			buttons[i].addActionListener(MyFrame); //class에 액션리스너(interface)
			panelCenter.add(buttons[i]);	
		}
		MyFrame.add("Center",panelCenter);
	}
	
	//카드 섞기
	static void mixCard() {
		Random rand = new Random();
		for(int i=0; i<1000; i++) {
			int random = rand.nextInt(15) + 1; //0~14가 섞이기 때문에 +1
			//스왑
			String temp = images[0];
			images[0] = images[random];
			images[random]=temp;
		}
	}
	
	//change이미지 메소드 정의 : 버튼에 '물음표'아이콘이 들어감
	static ImageIcon changeImage(String filename) {
		ImageIcon icon = new ImageIcon("./img/"+filename);
		Image originImage = icon.getImage();
		Image changedImage = originImage.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
		ImageIcon icon_new = new ImageIcon(changedImage);
		return icon_new;
	}
	
	public static void main(String[] args) {
		
		new MyFrame("Bingo Game");

	}
	
}
