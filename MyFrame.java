import java.awt.*;  
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.event.*;  
import java.util.Random;

// The canvas on which we will draw the world
class MyNewCanvas extends Canvas {
    World myWorld;
    public MyNewCanvas(World tt) {  
        myWorld = tt;
        setBackground (Color.gray);  
        setSize((int)(tt.WSIZE / 2.0), (int)(tt.WSIZE / 2.0));  
    }  
    public void paint(Graphics G){
        // The green oval is there for no reason at all.
        G.setColor(Color.green); G.fillOval(10, 50, 20, 10);
                G.setColor(Color.white);
        for (FoodBit f: myWorld.food)
        {
            G.fillOval((int)(f.x / 2.0), (int)(f.y / 2.0), 4, 4);
        }

        for (Agent a: myWorld.population)
        {
            a.draw(G);
        }
    }
}

public class MyFrame extends Frame implements ActionListener {
    World myWorld;
    TextField tf1;
    Label scorelabel;
    Button b1,b2, b3, b4; 
    Random R;
    MyNewCanvas cnv;

    MyFrame(World ww)
    {
        myWorld = ww;
        int numarg = 0;
        tf1=new TextField();  
        tf1.setText(Integer.toString(myWorld.delay));
        b1=new Button("+"); b2=new Button("-"); b3=new Button("Save population");  b4 = new Button("Read population");
        scorelabel = new Label(); scorelabel.setText("Score: 0");
        scorelabel.setPreferredSize(new Dimension(100, 25));
        b1.addActionListener(this); b2.addActionListener(this); b3.addActionListener(this); b4.addActionListener(this);
        add(tf1);add(b1);add(b2); add(scorelabel); //add(b3); add(b4);
        cnv = new MyNewCanvas(this.myWorld);
        add(cnv);
        addWindowListener ( new WindowAdapter() {
            public void windowClosing ( WindowEvent evt ) {
                /*if (myWorld.outputfilewriter != null)
                    myWorld.outputfilewriter.close();*/
                System.exit ( 0 );
            }
        } );
        setLayout(new FlowLayout());  
        pack();
        setVisible(true);  
    }         
    public void actionPerformed(ActionEvent e) {
        // The buttons control the waiting delay between refreshes.
        if(e.getSource()==b1){  
            myWorld.delay+=20;  
        }else if(e.getSource()==b2){  
            if (myWorld.delay >= 20)
                myWorld.delay-=20;  
        }            
        tf1.setText(Integer.toString(myWorld.delay));
    }
}
