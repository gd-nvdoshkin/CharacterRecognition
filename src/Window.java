import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class Window extends JFrame {
    private MainInterface mainInterface;

    private void recognize() {
        setVisible(false);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Укажите файл с текстом");
        chooser.setAcceptAllFileFilterUsed(false);
        FileFilter jpgFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".jpg") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "*.jpg | JPEG images";
            }
        };
        chooser.setFileFilter(jpgFilter);
        File input;
        if (chooser.showDialog(null, "Выбрать") != JFileChooser.APPROVE_OPTION) {
            setVisible(true);
            return;
        } else {
            input = chooser.getSelectedFile();
        }
        chooser.removeChoosableFileFilter(jpgFilter);
        chooser.setSelectedFile(new File("result.txt"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Directories only";
            }
        });
        chooser.setDialogTitle("Укажите, куда сохранить результат");
        File result;
        if (chooser.showDialog(null, "Сохранить") == JFileChooser.APPROVE_OPTION) {
            result = chooser.getSelectedFile();
        } else {
            setVisible(true);
            return;
        }
        try {
            showProgressBar();
            mainInterface.recognizePage(input.getPath(), result);
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, "Возникла ошибка при распознавании.", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }
        setVisible(true);
    }

    private void prepareLetters() {
        setVisible(false);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Укажите файл с текстом");
        chooser.setAcceptAllFileFilterUsed(false);
        FileFilter jpgFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".jpg") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "*.jpg | JPEG images";
            }
        };
        chooser.setFileFilter(jpgFilter);
        File input;
        if (chooser.showDialog(null, "Выбрать") != JFileChooser.APPROVE_OPTION) {
            setVisible(true);
            return;
        } else {
            input = chooser.getSelectedFile();
        }
        try {
            mainInterface.saveAllLettersFromPage(input.getPath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Возникла ошибка при подготовке страницы.", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        setVisible(true);
    }

    private void teach() {
        setVisible(false);
        Double recognitionLimit = 0.03;
        Double unrecognitionLimit = 0.04;
        Integer count = 100000;
        try {
            count = Integer.valueOf((String) JOptionPane.showInputDialog(null, "Введите число итераций:", "Обучение",
                    JOptionPane.PLAIN_MESSAGE, null, null, count));
            recognitionLimit = Double.valueOf((String) JOptionPane.showInputDialog(null, "recognitionLimit:", "Обучение",
                    JOptionPane.PLAIN_MESSAGE, null, null, recognitionLimit));
            unrecognitionLimit = Double.valueOf((String) JOptionPane.showInputDialog(null, "unrecognitionLimit:",
                    "Обучение", JOptionPane.PLAIN_MESSAGE, null, null, unrecognitionLimit));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Данные введены не корректно!", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            setVisible(true);
            return;
        }
        try {
            showProgressBar();
            mainInterface.teach(count, recognitionLimit, unrecognitionLimit);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ошибка при обучении.", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            setVisible(true);
            return;
        }
        setVisible(true);
    }

    private void showProgressBar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mainInterface.resetProgress();
                JFrame progressFrame = new JFrame("Идёт обработка...");
                JProgressBar progressBar = new JProgressBar(0, 100);
                progressFrame.getContentPane().add(progressBar);
                progressFrame.setResizable(false);
                progressFrame.setMinimumSize(new Dimension(400, 50));
                progressFrame.setLocationRelativeTo(null);
                progressFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                progressFrame.pack();
                progressFrame.setVisible(true);

                while (mainInterface.getProgress() < 1) {
                    progressBar.setValue((int) Math.floor(mainInterface.getProgress() * 100));
                    Thread.yield();
                }
                progressFrame.setVisible(false);
                progressFrame.dispose();
            }
        }).start();
    }

    public Window() {
        super("Распознавание текста в изображениях");

        mainInterface = new MainInterface();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container content = getContentPane();

        final JButton recognizeButton = new JButton("Распознать текст в изображении");
        final JButton prepareLettersButton = new JButton("Подготовить новую страницу для обучения");
        final JButton teachButton = new JButton("Провести обучение на приготовленных образцах");

        recognizeButton.setEnabled(true);
        teachButton.setEnabled(true);
        prepareLettersButton.setEnabled(true);

        recognizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        recognize();
                    }
                }).start();
            }
        });

        teachButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        teach();
                    }
                }).start();
            }
        });

        prepareLettersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        prepareLetters();
                    }
                }).start();
            }
        });

        content.add(recognizeButton, BorderLayout.NORTH);
        content.add(prepareLettersButton, BorderLayout.CENTER);
        content.add(teachButton, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(500, 0));
        setLocationRelativeTo(null);
        pack();
        setResizable(false);
    }
}
