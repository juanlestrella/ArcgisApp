package com.example.android.arcgis.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.arcgis.databinding.FragmentInfoBinding

class InfoFragment : Fragment() {

    companion object {
        private val TAG: String = InfoFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentInfoBinding.inflate(inflater)

        return binding.root
    }
}