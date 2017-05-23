import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Letter implements Serializable {
    private static final int SQUARE_SIZE = 40;
    private final int[] input;
    private boolean[] output;

    public Letter(int[] input) {
        this.input = input;
    }

    public Icon getIcon() {
        BufferedImage image = new BufferedImage(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        for (int i = 0; i < input.length; i++) {
            if (input[i] == 1) {
                graphics.setColor(Color.BLACK);
            } else {
                graphics.setColor(Color.WHITE);
            }
            graphics.drawLine(i % SQUARE_SIZE, i / SQUARE_SIZE, i % SQUARE_SIZE, i / SQUARE_SIZE);
        }
        return new ImageIcon(image);
    }

    public int[] getInput() {
        return input;
    }

    public boolean[] getOutput() {
        return output;
    }

    public void setOutput(boolean[] output) {
        this.output = output;
    }

    public void save(File file) throws IOException {
        new ObjectOutputStream(new FileOutputStream(file)).writeObject(this);
    }

    public static Letter load(File file) throws IOException, ClassNotFoundException {
        FileInputStream inputStream = new FileInputStream(file);
        ObjectInputStream stream = new ObjectInputStream(inputStream);
        Letter result = (Letter) stream.readObject();
        stream.close();
        inputStream.close();
        return result;
    }
}
