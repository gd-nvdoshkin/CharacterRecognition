import javax.swing.*;
import java.io.*;
import java.util.Random;

public class NeuralNetwork implements Serializable {
    private final double[][] w;
    private final double[] t;
    private static final String alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя.,|!?-«»()";
    private static final int INPUT_SIZE = 1600;
    private static double recognitionLimit = 0.03;
    private static double unrecognitionLimit = 0.05;
    private static final int NEURONS_COUNT = alphabet.length();

    private NeuralNetwork() {
        w = new double[NEURONS_COUNT][INPUT_SIZE];
        t = new double[NEURONS_COUNT];
        Random random = new Random();
        for (int i = 0; i < NEURONS_COUNT; i++) {
            t[i] = random.nextDouble() * 20 + 10;
            for (int j = 0; j < INPUT_SIZE; j++) {
                w[i][j] = random.nextDouble() * 10 + 5;
            }
        }
    }

    public static NeuralNetwork getInstance() {
        try {
            return (NeuralNetwork) new ObjectInputStream(new FileInputStream("ANN.bin")).readObject();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке нейронной сети. Была создана новая сеть", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return new NeuralNetwork();
        }
    }

    public void save() {
        try {
            new ObjectOutputStream(new FileOutputStream("ANN.bin")).writeObject(this);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при сохранении нейронной сети.", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public double[] recognize(int[] x) {
        double[] sums = new double[NEURONS_COUNT];
        double sum = 0;
        for (int i = 0; i < NEURONS_COUNT; i++) {
            sums[i] = -t[i];
            for (int j = 0; j < INPUT_SIZE; j++) {
                sums[i] += w[i][j] * x[j];
            }
            sum += sums[i];
        }
        double[] result = new double[NEURONS_COUNT];
        for (int i = 0; i < NEURONS_COUNT; i++) {
            result[i] = sums[i] / sum;
            //System.out.print(result[i] + " ");
        }
        //System.out.println();
        return result;
    }

    public void teach(int[] input, boolean[] correctOutput) {
        double[] currentOutput = recognize(input);
        boolean correctRecognition = isCorrectRecognition(currentOutput, correctOutput);
        while (!correctRecognition) {
            for (int i = 0; i < NEURONS_COUNT; i++) {
                if (correctOutput[i]) {
                    t[i] += -2 * (1 - currentOutput[i]);
                    for (int j = 0; j < INPUT_SIZE; j++) {
                        w[i][j] += 2 * (1 - currentOutput[i]) * input[j];
                    }
                } else if (currentOutput[i] > unrecognitionLimit) {
                    t[i] += 2 * currentOutput[i];
                    for (int j = 0; j < INPUT_SIZE; j++) {
                        w[i][j] += -2 * currentOutput[i] * input[j];
                    }
                }
            }
            currentOutput = recognize(input);
            correctRecognition = isCorrectRecognition(currentOutput, correctOutput);
        }
    }

    private boolean isCorrectRecognition(double[] currentOutput, boolean[] correctOutput) {
        for (int i = 0; i < NEURONS_COUNT; i++) {
            if ((correctOutput[i] && currentOutput[i] < recognitionLimit) ||
                    (!correctOutput[i] && currentOutput[i] > unrecognitionLimit)) {
                return false;
            }
        }
        return true;
    }

    public static boolean[] getOutputVectorFromLetter(char letter) {
        boolean[] result = new boolean[alphabet.length()];
        for (int i = 0; i < alphabet.length(); i++) {
            result[i] = alphabet.charAt(i) == letter;
        }
        return result;
    }

    public static char getLetterFromVector(double[] vector) {
        double max = Double.MIN_VALUE;
        char result = '\0';
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > max) {
                result = alphabet.charAt(i);
                max = vector[i];
            }
        }
        return result;
    }

    public static int[] squareArrayToLine(int[][] array) {
        int[] result = new int[array.length * array.length];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, result, i * array.length, array[i].length);
        }
        return result;
    }

    public static String getAlphabet() {
        return alphabet;
    }

    public static double getRecognitionLimit() {
        return recognitionLimit;
    }

    public static void setRecognitionLimit(double recognitionLimit) {
        NeuralNetwork.recognitionLimit = recognitionLimit;
    }

    public static double getUnrecognitionLimit() {
        return unrecognitionLimit;
    }

    public static void setUnrecognitionLimit(double unrecognitionLimit) {
        NeuralNetwork.unrecognitionLimit = unrecognitionLimit;
    }
}
