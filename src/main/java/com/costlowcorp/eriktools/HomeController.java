/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.costlowcorp.eriktools;

import com.costlowcorp.eriktools.checksum.ChecksumFileController;
import com.costlowcorp.fx.utils.UIUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author ecostlow
 */
public class HomeController implements Initializable {

    @FXML
    private TabPane tabs;

    private File lastFile = new File(System.getProperty("user.home"));

    private FileChooser.ExtensionFilter lastFilter = new FileChooser.ExtensionFilter("Anything", "*.jar", "*.cer", "*.der");

    private final List<FileChooser.ExtensionFilter> filters = Arrays.asList(
            lastFilter,
            new FileChooser.ExtensionFilter("JARs only", "*.jar"),
            new FileChooser.ExtensionFilter("KeyStores", "*.jks", "*.pkcs12")
    );

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void scanSystem(ActionEvent event) {
        final FXMLLoader loader = UIUtils.load(ListKnownKeyStoresController.class);
        final Pane root = loader.getRoot();

        final Tab newTab = new Tab("System", root);
        tabs.getTabs().add(newTab);
    }

    public void openChecksum(ActionEvent e) {
        final FXMLLoader loader = UIUtils.load(ChecksumFileController.class);
        final Pane root = loader.getRoot();

        final Tab tab = new Tab("Checksum", root);
        tabs.getTabs().add(tab);
    }

    public void openFile(ActionEvent event) {
        final FileChooser chooser = new FileChooser();
        if (lastFile != null) {
            final boolean isDir = lastFile.isDirectory();
            chooser.setInitialDirectory(isDir ? lastFile : lastFile.getParentFile());
            chooser.setInitialFileName(isDir ? null : lastFile.getName());
        }

        chooser.getExtensionFilters().addAll(filters);
        chooser.setSelectedExtensionFilter(lastFilter);
        final File check = chooser.showOpenDialog(UIUtils.getWindowFor(tabs));
        lastFilter = chooser.getSelectedExtensionFilter();

        if (check != null) {
            Node newTabRoot;
            try {
                final String filename = check.getName().toLowerCase();
                final int dot = filename.lastIndexOf(".");
                final String extension = dot > 0 ? filename.substring(dot) : filename;
                

                switch (extension) {
                    case ".cer":
                    case ".der":
                        newTabRoot = loadCertificatesFrom(check);
                        break;
                    default:
                        newTabRoot = new Label("Unable to open " + check.getName());
                }
                
            } catch (CertificateException | IOException ex) {
                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                newTabRoot = new Label(ex.getMessage() + " :: " + check.getName());
            }
            final Tab newTab = new Tab(check.getName(), newTabRoot);
            tabs.getTabs().add(newTab);
        }
    }

    private Node loadCertificatesFrom(File file) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final VBox retval = new VBox();
        retval.getChildren().add(new Label("Provider: " + cf.getProvider()));
        retval.getChildren().add(new Label("Type: " + cf.getType()));
        try (InputStream in = new FileInputStream(file)) {
            Collection<? extends Certificate> certs = cf.generateCertificates(in);
            certs.stream().forEach(certificate -> {
                final FXMLLoader loader = UIUtils.load(ShowCertificateController.class);
                final ShowCertificateController controller = loader.getController();
                controller.initilize(certificate, "N/A opened as file");
                retval.getChildren().add(loader.getRoot());
            });
        }

        return retval;
    }

}
