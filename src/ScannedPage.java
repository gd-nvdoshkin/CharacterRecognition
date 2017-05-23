import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ScannedPage implements Iterator<int[][]> {
    private Raster raster;
    private int cursorLeft;
    private int cursorRight;
    private int cursorTop;
    private int cursorBottom;
    private boolean hasNext;
    private boolean putSpace;
    private boolean putEnter;
    private int letterCount;
    private float averageLetterWidth;
    private static final int BLACK_LIMIT = 130;
    private static final float WIDTH_CONSTANT = 0.55f;
    private static final float HEIGHT_CONSTANT = 0.2f;
    private static final float SPACE_CONSTANT = 0.34f;
    private static final int SQUARE_SIZE = 40;
    private final int[][] buffer;
    private static final int BUFFER_SIZE = 3000;

    public ScannedPage(String path) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(path));
        raster = bufferedImage.getRaster();
        cursorBottom = -1;
        averageLetterWidth = 0;
        hasNext = adjustLine();
        putEnter = false;
        putSpace = false;
        buffer = new int[BUFFER_SIZE][BUFFER_SIZE];
    }

    private boolean isBlackPixel(int color[]) {
        return (color[0] < BLACK_LIMIT) && (color[1] < BLACK_LIMIT) && (color[2] < BLACK_LIMIT);
    }

    private boolean horizontalHasBlackPixels(int line, int left, int right) {
        int pixel[] = new int[3];
        for (int x = left; x < right; x++) {
            if (isBlackPixel(raster.getPixel(x, line, pixel))) {
                return true;
            }
        }
        return false;
    }

    private boolean verticalHasBlackPixels(int line, int top, int bottom) {
        int pixel[] = new int[3];
        for (int y = top; y < bottom; y++) {
            if (isBlackPixel(raster.getPixel(line, y, pixel))) {
                return true;
            }
        }
        return false;
    }

    private boolean adjustLine() {
        putEnter = true;
        cursorTop = cursorBottom + 1;
        while (cursorTop < raster.getHeight() && !horizontalHasBlackPixels(cursorTop, 0, raster.getWidth())) {
            cursorTop++;
        }
        cursorBottom = cursorTop;
        while (cursorBottom < raster.getHeight() && horizontalHasBlackPixels(cursorBottom, 0, raster.getWidth())) {
            cursorBottom++;
        }
        cursorRight = -1;
        return cursorTop < raster.getHeight() && adjustCursor();
    }

    private boolean adjustCursor() {
        cursorLeft = cursorRight + 1;
        while (cursorLeft < raster.getWidth() && !verticalHasBlackPixels(cursorLeft, cursorTop, cursorBottom)) {
            cursorLeft++;
        }
        if ((cursorLeft - cursorRight) > (averageLetterWidth * SPACE_CONSTANT)) {
            putSpace = true;
        }
        cursorRight = cursorLeft;
        while (cursorRight < raster.getWidth() && verticalHasBlackPixels(cursorRight, cursorTop, cursorBottom)) {
            cursorRight++;
        }
        if (cursorLeft == raster.getWidth()) {
            return adjustLine();
        } else {
            int letterWidth = cursorRight - cursorLeft;
            if (averageLetterWidth > 0 && letterWidth * WIDTH_CONSTANT > averageLetterWidth) {
                fixRightBorder();
            }
            letterWidth = cursorRight - cursorLeft;
            averageLetterWidth = (averageLetterWidth * letterCount + letterWidth) / (letterCount + 1);
            letterCount++;
            return true;
        }
    }

    private void fixRightBorder() {
        int letterWidth = cursorRight - cursorLeft;
        int letterHeight = cursorBottom - cursorTop;
        int minCount = Integer.MAX_VALUE;
        int minPosition = -1;
        int[] pixel = new int[3];
        for (int x = cursorLeft + (int) (letterWidth * (1 - WIDTH_CONSTANT));
             x < cursorRight - (int) (letterWidth * (1 - WIDTH_CONSTANT)); x++) {
            int count = 0;
            for (int y = cursorTop; y < cursorBottom; y++) {
                count += isBlackPixel(raster.getPixel(x, y, pixel)) ? 1 : 0;
            }
            if (count < minCount) {
                minPosition = x;
                minCount = count;
            }
        }
        if (minCount < letterHeight * HEIGHT_CONSTANT) {
            cursorRight = minPosition;
        }
    }

    private boolean[][] getArrayFromRectangle(int left, int right, int top, int bottom) {
        while (!horizontalHasBlackPixels(top, left, right)) {
            top++;
        }
        while (!horizontalHasBlackPixels(bottom, left, right)) {
            bottom--;
        }
        int size = Math.max(bottom - top, right - left);
        boolean[][] result = new boolean[size][size];
        int leftOffset = (size - right + left) / 2;
        int topOffset = (size - bottom + top) / 2;
        int pixel[] = new int[3];
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                result[y - top + topOffset][x - left + leftOffset] = isBlackPixel(raster.getPixel(x, y, pixel));
            }
        }
        return result;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    public boolean isPutSpace() {
        return putSpace;
    }

    public boolean isPutEnter() {
        return putEnter;
    }

    @Override
    public int[][] next() {
        boolean[][] booleanArray = getArrayFromRectangle(cursorLeft, cursorRight, cursorTop, cursorBottom);
        int[][] result = booleanArrayToResizedIntArray(booleanArray);
        putSpace = false;
        putEnter = false;
        cursorLeft = cursorRight + 1;
        hasNext = adjustCursor();
        return result;
    }

    private int[][] booleanArrayToResizedIntArray(boolean[][] booleanArray) {
        int[][] result = new int[SQUARE_SIZE][SQUARE_SIZE];
        int oldSize = booleanArray.length;
        int bufferSize = SQUARE_SIZE * oldSize;
        for (int i = 0; i < bufferSize; i++) {
            for (int j = 0; j < bufferSize; j++) {
                buffer[i][j] = 0;
            }
        }
        for (int i = 0; i < bufferSize; i++) {
            for (int j = 0; j < bufferSize; j++) {
                buffer[i][j] += booleanArray[i / SQUARE_SIZE][j / SQUARE_SIZE] ? 1 : 0;
            }
        }
        for (int i = 0; i < bufferSize; i++) {
            for (int j = 0; j < bufferSize; j++) {
                result[i / oldSize][j / oldSize] += buffer[i][j];
            }
        }
        for (int i = 0; i < SQUARE_SIZE; i++) {
            for (int j = 0; j < SQUARE_SIZE; j++) {
                result[i][j] = result[i][j] >= (oldSize * oldSize / 2) ? 1 : 0;
            }
        }
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public double getProgress() {
        int area = cursorTop * raster.getWidth() + (cursorBottom - cursorTop) * cursorLeft;
        return (double) area / (double) (raster.getWidth() * raster.getHeight());
    }
}
