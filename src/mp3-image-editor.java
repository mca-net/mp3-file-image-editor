import com.mpatric.mp3agic.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;



public class Mp3CoverViewer {

    // Make these static fields so lambdas can modify them freely
    private static byte[] newImageData = null;
    private static String newMimeType = null;
	private static ID3v2 id3v2Tag = null;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose an MP3 file");
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File mp3File = fileChooser.getSelectedFile();
                    Mp3File mp3 = new Mp3File(mp3File);

                    ImageIcon imageIcon = null;
                    id3v2Tag = mp3.getId3v2Tag();


                    if (mp3.hasId3v2Tag()) {
                        id3v2Tag = mp3.getId3v2Tag();
                        byte[] albumImageData = id3v2Tag.getAlbumImage();

                        if (albumImageData != null) {
                            imageIcon = new ImageIcon(albumImageData);
                        }
                    } else {
                        id3v2Tag = new ID3v24Tag();
                        mp3.setId3v2Tag(id3v2Tag);
                    }

                    JFrame frame = new JFrame("MP3 Cover Viewer & Editor");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(400, 400);
                    frame.setLayout(new BorderLayout());

                    JLabel imageLabel = new JLabel();
                    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    imageLabel.setVerticalAlignment(SwingConstants.CENTER);
                    if (imageIcon != null) {
                        imageLabel.setIcon(imageIcon);
                    } else {
                        imageLabel.setText("No image found");
                    }

                    JButton changeImageButton = new JButton("Change/Add Cover Image");
                    JButton saveButton = new JButton("Save MP3");

                    JPanel buttonsPanel = new JPanel();
                    buttonsPanel.add(changeImageButton);
                    buttonsPanel.add(saveButton);

                    frame.add(imageLabel, BorderLayout.CENTER);
                    frame.add(buttonsPanel, BorderLayout.SOUTH);

                    changeImageButton.addActionListener(e -> {
                        JFileChooser imgChooser = new JFileChooser();
                        imgChooser.setDialogTitle("Select Image (JPEG, PNG, GIF)");
                        int imgResult = imgChooser.showOpenDialog(frame);
                        if (imgResult == JFileChooser.APPROVE_OPTION) {
                            File imgFile = imgChooser.getSelectedFile();
                            try {
                                byte[] imgBytes = java.nio.file.Files.readAllBytes(imgFile.toPath());
                                newImageData = imgBytes;

                                String fileName = imgFile.getName().toLowerCase();
                                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                                    newMimeType = "image/jpeg";
                                } else if (fileName.endsWith(".png")) {
                                    newMimeType = "image/png";
                                } else if (fileName.endsWith(".gif")) {
                                    newMimeType = "image/gif";
                                } else {
                                    newMimeType = "image/jpeg"; // fallback
                                }

                                ImageIcon newIcon = new ImageIcon(imgBytes);
                                imageLabel.setIcon(newIcon);
                                imageLabel.setText(null);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(frame, "Failed to read image file.");
                            }
                        }
                    });

                    saveButton.addActionListener(e -> {
                        if (newImageData == null || newImageData.length == 0) {
                            JOptionPane.showMessageDialog(frame, "No new image selected.");
                            return;
                        }
                        try {
                            id3v2Tag.setAlbumImage(newImageData, newMimeType);

                            JFileChooser saveChooser = new JFileChooser();
                            saveChooser.setDialogTitle("Save updated MP3 file");
                            saveChooser.setSelectedFile(new File(mp3File.getParent(), "updated-" + mp3File.getName()));

                            int saveResult = saveChooser.showSaveDialog(frame);
                            if (saveResult == JFileChooser.APPROVE_OPTION) {
                                String outputPath = saveChooser.getSelectedFile().getAbsolutePath();
                                mp3.save(outputPath);
                                JOptionPane.showMessageDialog(frame, "MP3 saved successfully!");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(frame, "Error saving MP3: " + ex.getMessage());
                        }
                    });

                    frame.setVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        });
    }
}
