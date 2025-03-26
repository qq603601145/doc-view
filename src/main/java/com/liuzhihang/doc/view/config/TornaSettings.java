package com.liuzhihang.doc.view.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Torna 的相关设置
 *
 * @author liuzhihang
 * @date 2020/11/22 13:51
 */
@Data
@State(name = "DocViewYApiSettingsComment", storages = {@Storage("DocViewYApiSettings.xml")})
public class TornaSettings implements PersistentStateComponent<TornaSettings> {

    private String url;

    private Long projectId;

    private String token;

    public static TornaSettings getInstance(@NotNull Project project) {
        return project.getService(TornaSettings.class);
    }

    @Override
    public @Nullable
    TornaSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TornaSettings state) {

        XmlSerializerUtil.copyBean(state, this);
    }

}
