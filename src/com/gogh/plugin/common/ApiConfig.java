/*
 * Copyright 2016 xiaofeng gogh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gogh.plugin.common;

import com.gogh.plugin.config.Settings;

import java.util.Random;

/**
 * Created by xiaofeng on 9/8/2016.
 */
public class ApiConfig {

    private static final String[] DEFAULT_API_KEY_NAME = {"Easy-Translation", "Dr-Dictionary"};
    private static final String[] DEFAULT_API_KEY_VALUE = {"1525768550", "2053892299"};

    public static String[] getAPISet(){
        String apiKeyName;
        String apiKeyValue;
        int index = getIndex();
        boolean useDefaultKey = Settings.isUseDefaultKey();
        if (useDefaultKey) {
            apiKeyName = DEFAULT_API_KEY_NAME[index];
            apiKeyValue = DEFAULT_API_KEY_VALUE[index];
        } else {
            apiKeyName = Settings.getApiKeyName();
            apiKeyValue = Settings.getApiKeyValue();

            if (IString.isEmpty(apiKeyName) || IString.isEmpty(apiKeyValue)) {
                apiKeyName = DEFAULT_API_KEY_NAME[index];
                apiKeyValue = DEFAULT_API_KEY_VALUE[index];
            }
        }

        return new String[]{apiKeyName, apiKeyValue};
    }

    private static int getIndex(){
        Random random = new Random();// 定义随机类
        int result = random.nextInt(2);// 返回[0,2)集合中的整数，不包括2
        return result;
    }

}
