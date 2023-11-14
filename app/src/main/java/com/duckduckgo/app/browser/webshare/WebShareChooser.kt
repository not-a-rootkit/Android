/*
 * Copyright (c) 2023 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.browser.webshare

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.duckduckgo.js.messaging.api.JsCallbackData
import org.json.JSONObject
import timber.log.Timber

class WebShareChooser : ActivityResultContract<JsCallbackData, JsCallbackData>() {

    lateinit var data: JsCallbackData
    override fun createIntent(
        context: Context,
        input: JsCallbackData,
    ): Intent {
        data = input
        val url = runCatching { input.params.getString("url") }.getOrNull().orEmpty()
        val text = runCatching { input.params.getString("text") }.getOrNull().orEmpty()
        val title = runCatching { input.params.getString("title") }.getOrNull().orEmpty()

        val finalText = if (url.isNotEmpty() && text.isNotEmpty()) {
            "$url $text"
        } else if (url.isNotEmpty()) {
            url
        } else {
            text
        }

        val finalTitle = if (title.isNotEmpty() && text.isNotEmpty()) {
            ""
        } else {
            title
        }

        val getContentIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_TEXT, finalText)
            putExtra(Intent.EXTRA_TITLE, finalTitle)
        }

        return Intent.createChooser(getContentIntent, finalTitle)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): JsCallbackData {
        Timber.d("MARCOS result is $resultCode and $intent")
        if (resultCode == Activity.RESULT_OK) {
            return JsCallbackData(
                featureName = data.featureName,
                method = data.method,
                id = data.id,
                params = JSONObject("""{}"""),
            )
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            return JsCallbackData(
                featureName = data.featureName,
                method = data.method,
                id = data.id,
                params = JSONObject("""{ "failure": {"name":"AbortError", "message":"User aborted"} }"""),
            )
        }
        return JsCallbackData(
            featureName = data.featureName,
            method = data.method,
            id = data.id,
            params = JSONObject("""{ "failure": {"name":"DataError", "message":"Data not found"} }"""),
        )
    }
}
