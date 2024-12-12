/*
 * Copyright 2017 Google Inc.
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

package com.google.samples.apps.topeka.activity

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.samples.apps.topeka.base.R
import com.google.samples.apps.topeka.fragment.SignInFragment
import com.google.samples.apps.topeka.helper.ActivityLaunchHelper.Companion.EXTRA_EDIT
import com.google.samples.apps.topeka.helper.isLoggedIn
import com.google.samples.apps.topeka.helper.findFragmentById
import com.google.samples.apps.topeka.helper.replaceFragment

class SignInActivity : AppCompatActivity() {
    private var mClickTime: Long? = null
    private var mLastClickTime: Long? = null
    private var login = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        if (savedInstanceState === null) {
            replaceFragment(
                R.id.sign_in_container,
                SignInFragment.newInstance(isInEditMode || !isLoggedIn())
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (isLoggedIn()) {
            this.login = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Forwarding all results to SignInFragment for further handling.
        findFragmentById(R.id.sign_in_container)?.onActivityResult(requestCode, resultCode, data)
    }

    private val isInEditMode get() = intent.getBooleanExtra(EXTRA_EDIT, false)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            this.mClickTime = System.currentTimeMillis()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            this.mClickTime?.let { time ->
                if (System.currentTimeMillis() - time <= 1000) {
                    if (mLastClickTime === null || (System.currentTimeMillis() - (mLastClickTime ?: -1)) >= 2000L) {
                        Toast.makeText(this, "", Toast.LENGTH_SHORT)
                            .apply {
                                setText("再按一次退出")
                            }.show()
                        mLastClickTime = System.currentTimeMillis()
                    } else {
                        finish()
                    }
                }
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isLoggedIn() || !login) kotlin.system.exitProcess(0)
    }
    override fun onBackPressed() = Unit
}
