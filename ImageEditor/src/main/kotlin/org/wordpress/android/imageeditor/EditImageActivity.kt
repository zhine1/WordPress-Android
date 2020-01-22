package org.wordpress.android.imageeditor

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.NavHostFragment
import com.yalantis.ucrop.UCropFragment.UCropResult
import com.yalantis.ucrop.UCropFragmentCallback

class EditImageActivity : AppCompatActivity(), UCropFragmentCallback {
    private lateinit var hostFragment: NavHostFragment
    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar

    private var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.edit_image_activity)

        bundle = if (savedInstanceState != null) {
            savedInstanceState.getBundle(ARG_BUNDLE)
        } else {
            intent.getBundleExtra(ARG_BUNDLE)
        }

        val contentUri = bundle?.getString(ARG_IMAGE_CONTENT_URI)
        if (TextUtils.isEmpty(contentUri)) {
            delayedFinish()
            return
        }

        progressBar = findViewById(R.id.progress_loading)
        hostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        setupActionBar()
        if (savedInstanceState == null) {
            setupNavGraph()
        }
    }

    private fun setupActionBar() {
        toolbar = findViewById(R.id.toolbar)
        val toolbarColor = ContextCompat.getColor(
            this,
            R.color.black_translucent_40
        )
        val drawable = ColorDrawable(toolbarColor)
        ViewCompat.setBackground(toolbar, drawable)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupNavGraph() {
        val navController = hostFragment.navController
        val graph = navController.navInflater.inflate(R.navigation.mobile_navigation)

        val startDestination = bundle?.getInt(ARG_START_DESTINATION, R.id.home_dest)
        startDestination?.let {
            graph.startDestination = it
        }

        navController.setGraph(graph, bundle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(ARG_BUNDLE, bundle)
    }

    private fun delayedFinish() {
        Handler().postDelayed({ finish() }, 1500)
    }

    override fun onCropFinish(result: UCropResult?) {
        // TODO:
    }

    override fun loadingProgress(showLoader: Boolean) {
        progressBar.visibility = if (showLoader) View.VISIBLE else View.GONE
    }

    companion object {
        const val ARG_BUNDLE = "arg_bundle"

        const val ARG_IMAGE_CONTENT_URI = "arg_image_content_uri"
        const val ARG_START_DESTINATION = "arg_start_destination"

        fun startIntent(context: Context, intent: Intent) {
            val options = ActivityOptionsCompat.makeCustomAnimation(
                    context,
                    R.anim.fade_in,
                    R.anim.fade_out
            )
            ActivityCompat.startActivity(context, intent, options.toBundle())
        }
    }
}
