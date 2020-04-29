/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.idea.validator;

import com.android.tools.idea.validator.ValidatorData.Issue;
import com.android.tools.idea.validator.ValidatorData.Level;
import com.android.tools.layoutlib.annotations.NotNull;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * Results of layout validation.
 */
public class ValidatorResult {

    @NotNull private final ImmutableBiMap<Long, View> mSrcMap;
    @NotNull private final ArrayList<Issue> mIssues;

    /**
     * Please use {@link Builder} for creating results.
     */
    private ValidatorResult(BiMap<Long, View> srcMap, ArrayList<Issue> issues) {
        mSrcMap = ImmutableBiMap.<Long, View>builder().putAll(srcMap).build();
        mIssues = issues;
    }

    /**
     * @return the source map of all the Views.
     */
    public ImmutableBiMap<Long, View> getSrcMap() {
        return mSrcMap;
    }

    /**
     * @return list of issues.
     */
    public List<Issue> getIssues() {
        return mIssues;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Result containing ")
                .append(mIssues.size())
                .append(" issues:\n");

        for (Issue issue : mIssues) {
            if (issue.mLevel == Level.ERROR) {
                builder.append(" - [")
                        .append(issue.mLevel.name())
                        .append("] ")
                        .append(issue.mMsg)
                        .append("\n");
            }
        }
        return builder.toString();
    }

    public static class Builder {
        @NotNull public final BiMap<Long, View> mSrcMap = HashBiMap.create();
        @NotNull public final ArrayList<Issue> mIssues = new ArrayList<>();

        public ValidatorResult build() {
            return new ValidatorResult(mSrcMap, mIssues);
        }

    }
}
