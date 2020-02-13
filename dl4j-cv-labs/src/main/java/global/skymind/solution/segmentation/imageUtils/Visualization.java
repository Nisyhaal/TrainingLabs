package global.skymind.solution.segmentation.imageUtils;

import org.datavec.image.loader.Java2DNativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Visualization {

    private static final int MAX_SAMPLE_DISPLAY = 4;
    private static Logger log = LoggerFactory.getLogger(Visualization.class);
    private static int displayWidth = 256;
    private static int displayHeight = 256;
    private static Java2DNativeImageLoader _Java2DNativeImageLoader;
    private static final String OUTPUT_PATH = "/out/";


    public static JFrame initFrame(String title) {
        JFrame frame = new JFrame();
        frame.setTitle(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        return frame;
    }

    public static JPanel initPanel(JFrame frame, int displaySamples, int inputHeight, int inputWidth, int channels) {
        _Java2DNativeImageLoader = new Java2DNativeImageLoader(inputHeight, inputWidth, channels);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout((displaySamples > MAX_SAMPLE_DISPLAY ? MAX_SAMPLE_DISPLAY : displaySamples), 3, 1, 1));
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
        return panel;
    }

    public static void visualize(INDArray image, INDArray label, INDArray predict, JFrame frame, JPanel panel, int displaySamples, int outputHeight, int outputWidth) {
        panel.removeAll();
        for (int i = 0; i < displaySamples; i++) {
            if (i < image.size(0) && i < MAX_SAMPLE_DISPLAY) {
                panel.add(BufferedImagetoJLabel(_Java2DNativeImageLoader.asBufferedImage(image.slice(i, 0).mul(255))));
                panel.add(BufferedImagetoJLabel(_Java2DNativeImageLoader.asBufferedImage(label.slice(i, 0).mul(255))));
                panel.add(BufferedImagetoJLabel(_Java2DNativeImageLoader.asBufferedImage(predict.slice(i, 0).mul(255))));
            }
        }
        frame.revalidate();
        frame.pack();
    }

    private static BufferedImage INDArraytoBufferedImage(INDArray indarray, int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < w * h; i++) {
            int cls = (int) indarray.getFloat(i);
            bi.setRGB(i % w, i / h, cls * 255);
        }
        return bi;
    }

    private static JLabel BufferedImagetoJLabel(BufferedImage bi) {
        ImageIcon orig = new ImageIcon(bi);
        Image imageScaled = orig.getImage().getScaledInstance((1 * displayWidth), (1 * displayHeight), Image.SCALE_REPLICATE);
        ImageIcon scaled = new ImageIcon(imageScaled);
        return new JLabel(scaled);
    }

    public static void export(File dir, INDArray image, INDArray label, INDArray predict, int count) throws IOException {
        for (int i = 0; i < image.size(0); i++) {
            BufferedImage oriImage = _Java2DNativeImageLoader.asBufferedImage(image.slice(i, 0).mul(255));
            INDArrayIndex[] imageMat = {NDArrayIndex.indices(i), NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.all()}; // defect class
            BufferedImage labelImage = INDArraytoBufferedImage(Nd4j.argMax(label.get(imageMat), 1), 512, 512);
            BufferedImage predictImage = INDArraytoBufferedImage(Nd4j.argMax(predict.get(imageMat), 1), 512, 512);
            ImageIO.write(oriImage, "png", new File(dir.getAbsolutePath() + "\\" + (i + count) + "_image.png"));
            ImageIO.write(labelImage, "png", new File(dir.getAbsolutePath() + "\\" + (i + count) + "_label.png"));
            ImageIO.write(predictImage, "png", new File(dir.getAbsolutePath() + "\\" + (i + count) + "_predict.png"));
        }
    }
}
