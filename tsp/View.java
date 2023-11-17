import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javax.swing.*;

public class View {
    ArrayList<double[]> nodes;
    JFrame frame;
    DrawingCanvas canvas;
    final int WIDTH = 1280;
    final int HEIGHT = 720;
    public View(ArrayList<double[]> nodes, Path path) 
    {
        this.nodes = nodes;
        frame = new JFrame();
        frame.setSize(WIDTH,HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Dots");
        DrawingCanvas canvas = new DrawingCanvas(WIDTH, HEIGHT, nodes, path);
        frame.add(canvas);
        frame.setVisible(true);
    }

    class Points{
        double ScalarWidth;
        double ScalarHeight;
        ArrayList<double[]> nodes;
        public Points(ArrayList<double[]> nodes, int width, int height) {
            this.nodes = nodes;
            ScalarWidth = width;
            ScalarHeight = height;
            for (double[] point : nodes) {
                ScalarWidth  = ScalarWidth  * point[0] > width  ? width  / point[0] : ScalarWidth;
                ScalarHeight = ScalarHeight * point[1] > height ? height / point[1] : ScalarHeight;
            }
            // this.width = width;
            // this.height = height;
        }
        public void drawPoints(Graphics2D g2d){
            g2d.setColor(Color.BLACK);
            for (double[] point : nodes) {
                Ellipse2D.Double p = new Ellipse2D.Double(point[0]*ScalarWidth, point[1]*ScalarHeight, 3,3);
                g2d.fill(p);
            }
        }

        public Path2D.Double drawPath(int[] path){
            double[] point = nodes.get(path[path.length-1]);
            // Line2D.Double l;
            Path2D.Double p = new Path2D.Double();
            p.moveTo(point[0]*ScalarWidth, point[1]*ScalarHeight);
            for (int i = 0; i < path.length; i++) {
                point = nodes.get(path[i]);
                p.lineTo(point[0]*ScalarWidth, point[1]*ScalarHeight); 
            }
            return p;
        }

    }
    class DrawingCanvas extends JComponent{
        int width;
        int height;
        Points ps;
        Path p;
        Path2D.Double P2D;

        public DrawingCanvas(int width, int height, ArrayList<double[]> nodes, Path path){
            this.width = width;
            this.height = height;
            this.p = path;
            ps = new Points(nodes, width, height);
            this.P2D = ps.drawPath(p.getPath());
        }

        protected void paintComponent(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            // Rectangle2D.Double r = new Rectangle2D.Double(20, 40, 30, 55);
            ps.drawPoints(g2d);
            // this.P2D = ps.drawPath(p.getPath());
            g2d.draw(P2D);

            // ps.drawPath(g2d, p.getPath());
            // g2d.setColor(Color.LIGHT_GRAY);
            // g2d.fill(r);
        }
    }

}
