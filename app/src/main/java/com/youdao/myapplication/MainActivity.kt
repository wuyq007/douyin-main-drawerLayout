package com.youdao.myapplication

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.youdao.myapplication.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
        val drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        setDrawerLayoutScroll()
        setMainLayoutOnTouch()
    }

    private fun setDrawerLayoutScroll() {
        // 设置半透明的黑色背景
        binding.drawerLayout.setScrimColor(Color.parseColor("#40000000"));

        // 添加滑动监听器
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val params = drawerView.layoutParams as DrawerLayout.LayoutParams
                val gravity = params.gravity
                if (gravity == GravityCompat.START) {
                    // 左边的抽屉滑动，设置主布局的偏移
                    binding.appBarMain.appMainLayout.translationX = drawerView.width * slideOffset
                } else if (gravity == GravityCompat.END) {
                    // 右边的抽屉
                    binding.appBarMain.appMainLayout.translationX = -drawerView.width * slideOffset
                }
            }
        })
    }


    private var initialX = 0f
    private var totalDistance = 0f
    private var leftTotalDistance = 0f
    private var rightTotalDistance = 0f

    @SuppressLint("ClickableViewAccessibility")
    private fun setMainLayoutOnTouch() {
        val drawerLeftView: View = binding.drawerLayout.findDrawerWithGravity(GravityCompat.START)
        val drawerRightView: View = binding.drawerLayout.findDrawerWithGravity(GravityCompat.END)
        var drawerLeftWidth = 0
        var drawerRightWidth = 0
        var isLeftDrawerOpen = false
        var isRightDrawerOpen = false

        binding.appBarMain.appMainLayout.setOnTouchListener { view, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 记录手指初始位置
                    initialX = event.x;
                    totalDistance = 0f
                    leftTotalDistance = 0f
                    rightTotalDistance = 0f
                    drawerLeftWidth = drawerLeftView.width
                    drawerRightWidth = drawerRightView.width
                    isLeftDrawerOpen = binding.drawerLayout.isDrawerOpen(GravityCompat.START)
                    isRightDrawerOpen = binding.drawerLayout.isDrawerOpen(GravityCompat.END)
                }
                MotionEvent.ACTION_MOVE -> {
                    //抽屉展示时，不处理
                    if (isLeftDrawerOpen || isRightDrawerOpen) {
                        return@setOnTouchListener true
                    }
                    // 计算手指滑动的距离
                    val currentX = event.x
                    val distance = currentX - initialX
                    // 累计手指滑动的总距离
                    totalDistance += distance
                    // 更新初始位置
                    initialX = currentX
                    if (distance > 0 && totalDistance > 5) {
                        //保证左滑动时，右边抽屉不显示
                        if (rightTotalDistance == 0f) {
                            leftTotalDistance += distance
                            binding.drawerLayout.moveDrawerToOffset(drawerRightView, 0f)
                            drawerRightView.visibility = View.INVISIBLE

                            // 向左滑动
                            var slideOffset = abs(leftTotalDistance) / drawerLeftWidth
                            if (slideOffset > 1) {
                                slideOffset = 1f
                            }
                            if (slideOffset > 0) {
                                binding.drawerLayout.moveDrawerToOffset(drawerLeftView, slideOffset)
                                binding.drawerLayout.updateDrawerState(DrawerLayout.STATE_IDLE, drawerLeftView)
                                drawerLeftView.visibility = View.VISIBLE
                                binding.drawerLayout.invalidate()
                            } else {
                                binding.drawerLayout.moveDrawerToOffset(drawerLeftView, 0f)
                                binding.drawerLayout.updateDrawerState(DrawerLayout.STATE_IDLE, drawerLeftView)
                                drawerLeftView.visibility = View.INVISIBLE
                            }
                        }

                    } else if (distance < 0 && totalDistance < -5) {
                        if (leftTotalDistance == 0F) {
                            //保证右滑动时，左边抽屉不显示
                            rightTotalDistance += distance
                            binding.drawerLayout.moveDrawerToOffset(drawerLeftView, 0f)
                            drawerLeftView.visibility = View.INVISIBLE
                            // 向右滑动
                            var slideOffset = abs(totalDistance) / drawerRightWidth
                            if (slideOffset > 1) {
                                slideOffset = 1f
                            }
                            if (slideOffset > 0) {
                                binding.drawerLayout.moveDrawerToOffset(drawerRightView, slideOffset)
                                binding.drawerLayout.updateDrawerState(DrawerLayout.STATE_IDLE, drawerRightView)
                                drawerRightView.visibility = View.VISIBLE
                                binding.drawerLayout.invalidate()
                            } else {
                                binding.drawerLayout.moveDrawerToOffset(drawerRightView, 0f)
                                binding.drawerLayout.updateDrawerState(DrawerLayout.STATE_IDLE, drawerRightView)
                                drawerRightView.visibility = View.INVISIBLE
                            }
                        }
                    }

                }
                MotionEvent.ACTION_UP -> {
                    //抽屉展示时，不处理
                    if (isLeftDrawerOpen || isRightDrawerOpen) {
                        return@setOnTouchListener true
                    }
                    if (totalDistance > 0) {
                        //左边抽屉显示
                        if (abs(totalDistance) > drawerLeftWidth / 6) {
                            binding.drawerLayout.openDrawer(GravityCompat.START)
                        } else {
                            binding.drawerLayout.closeDrawer(GravityCompat.START)
                        }
                    } else {
                        //右边抽屉显示
                        if (abs(totalDistance) > drawerRightWidth / 6) {
                            binding.drawerLayout.openDrawer(GravityCompat.END)
                        } else {
                            binding.drawerLayout.closeDrawer(GravityCompat.END)
                        }
                    }
                    totalDistance = 0F
                    leftTotalDistance = 0F
                    rightTotalDistance = 0F
                    initialX = 0F
                }
            }
            true
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}