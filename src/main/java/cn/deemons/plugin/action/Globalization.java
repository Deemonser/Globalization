package cn.deemons.plugin.action;

import cn.deemons.plugin.ParserUtils;
import cn.deemons.plugin.ui.OptionsDialog;
import cn.deemons.plugin.ui.ProcessDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.io.File;

public class Globalization extends AnAction {

    private ProcessDialog processDialog;
    private VirtualFile file;

    @Override
    public void actionPerformed(AnActionEvent e) {
        file = e.getProject().getBaseDir();

        OptionsDialog dialog = new OptionsDialog();
        dialog.setSize(600, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setListener(this::start);
        dialog.setVisible(true);


    }


    private void start(boolean toTable, boolean upload, boolean isInitPush, boolean download, boolean toXml, boolean replace, boolean delete) {
        processDialog = new ProcessDialog();
        processDialog.setSize(600, 300);
        processDialog.setLocationRelativeTo(null);

        String path = Globalization.this.file.getPath();
        processDialog.addString("\n\n");
        processDialog.addString("FilePath: " + path);
        processDialog.addString("ToTable: " + toTable + " ,upload:" + upload + " ,download:" + download + " ,ToXml:" + toXml + " ,Replace:" + replace + " ,Delete:" + delete);

        new Thread(() -> {
            ParserUtils utils = ParserUtils.INSTANCE;

            File srcFile = new File(path);

            if (toTable) utils.parseXmlToTable(srcFile, s -> {
                SwingUtilities.invokeLater(() -> processDialog.addString(s));
                return null;
            });

            if (upload) utils.parseTableToJson(srcFile, isInitPush, s -> {
                SwingUtilities.invokeLater(() -> processDialog.addString(s));
                return null;
            });

            if (download) utils.getNetData(srcFile, s -> {
                SwingUtilities.invokeLater(() -> processDialog.addString(s));
                return null;
            });

            if (toXml) utils.parseTableToXml(srcFile, s -> {
                SwingUtilities.invokeLater(() -> processDialog.addString(s));
                return null;
            });

            if (replace) utils.replaceFiles(srcFile, s -> {
                SwingUtilities.invokeLater(() -> processDialog.addString(s));
                return null;
            });

            if (delete) utils.deleteFiles(srcFile, s -> {
                SwingUtilities.invokeLater(() -> processDialog.addString(s));
                return null;
            });
            processDialog.addString("\n\nFinish");
            processDialog.setFinishEnable();
        }).start();


        processDialog.setVisible(true);
    }
}
