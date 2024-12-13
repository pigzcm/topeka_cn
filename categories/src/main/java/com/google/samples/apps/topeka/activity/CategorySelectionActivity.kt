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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.transition.TransitionInflater
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.samples.apps.topeka.base.R as baseR
import com.google.samples.apps.topeka.categories.R
import com.google.samples.apps.topeka.fragment.CategorySelectionFragment
import com.google.samples.apps.topeka.helper.ActivityLaunchHelper
import com.google.samples.apps.topeka.helper.ApiLevelHelper
import com.google.samples.apps.topeka.helper.REQUEST_LOGIN
import com.google.samples.apps.topeka.helper.REQUEST_SAVE
import com.google.samples.apps.topeka.helper.database
import com.google.samples.apps.topeka.helper.findFragmentById
import com.google.samples.apps.topeka.helper.logout
import com.google.samples.apps.topeka.helper.onSmartLockResult
import com.google.samples.apps.topeka.helper.replaceFragment
import com.google.samples.apps.topeka.helper.requestLogin
import com.google.samples.apps.topeka.model.Player
import com.google.samples.apps.topeka.widget.AvatarView

class CategorySelectionActivity : AppCompatActivity() {
    private var mClickTime: Long? = null
    private var mLastClickTime: Long? = null
    private var needCallExit = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)
        initializePlayer()

        setUpToolbar()
        if (savedInstanceState == null) {
            attachCategoryGridFragment()
        } else {
            setProgressBarVisibility(View.GONE)
        }
        supportPostponeEnterTransition()
    }

    private fun updatePlayerViews(player: Player) {
        findViewById<TextView>(R.id.title).text = player.toString()
        player.avatar?.run { findViewById<AvatarView>(R.id.avatar).setAvatar(this) }
    }

    private fun initializePlayer() = requestLogin { updatePlayerViews(it) }

    private fun setUpToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar_player))
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun attachCategoryGridFragment() {
        replaceFragment(
            R.id.category_container,
            findFragmentById(R.id.category_container) ?: CategorySelectionFragment()
        )
        setProgressBarVisibility(View.GONE)
    }

    private fun setProgressBarVisibility(visibility: Int) {
        findViewById<View>(R.id.progress).visibility = visibility
    }

    override fun onResume() {
        super.onResume()
        (findViewById<TextView>(R.id.score)).text =
            getString(
                com.google.samples.apps.topeka.base.R.string.x_points,
                database().getScore()
            )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(baseR.menu.menu_category, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CategorySelectionFragment.REQUEST_CATEGORY) {
            data?.let {
                findFragmentById(R.id.category_container)?.run {
                    onActivityResult(requestCode, resultCode, data)
                }
            }
        } else if (requestCode == REQUEST_LOGIN || requestCode == REQUEST_SAVE) {
            onSmartLockResult(
                requestCode,
                resultCode,
                data,
                success = { },
                failure = { requestLogin { updatePlayerViews(it) } })
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == baseR.id.sign_out) {
            handleSignOut()
            needCallExit = false
            true
        } else super.onOptionsItemSelected(item)
    }

    @SuppressLint("NewApi")
    private fun handleSignOut() {
        if (ApiLevelHelper.isAtLeast(Build.VERSION_CODES.LOLLIPOP)) {
            window.exitTransition = TransitionInflater.from(this)
                .inflateTransition(R.transition.category_enter)
        }
        logout()
        ActivityLaunchHelper.launchSignIn(this, true)
        supportFinishAfterTransition()
    }

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
        if (needCallExit) kotlin.system.exitProcess(0)
    }

    override fun onBackPressed() = Unit
}
