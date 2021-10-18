package com.github.wenzewoo.jetbrains.plugin.coderemark.config;

import com.github.wenzewoo.jetbrains.plugin.coderemark.renderer.CodeRemarkRendererState;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CodeRemarkConfigForm implements SearchableConfigurable, Configurable.NoScroll {
    private JPanel rootPanel;
    private JTextField textPrefix;
    private JLabel lblPrefixColor;
    private JLabel lblBodyColor;

    @NotNull
    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Code Remark";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        final CodeRemarkConfig state = CodeRemarkConfigService.getInstance().getState();
        textPrefix.setText(state.getPrefix());
        lblPrefixColor.setText(textPrefix.getText());
        lblPrefixColor.setForeground(CodeRemarkConfig.asColor(state.getPrefixColor()));
        lblBodyColor.setForeground(CodeRemarkConfig.asColor(state.getBodyColor()));
        lblPrefixColor.setFont(
                new Font(lblPrefixColor.getFont().getName(), Font.BOLD, lblPrefixColor.getFont().getSize()));
        lblBodyColor.setFont(
                new Font(lblBodyColor.getFont().getName(), Font.ITALIC, lblBodyColor.getFont().getSize()));

        this.chooseColorListener(lblPrefixColor);
        this.chooseColorListener(lblBodyColor);
        textPrefix.getDocument().addDocumentListener(new DocumentListener() {

            void updatePreview() {
                lblPrefixColor.setText(textPrefix.getText());
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                updatePreview();
            }
        });
        return this.rootPanel;
    }

    private void chooseColorListener(final JLabel label) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                final Color chooseColor = JColorChooser.showDialog(
                        rootPanel, "Choose color", label.getForeground());

                if (null != chooseColor)
                    label.setForeground(chooseColor);
            }
        });
    }

    @Override
    public boolean isModified() {
        final CodeRemarkConfig state = CodeRemarkConfigService.getInstance().getState();

        if (!state.getPrefix().equals(textPrefix.getText()))
            return true;

        if (!state.getBodyColor().equals(CodeRemarkConfig.byColor(lblBodyColor.getForeground())))
            return true;

        return !state.getPrefixColor().equals(CodeRemarkConfig.byColor(lblPrefixColor.getForeground()));
    }

    @Override
    public void apply() {
        CodeRemarkConfigService.getInstance().getState()
                .setPrefix(textPrefix.getText())
                .setPrefixColor(CodeRemarkConfig.byColor(lblPrefixColor.getForeground()))
                .setBodyColor(CodeRemarkConfig.byColor(lblBodyColor.getForeground()));

        CodeRemarkRendererState.getInstance().resetAll();
    }
}
