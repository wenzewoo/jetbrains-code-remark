/*
 * MIT License
 *
 * Copyright (c) 2023 吴汶泽 <wenzewoo@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.wenzewoo.coderemark;

import com.github.wenzewoo.coderemark.toolkit.DigestUtils;
import com.github.wenzewoo.coderemark.toolkit.VirtualFileUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.Serial;
import java.io.Serializable;

public class CodeRemark implements Serializable, Navigatable {
    @Serial
    private static final long serialVersionUID = 5906557169110070235L;
    @Deprecated
    private String projectName;
    private String fileName;
    private String fileUrl;
    private String contentHash; // relativePath.md5() or fileBody.bytes().md5()
    private int lineNumber;
    private String text;

    private transient OpenFileDescriptor target;


    public CodeRemark() {
    }

    public CodeRemark(@NotNull final Project project, @NotNull final VirtualFile file, final int lineNumber, final String text) {
        projectName = project.getName();
        fileName = file.getName();
        fileUrl = file.getUrl();
        contentHash = CodeRemark.createContentHash(project, file);
        this.lineNumber = lineNumber;
        this.text = text;
        target = new OpenFileDescriptor(project, file, lineNumber, -1, true);
    }

    public static Icon getIcon() {
        return AllIcons.General.BalloonInformation;
    }


    public static String createContentHash(@NotNull final String relativePath) {
        return DigestUtils.hashMD5(relativePath.getBytes());
    }

    public static String createContentHash(@NotNull final Project project, @NotNull final VirtualFile file) {
        if (!file.isWritable())
            return DigestUtils.hashMD5(VirtualFileUtils.getContentBytes(file));

        return createContentHash(VirtualFileUtils.getRelativePath(project, file));
    }


    @Override
    public void navigate(final boolean requestFocus) {
        final OpenFileDescriptor target = getTarget(null);
        if (null == target) return;
        target.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        final OpenFileDescriptor target = getTarget(null);
        if (null == target) return false;
        return target.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        final OpenFileDescriptor target = getTarget(null);
        if (null == target) return false;
        return target.canNavigateToSource();
    }


    public OpenFileDescriptor getTarget(@Nullable Project project) {
        // If target is null, indicates that the object is deserialized.
        if (null == target) {
            final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
            if (null == file) return null;

            if (null == project)
                project = ProjectLocator.getInstance().guessProjectForFile(file);

            if (null == project) return null;
            target = new OpenFileDescriptor(project, file, lineNumber, -1, true);
        }
        return target;
    }


    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(final String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(final String contentHash) {
        this.contentHash = contentHash;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "CodeRemark{" +
                "projectName='" + projectName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", contentHash='" + contentHash + '\'' +
                ", lineNumber=" + lineNumber +
                ", text='" + text + '\'' +
                '}';
    }
}