package com.example.android.arcgis.info

import android.net.Uri
import android.net.Uri.parse
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.android.arcgis.databinding.FragmentInfoBinding
import java.net.URI

class InfoFragment : Fragment() {

    companion object {
        private val TAG: String = InfoFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentInfoBinding

    private val args: InfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentInfoBinding.inflate(inflater)

        binding.infoName.text = args.name
        binding.infoDescription.text = args.description
        binding.infoState.text = args.state
        binding.infoUrl.text = args.url
        Glide.with(this)
            .load(parse(args.image))
            .into(binding.infoImage)

        return binding.root
    }
}