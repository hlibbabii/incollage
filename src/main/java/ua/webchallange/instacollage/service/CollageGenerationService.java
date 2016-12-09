package ua.webchallange.instacollage.service;

import org.apache.commons.collections.CollectionUtils;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollageGenerationService {

    private final static int WIDTH_AND_HEIGHT = 640;

    public BufferedImage generate(int height, int width, List<BufferedImage> bufferedImages) {
        if (CollectionUtils.isEmpty(bufferedImages)) {
            throw new IllegalArgumentException();
        }

        int rows = computeRowsNumber(height, width, bufferedImages.size());
        double compressionRate = (double) height / WIDTH_AND_HEIGHT / rows;
        List<BufferedImage> compressedImages
            = bufferedImages.stream().map(im -> scale(im, compressionRate)).collect(Collectors.toList());
        return concatenateImages(compressedImages, height, width, rows);
    }

    private BufferedImage concatenateImages(List<BufferedImage> compressedImages, int height, int width, int rows) {
        BufferedImage collage = new BufferedImage(width, height, compressedImages.get(0).getType());
        int compressedImageHeight = compressedImages.get(0).getHeight();
        int compressedImageWidth = compressedImages.get(0).getWidth();

        int imagesLeftToRender = compressedImages.size();
        int currentColumn = 0;
        while(imagesLeftToRender > 0) {
            for (int i = 0; i < rows && imagesLeftToRender > 0; i++) {
                --imagesLeftToRender;
                collage.createGraphics().drawImage(compressedImages.get(imagesLeftToRender),
                        compressedImageWidth * currentColumn, compressedImageHeight * i, null);
            }
            ++currentColumn;
        }
        return collage;
    }

    private int computeRowsNumber(int height, int width, int size) {
        return (int)Math.ceil(Math.sqrt(size * height / (double) width));
    }

    private BufferedImage scale(BufferedImage bufferedImage, double compressionRate) {
        return Scalr.resize(bufferedImage, Scalr.Method.BALANCED,
                (int) (bufferedImage.getWidth() * compressionRate), (int) (bufferedImage.getHeight() * compressionRate));
    }
}
