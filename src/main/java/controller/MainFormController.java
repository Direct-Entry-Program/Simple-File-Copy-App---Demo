package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainFormController {


    public JFXButton btnSelectSource;
    public JFXTextField txtName;
    public JFXTextField txtSize;
    public JFXButton btnSelectDestination;
    public JFXTextField txtDestination;
    public ProgressBar pgrCopy;
    public JFXButton btnCopy;
    public ProgressIndicator pgrcrcl;

    private File sourceFile;

    private File sourceDirectory;

//    private double fileSize;

    private File destinationFolder;

    public void btnSelectSourceOnAction(ActionEvent actionEvent) {
//        FileChooser fileChooser = new FileChooser();
        DirectoryChooser directoryChooser = new DirectoryChooser();

//        fileChooser.setTitle("Choose source file");
        directoryChooser.setTitle("Choose directory");

//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*.*"));

//        sourceFile = fileChooser.showOpenDialog(btnSelectSource.getScene().getWindow());
        sourceDirectory =directoryChooser.showDialog(btnSelectSource.getScene().getWindow());

        String absolutePath = sourceDirectory.getAbsolutePath();
//        String absolutePath = sourceFile.getAbsolutePath();
//        fileSize = (double) (sourceFile.length());


        if (sourceDirectory != null) {
            txtName.setText(absolutePath);
            txtSize.setText(String.valueOf(sourceDirectory.length()));
            btnCopy.setDisable(false);


        } else {
            txtName.setText("Please Select a file");
            return;

        }


    }

    public void btnSelectDestinationOnAction(ActionEvent actionEvent) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select destination folder");

        destinationFolder = directoryChooser.showDialog(btnSelectSource.getScene().getWindow());

        txtDestination.setText(String.valueOf(destinationFolder));


    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {

        new Thread(() -> {

            try {
                File copyFile = new File(destinationFolder, sourceDirectory.getName() + "-Copy");
                if (!copyFile.exists()) {
                    copyFile.mkdir();
                }





                File[] files = sourceDirectory.listFiles();
                for (File file : files) {

                    FileInputStream fis = new FileInputStream(file);

                    File newFile = new File(copyFile, file.getName());
                    newFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(newFile);

                    double filesize = file.length();

                    for (int i = 0; i < filesize; i++) {
                        fos.write(fis.read());


                        int k = i;
                        Platform.runLater(() -> {
                            double progress = (k / filesize);

                            pgrCopy.setProgress(progress);
                            pgrcrcl.setProgress(progress);

                        });


                    }


                    fis.close();
                    fos.close();


                }




                Platform.runLater(() -> {
                    txtDestination.clear();
                    txtSize.clear();
                    txtName.clear();
                    new Alert(Alert.AlertType.INFORMATION, "Copied Succesfully", ButtonType.OK).showAndWait();

                    pgrCopy.setProgress(0);

                });


            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }).start();


    }
}
