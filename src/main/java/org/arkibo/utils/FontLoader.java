package org.arkibo.utils;

import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FontLoader {

    public static void loadFonts(String resourceFolder) {
        try {
            Path fontDir = Path.of(
                FontLoader.class
                    .getResource(resourceFolder)
                    .toURI()
            );

            try (Stream<Path> paths = Files.walk(fontDir)) {
                paths
                    .filter(Files::isRegularFile)
                    .filter(p ->
                        p.toString().endsWith(".ttf") ||
                        p.toString().endsWith(".otf"))
                    .forEach(p -> {
                        try {
                            Font f = Font.loadFont(
                                Files.newInputStream(p),
                                12
                            );
                            Logger.log("FONT", "Loaded font: " + f.getFamily());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
