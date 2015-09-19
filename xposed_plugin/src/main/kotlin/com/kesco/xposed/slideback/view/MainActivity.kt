package com.kesco.xposed.slideback.view

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.kesco.adk.ui.bindViewById
import com.kesco.xposed.slideback.view.adapter.AppAdapter
import com.kesco.xposed.slideback.R
import com.kesco.xposed.slideback.domain.AppInfo
import com.kesco.xposed.slideback.domain.genAppInfo
import java.util.*

public class MainActivity : AppCompatActivity() {

    val rvApps: RecyclerView by bindViewById(R.id.rv_apps)
    val fabAdd: FloatingActionButton by bindViewById(R.id.fab_add)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvApps.layoutManager = LinearLayoutManager(this)

        val packlist = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        val apps = ArrayList<AppInfo>()
        for (pack in packlist) {
            if (pack.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                apps.add(genAppInfo(this, pack))
            }
        }
        val adapter = AppAdapter(this)
        rvApps.adapter = adapter
        adapter.applist = apps
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
