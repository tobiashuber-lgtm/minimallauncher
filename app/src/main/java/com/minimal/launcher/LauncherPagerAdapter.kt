package com.minimal.launcher

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// Reihenfolge: Notizen (links) - Home (Mitte, Startpunkt) - Drawer (rechts)
// So ergibt sich "nahtloses" Wischen in beide Richtungen von ganz allein,
// weil ViewPager2 den Finger-Drag 1:1 zwischen den Seiten uebernimmt.
class LauncherPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NotesFragment()
            1 -> HomeFragment()
            2 -> DrawerFragment()
            else -> HomeFragment()
        }
    }
}
