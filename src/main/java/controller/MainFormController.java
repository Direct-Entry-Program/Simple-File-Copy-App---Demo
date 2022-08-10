package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.text.NumberFormat;
import java.util.Optional;

public class MainFormController {


    public JFXButton btnSelectSource;
    public JFXTextField txtName;
    public JFXTextField txtSize;
    public JFXButton btnSelectDestination;
    public JFXTextField txtDestination;
    public ProgressBar pgrCopy;
    public JFXButton btnCopy;
    public Label lblCopySize;
    public Label lblPre;
    public ProgressBar pgbTotal;
    public Label lblPreTot;
    public Label lblCopySizeTot;
    private File sourceDirectory;
    private double folderSize;
    private File destinationFolder;
    private int totalFileRead; //for total progress of folder
    public void btnSelectSourceOnAction(ActionEvent actionEvent) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");

        sourceDirectory = directoryChooser.showDialog(btnSelectSource.getScene().getWindow()); //source directory selection

        folderSize = 0;

        if (sourceDirectory != null) {
            txtName.setPromptText("Selected Directory :");
            txtName.setText(sourceDirectory.getAbsolutePath());
            txtName.setUnFocusColor(Color.rgb(2, 142, 188));

            File[] files = sourceDirectory.listFiles(); // get total size in files inside folder
            for (File file : files) {
                folderSize += file.length();
            }

            txtSize.setText(formatNumber(folderSize / 1024.00) + " Kb");


            btnCopy.setDisable(false);


        } else {
            txtName.setPromptText(null);
            txtName.setText("Please Select a directory");
            return;

        }


    }

    public void btnSelectDestinationOnAction(ActionEvent actionEvent) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select destination directory");

        destinationFolder = directoryChooser.showDialog(btnSelectSource.getScene().getWindow());

        if (destinationFolder != null) {
            txtDestination.setText(String.valueOf(destinationFolder));
            txtDestination.setUnFocusColor(Color.rgb(2, 142, 188));

        } else {
            txtDestination.setPromptText(null);
            txtDestination.setText("No destination directory selected");
        }


        btnCopy.setDisable(destinationFolder == null || sourceDirectory == null);


    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {
        File copyFile = new File(destinationFolder, sourceDirectory.getName() + "-Copy");
        if (!copyFile.exists()) {
            copyFile.mkdir();
        } else {
            Optional<ButtonType> result = new Alert(Alert.AlertType.INFORMATION, "Folder called " + sourceDirectory.getName() + "-Copy already exist. Do you want to replace?", ButtonType.YES, ButtonType.NO).showAndWait();
            if (result.get() == ButtonType.NO) {
                return;

            }

        }

        btnCopy.setDisable(true);

        btnSelectDestination.setDisable(true);
        btnSelectSource.setDisable(true);
        pgbTotal.requestFocus();


        var task = new Task<Void>() {


            @Override
            protected Void call() throws Exception {


                File[] files = sourceDirectory.listFiles();
                totalFileRead = 0;

                for (File file : files) {


                    File newFile = new File(copyFile, file.getName());
                    newFile.createNewFile();


                    FileInputStream fis = new FileInputStream(file);
                    FileOutputStream fos = new FileOutputStream(newFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    long filesize = file.length();
                    int totalRead = 0;


                    while (true) {
                        byte[] buffer = new byte[1024 * 10];
                        int read = bis.read(buffer);
                        totalRead += read;
                        if (read == -1) break;
                        bos.write(buffer, 0, read);
                        updateProgress(totalRead, file.length());


                    }
                    totalFileRead += totalRead; //for total progress


                    updateProgress(totalRead, folderSize); //file progress


                    bos.close();
                    bis.close();


                }
                updateProgress(folderSize, folderSize);


                return null;
            }
        };

        task.workDoneProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number curwork) {

                //progress of each file
                pgrCopy.setProgress(curwork.doubleValue() / task.getTotalWork());
                lblPre.setText(("Progress: " + formatNumber(task.getProgress() * 100) + "%"));
                lblCopySize.setText(formatNumber(task.getWorkDone() / 1024.0) + " / " + formatNumber(task.getTotalWork() / 1024.0) + " Kb");

                //total progress
                pgbTotal.setProgress(totalFileRead / folderSize);
                lblCopySizeTot.setText(formatNumber(totalFileRead / 1024.00) + " / " + formatNumber(folderSize / 1024.00) + " Kb");
                lblPreTot.setText(("Total Progress: " + (formatNumber((totalFileRead / folderSize) * 100)) + "%"));

            }
        });


        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {

                pgrCopy.setProgress(0);
                lblCopySize.setText("0 / 0 Kb");
                lblPre.setText("Progress: 0%");
                new Alert(Alert.AlertType.INFORMATION, "Copied Successfully", ButtonType.OK).showAndWait();

                pgbTotal.setProgress(0);

                txtDestination.clear();
                txtSize.clear();
                txtName.clear();


                lblCopySizeTot.setText("0 / 0 Kb");
                lblPreTot.setText("Progress: 0%");
                sourceDirectory = null;
                destinationFolder = null;
                btnSelectSource.setDisable(false);
                btnSelectDestination.setDisable(false);
                btnSelectSource.requestFocus();


            }
        });

        new Thread(task).start();


    }

    private String formatNumber(double input) {
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        numberInstance.setGroupingUsed(true);
        numberInstance.setMaximumFractionDigits(2);
        numberInstance.setMinimumFractionDigits(2);
        return numberInstance.format(input);
    }
}
