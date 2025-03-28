package com.liuzhihang.doc.view.config;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.liuzhihang.doc.view.ui.TornaSettingForm;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Torna 配置
 *
 * @author liuzhihang
 * @date 2021/6/8 17:10
 */
public class TornaSettingsConfigurable implements SearchableConfigurable {

    private TornaSettingForm tornaSettingForm;

    private final Project project;

    public TornaSettingsConfigurable(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String getId() {
        return "liuzhihang.api.doc.TornaSettingsConfigurable";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Torna Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {

        tornaSettingForm = new TornaSettingForm(project);

        return tornaSettingForm.getRootPanel();
    }


    @Override
    public boolean isModified() {

        return tornaSettingForm.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {

        tornaSettingForm.apply();
    }

    @Override
    public void reset() {

        tornaSettingForm.reset();
    }


}
