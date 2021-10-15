package com.github.wenzewoo.jetbrains.plugin.coderemark.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(
        name = "CodeRemarkConfigService",
        storages = {@Storage("jetbrains.coderemark.xml")}
)
public class CodeRemarkConfigService implements PersistentStateComponent<CodeRemarkConfig> {

    private final CodeRemarkConfig config = new CodeRemarkConfig();

    public static CodeRemarkConfigService getInstance() {
        return CodeRemarkConfigService.Singleton.INSTANCE.getInstance();
    }

    private enum Singleton {
        INSTANCE;
        private final CodeRemarkConfigService instance;

        Singleton() {
            instance = ServiceManager.getService(CodeRemarkConfigService.class);
        }

        public CodeRemarkConfigService getInstance() {
            return instance;
        }
    }

    @NotNull
    @Override
    public CodeRemarkConfig getState() {
        return this.config;
    }

    @Override
    public void loadState(@NotNull final CodeRemarkConfig state) {
        XmlSerializerUtil.copyBean(state, config);
    }
}
