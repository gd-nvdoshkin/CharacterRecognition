import javax.swing.*;
import java.io.*;
import java.util.Random;

public class MainInterface {
    private NeuralNetwork neuralNetwork;
    private final Character[] selectionValues;
    private volatile double progress;

    public MainInterface() {
        neuralNetwork = NeuralNetwork.getInstance();
        String alphabet = NeuralNetwork.getAlphabet();
        selectionValues = new Character[alphabet.length()];
        for (int i = 0; i < alphabet.length(); i++) {
            selectionValues[i] = alphabet.charAt(i);
        }
    }

    public void saveAllLettersFromPage(String path) throws IOException {
        ScannedPage scannedPage = new ScannedPage(path);
        File directory = new File("letters");
        Random random = new Random();
        if (!directory.exists() && !directory.mkdir()) {
            throw new IOException("Не возможно создать папку для сохранения букв.");
        }
        while (scannedPage.hasNext()) {
            Letter letter = new Letter(NeuralNetwork.squareArrayToLine(scannedPage.next()));
            Character res = (Character) JOptionPane.showInputDialog(null, "Введите символ, изображенный на картинке.",
                    "Распознайте символ", JOptionPane.PLAIN_MESSAGE, letter.getIcon(), selectionValues, null);
            if (res != null) {
                letter.setOutput(NeuralNetwork.getOutputVectorFromLetter(res));
                File subDirectory = new File(directory, res.toString());
                if (!subDirectory.exists() && !subDirectory.mkdir()) {
                    throw new IOException("Не возможно создать папку для сохранения букв.");
                }
                File file;
                do {
                    file = new File(subDirectory, String.valueOf(Math.abs(random.nextInt())) + ".let");
                }
                while (file.exists());
                letter.save(file);
            }
        }
    }

    public void teach(int count, double recognitionLimit, double unrecognitionLimit)
            throws IOException, ClassNotFoundException {
        Random random = new Random();
        NeuralNetwork.setRecognitionLimit(recognitionLimit);
        NeuralNetwork.setUnrecognitionLimit(unrecognitionLimit);
        File[] directories = new File("letters").listFiles();
        if (directories == null) {
            throw new NullPointerException("Не найдено образцов для обучения.");
        }
        progress = 0;
        for (int i = 0; i < count; i++) {
            int letterNumber = random.nextInt(directories.length);
            File[] examples = directories[letterNumber].listFiles();
            if (examples == null) {
                continue;
            }
            Letter example = Letter.load(examples[random.nextInt(examples.length)]);
            neuralNetwork.teach(example.getInput(), example.getOutput());
            progress = (double) i / (double) count;
        }
        progress = 1;
        neuralNetwork.save();
    }

    public void recognizePage(String pagePath, File result) throws IOException {
        ScannedPage scannedPage = new ScannedPage(pagePath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(result));
        progress = 0;
        while (scannedPage.hasNext()) {
            if (scannedPage.isPutEnter()) {
                writer.write("\r\n");
            } else if (scannedPage.isPutSpace()) {
                writer.write(" ");
            }
            int[] letterVector = NeuralNetwork.squareArrayToLine(scannedPage.next());
            char letter = NeuralNetwork.getLetterFromVector(neuralNetwork.recognize(letterVector));
            writer.write(letter);
            progress = scannedPage.getProgress();
        }
        progress = 1;
        writer.flush();
        writer.close();
    }

    public double getProgress() {
        return progress;
    }

    public void resetProgress() {
        progress = 0;
    }
}
